package com.ga.game;

import java.util.HashMap;

/**
 * Created by Ga on 1/27/2017.
 */
public class Dialog {
    private static HashMap<String,String> cache = new HashMap<>();
    protected static void init(){
        String[] data = FileManager.readText("dialog/Dialog");
        for (String i : data){
            String[] t = i.split(":");
            cache.put(t[0],t[1]);
        }
    }
    public static String get(String p){
        String r = cache.get(p);
        if (r == null) return p;
        return r;
    }
    public static String get(String p, String[] args){
        String data = cache.get(p);
        if (data == null) return p;
        String[] t = data.split("\\$");
        String r = "";
        for (int i=0;i<t.length;i++){
            r += t[i];
            if (i < args.length) r += args[i];
        }
        return r;
    }
}
