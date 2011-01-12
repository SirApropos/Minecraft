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

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class WirelessPowerConfig {
    private Map<String,EnumMap<Event,Integer>> permissions= new HashMap<String,EnumMap<Event,Integer>>();
    private static final Logger log = Logger.getLogger("Minecraft");
    private boolean protectTransmitters = false;
    private Double transmitterBasePower = 15.0;
    private Map<Integer,Double> boosterBlocks = new HashMap<Integer,Double>();
    private WirelessPowerMonitor monitor;
    private String table = "Transmitters";
    private Boolean mysql = false;
    private String dataFile = "transmitters.dat";

    public enum Event{
        createTransmitter("maxtransmitters"),
        createChannel("createchannel");

        private String value;

        private Event(String str){
            value = str;
        }

        public String getValue(){
            return value;
        }
    }

    public WirelessPowerConfig(WirelessPowerMonitor monitor){
        this.monitor = monitor;
        loadConfig();
        saveConfig();
    }
    private void loadConfig(){
        EnumMap<Event, Integer> perms = new EnumMap<Event, Integer>(Event.class);
        perms.put(Event.createChannel, 1);
        perms.put(Event.createTransmitter,-1);
        permissions.put("group:everyone",perms);

        File file = new File("WirelessPower.properties");
        if(!file.exists()){
            this.boosterBlocks.put(89,2.0);
            try{
                file.createNewFile();
            }catch(Exception ie){
                log.log(Level.SEVERE,"Could not create configuration file.");
            }
        }else if(file.length() > 0){
            try {
                FileInputStream stream = new FileInputStream(file);
                DataInputStream data = new DataInputStream(stream);
                BufferedReader reader = new BufferedReader(new InputStreamReader(data));
                String str;
                HashMap<String, String> vars = new HashMap<String, String>();
                while ((str = reader.readLine()) != null){
                    str = str.toLowerCase();
                    if(str.matches("[a-zA-Z0-9_]+=[^=]+")){
                        int i = str.indexOf("=");
                        if(i != -1){
                            vars.put(str.substring(0, i),str.substring(i+1));
                        }
                    }else if(str.matches("perm:[a-zA-Z]+:[a-zA-Z0-9]+:[a-zA-Z0-9]+:-?[0-9]+")){
                        addPermission(str);
                    }else if(str.matches("boost:[0-9]+:[0-9]+(\\.[0-9]+)?")){
                        addBoostBlock(str);
                    }
                }
                data.close();
                reader.close();
                setVars(vars);
            }catch (Exception ex){
                log.log(Level.SEVERE,"Could not read from configuration file.");
            }
        }
    }

    private void saveConfig(){
        List<String> lines = new ArrayList<String>();
        lines.add("protect_transmitters="+Boolean.toString(protectTransmitters));
        lines.add("base_power="+Double.toString(transmitterBasePower));
        lines.add("use_mysql="+Boolean.toString(mysql));
        lines.add("transmitters_file="+dataFile);
        lines.add("mysql_table="+table);
        StringBuilder line;
        for(Object key : boosterBlocks.keySet().toArray()){
            int blockType = (Integer)key;
            line = new StringBuilder("boost:");
            line.append(Integer.toString(blockType)).append(":");
            line.append(Double.toString(boosterBlocks.get(blockType)));
            lines.add(line.toString());
        }
        for(Object key : permissions.keySet().toArray()){
            for(Object permKey : permissions.get((String)key).keySet()){
                line = new StringBuilder("perm:");
                line.append((String)key).append(":");
                Event event = (Event)permKey;
                line.append(event.getValue()).append(":");
                line.append(Integer.toString(permissions.get((String)key).get(event)));
                lines.add(line.toString());
            }
        }
        File file = new File("WirelessPower.properties");
        try {
            FileWriter stream = new FileWriter(file);
            BufferedWriter writer = new BufferedWriter(stream);
            for(String ln : lines){
                writer.write(ln);
                writer.newLine();
            }
            writer.close();
        }catch (Exception ex){
          System.err.println("Error writing file: " + ex.getMessage());
        }

    }

    private void setVars(HashMap<String, String> vars){
        if(vars.containsKey("protect_transmitters")){
            protectTransmitters = Boolean.parseBoolean(vars.get("protect_transmitters"));
        }
        if(vars.containsKey("base_power")){
            transmitterBasePower = Double.parseDouble(vars.get("base_power"));
        }
        if(vars.containsKey("transmitters_file")){
            dataFile  =  vars.get("transmitters_file");
        }
        if(vars.containsKey("mysql_table")){
            table = vars.get("mysql_table");
        }
    }

    public void addPermission(String str){
        String[] split = str.split(":");
        //split[1] = user/group, split[2] = playerName, split[3] = permission, split[4] = value
        if(split[1].equals("user") || split[1].equals("group")){
            if(split[3].equals("maxtransmitters") || split[3].equals("createchannel")){
                Event event;
                if(split[3].equals("maxtransmitters")){
                    event = Event.createTransmitter;
                }else{
                    event = Event.createChannel;
                }
                String permstr = new StringBuilder(split[1]).append(":").append(split[2]).toString();
                if(!permissions.containsKey(permstr)){
                    permissions.put(permstr, new EnumMap<Event, Integer>(Event.class));
                }
                permissions.get(permstr).put(event, Integer.parseInt(split[4]));
            }
        }
    }

    public void addBoostBlock(String str){
        String[] split = str.split(":");
        //split[1] = blockType, split[2] = value
        boosterBlocks.put(Integer.parseInt(split[1]), Double.parseDouble(split[2]));
    }

    public double getBasePower(){
        return transmitterBasePower;
    }

    public double getBoost(Block block){
        double boost = 1.0;
        int type = block.getType();
        if(boosterBlocks.containsKey(type)) boost = boosterBlocks.get(type);
        return boost;
    }

    public boolean checkPermission(Player player, Event event){
        boolean result = false;
        String name = "user:"+player.getName().toLowerCase();
        String[] groups = player.getGroups();
        String group = "group:"+groups[0].toLowerCase();
        int perm = 0;
        if(permissions.containsKey(name)
                && permissions.get(name).containsKey(event)){
                perm = permissions.get(name).get(event);
        }else if(permissions.containsKey(group)
                && permissions.get(group).containsKey(event)){
                perm = permissions.get(group).get(event);
        }else{
            perm = permissions.get("group:everyone").get(event);
        }
        if(perm > 0){
            switch(event){
                case createChannel:
                    result = true;
                    break;
                case createTransmitter:
                    if(monitor.getOwned(player) < perm) result = true;
                    break;
            }
        }else if(perm == -1){
            result = true;
        }
        return result;
    }

    public boolean isProtected(){
        return protectTransmitters;
    }
}
