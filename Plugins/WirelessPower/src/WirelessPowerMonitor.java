/*
 *  Copyright (C) 2010 Apropos
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class WirelessPowerMonitor {
    private Map<String,Transmitter> watched = new HashMap<String,Transmitter>();
    private Map<Transmitter.Type,HashMap<String,ArrayList<Transmitter>>> transmitters = new HashMap();
    private Map<String,Integer> owned = new HashMap<String,Integer>();
    private WirelessPowerConfig config;
    private WirelessPowerDataSource datasource;

    public WirelessPowerMonitor(WirelessPowerDataSource localdatasource){
        this.transmitters.put(Transmitter.Type.RECEIVER, new HashMap<String,ArrayList<Transmitter>>());
        this.transmitters.put(Transmitter.Type.TRANSMITTER, new HashMap<String,ArrayList<Transmitter>>());
        this.transmitters.put(Transmitter.Type.REPEATER, new HashMap<String,ArrayList<Transmitter>>());
        this.datasource = localdatasource;
    }

    public void loadConfig(){
        config = new WirelessPowerConfig(this);
        owned.clear();
    }

    public void addTransmitter(Transmitter transmitter){
        watchTransmitter(transmitter);
        Transmitter.Type blockType = transmitter.getType();
        String channel = transmitter.getChannel();
        if(!this.transmitters.get(blockType).containsKey(channel)){
            this.transmitters.get(blockType).put(channel, new ArrayList<Transmitter>());
        }
        this.transmitters.get(blockType).get(channel).add(transmitter);
        if(!owned.containsKey(transmitter.getOwner().toLowerCase())){
            owned.put(transmitter.getOwner().toLowerCase(), 0);
        }
        owned.put(transmitter.getOwner().toLowerCase(),owned.get(transmitter.getOwner().toLowerCase())+1);
    }

    public void removeTransmitter(Transmitter transmitter){
        unwatchTransmitter(transmitter);
        Transmitter.Type blockType = transmitter.getType();
        String channel = transmitter.getChannel();
        HashMap<String, ArrayList<Transmitter>> map = transmitters.get(blockType);
        if(map.containsKey(channel)){
            map.get(channel).remove(transmitter);
            if(map.get(channel).isEmpty()){
                map.remove(channel);
            }
        }
        transmitter.destruct();
        owned.put(transmitter.getOwner().toLowerCase(),owned.get(transmitter.getOwner().toLowerCase())-1);
        delete(transmitter);
    }

    private void addBlock(Block block,Transmitter transmitter){
        String blockString = this.datasource.getBlockString(block);
        if(!this.watched.containsKey(blockString)){
            this.watched.put(blockString,transmitter);
        }
    }

    private void addBlocks(Block[] blocks, Transmitter transmitter){
        for(Block block : blocks){
            addBlock(block,transmitter);
        }
    }

    private void removeBlock(Block block){
        String blockString = this.datasource.getBlockString(block);
        if(this.watched.containsKey(blockString)){
            this.watched.remove(blockString);
        }
    }

    private void removeBlocks(Block[] blocks){
        for(Block block : blocks){
            removeBlock(block);
        }
    }

    public void watchTransmitter(Transmitter transmitter){
        addBlocks(transmitter.getBlocks(),transmitter);
    }

    public void unwatchTransmitter(Transmitter transmitter){
        removeBlocks(transmitter.getBlocks());
    }

    public void removeTransmitterFromBlock(Block block){
        Transmitter transmitter = belongsTo(block);
        if(transmitter != null){
            removeTransmitter(transmitter);
        }
    }

    public Transmitter belongsTo(Block block){
        Transmitter result = null;
        String blockString = this.datasource.getBlockString(block);
        if(this.watched.containsKey(blockString)){
            result = this.watched.get(blockString);
        }
        return result;
    }

    public List<Transmitter> getNearbyTransmitters(Transmitter transmitter){
        List<Transmitter> neighbors = new ArrayList<Transmitter>();
        List<Transmitter> matches = new ArrayList<Transmitter>();
        String owner = transmitter.getOwner();
        double range = transmitter.getRange();
        String channel = transmitter.getChannel();
        Transmitter.Type blockType = transmitter.getType();
        if(blockType != Transmitter.Type.TRANSMITTER){
            if(transmitters.get(Transmitter.Type.TRANSMITTER).containsKey(channel)) matches.addAll(transmitters.get(Transmitter.Type.TRANSMITTER).get(channel));
        }
        if(blockType != Transmitter.Type.RECEIVER){
            if(transmitters.get(Transmitter.Type.RECEIVER).containsKey(channel)) matches.addAll(transmitters.get(Transmitter.Type.RECEIVER).get(channel));
        }
        if(transmitters.get(Transmitter.Type.REPEATER).containsKey(channel)) matches.addAll(transmitters.get(Transmitter.Type.REPEATER).get(channel));
        for(Transmitter compare : matches){
            double distance = transmitter.calcDist(compare);
            if(blockType == Transmitter.Type.REPEATER){
                if(compare.getRange() > transmitter.getRange()){
                    range = compare.getRange();
                }else{
                    range = transmitter.getRange();
                }
            }else if(blockType == Transmitter.Type.RECEIVER){
                range = compare.getRange();
            }
            if(distance <= range && distance > 0.0){
                neighbors.add(compare);
            }
        }
        return neighbors;
    }

    public boolean isWatched(Block block){
        boolean result = false;
        if(this.watched.containsKey(this.datasource.getBlockString(block))){
            result = true;
        }
        return result;
    }
    
    public boolean isWatched(Transmitter transmitter){
        boolean result = false;
        if(this.transmitters.get(transmitter.getType()).get(transmitter.getChannel()).contains(transmitter)){
            result = true;
        }
        return result;
    }

    public void createTransmitter(Player player, Sign sign){
        String signText = sign.getText(1).toLowerCase();
        boolean bool = (signText.equals("[transmit]") || signText.equals("[receive]") || signText.equals("[repeat]"));
        if(bool){
            if(config.checkPermission(player,WirelessPowerConfig.Event.createTransmitter)){
                boolean isPrivate = sign.getText(3).equalsIgnoreCase("[Private]");
                String channelText = sign.getText(2).toLowerCase();
                Block[] blocks = getBlocks(sign); //[0]baseBlock, [1]torchBlock, [2]signBlock
                if(!isWatched(blocks[0])){
                    if(blocks[0].getType() != 12 && blocks[0].getType() != 13){
                        if(blocks[1].getType() == 75 || blocks[1].getType() == 76){
                            if(blocks[1].getData() == 0x5){
                                if(config.checkPermission(player,WirelessPowerConfig.Event.createChannel) || channelExists(channelText)){
                                    if(!isWatched(blocks[0]) && !isWatched(blocks[0]) && !isWatched(blocks[2])){
                                        Transmitter.Type blockType;
                                        if(signText.equalsIgnoreCase("[transmit]")){
                                            blockType = Transmitter.Type.TRANSMITTER;
                                        }else if(signText.equalsIgnoreCase("[repeat]")){
                                            blockType = Transmitter.Type.REPEATER;
                                        }else{
                                            blockType = Transmitter.Type.RECEIVER;
                                        }
                                        Transmitter transmitter = createTransmitter(player.getName(),blocks,blockType,channelText);
                                        player.sendMessage("§7Transmitter successfully created on channel '"+channelText+"'!");
                                        addTransmitter(transmitter);
                                        save(transmitter);
                                        transmitter.update();
                                    }else{
                                        belongsTo(blocks[1]).update();
                                    }
                                }else{
                                    player.sendMessage("§7Could not create new wireless channel. Transmitter not created.");
                                }
                            }else{
                                player.sendMessage("§cTorch must be standing on block. Transmitter not created.");
                            }
                        }else{
                            player.sendMessage("§cTorch not found. Transmitter not created.");
                        }
                    }else{
                        player.sendMessage("§cCannot make transmitters on sand or gravel.");
                    }
                }
            }else{
                    player.sendMessage("§cYou have reached your limit on transmitters.");
            }
        }
    }

    public Transmitter createTransmitter(String owner, Block[] blocks, Transmitter.Type blockType,
            String channel){
        Double power = 0.0;
        if(blockType != Transmitter.Type.RECEIVER){
            power = config.getBasePower();
                power = power*config.getBoost(blocks[0]);
        }
        return new Transmitter(this, owner, blocks, blockType, channel, power);
    }

    private Block[] getBlocks(Sign sign){
        Block[] blocks = new Block[3];
        Block signBlock = sign.getBlock();
        int data = signBlock.getData();
        int xmod = 0;
        int zmod = 0;
        switch(data){
            case 5:
                xmod = -1;
                break;
            case 2:
                zmod = 1;
                break;
            case 4:
                xmod = 1;
                break;
            case 3:
                zmod = -1;
                break;
        }
        blocks[0] = etc.getServer().getBlockAt(signBlock.getX()+xmod,signBlock.getY(),signBlock.getZ()+zmod); //baseBlock
        blocks[1] = etc.getServer().getBlockAt(signBlock.getX()+xmod,signBlock.getY()+1,signBlock.getZ()+zmod); //torchBlock
        blocks[2] = signBlock; //signBlock
        return blocks;
    }

    public boolean channelExists(String name){
        return (transmitters.get(Transmitter.Type.TRANSMITTER).containsKey(name) || transmitters.get(Transmitter.Type.RECEIVER).containsKey(name));
    }

    public void load(){
        this.datasource.load(this);
    }

    public void save(Transmitter transmitter){
        this.datasource.saveTransmitter(transmitter);
    }
    public void delete(Transmitter transmitter){
        this.datasource.deleteTransmitter(transmitter);
        transmitter = null;
    }

    public int getOwned(Player player){
        String name = player.getName().toLowerCase();
        int result = 0;
        if(owned.containsKey(name)){
            result = owned.get(name);
        }
        return result;
    }

    public boolean isProtected(){
        return config.isProtected();
    }

}
