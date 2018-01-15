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
    Idle { Task make(Unit host) {
        return new Task(0) {
            private int taskProgress = 0;
            boolean update(Unit host) {
                taskProgress++;
                return taskProgress > 32;
            }
        };
    }}, Wander { Task make(Unit host) {
        return new Task(0) {
            public void blocked(Unit host, boolean fall) {
                host.face(-host.dir, true);
                host.setState(Actions.Walk);
            }

            boolean update(Unit host) {
                if (Global.r.nextInt(16) == 0) {
                    int dir = Global.r.nextInt(3);
                    if (dir == 1) host.setState(Actions.Walk);
                    else {
                        host.face(dir - 1, false);
                        host.setState(Actions.Idle);
                    }
                }
                return false;
            }
        };
    }}, Travel { Task make(final Unit host) {
        return new Task(0) {
            private ArrayList<String> routes = new ArrayList<>();
            public void blocked(Unit host, boolean fall) {
            }

            public void load(int p){
                super.load(p);
                routes.add(host.map.name+"@0@0");
                moveToward(host.map.name, p+"");
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
                String[] objects = FileManager.read("map/"+from+".map").split("%")[3].split(";");
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
                        host.setState(Actions.Walk);
                        if (host.pos.getSubtract(target).getDist() <= host.getSpeed() * 400) {
                            routes.remove(0);
                            Global.server.requestChangeMap(data[0] + "@" + data[1]);
                        }
                        return false;
                    }
                }
                return true;
            }
        };
    }}, Follow { Task make(final Unit host) {
        return new Task(1) {
            public void blocked(Unit host, boolean fall) {
            }

            void load(int p){
                if (p > 0){
                    host.brain.target = Updater.getUnloadedActor(p);
                }
            }

            int getVar(){
                return host.brain.target.controlId;
            }

            boolean update(Unit host) {
                if (host.brain.target != null) {
                    int x = (int) ((host.brain.target.pos.x + host.brain.target.vel.x) - (host.pos.x + host.vel.x));
                    host.face(host.brain.target.pos.x > host.pos.x ? 1 : -1, false);
                    int absX = Math.abs(x);
                    if (absX > 160) {
                        host.face(x > 0 ? 1 : -1, false);
                        host.setState(Actions.Walk);
                    } else if (absX < 40) host.setState(Actions.Idle);
                    return false;
                }
                return true;
            }
        };
    }}, Search { Task make(Unit host) {
        return new Task(2) {
            private int taskProgress = 0;
            boolean update(Unit host) {
                taskProgress++;
                host.face(host.dir * -1, false);
                if (taskProgress > 64) {
                    host.brain.setTask(Idle);
                    return true;
                } else if (taskProgress % 4 == 0) {
                    ArrayList<Unit> allunits = host.map.updater.collideUnit(host.pos, taskProgress * 128, host.faction);
                    for (Unit i : allunits) {
                        host.brain.target = i;
                        host.brain.setTask(Hunt);
                    }
                }
                return false;
            }
        };
    }}, Hunt { Task make(final Unit host) {
        return new Task(3) {
            void blocked(Unit host, boolean fall) {
                host.vel.x = host.getSpeed() * host.dir;
                host.setState(Actions.Jump);
            }

            void load(int p){
                if (p > 0){
                    host.brain.target = Updater.getUnloadedActor(p);
                }
            }

            int getVar(){
                return host.brain.target.controlId;
            }

            boolean update(Unit host) {
                Unit target = host.brain.target;
                if (target == null){
                    return true;
                } else if (host.delay.over()) {
                    if (host.map == target.map) {
                        int x = (int) ((target.pos.x + target.vel.x) - (host.pos.x + host.vel.x));
                        int y = (int) (target.pos.y - host.pos.y);
                        host.face(target.pos.x > host.pos.x ? 1 : -1, false);
                        float dist = (float) Math.abs(Math.sqrt(x * x + y * y) - host.getReach());
                        Actions action = Actions.Idle;
                        float lowest = 1000;
                        float current;
                        for (Actions act : host.moveset) {
                            current = act.getRange(host) - dist;
                            if (current >= 0) {
                                if (current < lowest || (current == lowest && Math.random() < .5f)) {
                                    lowest = current;
                                    action = act;
                                }
                            }
                        }
                        if (dist > action.getRange(host)) move(host);
                        else {
                            if (Math.random() > host.get("Stamina").getPercentage()) {
                                host.setState(Actions.Rest);
                                host.delay.time += 40;
                            } else if (host.setState(action) != 0) {
                                move(host);
                            }
                            if (host.state.getTags().contains(Tag.Channeling)) {
                                host.delay.set(60 + Global.r.nextInt(80));
                            }
                        }
                    } else {
                        host.brain.setTask(Travel);
                        host.brain.tasks.peek().load(Integer.parseInt(target.map.name));
                    }
                }
                return false;
            }
        };
    }};
    Task make(Unit host){
        return null;
    }
    Task get(Unit host){
        return make(host).setId(ordinal());
    }
}
class Task {
    int id, var;
    int alertness; //0:temporary, 1:normal, 2:alert, 3:active
    Task (int alertness){
        this.alertness = alertness;
    }
    void load(int p){
        var = p;
    }
    int getVar(){
        return var;
    }
    Task setId(int p){
        id = p;
        return this;
    }
    boolean update(Unit host){
        return true;
    }
    void blocked(Unit host, boolean fall){

    }
    void move(Unit host){
        if (host.setState(Actions.Sprint) != 0) host.setState(Actions.Walk);
    }
    public String toString(){
        return id+"&"+getVar();
    }
}