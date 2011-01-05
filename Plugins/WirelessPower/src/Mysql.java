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
import java.sql.Connection;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Set;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.HashMap;

public class Mysql {
    private Connection connection;

    private String server;
    private String user;
    private String pass;
    private String driver;
    private String db;
    private ResultSet rs;

    private static final Logger log = Logger.getLogger("MySQL");

    public Mysql(){
    }

    public Mysql(Connection connection){
        this.connection = connection;
    }

    public Mysql(String server, String user, String pass, String driver, String db){
        connect(server, user, pass, driver, db);
    }

    final public boolean connect(String server, String user, String pass, String driver, String db){
        boolean result = false;
        this.server = server;
        this.user = user;
        this.pass = pass;
        this.driver = driver;
        this.db = db;
        try{
            this.connection = DriverManager.getConnection(db + "?autoReconnect=true&user=" + user + "&password=" + pass);
            result=true;
        } catch (SQLException localException) {
        }
        return result;
    }

    public ResultSet query(String query){
        ResultSet result = null;
        try{
            if(this.rs != null && !this.rs.isClosed()){
                this.rs.close();
            }
            result = this.connection.prepareStatement(query).executeQuery();
        }catch(SQLException ex){
            handleException(ex);
        }
        return result;
    }
    
    public boolean update(String query){
        boolean result = false;
        try{
            result = this.connection.prepareStatement(query).executeUpdate() > 0 ? true : false;
        }catch(SQLException ex){
            handleException(ex);
        }
        return result;
    }

    public Object[] fetchArray(ResultSet rs){
        Object[] result = null;
        try{
            int columnCount = rs.getMetaData().getColumnCount();
            result = new Object[columnCount];
            if(rs.next()){
                for(int i=0;i<columnCount;i++){
                    result[i] = rs.getObject(i);
                }
            }
        }catch(SQLException ex){
            handleException(ex);
        }
        return result;
    }

    public HashMap<String, Object> fetchHashMap(ResultSet rs){
       HashMap<String, Object> result = null;
        try{
            ResultSetMetaData data = rs.getMetaData();
            int columnCount = data.getColumnCount();
            if(rs.next()){
                result = new HashMap<String,Object>();
                for(int i=1;i<=columnCount;i++){
                    result.put(data.getColumnLabel(i),rs.getObject(i));
                }
            }
        }catch(SQLException ex){
            handleException(ex);
        }
        return result;
    }

    public int numRows(ResultSet rs){
        int result = 0;
        try{
            int cur = rs.getRow();
            rs.last();
            result = rs.getRow();
            if(cur > 0){
                rs.absolute(cur);
            }else{
                rs.beforeFirst();
            }
        }catch(SQLException ex){
            handleException(ex);
        }
       return result;
    }

    public ResultSet select(String table, String cond){
        return select(table, new String[]{"*"}, cond);
    }

    public ResultSet select(String table, String[] columns, String add){
        StringBuilder queryString = new StringBuilder("SELECT ").append(implode(",",columns));
        queryString.append(" FROM ").append(table);
        queryString.append(" ").append(add);
        return query(queryString.toString());
    }

    public boolean insert(String table, HashMap<String, Object> objects){
        StringBuilder queryString = new StringBuilder("INSERT INTO ").append(table);
        queryString.append(getKeyString(objects.keySet()));
        queryString.append(" VALUES").append(getValuesString(objects.values().toArray()));
        return update(queryString.toString());
    }
    public boolean insert(String table, Object[] objects){
        return insert(table, new Object[][]{objects});
    }

     public boolean insert(String table, Object[][] objects){
        StringBuilder queryString = new StringBuilder("INSERT INTO ").append(table);
        queryString.append(" VALUES").append(getValuesString(objects));
        return update(queryString.toString());
    }
    private String getKeyString(Set<String> keySet){
        Object[] keyObj = keySet.toArray();
        String[] keyStrings = new String[keyObj.length];
        for(int i=0;i<keyObj.length;i++){
            keyStrings[i] = (String)keyObj[i];
        }
        return new StringBuilder("(`").append(implode("`,`",keyStrings)).append("`)").toString();
    }
 
    private String getValuesString(Object[] objects){
        Object[] strings = new Object[objects.length];
        int i = 0;
        for(Object obj :objects){
            if(!(obj instanceof Number)){
                if(!(obj instanceof String)) obj = toString(obj);
                obj = new StringBuilder("'").append(((String)obj).replaceAll("'", "\'")).append("'").toString();
            }
            strings[i] = obj;
            i++;
        }
        return new StringBuilder("(").append(implode(",",strings)).append(")").toString();
    }

    private String getValuesString(Object[][] objects){
        int i = 0;
        String[] pieces = new String[objects.length];
        for(Object[] obj : objects){
            pieces[i] = getValuesString(obj);
            i++;
        }
        return implode(",",pieces);
    }

    private String implode(String glue, Object[] pieces){
        StringBuilder glueString = new StringBuilder("");
        if(pieces.length>0){
            glueString.append(toString(pieces[0]));
            for(int i = 1;i<pieces.length;i++){
                glueString.append(glue).append(toString(pieces[i]));
            }
        }
        return glueString.toString();
    }

    private String toString(Object obj){
        return obj.toString();
    }

    private void handleException(SQLException ex){
        StringBuilder err = new StringBuilder("[MySQL] Error: ").append(ex.getMessage());
        StackTraceElement[] st = ex.getStackTrace();
        err.append("\n").append("Method: ").append(st[0].getMethodName());
        err.append(" at line number ").append(st[0].getLineNumber());
        log.log(Level.SEVERE, err.toString());
    }

    private void handleException(Exception ex){
        log.log(Level.SEVERE, new StringBuilder("[").append(ex.getClass().getCanonicalName()).append(
                "] Error: ").append(ex.getMessage()).toString());
    }
}