package com.ga.map;

import com.ga.tool.P;
import com.ga.tool.Word;
import com.ga.unit.Beast;
import com.ga.unit.Faction;
import com.ga.unit.Man;
import com.ga.unit.Updater;
import com.ga.unit.item.Armor;
import com.ga.unit.item.ItemFactory;
import com.ga.unit.skill.Moveset;

/**
 * Created by Ga on 8/2/2016.
 */
public class BeastFactory {
    enum Type{
        Dog(Faction.Neutral, 10,30,10,10,3),
        Spider(Faction.Hostile, 10,30,10,10,3),
        Mantis(Faction.Evil, 100,40,10,10,3);
        int health,speed,stamina,energy,proficiency;
        Faction faction;
        Type(Faction faction, int hp, int spd, int arm, int leg, int prof){
            this.faction = faction;
            this.health = hp;
            this.speed = spd;
            this.stamina = arm;
            this.energy = leg;
            this.proficiency = prof;
        }
        Beast get(String p){
            Beast r;
            switch (this){
                case Dog:
                    r = new Beast(p);
                    r.shadow.offset.set(15,0);
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
            String stat = "Stamina:"+stamina+"&Speed:"+speed+"&Energy:"+energy+"&Proficiency:"+proficiency+"&Health:"+health;
            String t = name()+"@"+seed+"@"+p+"@"+Updater.getId()+"@"+stat+"@"+faction.name();
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
