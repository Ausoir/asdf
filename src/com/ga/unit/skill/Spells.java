package com.ga.unit.skill;

import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.tool.Word;
import com.ga.unit.*;
import com.ga.unit.intelligence.EnemyIntelligence;
import com.ga.unit.item.ItemFactory;
import com.ga.unit.item.Projectile;
import com.ga.unit.item.Weapon;

/**
 * Created by Ga on 8/1/2016.
 */
public enum Spells implements Moveset {
    Focus(Actions.Conjure, 15, null, 0, 240,"Focus grants you free Stamina and Energy. [3*Level Stamina and Energy]", new Spell(){
        @Override
        public boolean perform(){
            host.get("Stamina").add(3*level);
            host.get("Energy").add(3*level);
            return true;
        }
    }),
    Heal(Actions.Buff, 40, "Energy", 3, 240, "Heal grants you immediate Health. [1*Level Health per Energy]", new Spell(){
        @Override
        public boolean perform(){
            if (host.get("Energy").require(1)) {
                host.get("Health").add(level);
                return false;
            }
            return true;
        }
    }),
    Haste(Actions.Conjure, 25,"Energy",-2,100,"Haste doubles movement speed for 5 seconds. [Duration:10*Level]",produceBuff(Buffs.Haste)),
    Adrenaline(Actions.Conjure, 25,"Energy",3,100,"Haste grants the user a burst of Stamina for a short time. [Duration:10*Level]",produceBuff(Buffs.Rejuvenate)),
    Cleave(Actions.Conjure, 25,"Stamina",-1,100,"Cleave grants the user a cleaving property. [Level*1 Uses] [Duration:10*Level]",produceBuff(Buffs.Cleave)),
    Empower(Actions.Conjure, 25,"Stamina",-1,100,"Empower grants the next attack 10 bonus damage. [Level*1 Uses] [Duration:10*Level]",produceBuff(Buffs.Empower)),
    Frenzy(Actions.Conjure, 25,"Health",10,100,"Frenzy grants the user infinite Stamina and Energy at the cost of Health and restlessness for 10 seconds. [Duration:10*Level]",produceBuff(Buffs.Rejuvenate)),
    Evade(Actions.Conjure, 25,"Energy",-2,100,"Evade grants you invulnerability to physical attacks for 10 seconds. [Level*1 Uses] [Duration:10*Level]",produceBuff(Buffs.Evade)),
    Flamestride(Actions.Buff, 10,"Energy",4,100,"Flamestride leaves damaging flames in the trails of the caster. [Level*1 Uses] [Duration:8*Level] [Damage:10] [Damage Scale:2 per Proficiency]",produceBuff(Buffs.Flamestride)),
    Glacialwalk(Actions.Buff, 10,"Energy",4,100,"Glacialwalk leaves freezing trails behind the caster. [Level*1 Uses] [Duration:8*Level] [Trail Duration:15*Level] [Freeze Duration:5*Level] [Damage:0]",produceBuff(Buffs.Glacialwalk)),
    Stun(Actions.Cast, 40, "Energy",4,50,"Stun a single enemy. [Duration:5*Level]",new Spell(){
        @Override
        public boolean perform(){
            Matter t = new Matter();
            t.faction = host.faction;
            t.pos = host.target.getAdd(host.pos);
            Matter target = Updater.collide(t,40);
            if (target instanceof Unit){
                ((Unit) target).stats.addEffect(Buffs.Stun,level);
            }
            return true;
        }
    }),
    Poison(Actions.Cast, 40, "Energy",4,50,"Poison a single enemy. [Duration:5*Level]",new Spell(){
        @Override
        public boolean perform(){
            Matter t = new Matter();
            t.faction = host.faction;
            t.pos = host.target.getAdd(host.pos);
            Matter target = Updater.collide(t,40);
            if (target instanceof Unit){
                ((Unit) target).stats.addEffect(Buffs.Poison,level);
            }
            return true;
        }
    }),
    Shadowtwin(Actions.Buff, 40,"Energy",-1,100,"Summon a mirror of yourself to aid you in battle. [Level*1 Shadows] [Duration:30*Level] [6 Energy Reserve/Shadow] [3 Stamina Reserve/Shadow]",new Spell() {
        @Override
        public boolean perform() {
            for (int i=0;i<level;i++) {
                Man p = new Man(host.name, host.pos.getAdd(0, 0), host.seed) {
                    @Override
                    public void die() {
                        super.die();
                        host.get("Energy").reserve -= 6;
                        host.get("Stamina").reserve -= 3;
                        remove();
                        Particle.explode("Smoke", pos, P.getZero(), 80, 10, 0, true, 80);
                    }
                    @Override
                    public void update(){
                        super.update();
                        Stat expire = get("expire");
                        expire.add(-1);
                        if (expire.get() == 0) die();
                        Particle.smoke(pos.getAdd(clip.organs[0].ptd));
                    }
                    @Override
                    public String toString(){
                        return "";
                    }
                };
                p.stats.setStat("expire",new Stat(300*level));
                host.get("Energy").reserve += 6;
                host.get("Stamina").reserve += 3;
                host.get("Energy").add(0);
                host.get("Stamina").add(0);
                p.brain = new EnemyIntelligence(p);
                if (host instanceof Man) {
                    Man t = (Man) host;
                    p.setWeapon((Weapon) ItemFactory.produce(t.getWeapon().name, t.getWeapon().seed));
                    p.setArmor(t.getArmor());
                }
                p.dir = -host.dir;
                p.faction = host.faction;
                p.clip.setState(Actions.Buff);
            }
            return true;
        }
    }),
    Shadowstep(Actions.Conjure, 20,"Energy",2,40,"Blink to the target location.",new Spell() {
        @Override
        public boolean perform() {
            Particle.smoke(host.pos);
            P target = host.target.getAdd(host.pos);
            Line pp = new Line(target, target.getAdd(0,600));
            for (Line i : Global.map.land){
                if (i.lineCross(pp)){
                    host.pos.set(target);
                    Particle.smoke(host.pos);
                    return true;
                }
            }
            return false;
        }
    }),
    Fireworks(Actions.Cast, 40,"Energy",2,60,"Shoot streams of cackling sparks. [Range:100] [Damage:1 Constant] [Upkeep:.1 Energy]",new Spell(){
        @Override
        public boolean perform(){
            for (int i=0;i<2;i++) {
                final Projectile p = new Projectile("effect/Fire", host.getHand(), host.target.getAdd(host.pos).getAdd(Global.r.nextInt(50)-25, Global.r.nextInt(50)-25), Global.r.nextInt(12)+3) {
                    @Override
                    protected void onAttack(Matter p) {
                        if (!host.stats.hasEffect(Buffs.Cleave)) super.onAttack(p);
                    }

                    @Override
                    public void stuck() {
                        Particle.explode("Fire", pos, P.getZero(), 10, 10, 0, false, 4);
                        remove();
                    }
                };
                p.faction = host.faction;
                p.damage = 1;
                p.gravity = false;
                p.delay.set(12);
                p.delay.performer = new Performer() {
                    @Override
                    public boolean perform() {
                        p.stuck();
                        return true;
                    }
                };
            }
            if (Global.r.nextInt(8) == 0 && !host.get("Energy").require(1)) return true;
            return false;
        }
    }),
    Fireblast(Actions.Cast, 40,"Energy",4,60,"Shoot streams of blazing flame. [Range:10*Level+100] [Damage:10] [Damage Scale:.3 per Proficiency] [Upkeep:1/5+Level chance to consume 1 Energy]",new Spell(){
        @Override
        public boolean perform(){
            for (int i=0;i<3;i++) {
                final Projectile p = new Projectile("effect/Flame", host.getHand(), host.target.getAdd(host.pos).getAdd(Global.r.nextInt(50)-25, Global.r.nextInt(50)-25), Global.r.nextInt(10+level)+6+level) {
                    @Override
                    protected void onAttack(Matter p) {
                        if (!host.stats.hasEffect(Buffs.Cleave)) super.onAttack(p);
                    }

                    @Override
                    public void stuck() {
                        Particle.explode("Flame", pos, P.getZero(), 10, 10, 0, false, 4);
                        remove();
                    }
                };
                p.faction = host.faction;
                p.damage = (int) (10+(host.get("Proficiency").get()*.3f));
                p.gravity = false;
                p.delay.set(8+level);
                p.delay.performer = new Performer() {
                    @Override
                    public boolean perform() {
                        p.stuck();
                        return true;
                    }
                };
            }
            if (Global.r.nextInt(5+level) == 0 && !host.get("Energy").require(1)) return true;
            return false;
        }
    }),
    Freeze(Actions.Cast, 40,"Energy",3,24,"Shoot a stream of freezing blast. [Range:Low] [Damage:0 Constant]",new Spell(){
        @Override
        public boolean perform(){
            for (int i=0;i<3;i++) {
                final Projectile p = new Projectile("effect/Ice", host.getHand(), host.target.getAdd(host.pos).getAdd(Global.r.nextInt(150)-75, Global.r.nextInt(150)-75), 20) {
                    private int tick = 200;
                    @Override
                    protected void onAttack(Matter p) {
                        if (p instanceof Unit){
                            ((Unit)p).stats.addEffect(Buffs.Freeze, level+1);
                        }
                        if (!host.stats.hasEffect(Buffs.Cleave)) super.onAttack(p);
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
                    @Override
                    public boolean perform() {
                        p.stuck();
                        return true;
                    }
                };
            }
            if (Global.r.nextInt(5+level) == 0 && !host.get("Energy").require(1)) return true;
            return false;
        }
    }),
    Fireball(Actions.Cast, 40,"Energy",5,50,"Shoot a fireball. [Range:Extreme] [Damage:15] [Radius:20+10*Level]",new Spell(){
        @Override
        public boolean perform(){
            final Projectile p = new Projectile("item/Fireball", host.getHand(), host.target.getAdd(host.pos), 20+(host.get("Stamina").get()*2)){
                @Override
                protected void onAttack(Matter p){
                    if (!host.stats.hasEffect(Buffs.Cleave)) super.onAttack(p);
                }
                @Override
                public void stuck(){
                    Particle.explode("Fire", pos, P.getZero(), 100+(level*15), 10+(level*2), 0, false, 4);
                    Particle.smoke(pos);
                    Updater.collide(this,20+(level*10));
                    remove();
                }
                @Override
                public void update(){
                    super.update();
                    Particle.explode("Fire", pos, P.getZero(), 15, 10, 0, true, 4);
                }
            };
            p.faction = host.faction;
            p.damage = 15+host.get("Proficiency").get();
            p.gravity = false;
            p.delay.set(50);
            p.delay.performer = new Performer(){
                @Override
                public boolean perform(){
                    p.stuck();
                    return true;
                }
            };
            return true;
        }
    }),
    Blizzard(Actions.Buff, 30,"Energy",5,240,"Conjure an ice storm to slaughter your enemies in its vicinity. [Radius:800] [Damage:7 per Icebolt] [Freeze] [3*Level Icebolts/second] [Upkeep:1/(10+(3*Level)) chance per Icebolt to consume 2 Stamina]",new Spell(){
        @Override
        public boolean perform(){
            if (Global.r.nextInt(40) > (level*8)-4) return false;
            final Projectile p = new Projectile("effect/Ice", host.pos.getAdd(Global.nextGaussian(500),-500), host.pos.getAdd(Global.nextGaussian(500),0), 8+Global.r.nextInt(6*level)){
                @Override
                protected void onAttack(Matter p){
                    if (p instanceof Unit){
                        ((Unit)p).stats.addEffect(Buffs.Freeze, level+1);
                    }
                    if (!host.stats.hasEffect(Buffs.Cleave)) super.onAttack(p);
                }
                @Override
                public void stuck(){
                    Particle.explode("Ice",pos, P.getZero(), 30, 20, 0, false, 1);
                    remove();
                }
                @Override
                public void update(){
                    super.update();
                    Particle.explode("Ice",pos, P.getZero(), 3, 10, 0, false, 1);
                }
            };
            p.faction = host.faction;
            p.damage = 7+host.get("Proficiency").get();
            p.gravity = false;
            p.delay.set(150);
            p.delay.performer = new Performer(){
                @Override
                public boolean perform(){
                    p.stuck();
                    return true;
                }
            };
            if (Global.r.nextInt(10+(level*3)) == 1 && !host.get("Energy").require(2)) return true;
            host.setAnimation(Actions.Buff);
            return false;
        }
    }),
    Meteor(Actions.Cast, 40,"Energy",9,240,"Conjure down a meteor to devastate anything in its target range. [Range:0~500] [Radius:50] [Damage:15 per Meteor]",new Spell(){
        @Override
        public boolean perform(){
            if (Global.r.nextInt(60) > level*2) return false;
            final Projectile p = new Projectile("item/Fireball", host.pos.getAdd(Global.nextGaussian(50),-500), host.pos.getAdd((host.target.x)+Global.nextGaussian(50),0), 12+Global.r.nextInt(6)){
                @Override
                public void stuck(){
                    Particle.explode("Fire", pos, P.getZero(), 30, 25, 0, false, 4);
                    Particle.smoke(pos);
                    Updater.collide(this,50);
                    remove();
                }
                @Override
                public void update(){
                    super.update();
                    Particle.explode("Fire", pos, P.getZero(), 3, 10, 0, false, 1);
                }
            };
            p.faction = Faction.Hostile;
            p.damage = 15+host.get("Proficiency").get();
            p.gravity = false;
            p.delay.set(150);
            p.delay.performer = new Performer(){
                @Override
                public boolean perform(){
                    p.stuck();
                    return true;
                }
            };
            if (Global.r.nextInt(3+level) == 0 && !host.get("Energy").require(1)) return true;
            host.setAnimation(Actions.Buff);
            return false;
        }
    });
    private static Spell produceBuff(final Buffs p){
        return new Spell(){
            @Override
            public void start(Unit host, int level){
                super.start(host, level);
                host.stats.addEffect(p, level);
            }
        };
    }
    protected String stat;
    protected float cost;
    protected Actions action;
    protected int delay;
    protected Spell spell;
    protected String description;
    protected int cooldown;

    Spells(Actions action, int del, String statReq, float cost, int cooldown, String desc, Spell performer){
        this.action = action;
        this.delay = del;
        this.stat = statReq;
        this.cost = cost;
        this.cooldown = cooldown;
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
    public String getName() {
        return name();
    }

    public String getDescription(){
        return description + " [Cost:"+(int)cost+" "+ Word.wordify(stat)+"]";
    }

    public Actions getAction(){
        return action;
    }

    @Override
    public String getStatReq() {
        return stat;
    }

    public float getCost() {
        return cost;
    }

    @Override
    public int getDel() {
        return delay;
    }

    @Override
    public int getCooldown(){
        return cooldown;
    }

    public Spell getEvent(){
        return spell;
    }
}
