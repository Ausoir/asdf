package com.ga.tool;

import java.util.Random;

public class T extends L<T> {
    public static String wrap(String p){
        return "["+p+"]";
    }
    public static String unwrap(String p){
        return p.substring(1, p.length()-1);
    }
    private String value;
    public T(String p){
        if (p != null) {
            boolean addOne = false;
            if (p.startsWith("[")) {
                p = p.substring(1, p.length() - 1);
                if (!p.isEmpty()) addOne = true;
            }
            value = p;
            StringBuilder current = new StringBuilder();
            int depth = 0;
            for (int i = 0; i < p.length(); i++) {
                char ch = p.charAt(i);
                if (ch == '[') depth++;
                else if (ch == ']') depth--;
                if (ch == ',' && depth == 0) {
                    add(new T(current.toString()));
                    current = new StringBuilder();
                    addOne = true;
                } else current.append(ch);
            }
            if (addOne) add(new T(current.toString()));
        } else value = "";
    }
    public T startsWith(String p){
        for (T i : this){
            if (i.get(0).value.equals(p)) return i;
        }
        return null;
    }
    public T getRand(Random rand){
        return get(rand.nextInt(size()));
    }
    public String get(){
        return value;
    }
    public void refresh(){
        if (size() > 0) {
            for (T i : this) i.refresh();
            StringBuilder v = new StringBuilder();
            for (int i = 0; i < size(); i++) {
                v.append(get(i).toString()).append((i == size() - 1) ? "" : ",");
            }
            value = v.toString();
        }
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
        return size() > 0 ? wrap(value) : value;
    }
}