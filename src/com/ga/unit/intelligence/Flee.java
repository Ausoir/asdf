package com.ga.unit.intelligence;

import com.ga.unit.skill.Actions;
import com.ga.unit.Matter;
import com.ga.unit.Unit;
import com.ga.unit.skill.Skills;

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

    @Override
    public void blocked() {
        if (host.rand.nextFloat() < .5f) host.setState(Skills.Jump);
        else host.setState(Actions.Down);
    }

    public void update() {
        if (target == null) return;
        int x = (int) (target.pos.x - host.pos.x);
        host.face(x < 0 ? -1 : 1);
        host.setState(Actions.Walk);
    }

    @Override
    public int getAlertness() {
        return 2;
    }
}
