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

public class RingPlatformListener extends PluginListener{
    private final PlatformMonitor monitor = new PlatformMonitor();
    int[][] shape;
    int[][] ringShape;
    int height;
    public void RingPlatformListener(){
        
    }

    public void loadShape(){
        this.shape = new int[][]{
            {0,1,1,1,0},
            {1,1,2,1,1},
            {1,2,1,2,1},
            {1,1,2,1,1},
            {0,1,1,1,0}};
        this.ringShape = new int[][]{
            {0,1,1,1,0},
            {1,2,2,2,1},
            {1,2,2,2,1},
            {1,2,2,2,1},
            {0,1,1,1,0}};
        this.height = 5;
    }

@Override
    public void onBlockRightClicked(Player player, Block blockClicked, Item item) {
        ArrayList<Platform> platforms= new ArrayList<Platform>();
        if(isAPlatform(blockClicked) && monitor.getPlatform(blockClicked) == null){
            platforms.add(new Platform(blockClicked, ringShape, height));
            int y = blockClicked.getY();
            int i = y + 6;
            while(i > (y + 5) || i < (y - 5)){
                if(i < 122){
                    Block block = blockClicked.getRelative(0, i - y, 0);
                    if(block.getType() == blockClicked.getType()){
                        if(isAPlatform(block)){
                            platforms.add(new Platform(block, ringShape, height));
                            i=i+5;
                        }
                    }
                    i++;
                }else{
                    i = 0;
                }
            }
            if(platforms.size() > 1){
                monitor.animate(platforms);
            }
        }
    }

    private boolean isAPlatform(Block block){
        int type = block.getType();
        int mod = (int)((this.shape.length - 1)/2);
        Integer compareType = null;
        boolean result = true;
        Loop:
        for(int i=0;i<shape.length;i++){
            for(int j=0;j<this.shape.length;j++){
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
