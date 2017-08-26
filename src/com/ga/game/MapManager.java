package com.ga.game;

import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.unit.Stock;

import java.util.ArrayList;

public class MapManager {
    private static String getRandomPos(String map){
        String[] objects = FileManager.read("Data/map/"+map+".map").split("%")[2].split(";");
        Line land = new Line();
        do {
            int rand = Global.r.nextInt(objects.length);
            land.load(objects[rand]);
        } while (land.opt != 0 || !land.clockwise());
        int x = (int)((land.ptd.x+land.pt.x)/2);
        return x+","+(land.getCrossPointY(x)-1);
    }

    public static String[] moveRandom(String from){
        String[] objects = FileManager.read("Data/map/"+from+".map").split("%")[3].split(";");
        ArrayList<String> routes = new ArrayList<>();
        for (String i : objects){
            String[] sections = i.split("@");
            if (Integer.parseInt(sections[0]) == Stock.Travel.ordinal()){
                routes.add(sections[1]);
            }
        }
        String[] r = new String[2];
        r[0] = routes.get(Global.r.nextInt(routes.size()));
        r[1] = getRandomPos(from);
        return r;
    }

    public static String[] moveToward(String from, String to){
        String[] r = new String[2];
        ArrayList<String> routes = new ArrayList<>();
        routes.add(from);
        moveToward(routes, from, to);
        r[0] = routes.get(1);
        r[1] = getRandomPos(from);
        return r;
    }

    private static boolean moveToward(ArrayList<String> routes, String from, String to){
        if (from.equals(to)) return true;
        String[] objects = FileManager.read("Data/map/"+from+".map").split("%")[3].split(";");
        for (String i : objects){
            String[] sections = i.split("@");
            if (Integer.parseInt(sections[0]) == Stock.Travel.ordinal() && !routes.contains(sections[1])){
                routes.add(sections[1]);
                if (!moveToward(routes, sections[1], to)) routes.remove(sections[1]);
                else return true;
            }
        }
        return false;
    }
}
