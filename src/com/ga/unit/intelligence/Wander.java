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
    private int[] base;
    private int current;
    protected Wander(Unit host, int[] base){
        this.host = host;
        tick = new Timer(2, true);
        this.base = base;
        current = Global.r.nextInt(3);
    }

    @Override
    public void blocked() {
        if (host.dir == 1) base[current] = host.rand.nextInt((int) host.pos.x);
        if (host.dir == -1) base[current] = host.rand.nextInt((int) (Global.map.size.x-host.pos.x))+(int) host.pos.x;
        host.face(-host.dir);
        host.setState(Actions.Walk);
        tick.set(30);
    }

    public void update(){
        if (tick.update()) {
            int dir = Global.r.nextInt(3);
            if (host.rand.nextFloat() < .3f) current = dir;
            if (dir == 0) host.setState(Actions.Idle);
            else if (dir == 1) host.setState(Actions.Walk);
            else if (dir >= 2) {
                host.face(new P(base[current],0));
                host.setState(Actions.Walk);
            }
            tick.set(Global.r.nextInt(25)+1);
        }
    }

    @Override
    public int getAlertness() {
        return 0;
    }
}
