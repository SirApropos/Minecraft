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
import java.util.ArrayList;

public class AutoSneakListener extends PluginListener{
    private final Logger log  = Logger.getLogger("Minecraft");
    private ArrayList<Player> players = new ArrayList<Player>();
    public AutoSneakListener(){

    }
@Override
    public boolean onCommand(Player player, String[] split) {
        boolean result = false;
        if(split[0].equalsIgnoreCase("/sneak") && player.canUseCommand("/sneak")){
            if(players.contains(player)){
                player.sendMessage("§7You have stopped sneaking.");
                player.setSneaking(false);
                players.remove(player);
            }else{
                players.add(player);
                player.sendMessage("§7You are now sneaking.");
                player.setSneaking(true);
            }
            result = true;
        }
        return result;
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
