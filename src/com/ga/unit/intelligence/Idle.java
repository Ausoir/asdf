package com.ga.unit.intelligence;

import com.ga.unit.Actions;
import com.ga.unit.Unit;

/**
 * Created by Ga on 7/30/2016.
 */
public class Idle implements Task {
    private Unit host;
    public Idle(Unit host){
        this.host = host;
    }
    public void update(){
        host.setState(Actions.Idle);
    }
}
