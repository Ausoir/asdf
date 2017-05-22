package com.ga.tool;

/**
 * Created by Ga on 7/26/2016. wqe
 */
public class Timer {
    public int max;
    public int time;
    private boolean repeat = false;
    public Timer(String p){
        load(p);
    }
    public Timer(int time, boolean repeat){
        set(time, repeat);
    }
    public Timer(int time){
        set(time, true);
    }
    public void set(int time){
        set(time, false);
    }
    public void set(int time, boolean repeat){
        this.max = time;
        this.time = time;
        this.repeat = repeat;
    }
    public void reset(){
        time = max;
    }
    public float getPercentage(){
        return (10-(int)((time/(float)max)*10))/10f;
    }
    public boolean over(){
        return time == 0;
    }
    public String toString(){
        return time+","+max+","+(repeat?"*":"1");
    }
    public void load(String p){
        String[] t = p.split(",");
        time = Integer.parseInt(t[0]);
        max = Integer.parseInt(t[1]);
        repeat = t[2].equals("*");
    }
    public boolean update(int p){
        if (over()){
            if (repeat) time = max;
            return true;
        }
        else if (time > 0) time = Math.max(0,time-p);
        return false;
    }
    public boolean update(){
        return update(1);
    }
}
