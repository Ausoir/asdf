package com.ga.unit.skill;

/**
 * Created by Ga on 9/1/2016.
 */
public enum Buff {
    Adrenaline(50,"speed",1,"speed"), Rejuvenate(100,"arms",1,"arms"), Empower(80,"damage",10), Cleave(100), Evade(100);
    public int time;
    public float buff;
    public String stat;
    public String buffRel;
    Buff(int time){
        this.time = time;
    }
    Buff(int time, String stat, float buff){
        this(time);
        this.stat = stat;
        this.buff = buff;
    }
    Buff(int time, String stat, int buff, String buffRel){
        this(time, stat, buff);
        this.buffRel = buffRel;
    }
}
