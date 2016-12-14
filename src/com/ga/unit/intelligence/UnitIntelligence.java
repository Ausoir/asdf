package com.ga.unit.intelligence;

import com.ga.map.Global;
import com.ga.unit.skill.Actions;
import com.ga.unit.Unit;
import com.ga.unit.Matter;
import com.ga.unit.skill.Spells;

import java.util.ArrayList;

/**
 * Created by Ga on 7/30/2016.
 */
public class UnitIntelligence extends Intelligence {
    public ArrayList<Spells> skillset = new ArrayList<Spells>(){{
        add(Spells.Haste);
    }};
    private Unit leader;
    public UnitIntelligence(Unit man){
        super(man);
        leader = host.rand.nextInt(3) == 0 ? host : null;
    }
    protected void perceived(Matter p){
        if (p instanceof Unit) {
            if (host.faction.canAttack(p.faction)) {
                if (((Unit) p).state != Actions.Dead) {
                    target = (Unit) p;
                    host.say("@c tgt");
                    setTask(new Hunt(host, target));
                }
            } else if (host.faction == p.faction){
                if (leader == null) {
                    leader = (Unit) p;
                }
            }
        }
    }
    @Override
    protected void defaultTask(){
        if (leader == null || leader == host) super.defaultTask();
        else if (leader.state == Actions.Dead){
            leader = null;
        } else {
            target = leader;
            setTask(new Follow(host, target));
        }
    }
    @Override
    protected void provoked(int dir){
        super.provoked(dir);
        setTask(new Search(host,host.pos.getAdd(dir*1200,0)));
    }
    public void useSkill(String p){
        for (Spells i : skillset){
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
            setTask(new Flee(host, target));
        }
        else if (getTask() instanceof Flee && target != null) setTask(new Hunt(host, target));
    }
    public void useSkill(){
        if (skillset.size() > 0) {
            int r = Global.r.nextInt(skillset.size());
            host.setState(skillset.get(r));
        }
    }
}
