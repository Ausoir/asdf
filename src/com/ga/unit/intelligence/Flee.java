package com.ga.unit.intelligence;

import com.ga.unit.Actions;
import com.ga.unit.Man;
import com.ga.unit.Unit;

/**
 * Created by Ga on 8/2/2016.
 */
public class Flee implements Task {
    private Man host;
    private Unit target;

    public Flee(Man host, Unit target) {
        this.host = host;
        this.target = target;
    }

    public void update() {
        int x = (int) (target.pos.x - host.pos.x);
        host.face(x < 0 ? -1 : 1);
        if (x > 0) host.setState(Actions.WalkL);
        else host.setState(Actions.WalkR);
    }
}
