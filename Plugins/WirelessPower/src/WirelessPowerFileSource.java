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

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public class WirelessPowerFileSource extends WirelessPowerDataSource {
    private File file;

    public WirelessPowerFileSource(String filepath){
        file = new File(filepath);
        if(!file.exists()){
            try{
                file.createNewFile();
            }catch(Exception ex){
                log.log(Level.SEVERE,"Could not create transmitter data file.");
            }
        }
    }

    protected List<Object[]> loadTransmitters(){
        List<Object[]> transmitterData = new ArrayList<Object[]>();
        try {
            FileInputStream stream = new FileInputStream(file);
            DataInputStream data = new DataInputStream(stream);
            BufferedReader reader = new BufferedReader(new InputStreamReader(data));
            String str;
            HashMap<String, String> vars = new HashMap<String, String>();
            while ((str = reader.readLine()) != null){
                str = str.toLowerCase();
                if(str.matches("[a-z0-9_]+;(,?(:?-?[0-9]+){3}){3};[0-9];.+")){
                    String[] split = str.split(";");
                    String channel = "";
                    for(int i = 3;i<split.length;i++){
                        channel += split[i];
                    }
                    transmitterData.add(new Object[]{split[0], split[1], getTypeFromInt(Integer.parseInt(split[2])),channel});
                }
            }
            data.close();
            reader.close();
        }catch (Exception ex){
            log.log(Level.SEVERE,"Could not read from file: "+file.getName());
        }
        return transmitterData;
    }

    public void deleteTransmitter(Transmitter transmitter){
        List<Object[]> transmitterData = loadTransmitters();
        for(Object[] obj : transmitterData){
            if(getBlockStrings(transmitter.getBlocks()).equals((String)obj[1])){
                transmitterData.remove(obj);
                break;
            }
        }
        try{
            FileWriter stream = new FileWriter(file);
            BufferedWriter writer = new BufferedWriter(stream);
            for(Object[] obj : transmitterData){
                StringBuilder str = new StringBuilder((String)obj[0]).append(";");
                str.append((String)obj[1]).append(";");
                str.append(Integer.toString(((Transmitter.Type)obj[2]).intval())).append(";");
                str.append((String)obj[3]);
                writer.write(str.toString());
                writer.newLine();
            }
            writer.close();
        }catch(Exception ex){
            log.log(Level.SEVERE,"Could not write to file: "+file.getName());
        }

    }

    public void saveTransmitter(Transmitter transmitter){
        StringBuilder str = new StringBuilder(transmitter.getOwner()).append(";");
        str.append(getBlockStrings(transmitter.getBlocks())).append(";");
        str.append(transmitter.getType().intval()).append(";");
        str.append(transmitter.getChannel());
        try{
            FileWriter stream = new FileWriter(file,true);
            BufferedWriter writer = new BufferedWriter(stream);
            writer.write(str.toString());
            writer.newLine();
            writer.close();
        }catch(Exception ex){
            log.log(Level.SEVERE,"Could not write to file: "+file.getName());
        }
    }


}
