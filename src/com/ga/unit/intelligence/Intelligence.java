package com.ga.unit.intelligence;

import com.ga.game.Observer;
import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Timer;
import com.ga.unit.Man;
import com.ga.unit.Matter;
import com.ga.unit.Unit;
import com.ga.unit.Updater;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Moveset;

import java.util.ArrayList;

/**
 * Created by Ga on 8/26/2016.
 */
public abstract class Intelligence implements Observer {
    protected Unit host;
    protected Timer forget = new Timer(500, true);
    private ArrayList<Unit> seen;
    public Unit target;
    private Task task;
    protected Communication comm;
    private int alertness = 0;
    public int range = 150;
    public Intelligence(Unit man){
        host = man;
        task = new Idle(host);
        forget.time = 1;
        Updater.add(this);
    }
    protected void defaultTask(){
        setTaskEcho(new Wander(host));
    }
    public void update(){
        if (forget.update()){
            target = null;
            defaultTask();
        } else {
            if (host.state != Actions.Walk || !blocked(host.dir)) task.update();
        }
        if (alertness < 1) perceive();
    }
    protected void setTaskEcho(Task p){
        task = p;
        alertness = p.getAlertness();
        forget.reset();
    }
    protected Task getTask(){
        return task;
    }
    public void attacked(){
        if (target != null) {
            if (target.state != Actions.Dead) {
                host.say("@atk tgt");
            } else {
                host.say("@tgt x");
                target = null;
            }
        }
    }
    public void hit(){
        if (target == null) host.say("@agro");
        else host.say("@hit");
    }
    public void setState(Moveset state){
        if (state == Actions.Dead){
            Updater.remove(this);
            task = new Idle(host);
        } else {
            if (target != null) {
                P p = target.pos.getSubtract(host.pos);
                host.target = p;
                if (host instanceof Man) ((Man)host).rotateWeapon(p.getMult(host.dir,host.dir));
            }
        }
    }
    private boolean blocked(int dir){
        boolean blocked = false;
        boolean fall = true;
        P front = host.pos.getAdd((host.getSpeed()*3+48)*dir,0);
        int edge = Global.mapEdge(front);
        if (edge > 1 && Global.mapEdge(front) % 3 == 0) blocked = true;
        else {
            Line pp = new Line(host.pos, front);
            Line pp2 = new Line(front.getAdd(0, -40), front.getAdd(0, 40));
            for (Line i : Global.map.land) {
                if (i.isLand()) {
                    if (i.lineCross(pp) && (Math.abs(i.getSlope()) > Line.MAXSLOPE || !i.clockwise())) {
                        blocked = true;
                    }
                    if (i.lineCross(pp2)) {
                        fall = false;
                    }
                }
            }
        }
        if (blocked){
            task.blocked(false);
            return true;
        } else if (fall){
            task.blocked(true);
            return true;
        }
        return false;
    }
    public void hear(Matter u, String p){
        if (alertness < 1) {
            int dir = u.pos.x - host.pos.x > 0 ? 1 : -1;
            if (!host.faction.canAttack(u.faction)) {
                if (comm != null && u instanceof Man) {
                    if (u.isFacing(host.pos) && Math.abs(u.pos.x - host.pos.x) < 240) {
                        host.face(dir);
                        setTaskEcho(new Idle(host));
                        host.say(comm.hear(p));
                    }
                }
            } else {
                host.face(dir);
            }
        }
    }
    protected void perceive(){
        seen = Updater.perceive(host, host.pos.getAdd(host.dir<0?-range:range,0), range*2);
        for (Unit t : seen) {
            if (target != t) {
                perceived(t);
            } else if (target.state == Actions.Dead) forget.time = 0;
        }
    }
    protected void perceived(Matter p){
    }
    public void setComm(Communication comm) {
        this.comm = comm;
    }
}
