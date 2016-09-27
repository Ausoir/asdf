package com.ga.unit;

import com.ga.tool.P;
import com.ga.unit.intelligence.EnemyIntelligence;
import com.ga.unit.skill.Moveset;
import com.ga.unit.skill.Skills;

/**
 * Created by Ga on 9/5/2016.
 */
public class Beast extends Unit {
    private String type;

    public Beast(String type, String name, P pt, int seed) {
        super(name, pt, seed);
        this.name = name;
        this.type = type;
        clip = new Body(this, type);
        get("Health").setTotal(5);
        brain = new EnemyIntelligence(this);
        moveset.put("Melee",new Moveset[]{Skills.Kick});
    }

    @Override
    public String toString(){
        String r = super.toString();
        if (!r.equals("")) return type+":"+r;
        return r;
    }
}
