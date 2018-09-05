package com.ga.unit.skill;

import com.ga.tool.T;

/**
 * Created by Ga on 8/1/2016.
 */
public class Stat {
    public String name;
    private int max;
    private int current;
    public Stat(String name, T p){
        this.name = name;
        current = p.get(0).getInt();
        max = p.get(1).getInt();
    }
    public Stat(int p){
        setTotal(p, false);
    }
    public void setTotal(int p, boolean force){
        if (!force) current += p-max;
        max = p;
    }
    public void set(int p, boolean force){
        current = p;
        if (!force) {
            if (current > max) current = max;
            else if (current < 0) current = 0;
        }
    }
    public void addTotal(int p){
        setTotal(max + p, false);
    }
    public int add(int p, boolean force){
        int prev = current;
        set(current+p, force);
        if (!force) return current-prev;
        else return p;
    }
    public float getPercentage(){
        return (float)Math.max(0,current)/max;
    }
    public boolean require(int p){
        if (p <= 0) {
            return true;
        } else if (current >= p) {
            add(-p, false);
            return true;
        }
        return false;
    }
    public boolean isFull(){
        return current >= max;
    }
    public void heal(){
        current = max;
    }
    public int get(){
        return current;
    }
    public int getMax() {
        return max;
    }
    public String toString(){
        return "["+current+","+max+"]";
    }
}
