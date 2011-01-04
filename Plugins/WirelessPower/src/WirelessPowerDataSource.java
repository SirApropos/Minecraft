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
import java.util.List;
import java.util.logging.Logger;

public abstract class WirelessPowerDataSource {
    public static final Logger log = Logger.getLogger("Minecraft");

    public static WirelessPowerDataSource create() {
        WirelessPowerDataSource datasource;
        if(WirelessPower.config.getBoolean("use-mysql",true)){
            datasource = new WirelessPowerMysqlSource(new Mysql(etc.getSQLConnection()),WirelessPower.config.getString("table-name","transmitters"));
        }else{
            datasource = null;
        }
        return datasource;
    }

    public void load(WirelessPowerMonitor monitor){
        Block[] blocks;
        for(Object[] obj : loadTransmitters()){
            blocks = getBlocks((String)obj[1]);
            monitor.addTransmitter(monitor.createTransmitter(
                    (String)obj[0],blocks,(Transmitter.Type)obj[2], (String)obj[3]));
        }
    }
    
    public Block[] getBlocks(String blockString){
        String[] coords = blockString.split(",");
        Block[] blocks = new Block[coords.length];
        for(int i=0;i<coords.length;i++){
            blocks[i] = getBlockFromString(coords[i]);
        }
        return blocks;
    }

    public String getBlockString(Block block){
        StringBuilder blockString = new StringBuilder();
        blockString.append(Integer.toString(block.getX())).append(":");
        blockString.append(Integer.toString(block.getY())).append(":");
        blockString.append(Integer.toString(block.getZ()));
        return blockString.toString();
    }

    protected Block getBlockFromString(String blockString){
        String[] strings = blockString.split(":");
        int[] coords = new int[strings.length];
        for(int i=0;i<strings.length;i++){
            coords[i] = Integer.parseInt(strings[i]);
        }
        return etc.getServer().getBlockAt(coords[0], coords[1], coords[2]);
    }

    protected String getBlockStrings(Block[] blocks){
        StringBuilder result = new StringBuilder();
        int i = 0;
        for(Block block : blocks){
            result.append(getBlockString(block));
            if(i<2) result.append(",");
            i++;
        }
        return result.toString();
    }
    protected Transmitter.Type getTypeFromInt(int i){
        Transmitter.Type blockType = Transmitter.Type.TRANSMITTER;
        for(Transmitter.Type type : Transmitter.Type.values()){
            if(i == type.intval()){
                blockType = type;
                break;
            }
        }
        return blockType;
    }

    protected abstract List<Object[]> loadTransmitters();

    public abstract void deleteTransmitter(Transmitter transmitter);

    public abstract void saveTransmitter(Transmitter transmitter);

}
