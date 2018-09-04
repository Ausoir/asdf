package com.ga.tool;

import java.util.HashMap;
import java.util.Map;

public class H<K,V> extends HashMap<K,V> {
    public H(){
        super();
    }
    public H(H p){
        super(p);
    }
    public String toString(){
        if (size() == 0) return "";
        StringBuilder r = new StringBuilder();
        for (Map.Entry pair : entrySet()){
            r.append("[").append(pair.getKey()).append(",").append(pair.getValue()).append("]").append(",");
        }
        return "["+r.substring(0,r.length()-1)+"]";
    }
}
