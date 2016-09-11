package com.ga.game;

import com.ga.map.Global;
import com.ga.map.ManFactory;
import com.ga.map.Map;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Beast;
import com.ga.unit.Clip;
import com.ga.unit.Player;
import com.ga.unit.Updater;
import com.ga.unit.intelligence.PlayerInput;

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
    public static void saveMap(String name){
        String result = "" + Global.map.toString();
        result += "\n";
        result += Updater.toMapString();
        save(name+".map", result);
    }
    public static void saveCharacter(){
        saveMap("map/Map");
        save("map/"+"Player"+".char", Global.player.host.toCharacterString());
    }
    public static void save(String dir, String input) {
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
        String data = read("map/"+"Player"+".char");
        String[] line = data.split("%");
        String[] values = line[0].split("@");
        if (!editor){
            readMap("Map",0);
            Global.player = new PlayerInput(new Player(new P(values[2]), Integer.parseInt(values[1])));
            Global.player.host.load(read("map/Player.char"));
            Global.setTime(Integer.parseInt(values[4]));
        } else {
            readMap("Map",-2);
            new EditorInput();
        }
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
                String values[] = sections[section].split(",");
                int[] v = new int[values.length];
                for (int i = 0; i < values.length; i++) {
                    v[i] = Integer.parseInt(values[i]);
                }
                Global.map.land.add(new Line(v[0], v[1], v[2], v[3], v[4]));
            }

            if (line.length > 2) {
                sections = line[2].split(";");
                if (sections.length > 0)
                    for (int section = 0; section < sections.length; section++) {
                        String[] type = sections[section].split("@");
                        String[] unit = type[0].split(":");
                        P pos;
                        int seed = Integer.parseInt(type[1]);
                        String[] values = type[2].split(",");
                        int[] v = new int[values.length];
                        for (int i = 0; i < values.length; i++) {
                            v[i] = Integer.parseInt(values[i]);
                        }
                        pos = new P(v[0], v[1]);
                        if (unit[1].equals("Player")) {
                            if (player == 0){
                                r = pos;
                            } else if (player == -2){
                                new Player(pos, -1);
                            }
                            if (player > -1) player--;
                        }
                        else if (unit[0].equals("M:")) ManFactory.produce(unit[1], pos, seed);
                        else new Beast(unit[0], unit[1], pos, seed);
                    }
            }
        }
        return r;
    }

    public static int[] readClipFrames(String dir) {
        String data = read(dir);
        int[] result;
        String[] lines = data.split("%%")[1].split("%");
        result = new int[lines.length];
        for (int line = 0; line < lines.length; line++) {
            result[line] = Integer.parseInt(lines[line].split("/")[1]);
        }
        return result;
    }

    public static int[][] readModelProperty(String dir) {
        String data = read(dir);
        int[][] result;
        String line = data.split("%%")[0].split("%")[1];
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

    public static int[][] readModel(String dir) {
        String data = read(dir);
        int[][] result;
        String line = data.split("%")[0];
        String[] sections = line.split(";");
        result = new int[sections.length][];
        for (int section = 0; section < sections.length; section++) {
            String values[] = sections[section].split(",");
            result[section] = new int[values.length];
            int[] v = new int[values.length];
            for (int i = 0; i < values.length; i++) {
                v[i] = Integer.parseInt(values[i]);
                result[section][i] = v[i];
            }
        }
        return result;
    }

    public static int[][][] readAnimation(String dir) {
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

    public static HashMap<String, int[]> readMoveset(String dir) {
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
}
