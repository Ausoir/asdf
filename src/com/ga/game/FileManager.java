package com.ga.game;

import com.ga.map.*;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Clip;
import com.ga.unit.PlayerInput;
import com.ga.unit.Stock;
import com.ga.unit.player.MainPlayer;
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

    public static void init(){
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
    public static void save(String dir, String input) {
        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("Data/" + dir))) {
                writer.write(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void saveCharacter(){
        if (Global.player.state != Actions.Dead) {
            saveMap("map/"+Global.player.cmap);
            save("map/Player.user", Global.player.toCharacterString());
            saveWorld();
        }
    }
    public static void saveMap(String name){
        String result = "" + Global.map.toString();
        result += "\n";
        result += Updater.toPropString();
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
    public static void loadGame(){
        loadActors();
        loadPlayer(1);
        loadMap(Global.player.cmap);
    }
    public static PlayerInput loadPlayer(int id){
        String[] data = read("Data/map/Player.user").split("%");
        PlayerInput pi;
        if (id == 1) {
            pi = new PlayerInput((MainPlayer) Updater.getUnloadedActor(1));
            Global.display.input = pi;
            pi.load(data[5]);
            Command.host = pi.host;
        } else {
            pi = new PlayerInput((MainPlayer) Updater.getUnloadedActor(id));
        }
        pi.host.loadPlayer(data);
        return pi;
    }
    public static int loadMap(String map) {
        Global.houseId = 1;
        Global.player.lastLand = new Line();
        String[] mapProperties = map.split("@");
        String dir = mapProperties[0];
        int player = -1;
        if (Global.map != null) player = Integer.parseInt(mapProperties[1]);
        Global.map = new Map(dir, new P());
        Global.map.start();
        Global.map.load(dir);
        Global.player.cmap = dir;
        Global.camera.set(Global.player.pos);
        Global.map.setTexture();
        Global.mapEdgeCenter(Global.camera = Global.map.size.getMult(.5f,.5f));
        Global.map.onTimeChange();
        return player;
    }

    public static void loadActors(){
        String data = read("Data/map/Actors.map");
        if (data.length() > 0) {
            String[] actors = data.split("%");
            for (String i : actors) {
                Stock.get(i).start();
            }
        }
    }

    public static boolean moveToward(int from, int to){
        String[] objects = read("Data/map/"+from+".map").split("%")[3].split(";");
        for (String i : objects) {
            String[] sections = i.split("@");
            if (Integer.parseInt(sections[0]) == Stock.Travel.ordinal()) {
                if (moveTowardRecurse())
            }
        }
    }

    private static boolean moveTowardRecurse(int from, int to){
        if (from == to) return true;
        String[] objects = read("Data/map/"+from+".map").split("%")[3].split(";");
        for (String i : objects){
            String[] sections = i.split("@");
            if (Integer.parseInt(sections[0]) == Stock.Travel.ordinal()){
                if (moveToward(Integer.parseInt(sections[1]), to)) return true;
            }
        }
        return false;
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
        String data = read("Data/map/World.txt");
        String[] lines = data.split("%");
        world = new char[lines.length-1][lines[1].length()];
        for (int y=1;y<lines.length;y++){
            for (int x=0;x<lines[y].toCharArray().length;x++){
                char node = lines[y].charAt(x);
                world[y-1][x] = node;
            }
        }
        String[] meta = lines[0].split(",");
        Creation.seed = Integer.parseInt(meta[0]);
        Global.time = Integer.parseInt(meta[1]);
        Global.controlId = Integer.parseInt(meta[2]);
        Creation.world = world;
    }

    public static void saveWorld(){
        StringBuilder r = new StringBuilder(Creation.seed+","+Global.time+","+Global.controlId+"\n");
        for (char[] y : Creation.world){
            for (char x : y){
                r.append(x);
            }
            r.append("\n");
        }
        save("map/World.txt", r.toString());
        save("map/Actors.map", Updater.toActorString());
    }
}