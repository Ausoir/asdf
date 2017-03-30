package com.ga.game;

import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.map.MapGenerator;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.*;
import com.ga.unit.Stock;
import com.ga.unit.intelligence.PlayerInput;
import com.ga.unit.skill.Actions;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ga on 7/26/2016.
 */
public class FileManager {
    public static void saveCharacter(){
        if (Global.player.state != Actions.Dead) {
            saveMap("map/"+Global.player.cmap);
            save("map/0", Global.player.toCharacterString());
        }
    }
    private static void save(String dir, String input) {
        try {
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter("Data/"+dir));
                writer.write(input);
            } finally {
                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void saveMap(String name){
        String result = "" + Global.map.toString();
        result += "\n";
        result += ";"+Updater.toMapString();
        save(name+".map", result);
    }
    private static String read(String dir) {
        String result = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader("Data/"+dir));
            try {
                String line = br.readLine().split(" //")[0];
                result = line + "%";
                while (line != null) {
                    line = br.readLine();
                    if (line != null) result += line.split(" //")[0] + "%";
                }
                result = result.substring(0, result.length() - 1);
            } finally {
                br.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println(result);
        return result;
    }
    public static PlayerInput loadPlayer(boolean main, String dir){
        String data = read("map/"+dir);
        String[] line = data.split("%");
        PlayerInput pi;
        String playerString = line[0];
        pi = new PlayerInput(main ? new MainPlayer(playerString) : new Player(playerString));
        pi.host.load(data);
        pi.host.controlId = -Integer.parseInt(dir)-1;
        pi.load(line[2]);
        if (!Updater.editor) Global.display.input = pi;
        Command.host = pi.host;
        loadMap(Global.player.cmap);
        Global.setHour(Global.gameHour);
        Global.mapEdgeCenter(Global.camera = Global.map.size.getMult(.5f,.5f));
        return pi;
    }
    public static void delete(String dir){
        File file = new File("Data/"+dir);
        if (file.exists()){
            if (!file.delete()) System.out.println("Instance Map File Deletion Failed! ("+dir+")");
        }
    }
    public static int loadMap(String map) {
        Global.thouseId = -1;
        Global.player.lastLand = new Line();
        String[] oldmap = null;
        String[] mapProperties = map.split("@");
        String dir = mapProperties[0];
        int regrowths;
        int player = -999;
        if (mapProperties.length > 1) player = Integer.parseInt(mapProperties[1]);
        if (Global.map != null) oldmap = Global.map.maps;
        if (oldmap != null && dir.startsWith("House")){
            String[] houseInfo = dir.split("/");
            if (houseInfo.length > 1) dir = houseInfo[0];
            else houseInfo = new String[]{houseInfo[0], "1200,800"};
            if (new File("Data/map/" + dir + ".map").isFile()) readMap(dir);
            else MapGenerator.generateHouse(Global.player.cmap, Integer.parseInt(dir.split("_")[1]), houseInfo[1]);
            Global.player.cmap = dir;
            regrowths = 0;
        } else if (oldmap != null && dir.startsWith("Instance:")){
            String instanceData[] = dir.split(":")[1].split("/");
            String[] param = instanceData[0].split("&");
            String returnAddr;
            if (instanceData.length > 1){
                returnAddr = instanceData[1].split("&")[1];
            } else {
                String[] t = oldmap[4].split(":")[1].split("@")[0].split("&");
                returnAddr = t[1];
            }
            int maxDist = Math.max(1,Integer.parseInt(param[1]));
            int returndist = Global.player.fastTravel ? Global.r.nextInt(maxDist) + 1 : 1;
            if (Global.player.get("Hunger").get() < 0)
                returndist = Math.max(1,Math.min(Global.player.get("Health").get()/(Global.player.get("Hunger").get()*-2), returndist));
            int distance = maxDist - returndist;
            if (distance == 0) readMap(param[2]);
            else if (returndist == 0) readMap(returnAddr);
            else {
                boolean rightSide = Global.player.pos.x > Global.map.size.x/2;
                String destination = "Instance:"+param[0]+"&"+distance+"&"+param[2]+"@0";
                String prevmap = "Instance:"+param[0]+"&"+returndist+"&"+returnAddr+"@0";
                Global.player.cmap = "Instance";
                MapGenerator.generateMap(param[0], rightSide, prevmap, destination);
            }
            regrowths = distance;
        } else {
            readMap(dir);
            regrowths = oldmap != null && !oldmap[0].equals("1") && !Global.map.isIndoors() ? 1 : 0;
        }
        Global.map.setTexture();
        Global.map.event.onChange(regrowths);
        return player;
    }
    private static void readMap(String dir){
        String data = read("map/" + dir + ".map");
        String[] line = data.split("%");
        String[] names = line[0].split(",");
        Global.map = new Map(names);
        if (line.length > 1) {
            String[] sections = line[1].split(";");
            for (int section = 0; section < sections.length; section++) {
                String values[] = sections[section].split(":");
                Global.map.formTextures.put(Integer.parseInt(values[0]), values[1]);
            }
            if (line.length > 2) {
                sections = line[2].split(";");
                for (int section = 0; section < sections.length; section++) {
                    Line surface = new Line(sections[section]);
                    Global.map.land.add(surface);
                }
                if (line.length > 3 && !line[3].equals(";")) {
                    sections = line[3].substring(1).split(";");
                    if (sections.length > 0) {
                        for (int section = 0; section < sections.length; section++) {
                            String[] property = sections[section].split("@");
                            Stock.get(property);
                        }
                    }
                }
            }
        }
        Global.player.cmap = dir;
    }

    private static int[] readClipFrames(String dir) {
        String data = read(dir);
        int[] result;
        String[] lines = data.split("%%")[1].split("%");
        result = new int[lines.length];
        for (int line = 0; line < lines.length; line++) {
            result[line] = Integer.parseInt(lines[line].split("/")[1]);
        }
        return result;
    }

    private static int[][] readModelProperty(String dir) {
        String data = read(dir);
        int[][] result;
        String line = data.split("%")[1];
        String[] sections = line.split(";");
        result = new int[sections.length][];
        for (int section = 0; section < sections.length; section++) {
            String values[] = sections[section].split(",");
            int[] v = new int[values.length];
            for (int i = 0; i < values.length; i++) {
                v[i] = Integer.parseInt(values[i]);
            }
            result[section] = v;
        }
        return result;
    }

    private static String[][] readModel(String dir) {
        String data = read(dir);
        String[][] result;
        String line = data.split("%")[0];
        String[] sections = line.split(";");
        result = new String[sections.length][];
        for (int section = 0; section < sections.length; section++) {
            String values[] = sections[section].split(",");
            result[section] = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                result[section][i] = values[i];
            }
        }
        return result;
    }

    private static int[][][] readAnimation(String dir) {
        String data = read(dir);
        int[][][] result;
        String[] lines = data.split("%%")[1].split("%");
        result = new int[lines.length][][];
        for (int line = 0; line < lines.length; line++) {
            String[] sections = lines[line].split("/")[0].split(";");
            result[line] = new int[sections.length][];
            for (int section = 0; section < sections.length; section++) {
                String values[] = sections[section].split(",");
                result[line][section] = new int[values.length];
                int[] v = new int[values.length];
                for (int i = 0; i < values.length; i++) {
                    v[i] = Integer.parseInt(values[i]);
                    result[line][section][i] = v[i];
                }
            }
        }
        return result;
    }

    private static HashMap<String, int[]> readMoveset(String dir) {
        String data = read(dir);
        HashMap<String, int[]> result;
        String[] lines = data.split("%%")[2].split("%");
        result = new HashMap<>();
        for (int line = 0; line < lines.length; line++) {
            String[] sections = lines[line].split("@");
            String values[] = sections[1].split(",");
            int[] v = new int[values.length];
            for (int i = 0; i < values.length; i++) {
                v[i] = Integer.parseInt(values[i]);
            }
            result.put(sections[0],v);
        }
        return result;
    }

    public static Clip readClip(String dir) {
        return new Clip(readModelProperty(dir), readModel(dir), readAnimation(dir), readClipFrames(dir), readMoveset(dir));
    }

    public static HashMap<String, ArrayList<String>> readDialogue(String dir){
        String data = read(dir);
        String[] lines = data.split("%");
        HashMap<String,ArrayList<String>> result = new HashMap<>();
        for (int line = 0; line < lines.length; line++) {
            String[] type = lines[line].split("@");
            String[] results = type[1].split("/");
            ArrayList<String> values = new ArrayList<>();
            for (String i : results) values.add(i);
            result.put(type[0],values);
        }
        return result;
    }

    public static String[] readText(String dir){
        String data = read(dir+".txt");
        return data.split("%");
    }
}
