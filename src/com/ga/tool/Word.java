package com.ga.tool;

/**
 * Created by Ga on 8/29/2016.
 */
public class Word {
    public static String wordify(String p){
        String r = p.substring(0,1).toUpperCase()+p.substring(1).toLowerCase();
        int space = r.indexOf("_");
        if (space != -1){
            String[] word = p.split("_");
            r = wordify(word[0]) +"_"+ wordify(word[1]);
        }
        return r;
    }
    public static String getKeyName(int p){
        if (p == 1) return "LMB";
        else if (p == 3) return "RMB";
        else if (p == 32) return "SPACE";
        else if (p == 16) return "SHIFT";
        return (char) (int) p + "";
    }
}
