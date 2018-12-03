package com.ga.unit;

import com.ga.map.Global;
import com.ga.map.Updater;
import com.ga.unit.skill.Actions;

/**
 * Created by ga on 6/5/17. asd
 */
public enum Behavior {
    Idle {
        void update(Unit host) {
            host.setState(Actions.Idle);
            if (host.brain.taskProgress++ > 32) host.brain.removeTask();
        }
    }, Wander {
        public void blocked(Unit host, boolean fall) {
            host.face(-host.dir, true);
            if (host.dir == 1) host.setState(Actions.Right);
            else if (host.dir == -1) host.setState(Actions.Left);
            host.delay.time = 100;
        }
        void update(Unit host) {
            if (Global.r.nextInt(16) == 0) {
                int dir = Global.r.nextInt(3)-1;
                if (dir == 1) host.setState(Actions.Right);
                else if (dir == -1) host.setState(Actions.Left);
                else {
                    host.face(dir - 1, false);
                    host.brain.removeTask();
                }
            }
        }
    }, Travel {
        int direction = 0;
        public void blocked(Unit host, boolean fall) {
            direction = 0;
        }
        void load(Unit host, String p){
            if (!p.isEmpty()) direction = Integer.parseInt(p);
        }
        String getVar(Unit host){
            return direction+"";
        }
        void update(Unit host) {
            if (direction != 0) {
                host.face(direction, false);
                host.setState(Actions.Right);
            }
            host.brain.removeTask();
        }
    }, Follow {
        public int getAlertness(){
            return 1;
        }
        public void blocked(Unit host, boolean fall) {
        }
        void update(Unit host) {
            if (host.brain.target != null) {
                int x = (int) ((host.brain.target.pos.x + host.brain.target.vel.x) - (host.pos.x + host.vel.x));
                host.face(host.brain.target.pos.x > host.pos.x ? 1 : -1, false);
                int absX = Math.abs(x);
                if (absX > 160) {
                    host.face(x > 0 ? 1 : -1, false);
                    host.setState(Actions.Right);
                } else if (absX < 40) host.setState(Actions.Idle);
            } else host.brain.removeTask();
        }
    }, Search {
        public int getAlertness(){
            return 2;
        }
        void update(Unit host) {
            host.brain.taskProgress++;
            host.face(host.dir * -1, false);
            if (host.brain.taskProgress > 16) {
                host.brain.removeTask();
            } else if (host.brain.taskProgress % 4 == 0) {
                host.brain.target = host.map.updater.collideUnit(host.pos, host.brain.taskProgress*128, host.faction).getFirst();
                host.brain.removeTask();
                host.brain.setTask(Hunt);
            }
        }
    }, Hunt {
        public int getAlertness(){
            return 3;
        }
        void blocked(Unit host, boolean fall) {
            host.vel.x = host.getSpeed() * host.dir;
            host.setState(Actions.Jump);
        }
        void update(Unit host) {
            Unit target = host.brain.target;
            if (target == null || host.map != target.map || target.state == Actions.Dead){
                host.brain.target = null;
                host.brain.removeTask();
            } else {
                host.face(host.brain.target.pos.x>host.pos.x?1:-1, true);
                Strategy.values()[host.brain.type].update(host);
            }
        }
    };
    void load(Unit host, String p){
        if (!p.isEmpty()) host.brain.target = Updater.getActor(Integer.parseInt(p));
    }
    String getVar(Unit host){
        return host.brain.target == null ? "" : ""+host.brain.target.controlId;
    }
    int getAlertness(){
        return 0;
    }
    void update(Unit host){
    }
    void blocked(Unit host, boolean fall){
    }
    public String toString(Unit host){
        return "["+ordinal()+","+getVar(host)+"]";
    }
}