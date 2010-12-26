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
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;
import java.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ObituaryListener extends PluginListener {
    private static final Logger log = Logger.getLogger("Minecraft");
    private HashMap<String, ArrayList<String>> messages = new HashMap<String, ArrayList<String>>();
    private Random generator = new Random();
    private HashMap<Player, Object[]> pendingDeaths = new HashMap<Player, Object[]>();

    public ObituaryListener(){
    }

    @Override
    public boolean onDamage(PluginLoader.DamageType type, BaseEntity attacker, BaseEntity defender, int amount) {
        if(defender != null && defender.isPlayer()){
            Player player = defender.getPlayer();
            if(player.getHealth() <= 0 || player.getHealth()>0 && player.getHealth() <= amount){
                if(type == PluginLoader.DamageType.FIRE){
                    Block block = etc.getServer().getBlockAt((int)Math.floor(player.getX()), (int)Math.floor(player.getY()),(int)Math.floor(player.getZ()));
                    if(block.getType() == 10 || block.getType() == 11){
                        type = PluginLoader.DamageType.LAVA;
                    }
                }
                pendingDeaths.put(player, new Object[]{type, attacker, amount});
            }
        }
        return false;
    }

    @Override
    public boolean onHealthChange(Player player, int oldValue, int newValue) {
        if(newValue <= 0){
            HashMap<String, String> vars = new HashMap<String,String>();
            vars.put("player",player.getName());
            if(pendingDeaths.containsKey(player)){
                Object[] deathVars = pendingDeaths.get(player);
                pendingDeaths.remove(player);
                PluginLoader.DamageType type = (PluginLoader.DamageType)deathVars[0];
                BaseEntity attacker = (BaseEntity)deathVars[1];
                int amount = (Integer)deathVars[2];
                vars.put("damage",Integer.toString(amount));
                vars.put("message",getMessage(type));
                if(attacker != null){
                    double dist = calcDist(player, attacker);
                    vars.put("distance", Double.toString(dist));
                    if(attacker.isPlayer()){
                        Player killer = attacker.getPlayer();
                        if(!killer.getName().equals(player.getName())){
                            vars.put("killer",killer.getName());
                            String item = etc.getDataSource().getItem(killer.getItemInHand());
                            if(item.equals("-1")) item = "bare hands";
                            vars.put("item", item);
                            if(dist > 3.0){
                                vars.put("message", getMessage("ranged"));
                            }else{
                                vars.put("message", getMessage("player"));
                            }
                        }else{
                            vars.put("message", getMessage("suicide"));
                        }
                    }else if(attacker.isMob()){
                        try{
                            vars.put("killer",in.b(attacker.getEntity()));
                            if(in.b(attacker.getEntity()).equalsIgnoreCase("creeper")){
                                vars.put("message",getMessage("creeper"));
                            }
                        } catch (Exception ex) {
                            vars.put("killer","mob");
                        }
                    }else if(attacker.isAnimal()){
                        vars.put("killer","animal");
                    }else{
                        vars.put("killer","unknown entity");
                    }
                }
            }else{
                vars.put("message",getMessage("unknown"));
            }
            broadcastMessage(createDeathMessage(vars));
        }
        return false;
    }
@Override
    public void onDisconnect(Player player){
        if(pendingDeaths.containsKey(player)) pendingDeaths.remove(player);
    }

    private String createDeathMessage(HashMap<String,String> vars){
        String str = vars.get("message");
        vars.remove("message");
        String[] keys = (String[])vars.keySet().toArray(new String[vars.keySet().size()]);
        for(String key : keys){
            str = str.replaceAll(new StringBuilder("<").append(key).append(">").toString(),vars.get(key));
        }
        str = str.replaceAll("<[cC]([0-9a-fA-F])>","§$1");
        str = str.replaceAll("<n>( +[oOiIuUaAeE])","n$1");
        str = str.replaceAll("<n>","");
        return str;
    }

    private void broadcastMessage(String message){
        for(Player player : etc.getServer().getPlayerList()){
            player.sendMessage(message);
        }
    }

    private double calcDist(BaseEntity entity1, BaseEntity entity2){
        double dist = 0.00;
        double x = entity1.getX()-entity2.getX();
        double y = entity1.getY()-entity2.getY();
        double z = entity1.getZ()-entity2.getZ();
        dist = Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));
        dist = Math.round(dist*100)/100;
        return dist;
    }
    private String getMessage(String string){
        ArrayList<String> messageList = null;
        if(messages.containsKey(string)){
            messageList = messages.get(string);
        }
        return getRandomMessage(messageList);
    }

    private String getMessage(PluginLoader.DamageType type){
        ArrayList<String> messageList = null;
        String str = "";
        switch(type){
            case CREEPER_EXPLOSION:
                str = "creeper";
                break;
            case FIRE_TICK:
                str = "fire";
                break;
            default:
                str = type.toString().toLowerCase();
                break;
        }
        return getMessage(str);
    }

    public void loadMessages(){
        File file = new File("obituary_messages.txt");
        if(file.exists()){
            try {
                FileInputStream stream = new FileInputStream("obituary_messages.txt");
                DataInputStream data = new DataInputStream(stream);
                BufferedReader reader = new BufferedReader(new InputStreamReader(data));
                String str;
                while ((str = reader.readLine()) != null){
                    addMessage(str);
                }
                data.close();
            }catch (Exception ex){
              System.err.println("Error: " + ex.getMessage());
            }
        }else{
            log.log(Level.SEVERE,"File obituary_messages.txt does not exist. Please copy it from the JAR or refer to the plugin documenation.");
        }
    }

    private void addMessage(String message){
        int i = message.indexOf(":");
        String name = "";
        if(i != -1){
            name = message.substring(0, i).toLowerCase();
            message = message.substring(i+1);
        }
        if(!messages.containsKey(name)){
            messages.put(name, new ArrayList<String>());
        }
        messages.get(name).add(message);
    }

    private String getRandomMessage(ArrayList<String> stringList){
        if(stringList == null){
             stringList = new ArrayList<String>();
             stringList.add("<cc><player> was killed.");
        }
        String[] strings = (String[])stringList.toArray(new String[stringList.size()]);
        return strings[generator.nextInt(strings.length)];
    }
}
