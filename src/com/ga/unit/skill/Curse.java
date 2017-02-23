package com.ga.unit.skill;

import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.unit.Man;
import com.ga.unit.Unit;
import com.ga.unit.factory.Stock;

/**
 * Created by Ga on 9/4/2016.
 */
public enum Curse {
    Death(new Spell(){
        public void perform(Unit host, int level) {
            host.stats.addEffect(Buffs.Poison,level);
        }
    }),
    Shadowcurse(new Spell(){
        public void perform(Unit host, int level) {
            Man t = (Man) Stock.getByName("Phantom",-1);
            t.pos = new P(host.pos.x, host.pos.y - 300);
            Spells.Shadowtwin.getEvent().perform(t, level);
            t.remove();
        }
    }),
    Elemental(new Spell(){
        public void perform(Unit host, int level) {
            Man d = new Man(host.toString());
            d.target = new P();
            for (int i=0;i<level*4;i++) Spells.Meteor.getEvent().perform(d, level);
            for (int i=0;i<level*4;i++) Spells.Blizzard.getEvent().perform(d, level);
            d.remove();
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
        effect.perform(host, level);
    }
}
