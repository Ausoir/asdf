package com.ga.unit.intelligence;

import com.ga.unit.skill.Actions;
import com.ga.unit.Unit;

/**
 * Created by Ga on 7/30/2016.
 */
public class Idle implements Task {
    private Unit host;
    public Idle(Unit host){
        this.host = host;
    }

    @Override
    public void blocked() {

    }

    public void update(){
        host.setState(Actions.Idle);
    }

    @Override
    public int getAlertness() {
        return 0;
    }
}
