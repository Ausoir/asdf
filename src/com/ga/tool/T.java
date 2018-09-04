package com.ga.tool;

import java.util.Random;

public class T extends L<T> {
    public static String wrap(String p){
        return "["+p+"]";
    }
    private String value;
    public T(String p){
        if (p != null) {
            value = p;
            String current = "";
            int depth = 0;
            boolean addOne = false;
            for (int i = 0; i < p.length(); i++) {
                boolean write = true;
                char ch = p.charAt(i);
                if (ch == '[') {
                    if (depth == 0) write = false;
                    depth++;
                } else if (ch == ']') {
                    depth--;
                    if (depth == 0) {
                        write = false;
                        addOne = true;
                    }
                }
                if (ch == ',' && depth == 0) {
                    add(new T(current));
                    current = "";
                    addOne = true;
                    write = false;
                }
                if (write) current += ch;
            }
            if (addOne) add(new T(current));
        } else value = "";
    }
    public T startsWith(String p){
        for (T i : this){
            if (i.get(0).value.equals(p)) return i;
        }
        return null;
    }
    public String[] toStringArray(){
        return value.split(",");
    }
    public T getRand(Random rand){
        return get(rand.nextInt(size()));
    }
    public String get(){
        return value;
    }
    public T get(int index){
        if (size() <= index) return new T(""); //Performance Optimized
        T r = super.get(index);
        if (r == null) return new T("");
        return r;
    }
    public T get(int index, T def){
        if (size() <= index) return def;
        T r = super.get(index);
        if (r == null) return def;
        return r;
    }
    public int getInt(){
        return Integer.parseInt(value);
    }
    public float getFloat(){
        return Float.parseFloat(value);
    }
    public boolean equals(String p){
        return value.equals(p);
    }
    public String toString(){
        return value;
    }
}