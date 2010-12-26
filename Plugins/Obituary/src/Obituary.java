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

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;

public class Obituary extends Plugin {
    private static final Logger log = Logger.getLogger("Minecraft");
    private String name = "Obituary";
    private List<PluginRegisteredListener> listeners = new ArrayList<PluginRegisteredListener>();
    private final ObituaryListener listener = new ObituaryListener();

    public void enable(){
        addListener("DAMAGE","MEDIUM");
        addListener("HEALTH_CHANGE","MEDIUM");
        addListener("DISCONNECT","MEDIUM");
        listener.loadMessages();
        log.log(Level.INFO, this.name+" Plugin Enabled.");
        try{
           in.class.getMethod("b", new Class[]{ep.class});
        }catch(Exception ex){
            log.log(Level.INFO,"Obituary plugin does not match server version?");
        }
    }

    public void disable(){
        for(PluginRegisteredListener listen : this.listeners){
            etc.getLoader().removeListener(listen);
        }
        log.log(Level.INFO, this.name+" Plugin Disabled.");
    }
    private void addListener(String hookName, String priorityName){
        try{
            PluginLoader.Hook hook = PluginLoader.Hook.valueOf(hookName.toUpperCase());
            PluginListener.Priority priority = PluginListener.Priority.valueOf(priorityName.toUpperCase());
            listeners.add(etc.getLoader().addListener(hook, listener, this, priority));
        }catch(Exception localException){
            log.log(Level.SEVERE, "Failed to register hook \""+hookName+"\":"+localException.getMessage());
        }
    }
@Override
    public void initialize() {

    }
}
