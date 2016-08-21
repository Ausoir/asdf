package com.ga.unit.skill;

/**
 * Created by Ga on 8/1/2016.
 */
public class Empower extends Skill {
    private int mag = 0;
    public Empower(){
        stat = "arms";
        cost = 2;
        timer.set(50);
    }
    protected void start(){
        super.start();
        mag = host.get("damage").get();
        host.get("damage").add(mag,true);
    }
    protected void end(){
        host.get("damage").add(-mag,true);
        super.end();
    }
}
