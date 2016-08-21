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
    private int range;

    public Hunt(Man host, Unit target) {
        this.host = host;
        this.target = target;
        skill = new Timer(Global.r.nextInt(30));
        range = (int) Math.pow(host.height / 2 + (int) host.getWeapon().length,2);
    }

    public void update() {
        if (skill.update()){
            skill.set(Global.r.nextInt(150));
            host.brain.useSkill();
        }
        int x = (int) (target.pos.x - host.pos.x);
        int y = (int) (target.pos.y - host.pos.y);
        host.face(target.pos);
        int dist = ((x*x)+(y*y))-range;
        if (y < -20)
            host.setState(Actions.JUMP);
        if (host.isExhausted()){
            if (dist > 80000){
                if (x > 0) host.setState(Actions.WALKR);
                else if (x < 0) host.setState(Actions.WALKL);
            } else if (dist < 30000){
                if (x > 0) host.setState(Actions.WALKL);
                else if (x < 0) host.setState(Actions.WALKR);
            }
        } else if (dist <= 0) {
            if (Math.abs(target.vel.x) == 0){
                host.vel.x = 0;
            }
            host.brain.useMove();
        } else {
            if (x > 0) host.setState(Actions.WALKR);
            else if (x < 0) host.setState(Actions.WALKL);
        }
    }
}
