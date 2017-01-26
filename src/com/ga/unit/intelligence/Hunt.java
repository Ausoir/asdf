package com.ga.unit.intelligence;

import com.ga.unit.Unit;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Skills;
import com.ga.unit.skill.Spells;

/**
 * Created by Ga on 7/30/2016.
 */
public class Hunt implements Task {
    private Unit host;
    private float range = 0;

    public Hunt(Unit host) {
        this.host = host;
        if (host.brain.target != null) range = host.getReach()+(host.brain.target.image.sizeRadius/2);
    }

    @Override
    public void blocked() {
        host.say("Blocked!");
        host.face(-host.dir);
        host.setState(Actions.Walk);
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

            if (host.state instanceof Spells){
                host.delay.set(300);
            }
            if (t == -1){
                if (y < -50){
                    if (host.vel.x == 0) {
                        host.face(x > 0 ? 1 : -1);
                        host.setState(Actions.Walk);
                    } else host.setState(Skills.Jump);
                } else {
                    host.face(x > 0 ? 1 : -1);
                    host.setState(Actions.Walk);
                }
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
