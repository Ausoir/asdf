package com.ga.unit.intelligence;

import com.ga.unit.Matter;
import com.ga.unit.Unit;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Skills;

/**
 * Created by Ga on 7/30/2016.
 */
public class Follow implements Task {
    private Unit host;
    private Matter target;
    public Follow(Unit host, Matter target) {
        this.host = host;
        this.target = target;
    }

    @Override
    public void blocked() {
        host.say("Follow blocked!");
    }

    public void update() {
        if (target == null) return;
        int x = (int) ((target.pos.x+target.vel.x) - (host.pos.x+host.vel.x));
        host.face(target.pos);
        int absX = Math.abs(x);
        if (absX > 160) {
            host.setState(Actions.Walk);
        } else if (absX < 40) host.setState(Actions.Idle);
    }

    @Override
    public int getAlertness() {
        return 0;
    }
}
