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

public class AutoSneakListener extends PluginListener{
    private ArrayList<Player> players = new ArrayList<Player>();
    public AutoSneakListener(){

    }
@Override
    public boolean onCommand(Player player, String[] split) {
        boolean result = false;
        if(split[0].equalsIgnoreCase("/sneak") && player.canUseCommand("/sneak")){
            toggleSneak(player);
            result = true;
        }
        return result;
    }

@Override
    public void onLogin(Player player) {
        ArrayList<String[]> commandLists = new ArrayList<String[]>();
        commandLists.add(player.getCommands());
        for(String groupName : player.getGroups()){
            for(String[] commands : getRecursiveCommands(groupName)){
                commandLists.add(commands);
            }
        }
        Loop:
        for(String[] commands : commandLists){
            for(String command : commands){
                if(command.equals("/autosneak")){
                    setSneak(player, true);
                    break Loop;
                }
            }
        }
    }

    private ArrayList<String[]> getRecursiveCommands(String groupName){
        ArrayList<String[]> commands = new ArrayList<String[]>();
        Group group = etc.getDataSource().getGroup(groupName);
        if(group != null){
            if(group.Commands != null) commands.add(group.Commands);
            for(String inheritName : group.InheritedGroups){
                for(String[] groupCommands : getRecursiveCommands(inheritName)){
                    commands.add(groupCommands);
                }
            }
        }
        return commands;
    }

    private void toggleSneak(Player player){
        if(players.contains(player)){
            player.sendMessage("§7You have stopped sneaking.");
            setSneak(player, false);
        }else{
            player.sendMessage("§7You are now sneaking.");
            setSneak(player, true);
        }
    }

    private void setSneak(Player player, boolean sneak){
        if(sneak){
            player.setSneaking(true);
            if(!players.contains(player)){
                players.add(player);
            }
        }else{
            player.setSneaking(false);
            if(players.contains(player)){
                players.remove(player);
            }
        }
    }

@Override
    public void onDisconnect(Player player){
        if(players.contains(player)) players.remove(player);
    }

@Override
    public void onPlayerMove(Player player, Location from, Location to) {
        if(players.contains(player) && player.getSneaking() == false){
            player.setSneaking(true);
        }
    }
}
