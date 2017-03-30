package com.ga.unit.intelligence;

import com.ga.unit.skill.Actions;
import com.ga.unit.Prop;
import com.ga.unit.Unit;

/**
 * Created by Ga on 8/2/2016.
 */
public class Flee implements Task {
    private Unit host;
    private Prop target;

    public Flee(Unit host, Prop target) {
        this.host = host;
        this.target = target;
    }

    @Override
    public void blocked(boolean fall) {
        if (host.rand.nextFloat() < .5f) host.setState(Actions.Jump);
        else host.setState(Actions.Down);
    }

    public void update() {
        if (target == null) return;
        int x = (int) (target.pos.x - host.pos.x);
        host.face(x < 0 ? 1 : -1, false);
        host.setState(Actions.Sprint);
    }

    @Override
    public int getAlertness() {
        return 2;
    }
}
