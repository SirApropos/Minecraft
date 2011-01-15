/*
 *  Copyright (C) 2011 Apropos
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
import java.util.HashMap;

public class Platform {
    private Block center;
    private int[][] shape;
    private int height = 0;
    private ArrayList<Integer> exclude = new ArrayList<Integer>();
    private ArrayList<Integer> special = new ArrayList<Integer>();

    public Platform(Block center, final int[][] shape, int height){
        this.center = center;
        this.shape = shape;
        this.height = height;
        for(int i : new int[]{}){
            exclude.add(i);
        }
        for(int i : new int[]{6,18,37,38,39,40,50,59,63,64,65,68,69,71,75,76,83,90}){
            special.add(i);
        }
    }

    public HashMap<String, Object> getContents(){
        HashMap<String, Object> contents = new HashMap<String, Object>();
        ArrayList<Block> blockList = new ArrayList<Block>();
        ArrayList<Player> playerList = new ArrayList<Player>();
        Block[][][] blockArray = new Block[shape.length][shape.length][height];
        HashMap<String, Object>[][][] specialArray = new HashMap[shape.length][shape.length][height];
        int mod = (int)((shape.length - 1)/2);
        for(int y = 1;y<=height;y++){
            for(int i=0;i<shape.length;i++){
                for(int j=0;j<shape.length;j++){
                    if(shape[i][j] == 1){
                        Block block = center.getRelative(i-mod, y, j-mod);
                        blockList.add(block);
                        int type = block.getType();
                        if(special.contains(type)){
                            HashMap<String, Object> data = new HashMap<String, Object>();
                            data.put("type", type);
                            data.put("data", block.getData());
                            if(type == 63 || type == 68){
                                //Is a sign.
                                String[] text = new String[4];
                                Sign sign = (Sign)etc.getServer().getComplexBlock(block);
                                for(int k=0;k<4;k++){
                                    text[k] = sign.getText(k);
                                }
                                data.put("signText", text);
                            }
                            specialArray[i][j][y-1] = data;
                            blockArray[i][j][y-1] = null;
                        }else{
                            blockArray[i][j][y-1] = block;
                        }
                    }else{
                        blockArray[i][j][y-1] = null;
                    }
                }
            }
        }
        for(Player player : etc.getServer().getPlayerList()){
            Block block = etc.getServer().getBlockAt((int)Math.floor(player.getX()), (int)Math.floor(player.getY()), (int)Math.floor(player.getZ()));
            if(blockList.contains(block)){
                playerList.add(player);
            }
        }
        contents.put("blocks",blockArray);
        contents.put("special",specialArray);
        contents.put("players",playerList);
        return contents;
    }

    public void setContents(HashMap<String, Object> contents){
        Block[][][] blockArray = (Block[][][])contents.get("blocks");
        HashMap<String, Object>[][][] specialArray = (HashMap<String, Object>[][][])contents.get("special");
        int mod = (int)((blockArray.length - 1)/2);
        int offset = 0;
        for(int y = 1;y<=height;y++){
            for(int i=0;i<blockArray.length;i++){
                for(int j=0;j<blockArray.length;j++){
                    if(blockArray[i][j][y-1] != null){
                        if(offset == 0) offset = center.getY() - blockArray[i][j][y-1].getY()-y+2;
                        Block block = center.getRelative(i-mod, y, j-mod);
                        block.setType(blockArray[i][j][y-1].getType());
                        block.update();
                    }
                }
            }
        }
        for(int y = 1;y<=height;y++){
            for(int i=0;i<specialArray.length;i++){
                for(int j=0;j<specialArray.length;j++){
                    if(specialArray[i][j][y-1] != null){
                        HashMap<String, Object> data = specialArray[i][j][y-1];
                        int type = (Integer)data.get("type");
                        int blockData = (Integer)data.get("data");
                        Block block = center.getRelative(i-mod, y, j-mod);
                        block.setType(type);
                        block.setData(blockData);
                        if(type == 64 || type == 71){
                            if( y < height && specialArray[i][j][y] != null && type == (Integer)specialArray[i][j][y].get("type")){
                                Block door = block.getRelative(0, 1, 0);
                                door.setType(type);
                                door.setData((Integer)specialArray[i][j][y].get("data"));
                                specialArray[i][j][y] = null;
                                block.update();
                                door.update();
                            }else{
                                block.setType(0);
                            }
                        }
                        block.update();
                        if(type==63 || type == 68){
                            Sign sign = (Sign)etc.getServer().getComplexBlock(block);
                            if(sign != null && data.containsKey("signText")){
                                for(int k=0;k<4;k++){
                                    sign.setText(k, ((String[])data.get("signText"))[k]);
                                }
                                sign.update();
                            }
                        }
                    }
                }
            }
        }

        for(Player player : (ArrayList<Player>)contents.get("players")){
            Location loc = player.getLocation();
            loc.y = loc.y+(double)offset;
            player.teleportTo(loc);
        }
    }

    public Block getCenter(){
        return this.center;
    }

    public int getWidth(){
        return shape.length;
    }

    public int getHeight(){
        return height;
    }
}
