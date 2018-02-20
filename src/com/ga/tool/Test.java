package com.ga.tool;

import com.ga.game.FileManager;

/**
 * Created by Ga on 12/13/2016. asdasdasdawdadqw
 */
public class Test {
    public static void main(String[] args){
        String target = "In_Shop";
        String[] lines = FileManager.read("map/building/"+target+".map").split("%");
        String[] lands = lines[2].split(";");
        StringBuilder r = new StringBuilder();
        r.append(lines[0]).append("\n");
        r.append(lines[1]).append("\n");
        for (String i : lands){
            r.append(i.substring(0, i.length() - 1));
            if (i.endsWith("0")) r.append("1;");
            else r.append("0;");
        }
        r.deleteCharAt(r.length()-1);
        r.append("\n").append(lines[3]);
        FileManager.save("map/building/"+target+".map", r.toString());
    }
}
