package com.ga.unit.intelligence;

import com.ga.map.Global;
import com.ga.unit.Tag;
import com.ga.unit.Unit;
import com.ga.unit.skill.Actions;

/**
 * Created by ga on 6/5/17. asd
 */
public enum Behavior {
    Idle(0){

    }, Wander(0){
        public void blocked(Unit host, boolean fall) {
            host.face(-host.dir, true);
            host.setState(Actions.Walk);
        }
        public void update(Unit host){
            if (Global.r.nextInt(16) == 0) {
                int dir = Global.r.nextInt(3);
                if (dir == 1) host.setState(Actions.Walk);
                else {
                    host.face(dir - 1, false);
                    host.setState(Actions.Idle);
                }
            }
        }
    }, Travel(0){
        public void blocked(Unit host, boolean fall) {
        }
        public void update(Unit host){
            int dir = host.pos.x > host.target.x ? 1 : -1;
            host.face(dir, false);
            host.setState(Actions.Walk);
        }
    }, Follow(0){
        public void blocked(Unit host, boolean fall) {
            host.brain.leader = null;
        }
        public void update(Unit host){
            if (host.brain.target != null) {
                int x = (int) ((host.brain.target.pos.x + host.brain.target.vel.x) - (host.pos.x + host.vel.x));
                host.face(host.brain.target.pos.x > host.pos.x ? 1 : -1, false);
                int absX = Math.abs(x);
                if (absX > 160) {
                    host.face(x > 0 ? 1 : -1, false);
                    host.setState(Actions.Walk);
                } else if (absX < 40) host.setState(Actions.Idle);
            }
        }
    }, Hunt(1){
        void blocked(Unit host, boolean fall){
            host.vel.x = host.getSpeed()*host.dir;
            host.setState(Actions.Jump);
        }
        void update(Unit host){
            Unit target = host.brain.target;
            if (target == null || !host.delay.over()) return;
            int x = (int) ((target.pos.x+target.vel.x) - (host.pos.x+host.vel.x));
            int y = (int) (target.pos.y - host.pos.y);
            host.face(target.pos.x>host.pos.x?1:-1, false);
            float dist = Math.abs(x)-host.getReach();
            if (dist < 1000) {
                if (y < -64) {
                    Actions.Walk.update(host);
                    host.setState(Actions.Jump);
                } else {
                    if (y > 64 && Global.r.nextBoolean()) host.setState(Actions.Drop);
                    else {
                        int t = host.performActions((int) dist);
                        if (host.state.getTags().contains(Tag.Channeling)) {
                            host.delay.set(60 + Global.r.nextInt(80));
                        }
                        if (t == 2) { //no stamina
                            host.setState(Actions.Rest);
                            host.delay.time += 40;
                        }
                    }
                }
            }
        }
    };
    int alertness;
    Behavior(int alertness){
        this.alertness = alertness;
    }
    void update(Unit host){

    }
    void blocked(Unit host, boolean fall){

    }
}