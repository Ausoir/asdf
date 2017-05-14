package com.ga.unit.intelligence;

import com.ga.map.Global;
import com.ga.unit.Tag;
import com.ga.unit.Unit;
import com.ga.unit.skill.Actions;

/**
 * Created by Ga on 7/30/2016. asd
 */
class Hunt implements Task {
    private Unit host;
    private float range = 0;

    Hunt(Unit host) {
        this.host = host;
        if (host.brain.target != null) range = host.getReach();
    }

    @Override
    public void blocked(boolean fall) {
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
                Actions.Walk.update(host);
                host.setState(Actions.Jump);
            } else {
                if (y > 64 && Global.r.nextBoolean()) host.setState(Actions.Drop);
                else {
                    int t = host.performActions((int) dist);
                    if (host.state.getTags().contains(Tag.Channel)) {
                        host.delay.set(60 + Global.r.nextInt(80));
                    }
                    if (t == 2 && target.vel.x > 0 != host.dir > 0) { //no stamina
                        host.setState(Actions.Rest);
                        host.delay.time += 40;
                    }
                }
            }
        }
    }

    @Override
    public int getAlertness() {
        return 1;
    }
}
