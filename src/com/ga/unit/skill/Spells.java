package com.ga.unit.skill;

import com.ga.game.Dialog;
import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.Shader;
import com.ga.map.Terrain;
import com.ga.tool.EventTimer;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.*;
import com.ga.unit.Stock;
import com.ga.unit.intelligence.IntelligenceSocial;
import com.ga.unit.item.Item;
import com.ga.unit.item.Projectile;

import java.util.ArrayList;

/**
 * Created by Ga on 8/1/2016.
 */
public enum Spells implements Moveset {
    Innersword(Actions.Cast, 6,3,14,false,Dialog.get("mvsDscInnersword"),new Spell(){
        public void perform(final Unit host, int level){
            Man man = (Man)host;
            if (man.wieldingNone()) {
                Sound.play("shadowstep");
                final Item t = (Item) Stock.getByName("Ethersword", -1);
                man.setWeapon(t);
                int duration = 1;
                if (level == 2) duration = 4;
                if (level == 3) duration = 16;
                if (level > 3) duration = 256;
                t.uses.setTotal(duration);
                Global.timers.add(new EventTimer(duration,false,new Performer(){
                    public void perform(){
                        t.remove();
                    }
                }));
            }
        }
    }),
    Materialize(Actions.Cast, 40,4,4,false,Dialog.get("mvsDscMaterialize"),new Spell(){
        public void perform(final Unit host, int level){
            Sound.play("shadowstep");
            final Terrain r = new Terrain("water", Shader.None, 0);
            P[] points = new P[3];
            int power = 15+(50*level);
            final P target = host.target.getAdd(host.pos);
            for (int i=0;i<3;i++){
                points[i] = target.getAdd(Global.r.nextInt(power)-(power/2),Global.r.nextInt(power)-(power/2));
                r.addPoint(points[i].x, points[i].y);
            }
            final Line[] lines = new Line[3];
            for (int i=0;i<3;i++) {
                Global.map.land.add(lines[i] = new Line(points[i], points[i+1<3?i+1:0]) {
                    public String toString() {
                        return "";
                    }
                });
            }
            r.render();
            Global.timers.add(new EventTimer(level*2,false,new Performer(){
                public void perform(){
                    Particle.holyExplosion(target);
                    for (int i = 0;i<3;i++) {
                        Global.map.land.remove(lines[i]);
                        Updater.remove(r);
                    }
                }
            }));
        }
    }),
    Focus(Actions.Conjure, 15, -1, 240, false, "Focus grants you free Stamina. [2+Level^2 Stamina]", new Spell(){
        private boolean first = true;
        public void perform(Unit host, int level){
            if (first) Sound.play("heal");
            host.get("Stamina").add(2 + level * level);
        }
    }),
    Heal(Actions.Buff, 40, 10, 240, true, "Heal channels you small streams of Health. [2*Level Health per Stamina] [Upkeep:10/20]", new Spell(){
        private boolean first = true;
        public void perform(Unit host, int level){
            if (first) Sound.play("heal");
            host.get("Health").add(level*2);
        }
    }),
    Frenzy(Actions.Buff, 40, 5, 240, false, "Frenzy grants you a burst of immense power. [20*Level Stamina]", new Spell(){
        @Override
        public void perform(Unit host, int level){
            host.get("Stamina").add(20+(20*level),true);
            host.stats.addEffect(Buffs.Exhaust, 2);
        }
    }),
    Haste(Actions.Conjure, 25,2,100,false, Dialog.get("mvsDscHaste"),produceBuff(Buffs.Haste)),
    Adrenaline(Actions.Conjure, 25,3,100,false,Dialog.get("mvsDscAdrenaline"),produceBuff(Buffs.Rejuvenate)),
    Cleave(Actions.Conjure, 25,1,100,false,Dialog.get("mvsDscCleave"),produceBuff(Buffs.Cleave)),
    Empower(Actions.Conjure, 25,1,100,false,Dialog.get("mvsDscEmpower"),produceBuff(Buffs.Empower)),
    Evade(Actions.Conjure, 25,2,100,false,Dialog.get("mvsDscEvade"),produceBuff(Buffs.Evade)),
    Flamestride(Actions.Buff, 10,4,100,false,Dialog.get("mvsDscFlamestride"),produceBuff(Buffs.Flamestride)),
    Glacialwalk(Actions.Buff, 10,4,100,false,Dialog.get("mvsDscGlacialwalk"),produceBuff(Buffs.Glacialwalk)),
    Stun(Actions.Cast, 40,4,50,false,Dialog.get("mvsDscStun"),new Spell(){
        @Override
        public void perform(Unit host, int level){
            ArrayList<Unit> t = Updater.collideUnit(host.target.getAdd(host.pos),40,Faction.Destructive);
            if (t.size() > 0) t.get(0).stats.addEffect(Buffs.Stun,level);
        }
    }),
    Slow(Actions.Cast, 30,1,3,false,Dialog.get("mvsDscSlow"),new Spell(){
        @Override
        public void perform(Unit host, int level){
            ArrayList<Unit> t = Updater.collideUnit(host.target.getAdd(host.pos),40,Faction.Destructive);
            if (t.size() > 0) t.get(0).stats.addEffect(Buffs.Slow,level);
        }
    }),
    Poison(Actions.Cast, 40,4,50,false,Dialog.get("mvsDscPoison"),new Spell(){
        @Override
        public void perform(Unit host, int level){
            ArrayList<Unit> t = Updater.collideUnit(host.target.getAdd(host.pos),40,Faction.Destructive);
            if (t.size() > 0) t.get(0).stats.addEffect(Buffs.Poison,level);
        }
    }),
    Shadowtwin(Actions.Buff, 40,3,100,false,Dialog.get("mvsDscShadowtwin"),new Spell() {
        @Override
        public void perform(final Unit host, int level){
            Sound.play("shadowstep");
            for (int i=0;i<level;i++) {
                Man p = new Man(host.toString().split("]")[1]) {
                    @Override
                    public void die() {
                        super.die();
                        host.get("Stamina").reserve -= 3;
                        remove();
                        Particle.explode("Smoke", pos, new P(), 80, 10, 0, true, 80);
                    }
                    @Override
                    public void update(){
                        super.update();
                        Stat expire = get("expire");
                        expire.add(-1);
                        if (expire.get() == 0) die();
                        Particle.smoke(pos.getAdd(clip.organs[4].ptd));
                    }
                };
                p.save = false;
                p.rangeZone = new int[]{-5};
                p.moveset.put(0,new Moveset[]{Actions.Use, Actions.Sidekick});
                p.stats.setStat("expire",new Stat(500+(level*100)));
                host.get("Stamina").reserve += 3;
                host.get("Stamina").add(0);
                p.brain = new IntelligenceSocial(p);
                p.dir = -host.dir;
                p.faction = host.faction;
                p.clip.setState(Actions.Buff);
            }
        }
    }),
    Shadowstep(Actions.Conjure, 20,1,40,true,Dialog.get("mvsDscShadowstep"),new Spell() {
        private int turn = 0;
        public void perform(Unit host, int level){
            if (turn++%4==0) Sound.play("shadow");
            Particle.smoke(host.pos);
            P target = host.target.getAdd(host.pos);
            Line pp = new Line(target, target.getAdd(0,1200));
            for (Line i : Global.map.land){
                if (i.lineCross(pp)){
                    Particle.smoke(host.pos);
                    if (!Global.map.isInside(target)) {
                        Sound.play("shadowstep");
                        host.pos.set(target);
                        host.setState(Actions.Idle);
                        host.lastLand = new Line();
                        return;
                    }
                    return;
                }
            }
        }
    }),
    Fireworks(Actions.Cast, 40,0,10,true,Dialog.get("mvsDscFireworks"),new Spell(){
        private int turn = 0;
        @Override
        public void perform(final Unit host, int level){
            if (turn++%8==0) Sound.play("fire");
            for (int i=0;i<2;i++) {
                final Projectile p = new Projectile("Fire", host.getHand(), host.target.getAdd(host.pos).getAdd(Global.r.nextInt(50)-25, Global.r.nextInt(50)-25), Global.r.nextInt(12)+3) {
                    @Override
                    public void stuck() {
                        Particle.explode("Fire", pos, new P(), 10, 10, 0, false, 4);
                        remove();
                    }
                };
                p.faction = host.faction;
                p.damage = 0;
                p.gravity = false;
                p.delay.set(12);
                p.delay.performer = new Performer() {
                    public void perform() {
                        p.stuck();
                    }
                };
            }
        }
    }),
    Fireblast(Actions.Cast, 40,4,60,true,Dialog.get("mvsDscFireblast"),new Spell(){
        private int turn = 0;
        @Override
        public void perform(final Unit host, int level){
            if (turn++%8==0) Sound.play("fire");
            for (int i=0;i<3;i++) {
                final Projectile p = new Projectile("Flame", host.getHand(), host.target.getAdd(host.pos).getAdd(Global.r.nextInt(50)-25, Global.r.nextInt(50)-25), Global.r.nextInt(10+level)+6+level) {
                    @Override
                    public void stuck() {
                        Particle.explode("Flame", pos, new P(), 10, 10, 0, false, 4);
                        remove();
                    }
                };
                p.faction = host.faction;
                p.damage = (int) (10+(host.get("Proficiency").get()*.3f));
                p.damageType = DamageType.Burn;
                p.gravity = false;
                p.delay.set(8+level);
                p.delay.performer = new Performer() {
                    public void perform() {
                        p.stuck();
                    }
                };
            }
        }
    }),
    Freeze(Actions.Cast, 40,2,24,true,Dialog.get("mvsDscFreeze"),new Spell(){
        private int turn = 0;
        @Override
        public void perform(final Unit host, final int level){
            if (turn++%6==0) Sound.play("ice");
            for (int i=0;i<3;i++) {
                final Projectile p = new Projectile("Ice", host.getHand(), host.target.getAdd(host.pos).getAdd(Global.r.nextInt(150)-75, Global.r.nextInt(150)-75), 20) {
                    private int tick = 200;
                    @Override
                    protected void onAttack(Unit p) {
                        p.stats.addEffect(Buffs.Freeze, level+1);
                        super.onAttack(p);
                    }
                    @Override
                    public void update(){
                        super.update();
                        vel.mult(.9f,.9f);
                        tick--;
                        if (tick == 0) remove();
                    }
                };
                p.faction = host.faction;
                p.gravity = false;
                p.delay.set(15);
                p.delay.performer = new Performer() {
                    public void perform() {
                        p.stuck();
                    }
                };
            }
        }
    }),
    Flameroll(Actions.Cast, 40,5,120,false,Dialog.get("mvsDscFlameroll"),new Spell(){
        @Override
        public void perform(final Unit host, final int level){
            final Projectile p = new Projectile("Fireball", host.pos, host.target.getAdd(host.pos), 80){
                @Override
                public void stuck(){
                    attackHitSound();
                    host.setVisible(true);
                    Particle.explode("Fire", pos, new P(), 100+(level*15), 10+(level*2), 0, false, 4);
                    Particle.smoke(pos);
                    explode(40+(level*5));
                    remove();
                }
                @Override
                public void update(){
                    super.update();
                    Particle.explode("Fire", pos, new P(), 15, 10, 0, true, 4);
                }
                public void attackHitSound(){
                    Sound.play("explosion_small");
                }
            };
            host.setVisible(false);
            p.gravity = true;
            p.faction = host.faction;
            p.damage = 15+host.get("Proficiency").get();
            p.delay.set(50);
            p.delay.performer = new Performer(){
                public void perform(){
                    p.stuck();
                }
            };
        }
    }),
    Fireball(Actions.Cast, 40,5,50,false,Dialog.get("mvsDscFireball"),new Spell(){
        @Override
        public void perform(final Unit host, final int level){
            final Projectile p = new Projectile("Fireball", host.getHand(), host.target.getAdd(host.pos), 50){
                @Override
                public void stuck(){
                    attackHitSound();
                    Particle.explode("Fire", pos, new P(), 100+(level*15), 10+(level*2), 0, false, 4);
                    Particle.smoke(pos);
                    explode(40+(level*5));
                    remove();
                }
                @Override
                public void update(){
                    super.update();
                    Particle.explode("Fire", pos, new P(), 15, 10, 0, true, 4);
                }
                public void attackHitSound(){
                    Sound.play("explosion_small");
                }
            };
            p.gravity = true;
            p.faction = host.faction;
            p.damage = 15+host.get("Proficiency").get();
            p.delay.set(2);
            p.delay.performer = new Performer(){
                public void perform(){
                    p.faction = Faction.Destructive;
                }
            };
        }
    }),
    Blizzard(Actions.Buff, 30,3,240,true,Dialog.get("mvsDscBlizzard"),new Spell(){
        @Override
        public void perform(final Unit host, final int level){
            if (Global.r.nextInt(40) > (level*8)-4) return;
            if (Math.random() < .2f) Sound.play("ice");
            final Projectile p = new Projectile("Ice", host.pos.getAdd(Global.nextGaussian(500),-500), host.pos.getAdd(Global.nextGaussian(500),0), 8+Global.r.nextInt(6*level)){
                @Override
                protected void onAttack(Unit p){
                    p.stats.addEffect(Buffs.Freeze, level+1);
                    super.onAttack(p);
                }
                @Override
                public void stuck(){
                    Particle.explode("Ice",pos, new P(), 30, 20, 0, false, 1);
                    remove();
                }
                @Override
                public void update(){
                    super.update();
                    Particle.explode("Ice",pos, new P(), 3, 10, 0, false, 1);
                }
            };
            p.faction = host.faction;
            p.damage = 7+host.get("Proficiency").get();
            p.gravity = false;
            p.delay.set(150);
            p.delay.performer = new Performer(){
                @Override
                public void perform(){
                    p.stuck();
                }
            };
        }
    }),
    Meteor(Actions.Cast, 40,6,240,true,Dialog.get("mvsDscMeteor"),new Spell(){
        public void perform(Unit host, int level){
            if (Global.r.nextInt(60) > level*2) return;
            Sound.play("meteor");
            final Projectile p = new Projectile("Fireball", host.pos.getAdd(Global.nextGaussian(50),-500), host.pos.getAdd((host.target.x)+Global.nextGaussian(50),0), 12+Global.r.nextInt(6)){
                @Override
                public void stuck(){
                    Particle.explode("Fire", pos, new P(), 30, 25, 0, false, 4);
                    Particle.smoke(pos);
                    explode(50);
                    remove();
                }
                @Override
                public void update(){
                    super.update();
                    Particle.explode("Fire", pos, new P(), 3, 10, 0, false, 1);
                }
            };
            p.faction = Faction.Destructive;
            p.damage = 15+host.get("Proficiency").get();
            p.gravity = false;
            p.delay.set(150);
            p.delay.performer = new Performer(){
                public void perform(){
                    p.stuck();
                }
            };
        }
    });
    private static Spell produceBuff(final Buffs p){
        return new Spell(){
            public void perform(Unit host, int level){
                host.stats.addEffect(p, level);
            }
        };
    }
    protected int cost;
    protected Actions action;
    protected int delay;
    protected Spell spell;
    protected String description;
    protected int cooldown;
    protected boolean channel;

    Spells(Actions action, int del, int cost, int cooldown, boolean channel, String desc, Spell performer){
        this.action = action;
        this.delay = del;
        this.cost = cost;
        this.cooldown = cooldown;
        this.channel = channel;
        this.description = desc;
        this.spell = performer;
    }
    public static Spells getByName(String p){
        for (Spells s : values()) {
            if (s.getName().equals(p)){
                return s;
            }
        }
        return null;
    }

    @Override
    public String getCodeName() {
        return "M"+getName();
    }
    public String getName() {
        return name();
    }

    public String getDescription(){
        return description + " [Cost:"+cost+"]";
    }

    public Actions getAction(){
        return action;
    }

    public float getCost() {
        return cost;
    }

    @Override
    public int getDel() {
        return delay;
    }

    public Spell getEvent() {
        return spell;
    }

    @Override
    public int getCooldown(){
        return cooldown;
    }

    @Override
    public void update(Unit host, int level){
        host.face(host.target.x > 0 ? 1 : -1, true);
        spell.perform(host, level);
        if (!channel || (Global.r.nextInt(Math.max(0,40-(cost*2))) == 0 && !host.get("Stamina").require(1+host.getStatPenalty()))) host.setState(Actions.Idle);
    }

    @Override
    public void onSet(Unit p){
    }

    @Override
    public void onFinish(Unit p){
    }

    @Override
    public void onInterrupt(Unit p){
    }
}
