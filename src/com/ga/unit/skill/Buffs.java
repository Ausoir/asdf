package com.ga.unit.skill;

import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.*;
import com.ga.unit.item.Projectile;

/**
 * Created by Ga on 9/1/2016.
 */
public enum Buffs {
    Haste(hostEffect(), 50, "Speed",1,"Speed"), Rejuvenate(hostEffect(), 100, "Stamina",.5f,"Stamina"), Empower(weaponEffect(1), 80, "Proficiency",10), Cleave(weaponEffect(2), 300),
    Evade(new Skill(){
        @Override
        public boolean perform(){
            Particle.smoke(new P(host.pos));
            return false;
        }
    }, 100),
    Freeze(new Skill(){
        @Override
        public boolean perform(){
            Particle.frozen(host.pos);
            return false;
        }
    }, 50, "Speed",-1,"Speed"),
    Poison(new Skill(){
        private int tick = 1;
        @Override
        public boolean perform(){
            tick--;
            if (tick == 0) {
                host.get("Health").add(-1);
                tick = 30-level;
                if (host.get("Health").get() == 1) return true;
            }
            return false;
        }
    },100),
    Glacialwalk(new Skill(){
        private int tick = 1;
        @Override
        public boolean perform(){
            tick--;
            if (tick == 0) {
                tick = 2;
                final Projectile p = new Projectile("effect/Ice", host.pos.getAdd(0, -5), P.getZero(), 0) {
                    @Override
                    protected void onAttack(Matter p) {
                        if (p instanceof Unit){
                            ((Unit) p).stats.addEffect(Buffs.Freeze,level);
                        }
                        Particle.explode("Ice", pos, new P(0,-20), 30, 10, 5, true, 10);
                        remove();
                    }
                    @Override
                    public void update() {
                        super.update();
                        Particle.explode("Ice", pos, new P(0, -5), 1, 3, 2, false, 6);
                        Matter t = Updater.collide(this, 500);
                        if (t!=null) onAttack(t);
                    }
                };
                p.faction = host.faction;
                p.gravity = true;
                p.delay.set(50+(level*150));
                p.delay.performer = new Performer(){
                    @Override
                    public boolean perform(){
                        p.remove();
                        return true;
                    }
                };
            }
            return false;
        }
    },50),
    Flamestride(new Skill(){
        private int tick = 1;
        @Override
        public boolean perform(){
            tick--;
            if (tick == 0) {
                tick = 1;
                final Projectile p = new Projectile("effect/Flamecluster", host.pos.getAdd(0, -5), P.getZero(), 0) {
                    @Override
                    protected void onAttack(Matter p) {
                        Particle.explode("Flame", pos, P.getZero(), 30, 20, 0, false, 2);
                        remove();
                    }
                    @Override
                    public void update() {
                        super.update();
                        Particle.explode("Flame", pos, new P(0, -5), 1, 12, 3, false, 3);
                        Matter t = Updater.collide(this, 500);
                        if (t!=null) onAttack(t);
                    }
                };
                p.faction = host.faction;
                p.damage = 10+host.get("Proficiency").get();
                p.gravity = true;
                p.delay.set(level*20);
                p.delay.performer = new Performer(){
                    @Override
                    public boolean perform(){
                    p.remove();
                    return true;
                    }
                };
            }
            return false;
        }
    },80);
    public int time;
    public float buff;
    public String stat;
    public String buffRel;
    public Skill effect;
    private static Skill weaponEffect(final int type){
        return new Skill(){
            @Override
            public boolean perform(){
                Man t = (Man) host;
                if (type == 1) Particle.dripFire(t.getWeapon().getPoint());
                else if (type == 2) Particle.dripHoly(t.getWeapon().getPoint());
                return false;
            }
        };
    }
    private static Skill hostEffect(){
        return new Skill(){
            @Override
            public boolean perform(){
                if (Global.r.nextInt(5) == 0) {
                    Man t = (Man) host;
                    Particle.cleanse(t.pos);
                }
                return false;
            }
        };
    }
    Buffs(Skill effect, int time){
        this.effect = effect;
        this.time = time;
    }
    Buffs(Skill effect, int time, String stat, float buff){
        this(effect, time);
        this.stat = stat;
        this.buff = buff;
    }
    Buffs(Skill effect, int time, String stat, float buff, String buffRel){
        this(effect, time, stat, buff);
        this.buffRel = buffRel;
    }
}
