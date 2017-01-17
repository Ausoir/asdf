package com.ga.map;

import com.ga.tool.P;
import com.ga.tool.Word;
import com.ga.unit.Beast;
import com.ga.unit.Faction;
import com.ga.unit.Updater;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Moveset;
import com.ga.unit.skill.Skills;

import java.util.HashMap;

/**
 * Created by Ga on 8/2/2016.
 */
public class BeastFactory {
    enum Type{
        Dog(Faction.Evil, 10,30,10,3,"-5,50/Kick&Pierce"),
        Spider(Faction.Evil, 10,30,10,3,"-5/Kick"),
        Mantis(Faction.Evil, 100,40,10,3,"-5/Kick");
        int health,speed,stamina,proficiency;
        Faction faction;
        String rangeMove;
        Type(Faction faction, int hp, int spd, int arm, int prof, String rangeMove){
            this.faction = faction;
            this.health = hp;
            this.speed = spd;
            this.stamina = arm;
            this.proficiency = prof;
            this.rangeMove = rangeMove;
        }
        Beast get(String p){
            Beast r;
            switch (this){
                case Dog:
                    r = new Beast(p){
                        protected void actionSet(Moveset state, int level){
                            super.actionSet(state, level);
                            if (state == Skills.Pierce){
                                delay.time = 30;
                                vel.x += getSpeed()*3*dir;
                                vel.y -= 20;
                            }
                        }
                    };
                    break;
                case Mantis:
                    r = new Beast(p);
                    r.clip.organs[1].pt.y -= r.clip.organs[0].ptd.y;
                    break;
                default:
                    r = new Beast(p);
            }
            return r;
        }
        Beast get(int seed, P p){
            String stat = "Stamina:"+stamina+"&Speed:"+speed+"&Proficiency:"+proficiency+"&Health:"+health;
            String t = name()+"@"+seed+"@"+p+"@"+Updater.getId()+"@"+stat+"@"+faction.name()+"@"+rangeMove;
            return get(t);
        }
    }
    public static Beast produce(String p){
        return Type.valueOf(p.split("@")[0]).get(p);
    }
    public static Beast produce(String name, P pt, int seed){
        Beast result;
        String t = Word.wordify(name);
        try {
            result = Type.valueOf(t).get(seed, pt);
        } catch (Exception e) {
            Global.p("Can't find beast ["+t+"]");
            result = Type.Dog.get(seed, pt);
        }
        return result;
    }
}
