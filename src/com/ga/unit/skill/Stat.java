package com.ga.unit.skill;

/**
 * Created by Ga on 8/1/2016.
 */
public class Stat {
    private int max;
    protected int current;
    public Stat(String p){
        String[] t = p.split(",");
        if (t.length == 1){
            setTotal(Integer.parseInt(p));
        } else {
            current = Integer.parseInt(t[0]);
            max = Integer.parseInt(t[1]);
        }
    }
    public Stat(int p){
        setTotal(p);
    }
    public Stat(int p, int max){
        setTotal(max);
        current = p;
    }
    public void setTotal(int p){
        max = p;
        current = p;
    }
    public void set(int p, boolean force){
        current = p;
        if (!force) {
            if (current > max) current = max;
            else if (current < 0) current = 0;
        }
    }
    public void setMax(int p){
        int diff = p - max;
        max = p;
        current += diff;
    }
    public void addTotal(int p){
        setTotal(max + p);
    }
    public int add(int p, boolean force){
        int prev = current;
        set(current+p, force);
        if (!force) return current-prev;
        else return p;
    }
    public float getPercentage(){
        return (float)current/max;
    }
    public boolean require(int p){
        if (current >= p){
            if (p > 0) add(-p, false);
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
    public String toString(){
        return current+","+max;
    }
    public int getMax() {
        return max;
    }
}
