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
import java.util.logging.Logger;

public class WirelessPowerListener extends PluginListener {
    private List<Player> players = new ArrayList<Player>();
    private static WirelessPowerMonitor monitor;
    private static final Logger log = Logger.getLogger("Minecraft");

    public WirelessPowerListener(WirelessPowerMonitor localMonitor){
        monitor = localMonitor;
    }

@Override
    public boolean onSignChange(Player player, Sign sign) {
        monitor.createTransmitter(player, sign);
        return false;
    }

@Override
    public void onBlockRightClicked(Player player, Block block, Item item) {
        if(block.getType() == 68 && !monitor.isWatched(block)){
            monitor.createTransmitter(player, (Sign)etc.getServer().getComplexBlock(block.getX(), block.getY(), block.getZ()));
        }
    }

@Override
    public int onRedstoneChange(Block block, int oldLevel, int newLevel) {
        if(monitor.isWatched(block)){
            Transmitter transmitter = monitor.belongsTo(block);
            if(transmitter.getType() == Transmitter.Type.TRANSMITTER){
                boolean bool = false;
                if(newLevel == 1){
                    bool = true;
                }
                transmitter.changeTorch(bool);
                transmitter.update();
            }else{
                newLevel = oldLevel;
            }
        }
        return newLevel;
    }

@Override
    public void onSignShow(Player player, Sign sign) {
        if(monitor.isWatched(sign.getBlock())){
            Transmitter transmitter = monitor.belongsTo(sign.getBlock());
            boolean owned = transmitter.getOwner().equalsIgnoreCase(player.getName());
            if(sign.getText(3).equalsIgnoreCase("[private]") && !owned){
                sign.setText(1,"");
                sign.setText(2,"");
            }else{
                sign.setText(1, new StringBuilder("[").append(transmitter.getType().value()).append("]").toString());
                sign.setText(2, transmitter.getChannel());
            }
        }
    }

@Override
    public boolean onBlockBreak(Player player, Block block) {
        boolean result = false;
        if(monitor.isWatched(block)){
            if(monitor.isProtected() && !monitor.belongsTo(block).getOwner().equalsIgnoreCase(player.getName())){
                result = true;
            }else{
                monitor.removeTransmitterFromBlock(block);
            }
        }
        return result;
    }

@Override
    public boolean onFlow(Block blockFrom, Block blockTo) {
        boolean result = false;
        if(monitor.isWatched(blockTo)){
            if(blockTo.getType() == 75 || blockTo.getType() == 76){
                if(monitor.isProtected()){
                    result = true;
                }else{
                    monitor.removeTransmitterFromBlock(blockTo);
                }
            }
        }
        return result;
    }
}
