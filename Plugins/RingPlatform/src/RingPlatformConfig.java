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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.*;

public class RingPlatformConfig {
    private ArrayList<HashMap<String, Object>> shapes = new ArrayList<HashMap<String, Object>>();
    private ArrayList<HashMap<String, Object>> animations = new ArrayList<HashMap<String, Object>>();
    private HashMap<String, Object> vars = new HashMap<String, Object>();
    private static final Logger log = Logger.getLogger("minecraft");
    private String filename = "RingPlatform.properties";

    public RingPlatformConfig(){
    }

    public void load(){
        shapes.clear();
        animations.clear();
        vars.clear();
        vars.put("check_permissions", false);
        vars.put("max_platforms_in_stack",-1);
        File file = new File(filename);
        if(!file.exists()){
            createDefaultShape();
            save();
        }else{
            try{
                parseConfig();
                save();
            }catch(ParseException ex){
                log.log(Level.SEVERE,"[RingPlatform] "+ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void save(){
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("#RingPlatform variables");
        for(String str : vars.keySet()){
            lines.add(new StringBuilder(str).append("=").append(vars.get(str).toString()).toString());
        }
        lines.add("");
        lines.add("#Advanced options. Do not change unless you know what you're doing.");
        lines.add("#Begin RingPlatform shapes");
        for(HashMap<String,Object> shape : shapes){
            lines.add("#Start shape");
            lines.add("shape:{");
            ArrayList<String> attached = (ArrayList<String>)shape.get("animations");
            shape.remove("animations");
            lines.addAll(getObjectString(shape));
            shape.put("animations", attached);
            for(String anim : attached){
                lines.add("    attach:"+anim);
            }
            lines.add("}");
            lines.add("#End shape");
        }
        lines.add("#End RingPlatform shapes");
        lines.add("");
        lines.add("#Begin RingPlatform animations");
        for(HashMap<String,Object> animation : animations){
            lines.add("#Start animation");
            lines.add("animation:{");
            ArrayList<String> frames = (ArrayList<String>)animation.get("frames");
            animation.remove("frames");
            lines.addAll(getObjectString(animation));
            animation.put("frames", frames);
            for(String frame : frames){
                lines.add("    frame:"+frame);
            }
            lines.add("}");
            lines.add("#End animation");
        }
        lines.add("#End RingPlatform animations");
        File file = new File(filename);
        try {
            FileWriter stream = new FileWriter(file);
            BufferedWriter writer = new BufferedWriter(stream);
            for(String ln : lines){
                writer.write(ln);
                writer.newLine();
            }
            writer.close();
        }catch (Exception ex){
          log.log(Level.SEVERE,"Error writing file: " + ex.getMessage());
        }
    }
    
    private ArrayList<String> getObjectString(HashMap<String, Object> shape){
        ArrayList<String> lines = new ArrayList<String>();
        for(String key : shape.keySet()){
            Object obj = shape.get(key);
            if(obj instanceof int[][]){
                lines.add("    "+key+":{");
                lines.addAll(getShapeString((int[][])obj));
                lines.add("    }");
            }else{
                StringBuilder str = new StringBuilder("    ");
                str.append(key).append("=").append(obj.toString());
                lines.add(str.toString());
            }
        }
        return lines;
    }
    
    private ArrayList<String> getShapeString(int[][] shape){
        ArrayList<String> result = new ArrayList<String>();
        for(int i = 0; i < shape.length; i++){
            int[] line = shape[i];
            StringBuilder str = new StringBuilder("        {");
            str.append(Integer.toString(line[0]));
            for(int j = 1; j < line.length ; j++){
                str.append(",").append(Integer.toString(line[j]));
            }
            str.append("}");
            if(i < shape.length -1) str.append(",");

            result.add(str.toString());
        }
        return result;
    }

    public ArrayList<HashMap<String, Object>> getShapes(){
        return shapes;
    }

    private void parseConfig() throws ParseException{
        int line = 0;
        try{
            FileInputStream stream = new FileInputStream(filename);
            DataInputStream data = new DataInputStream(stream);
            BufferedReader reader = new BufferedReader(new InputStreamReader(data));
            String str;
            while ((str = reader.readLine()) != null){
                line++;
                str = str.replace(" ", "");
                if(str.matches("[a-zA-Z0-9_]+=[^=]+")){
                    int i = str.indexOf("=");
                    vars.put(str.substring(0, i).toLowerCase(),str.substring(i+1));
                }else if(str.matches("shape:\\{") || str.matches("animation:\\{")){
                    ArrayList<String> objLines = new ArrayList<String>();
                    int i = str.indexOf(":");
                    String type = str.substring(0,i).toLowerCase();
                    str = "{";
                    while(!(parseBrackets(str))){
                        line++;
                        String add;
                        if((add = reader.readLine()) != null){
                            add = add.replace(" ", "");
                            str += add;
                            objLines.add(add);
                        }else{
                            throw new ParseException(line);
                        }
                    }
                    objLines.remove(objLines.size()-1);
                    objLines.trimToSize();
                    HashMap<String, Object> obj = parseObject(objLines);
                    if(type.equals("shape")){
                        shapes.add(obj);
                    }else{
                        if(!obj.containsKey("delay")) obj.put("delay", 100L);
                        animations.add(obj);
                    }
                }else if(str.length() > 0 && !(str.matches("\\#.+"))){
                    throw new ParseException(line);
                }
            }
        if(vars.get("check_permissions") instanceof String) vars.put("check_permissions",Boolean.parseBoolean((String)vars.get("check_permissions")));
        if(vars.get("max_platforms_in_stack") instanceof String) vars.put("max_platforms_in_stack",Integer.parseInt((String)vars.get("max_platforms_in_stack")));
        }catch(Exception ex){
            throw new ParseException(line);
        }
    }

    private boolean parseBrackets(String str){
        //Terrible, but can't make a complex enough regex pattern.
        int leftCount = 0;
        int rightCount = 0;
        for(int i=0;i<str.length();i++){
            String chr = str.substring(i,i+1);
            if(chr.equals("{")) leftCount++;
            if(chr.equals("}")) rightCount++;
        }
        return (leftCount == rightCount);
    }
    private HashMap<String, Object> parseObject(ArrayList<String> lines) throws ParseException{
        HashMap<String, Object> result = new HashMap<String, Object>();
        for(int currentLine = 0; currentLine < lines.size(); currentLine++){
            String line = lines.get(currentLine);
            if(line.matches("[a-zA-Z0-9_]+=[^=]+")){
                int i = line.indexOf("=");
                result.put(line.substring(0, i).toLowerCase(),line.substring(i+1));
            }else if(line.matches("[a-zA-Z]+:\\{")){
                int i = line.indexOf(":");
                String objName = line.substring(0,i);
                line = "{";
                while(!(parseBrackets(line))){
                    currentLine++;
                    if(currentLine<lines.size()){
                        line += lines.get(currentLine);
                    }else{
                        throw new ParseException(currentLine);
                    }
                }
                result.put(objName, parseShape(line));
            }else if(line.matches("^attach:.+")){
                if(!result.containsKey("animations")) result.put("animations",new ArrayList<String>());
                int i = line.indexOf(":");
                ((ArrayList<String>)result.get("animations")).add(line.substring(i+1));
            } else if (line.matches("frame:.+")) {
                if(!result.containsKey("frames")) result.put("frames",new ArrayList<String>());
                int i = line.indexOf(":");
                ((ArrayList<String>)result.get("frames")).add(line.substring(i+1));
            }else if (line.length() >= 0 && !(line.matches("\\#.+"))) {
                    throw new ParseException(currentLine);
            }
        }
        Boolean test = true;
        if(!(result.containsKey("name") && result.containsKey("height"))) test = false;
        if(!((result.containsKey("platformShape") && result.containsKey("teleportShape")) || result.containsKey("shape"))) test = false;
        if(result.containsKey("platformShape") ^ result.containsKey("animations")) test = false;
        if(result.containsKey("shape") ^ result.containsKey("frames")) test = false;
        if(!test){
            throw new ParseException();
        }
        result.put("height",Integer.parseInt((String)result.get("height")));
        if(result.containsKey("delay")) result.put("delay",Long.parseLong((String)result.get("delay")));
        return result;
    }

    private int[][] parseShape(String str) throws ParseException{
        str = str.substring(1,str.length()-1);
        String[] split = str.split("\\},\\{");
        int[][] result = new int[split.length][split.length];
        try{
            for(int i = 0; i < split.length; i++){
                String line = split[i].replaceAll("[{}]", "");
                String[] lineSplit = line.split(",");
                if(lineSplit.length != split.length){
                    throw new ParseException();
                }
                for(int j = 0; j < lineSplit.length ; j++){
                    result[i][j] = Integer.parseInt(lineSplit[j].replaceAll("[^0-9]", ""));
                }

            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return result;
    }

    public boolean checkPerms(){
        return (Boolean)vars.get("check_permissions");
    }

    public int maxPlatformsInStack(){
        int result = (Integer)vars.get("max_platforms_in_stack") == -1 ? 127 : (Integer)vars.get("max_platforms_in_stack");
        return result;
    }

    public ArrayList<HashMap<String, Object>> getAnimations(ArrayList<String> animationList){
        ArrayList<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
        for(String animationName : animationList){
            try{
                result.add(getAnimation(animationName));
            }catch(NoSuchAnimationException ex){
                log.log(Level.SEVERE, "[RingPlatform] No such animation: "+ex.getMessage());
            }
        }
        return result;
    }

    public HashMap<String, Object> getAnimation(String animationName) throws NoSuchAnimationException{
        HashMap<String, Object> result = null;
        for(HashMap<String, Object> animation : animations){
            if(animationName.equalsIgnoreCase((String)animation.get("name"))){
                result = animation;
                break;
            }
        }

        if(result != null){
            return result;
        }else{
            throw new NoSuchAnimationException(animationName);
        }
    }

    private void createDefaultShape(){
        HashMap<String, Object> shape = new HashMap<String, Object>();
        HashMap<String, Object> animation = new HashMap<String, Object>();
        shape.put("name","defaultShape");
        shape.put("height",5);
        shape.put("platformShape", new int[][]{
            {0,1,1,1,0},
            {1,1,2,1,1},
            {1,2,1,2,1},
            {1,1,2,1,1},
            {0,1,1,1,0}});
        shape.put("teleportShape", new int[][]{
            {0,0,0,0,0},
            {0,1,1,1,0},
            {0,1,1,1,0},
            {0,1,1,1,0},
            {0,0,0,0,0}});
        ArrayList<String> animList = new ArrayList<String>();
        animList.add("defaultAnim");
        shape.put("animations", animList);
        shapes.add(shape);

        animation.put("name","defaultAnim");
        animation.put("height",5);
        animation.put("delay",100L);
        animation.put("shape", new int[][]{
            {0,1,1,1,0},
            {1,0,0,0,1},
            {1,0,0,0,1},
            {1,0,0,0,1},
            {0,1,1,1,0}});

        ArrayList<String> frames = new ArrayList<String>();

        frames.add("{{1,0},{2,0},{3,0},{4,0},{5,0}}"); //Special frame to remove everything if error occurs.
        frames.add("{{1,49}},prune:{1}");
        frames.add("{{1,0},{3,49}},prune:{2,3}");
        frames.add("{{3,0},{4,49}},prune:{4}");
        frames.add("{{4,0},{5,49}},prune:{5}");
        frames.add("{{1,49}}");
        frames.add("{{1,0},{2,49}}");
        frames.add("{{2,0},{3,49}}");
        frames.add("{{1,49}}");
        frames.add("protect:{{4,57}}");
        frames.add("protect:{}");
        frames.add("protect:{{2,57}}");
        frames.add("protect:{}");
        frames.add("teleport:protect:{}");
        frames.add("protect:{}");
        frames.add("protect:{}");
        frames.add("protect:{{4,0}}");
        frames.add("protect:{}");
        frames.add("protect:{{2,0}}");
        frames.add("{{1,0}}");
        frames.add("{{3,0},{2,49}}");
        frames.add("{{2,0},{1,49}}");
        frames.add("{{1,0}}");
        frames.add("{{5,0},{4,49}}");
        frames.add("{{4,0},{3,49}}");
        frames.add("{{3,0},{1,49}}");
        frames.add("{{1,0}}");
        animation.put("frames", frames);

        animations.add(animation);
    }

    private class ParseException extends Exception{
        String message = "Error parsing config";
        int line = 0;
        public ParseException(){
        }
        public ParseException(int line){
            this.line = line;
        }
        public ParseException(String message){
            this.message = message;
        }
@Override
        public String getMessage(){
            return this.message+" on line "+Integer.toString(line);
        }
    }

    private class NoSuchAnimationException extends Exception{
        String message = "";
        public NoSuchAnimationException(){
        }

        public NoSuchAnimationException(String message){
            this.message = message;
        }
@Override
        public String getMessage(){
            return this.message;
        }
    }
}
