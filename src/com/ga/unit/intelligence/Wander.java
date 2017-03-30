package com.ga.unit.intelligence;

import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.tool.Timer;
import com.ga.unit.skill.Actions;
import com.ga.unit.Unit;

/**
 * Created by Ga on 7/30/2016.
 */
class Wander implements Task {
    private Unit host;
    private Timer tick;
    protected Wander(Unit host){
        this.host = host;
        tick = new Timer(2, true);
    }

    @Override
    public void blocked(boolean fall) {
        host.face(-host.dir, true);
        host.setState(Actions.Walk);
        tick.set(30);
    }

    public void update(){
        if (tick.update()) {
            int dir = Global.r.nextInt(3);
            if (dir == 1) host.setState(Actions.Walk);
            else {
                host.face(dir-1, false);
                host.setState(Actions.Idle);
            }
            tick.set(Global.r.nextInt(25)+1);
        }
    }

    @Override
    public int getAlertness() {
        return 0;
    }
}
