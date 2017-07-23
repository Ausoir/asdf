package com.ga.game;

import com.ga.map.*;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Clip;
import com.ga.unit.PlayerInput;
import com.ga.unit.player.MainPlayer;
import com.ga.unit.player.Player;
import com.ga.unit.skill.Actions;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by Ga on 7/26/2016. asd
 */
public class FileManager {
    public static HashMap<String, ArrayList<String[]>> fileCache;

    static void init(){
        fileCache = new HashMap<>();
        searchFiles("Data", fileCache);
    }
    private static void searchFiles(String p, HashMap<String, ArrayList<String[]>> r){
        File[] files = new File(p).listFiles();
        if (files != null) {
            for (File file : files) {
                String path = file.getPath();
                if (file.isDirectory()) {
                    searchFiles(path, r);
                } else if (file.isFile() && file.length() > 0){
                    String t = file.getName();
                    String name, ext;
                    if (t.contains(".")){
                        ext = t.substring(t.indexOf(".")+1);
                        name = t.substring(0, t.indexOf("."));
                    } else {
                        ext = "!";
                        name = t;
                    }
                    if (r.get(ext) == null) r.put(ext,new ArrayList<String[]>());
                    r.get(ext).add(new String[]{name, path});
                }
            }
        }
    }
    public static void saveCharacter(){
        if (Global.player.state != Actions.Dead) {
            saveMap("map/"+Global.player.cmap);
            save("map/0.user", Global.player.toCharacterString());
        }
    }
    public static void save(String dir, String input) {
        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("Data/" + dir))) {
                writer.write(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void saveMap(String name){
        String result = "" + Global.map.toString();
        result += "\n";
        result += Updater.toMapString();
        save(name+".map", result);
    }
    public static String read(String dir) {
        String result = null;
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(dir))) {
                String line = br.readLine().split(" //")[0];
                result = line + "%";
                while (line != null) {
                    line = br.readLine();
                    if (line != null) result += line.split(" //")[0] + "%";
                }
                result = result.substring(0, result.length() - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    public static PlayerInput loadPlayer(int id, String dir){
        String data = read("Data/map/"+dir+".user");
        String[] line = data.split("%");
        PlayerInput pi;
        pi = new PlayerInput(id == -1 ? new MainPlayer(0) : new Player("Player", 0));
        pi.host.load(data);
        pi.host.controlId = id;
        pi.load(line[2]);
        if (!Updater.editor) Global.display.input = pi;
        Command.host = pi.host;
        loadMap(Global.player.cmap);
        Global.mapEdgeCenter(Global.camera = Global.map.size.getMult(.5f,.5f));
        Global.map.onTimeChange();
        return pi;
    }
    public static void delete(String dir){
        File file = new File("Data/"+dir);
        if (file.exists()){
            if (!file.delete()) System.out.println("Instance Map File Deletion Failed! ("+dir+")");
        }
    }
    public static int loadMap(String map) {
        Global.houseId = 1;
        Global.player.lastLand = new Line();
        String[] mapProperties = map.split("@");
        String dir = mapProperties[0];
        int player = -1;
        if (Global.map != null) player = Integer.parseInt(mapProperties[1]);
        if (FileManager.isFile("Data/map/" + dir + ".map")){
            Global.map = new Map(dir, new P());
            Global.map.load(dir);
        } else {
            Generator.generate(mapProperties);
        }
        Global.player.cmap = dir;
        Global.camera.set(Global.player.pos);
        Global.map.setTexture();
        return player;
    }

    private static int[] readClipFrames(String[] data) {
        int[] result;
        String[] lines = data[1].split("%");
        result = new int[lines.length];
        for (int line = 0; line < lines.length; line++) {
            result[line] = Integer.parseInt(lines[line].split("/")[1]);
        }
        return result;
    }

    private static int[][] readModelProperty(String[] data) {
        int[][] result;
        String line = data[0].split("%")[1];
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

    private static String[][] readModel(String[] data) {
        String[][] result;
        String line = data[0].split("%")[0];
        String[] sections = line.split(";");
        result = new String[sections.length][];
        for (int section = 0; section < sections.length; section++) {
            String values[] = sections[section].split(",");
            result[section] = new String[values.length];
            System.arraycopy(values, 0, result[section], 0, values.length);
        }
        return result;
    }

    private static int[][][] readAnimation(String[] data) {
        int[][][] result;
        String[] lines = data[1].split("%");
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

    private static HashMap<String, int[]> readMoveset(String[] data) {
        HashMap<String, int[]> result;
        String[] lines = data[2].split("%");
        result = new HashMap<>();
        for (String line1 : lines) {
            String[] sections = line1.split("@");
            String values[] = sections[1].split(",");
            int[] v = new int[values.length];
            for (int i = 0; i < values.length; i++) {
                v[i] = Integer.parseInt(values[i]);
            }
            result.put(sections[0], v);
        }
        return result;
    }

    public static Clip readClip(String dir) {
        String[] data = read(dir).split("%%");
        return new Clip(readModelProperty(data), readModel(data), readAnimation(data), readClipFrames(data), readMoveset(data));
    }

    public static HashMap<String, ArrayList<String>> readDialogue(String dir){
        String data = read("Data/"+dir);
        String[] lines = data.split("%");
        HashMap<String,ArrayList<String>> result = new HashMap<>();
        for (String line1 : lines) {
            String[] type = line1.split("@");
            String[] results = type[1].split(";");
            ArrayList<String> values = new ArrayList<>();
            Collections.addAll(values, results);
            result.put(type[0], values);
        }
        return result;
    }

    public static String[] readText(String dir){
        String data = read("Data/"+dir);
        return data.split("%");
    }

    public static boolean isFile(String p){
        File file = new File(p);
        return file.isFile() && file.length() > 0;
    }

    public static void readWorld(){
        char[][] world;
        String data = read("Data/map/World.map");
        String[] lines = data.split("%");
        world = new char[lines.length-1][lines[1].length()];
        for (int y=1;y<lines.length;y++){
            for (int x=0;x<lines[y].toCharArray().length;x++){
                char node = lines[y].charAt(x);
                world[y-1][x] = node;
            }
        }
        String[] meta = lines[0].split(" ");
        Creation.seed = Integer.parseInt(meta[0]);
        Creation.world = world;
    }
}