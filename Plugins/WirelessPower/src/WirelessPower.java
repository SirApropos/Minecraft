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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WirelessPower extends Plugin{
    private String name = "WirelessPower";
    private List<PluginRegisteredListener> listeners = new ArrayList<PluginRegisteredListener>();
    private final WirelessPowerDataSource data = WirelessPowerDataSource.create();
    private final WirelessPowerMonitor monitor = new WirelessPowerMonitor(this.data);
    private final WirelessPowerListener listener = new WirelessPowerListener(this.monitor);
    public static final PropertiesFile config = new PropertiesFile("WirelessPower.txt");
    private static final Logger log = Logger.getLogger("Minecraft");

    public WirelessPower(){
    }

    public void enable(){
        log.log(Level.INFO, this.name+" Plugin Enabled.");
        this.monitor.loadConfig();
        this.monitor.load();
        addListener("SIGN_CHANGE","MEDIUM");
        addListener("FLOW","MEDIUM");
        addListener("BLOCK_RIGHTCLICKED","MEDIUM");
        addListener("SIGN_SHOW","MEDIUM");
        addListener("REDSTONE_CHANGE","MEDIUM");
        addListener("BLOCK_PHYSICS","MEDIUM");
        addListener("BLOCK_BROKEN","MEDIUM");
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
    public void disable(){
        for(PluginRegisteredListener listen : this.listeners){
            etc.getLoader().removeListener(listen);
        }
        listeners.clear();
        log.log(Level.INFO, this.name+" Plugin Disabled.");
    }
@Override
    public void initialize() {

    }
}
