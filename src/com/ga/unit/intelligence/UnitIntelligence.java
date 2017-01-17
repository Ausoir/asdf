package com.ga.unit.intelligence;

import com.ga.unit.Matter;
import com.ga.unit.Unit;
import com.ga.unit.skill.Actions;

/**
 * Created by Ga on 7/30/2016.
 */
public class UnitIntelligence extends Intelligence {
    private Unit leader;
    private int charisma;
    public UnitIntelligence(Unit man){
        super(man);
        charisma = host.rand.nextInt(7);
        leader = host;
    }
    protected void perceived(Matter p){
        if (p instanceof Unit) {
            Unit unit = (Unit) p;
            if (host.faction.canAttack(p.faction)) {
                if (unit.state != Actions.Dead) {
                    target = (Unit) p;
                    host.say("@c tgt");
                    setTaskEcho(new Hunt(host));
                }
            } else if (host.faction == p.faction && host.name.equals(unit.name)){
                if (leader == null && ((UnitIntelligence)unit.brain).charisma > charisma) {
                    ((UnitIntelligence)unit.brain).charisma--;
                    leader = unit;
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
            setTaskEcho(new Follow(host, target));
        }
    }
    @Override
    protected void think(){
        super.think();
        if (host.get("Health").getPercentage() < .15f){
            host.say("@flee");
            setTaskEcho(new Flee(host, target));
        } else if (getTask() instanceof Flee && target != null) setTaskEcho(new Hunt(host));
    }
}
