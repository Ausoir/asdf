package com.ga.tool;

import java.util.ArrayList;

public class ToolString {
    public static String tosArrayList(ArrayList p){
        if (p.size() == 0) return "";
        String r = "";
        for (Object i : p){
            r += i.toString()+",";
        }
        return r.substring(0,r.length()-1);
    }
}
