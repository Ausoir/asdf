package com.ga.unit.skill;

/**
 * Created by Ga on 8/1/2016.
 */
public class Rejuvenate extends Skill {
    public Rejuvenate(){
        stat = "legs";
        cost = 3;
        timer.set(150);
    }
    protected void start(){
        super.start();
        host.get("arms").add(100,false);
    }
    protected void end(){
        super.end();
    }
}
