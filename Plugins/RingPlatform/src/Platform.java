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
import java.util.logging.Logger;
import java.util.logging.Level;

public class Platform {
    private Block center;
    private ArrayList<int[][]> frames  = new ArrayList<int[][]>();
    private ArrayList<Block> ringBlocks = new ArrayList<Block>();
    private int[][] shape;
    private int height=5;
    private int currentFrame = 0;
    private int teleportFrame = 14;
    private ArrayList<Integer> protectedFrames = new ArrayList<Integer>();
    private HashMap<Integer, int[]> pruneFrames = new HashMap<Integer, int[]>();
    private ArrayList<Integer> exclude = new ArrayList<Integer>();
    private ArrayList<Integer> special = new ArrayList<Integer>();
    private static final Logger log = Logger.getLogger("Minecraft");

    public Platform(Block center, int[][] shape, int height){
        this.center = center;
        this.shape = shape;
        this.height = height;
        createFrames();
        int mod = (int)((shape.length - 1)/2);
        for(int i=0;i<shape.length;i++){
            for(int j=0;j<shape.length;j++){
                if(shape[i][j] == 1){
                    ringBlocks.add(center.getRelative(i-mod, 0, j-mod));
                }
            }
        }
        for(int i : new int[]{0,8,9,10,11,18,20,51,64,68,71,78,90}){
            exclude.add(i);
        }
        for(int i : new int[]{6,18,37,38,39,40,50,59,63,64,65,68,69,71,75,76,83,90}){
            special.add(i);
        }
    }

    public void nextFrame(){
        try{
            currentFrame++;
            if(pruneFrames.containsKey(currentFrame)){
                prune();
            }
            if(currentFrame <  frames.size() && frames.get(currentFrame) != null){
                for(int[] frame : frames.get(currentFrame)){
                    setBlocks(frame[0],frame[1]);
                }
            }
        }catch(Exception ex){
            cleanup();
        }
    }

    private void prune(){
        if(pruneFrames.containsKey(currentFrame)){
            for(int y : pruneFrames.get(currentFrame)){
                for(Block ringBlock : ringBlocks){
                    Block block  = ringBlock.getRelative(0,y,0);
                    if(!exclude.contains(block.getType())){
                        Location loc = new Location((double)block.getX(), (double)block.getY(), (double)block.getZ());
                        etc.getServer().dropItem(loc, block.getType());
                    }
                }
            }
        }
    }

    private void setBlocks(int y, int type){
        for(Block ringBlock : ringBlocks){
            Block block = new Block(type, ringBlock.getX(), ringBlock.getY()+y, ringBlock.getZ());
            etc.getServer().setBlock(block);
        }
    }

    private void createFrames(){
        addFrame(new int[][]{{1,0},{2,0},{3,0},{4,0},{5,0}}); //Special frame to remove everything if error occurs.
        pruneFrame(addFrame(new int[][]{{1,49}}),new int[]{1});
        pruneFrame(addFrame(new int[][]{{1,0},{3,49}}),new int[]{2,3});
        pruneFrame(addFrame(new int[][]{{3,0},{4,49}}),new int[]{4});
        pruneFrame(addFrame(new int[][]{{4,0},{5,49}}),new int[]{5});
        addFrame(new int[][]{{1,49}});
        addFrame(new int[][]{{1,0},{2,49}});
        addFrame(new int[][]{{2,0},{3,49}});
        addFrame(new int[][]{{1,49}});
        protectFrame(addFrame(new int[][]{{4,57}}));
        protectFrame(addFrame(new int[][]{}));
        protectFrame(addFrame(new int[][]{{2,57}}));
        protectFrame(addFrame(new int[][]{}));
        protectFrame(addTeleportFrame(new int[][]{}));
        protectFrame(addFrame(new int[][]{}));
        protectFrame(addFrame(new int[][]{}));
        protectFrame(addFrame(new int[][]{{4,0}}));
        protectFrame(addFrame(new int[][]{}));
        protectFrame(addFrame(new int[][]{{2,0}}));
        addFrame(new int[][]{{1,0}});
        addFrame(new int[][]{{3,0},{2,49}});
        addFrame(new int[][]{{2,0},{1,49}});
        addFrame(new int[][]{{1,0}});
        addFrame(new int[][]{{5,0},{4,49}});
        addFrame(new int[][]{{4,0},{3,49}});
        addFrame(new int[][]{{3,0},{1,49}});
        addFrame(new int[][]{{1,0}});
    }

    private int addFrame(int[][] frame){
        frames.add(frame);
        return (frames.size()-1);
    }

    private int protectFrame(int frame){
        protectedFrames.add(frame);
        return frame;
    }

    private int pruneFrame(int frame, int[] heights){
        pruneFrames.put(frame, heights);
        return frame;
    }

    private int addTeleportFrame(int[][] frame){
        teleportFrame = addFrame(frame);
        return teleportFrame;
    }

    public HashMap<String, Object> getContents(){
        HashMap<String, Object> contents = new HashMap<String, Object>();
        ArrayList<Block> blockList = new ArrayList<Block>();
        ArrayList<Player> playerList = new ArrayList<Player>();
        Block[][][] blockArray = new Block[shape.length][shape.length][5];
        HashMap<String, Object>[][][] specialArray = new HashMap[shape.length][shape.length][5];
        int mod = (int)((shape.length - 1)/2);
        for(int y = 1;y<=height;y++){
            for(int i=0;i<shape.length;i++){
                for(int j=0;j<shape.length;j++){
                    if(shape[i][j] == 2){
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
        try{
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
                                if( y < height && specialArray[i][j][y] != null && type == specialArray[i][j][y].get("type")){
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
        }catch(Exception ex){
            cleanup();
        }
    }

    private void cleanup(){
        if(currentFrame>0){
            log.log(Level.SEVERE, "[RingPlatform] An unexpected error has occurred. Forcing cleanup.");
            currentFrame = -1;
            nextFrame();
            currentFrame = frames.size();
        }
    }

    public boolean hasNextFrame(){
        boolean result = (currentFrame+1 < frames.size()) ? true : false;
        return result;
    }

    public boolean isTeleportFrame(){
        return (currentFrame == teleportFrame);
    }

    public boolean isProtectedFrame(){
        return protectedFrames.contains(currentFrame);
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
