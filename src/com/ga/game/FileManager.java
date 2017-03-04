package com.ga.game;

import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.map.MapGenerator;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.*;
import com.ga.unit.factory.Stock;
import com.ga.unit.intelligence.PlayerInput;
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
    static void loadFirst(){
        loadMap(Global.player.host.cmap,0);
        Global.mapEdgeCenter(Global.cameraTo = Global.player.host.pos.getAdd(0,0));
    }
    public static PlayerInput loadPlayer(boolean main, String dir){
        String data = read("map/"+dir);
        String[] line = data.split("%");
        PlayerInput pi;
        String playerString = line[0];
        pi = new PlayerInput(main ? new MainPlayer(playerString) : new Player(playerString));
        pi.host.load(data);
        pi.host.id = -Integer.parseInt(dir)-1;
        pi.load(line[2]);
        return pi;
    }
    public static P loadMap(String dir, int player) {
        P r;
        String[] oldmap = null;
        if (Global.map != null) oldmap = Global.map.maps;
        if (oldmap != null && dir.startsWith("Instance")){
            String instanceData[] = dir.split(":")[1].split("/");
            String[] param = instanceData[0].split("&");
            String[] returnAddr;
            int returnPos = player;
            int toPos = player;
            if (instanceData.length > 1){
                returnAddr = instanceData[1].split("&");
                returnPos = Integer.parseInt(returnAddr[2]);
                toPos = Integer.parseInt(param[3]);
            } else {
                String[] t = oldmap[5].split(":")[1].split("@")[0].split("&");
                returnAddr = new String[]{t[1],t[2]};
            }
            int maxDist = Math.max(1,Integer.parseInt(param[1]));
            int returndist = Math.min(Math.max(0,(Global.player.host.get("Hunger").get()+Global.player.host.get("Health").get())/2), Global.r.nextInt(maxDist)+1);
            int distance = maxDist - returndist;
            if (distance == 0)
                r = readMap(param[2], toPos);
            else if (returndist == 0)
                r = readMap(returnAddr[1], returnPos);
            else {
                r = MapGenerator.generate(param[0], distance, param[2], toPos,
                        returndist, returnAddr[1], returnPos);
            }
            for (int i = 0; i < returndist; i++){
                Global.map.event.onChange(oldmap, i == returndist-1);
            }
        } else {
            r = readMap(dir, player);
            if (player != -2) Global.map.event.onChange(oldmap, true);
        }
        Global.map.setTexture();
        if (r == null) {
            System.out.println("Invalid Player Spawn Location");
            r = new P(Settings.centerX, Settings.centerY);
        }
        return r;
    }
    private static P readMap(String dir, int player){
        P r = null;
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
                            String[] property = sections[section].split("@");
                            Matter object = Stock.get(property);
                            if (object instanceof Matter && object.name.equals("PlayerSpawn")) {
                                if (player == 0) r = new P(property[2]);
                                player--;
                            }
                        }
                    }
                }
            }
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
