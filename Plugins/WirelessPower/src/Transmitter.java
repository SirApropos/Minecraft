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
import java.util.EnumMap;

public class Transmitter {
    private double range = 0.0;
    private String channel;
    private String owner;

    private int check = 0;

    private WirelessPowerMonitor monitor;

    private Type type;

    public List<Transmitter> transmitters = new ArrayList<Transmitter>();
    
    private EnumMap<Part, Block> blocks = new EnumMap<Part, Block>(Part.class);

    public static enum Type{
        TRANSMITTER("Transmit",0),
        RECEIVER("Receive",1),
        REPEATER("Repeat",2);

        private String value;
        private int intval;

        Type(String value, int intval){
            this.value = value;
            this.intval = intval;
        }
        public String value(){
            return this.value;
        }
        public int intval(){
            return this.intval;
        }
    }
    
    private enum Part{
        BASE,
        TORCH,
        SIGN
    }

    public Transmitter(WirelessPowerMonitor monitor, String owner, Block[] blocks,
        Type type, String channel, double range){
        this.monitor = monitor;
        this.owner = owner;
        this.blocks.put(Part.BASE, blocks[0]);
        this.blocks.put(Part.TORCH, blocks[1]);
        this.blocks.put(Part.SIGN, blocks[2]);
        this.channel = channel;
        this.type = type;
        this.range = range;
        getNearbyTransmitters();
    }

    private void getNearbyTransmitters(){
        this.transmitters = this.monitor.getNearbyTransmitters(this);
        for(Transmitter transmitter : this.transmitters){
            transmitter.addTransmitter(this);
        }
    }

    public void addTransmitter(Transmitter transmitter){
        if(!this.transmitters.contains(transmitter)){
            this.transmitters.add(transmitter);
        }
    }

    public void removeTransmitter(Transmitter transmitter){
        if(this.transmitters.contains(transmitter)){
            this.transmitters.remove(transmitter);
        }
    }

    public void destruct(){
        this.monitor.unwatchTransmitter(this);
        for(Transmitter transmitter : this.transmitters){
            transmitter.removeTransmitter(this);
        }
        for(Transmitter transmitter : this.transmitters){
            transmitter.update();
        }
        if(etc.getServer().getPlayer(this.owner) != null){
            etc.getServer().getPlayer(this.owner).sendMessage("§CTransmitter destroyed.");
        }
    }
    public boolean isPowered(){
        boolean result = false;
        Block block = getPart(Part.TORCH);
        if(this.type != Type.RECEIVER){
            if(block.getType() == 75){
                result = true;
            }
        }else{
            if(block.getType() == 76){
                result = true;
            }
        }
        return result;
    }

    public void update(){
        if(this.type == Type.TRANSMITTER){
            for(Transmitter transmitter : this.transmitters){
                if(transmitter.getType() == Type.RECEIVER){
                    if(isPowered()){
                        transmitter.changeTorch(true);
                    }else{
                        transmitter.update();
                    }
                }else{
                    propegate(new ArrayList<Transmitter>(),isPowered());
                }
            }
        }else if(this.type == Type.REPEATER){
            propegate(new ArrayList<Transmitter>(),isPowered());
        }else{
            boolean powered = false;
            for(Transmitter transmitter : this.transmitters){
                if(transmitter.isPowered()){
                    powered = true;
                    break;
                }
            }
            changeTorch(powered);
        }
    }

    private boolean propegate(ArrayList<Transmitter> checked, boolean powered){
        //Should only ever be called by receivers.
        checked.add(this);
        ArrayList<Transmitter> repeaters = new ArrayList<Transmitter>();
        ArrayList<Transmitter> receivers = new ArrayList<Transmitter>();
        for(Transmitter transmitter : transmitters){
            if(transmitter.getType() == Type.TRANSMITTER && !powered){
                if(transmitter.isPowered()){
                    powered = true;
                }
            }else{
                if(transmitter.getType() == Type.REPEATER){
                    repeaters.add(transmitter);
                }else{
                    receivers.add(transmitter);
                }
            }
        }
        for(Transmitter transmitter : repeaters){
            if(!checked.contains(transmitter) && calcDist(transmitter) <= range){
               powered = transmitter.propegate(checked, powered);
            }
        }

        changeTorch(!powered);
        
        if(powered){
            for(Transmitter transmitter : receivers){
                transmitter.changeTorch(true);
            }
        }else{
            for(Transmitter transmitter : receivers){
                transmitter.update();
            }
        }

        return powered;
    }

    public void changeTorch(boolean power){
        int blockType = getPart(Part.TORCH).getType();
        if((power && blockType == 75) || (!power && blockType == 76)){
            int torch = power ? 76 : 75;
            int[] coords = getCoords(Part.TORCH);
            Block block = new Block(torch,coords[0],coords[1],coords[2]);
            block.setData(0x5);
            etc.getServer().setBlock(block);
            block.update();
            blocks.put(Part.TORCH,block);
        }
    }

    public int[] getCoords(){
        return getCoords(Part.TORCH);
    }

    private Block getPart(Part part){
        int[] coords = getCoords(part);
        return etc.getServer().getBlockAt(coords[0], coords[1], coords[2]);
    }

    private int[] getCoords(Part part){
        return getCoords(blocks.get(part));
    }

    private int[] getCoords(Block block){
        return new int[]{block.getX(), block.getY(), block.getZ()};
    }

    public double getRange(){
        //Should only be called if this.type=TRANSMITTER
        return this.range;
    }

    public String getChannel(){
        return this.channel;
    }

    public String getOwner(){
        return this.owner;
    }

    public Block[] getBlocks(){
        return new Block[]{blocks.get(Part.BASE),blocks.get(Part.TORCH),blocks.get(Part.SIGN)};
    }

    public Type getType(){
        return this.type;
    }

    public boolean checkIntegrity(){
        //Even when rapidly being switched, shouldn't be called more than five times per second.
        //Do a complete check only once every five level changes. Expect that a partial
        //Check will be good enough. Only explosions are not explicitly tested for.
        //This may lead to orphaned data if both a transmitter and receiver are removed
        //Simultaneously. Full checks will occur on plugin/server start.
        boolean full = false;
        if(check == 4){
            full = true;
            check = 0;
        }else{
            check++;
        }
        return checkIntegrity(full);
    }

    public boolean checkIntegrity(boolean full){
        boolean result = false;
        if(etc.getServer().isChunkLoaded(getPart(Part.TORCH))){
            if(this.monitor.isWatched(this)){
                Block torch = getPart(Part.TORCH);
                torch.refresh();
                if((torch.getType() == 75 || torch.getType() == 76) && torch.getData() == 0x5){
                    result = true;
                }else{
                }
            }
            if(full){
                Block base = getPart(Part.BASE);
                Block sign = getPart(Part.SIGN);
                if(sign.getType()!=68 || sign.getData()!=blocks.get(Part.SIGN).getData() || base.getType()!=blocks.get(Part.BASE).getType()){
                    result = false;
                }
            }
        }
        if(!result){
            this.monitor.removeTransmitter(this);
        }
        return result;
    }

    public double calcDist(Transmitter compare){
        int[] coords = this.getCoords();
        int[] ccoords = compare.getCoords();
        return Math.sqrt(Math.pow(coords[0]-ccoords[0],2) + Math.pow(coords[1]-ccoords[1],2) + Math.pow(coords[2]-ccoords[2],2));
    }
}