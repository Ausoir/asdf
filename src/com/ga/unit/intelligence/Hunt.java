package com.ga.unit.intelligence;

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
    private EnemyIntelligence ai;
    private Unit target;
    private Timer skill;
    private float range;

    public Hunt(Man host, Unit target) {
        this.host = host;
        ai = (EnemyIntelligence) host.brain;
        this.target = target;
        skill = new Timer(Global.r.nextInt(30));
        range = host.getWeapon().getPoint().getAdd(host.pos.getMinus()).getDist();
    }

    public void update() {
        if (skill.update()){
            skill.set(Global.r.nextInt(150));
            ai.useSkill();
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
                ai.setMove(EnemyIntelligence.Moves.OVERLAP);
            } else if (dist < -range+50) {
                ai.setMove(EnemyIntelligence.Moves.CLOSE);
            } else if (dist > -10) { //edge
                ai.setMove(EnemyIntelligence.Moves.CHASING);
            } else if (target.vel.x == 0){
                ai.setMove(EnemyIntelligence.Moves.STANDING);
            } else {
                ai.setMove(EnemyIntelligence.Moves.CHASING);
            }
        } else {
            if (x > 0) host.setState(Actions.WALKR);
            else if (x < 0) host.setState(Actions.WALKL);
        }
    }
}
