package com.ga.unit.skill;

import com.ga.game.Dialog;
import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.Shader;
import com.ga.map.Terrain;
import com.ga.map.Updater;
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
 * Created by Ga on 8/1/2016. asd
 */
public enum Spells implements Moveset {
    Innersword(Actions.Cast, 6,3,14,new Tags(){{
        add(Tag.Conjuration);
        add(Tag.Buff);
    }}){
        public void trigger(Unit host){
            Man man = (Man)host;
            if (man.wieldingNone()) {
                Sound.play("shadowstep");
                final Item t = (Item) Stock.getByName("Ethersword", -1);
                man.setWeapon(t);
                int duration = 1;
                if (host.stateLevel == 2) duration = 4;
                if (host.stateLevel == 3) duration = 16;
                if (host.stateLevel > 3) duration = 256;
                t.uses = duration;
                Global.timers.add(new EventTimer(duration,false,new Performer(){
                    public void perform(){
                        t.remove();
                    }
                }));
            }
        }
    },
    Materialize(Actions.Cast, 40,4,4,new Tags(){{
        add(Tag.Conjuration);
    }}){
        public void trigger(Unit host){
            Sound.play("shadowstep");
            final Terrain r = new Terrain("water", Shader.None, 0);
            P[] points = new P[3];
            int power = 15+(50*host.stateLevel);
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
            Global.timers.add(new EventTimer(host.stateLevel*2,false,new Performer(){
                public void perform(){
                    Particle.holyExplosion(target);
                    for (int i = 0;i<3;i++) {
                        Global.map.land.remove(lines[i]);
                        Updater.remove(r);
                    }
                }
            }));
        }
    },
    Focus(Actions.Conjure, 15, -1, 40,new Tags(){{
        add(Tag.Buff);
        add(Tag.Martial);
    }}){
        public void trigger(Unit host){
            Sound.play("heal");
            host.get("Stamina").add(2 + host.stateLevel * host.stateLevel);
        }
    },
    Rejuvenate(Actions.Buff, 40, 1, 40, new Tags(){{
        add(Tag.Buff);
        add(Tag.Biology);
    }}){
        public void trigger(Unit host){
            host.buff(Buffs.Rejuvenate, host.stateLevel);
        }
    },
    Frenzy(Actions.Buff, 40, 5, 40, new Tags(){{
        add(Tag.Buff);
        add(Tag.Biology);
    }}){
        public void trigger(Unit host){
            host.get("Stamina").add(20+(20*host.stateLevel),true);
            host.buff(Buffs.Exhaust, 2);
        }
    },
    Windwalk(Actions.Conjure, 25,2,20,new Tags(){{
        add(Tag.Buff);
        add(Tag.Air);
    }}){
        public void trigger(Unit host){
            host.buff(Buffs.Haste, host.stateLevel);
        }
    },
    Flamestride(Actions.Buff, 10,4,20,new Tags(){{
        add(Tag.Buff);
        add(Tag.Fire);
    }}){
        public void trigger(Unit host){
            host.buff(Buffs.Flamestride, host.stateLevel);
        }
    },
    Glacialwalk(Actions.Buff, 10,4,20,new Tags(){{
        add(Tag.Buff);
        add(Tag.Cold);
    }}){
        public void trigger(Unit host){
            host.buff(Buffs.Glacialwalk, host.stateLevel);
        }
    },
    Stun(Actions.Cast, 40,4,40,new Tags(){{
        add(Tag.Debuff);
        add(Tag.Cunning);
    }}){
        public void trigger(Unit host){
            ArrayList<Unit> t = Updater.collideUnit(host.target.getAdd(host.pos),40,Faction.Destructive);
            if (t.size() > 0) t.get(0).buff(Buffs.Stun,host.stateLevel);
        }
    },
    Slow(Actions.Cast, 40,4,40,new Tags(){{
        add(Tag.Debuff);
        add(Tag.Agility);
    }}){
        public void trigger(Unit host){
            ArrayList<Unit> t = Updater.collideUnit(host.target.getAdd(host.pos),40,Faction.Destructive);
            if (t.size() > 0) t.get(0).buff(Buffs.Slow,host.stateLevel);
        }
    },
    Calling(Actions.Buff, 40,2,4,new Tags(){{
        add(Tag.Conjuration);
        add(Tag.Biology);
    }}){
        public void trigger(Unit host){
            Sound.play("shadowstep");
            for (int i=0;i<host.rand.nextInt(host.stateLevel+1);i++) {
                Unit p = (Unit) Stock.getByName("Dog",-1);
                p.pos.set(host.pos);
                p.save = false;
                p.stats.setStat("expire",new Stat(500+(host.stateLevel*100)));
                host.get("Stamina").reserve += 3;
                host.get("Stamina").add(0);
                p.brain = new IntelligenceSocial(p);
                p.dir = host.dir;
                if (host.brain != null){
                    p.brain.target = host.brain.target;
                }
                p.faction = host.faction;
            }
        }
    },
    Shadowtwin(Actions.Buff, 40,3,60,new Tags(){{
        add(Tag.Shadow);
        add(Tag.Conjuration);
    }}){
        public void trigger(final Unit host){
            Sound.play("shadowstep");
            for (int i=0;i<host.rand.nextInt(host.stateLevel+1);i++) {
                Man p = new Man(host.seed) {
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
                p.load(host.toString());
                p.setHead("Phantom");
                p.save = false;
                p.stats.setStat("expire",new Stat(500+(host.stateLevel*100)));
                host.get("Stamina").reserve += 3;
                host.get("Stamina").add(0);
                p.brain = new IntelligenceSocial(p);
                p.dir = -host.dir;
                p.faction = host.faction;
                p.clip.setState(Actions.Buff);
            }
        }
    },
    Shadowstep(Actions.Conjure, 20,1,40,new Tags(){{
        add(Tag.Shadow);
        add(Tag.Cunning);
    }}){
        public void trigger(Unit host){
            Sound.play("shadow");
            Particle.smoke(host.pos);
            P target = host.target.getAdd(host.pos);
            Line pp = new Line(target, target.getAdd(0,1200));
            for (Line i : Global.map.land){
                if (i.lineCross(pp)){
                    Particle.smoke(host.pos);
                    if (Global.map.fallSafe(target, 0) != null) {
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
    },
    Fireworks(Actions.Cast, 40,0,10,new Tags(){{
        add(Tag.Fire);
    }}){
        public void update(Unit host){
            Sound.play("fire");
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
            super.update(host);
        }
    },
    Fireblast(Actions.Cast, 40,1,60,new Tags(){{
        add(Tag.Fire);
        add(Tag.Air);
    }}){
        public void update(Unit host){
            Sound.play("fire");
            final int level = host.stateLevel;
            for (int i=0;i<3;i++) {
                final Projectile p = new Projectile("Flame", host.getHand(), host.target.getAdd(host.pos).getAdd(Global.r.nextInt(50)-25, Global.r.nextInt(50)-25), Global.r.nextInt(10+level)+6+level) {
                    @Override
                    public void stuck() {
                        Particle.explode("Flame", pos, new P(), 10, 10, 0, false, 4);
                        remove();
                    }
                    @Override
                    protected void onAttack(Unit p) {
                        p.buff(Buffs.Burn, level);
                    }
                };
                p.faction = host.faction;
                p.damage = 1;
                p.gravity = false;
                p.delay.set(8+level);
                p.delay.performer = new Performer() {
                    public void perform() {
                        p.stuck();
                    }
                };
            }
            super.update(host);
        }
    },
    Freeze(Actions.Cast, 40,1,24,new Tags(){{
        add(Tag.Cold);
        add(Tag.Air);
    }}){
        public void update(Unit host){
            Sound.play("ice");
            final int level = host.stateLevel;
            for (int i=0;i<3;i++) {
                final Projectile p = new Projectile("Ice", host.getHand(), host.target.getAdd(host.pos).getAdd(Global.r.nextInt(150)-75, Global.r.nextInt(150)-75), 20) {
                    private int tick = 200;
                    @Override
                    protected void onAttack(Unit p) {
                        p.buff(Buffs.Freeze, level+1);
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
            super.update(host);
        }
    },
    Flameroll(Actions.Cast, 40,5,120,new Tags(){{
        add(Tag.Fire);
        add(Tag.Biology);
    }}){
        public void trigger(final Unit host){
            final int level = host.stateLevel;
            final Projectile p = new Projectile("Fireball", host.pos, host.target.getAdd(host.pos), 40+(level*20)){
                @Override
                public void stuck(){
                    attackHitSound();
                    Particle.explode("Fire", pos, new P(), 85+(level*10), 10+(level*2), 0, false, 4);
                    Particle.smoke(pos);
                    explode(80+(level*10));
                    remove();
                    host.setVisible(true);
                    host.stats.triggerEffect(Buffs.Invincible);
                }
                @Override
                public void update(){
                    super.update();
                    Particle.explode("Fire", pos, new P(), 15, 10, 0, true, 4);
                }
                void attackHitSound(){
                    Sound.play("explosion_small");
                }
            };
            host.setVisible(false);
            host.buff(Buffs.Invincible, 80);
            p.gravity = true;
            p.faction = host.faction;
            p.damage = 8+level;
            p.delay.set(50);
            p.delay.performer = new Performer(){
                public void perform(){
                    p.stuck();
                }
            };
        }
    },
    Fireball(Actions.Cast, 40,5,50,new Tags(){{
        add(Tag.Fire);
        add(Tag.Conjuration);
    }}){
        public void trigger(Unit host){
            final int level = host.stateLevel;
            final Projectile p = new Projectile("Fireball", host.getHand(), host.target.getAdd(host.pos), 30+(level*10)){
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
                void attackHitSound(){
                    Sound.play("explosion_small");
                }
            };
            p.gravity = true;
            p.faction = host.faction;
            p.damage = 9+level;
            p.delay.set(2);
            p.delay.performer = new Performer(){
                public void perform(){
                    p.faction = Faction.Destructive;
                }
            };
        }
    },
    Blizzard(Actions.Buff, 30,2,240,new Tags(){{
        add(Tag.Cold);
        add(Tag.Air);
        add(Tag.Mastery);
    }}){
        public void update(Unit host){
            final int level = host.stateLevel;
            if (Global.r.nextInt(40) > (level*8)-4) return;
            if (Math.random() < .2f) Sound.play("ice");
            final Projectile p = new Projectile("Ice", host.pos.getAdd(Global.nextGaussian(500),-500), host.pos.getAdd(Global.nextGaussian(500),0), 8+Global.r.nextInt(6*level)){
                @Override
                protected void onAttack(Unit p){
                    p.buff(Buffs.Freeze, Global.r.nextInt(level));
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
            p.damage = 4;
            p.gravity = false;
            p.delay.set(150);
            p.delay.performer = new Performer(){
                @Override
                public void perform(){
                    p.stuck();
                }
            };
            super.update(host);
        }
    },
    Meteor(Actions.Cast, 40,2,240,new Tags(){{
        add(Tag.Fire);
        add(Tag.Conjuration);
        add(Tag.Mastery);
    }}){
        public void update(Unit host){
            if (Global.r.nextInt(60) > host.stateLevel*2) return;
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
            p.damage = 9+host.stateLevel;
            p.gravity = false;
            p.delay.set(150);
            p.delay.performer = new Performer(){
                public void perform(){
                    p.stuck();
                }
            };
            super.update(host);
        }
    };
    protected int cost;
    protected Actions action;
    protected int delay;
    protected String description;
    protected int cooldown;
    Tags tags;

    Spells(Actions action, int del, int cost, int cooldown, Tags tags){
        this.action = action;
        this.delay = del;
        this.cost = cost;
        this.cooldown = cooldown;
        this.description = Dialog.get("mvsDsc"+name());
        this.tags = tags;
        this.tags.add(Tag.Swinging);
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

    public int getCost() {
        return cost;
    }

    @Override
    public float getVelX() {
        return 0;
    }

    @Override
    public int getDel() {
        return delay;
    }

    @Override
    public int getCooldown(){
        return cooldown;
    }

    @Override
    public int getDifficulty(){
        return 0;
    }

    @Override
    public void update(Unit host){
        host.face(host.target.x > 0 ? 1 : -1, true);
        if (Updater.time.over() && !host.get("Stamina").require(cost)) host.stateSet(Actions.Idle);
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

    @Override
    public void trigger(Unit p){
        Sound.play("spell");
        p.state = this;
    }

    @Override
    public Tags getTags(){
        return tags;
    }
}
