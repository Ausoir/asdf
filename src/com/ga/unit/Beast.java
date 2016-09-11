package com.ga.unit;

import com.ga.tool.P;
import com.ga.unit.intelligence.EnemyIntelligence;

/**
 * Created by Ga on 9/5/2016.
 */
public class Beast extends Unit {
    private String type;

    public Beast(String type, String name, P pt, int seed) {
        super("unit/"+name, pt, seed);
        this.name = name;
        this.type = type;
        clip = new ClipUnit(this, type);
        get("Health").setTotal(5);
        brain = new EnemyIntelligence(this);
        moveset.put("Melee",new Moveset[]{Actions.Kick});
    }

    @Override
    public String toString(){
        String r = super.toString();
        if (!r.equals("")) return type+":"+r;
        return r;
    }
}
