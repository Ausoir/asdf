package com.ga.unit.skill;

import com.ga.map.Global;
import com.ga.map.Updater;
import com.ga.unit.Faction;
import com.ga.unit.Man;
import com.ga.unit.Stock;
import com.ga.unit.Unit;

/**
 * Created by Ga on 9/4/2016. asd
 */
public enum Curse {
    Death {
        public void start(Man host, int level) {
            host.buff(Buffs.Poison,level);
        }
    },
    Shadowcurse{
        public void start(Man host, int level) {
            for (int i=0;i<level;i++) Stock.getByName("Phantom",-1).pos.set(host.pos.x, host.pos.y - 300);
        }
    },
    Ensnare{
        public void start(Man host, int level) {
            int r = Global.r.nextInt(3);
            if (r == 0) host.buff(Buffs.Slow, level);
            else if (r == 1) host.buff(Buffs.Stun, level);
            else if (r == 2) host.buff(Buffs.Freeze, level);
        }
    },
    Alert{
    public void start(Man host, int level) {
        for (Unit i : Updater.collideUnit(host.pos,2000*level, Faction.Destructive)){
                if (i.brain != null) {
                    i.brain.target = host;
                    i.brain.attacked();
                }
            }
        }
    };
    public static void get(Man host, int severity){
        Curse t = values()[Global.r.nextInt(values().length)];
        t.start(host, severity);
    }
    public void start(Man host, int level){
    }
}
