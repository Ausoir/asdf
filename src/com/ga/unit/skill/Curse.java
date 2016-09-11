package com.ga.unit.skill;

import com.ga.map.Global;
import com.ga.map.ManFactory;
import com.ga.tool.P;
import com.ga.unit.Man;

/**
 * Created by Ga on 9/4/2016.
 */
public enum Curse {
    Death(new Skill(){
        @Override
        public boolean perform() {
            host.stats.addEffect(Buffs.Poison,level);
            return true;
        }
    }),
    Shadowcurse(new Skill(){
        @Override
        public boolean perform() {
            Man t = ManFactory.produce("Phantom",new P(host.pos.x, host.pos.y - 300),0);
            Skills.Shadowtwin.getEvent().start(t, level);
            Skills.Shadowtwin.getEvent().perform();
            t.remove();
            return true;
        }
    }),
    Elemental(new Skill(){
        @Override
        public boolean perform() {
            Man d = new Man("Player",new P(host.pos));
            d.target = P.getZero();
            Skills.Meteor.getEvent().start(d, level);
            Skills.Blizzard.getEvent().start(d, level);
            for (int i=0;i<level*4;i++) Skills.Meteor.getEvent().perform();
            for (int i=0;i<level*4;i++) Skills.Blizzard.getEvent().perform();
            d.remove();
            return true;
        }
    });
    private Skill effect;
    Curse(Skill p){
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
