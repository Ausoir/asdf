package com.ga.unit;

import com.ga.game.Observer;
import com.ga.map.Updater;
import com.ga.unit.skill.Actions;

import java.util.ArrayList;

/**
 * Created by ga on 6/12/17. asd
 */
public abstract class AI implements Observer {
    int factoryId = -1;
    Unit host;
    Behavior task = Behavior.Wander, defaultTask = Behavior.Wander;
    public Unit target;
    private int targetId = 0;
    int taskProgress;
    AI(Unit host){
        this.host = host;
        Updater.addQueue.add(this);
    }
    public void update(){
        if (targetId != 0){
            target = Updater.unitList.get(targetId);
            targetId = 0;
        }
        perceive(160);
        task.update(host);
        taskProgress++;
    }
    public void setTaskEcho(Behavior p){
        taskProgress = 0;
        task = p;
        task.onSet(host);
    }
    void onAttack(){}
    public void onHit(){}
    void setState(Actions state){}
    boolean blocked(int dir){
        return false;
    }
    void hear(Prop u, String p){}
    boolean perceive(int d){
        ArrayList<Unit> seen = Updater.perceive(host, host.pos.getAdd(host.dir < 0 ? -d : d, 0), d * 2);
        for (Unit t : seen) {
            if (t != host && target != t && t.state != Actions.Dead) {
                return perceived(t);
            }
        }
        return false;
    }
    boolean perceived(Prop p){
        return false;
    }
    void load(String p){
        String[] data = p.split(",");
        setTaskEcho(Behavior.values()[Integer.parseInt(data[1])]);
        targetId = Integer.parseInt(data[2]);
    }
    public String toString(){
        int targetId = 0;
        if (target != null) targetId = target.controlId;
        return factoryId+","+task.ordinal()+","+targetId;
    }
}