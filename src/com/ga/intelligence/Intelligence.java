package com.ga.intelligence;

import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.tool.Timer;
import com.ga.unit.*;
import com.ga.unit.skill.Adrenaline;
import com.ga.unit.skill.Skill;

import java.util.ArrayList;

/**
 * Created by Ga on 7/30/2016.
 */
public class Intelligence {
    protected Man host;
    private Timer think = new Timer(1, true);
    private Timer forget = new Timer(-1, true);
    private ArrayList<Unit> seen;
    public Man target;
    public Task task;
    public ArrayList<Actions> moveset = new ArrayList<Actions>(){{
        add(Actions.ATTACK);
        add(Actions.ATTACK);
        add(Actions.KICK);
        add(Actions.SWING);
        add(Actions.WHIRLWIND);
    }};
    public ArrayList<Skill> skillset = new ArrayList<Skill>(){{
        add(new Adrenaline());
    }};
    public Intelligence(Man man){
        host = man;
        task = new Wander(man);
    }
    public void update(){
        if (think.update()){
            think();
        } else {
            if (target == null) task = new Wander(host);
            task.update();
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
            host.face(tgt);
            P p = new P(tgt.x-host.pos.x,tgt.y-host.pos.y);
            float rot = (float) Math.atan2(p.y,p.x);
            host.getWeapon().rotate(p.getMult(host.dir,host.dir));
            switch (state) {
                case ATTACK:
                    host.clip.setPunch(rot, host.dir);
                    break;
                case KICK:
                    host.clip.setKick(rot, host.dir);
                    break;
            }
        }
    }
    private void perceive(){
        seen = Updater.perceive(host, host.pos.getAdd(host.dir<0?-150:150,0), 300);
        for (Unit t : seen) {
            if (target != t) {
                if (t.faction != host.faction) {
                    if (t instanceof Man) {
                        target = (Man)t;
                        if (target.state != Actions.DEAD) {
                            forget.set(1000);
                            host.say("@c tgt");
                            task = new Hunt(host, target);
                            break;
                        }
                    }
                }
            }
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
    public void useSkill(String p){
        for (Skill i : skillset){
            if (i.getClass().getSimpleName().equals(p)){
                host.skill = i;
                host.setState(Actions.SKILL);
            }
        }
    }
    private void think(){
        if (forget.update()) target = null;
        perceive();
        if (host.get("health").getPercentage() < .1f){
            host.say("@flee");
            task = new Flee(host, target);
        }
        else if (task instanceof Flee && target != null) task = new Hunt(host, target);
        think.set(Global.r.nextInt(10)+2);
    }
    public void useSkill(){
        if (skillset.size() > 0) {
            int r = Global.r.nextInt(skillset.size());
            host.skill = skillset.get(r);
            host.setState(Actions.SKILL);
        }
    }
    public void useMove(){
        int r = Global.r.nextInt(moveset.size());
        host.setState(moveset.get(r));
    }
}
