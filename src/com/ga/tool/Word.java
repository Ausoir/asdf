package com.ga.tool;

/**
 * Created by Ga on 8/29/2016.
 */
public enum Word {
    Affirmitive(new String[]{"ok","yes","sure"}), Negative(new String[]{"no"}), Interest(new String[]{"what","why"}),
    Identity(new String[]{"who"});
    String[] qualifiers;
    Word(String[] p){
        qualifiers = p;
    }
    public static String wordify(String p){
        String r = p.substring(0,1).toUpperCase()+p.substring(1);
        int space = r.indexOf("_");
        if (space != -1){
            r = r.substring(0,space)+"_"+r.substring(space+1,space+2).toUpperCase()+r.substring(space+2);
        }
        return r;
    }
    public boolean is(String p){
        for (String t : qualifiers){
            if (t.equals(p)) return true;
        }
        return false;
    }
}
