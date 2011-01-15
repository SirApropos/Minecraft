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

public class Animation {
    private final int[][] shape;
    private int height;
    private int currentFrame = 0;
    private Long delay = 100L;
    private boolean loop = false;
    private ArrayList<Block> centers = new ArrayList<Block>();
    private ArrayList<Block> blocks = new ArrayList<Block>();
    private ArrayList<Frame> frames  = new ArrayList<Frame>();
    private ArrayList<Integer> exclude = new ArrayList<Integer>();

    public Animation(HashMap<String, Object> animation){
        shape = (int[][])animation.get("shape");
        height = (Integer)animation.get("height");
        delay = (Long)animation.get("delay");
        createFrames((ArrayList<String>)animation.get("frames"));
        for(int i : new int[]{0,8,9,10,11,18,20,51,64,68,71,78,90}){
            exclude.add(i);
        }
    }

    public void addAnimationPoint(Block center){
        centers.add(center);
        int mod = (int)((shape.length - 1)/2);
        for(int i=0;i<shape.length;i++){
            for(int j=0;j<shape.length;j++){
                if(shape[i][j] == 1){
                    blocks.add(center.getRelative(i-mod, 0, j-mod));
                }
            }
        }
    }

    private void createFrames(ArrayList<String> frameStrings){
        for(String frameString : frameStrings){
            frames.add(new Frame(frameString));
        }
    }

    public void nextFrame(){
        if(this.centers.size() > 0){
            currentFrame++;
            if(frames.get(currentFrame).isPruningFrame()){
                prune();
            }
            try{
                if(currentFrame <  frames.size() && frames.get(currentFrame) != null){
                    for(int[] frame : frames.get(currentFrame).getRows()){
                        setBlocks(frame[0],frame[1]);
                    }
                }
            }catch(Exception ex){
                cleanup();
            }
        }
    }

    public int getCurrentFrame(){
        return currentFrame;
    }

    private void prune(){
        for(int y : frames.get(currentFrame).getPruneRows()){
            for(Block animBlock : blocks){
                Block block  = animBlock.getRelative(0,y,0);
                if(!exclude.contains(block.getType())){
                    Location loc = new Location((double)block.getX(), (double)block.getY(), (double)block.getZ());
                    etc.getServer().dropItem(loc, block.getType());
                }
            }
        }
    }

    private void setBlocks(int y, int type){
        for(Block animBlock : blocks){
            Block block = new Block(type, animBlock.getX(), animBlock.getY()+y, animBlock.getZ());
            etc.getServer().setBlock(block);
        }
    }

    public boolean hasNextFrame(){
        return (loop || currentFrame+1 < frames.size());
    }

    public boolean isTeleportFrame(){
        return frames.get(currentFrame).isTeleportFrame();
    }

    public boolean isProtectedFrame(){
        return frames.get(currentFrame).isProtectedFrame();
    }

    public Long getDelay(){
        return delay;
    }

    public void cleanup(){
        if(currentFrame>0){
            currentFrame = -1;
            nextFrame();
            currentFrame = frames.size();
        }
    }

    public boolean isDenyExplode(Block block){
        boolean result = false;
        if(frames.get(currentFrame).isProtectedFrame()){
            for(Block center : centers){
                double x = center.getX() - block.getX();
                double y = center.getY() - block.getY();
                double z = center.getZ() - block.getZ();
                double dist = Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));
                if(dist < getWidth()+5.0){
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public int getWidth(){
        return shape.length;
    }

    public int getHeight(){
        return height;
    }

    private class Frame{
        private int[][] rows;
        private int[] pruneRows;
        private boolean teleport = false;
        private boolean protect = false;

        public Frame(String frame){
            if(frame.matches(".*protect:.+")){
                frame = frame.replace("protect:","");
                protect = true;
            }
            if(frame.matches(".*teleport:.+")){
                frame = frame.replace("teleport:","");
                teleport = true;
            }
            if(frame.matches(".+\\},prune:\\{-?[0-9]+(,-?[0-9]+)*\\}")){
                String pruneStr = frame.replaceFirst(".+\\},prune:\\{(-?[0-9]+(,-?[0-9]+)*)\\}","$1");
                frame = frame.replaceFirst("(.+\\}),prune:\\{-?[0-9]+(,-?[0-9]+)*\\}","$1");
                String[] split = pruneStr.split(",");
                pruneRows = new int[split.length];
                int i=0;
                for(String str : split){
                    pruneRows[i] = Integer.parseInt(str);
                    i++;
                }
            }
            if(!frame.equals("{}")){
                if(frame.matches("\\{(\\{-?[0-9]+,-?[0-9]+\\}(,\\{-?[0-9]+,-?[0-9]+\\})*)?\\}")){
                    String[] split  = frame.replaceFirst("\\{(.*)\\}","$1").split("\\},\\{");
                    rows = new int[split.length][2];
                    int i=0;
                    for(String str : split){
                        String[] rowStr = str.replaceAll("[{}]","").split(",");
                        if(rowStr.length==2){
                            rows[i][0] = Integer.parseInt(rowStr[0]);
                            rows[i][1] = Integer.parseInt(rowStr[1]);
                            i++;
                        }
                    }
                }
            }
        }

        public boolean isTeleportFrame(){
            return teleport;
        }

        public boolean isProtectedFrame(){
            return protect;
        }

        public boolean isPruningFrame(){
            return (pruneRows != null && pruneRows.length > 0);
        }

        public int[] getPruneRows(){
            if(pruneRows == null) pruneRows = new int[0];
            return pruneRows;
        }

        public int[][] getRows(){
            if(rows == null) rows = new int[0][0];
            return rows;
        }
    }
}
