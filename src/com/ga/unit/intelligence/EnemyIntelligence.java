package com.ga.unit.intelligence;

import com.ga.map.Global;
import com.ga.unit.Actions;
import com.ga.unit.Man;
import com.ga.unit.Unit;
import com.ga.unit.skill.Skills;

import java.util.ArrayList;

/**
 * Created by Ga on 7/30/2016.
 */
public class EnemyIntelligence extends Intelligence {
    enum Moves{
        STANDING(new Actions[]{Actions.Kick, Actions.Weapon, Actions.Swing}),
        OVERLAP(new Actions[]{Actions.Weapon, Actions.Swing}),
        CLOSE(new Actions[]{Actions.Kick}),
        CHASING(new Actions[]{Actions.Pierce});
        private Actions[] actions;
        Moves(Actions[] actions){
            this.actions = actions;
        }
        Actions get(){
            int r = Global.r.nextInt(actions.length);
            return actions[r];
        }
    }
    public ArrayList<Skills> skillset = new ArrayList<Skills>(){{
        add(Skills.Adrenaline);
    }};
    public EnemyIntelligence(Man man){
        super(man);
    }
    @Override
    public void hear(Unit u, String p){
        if (target == null){
            task = new Search(host,u.pos);
            host.say("@hear");
            forget.set(10);
        }
    }
    protected void perceived(Unit p){
        if (p.faction != host.faction && p.faction != -1) {
            if (p instanceof Man) {
                if (((Man)p).state != Actions.Dead) {
                    target = (Man) p;
                    forget.set(30);
                    host.say("@c tgt");
                    task = new Hunt(host, target);
                }
            }
        }
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
        if (host.get("health").getPercentage() < .1f){
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
    public void setMove(Moves move){
        host.setState(move.get());
    }
}
