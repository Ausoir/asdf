package com.ga.unit.skill;

/**
 * Created by Ga on 8/1/2016.
 */
public class Adrenaline extends Skill {
    public Adrenaline(){
        stat = "legs";
        cost = 2;
        timer.set(50);
    }
    protected void start(){
        super.start();
        host.get("speed").add(10,true);
    }
    protected void end(){
        host.get("speed").add(-10,true);
        super.end();
    }
}
