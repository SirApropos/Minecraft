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
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.util.HashMap;
        

public class WirelessPowerMysqlSource extends WirelessPowerDataSource{
    private Mysql mysql;
    private String table;

    public WirelessPowerMysqlSource(Mysql mysql, String table){
        this.mysql = mysql;
        this.table = table;
    }

    protected List<Object[]> loadTransmitters(){
        List<Object[]> transmitterData = new ArrayList<Object[]>();
        ResultSet rs = this.mysql.query("SELECT *, CONVERT(blockType, SIGNED) AS blockTypeInt FROM " + this.table);
        HashMap<String, Object> row;
        String[] coords;
        Transmitter.Type blockType;
        for(int i=0;i<this.mysql.numRows(rs);i++){
            blockType = null;
            row = this.mysql.fetchHashMap(rs);
            coords = new String[3];
            blockType = getTypeFromInt(((Long)row.get("blockTypeInt")).intValue());
            transmitterData.add(new Object[]{(String)row.get("owner"), (String)row.get("blocks"), blockType,(String)row.get("channel")});
        }
        return transmitterData;
    }

    public void deleteTransmitter(Transmitter transmitter){
        String blockString = getBlockStrings(transmitter.getBlocks());
        StringBuilder query = new StringBuilder("DELETE FROM ").append(this.table);
        query.append(" WHERE blocks = '").append(blockString).append("'");
        this.mysql.update(query.toString());
    }

    public void saveTransmitter(Transmitter transmitter){
        HashMap<String,Object> data = new HashMap<String,Object>();
        String blockString = getBlockStrings(transmitter.getBlocks());
        int type = transmitter.getType().intval();
        data.put("owner", transmitter.getOwner());
        data.put("blocks", blockString);
        data.put("blockType", type);
        data.put("channel", transmitter.getChannel());
        this.mysql.insert(this.table, data);
    }
}
