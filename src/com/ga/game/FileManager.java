package com.ga.game;

import com.ga.map.BeastFactory;
import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.map.ObjectFactory;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.*;
import com.ga.unit.intelligence.PlayerInput;
import com.ga.unit.item.Item;
import com.ga.unit.item.ItemFactory;
import com.ga.unit.skill.Actions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ga on 7/26/2016.
 */
public class FileManager {
    public static void saveCharacter(){
        if (Global.player.host.state != Actions.Dead) {
            saveMap("map/"+Global.player.host.cmap);
            save("map/0", Global.player.host.toCharacterString());
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
        result += Updater.toMapString();
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
    static void loadFirst(boolean editor){
        readMap(Global.player.host.cmap,0);
        Global.mapEdgeCenter(Global.cameraTo = Global.player.host.pos.getAdd(0,0));
        if (editor){
            new EditorInput();
        }
    }
    public static PlayerInput loadPlayer(boolean main, String dir){
        String data = read("map/"+dir);
        String[] line = data.split("%");
        PlayerInput pi;
        String playerString = line[0].split("]")[1];
        pi = new PlayerInput(main ? new MainPlayer(playerString) : new Player(playerString));
        pi.host.load(data);
        pi.host.id = -Integer.parseInt(dir)-1;
        pi.load(line[2]);
        Global.setTime(Integer.parseInt(line[1].split(";")[3]));
        Global.gameHour = Integer.parseInt(line[1].split(";")[4]);
        return pi;
    }
    public static P readMap(String dir, int player) {
        P r = null;
        String data = read("map/"+dir+".map");
        String[] line = data.split("%");
        P mapPos = new P();
        String[] names = line[0].split(",");
        Global.map = new Map(names, mapPos);

        if (line.length > 1) {
            String[] sections = line[1].split(";");
            for (int section = 0; section < sections.length; section++) {
                String values[] = sections[section].split(":");
                Global.map.formTextures.put(Integer.parseInt(values[0]), values[1]);
            }
            if (line.length > 2) {
                sections = line[2].split(";");
                for (int section = 0; section < sections.length; section++) {
                    String values[] = sections[section].split(",");
                    int[] v = new int[values.length];
                    for (int i = 0; i < values.length; i++) {
                        v[i] = Integer.parseInt(values[i]);
                    }
                    Global.map.land.add(new Line(v[0], v[1], v[2], v[3], v[4], v[5]));
                }
                if (line.length > 3) {
                    sections = line[3].split(";");
                    if (sections.length > 0) {
                        for (int section = 0; section < sections.length; section++) {
                            String[] unit = sections[section].split("]");
                            if (unit.length == 1) { //object
                                String[] type = unit[0].split("@");
                                if (type[0].equals("PlayerSpawn")) {
                                    if (player == 0) r = new P(type[2]);
                                    player--;
                                }
                                ObjectFactory.produce(type[0], new P(type[2]), Integer.parseInt(type[1]));
                            } else if (unit.length == 2){ //unit
                                int id = Integer.parseInt(unit[1].split("@")[3]);
                                if (id >= Updater.id) Updater.id = id + 1;
                                if (unit[0].equals("Man")) new Man(unit[1]);
                                else BeastFactory.produce(unit[1]);
                            } else if (!Global.map.event.regrowth){ //item
                                String[] t = unit[2].split("/");
                                Item item = ItemFactory.load(t[0]);
                                item.pos = new P(t[1]);
                                item.rotation = Float.parseFloat(t[2]);
                                item.field();
                            }
                        }
                    }
                }
            }
        }
        Global.map.setTexture();
        Global.map.setShader();
        if (Global.map.event.regrowth){
            Global.map.event.regrowth = false;
            Global.map.generateProps();
        }
        if (r == null){
            System.out.println("Invalid Player Spawn Location");
            r = new P(Settings.centerX, Settings.centerY);
        }
        return r;
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
