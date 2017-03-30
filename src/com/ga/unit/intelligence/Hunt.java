package com.ga.unit.intelligence;

import com.ga.unit.Unit;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Spells;

/**
 * Created by Ga on 7/30/2016.
 */
class Hunt implements Task {
    private Unit host;
    private float range = 0;

    protected Hunt(Unit host) {
        this.host = host;
        if (host.brain.target != null) range = host.getReach();
    }

    @Override
    public void blocked(boolean fall) {
        host.brain.forget.time = 5;
        host.vel.x = host.getSpeed()*host.dir;
        host.setState(Actions.Jump);
    }

    public void update() {
        Unit target = host.brain.target;
        if (target == null || !host.delay.over()) return;
        int x = (int) ((target.pos.x+target.vel.x) - (host.pos.x+host.vel.x));
        int y = (int) (target.pos.y - host.pos.y);
        host.face(target.pos.x>host.pos.x?1:-1, false);
        float dist = Math.abs(x)-range;
        if (dist < 1000) {
            if (y < -64) {
                Actions.Walk.update(host,1);
                host.setState(Actions.Jump);
            } else if (y > 64){
                if (Math.abs(x) < 160 && host.lastLand != target.lastLand) host.setState(Actions.Drop);
            } else {
                int t = host.performMoveset((int) dist);
                if (host.state instanceof Spells) {
                    host.delay.set(300);
                }
                if (t == 2) { //no stamina
                    host.setState(Actions.Rest);
                    host.delay.time += 40;
                }
            }
        }
    }

    @Override
    public int getAlertness() {
        return 1;
    }
}
