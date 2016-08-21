package com.ga.tool;

/**
 * Created by Ga on 7/26/2016.
 */
public class Timer {
    public int max;
    public int time;
    public boolean repeat = false;
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
    public boolean update(){
        if (over()){
            if (repeat) time = max;
            return true;
        }
        else if (time > 0) time--;
        return false;
    }
}
