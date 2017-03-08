package com.ga.unit.intelligence;

import com.ga.map.Global;
import com.ga.unit.Matter;
import com.ga.unit.Unit;

/**
 * Created by Ga on 3/6/2017.
 */
public class ResidentIntelligence extends Intelligence {
    public ResidentIntelligence(Unit man) {
        super(man);
    }
    @Override
    public void hear(Matter u, String p){
        super.hear(u, p);
        if (u == Global.player.host){
            host.say("@talk");
        }
    }
}