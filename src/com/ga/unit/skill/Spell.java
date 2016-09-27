package com.ga.unit.skill;

import com.ga.tool.Performer;
import com.ga.unit.Unit;

/**
 * Created by Ga on 8/30/2016.
 */
public class Spell extends Performer {
    protected Unit host;
    protected int level;
    public void start(Unit host, int level){
        this.host = host;
        this.level = level;
    }
}
