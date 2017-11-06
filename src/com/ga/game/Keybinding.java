package com.ga.game;

import java.util.HashMap;

/**
 * Created by Ga on 3/1/2017. asd
 */
public class Keybinding {
    private static HashMap<String, Integer> map = new HashMap<>();
    static void init(){
        String[] t = FileManager.readText("Keybinding.txt");
        for (String i : t){
            String[] mapping = i.split(":");
            map.put(mapping[0], Integer.parseInt(mapping[1]));
        }
    }
    public static Integer get(String p){
        Integer r = map.get(p);
        if (r == null) return -2;
        return r;
    }
}
