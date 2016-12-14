package com.ga.unit.skill;

import com.ga.map.Global;
import com.ga.map.ManFactory;
import com.ga.tool.P;
import com.ga.unit.Man;

/**
 * Created by Ga on 9/4/2016.
 */
public enum Curse {
    Death(new Spell(){
        @Override
        public boolean perform() {
            host.stats.addEffect(Buffs.Poison,level);
            return true;
        }
    }),
    Shadowcurse(new Spell(){
        @Override
        public boolean perform() {
            Man t = ManFactory.produce("Phantom",new P(host.pos.x, host.pos.y - 300),0);
            Spells.Shadowtwin.getEvent().start(t, level);
            Spells.Shadowtwin.getEvent().perform();
            t.remove();
            return true;
        }
    }),
    Elemental(new Spell(){
        @Override
        public boolean perform() {
            Man d = new Man(host.toString());
            d.target = P.getZero();
            Spells.Meteor.getEvent().start(d, level);
            Spells.Blizzard.getEvent().start(d, level);
            for (int i=0;i<level*4;i++) Spells.Meteor.getEvent().perform();
            for (int i=0;i<level*4;i++) Spells.Blizzard.getEvent().perform();
            d.remove();
            return true;
        }
    });
    private Spell effect;
    Curse(Spell p){
        effect = p;
    }
    public static void get(Man host, int severity){
        Curse t = values()[Global.r.nextInt(values().length)];
        t.start(host, severity);
    }
    public void start(Man host, int level){
        effect.start(host, level);
        effect.perform();
    }
}
