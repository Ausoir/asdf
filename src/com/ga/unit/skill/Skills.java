package com.ga.unit.skill;

import com.ga.game.Dialog;
import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.Shader;
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
    Sprint(15,1,0,36,Dialog.get("mvsDscSprint"),new Spell(){
        public void perform(Unit host, int lvl){
            host.vel.x = host.getSpeed() * 2 * host.dir;
        }
    }, null),
    Duck(18,-1,0,6,Dialog.get("mvsDscDuck"),null,null),
    Guard(8,-1,0,0,Dialog.get("mvsDscGuard"),null,null),
    Lunge(0,-1,0,0,Dialog.get("mvsDscLunge"),null,basic()),
    Pierce(25,1,1.5f,0,"A piercing attack that attacks while keeping momentum. Velocity is added to the damage.",null,basic()),
    Swing(25,1,0,0,Dialog.get("mvsDscSwing"),new Spell(){
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
    Kick(3,-1,1,2, Dialog.get("mvsDscKick"),null,basic()),
    Sidekick(18,0,1,2,Dialog.get("mvsDscSidekick"),null,basic()),
    Whirlwind(35, 5, 0, 60, "Swing your weapon back and forth. Costly, but quick and effective even to multiple enemies. Hits multiple times. [Level*3 seconds duration]", new Spell() {
        public void perform(Unit host, int level) {
            if (host.delay.time < 36) {
                Man t = (Man) host;
                t.weapon.rotation = host.delay.time * -t.weapon.dir;
                host.attack();
            }
        }
    }, new Spell() {
        @Override
        public void perform(Unit host, int level) {
            if (host.delay.time % 4 == 0) host.attackSound();
        }
    }),
    Stab(40,2,0,24,Dialog.get("mvsDscStab"),null,basic()),
    Dash(35,1,0,24,Dialog.get("mvsDscDash"),new Spell(){
        public void perform(Unit host, int level){
            if (host.vel.x > host.getSpeed()) host.attack();
        }
    },new Spell(){
        public void perform(Unit host, int lvl){
            host.attackSound();
            host.vel.set(host.getSpeed() * 10 * host.dir * (host.damageMod / 2), -5);
        }
    }),
    Rest(0,-1,0,0,Dialog.get("mvsDscRest"),new Spell(){
        public void perform(Unit host, int lvl){
            if (host.stats.rest(4+lvl)) host.setState(Actions.Idle);
        }
    },null),
    Sneak(3,-1,0,0,Dialog.get("mvsDscSneak"),new Spell(){
        public void perform(Unit host, int lvl){
            host.stats.rest(2+lvl);
        }
    },null){
        public void onSet(Unit p){
            p.clip.setShade(Shader.Gray);
            p.setDepth(3);
        }
        public void onFinish(Unit p){
        }
        public void onInterrupt(Unit p){
            p.clip.setShade(Shader.XYLinearGradient);
            p.setDepth(10);
        }
    },
    Underhand(32,0,0,0,Dialog.get("mvsDscUnderhand"),null,new Spell(){
        public void perform(Unit host, int lvl){
            host.attackSound();
            host.vel.set(5*host.dir,-1);
            host.attack();
        }
    }),
    Throw(12,-1,0,0,Dialog.get("mvsDscThrow"),null,new Spell(){
        public void perform(Unit host, int lvl){
            final Man t = (Man)host;
            if (!t.wieldingNone()) {
                Sound.play("swing_normal");
                Projectile p = new Projectile(null, t.pos.getAdd(t.clip.organs[OFIST].ptd), t.pos.getAdd(t.target), 50) {
                    private Item wpn = t.weapon;
                    @Override
                    public void stuck() {
                        if (tangible) Particle.groundSlam(pos);
                        wpn.gravity = true;
                        remove();
                    }
                    public void update(){
                        super.update();
                        wpn.rotation = rotation;
                    }
                };
                p.gravity = true;
                p.rotSpeed = .03f;
                p.faction = t.faction;
                p.damage = t.getDamage()+t.weapon.weight;
                Item item = t.weapon;
                t.setWeapon(null);
                item.pos = p.pos;
                item.gravity = false;
            }
        }
    }),
    Dart(12,0,0,1,Dialog.get("mvsDscDart"),null,null),
    Shoot(27,-1,0,0,Dialog.get("mvsDscShoot"),null,new Spell(){
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
            Particle.explode("Spark", tip, p.vel, 5, 10, 15, false,3);
            float start = t.rand.nextFloat();
            p.pos.add(p.vel.getMult(start, start));
            t.weapon.rotation += t.dir * -.3f;
        }
    }),
    Slash(60,2,0,40,Dialog.get("mvsDscSlash"),null,new Spell(){
        public void perform(Unit host, int lvl){
            Man t = (Man)host;
            t.attackSound();
            t.weapon.rotation = t.delay.time * -.3f * -t.weapon.dir;
            t.vel.set(t.dir * Global.cos(t.weapon.rotation)*15, -1);
            t.attack();
        }
    }),
    Jump(47,1,4f,0,"Jump into the air.",null,null),
    Airstrike(27,1,1.5f,4,"Jump into the air while attacking.",new Spell(){
        public void perform(Unit host, int level){
            if (host.jump){
                ((Man)host).weapon.rotation = 1f*host.dir;
                host.delay.time = Math.min(host.delay.time, 4);
            } else {
                Man t = (Man)host;
                t.weapon.rotation = t.delay.time * -t.weapon.dir;
                if (host.delay.time % 4 == 0) host.attackSound();
                host.attack();
            }
        }
    },null),
    Leap(40,2,3,60,Dialog.get("mvsDscLeap"),null,new Spell(){
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
    public void onSet(Unit p){
    }

    @Override
    public void onFinish(Unit p){
        p.setState(Actions.Idle);
    }

    public void trigger(Unit p, int p2){
        if (trigger != null) trigger.perform(p, p2);
    }

    public void onInterrupt(Unit p){
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