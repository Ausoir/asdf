package com.ga.unit.intelligence;

import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.tool.Timer;
import com.ga.unit.Actions;
import com.ga.unit.Man;
import com.ga.unit.Unit;
import com.ga.unit.Updater;

import java.util.ArrayList;

/**
 * Created by Ga on 8/26/2016.
 */
public class Intelligence {
    protected Man host;
    protected Timer think = new Timer(1, true);
    protected Timer forget = new Timer(500, true);
    private ArrayList<Unit> seen;
    public Man target;
    public Task task;
    public Intelligence(Man man){
        host = man;
        task = new Wander(man);
    }
    public void update(){
        if (think.update()){
            think();
        } else {
            task.update();
        }
    }
    public void attacked(){
        if (target != null) {
            if (target.state != Actions.DEAD) {
                host.say("@atk tgt");
            } else {
                host.say("@tgt x");
                target = null;
            }
        }
    }
    public void hit(Man hitter){
        if (target != hitter) host.say("@agro");
        if (hitter != null) target = hitter;
        else host.dir *= -1;
    }
    public void setState(Actions state){
        if (target != null) {
            P tgt = target.pos;
            P p = new P(tgt.x-host.pos.x,tgt.y-host.pos.y);
            host.target = p;
            host.getWeapon().rotate(p.getMult(host.dir,host.dir));
        }
    }
    public void hear(P pos, String p){
    }
    protected void perceive(){
        seen = Updater.perceive(host, host.pos.getAdd(host.dir<0?-150:150,0), 300);
        for (Unit t : seen) {
            if (target != t) {
                perceived(t);
            }
        }
    }
    protected void perceived(Unit p){
    }
    protected void think(){
        if (forget.update()){
            target = null;
            if (!(task instanceof Wander)) task = new Wander(host);
        }
        think.set(Global.r.nextInt(10)+2);
        perceive();
    }
}
