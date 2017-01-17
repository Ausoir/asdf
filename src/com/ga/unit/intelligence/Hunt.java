package com.ga.unit.intelligence;

import com.ga.unit.Unit;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Skills;

/**
 * Created by Ga on 7/30/2016.
 */
public class Hunt implements Task {
    private Unit host;
    private float range;

    public Hunt(Unit host) {
        this.host = host;
        range = host.getReach()+(host.brain.target.image.sizeRadius/2);
    }

    @Override
    public void blocked() {
        host.say("Blocked!");
        if (host.dir > 0) host.setState(Actions.WalkL);
        else host.setState(Actions.WalkR);
    }

    public void update() {
        Unit target = host.brain.target;
        if (target == null || !host.delay.over()) return;
        int x = (int) ((target.pos.x+target.vel.x) - (host.pos.x+host.vel.x));
        int y = (int) (target.pos.y - host.pos.y);
        host.face(target.pos);
        float dist = (float) (Math.sqrt((x*x)+(y*y))-range);
        if (host.isExhausted()){
            if (host.rand.nextFloat() < .1f) host.get("Stamina").heal();
            host.setState(Skills.Rest);
        } else if (dist < 600) {
            int t = host.performMoveset((int) dist);
            host.target = target.pos.getSubtract(host.pos).add(0, -30);
            if (t == -1){
                if (x > 0) host.setState(Actions.WalkR);
                else if (x < 0) host.setState(Actions.WalkL);
            } else if (t == 2){
                host.setState(Skills.Rest);
                host.delay.time += 28;
            }
        }
    }

    @Override
    public int getAlertness() {
        return 1;
    }
}
