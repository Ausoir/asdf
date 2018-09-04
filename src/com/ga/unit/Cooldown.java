package com.ga.unit;

import com.ga.map.Global;

public class Cooldown {
    public int time;
    private int duration;
    public Cooldown(int duration){
        time = Global.time;
        setDuration(duration);
    }
    public String toString(){
        return ""+Math.max(0, time-Global.time);
    }
    public boolean isReady(){
        return Global.time >= time;
    }
    public void setDuration(int p){
        duration = p;
    }
    public void setDone(){
        time = Global.time;
    }
    public void reset(){
        time = Global.time + duration;
    }
    Cooldown add(int p){
        time = Global.time + p;
        return this;
    }
    public int getCurrentState(){
        return Math.max(0, time - Global.time);
    }
}
