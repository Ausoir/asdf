package com.ga.unit;

import com.ga.game.FileManager;
import com.ga.map.Global;
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
            host.setState(Actions.Right);
        }

        boolean update(Unit host) {
            if (Global.r.nextInt(16) == 0) {
                int dir = Global.r.nextInt(3);
                if (dir == 1) host.setState(Actions.Right);
                else {
                    host.face(dir - 1, false);
                    host.setState(Actions.Idle);
                }
            }
            return false;
        }
    }, Travel {
        private ArrayList<String> routes = new ArrayList<>();
        public void blocked(Unit host, boolean fall) {
        }

        public void load(Unit host, int p){
            super.load(host, p);
            routes.add(host.map.name+"@0@0");
            moveToward(host.map.name.toString(), p+"");
            routes.remove(0);
        }

        private boolean routesContains(String p){
            for (String i : routes){
                if (i.split("@")[0].equals(p)) return true;
            }
            return false;
        }

        private boolean moveToward(String from, String to){
            if (from.equals(to)) return true;
            String[] objects = FileManager.read("Data/map/"+from+".map").split("%")[3].split(";");
            for (String i : objects){
                String[] sections = i.split("@");
                if (Integer.parseInt(sections[0]) == Stock.Travel.ordinal() && !routesContains(sections[1])){
                    routes.add(sections[1]+"@"+sections[2]+"@"+sections[3]);
                    if (!moveToward(sections[1], to)){
                        routes.remove(sections[1]+"@"+sections[2]+"@"+sections[3]);
                    } else return true;
                }
            }
            return false;
        }

        boolean update(Unit host) {
            if (routes.size() > 0) {
                String[] data = routes.get(0).split("@");
                if (!host.map.name.equals(data[1])) {
                    P target = new P(data[2]);
                    host.face(host.pos.x > target.x ? -1 : 1, false);
                    host.setState(Actions.Right);
                    if (host.pos.getSubtract(target).getDist() <= host.getSpeed() * 400) {
                        routes.remove(0);
                        Global.server.requestChangeMap(data[0] + "@" + data[1]);
                    }
                    return false;
                }
            }
            return true;
        }
    }, Follow {
        public int getAlertness(){
            return 1;
        }
        public void blocked(Unit host, boolean fall) {
        }
        void load(Unit host, int p){
            if (p > 0){
                host.brain.target = Updater.getActor(p);
            }
        }
        int getVar(Unit host){
            return host.brain.target.controlId;
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
            if (host.brain.taskProgress > 64) {
                host.brain.setTask(Idle);
                return true;
            } else if (host.brain.taskProgress % 4 == 0) {
                ArrayList<Unit> allunits = host.map.updater.collideUnit(host.pos, host.brain.taskProgress*128, host.faction);
                for (Unit i : allunits) {
                    host.brain.target = i;
                    host.brain.setTask(Hunt);
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
        void load(Unit host, int p){
            if (p > 0){
                host.brain.target = Updater.getActor(p);
            }
        }
        int getVar(Unit host){
            return host.brain.target == null ? -1 : host.brain.target.controlId;
        }
        boolean update(Unit host) {
            Unit target = host.brain.target;
            if (target == null){
                return true;
            } else if (host.delay.over()) {
                if (host.map == target.map) {
                    Strategy.values()[host.brain.type].update(host);
                } else {
                    host.brain.setTask(Travel);
                    host.brain.tasks.peek().load(host, target.map.name.getIndex());
                }
                if (target.state == Actions.Dead) host.brain.target = null;
            }
            return false;
        }
    };

    void load(Unit host, int p){
    }
    int getVar(Unit host){
        return 0;
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
        return ordinal()+":"+getVar(host);
    }
}