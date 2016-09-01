package com.ga.unit.skill;

import com.ga.tool.Performer;
import com.ga.unit.Man;

/**
 * Created by Ga on 8/30/2016.
 */
public class Skill extends Performer {
    protected Man host;
    void start(Man host){
        this.host = host;
    }
}
