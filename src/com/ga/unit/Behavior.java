package com.ga.unit;

import com.ga.game.MapManager;
import com.ga.map.Global;
import com.ga.map.Updater;
import com.ga.unit.skill.Actions;

import java.util.ArrayList;

/**
 * Created by ga on 6/5/17. asd
 */
public enum Behavior {
    Idle(0){
        public void update(Unit host){
            if (host.brain.taskProgress > 32) host.brain.setTask(Wander);
        }
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
        void updateOutside(Unit host){
//            String[] returns = MapManager.moveRandom(host.cmap);
//            host.cmap = returns[0];
//            host.pos.load(returns[1]);
        }
    }, Travel(1){
        public void blocked(Unit host, boolean fall) {
        }
        public void update(Unit host){
            int dir = host.pos.x > host.target.x ? 1 : -1;
            host.face(dir, false);
            host.setState(Actions.Walk);
        }
    }, Follow(1){
        public void blocked(Unit host, boolean fall) {
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
        void updateOutside(Unit host){
//            String[] returns = MapManager.moveToward(host.cmap, host.brain.target.cmap);
//            host.cmap = returns[0];
//            host.pos.load(returns[1]);
        }
    }, Search(2){
        public void update(Unit host){
            host.face(host.dir*-1, false);
            if (host.brain.taskProgress > 64){
                host.brain.setTask(Idle);
            } else if (host.brain.taskProgress % 4 == 0) {
                ArrayList<Unit> allunits = host.map.updater.collideUnit(host.pos, host.brain.taskProgress*128, host.faction);
                for (Unit i : allunits) {
                    host.brain.target = i;
                    host.brain.setTask(Hunt);
                }
            }
        }
    }, Hunt(3){
        void blocked(Unit host, boolean fall){
            host.vel.x = host.getSpeed()*host.dir;
            host.setState(Actions.Jump);
        }
        void update(Unit host){
            Unit target = host.brain.target;
            if (target == null) host.brain.setTask(Idle);
            else if (host.delay.over()) {
                int x = (int) ((target.pos.x + target.vel.x) - (host.pos.x + host.vel.x));
                int y = (int) (target.pos.y - host.pos.y);
                host.face(target.pos.x > host.pos.x ? 1 : -1, false);
                float dist = (float) Math.abs(Math.sqrt(x * x + y * y) - host.getReach());
                Actions action = Actions.Idle;
                float lowest = 1000;
                float current;
                for (Actions act : host.moveset){
                    current = act.getRange(host)-dist;
                    if (current >= 0) {
                        if (current < lowest || (current == lowest && Math.random() < .5f)) {
                            lowest = current;
                            action = act;
                        }
                    }
                }
                if (dist>action.getRange(host)) move(host);
                else {
                    if (Math.random() > host.get("Stamina").getPercentage()){
                        host.setState(Actions.Rest);
                        host.delay.time += 40;
                    } else if (host.setState(action) != 0){
                        move(host);
                    }
                    if (host.state.getTags().contains(Tag.Channeling)) {
                        host.delay.set(60 + Global.r.nextInt(80));
                    }
                }
            }
        }
        void updateOutside(Unit host){
//            String[] returns = MapManager.moveToward(host.cmap, host.brain.target.cmap);
//            host.cmap = returns[0];
//            host.pos.load(returns[1]);
        }
    };
    int alertness; //0:temporary, 1:normal, 2:alert, 3:active
    Behavior(int alertness){
        this.alertness = alertness;
    }
    void onSet(Unit host){

    }
    void update(Unit host){

    }
    void updateOutside(Unit host){

    }
    void blocked(Unit host, boolean fall){

    }
    void move(Unit host){
        if (host.setState(Actions.Sprint) != 0) host.setState(Actions.Walk);
    }
}