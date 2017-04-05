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
 * Created by Ga on 8/1/2016. asd
 */
public enum Spells implements Moveset {
    Innersword(Actions.Cast, 6,3,14,false,new Spell(){
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
    }, new Tags(){{
        add(Tag.Conjuration);
        add(Tag.Buff);
    }}),
    Materialize(Actions.Cast, 40,4,4,false,new Spell(){
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
    }, new Tags(){{
        add(Tag.Conjuration);
    }}),
    Focus(Actions.Conjure, 15, -1, 40, false, new Spell(){
        private boolean first = true;
        public void perform(Unit host, int level){
            if (first) Sound.play("heal");
            host.get("Stamina").add(2 + level * level);
        }
    }, new Tags(){{
        add(Tag.Buff);
        add(Tag.Skill);
    }}),
    Heal(Actions.Buff, 40, 10, 40, true, new Spell(){
        private boolean first = true;
        public void perform(Unit host, int level){
            if (first) Sound.play("heal");
            host.get("Health").add(level*2);
        }
    }, new Tags(){{
        add(Tag.Buff);
        add(Tag.Biology);
    }}),
    Frenzy(Actions.Buff, 40, 5, 40, false, new Spell(){
        @Override
        public void perform(Unit host, int level){
            host.get("Stamina").add(20+(20*level),true);
            host.buff(Buffs.Exhaust, 2);
        }
    }, new Tags(){{
        add(Tag.Buff);
        add(Tag.Biology);
    }}),
    Haste(Actions.Conjure, 25,2,20,false,produceBuff(Buffs.Haste), new Tags(){{
        add(Tag.Buff);
        add(Tag.Air);
    }}),
    Adrenaline(Actions.Conjure, 25,3,20,false,produceBuff(Buffs.Rejuvenate), new Tags(){{
        add(Tag.Buff);
        add(Tag.Biology);
    }}),
    Flamestride(Actions.Buff, 10,4,20,false,produceBuff(Buffs.Flamestride), new Tags(){{
        add(Tag.Buff);
        add(Tag.Fire);
    }}),
    Glacialwalk(Actions.Buff, 10,4,20,false,produceBuff(Buffs.Glacialwalk), new Tags(){{
        add(Tag.Buff);
        add(Tag.Cold);
    }}),
    Stun(Actions.Cast, 40,4,40,false,new Spell(){
        @Override
        public void perform(Unit host, int level){
            ArrayList<Unit> t = Updater.collideUnit(host.target.getAdd(host.pos),40,Faction.Destructive);
            if (t.size() > 0) t.get(0).buff(Buffs.Stun,level);
        }
    }, new Tags(){{
        add(Tag.Debuff);
        add(Tag.Cunning);
    }}),
    Slow(Actions.Cast, 30,1,3,false,new Spell(){
        @Override
        public void perform(Unit host, int level){
            ArrayList<Unit> t = Updater.collideUnit(host.target.getAdd(host.pos),40,Faction.Destructive);
            if (t.size() > 0) t.get(0).buff(Buffs.Slow,level);
        }
    }, new Tags(){{
        add(Tag.Debuff);
        add(Tag.Agile);
    }}),
    Poison(Actions.Cast, 40,4,20,false,new Spell(){
        @Override
        public void perform(Unit host, int level){
            ArrayList<Unit> t = Updater.collideUnit(host.target.getAdd(host.pos),40,Faction.Destructive);
            if (t.size() > 0) t.get(0).buff(Buffs.Poison,level);
        }
    }, new Tags(){{
        add(Tag.Debuff);
        add(Tag.Biology);
    }}),
    Calling(Actions.Buff, 40,2,4,false,new Spell() {
        @Override
        public void perform(final Unit host, int level){
            Sound.play("shadowstep");
            for (int i=0;i<host.rand.nextInt(level+1);i++) {
                Unit p = (Unit) Stock.getByName("Dog",-1);
                p.pos.set(host.pos);
                p.save = false;
                p.stats.setStat("expire",new Stat(500+(level*100)));
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
    }, new Tags(){{
        add(Tag.Conjuration);
        add(Tag.Biology);
    }}),
    Shadowtwin(Actions.Buff, 40,3,60,false,new Spell() {
        @Override
        public void perform(final Unit host, int level){
            Sound.play("shadowstep");
            for (int i=0;i<host.rand.nextInt(level+1);i++) {
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
                p.stats.setStat("expire",new Stat(500+(level*100)));
                host.get("Stamina").reserve += 3;
                host.get("Stamina").add(0);
                p.brain = new IntelligenceSocial(p);
                p.dir = -host.dir;
                p.faction = host.faction;
                p.clip.setState(Actions.Buff);
            }
        }
    }, new Tags(){{
        add(Tag.Shadow);
        add(Tag.Conjuration);
    }}),
    Shadowstep(Actions.Conjure, 20,1,40,true,new Spell() {
        private int turn = 0;
        public void perform(Unit host, int level){
            if (turn++%4==0) Sound.play("shadow");
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
    }, new Tags(){{
        add(Tag.Shadow);
        add(Tag.Cunning);
    }}),
    Fireworks(Actions.Cast, 40,0,10,true,new Spell(){
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
    }, new Tags(){{
        add(Tag.Fire);
    }}),
    Fireblast(Actions.Cast, 40,4,60,true,new Spell(){
        private int turn = 0;
        @Override
        public void perform(final Unit host, final int level){
            if (turn++%8==0) Sound.play("fire");
            for (int i=0;i<3;i++) {
                final Projectile p = new Projectile("Flame", host.getHand(), host.target.getAdd(host.pos).getAdd(Global.r.nextInt(50)-25, Global.r.nextInt(50)-25), Global.r.nextInt(10+level)+6+level) {
                    @Override
                    public void stuck() {
                        Particle.explode("Flame", pos, new P(), 10, 10, 0, false, 4);
                        remove();
                    }
                    @Override
                    protected void onAttack(Unit p) {
                        p.stats.addEffect(Buffs.Burn, level);
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
    }, new Tags(){{
        add(Tag.Fire);
        add(Tag.Air);
    }}),
    Freeze(Actions.Cast, 40,2,24,true,new Spell(){
        private int turn = 0;
        @Override
        public void perform(final Unit host, final int level){
            if (turn++%6==0) Sound.play("ice");
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
        }
    }, new Tags(){{
        add(Tag.Cold);
        add(Tag.Air);
    }}),
    Flameroll(Actions.Cast, 40,5,120,false,new Spell(){
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
                void attackHitSound(){
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
    }, new Tags(){{
        add(Tag.Fire);
        add(Tag.Biology);
    }}),
    Fireball(Actions.Cast, 40,5,50,false,new Spell(){
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
                void attackHitSound(){
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
    }, new Tags(){{
        add(Tag.Fire);
        add(Tag.Conjuration);
    }}),
    Blizzard(Actions.Buff, 30,3,240,true,new Spell(){
        @Override
        public void perform(final Unit host, final int level){
            if (Global.r.nextInt(40) > (level*8)-4) return;
            if (Math.random() < .2f) Sound.play("ice");
            final Projectile p = new Projectile("Ice", host.pos.getAdd(Global.nextGaussian(500),-500), host.pos.getAdd(Global.nextGaussian(500),0), 8+Global.r.nextInt(6*level)){
                @Override
                protected void onAttack(Unit p){
                    p.buff(Buffs.Freeze, level+1);
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
    }, new Tags(){{
        add(Tag.Cold);
        add(Tag.Air);
        add(Tag.Mastery);
    }}),
    Meteor(Actions.Cast, 40,6,240,true,new Spell(){
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
    }, new Tags(){{
        add(Tag.Fire);
        add(Tag.Conjuration);
        add(Tag.Mastery);
    }});
    private static Spell produceBuff(final Buffs p){
        return new Spell(){
            public void perform(Unit host, int level){
                host.buff(p, level);
            }
        };
    }
    protected int cost;
    protected Actions action;
    protected int delay;
    protected Spell spell;
    protected String description;
    protected int cooldown;
    public boolean channel;
    Tags tags;

    Spells(Actions action, int del, int cost, int cooldown, boolean channel, Spell performer, Tags tags){
        this.action = action;
        this.delay = del;
        this.cost = cost;
        this.cooldown = cooldown;
        this.channel = channel;
        this.spell = performer;
        this.description = Dialog.get("mvsDsc"+name());
        this.tags = tags;
        this.tags.add(Tag.Arms);
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
    public float getVelX() {
        return 0;
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
        if (!channel || (Global.r.nextInt(Math.max(0,40-(cost*2))) == 0 && !host.get("Stamina").require(1))) host.stateSet(Actions.Idle);
    }

    @Override
    public void onSet(Unit p, int level){
    }

    @Override
    public void onFinish(Unit p){
    }

    @Override
    public void onInterrupt(Unit p){
    }

    @Override
    public void trigger(Unit p, int organ){
    }

    @Override
    public Tags getTags(){
        return tags;
    }
}
