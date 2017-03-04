package com.ga.game;

import java.util.HashMap;

/**
 * Created by Ga on 3/1/2017.
 */
public class Keybinding {
    private static HashMap<String, Integer> map = new HashMap<>();
    public static void init(){
        String[] t = FileManager.readText("Keybinding");
        for (String i : t){
            String[] mapping = i.split(":");
            map.put(mapping[0], Integer.parseInt(mapping[1]));
        }
    }
    public static Integer get(String p){
        return map.get(p);
    }
}
