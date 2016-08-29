package com.ga.unit.skill;

/**
 * Created by Ga on 8/1/2016.
 */
public class Adrenaline extends Skill {
    private int added = 0;
    public Adrenaline(){
        stat = "legs";
        cost = 2;
        timer.set(40);
    }
    protected void start(){
        super.start();
        added = host.get("arms").get()+3;
        host.get("speed").add(added,true);
        timer.set(40+(host.get("legs").max*3));
    }
    public void end(){
        host.get("speed").add(-added,true);
        super.end();
    }
}
