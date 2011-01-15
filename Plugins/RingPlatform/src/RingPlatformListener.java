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

public class RingPlatformListener extends PluginListener{
    public static final RingPlatformConfig config = new RingPlatformConfig();
    private final PlatformMonitor monitor = new PlatformMonitor();
    private ArrayList<HashMap<String, Object>> shapes = new ArrayList<HashMap<String, Object>>();
    public void RingPlatformListener(){
        
    }

    public void loadShapes(){
        config.load();
        this.shapes = config.getShapes();        
    }

@Override
    public void onBlockRightClicked(Player player, Block blockClicked, Item item) {
        if(!config.checkPerms() || player.canUseCommand("/ringplatform")){
            ArrayList<Platform> platforms= new ArrayList<Platform>();
            int platformType = getPlatformType(blockClicked);
            if(platformType >=0 && monitor.getPlatform(blockClicked) == null){
                HashMap<String, Object> shape = shapes.get(platformType);
                platforms.add(new Platform(blockClicked, (int[][])shape.get("teleportShape"), (Integer)shape.get("height")));
                int y = blockClicked.getY();
                int height = (Integer)shape.get("height");
                int i = y + height + 1;
                int platformsInStack = 1;
                while((platformsInStack < config.maxPlatformsInStack()) && i > (y + height) || i < (y - height)){
                    if(i < 128 - height - 1){
                        Block block = blockClicked.getRelative(0, i - y, 0);
                        if(block.getType() == blockClicked.getType()){
                            if(isPlatform(block, (int[][])shapes.get(platformType).get("platformShape"))){
                                platforms.add(new Platform(block, (int[][])shape.get("teleportShape"), (Integer)shape.get("height")));
                                i=i+height;
                            }
                        }
                        i++;
                    }else{
                        i = 0;
                    }
                }
                if(platforms.size() > 1){
                    monitor.animate(platforms, config.getAnimations((ArrayList<String>)shape.get("animations")));
                }
            }
        }
    }

    private int getPlatformType(Block block){
        int result = -1;
        for(HashMap<String, Object> shape : shapes){
            if(isPlatform(block,(int[][])shape.get("platformShape"))){
                result = shapes.indexOf(shape);
                break;
            }
        }
        return result;
    }

    private boolean isPlatform(Block block, int[][] shape){
        int type = block.getType();
        int mod = (int)((shape.length - 1)/2);
        Integer compareType = null;
        boolean result = true;
        Loop:
        for(int i=0;i<shape.length;i++){
            for(int j=0;j<shape.length;j++){
                int compare = block.getRelative(i-mod, 0, j-mod).getType();
                if(shape[i][j] == 2){
                    if(compareType == null){
                        if(compare == type){
                            result = false;
                        }
                        compareType = compare;
                    }
                    if(compare != compareType){
                        result = false;
                    }
                }else if(shape[i][j] == 1){
                    if(compare != type) result = false;
                }
                if(!result) break Loop;
            }
        }
        return result;
    }

@Override
    public boolean onExplode(Block block) {
        return monitor.canExplode(block);
    }
}
