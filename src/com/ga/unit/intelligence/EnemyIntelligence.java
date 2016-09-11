package com.ga.unit.intelligence;

import com.ga.map.Global;
import com.ga.unit.Actions;
import com.ga.unit.Unit;
import com.ga.unit.Matter;
import com.ga.unit.skill.Skills;

import java.util.ArrayList;

/**
 * Created by Ga on 7/30/2016.
 */
public class EnemyIntelligence extends Intelligence {
    public ArrayList<Skills> skillset = new ArrayList<Skills>(){{
        add(Skills.Haste);
    }};
    public EnemyIntelligence(Unit man){
        super(man);
    }
    @Override
    public void hear(Matter u, String p){
        if (target == null){
            task = new Search(host,u.pos);
            host.say("@hear");
            forget.set(10);
        }
    }
    protected void perceived(Matter p){
        if (host.faction.canAttack(p.faction)) {
            if (p instanceof Unit) {
                if (((Unit)p).state != Actions.Dead) {
                    target = (Unit) p;
                    forget.set(30);
                    host.say("@c tgt");
                    task = new Hunt(host, target);
                }
            }
        }
    }
    @Override
    protected void provoked(int dir){
        super.provoked(dir);
        task = new Search(host,host.pos.getAdd(dir*1200,0));
        forget.set(30);
    }
    public void useSkill(String p){
        for (Skills i : skillset){
            if (i.getClass().getSimpleName().equals(p)){
                host.setState(i);
            }
        }
    }
    @Override
    protected void think(){
        super.think();
        if (host.get("Health").getPercentage() < .1f){
            host.say("@flee");
            task = new Flee(host, target);
        }
        else if (task instanceof Flee && target != null) task = new Hunt(host, target);
    }
    public void useSkill(){
        if (skillset.size() > 0) {
            int r = Global.r.nextInt(skillset.size());
            host.setState(skillset.get(r));
        }
    }
}
