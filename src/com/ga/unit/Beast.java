package com.ga.unit;

import com.ga.unit.intelligence.UnitIntelligence;

/**
 * Created by Ga on 9/5/2016.
 */
public class Beast extends Unit {
    public Beast(String p) {
        super(p);
        clip = new Body(this, name);
        brain = new UnitIntelligence(this);
    }

    @Override
    public String toString(){
        String r = super.toString();
        if (!r.equals("")) return r;
        return r;
    }
}