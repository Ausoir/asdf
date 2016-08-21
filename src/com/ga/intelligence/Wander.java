package com.ga.intelligence;

import com.ga.map.Global;
import com.ga.tool.Timer;
import com.ga.unit.Actions;
import com.ga.unit.Man;

/**
 * Created by Ga on 7/30/2016.
 */
public class Wander implements Task {
    private Man host;
    private Timer tick;
    public Wander(Man host){
        this.host = host;
        tick = new Timer(1);
    }
    public void update(){
        if (tick.update()) {
            int dir = Global.r.nextInt(3);
            if (dir == 2) host.setState(Actions.IDLE);
            else host.setState(dir == 1 ? Actions.WALKR : Actions.WALKL);
            tick.set(Global.r.nextInt(500)+10);
        }
    }
}
