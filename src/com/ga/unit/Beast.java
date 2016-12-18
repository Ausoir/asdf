package com.ga.unit;

import com.ga.map.Global;
import com.ga.unit.intelligence.UnitIntelligence;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Moveset;
import com.ga.unit.skill.Skills;

/**
 * Created by Ga on 9/5/2016.
 */
public class Beast extends Unit {
    public Beast(String p) {
        super(p);
        clip = new Body(this, name);
        if (Global.server.server) brain = new UnitIntelligence(this);
        moveset.put("Melee",new Moveset[]{Actions.WalkL});
    }

    @Override
    public String toString(){
        String r = super.toString();
        if (!r.equals("")) return r;
        return r;
    }
}