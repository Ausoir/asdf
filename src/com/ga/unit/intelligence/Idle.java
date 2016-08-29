package com.ga.unit.intelligence;

import com.ga.map.Global;
import com.ga.tool.Timer;
import com.ga.unit.Actions;
import com.ga.unit.Man;

/**
 * Created by Ga on 7/30/2016.
 */
public class Idle implements Task {
    private Man host;
    public Idle(Man host){
        this.host = host;
    }
    public void update(){
        host.setState(Actions.IDLE);
    }
}
