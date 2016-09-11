package com.ga.unit.intelligence;

import com.ga.unit.Actions;
import com.ga.unit.Matter;
import com.ga.unit.Unit;

/**
 * Created by Ga on 8/2/2016.
 */
public class Flee implements Task {
    private Unit host;
    private Matter target;

    public Flee(Unit host, Matter target) {
        this.host = host;
        this.target = target;
    }

    public void update() {
        if (target == null) return;
        int x = (int) (target.pos.x - host.pos.x);
        host.face(x < 0 ? -1 : 1);
        if (x > 0) host.setState(Actions.WalkL);
        else host.setState(Actions.WalkR);
    }
}
