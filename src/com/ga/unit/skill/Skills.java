package com.ga.unit.skill;

import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.tool.EventTimer;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.*;
import com.ga.unit.item.Item;
import com.ga.unit.item.Projectile;

import static com.ga.unit.BodyHumanoid.OFIST;

/**
 * Created by Ga on 7/28/2016.
 */
public enum Skills implements Moveset {
    Sprint(15,1,0,36,"Run faster using stamina.",new Spell(){
        public void perform(Unit host, int lvl){
            host.vel.x = host.getSpeed() * 2 * host.dir;
        }
    }, null),
    Duck(18,-1,0,6,"Duck down to dodge attacks.",null,null),
    Guard(23,-1,0,0,"Deflect an oncoming blow.",null,null),
    Lunge(0,-1,0,0,"Lunge at the target, dealing little damage but halting its advance.",null,basic()),
    Pierce(25,1,2.5f,0,"A piercing attack that attacks while keeping momentum. Velocity is added to the damage.",null,basic()),
    Swing(25,1,0,0,"Swing your weapon from top to bottom.",new Spell(){
        public void perform(Unit host, int level){
            if (host.delay.time <= 25 && host.delay.time > 12) {
                Item item = ((Man)host).weapon;
                int del = 25 - host.delay.time;
                if (host.delay.time == 13) host.attackSound();
                float rot = -((item.dir * 10) + del * del * .03f * -item.dir);
                if (Math.abs(rot) < 8){
                    float rot1 = rot;
                    float rot2 = item.rotation;
                    if (rot1 > rot2){
                        float t = rot1;
                        rot1 = rot2;
                        rot2 = t;
                    }
                    for (float i=rot1;i<rot2;i+=.2f){
                        item.rotation = i;
                        host.attack();
                    }
                }
                item.rotation = rot;
            }
        }
    }, null),
    Kick(3,-1,1,1,"A quick close range attack to push away enemies at no stamina cost.",null,new Spell(){
        public void perform(Unit host, int lvl){
            boolean man = host instanceof Man;
            if (man) Sound.play("swing_normal");
            else host.attackSound();
            for (Matter i : Updater.collide(host.clip.organs[lvl].ptd.getAdd(host.pos), 50)){
                if (i instanceof Unit) {
                    if (host.faction.canAttack(i.faction)) {
                        int dam;
                        if (man){
                            dam = (int) ((((Man)host).armor.armor[lvl].getDamage()+1)*host.damageMod);
                        } else dam = host.getDamage();
                        if (i.hittable()) ((Unit)i).hit(host, dam, host.damageType);
                        Sound.play("hit_whip");
                    }
                }
            }
        }
    }),
    Sidekick(18,-1,1,1,"A slow and damaging close range attack for when you run out of stamina.",null,new Spell(){
        public void perform(Unit host, int lvl){
            boolean man = host instanceof Man;
            if (man) Sound.play("swing_normal");
            else host.attackSound();
            for (Matter i : Updater.collide(host.clip.organs[lvl].ptd.getAdd(host.pos), 50)){
                if (i instanceof Unit) {
                    if (host.faction.canAttack(i.faction)) {
                        int dam;
                        if (man){
                            dam = (int) ((((Man)host).armor.armor[lvl].getDamage()+1)*host.damageMod);
                        } else dam = host.getDamage();
                        if (i.hittable()) ((Unit)i).hit(host, dam, host.damageType);
                        Sound.play("hit_whip");
                    }
                }
            }
        }
    }),
    Whirlwind(35, 5, 0, 60, "Swing your weapon back and forth. Costly, but quick and effective even to multiple enemies. Hits multiple times. [Level*3 seconds duration]", new Spell() {
        public void perform(Unit host, int level) {
            if (host.delay.time < 36) {
                Man t = (Man) host;
                t.weapon.rotation = host.delay.time * -t.weapon.dir;
            }
        }
    }, new Spell() {
        @Override
        public void perform(Unit host, int level) {
            if (host.delay.time % 4 == 0) host.attackSound();
            host.attack();
        }
    }),
    Stab(40,2,0,24,"Stab with your weapon, dealing enormous damage. [Damage:(2+Level)*100%]",null,basic()),
    Use(0,-1,1,0,"Use the current weapon's basic moveset.",null,null),
    Dash(35,1,0,24,"Dash through enemies with a piercing attack. [10*Level Distance] [Damage:.5*Level]",new Spell(){
        public void perform(Unit host, int level){
            if (host.vel.x > host.getSpeed()) host.attack();
        }
    },new Spell(){
        public void perform(Unit host, int lvl){
            host.attackSound();
            host.vel.set(host.getSpeed() * 10 * host.dir * (host.damageMod / 2), -5);
        }
    }),
    Rest(0,-1,0,0,"Resting restores stamina.",new Spell(){
        public void perform(Unit host, int lvl){
            if (host.stats.rest(4+lvl)) host.setState(Actions.Idle);
        }
    },null),
    Underhand(32,0,0,0,"A careful lunge from the waist. Successful stab refunds stamina.",null,new Spell(){
        public void perform(Unit host, int lvl){
            host.attackSound();
            host.vel.set(5*host.dir,-1);
            host.attack();
        }
    }),
    Throw(12,-1,0,1,"Throw your weapon to deal damage from afar.",null,new Spell(){
        public void perform(Unit host, int lvl){
            Sound.play("swing_normal");
            final Man t = (Man)host;
            if (!t.wieldingNone()) {
                Projectile p = new Projectile(null, t.pos.getAdd(t.clip.organs[OFIST].ptd), t.pos.getAdd(t.target), 50) {
                    Item wpn = t.weapon;
                    @Override
                    public void stuck() {
                        if (tangible) Particle.groundSlam(pos);
                        wpn.field();
                        wpn.pos = new P(pos);
                        wpn.rotation = rotation;
                        remove();
                    }
                };
                p.image = t.weapon.image;
                p.rotSpeed = .03f;
                p.faction = t.faction;
                p.damage = t.getDamage()+t.weapon.weight;
                t.weapon.remove();
                t.setWeapon(null);
            }
        }
    }),
    Dart(12,0,0,1,"Dart your weapon to deal damage from afar.",null,null),
    Shoot(27,-1,0,0,"Fire your weapon.",null,new Spell(){
        public void perform(Unit host, int lvl){
            Man t = (Man)host;
            P tip = t.weapon.getPoint();
            final Projectile p = new Projectile("Bullet",tip,t.pos.getAdd(t.target), (int) (150+(t.rand.nextFloat()*50))){
                @Override
                public void onCollide(Line l){
                    remove();
                }
            };
            p.delay = new EventTimer(4,false,new Performer(){
                public void perform(){
                    p.remove();
                }
            });
            p.delay.set(2);
            p.dir = t.dir;
            p.rotSpeed = 0;
            p.rotate(t.target.getSubtract(tip).add(t.pos));
            p.faction = t.faction;
            p.damage = (t.weapon.getDamage()*10)+4;
            p.gravity = false;
            Particle.explode("Spark", tip, p.vel, 5, 10, 15, false,3);
            float start = t.rand.nextFloat();
            p.pos.add(p.vel.getMult(start, start));
            t.weapon.rotation += t.dir * -.3f;
        }
    }),
    Snipe(27,-1,.5f,0,"Fire your weapon.",null,new Spell(){
        public void perform(Unit host, int lvl){
            Man t = (Man)host;
            P tip = t.weapon.getPoint();
            final Projectile p = new Projectile("Bullet",tip,t.pos.getAdd(t.target), (int) (150+(t.rand.nextFloat()*50))){
                @Override
                public void onCollide(Line l){
                    remove();
                }
            };
            p.delay = new EventTimer(4,false,new Performer(){
                public void perform(){
                    p.remove();
                }
            });
            p.dir = t.dir;
            p.rotSpeed = 0;
            p.rotate(t.target.getSubtract(tip).add(t.pos));
            p.faction = t.faction;
            p.damage = (t.weapon.getDamage()*10)+4;
            p.gravity = false;
            Particle.explode("Spark", tip, p.vel, 5, 10, 15, false,3);
            float start = t.rand.nextFloat();
            p.pos.add(p.vel.getMult(start, start));
            t.weapon.rotation += t.dir * -.3f;
        }
    }),
    Slash(60,2,0,40,"Slash forward twice, dealing massive damage. [Damage: (1+Level)*100%]",null,new Spell(){
        public void perform(Unit host, int lvl){
            Man t = (Man)host;
            t.attackSound();
            t.weapon.rotation = t.delay.time * -.3f * -t.weapon.dir;
            t.vel.set(t.dir * Global.cos(t.weapon.rotation)*15, -1);
            t.attack();
        }
    }),
    Jump(47,1,4f,0,"Jump into the air.",null,null),
    Airstrike(27,1,4f,4,"Jump into the air while attacking.",new Spell(){
        public void perform(Unit host, int level){
            if (host.jump){
                ((Man)host).weapon.rotation = 1f*host.dir;
                host.attack();
            } else {
                Man t = (Man)host;
                t.weapon.rotation = t.delay.time * -t.weapon.dir;
                if (host.delay.time % 4 == 0) host.attackSound();
            }
        }
    },null),
    Leap(40,2,3,60,"Perform a leap over great distances.",null,new Spell(){
        public void perform(Unit host, int lvl){
            if (host.jump) {
                Sound.play("jump");
                host.vel.y -= 80;
                host.vel.x = host.getSpeed() * 10 * host.dir;
                host.jump = false;
            }
        }
    }),
    Backstep(20,0,-1f,7,"Take a step backwards.",null,null);
    private static Spell basic(){
        return new Spell(){
            public void perform(Unit host, int lvl){
                host.attackSound();
                host.attack();
            }
        };
    }
    private String description;
    private int del, cooldown = 0;
    private float cost, velx;
    private Spell update, trigger;
    Skills(int del, float cost, float velx, int cooldown, String desc, Spell update, Spell trigger){
        this.del = del;
        this.cost = cost;
        this.velx = velx;
        this.cooldown = cooldown;
        this.description = desc;
        this.update = update;
        this.trigger = trigger;
    }
    public String getDescription(){
        String t = ""+((int)cost);
        return description + " [Cost:"+t+" "+"]";
    }
    public String getName(){
        return name();
    }
    public String getCodeName(){
        return "K"+name();
    }

    public float getCost() {
        return cost;
    }

    public float getVelX() {
        return velx;
    }

    @Override
    public int getDel() {
        return del;
    }

    @Override
    public int getCooldown() {
        return cooldown;
    }

    @Override
    public void update(Unit p, int level){
        if (update != null) update.perform(p, level);
    }

    @Override
    public void done(Unit p){
        p.setState(Actions.Idle);
    }

    public void trigger(Unit p, int p2){
        if (trigger != null) trigger.perform(p, p2);
    }

    public static Moveset getByName(String p) {
        for (Skills s : values()) {
            if (s.getName().equals(p)){
                return s;
            }
        }
        return null;
    }
}