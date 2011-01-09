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
import java.util.Timer;
import java.util.TimerTask;

public class PlatformMonitor {
    private ArrayList<ArrayList<Platform>> animations = new ArrayList<ArrayList<Platform>>();
    private Long delay = 100L;

    public PlatformMonitor(){

    }

    public boolean isTeleporting(Platform platform){
        return false;
    }

    public Platform getPlatform(Block center){
        Platform result = null;
        Find:
        for(ArrayList<Platform> animation : animations){
            for(Platform platform : animation){
                if(platform.getCenter().equals(center)){
                    result = platform;
                    break Find;
                }
            }
        }
        return result;
    }

    public void animate(ArrayList<Platform> platforms){
        animations.add(platforms);
        nextFrame(platforms);
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

    public void nextFrame(ArrayList<Platform> platforms){
        boolean result = false;
        for(Platform platform : platforms){
            platform.nextFrame();
        }
        Platform platform = platforms.get(0);
        if(platform != null){
            if(platform.isTeleportFrame()) teleport(platforms);
            if(platform.hasNextFrame()) result = true;
        }
        if(result == true){
            Timer timer = new Timer();
            TimerTask task = new AnimateTask(platforms);
            timer.schedule(task, delay);
        }else{
            animations.remove(platforms);
        }
    }

    private class AnimateTask extends TimerTask{
        private ArrayList<Platform> platforms;

        public AnimateTask(ArrayList<Platform> platforms){
            this.platforms = platforms;
        }

        public void run(){
            nextFrame(platforms);
        }
    }

    public boolean canExplode(Block block){
        boolean result = false;
        Loop:
        for(ArrayList<Platform> platforms : animations){
            for(Platform platform : platforms){
                if(platform.isProtectedFrame()){
                    double x = platform.getCenter().getX() - block.getX();
                    double y = platform.getCenter().getY() - block.getY();
                    double z = platform.getCenter().getZ() - block.getZ();
                    double dist = Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));
                    if(dist < 10.0){
                        result = true;
                        break Loop;
                    }
                }
            }
        }
        return result;
    }
}
