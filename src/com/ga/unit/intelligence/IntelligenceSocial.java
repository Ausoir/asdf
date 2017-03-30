package com.ga.unit.intelligence;

import com.ga.unit.Prop;
import com.ga.unit.Unit;
import com.ga.unit.skill.Actions;

/**
 * Created by Ga on 7/30/2016.
 */
public class IntelligenceSocial extends Intelligence {
    protected Unit leader;
    private int charisma;
    private int travel = -1; //top left:1, top right:2, bottom left:3, bottom right:4
    public IntelligenceSocial(Unit man){
        super(man);
        charisma = host.rand.nextInt(7);
        leader = null;
    }
    protected void perceived(Prop p){
        if (p instanceof Unit) {
            Unit unit = (Unit) p;
            if (host.faction.canAttack(p.faction)) {
                if (unit.state != Actions.Dead) {
                    target = (Unit) p;
                    host.say("@c tgt");
                    setTaskEcho(new Hunt(host));
                }
            } else if (host.faction == p.faction && unit.brain instanceof IntelligenceSocial){
                if (leader == null && ((IntelligenceSocial)unit.brain).charisma > charisma) {
                    ((IntelligenceSocial)unit.brain).charisma--;
                    leader = unit;
                    setTaskEcho(new Follow(host, leader));
                }
            }
        }
    }
    @Override
    protected void defaultTask(){
        if (leader == null || leader == host){
            if (travel == -1) super.defaultTask();
            else setTaskEcho(new Travel(host, travel));
        } else if (leader.state == Actions.Dead){
            leader = null;
        } else {
            setTaskEcho(new Follow(host, leader));
        }
    }
    public void update(){
        super.update();
        if (host.get("Health").getPercentage() < .15f){
            host.say("@flee");
            setTaskEcho(new Flee(host, target));
        } else if (getTask() instanceof Flee && target != null) setTaskEcho(new Hunt(host));
    }
}
