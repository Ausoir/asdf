package com.ga.unit;

import com.ga.tool.P;

/**
 * Created by Ga on 8/12/2016.
 */
public class Unit extends Thing {
    public int faction = 2;
    public P vel = new P(0, 0);
    public Unit(String name, P pt, int depth) {
        super(name, pt, depth);
    }
    public P getCollisionPoint(){
        return pos;
    }
    public void hit(Unit hitter, int dmg){
    }
    public void update(){
        pos.add(vel);
    }
}
