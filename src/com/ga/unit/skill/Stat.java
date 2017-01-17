package com.ga.unit.skill;

/**
 * Created by Ga on 8/1/2016.
 */
public class Stat {
    private int max;
    protected int current;
    public int reserve = 0;
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
        onChange();
    }
    public void set(int p, boolean force){
        int prev = current;
        current = p;
        if (!force) {
            if (current > max - reserve) current = max - reserve;
            else if (current < 0) current = 0;
        }
        if (prev != current) onChange();
    }
    public void setMax(int p){
        max = p;
        if (max < 0) max = 0;
    }
    public void addTotal(int p){
        setTotal(max + p);
    }
    public void add(int p){
        add(p, false);
    }
    public void add(int p, boolean force){
        set(current+p, force);
    }
    public float getByPercentage(float p){
        return max*p;
    }
    public float getPercentage(){
        return (float)current/max;
    }
    public boolean require(int p){
        if (current >= p){
            if (p > 0) add(-p);
            return true;
        }
        return false;
    }
    public boolean isFull(){
        return max == current;
    }
    public void heal(){
        current = max;
    }
    public int get(){
        return current;
    }
    public void onChange(){
    }
    public String toString(){
        return current+","+max;
    }
    public int getMax() {
        return max;
    }

    public void addMax(int i) {
        setMax(max + i);
    }
}
