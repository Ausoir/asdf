package com.ga.unit.skill;

import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.unit.*;

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
            for (int i=0;i<level;i++) Stock.getByName("Phantom",-1).pos.set(host.pos.x, host.pos.y - 300);
        }
    }),
    Ensnare(new Spell(){
        public void perform(Unit host, int level) {
            int r = Global.r.nextInt(3);
            if (r == 0) host.stats.addEffect(Buffs.Slow, level);
            else if (r == 1) host.stats.addEffect(Buffs.Stun, level);
            else if (r == 2) host.stats.addEffect(Buffs.Freeze, level);
        }
    }),
    Alert(new Spell(){
        public void perform(Unit host, int level) {
            for (Unit i : Updater.collideUnit(host.pos,2000*level, Faction.Destructive)){
                if (i.brain != null) {
                    i.brain.target = host;
                    i.brain.attacked();
                }
            }
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
