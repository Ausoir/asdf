package com.ga.unit.skill;

import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.*;
import com.ga.unit.item.Projectile;

import java.util.ArrayList;

/**
 * Created by Ga on 9/1/2016.
 */
public enum Buffs {
    Haste(null, hostEffect(), 100, "Speed",1,"Speed"),
    Rejuvenate(null, hostEffect(), 100, "Stamina",.5f,"Stamina"),
    Empower(null, weaponEffect(1), 80, "Proficiency",10),
    Cleave(null, weaponEffect(2), 300),
    Evade(null, new Spell(){
        @Override
        public void perform(Unit host, int level){
            Particle.smoke(new P(host.pos));
        }
    }, 100),
    Freeze(sound("freeze"), new Spell(){
        @Override
        public void perform(Unit host, int level){
            Particle.frozen(host.pos);
        }
    }, 50, "Speed",-1,"Speed"),
    Slow(null, new Spell(){
        public void perform(Unit host, int level){
            Particle.implode("Glare", host.pos.getAdd(0,-25), new P(), 1, 10, 1, false, 6);
        }
    }, 50, "Speed", -.5f, "Speed"),
    Exhaust(null, new Spell(){
        @Override
        public void perform(Unit host, int level){
            host.get("Stamina").add(-level);
        }
    }, 100),
    Poison(null, new Spell(){
        private int tick = 1;
        @Override
        public void perform(Unit host, int level){
            tick--;
            if (tick == 0) {
                host.get("Health").add(-level);
                tick = Math.max(1,30-(level*2));
                Particle.explode("Poison", host.getRandomPoint(), new P(host.dir*15,-15), 10, 3, 10, true, 16);
            }
        }
    }, 100),
    Glacialwalk(null, new Spell(){
        private int tick = 1;
        @Override
        public void perform(Unit host, final int level){
            tick--;
            if (tick == 0) {
                tick = 2;
                final Projectile p = new Projectile("Ice", host.pos.getAdd(0, -5), new P(), 0) {
                    @Override
                    protected void onAttack(Unit p) {
                        p.stats.addEffect(Buffs.Freeze,level);
                        Particle.explode("Ice", pos, new P(0,-20), 30, 10, 5, true, 10);
                        remove();
                    }
                    @Override
                    public void stuck() {
                        vel.set(0, 0);
                    }
                    @Override
                    public void update() {
                        super.update();
                        Particle.explode("Ice", pos, new P(0, -5), 1, 3, 2, false, 6);
                    }
                    @Override
                    public int getCollisionSize(){
                        return 30;
                    }
                };
                p.faction = host.faction;
                p.gravity = true;
                p.delay.set(50+(level*150));
                p.delay.performer = new Performer(){
                    public void perform(){
                        p.remove();
                    }
                };
            }
        }
    },150),
    Flamestride(null, new Spell(){
        private int tick = 0;
        @Override
        public void perform(Unit host, int level){
            tick++;
            if (tick % 2 == 0) {
                if (tick % 6 == 0) Sound.play("fire");
                final Projectile p = new Projectile("Flamecluster", host.pos.getAdd(0, -5), new P(), 0) {
                    @Override
                    protected void onAttack(Unit p) {
                        super.onAttack(p);
                        Particle.explode("Flame", pos, new P(), 30, 20, 0, false, 2);
                        remove();
                    }
                    @Override
                    public void stuck() {
                        vel.set(0, 0);
                    }
                    @Override
                    public void update() {
                        super.update();
                        Particle.explode("Flame", pos, new P(0, -5), 1, 12, 3, false, 3);
                    }
                    @Override
                    public int getCollisionSize(){
                        return 30;
                    }
                };
                p.faction = host.faction;
                p.damage = 10+host.get("Proficiency").get();
                p.damageType = DamageType.Burn;
                p.gravity = true;
                p.delay.set(level*20);
                p.delay.performer = new Performer(){
                    @Override
                    public void perform(){
                        p.remove();
                    }
                };
            }
        }
    },500),
    Stun(sound("stun"), new Spell(){
        @Override
        public void perform(Unit host, int level){
            host.delay.set(150);
            host.clip.setSpeed(999);
            Particle.implode("Glare", host.pos.getAdd(0,-25), new P(), 1, 10, 1, false, 6);
        }
    }, 150);
    public int time;
    public float buff;
    public String stat;
    public String buffRel;
    public Spell start, effect;
    private static Spell weaponEffect(final int type){
        return new Spell(){
            @Override
            public void perform(Unit host, int level){
                Man t = (Man) host;
                if (type == 1) Particle.dripFire(t.weapon.getPoint());
                else if (type == 2) Particle.dripHoly(t.weapon.getPoint());
            }
        };
    }
    private static Spell hostEffect(){
        return new Spell(){
            @Override
            public void perform(Unit host, int level){
                if (Global.r.nextInt(5) == 0) {
                    Man t = (Man) host;
                    Particle.cleanse(t.pos);
                }
            }
        };
    }
    private static Spell sound(final String p){
        return new Spell(){
            @Override
            public void perform(Unit host, int level){
                Sound.play(p);
            }
        };
    }
    Buffs(Spell start, Spell effect, int time){
        this.start = start;
        this.effect = effect;
        this.time = time;
    }
    Buffs(Spell start, Spell effect, int time, String stat, float buff){
        this(start, effect, time);
        this.stat = stat;
        this.buff = buff;
    }
    Buffs(Spell start, Spell effect, int time, String stat, float buff, String buffRel){
        this(start, effect, time, stat, buff);
        this.buffRel = buffRel;
    }
    public static Buffs getByName(String p) {
        for (Buffs s : values()) {
            if (s.name().equals(p)){
                return s;
            }
        }
        return null;
    }
}
