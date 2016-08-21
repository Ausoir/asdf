package com.ga.unit.skill;

import com.ga.tool.Performer;

/**
 * Created by Ga on 8/1/2016.
 */
public class Stat {
    public int max;
    public int current;
    public Performer onChange;
    public Stat(int p){
        set(p);
    }
    public void set(int p){
        max = p;
        current = p;
        onChange();
    }
    public void addTotal(int p){
        set(max + p);
    }
    public void add(int p){
        add(p, false);
    }
    public void add(int p, boolean force){
        current += p;
        if (!force && current > max) current = max;
        else if (current < 0) current = 0;
        else if (p != 0) onChange();
    }
    public float getByPercentage(float p){
        return max*p;
    }
    public float getPercentage(){
        return (float)current/max;
    }
    public boolean require(int p){
        if (current >= p){
            add(-p);
            return true;
        }
        return false;
    }
    public void heal(){
        current = max;
    }
    public int get(){
        return current;
    }
    public void onChange(){
        if (onChange != null) onChange.perform();
    }
}
