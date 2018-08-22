package com.ga.game;

import com.ga.map.Creation;
import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.map.Updater;
import com.ga.tool.Line;
import com.ga.unit.Clip;
import com.ga.unit.Stock;
import com.ga.unit.Unit;
import com.ga.unit.man.MainPlayer;
import com.ga.unit.skill.Actions;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by Ga on 7/26/2016. asd
 */
public class FileManager {
    public static final HashMap<String, ArrayList<String[]>> fileCache = new HashMap<>();

    public static void init(){
        searchFiles("Data", fileCache);
    }
    public static String makeDirectory(String p){
        return p.replace("/", File.separator).replace("\\", File.separator);
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
                    r.computeIfAbsent(ext, k -> new ArrayList<>());
                    r.get(ext).add(new String[]{name, path});
                }
            }
        }
    }
    public static void save(String dir, String input) {
        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(dir))) {
                writer.write(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void saveCharacter(){
        if (Global.player.state != Actions.Dead) {
            if (Global.server.isHost()) saveMap(Global.player.map.name.value, Global.player.map);
            save(FileManager.makeDirectory("Data/map/Player.user"), Global.player.toString());
            save(FileManager.makeDirectory("Data/Keymap.txt"), Global.player.input.toString());
        }
    }
    public static void saveMap(String name, Map map){
        map.updater.processQueue();
        String result = map.toString();
        save(FileManager.makeDirectory("Data/map/"+name+".map"), result);
    }
    public static String read(String dir) {
        StringBuilder result = new StringBuilder();
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(dir))) {
                String newline = br.readLine();
                if (newline != null) {
                    String line = newline.split(" //")[0];
                    result = new StringBuilder(line + "%");
                    while (line != null) {
                        line = br.readLine();
                        if (line != null) result.append(line.split(" //")[0]).append("%");
                    }
                    result = new StringBuilder(result.substring(0, result.length() - 1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }
    public static void loadPlayer(){
        String data = read("Data/map/Player.user");
        String keymap = read("Data/Keymap.txt");
        MainPlayer player = ((MainPlayer) Stock.Player.create(null, 1, 0));
        Global.display.input = player.input;
        if (!data.contains("@")){
            Global.p("Player gone!");
            System.exit(1);
        }
        player.load(data);
        player.input.load(keymap);
        Command.host = player;
        player.lastLand = new Line();
        Global.camera.set(player.pos);
        player.map.setTexture();
        player.map.render();
        Global.camera = player.map.mapEdgeCenter(player.map.size.getMult(.5f, .5f));
        player.start();
        Menu.init();
    }

    public static void loadActors(){
        String data = read("Data/map/Actors.map");
        if (!data.isEmpty()) {
            String[] actors = data.split("%");
            for (String i : actors) {
                ((Unit) Stock.get(null, i).start()).traveler = true;
            }
        }
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
        String data = read(dir);
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
        String data = read(dir);
        return data.split("%");
    }

    public static boolean isFile(String p){
        File file = new File(p);
        return file.isFile() && file.length() > 0;
    }

    public static void loadWorld(){
        String data = read("Data/map/World.map");
        String[] meta = data.split(",");
        Creation.seed = Integer.parseInt(meta[0]);
        Global.hour = Integer.parseInt(meta[1]);
        Global.controlId = Integer.parseInt(meta[2]);
    }

    public static void saveWorld(){
        save(FileManager.makeDirectory("Data/map/World.map"), Creation.seed + "," + Global.hour + "," + Global.controlId);
        save(FileManager.makeDirectory("Data/map/Actors.map"), Updater.toActorString());
    }
}