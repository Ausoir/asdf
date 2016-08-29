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
    public void set(int p){
        current = p;
        if (current > max) current = max;
        else if (current < 0) current = 0;
        else if (p != 0) onChange();
    }
    public void addTotal(int p){
        setTotal(max + p);
    }
    public void add(int p){
        add(p, false);
    }
    public void add(int p, boolean force){
        if (!force) set(current+p);
        else current += p;
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
        if (onChange != null) onChange.perform();
    }
    public String toString(){
        return current+","+max;
    }
}
