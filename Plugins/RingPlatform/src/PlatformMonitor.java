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

import java.util.HashMap;
import java.util.ArrayList;
import java.util.TimerTask;

public class PlatformMonitor {
    private ArrayList<ArrayList<Platform>> platformSets = new ArrayList<ArrayList<Platform>>();
    private HashMap<ArrayList<Platform>, ArrayList<Animation>> animationSets = new HashMap<ArrayList<Platform>, ArrayList<Animation>>();

    public PlatformMonitor(){
    }

    public Platform getPlatform(Block center){
        Platform result = null;
        Find:
        for(ArrayList<Platform> animation : platformSets){
            for(Platform platform : animation){
                if(platform.getCenter().equals(center)){
                    result = platform;
                    break Find;
                }
            }
        }
        return result;
    }

    public void animate(ArrayList<Platform> platforms, ArrayList<HashMap<String, Object>> animationsInfo){
        boolean result = true;
        Loop:
        for(ArrayList<Platform> animation : platformSets){
            for(Platform animPlatform : animation){
                for(Platform platform : platforms){
                    int x = Math.abs(platform.getCenter().getX()- animPlatform.getCenter().getX());
                    int y = Math.abs(platform.getCenter().getY()- animPlatform.getCenter().getY());
                    int z = Math.abs(platform.getCenter().getZ()- animPlatform.getCenter().getZ());
                    int width = platform.getWidth();
                    int height = platform.getHeight();
                    if((x < width || z < width) && (y > 0 && y < height+1)){
                        result = false;
                        break Loop;
                    }
                }
            }
        }
        if(result){
            ArrayList<Animation> animations = new ArrayList<Animation>();
            for(HashMap<String, Object> animationInfo : animationsInfo){
                Animation animation = new Animation(animationInfo);
                for(Platform platform : platforms){
                    animation.addAnimationPoint(platform.getCenter());
                }
                animations.add(animation);
            }
            platformSets.add(platforms);
            animationSets.put(platforms, animations);
            for(Animation animation : animations){
                nextFrame(platforms, animation);
            }
        }
    }

    private void teleport(ArrayList<Platform> platforms){
        ArrayList<HashMap<String, Object>> contents = new ArrayList<HashMap<String, Object>>();
        for(Platform platform : platforms){
            contents.add(platform.getContents());
        }
        for(int i = 1;i < contents.size();i++){
            platforms.get(i).setContents(contents.get(i-1));
        }
        platforms.get(0).setContents(contents.get(contents.size()-1));
    }

    public void nextFrame(ArrayList<Platform> platforms, Animation animation){
        animation.nextFrame();
        if(animation.isTeleportFrame()){
            teleport(platforms);
        }
        if(animation.hasNextFrame()){
            TimerTask task = new AnimateTask(platforms, animation);
            etc.getServer().addToServerQueue(task, animation.getDelay());
        }else{
            platformSets.remove(platforms);
            animationSets.remove(platforms);
        }
    }

    private class AnimateTask extends TimerTask{
        private ArrayList<Platform> platforms;
        private Animation animation;

        public AnimateTask(ArrayList<Platform> platforms, Animation animation){
            this.platforms = platforms;
            this.animation = animation;
        }

        public void run(){
            nextFrame(platforms, animation);
        }
    }

    public boolean canExplode(Block block){
        boolean result = false;
        Loop:
        for(ArrayList<Platform> key : animationSets.keySet()){
            ArrayList<Animation> animations = animationSets.get(key);
            for(Animation animation : animations){
                if(animation.isDenyExplode(block)){
                    result = true;
                    break Loop;
                }
            }
        }
        return result;
    }
}
