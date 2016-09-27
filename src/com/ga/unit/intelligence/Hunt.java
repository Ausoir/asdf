package com.ga.unit.intelligence;

import com.ga.map.Global;
import com.ga.tool.Timer;
import com.ga.unit.Matter;
import com.ga.unit.Unit;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Skills;

/**
 * Created by Ga on 7/30/2016.
 */
public class Hunt implements Task {
    private Unit host;
    private Matter target;
    private Timer random;
    private float range;

    public Hunt(Unit host, Matter target) {
        this.host = host;
        this.target = target;
        random = new Timer(Global.r.nextInt(30));
        range = host.getReach();
    }

    public void update() {
        if (target == null) return;
        if (random.update()){
            random.set(Global.r.nextInt(150));
            host.performMoveset("Random");
        }
        int x = (int) ((target.pos.x+target.vel.x) - (host.pos.x+host.vel.x));
        int y = (int) (target.pos.y - host.pos.y);
        host.face(target.pos);
        float dist = (float) (Math.sqrt((x*x)+(y*y))-range);
        if (y < -20)
            host.setState(Skills.Jump);
        if (host.isExhausted()){
            if (dist > 300){
                host.setState(Skills.Rest);
            } else if (dist < 300){
                if (x > 0) host.setState(Actions.WalkL);
                else if (x < 0) host.setState(Actions.WalkR);
            }
        } else if (dist <= 0) {
            if (dist <= -10) { //edge
                host.performMoveset("Melee");
            } else if (dist > -10 && dist < 500) {
                host.performMoveset("Range");
            }
        } else {
            if (x > 0) host.setState(Actions.WalkR);
            else if (x < 0) host.setState(Actions.WalkL);
        }
    }
}
