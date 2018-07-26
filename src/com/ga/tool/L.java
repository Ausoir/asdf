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
    public String toString(){
        if (size() == 0) return "";
        String r = "";
        for (Object i : this){
            r += i.toString()+",";
        }
        return r.substring(0,r.length()-1);
    }
}
