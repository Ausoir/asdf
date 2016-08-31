package com.ga.tool;

/**
 * Created by Ga on 8/29/2016.
 */
public enum Word {
    Affirmitive(new String[]{"ok","yes","sure"}), Negative(new String[]{"no"}), Interest(new String[]{"what","why","wares","want"}),
    Identity(new String[]{"who"}), Buy(new String[]{"buy","take","give me","i want"}), Greeting(new String[]{"hi","hello","good day","fine day","greet"});
    String[] qualifiers;
    Word(String[] p){
        qualifiers = p;
    }
    public static String wordify(String p){
        String r = p.substring(0,1).toUpperCase()+p.substring(1).toLowerCase();
        int space = r.indexOf("_");
        if (space != -1){
            String[] word = p.split("_");
            r = wordify(word[0]) +"_"+ wordify(word[1]);
        }
        return r;
    }
    public boolean is(String p){
        for (String t : qualifiers){
            if (t.equals(p)) return true;
        }
        return false;
    }
    public static String getKeyName(int p){
        if (p == 1) return "LMB";
        else if (p == 3) return "RMB";
        else if (p == 32) return "SPACE";
        else if (p == 16) return "SHIFT";
        return (char) (int) p + "";
    }
}
