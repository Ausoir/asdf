package com.ga.tool;

import java.util.ArrayList;

public class L<T> extends ArrayList<T> {
    public L(){
        super();
    }
    public L(L p){
        super(p);
    }
    public T getFirst(){
        if (!isEmpty()) return super.get(0);
        return null;
    }
    public void addAll(T[] p){
        for (T i : p){
            add(i);
        }
    }
    public String toString(){
        if (size() == 0) return "";
        StringBuilder r = new StringBuilder();
        for (Object i : this){
            r.append(i.toString()).append(",");
        }
        return "["+r.substring(0,r.length()-1)+"]";
    }
}
