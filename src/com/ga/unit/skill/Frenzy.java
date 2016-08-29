package com.ga.unit.skill;

/**
 * Created by Ga on 8/1/2016.
 */
public class Frenzy extends Skill {
    public Frenzy(){
        stat = "legs";
        cost = 3;
        timer.set(150);
    }
    protected void start(){
        super.start();
        host.get("arms").add(100,true);
        host.get("health").add(-5,false);
    }
    public void end(){
        host.get("arms").add(-100,false);
        host.get("legs").add(-100,false);
        super.end();
    }
}
