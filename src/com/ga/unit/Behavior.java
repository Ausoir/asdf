package com.ga.unit;

import com.ga.game.Observer;
import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.map.Updater;
import com.ga.tool.P;
import com.ga.unit.skill.Actions;

import java.util.ArrayList;

/**
 * Created by ga on 6/5/17. asd
 */
public enum Behavior {
    Idle {
        boolean update(Unit host) {
            return host.brain.taskProgress++ > 32;
        }
    }, Wander {
        public void blocked(Unit host, boolean fall) {
            host.face(-host.dir, true);
            if (host.dir == 1) host.setState(Actions.Right);
            else if (host.dir == -1) host.setState(Actions.Left);
            host.delay.time = 100;
        }
        boolean update(Unit host) {
            if (Global.r.nextInt(16) == 0) {
                int dir = Global.r.nextInt(3)-1;
                if (dir == 1) host.setState(Actions.Right);
                else if (dir == -1) host.setState(Actions.Left);
                else {
                    host.face(dir - 1, false);
                    host.brain.removeTask();
                }
            }
            return false;
        }
    }, Travel {
        String destination = "", var = ""; //todo: CANNOT HAVE STATIC VALUES
        public void blocked(Unit host, boolean fall) {
        }
        public void load(Unit host, String p){
            super.load(host, p);
            var = p;
            moveToward(host);
        }
        String getVar(Unit host){
            return var;
        }
        private boolean moveToward(Unit host){
            for (Prop i : host.map.updater.props){
                if (i.factoryId == Stock.Travel.ordinal()){
                    if (i.name.equals(var)){
                        destination = i.pos.toString();
                        return true;
                    }
                }
            }
            for (Observer i : host.map.updater.addQueue){
                if (i instanceof Prop) {
                    Prop prop = (Prop) i;
                    if (prop.factoryId == Stock.Travel.ordinal()) {
                        if (prop.name.equals(var)){
                            destination = prop.pos.toString();
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        boolean update(Unit host) {
            P target = new P(destination);
            float dist = host.pos.getSubtract(target).getDist();
            Strategy.Brute.micromanage(host, Strategy.getMove(host, target), dist);
            if (dist <= host.getSpeed()*400) {
                for (Prop i : host.map.updater.props){
                    if (i.factoryId == Stock.Travel.ordinal() && i.pos.toString().equals(destination)){
                        host.brain.removeTask();
                        Map oldmap = host.map;
                        host.map = Global.getMap(i.name);
                        host.pos.set(host.map.updater.getEntrancePos(i.seed, host.map.getFailsafePos()));
                        String actor = host.toString();
                        host.map = oldmap;
                        host.remove();
                        Unit unit = (Unit) Stock.get(host.map, actor);
                        unit.start().controlId = host.controlId;
                        return true;
                    }
                }
            }
            return false;
        }
    }, Follow {
        public int getAlertness(){
            return 1;
        }
        public void blocked(Unit host, boolean fall) {
        }
        void load(Unit host, String p){
            if (!p.equals("x")) host.brain.target = Updater.getActor(Integer.parseInt(p));
        }
        String getVar(Unit host){
            return ""+host.brain.target.controlId;
        }
        boolean update(Unit host) {
            if (host.brain.target != null) {
                int x = (int) ((host.brain.target.pos.x + host.brain.target.vel.x) - (host.pos.x + host.vel.x));
                host.face(host.brain.target.pos.x > host.pos.x ? 1 : -1, false);
                int absX = Math.abs(x);
                if (absX > 160) {
                    host.face(x > 0 ? 1 : -1, false);
                    host.setState(Actions.Right);
                } else if (absX < 40) host.setState(Actions.Idle);
                return false;
            }
            return true;
        }
    }, Search {
        public int getAlertness(){
            return 2;
        }
        boolean update(Unit host) {
            host.brain.taskProgress++;
            host.face(host.dir * -1, false);
            if (host.brain.taskProgress > 32) {
                host.brain.removeTask();
                return true;
            } else if (host.brain.taskProgress % 4 == 0) {
                ArrayList<Unit> allunits = host.map.updater.collideUnit(host.pos, host.brain.taskProgress*128, host.faction);
                for (Unit i : allunits) {
                    host.brain.target = i;
                    host.brain.setTask(Hunt);
                    return true;
                }
            }
            return false;
        }
    }, Hunt {
        public int getAlertness(){
            return 3;
        }
        void blocked(Unit host, boolean fall) {
            host.vel.x = host.getSpeed() * host.dir;
            host.setState(Actions.Jump);
        }
        void load(Unit host, String p){
            if (!p.equals("x")) host.brain.target = Updater.getActor(Integer.parseInt(p));
        }
        String getVar(Unit host){
            return host.brain.target == null ? "" : ""+host.brain.target.controlId;
        }
        boolean update(Unit host) {
            Unit target = host.brain.target;
            if (target == null){
                return true;
            } else {
                host.face(host.brain.target.pos.x>host.pos.x?1:-1, true);
                if (host.delay.over()) {
                    if (host.map == target.map) {
                        Strategy.values()[host.brain.type].update(host);
                    } else if (target.map != null) {
                        host.brain.setTask(Travel);
                        host.brain.tasks.peek().load(host, target.map.name.toString());
                    }
                    if (target.state == Actions.Dead) host.brain.target = null;
                }
            }
            return false;
        }
    };

    void load(Unit host, String p){
    }
    String getVar(Unit host){
        return "x";
    }
    int getAlertness(){
        return 0;
    }
    boolean update(Unit host){
        return true;
    }
    void blocked(Unit host, boolean fall){

    }
    public String toString(Unit host){
        return "["+ordinal()+","+getVar(host)+"]";
    }
}