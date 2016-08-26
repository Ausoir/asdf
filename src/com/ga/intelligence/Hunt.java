package com.ga.intelligence;

import com.ga.map.Global;
import com.ga.tool.Timer;
import com.ga.unit.Actions;
import com.ga.unit.Man;
import com.ga.unit.Unit;

/**
 * Created by Ga on 7/30/2016.
 */
public class Hunt implements Task {
    private Man host;
    private Unit target;
    private Timer skill;
    private float range;

    public Hunt(Man host, Unit target) {
        this.host = host;
        this.target = target;
        skill = new Timer(Global.r.nextInt(30));
        range = host.getWeapon().getPoint().getAdd(host.pos.getMinus()).getDist();
    }

    public void update() {
        if (skill.update()){
            skill.set(Global.r.nextInt(150));
            host.brain.useSkill();
        }
        int x = (int) ((target.pos.x+target.vel.x) - (host.pos.x+host.vel.x));
        int y = (int) (target.pos.y - host.pos.y);
        host.face(target.pos);
        float dist = (float) (Math.sqrt((x*x)+(y*y))-range);
        if (y < -20)
            host.setState(Actions.JUMP);
        if (host.isExhausted()){
            if (dist > 300){
                if (x > 0) host.setState(Actions.WALKR);
                else if (x < 0) host.setState(Actions.WALKL);
            } else if (dist < 300){
                if (x > 0) host.setState(Actions.WALKL);
                else if (x < 0) host.setState(Actions.WALKR);
            }
        } else if (dist <= 0) {
            if (dist < -range+10) {
                host.brain.setMove(Intelligence.Moves.OVERLAP);
            } else if (dist < -range+50) {
                host.brain.setMove(Intelligence.Moves.CLOSE);
            } else if (dist > -10) { //edge
                host.brain.setMove(Intelligence.Moves.CHASING);
            } else if (target.vel.x == 0){
                host.brain.setMove(Intelligence.Moves.STANDING);
            } else {
                host.brain.setMove(Intelligence.Moves.CHASING);
            }
        } else {
            if (x > 0) host.setState(Actions.WALKR);
            else if (x < 0) host.setState(Actions.WALKL);
        }
    }
}
