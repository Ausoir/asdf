package com.ga.unit.intelligence;

import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.tool.Timer;
import com.ga.tool.Word;
import com.ga.unit.Actions;
import com.ga.unit.Unit;
import com.ga.unit.Matter;
import com.ga.unit.Updater;

import java.util.ArrayList;

/**
 * Created by Ga on 8/26/2016.
 */
public class Intelligence {
    protected Unit host;
    protected Timer think = new Timer(1, true);
    protected Timer forget = new Timer(100, true);
    private ArrayList<Matter> seen;
    public Unit target;
    public Task task;
    public Intelligence(Unit man){
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
            if (target.state != Actions.Dead) {
                host.say("@atk tgt");
            } else {
                host.say("@tgt x");
                target = null;
            }
        }
    }
    public void hit(Matter hitter){
        if (target != hitter) host.say("@agro");
        if (hitter != null && hitter instanceof Unit) target = (Unit) hitter;
        else{
            provoked(hitter.pos.x-host.pos.x>0?1:-1);
            host.say("@hit");
        }
    }
    public void setState(Actions state){
        if (target != null) {
            P p = target.pos.getSubtract(host.pos);
            host.target = p;
            host.rotateWeapon(p.getMult(host.dir,host.dir));
        }
    }
    public void hear(Matter u, String p){
        if (host.faction.canAttack(u.faction)){
            if (Word.Greeting.is(p)){
                host.say("@greet");
            }
        } else{
            provoked(u.pos.x-host.pos.x>0?1:-1);
        }
    }
    protected void perceive(){
        seen = Updater.perceive(host, host.pos.getAdd(host.dir<0?-150:150,0), 300);
        for (Matter t : seen) {
            if (target != t) {
                perceived(t);
            }
        }
    }
    protected void provoked(int dir){
        host.face(dir);
    }
    protected void perceived(Matter p){
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
