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
        if(currentFrame <  frames.size()){
            for(int[] frame : frames.get(currentFrame)){
                setBlocks(frame[0],frame[1]);
            }
        }
        }catch(ArrayIndexOutOfBoundsException ex){
            if(currentFrame>0){
                currentFrame = -1;
                nextFrame();
                currentFrame = frames.size();
            }
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
            Block block  = ringBlock.getRelative(0,y,0);
            block.setType(type);
            block.update();
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
        Block[][][] specialArray = new Block[shape.length][shape.length][5];
        int mod = (int)((shape.length - 1)/2);
        for(int y = 1;y<=height;y++){
            for(int i=0;i<shape.length;i++){
                for(int j=0;j<shape.length;j++){
                    if(shape[i][j] == 2){
                        Block block = center.getRelative(i-mod, y, j-mod);
                        blockList.add(block);
                        if(special.contains(block.getType())){
                            specialArray[i][j][y-1] = block;
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
        Block[][][] specialArray = (Block[][][])contents.get("special");
        int mod = (int)((blockArray.length - 1)/2);
        int offset = center.getY() - blockArray[mod][mod][0].getY()+1;
        for(int y = 1;y<=height;y++){
            for(int i=0;i<blockArray.length;i++){
                for(int j=0;j<blockArray.length;j++){
                    if(blockArray[i][j][y-1] != null){
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
                        Block block = center.getRelative(i-mod, y, j-mod);
                        block.setType(specialArray[i][j][y-1].getType());
                        if( y < height - 2 && (block.getType() == 64 || block.getType() == 71) && block.getType() == specialArray[i][j][y].getType()){
                            block.setData(0x4);
                            Block door = center.getRelative(i-mod, y+1, j-mod);
                            door.setType(specialArray[i][j][y-1].getType());
                            door.setData(0x8);
                            specialArray[i][j][y] = null;
                            door.update();
                        }else{
                            block.setData(specialArray[i][j][y-1].getData());
                        }
                        block.update();
                        if(block.getType()==63 || block.getType() == 68){
                            Sign sign = (Sign)etc.getServer().getComplexBlock(block);
                            Sign original = (Sign)etc.getServer().getComplexBlock(specialArray[i][j][y-1]);
                            if(sign != null && original!=null){
                                for(i=0;i<3;i++){
                                    sign.setText(i, original.getText(i));
                                }
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
}
