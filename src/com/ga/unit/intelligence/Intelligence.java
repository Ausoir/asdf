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
    private ArrayList<Matter> seen;
    public Unit target;
    private Task task;
    protected Communication comm;
    private int[] base;
    private int alertness = 0;
    public int range = 150;
    public Intelligence(Unit man){
        host = man;
        base = new int[]{host.rand.nextInt(Global.map.getWidth()),
            host.rand.nextInt(Global.map.getWidth()),
            host.rand.nextInt(Global.map.getWidth())};
        task = new Idle(host);
        forget.time = 1;
        Updater.add(this);
    }
    protected void defaultTask(){
        setTaskEcho(new Wander(host, base));
    }
    public void update(){
        if (forget.update()){
            target = null;
            defaultTask();
        } else {
            task.update();
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
    public void hit(Matter hitter){
        if (hitter != null) {
            if (target != hitter) host.say("@agro");
            if (hitter instanceof Unit) target = (Unit) hitter;
            else {
                provoked(hitter.pos.x - host.pos.x > 0 ? 1 : -1);
                host.say("@hit");
            }
        }
    }
    public void setState(Moveset state){
        if (target != null) {
            P p = target.pos.getSubtract(host.pos);
            host.target = p;
            if (host instanceof Man) ((Man)host).rotateWeapon(p.getMult(host.dir,host.dir));
        }
        if (state == Actions.Dead){
            Updater.remove(this);
            task = new Idle(host);
        } else if (state == Actions.Walk && blocked(host.dir)){
            host.setState(Actions.Idle);
            task.blocked();
        }
    }
    protected boolean blocked(int dir){
        Line pp = new Line(host.pos,host.pos.getAdd(120*dir,0));
        for (Line i : Global.map.land){
            if (i.lineCross(pp) && Math.abs(i.getSlope()) > 5){
                return true;
            }
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
                provoked(dir);
            }
        }
    }
    protected void perceive(){
        seen = Updater.perceive(host, host.pos.getAdd(host.dir<0?-range:range,0), range*2);
        for (Matter t : seen) {
            if (target != t) {
                perceived(t);
            } else if (target.state == Actions.Dead) forget.time = 0;
        }
    }
    protected void provoked(int dir){
        host.face(dir);
    }
    protected void perceived(Matter p){
    }
    public void setComm(Communication comm) {
        this.comm = comm;
    }
}
