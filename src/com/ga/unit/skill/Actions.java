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
import com.ga.unit.intelligence.Intelligence;
import com.ga.unit.item.Item;
import com.ga.unit.item.Projectile;

import java.awt.*;
import java.util.ArrayList;

import static com.ga.unit.BodyHumanoid.OFIST;

/**
 * Created by Ga on 7/28/2016. asd
 */
public enum Actions {
    Idle{
        public void onSet(Unit p){
            p.attackSpeed = -1;
        }
        public void update(Unit p){
            if (Math.abs(p.vel.x) < .5f) p.vel.x = 0;
        }
    }, Walk(0,-1,0,0,new Tags(){{
        add(Tag.Athletics);
    }}){
        public void update(Unit p){
            p.vel.x += p.getSpeed()*p.dir;
        }
    }, Climb{
        public void onSet(Unit p){
            p.vel.x = 0;
        }
        public void update(Unit p){
            if (p.clip.organs[0].ptd.y + p.pos.y < ((Man)p).rope.pt.y){
                p.vel.x = 2*p.dir;
                p.stateSet(Jump);
            } else p.vel.y = -p.getSpeed()*24;
        }
    }, Drop(12){
        public void onSet(Unit p){
            p.attackSpeed = -1;
            if (p.lastLand.opt != 2 && Global.map.checkLand(new Line(p.pos.getAdd(0,4), p.pos.getAdd(0,1200)), p.lastLand.depth) != null){
                p.setDepth(100);
                p.pos.y+=4;
            }
        }
    }, Hit(14){
        public void onSet(Unit p){
            p.attackSpeed = -1;
            p.clip.organs[p.clip.BODY].pt.y += 14;
        }
        public void onInterrupt(Unit p){
            onFinish(p);
        }
        public void onFinish(Unit p){
            p.clip.setHeight();
            super.onFinish(p);
        }
        public void update(Unit p) {
            p.stats.rest(4);
        }
    }, Dead(-1){
        public void onSet(final Unit p){
            p.attackSpeed = -1;
            p.clip.organs[p.clip.BODY].pt.y = 0;
        }
        public void onInterrupt(Unit p){
            onFinish(p);
        }
        public void onFinish(Unit p){
            p.clip.setHeight(); //clip animation too short
            super.onFinish(p);
        }
    }, Down(80){
        public void onSet(final Unit p){
            p.attackSpeed = -1;
            p.clip.organs[p.clip.BODY].pt.y = 0;
            p.delay.time += Global.r.nextInt(120);
        }
        public void onInterrupt(Unit p){
            onFinish(p);
        }
        public void onFinish(Unit p){
            p.clip.setHeight(); //clip animation too short
            super.onFinish(p);
        }
    }, Wield(18){
    }, Conjure{
    }, Use{
        public void onFinish(Unit p){
            Man t = (Man)p;
            t.weapon.use((Man) p);
        }
    }, WalkL{
    }, Consume(8){
    }, Exhausted{
    }, Interact(8){
    }, Grab(8){
    }, Swap(4){
    }, Buff{
        public void onFinish(Unit p){}
    }, Cast{
        public void onFinish(Unit p){}
    }, Sprint(15,-1,5,0,new Tags(){{
        add(Tag.Athletics);
    }}){
        public void onSet(Unit p){
            p.attackSpeed = -1;
        }
        public void update(Unit host){
            host.vel.x += host.getSpeed() * 2 * host.dir;
            if (host.stateDuration % (20+host.stateLevel*4) == 0 && !host.get("Stamina").require(1))
                host.setState(Actions.Walk);
        }
        public void onFinish(Unit p){}
    },
    Melee(24,-1,0,0,new Tags(){{
    }}){
        public void trigger(Unit p){
            p.attack();
        }
    },
    Duck(24,-1,6,0,new Tags(){{
        add(Tag.Finesse);
    }}){
        public String getClipName(){
            return "Conjure";
        }
        public void onSet(Unit p){
            p.attackSpeed = -1;
        }
    },
    Guard(8,-1,0,0,new Tags(){{
        add(Tag.Combat);
        add(Tag.Aiming);
    }}){
        public void onSet(Unit p){
            ((Man)p).weapon.rotation = p.target.getAtan();
            p.attackSpeed = -1;
        }
    },
    Thrust(25,2,0,0,new Tags(){{
        add(Tag.Combat);
        add(Tag.Aiming);
    }}){
        public void onSet(Unit p){
            if (p.vel.x < 0 != p.dir < 0) p.vel.x = 0;
            p.damageMod = .8f + (Math.abs(p.vel.x) / 10);
        }
        public void trigger(Unit p){
            ((Man)p).weapon.rotateTo(p.target.getMult(p.dir, p.dir));
            p.attackSound();
            p.attack();
        }
    },
    Slash(25,4,0,0,new Tags(){{
        add(Tag.Combat);
        add(Tag.Might);
    }}){
        public void onSet(Unit p){
            Item weapon = ((Man)p).weapon;
            weapon.rotation = -(weapon.dir * 10);
            p.damageMod = 1;
        }
        public void update(Unit p){
            if (p.delay.time <= 25 && p.delay.time > 12) {
                if (p.delay.time == 17) p.vel.x = 4*p.dir;
                Item item = ((Man)p).weapon;
                int del = 25 - p.delay.time;
                if (p.delay.time == 13) p.attackSound();
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
                        if (p.attack() != null) {
                            p.damageMod = 0;
                            break;
                        }
                    }
                }
                item.rotation = rot;
            }
        }
    },
    Kick(3,-1,2,0,new Tags(){{
        add(Tag.Combat);
    }}){
        public void onSet(Unit p){
            p.attackSpeed = -1;
        }
        public void trigger(Unit p){
            p.attackSound();
            Man t = (Man)p;
            Line hitbox = t.clip.organs[1].getAdd(t.pos);
            for (Unit i : Updater.collideUnit(t.pos, (int) hitbox.getLength(), t.faction)) {
                i.stateSet(Actions.Hit);
                Sound.play("hit_whip");
                new Thing("hit_0", -1, 150){
                    private int n = 0;
                    public void draw(Graphics2D g){
                        if (n <= 7) {
                            super.draw(g);
                            setImage("hit_" +(n++)/2);
                            if (n > 7) remove();
                        }
                    }
                }.pos = hitbox.ptd;
                i.vel.x = (p.pos.x > i.pos.x ? -1 : 1)*6;
                i.vel.y -= 6;
            }
        }
    },
    Sidekick(18,1,0,0,new Tags(){{
        add(Tag.Combat);
        add(Tag.Might);
    }}){
        public void onSet(Unit p){
            p.attackSpeed = -1;
        }
        public void trigger(Unit p){
            p.attackSound();
            Man t = (Man)p;
            Line hitbox = t.clip.organs[2].getAdd(t.pos);
            int damage = (int)(t.armor.armor[1].getDamage()+p.stateLevel*.5f)+2;
            for (Unit i : Updater.collideUnit(t.pos, (int) hitbox.getLength(), t.faction)) {
                if (i.checkHit(hitbox, damage)) {
                    Sound.play("hit_bash");
                    new Thing("hit_1", -1, 150) {
                        private int n = 0;

                        public void draw(Graphics2D g) {
                            if (n <= 7) {
                                super.draw(g);
                                setImage("hit_" + (n++) / 2);
                                if (n > 7) remove();
                            }
                        }
                    }.pos = hitbox.ptd;
                    Particle.explode("Fire", hitbox.ptd, new P(), 6, 12, 0, false, 4);
                }
            }
        }
    },
    Punch(12,1,0,0,new Tags(){{
        add(Tag.Combat);
        add(Tag.Might);
    }}){
        public String getClipName(){
            return "Thrust";
        }
        public void onSet(Unit p){
            p.attackSpeed = -1;
        }
        public void trigger(Unit p){
            p.attackSound();
            Man t = (Man)p;
            Line hitbox = t.clip.organs[0].getAdd(t.pos);
            int damage = Global.r.nextInt((int) (t.armor.armor[2].getDamage()+p.stateLevel*.5f)+1);
            for (Unit i : Updater.collideUnit(t.pos, (int) hitbox.getLength(), t.faction)) {
                if (i.checkHit(hitbox, damage)) {
                    Sound.play("hit_punch");
                    new Thing("hit_1", -1, 150) {
                        private int n = 0;
                        public void draw(Graphics2D g) {
                            if (n <= 7) {
                                super.draw(g);
                                setImage("hit_" + (n++) / 2);
                                if (n > 7) remove();
                            }
                        }
                    }.pos = hitbox.ptd;
                }
            }
        }
    },
    Whirlwind(35, 8, 40, 4, new Tags(){{
        add(Tag.Combat);
        add(Tag.Might);
    }}){
        public void onSet(final Unit p){
            p.delay.time += 8 * p.stateLevel;
            p.damageMod = 1 + ((float) p.stateLevel * .5f);
            p.delay.performer = new Performer(){
                public void perform(){
                    p.setState(Actions.Rest);
                }
            };
        }
        public void update(Unit p) {
            if (p.delay.time < 36) {
                Man t = (Man) p;
                t.weapon.rotation = p.delay.time * -t.weapon.dir;
                p.attack();
            }
        }
        public void trigger(Unit p){
            if (p.delay.time % 4 == 0) p.attackSound();
        }
    },
    Stab(40,2,24,5,new Tags(){{
        add(Tag.Aiming);
        add(Tag.Cunning);
    }}){
        public void onSet(Unit p){
            p.damageMod = 4 + p.stateLevel;
        }
        public void trigger(Unit p){
            p.attackSound();
            p.attack();
        }
    },
    Dash(35,6,24,2,new Tags(){{
        add(Tag.Aiming);
        add(Tag.Athletics);
        add(Tag.Finesse);
    }}){
        public void onSet(Unit p){
            p.damageMod = p.stateLevel * .5f;
            p.vel.x -= p.getSpeed() * 6 * p.dir;
            p.delay.time += 2 * p.stateLevel;
            p.delay.max += 2 * p.stateLevel;
        }
        public void update(Unit p) {
            if (p.vel.x > 0 == p.dir > 0 && Math.abs(p.vel.x) >= p.getSpeed() * 4) p.attack();
        }
        public void trigger(Unit p){
            p.attackSound();
            p.vel.set(20 * p.dir * (1+(p.stateLevel*.2f)), -5);
            ((Man)p).weapon.rotateTo(p.target.getMult(p.dir, p.dir));
        }
    },
    Rest(0,-1,0,1,new Tags(){{
        add(Tag.Athletics);
    }}){
        public void onSet(Unit p){
            p.get("Stamina").add(-1, false);
        }
        public void update(Unit p) {
            p.stats.rest(4+p.stateLevel);
            if (p.get("Stamina").isFull()) p.stateSet(Idle);
        }
    },
    Sneak(3,-1,0,2,new Tags(){{
        add(Tag.Cunning);
    }}){
        public String getClipName(){
            return "Conjure";
        }
        public void update(Unit p) {
            p.stats.rest(2+p.stateLevel);
        }
        public void onSet(Unit p){
            p.clip.setShade(Shader.Color,new Color(0, 0, 0, 180).getRGB());
        }
        public void onFinish(Unit p){
        }
        public void onInterrupt(Unit p){
            p.clip.setShade(Shader.XYLinearGradient,0);
        }
    },
    Lunge(24,0,12,3,new Tags(){{
        add(Tag.Finesse);
    }}){
        public void onSet(Unit p){
            p.damageMod = 0;
        }
        public void trigger(Unit p){
            p.attackSound();
            p.vel.set(8*p.dir,-1);
            ((Man)p).weapon.rotateTo(p.target.getMult(p.dir, p.dir));
            Unit tgt = p.attack();
            if (tgt != null){
                tgt.buff(Buffs.Stun, Global.r.nextInt(1+p.stateLevel));
            }
        }
    },
    Underhand(32,0,24,4,new Tags(){{
        add(Tag.Combat);
        add(Tag.Finesse);
    }}){
        public void onSet(Unit p){
            p.damageMod = 2 + p.stateLevel/2;
        }
        public void trigger(Unit p){
            Man t = (Man) p;
            p.attackSound();
            p.vel.set(5*p.dir,-1);
            t.weapon.rotateTo(p.target.getMult(p.dir, p.dir));
            Unit tgt = p.attack();
            if (tgt != null){
                Particle.explode("Fire", t.weapon.getLine().ptd, new P(), 6, 12, 0, false, 4);
            }
        }
    },
    Mark(80,-1,17,3,new Tags(){{
        add(Tag.Aiming);
        add(Tag.Cunning);
    }}){
        public void onSet(Unit p){
            ArrayList<Unit> t = Updater.collideUnit(p.target.getAdd(p.pos),40,Faction.Destructive);
            if (t.size() > 0) t.get(0).buff(Buffs.Mark, p.stateLevel);
        }
    },
    Identify(1,-1,0,0,new Tags(){{
    }}){
        public void onSet(Unit p){
            if (p == Global.player) {
                ArrayList<Unit> t = Updater.collideUnit(p.target.getAdd(p.pos), 40, Faction.Destructive);
                if (t.size() > 0){
                    Unit target = t.get(0);
                    Global.display.menuDialog.show(target.name,target.getDescription());
                }
            }
        }
    },
    Throw(12,-1,0,0,new Tags(){{
        add(Tag.Aiming);
    }}){
        public String getClipName(){
            return "Thrust";
        }
        public void trigger(Unit p){
            final Man t = (Man)p;
            t.damageMod = p.stateLevel*.3f;
            if (!t.wieldingNone()) {
                int speed = 5;
                int damage = 0;
                if (t.weapon.weight != 0 && t.get("Stamina").require((int) (t.weapon.weight*1.5f))){
                    speed = 20+(5*Math.max(1,t.get("Strength").get()-t.weapon.weight));
                    damage = t.getDamage()+t.weapon.weight;
                }
                Projectile pj = new Projectile(null, t.pos.getAdd(t.clip.organs[OFIST].ptd), t.pos.getAdd(t.target), speed) {
                    private Item wpn = t.weapon;
                    @Override
                    public void stuck() {
                        if (tangible) Particle.groundSlam(pos);
                        wpn.gravity = true;
                        wpn.onField();
                        remove();
                    }
                    public void onAttack(Unit p) {
                        if (p.checkHit(new Line(pos,pos.getAdd(vel)),damage)){
                            if (wpn.usable(1)) wpn.trigger(p);
                            Sound.play("hit_bash");
                        }
                    }
                    public void update(){
                        super.update();
                        wpn.rotation = rotation;
                    }
                };
                pj.gravity = true;
                pj.rotSpeed = speed*.01f;
                pj.faction = t.faction;
                pj.damage = damage;
                Item item = t.weapon;
                t.setWeapon(null);
                item.pos = pj.pos;
                item.gravity = false;
            }
        }
    },
    Dart(12,0,1,6,new Tags(){{
        add(Tag.Aiming);
    }}),
    Aim(120,2,0,3,new Tags(){{
        add(Tag.Aiming);
    }}){
        public void onSet(Unit p){
            Man t = (Man)p;
            t.weapon.rotateTo(t.target.getMult(t.dir,t.dir));
            t.weapon.rotation += .1 * t.dir;
        }
        public void trigger(Unit p){
            Man t = (Man)p;
            P tip = t.weapon.getLine().ptd;
            final Item wpn = t.weapon;
            int speed = (int) (35+(p.get("Strength").get()*10)+(t.rand.nextFloat()*(60/p.stateLevel)));
            final Projectile pj = new Projectile("Arrow",tip,t.pos.getAdd(t.target), speed){
                public void onAttack(Unit p){
                    wpn.trigger(p);
                    Sound.play("hit_bloody");
                    remove();
                }
                public void update(){
                    super.update();
                    if (tangible || lastLand.depth == 0) rotateTo(vel);
                }
            };
            pj.setDepth(t.getDepth());
            pj.rotSpeed = 0;
            pj.rotation = pj.vel.getAtan();
            pj.gravity = true;
            pj.faction = t.faction;
            pj.damage = t.weapon.getDamage() * 3;
            t.weapon.rotation += t.dir * .3f;
            p.stateSet(Actions.Idle);
        }
    },
    Shoot(27,-1,2,8,new Tags(){{
        add(Tag.Aiming);
    }}){
        public void onSet(Unit p){
            Man t = (Man)p;
            t.weapon.rotateTo(t.target.getMult(t.dir, t.dir));
            t.weapon.rotation += .1 * t.dir;
        }
        public void trigger(Unit p){
            Man t = (Man)p;
            P tip = t.weapon.getLine().ptd;
            final Projectile pj = new Projectile("Bullet",tip,t.pos.getAdd(t.target), (int) (150+(t.rand.nextFloat()*50))){
                @Override
                public void onCollide(Line l){
                    remove();
                }
            };
            pj.delay = new EventTimer(2,false,new Performer(){
                public void perform(){
                    pj.remove();
                }
            });
            pj.rotSpeed = 0;
            pj.rotateTo(t.target.getSubtract(tip).add(t.pos));
            pj.faction = t.faction;
            pj.damage = t.weapon.getDamage();
            Particle.explode("Spark", tip, pj.vel, 5, 10, 15, false,3);
            float start = t.rand.nextFloat();
            pj.pos.add(pj.vel.getMult(start, start));
            t.weapon.rotation += t.dir * -.3f;
        }
    },
    Snipe(27,-1,0,8,new Tags(){{
        add(Tag.Aiming);
    }}){
        public void onSet(Unit p){
            Man t = (Man)p;
            t.weapon.rotateTo(t.target.getMult(t.dir,t.dir));
            t.weapon.rotation += .1 * t.dir;
        }
        public void trigger(Unit p){
            Man t = (Man)p;
            P tip = t.weapon.getLine().ptd;
            final Projectile pj = new Projectile("Bullet",tip,t.pos.getAdd(t.target), (int) (150+(t.rand.nextFloat()*50))){
                @Override
                public void onCollide(Line l){
                    remove();
                }
            };
            pj.delay = new EventTimer(4,false,new Performer(){
                public void perform(){
                    pj.remove();
                }
            });
            pj.dir = t.dir;
            pj.rotSpeed = 0;
            pj.rotateTo(t.target.getSubtract(tip).add(t.pos));
            pj.faction = t.faction;
            pj.damage = t.weapon.getDamage();
            Particle.explode("Spark", tip, pj.vel, 5, 10, 15, false,3);
            float start = t.rand.nextFloat();
            pj.pos.add(pj.vel.getMult(start, start));
            t.weapon.rotation += t.dir * -.3f;
        }
    },
    Jump(47,1,0,0,new Tags(){{
        add(Tag.Athletics);
    }}){
        public void onSet(Unit p){
            float x = Math.abs(p.vel.x);
            float t = Math.max(0, 36 + p.stateLevel*2 - x);
            Sound.play("jump");
            p.vel.y -= t;
            p.vel.x *= 5;
            if (Math.abs(p.vel.x) >= -p.vel.y) p.stateSet(Down);
            p.attackSpeed = -1;
        }
    },
    Warcry(60,10,40,4,new Tags(){{
        add(Tag.Might);
    }}){
        public void onSet(Unit p){
            ArrayList<Unit> t = Updater.collideUnit(p.pos,240+(120*p.stateLevel),Faction.Destructive);
            for (Unit i : t){
                if (i != p) i.buff(Buffs.Stun,3+p.stateLevel);
            }
        }
    },
    Evade(40,4,27,2,new Tags(){{
        add(Tag.Shadow);
        add(Tag.Finesse);
    }}){
        public void onSet(Unit p){
            p.buff(Buffs.Evade,1+p.stateLevel);
        }
    },
    Cleave(40,-1,27,1,new Tags(){{
        add(Tag.Buff);
        add(Tag.Might);
    }}){
        public void onSet(Unit p){
            p.buff(Buffs.Cleave,p.stateLevel);
        }
    },
    Airstrike(27,1,4,2,new Tags(){{
        add(Tag.Aiming);
        add(Tag.Combat);
        add(Tag.Finesse);
    }}){
        public void onSet(Unit p){
            p.damageMod = 1 + p.stateLevel * .5f;
            p.attackSpeed = -1;
            p.vel.y -= 20;
        }
        public void update(Unit p){
            Man t = (Man)p;
            if (p.jump){
                t.weapon.rotation = 1f*p.dir;
                t.stateSet(Rest);
                t.damageMod *= 2;
                t.attack();
            } else {
                t.weapon.rotation = t.delay.time * -t.weapon.dir;
                if (p.delay.time % 4 == 0) p.attackSound();
                p.attack();
            }
        }
        public void onFinish(Unit p){
            Stat stamina = p.get("Stamina");
            int b4 = stamina.get();
            if (!stamina.require(8)){
                p.stateSet(Rest);
                p.delay.time = (8-b4)*16;
            }
        }
    },
    Leap(40,2,40,4,new Tags(){{
        add(Tag.Might);
    }}){
        public void trigger(Unit p){
            Sound.play("jump");
            p.vel.y -= 80;
            p.vel.x = 80 * p.dir;
        }
    },
    Backstep(20,1,12,0,new Tags(){{
        add(Tag.Athletics);
        add(Tag.Finesse);
    }}){
        public void onSet(Unit p){
            p.vel.x = -p.dir*20;
            p.vel.y -= 10;
        }
    },
    Innersword(18,3,21,1,new Tags(){{
        add(Tag.Conjuration);
        add(Tag.Buff);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void onSet(Unit host){
            host.attackSpeed = 12;
        }
        public void trigger(final Unit host){
            Man man = (Man)host;
            if (man.wieldingNone()) {
                Sound.play("shadowstep");
                final Item t = (Item) Stock.getByName("Ethersword", -1);
                man.setWeapon(t);
                int duration = 2;
                if (host.stateLevel == 2) duration = 4;
                if (host.stateLevel == 3) duration = 16;
                if (host.stateLevel >= 4) duration = 256;
                Global.timers.add(new EventTimer(duration,false,new Performer(){
                    public void perform(){
                        ((Man) host).setWeapon(null);
                        t.remove();
                    }
                }));
            }
        }
    },
    Shock(40,4,7,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Energy);
        add(Tag.Air);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
        }
        public void trigger(Unit host){
            Particle.explode("Glare",host.getHand(),new P(0,-1),8,20,12,true,4);
            Sound.play("shadowstep");
            P origin = host.getHand();
            ArrayList<Unit> t = Updater.collideUnit(origin,800,Faction.Destructive);
            int size = t.size();
            if (size > 0) {
                Unit target = t.get(Global.r.nextInt(size));
                P targetPos = target.getRandomPoint();
                host.target = targetPos.getSubtract(host.pos);
                int num = Global.r.nextInt(6) + 3;
                float div = host.target.getDist() / (num - 1);
                float atan = host.target.getAdd(host.pos.getSubtract(origin)).getAtan();
                P[] points = new P[num * 2 - 2];
                for (int ii = 0; ii < Global.r.nextInt(2) + 2; ii++) {
                    Terrain lightning = new Terrain("yellow", Shader.None, 50){
                        public void draw(Graphics2D g) {
                            super.draw(g);
                            Updater.remove(this);
                        }
                    };
                    for (int i = 0; i < points.length; i++) {
                        if (i < num) {
                            float x = Global.cos(atan) * div * i, y = Global.sin(atan) * div * i;
                            if (i != 0 && i != num - 1) {
                                float var = (float) (Math.random() * 64 - 32);
                                x += var * Global.cos(atan + 1.57f);
                                y += var * Global.sin(atan + 1.57f);
                            }
                            points[i] = new P(x, y);
                        } else {
                            points[i] = new P(points[points.length - i]).getAdd(0, 6);
                        }
                        lightning.addPoint(origin.x + points[i].x, origin.y + points[i].y);
                    }
                    lightning.render();
                }
                Particle.explode("Glare",targetPos,new P(0,-1),20,20,12,true,7);
                target.buff(Buffs.Shock, 3);
                target.buff(Buffs.Stun, 1);
            }
        }
    },
    Lightning(40,0,7,3,new Tags(){{
        add(Tag.Runic);
        add(Tag.Energy);
        add(Tag.Channeling);
        add(Tag.Air);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
        }
        public void onFinish(Unit host){}
        public void trigger(Unit host){
            Particle.explode("Glare",host.getHand(),new P(0,-1),20,20,12,true,4);
        }
        public void update(Unit host){
            if (host.delay.time > 0) return;
            Sound.play("shadowstep");
            P origin = host.getHand();
            ArrayList<Unit> t = Updater.collideUnit(origin,800,host.faction);
            int size = t.size();
            if (size > 0) {
                Unit target = t.get(Global.r.nextInt(size));
                P targetPos = target.getRandomPoint();
                host.target = targetPos.getSubtract(host.pos);
                int num = Global.r.nextInt(6) + 3;
                float div = host.target.getDist() / (num - 1);
                float atan = host.target.getAdd(host.pos.getSubtract(origin)).getAtan();
                P[] points = new P[num * 2 - 2];
                for (int ii=0;ii<2;ii++) {
                    Terrain lightning = new Terrain("yellow", Shader.None, 50) {
                        public void draw(Graphics2D g) {
                            super.draw(g);
                            Updater.remove(this);
                        }
                    };
                    for (int i = 0; i < points.length; i++) {
                        if (i < num) {
                            float x = Global.cos(atan) * div * i, y = Global.sin(atan) * div * i;
                            if (i != 0 && i != num - 1) {
                                float var = (float) (Math.random() * 64 - 32);
                                x += var * Global.cos(atan + 1.57f);
                                y += var * Global.sin(atan + 1.57f);
                            }
                            points[i] = new P(x, y);
                        } else {
                            points[i] = new P(points[points.length - i]).getAdd(0, 6);
                        }
                        lightning.addPoint(origin.x + points[i].x, origin.y + points[i].y);
                    }
                    lightning.render();
                }
                Particle.holyExplosion(targetPos);
                target.buff(Buffs.Shock, 1);
            }
            if (host.stateDuration % (12+host.stateLevel*2) == 0 && !host.get("Stamina").require(1))
                host.setState(Actions.Walk);
        }
    },
    Materialize(40,4,4,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Conjuration);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
        }
        public void trigger(Unit host){
            Sound.play("shadowstep");
            final Terrain r = new Terrain("water", Shader.None, 50);
            P[] points = new P[3];
            int power = 15+(50*host.stateLevel);
            final P target = host.target.getAdd(host.pos);
            for (int i=0;i<3;i++){
                points[i] = target.getAdd(Global.r.nextInt(power)-(power/2),Global.r.nextInt(power)-(power/2));
                r.addPoint(points[i].x, points[i].y);
            }
            final Line[] lines = new Line[3];
            for (int i=0;i<3;i++) {
                lines[i] = new Line(points[i], points[i+1<3?i+1:0]) {
                    public String toString() {
                        return "";
                    }
                };
                lines[i].depth = host.getDepth();
                Global.map.land.add(lines[i]);
            }
            r.render();
            Global.timers.add(new EventTimer(host.stateLevel*2,false,new Performer(){
                public void perform(){
                    Particle.holyExplosion(target);
                    for (int i = 0;i<3;i++) {
                        Global.map.land.remove(lines[i]);
                    }
                    Updater.remove(r);
                }
            }));
        }
    },
    Focus(15, -1, 24, 1, new Tags(){{
        add(Tag.Buff);
        add(Tag.Combat);
    }}){
        public String getClipName(){
            return "Conjure";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
        }
        public void trigger(Unit host){
            Sound.play("heal");
            host.get("Stamina").add(2 + host.stateLevel * host.stateLevel, false);
        }
    },
    Rejuvenate(40, 1, 40, 1, new Tags(){{
        add(Tag.Buff);
        add(Tag.Biology);
    }}){
        public String getClipName(){
            return "Buff";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
        }
        public void onFinish(Unit host){
            host.buff(Buffs.Rejuvenate, host.stateLevel);
        }
    },
    Frenzy(40, -1, 3, 1, new Tags(){{
        add(Tag.Buff);
        add(Tag.Biology);
    }}){
        public String getClipName(){
            return "Buff";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
        }
        public void onFinish(Unit host){
            host.buff(Buffs.Frenzy, host.stateLevel);
        }
    },
    Windwalk(25,2,20,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Buff);
        add(Tag.Air);
    }}){
        public String getClipName(){
            return "Conjure";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
        }
        public void trigger(Unit host){
            host.buff(Buffs.Haste, host.stateLevel);
        }
    },
    Glacialwalk(10,4,20,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Buff);
        add(Tag.Cold);
    }}){
        public String getClipName(){
            return "Buff";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
        }
        public void onFinish(Unit host){
            host.buff(Buffs.Glacialwalk, host.stateLevel);
        }
    },
    Stun(40,4,40,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Debuff);
        add(Tag.Cunning);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
        }
        public void trigger(Unit host){
            ArrayList<Unit> t = Updater.collideUnit(host.target.getAdd(host.pos),40,Faction.Destructive);
            if (t.size() > 0) t.get(0).buff(Buffs.Stun,host.stateLevel);
        }
    },
    Calling(40,2,4,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Conjuration);
        add(Tag.Biology);
    }}){
        public String getClipName(){
            return "Buff";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
        }
        public void update(Unit host){
            if (host.delay.time < 5) {
                Sound.play("shadowstep");
                for (int i = 0; i < host.rand.nextInt(host.stateLevel + 4); i++) {
                    Unit p = (Unit) Stock.getByName("Dog", -1);
                    p.pos.set(host.pos);
                    p.brain = new Intelligence(p);
                    p.dir = host.dir;
                    if (host.brain != null) {
                        p.brain.target = host.brain.target;
                    }
                    p.buff(Buffs.Banishment, 2 + (int) (host.stateLevel * .4f));
                    p.faction = host.faction;
                }
            }
        }
    },
    Shadowtwin(40,3,60,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Shadow);
        add(Tag.Conjuration);
    }}){
        public String getClipName(){
            return "Buff";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
        }
        public void trigger(final Unit host){
            Sound.play("shadowstep");
            for (int i=0;i<host.rand.nextInt(host.stateLevel+1);i++) {
                Man p = new Man(host.seed) {
                    @Override
                    public void die() {
                        super.die();
                        remove();
                        Particle.explode("Smoke", pos, new P(), 80, 10, 0, true, 80);
                    }
                };
                p.load(host.toString());
                p.setHead("Phantom");
                p.brain = new Intelligence(p);
                p.buff(Buffs.Banishment,4+(int)(host.stateLevel*.4f));
                p.dir = -host.dir;
                p.faction = host.faction;
                p.clip.setState("Buff");
            }
        }
    },
    Blink(20,1,0,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Shadow);
        add(Tag.Air);
    }}){
        public String getClipName(){
            return "Conjure";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
            Sound.play("shadow");
            Particle.smoke(host.pos);
            for (int i=0;i<24;i++) {
                int range = Global.r.nextInt(host.stateLevel + 320) + 128;
                float rot = Global.r.nextFloat() * 6.28318f - 3.14159f;
                Line to = new Line(host.pos, host.pos.getAdd(Global.cos(rot) * range, Global.sin(rot) * range));
                if (Global.map.checkLand(new Line(to.ptd, to.ptd.getAdd(0, 400)), 0) != null) {
                    host.pos.set(to.ptd);
                    break;
                }
            }
        }
    },
    Shadowstep(20,1,40,5,new Tags(){{
        add(Tag.Runic);
        add(Tag.Shadow);
        add(Tag.Cunning);
    }}){
        public String getClipName(){
            return "Conjure";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
        }
        public void trigger(Unit host){
            Sound.play("shadow");
            Particle.smoke(host.pos);
            P target = host.target.getAdd(host.pos);
            Line pp = new Line(target, target.getAdd(0,1200));
            for (Line i : Global.map.land){
                if (i.lineCross(pp)){
                    Particle.smoke(host.pos);
                    Particle.smoke(target);
                    if (Global.map.checkLand(new Line(target,target.getAdd(0,800)), 0) != null) {
                        Sound.play("shadowstep");
                        host.pos.set(target);
                        host.setState(Actions.Idle);
                        return;
                    }
                    return;
                }
            }
        }
    },
    Fireblast(40,0,24,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Channeling);
        add(Tag.Fire);
        add(Tag.Air);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
        }
        public void onFinish(Unit host){}
        public void update(Unit host){
            if (host.delay.time > 0) return;
            Sound.play("fire");
            final int level = host.stateLevel;
            for (int i=0;i<3;i++) {
                final Projectile p = new Projectile("Flame", host.getHand(), host.target.getAdd(host.pos)
                        .getAdd(Global.r.nextInt(50)-25, Global.r.nextInt(50)-25), Global.r.nextInt(10+level)+6+level) {
                    @Override
                    public void stuck() {
                        Particle.explode("Flame", pos, new P(), 10, 10, 0, false, 4);
                        remove();
                    }
                    @Override
                    public void onAttack(Unit p) {
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
            float atan = host.target.getAtan();
            host.vel.add(Global.cos(atan)*-.1f*level, Global.sin(atan)*-.1f*level);
            if (host.stateDuration % (8+host.stateLevel*2) == 0 && !host.get("Stamina").require(1))
                host.setState(Actions.Walk);
        }
    },
    Freeze(40,0,16,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Channeling);
        add(Tag.Cold);
        add(Tag.Air);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
        }
        public void onFinish(Unit host){}
        public void update(Unit host){
            if (host.delay.time > 0) return;
            Sound.play("ice");
            final int speed = 8+host.stateLevel*8;
            for (int i=0;i<3;i++) {
                final Projectile p = new Projectile("Ice", host.getHand(), host.target.getAdd(host.pos).getAdd(Global.r.nextInt(150)-75, Global.r.nextInt(150)-75), speed) {
                    private int tick = 200;
                    @Override
                    public void onAttack(Unit p) {
                        p.buff(Buffs.Freeze, 1);
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
            if (host.stateDuration % (24+host.stateLevel*8) == 0 && !host.get("Stamina").require(1))
                host.setState(Actions.Walk);
        }
    },
    Flameroll(40,5,120,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Fire);
        add(Tag.Biology);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
        }
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
    Fireball(40,5,50,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Fire);
        add(Tag.Conjuration);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
        }
        public void trigger(Unit host){
            final int level = host.stateLevel;
            final Projectile p = new Projectile("Fireball", host.getHand(), host.target.getAdd(host.pos), 36+(level*8)){
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
    Blizzard(30,2,240,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Channeling);
        add(Tag.Cold);
        add(Tag.Air);
    }}){
        public String getClipName(){
            return "Buff";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
        }
        public void update(final Unit host){
            final int level = host.stateLevel;
            if (Global.r.nextInt(40) > (level*8)-4) return;
            if (Math.random() < .2f) Sound.play("ice");
            final Projectile p = new Projectile("Ice", host.pos.getAdd(Global.nextGaussian(500),-500), host.pos.getAdd(Global.nextGaussian(500),0), 8+Global.r.nextInt(6*level)){
                @Override
                public void onAttack(Unit p){
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
            if (host.stateDuration % (7+host.stateLevel) == 0 && !host.get("Stamina").require(1))
                host.setState(Actions.Idle);
        }
        public void onFinish(Unit host){}
    };
    private String description;
    private int del, cooldown, cost;
    private Tags tags;
    private int difficulty;
    Actions(){
        this(0);
    }
    Actions(int del){
        this(del,-1,0,0, new Tags());
    }
    Actions(int del, int cost, int cooldown, int difficulty, Tags tags){
        this.del = del;
        this.cost = cost;
        this.cooldown = cooldown;
        this.description = Dialog.get("mvsDsc"+name());
        this.difficulty = difficulty;
        this.tags = tags;
    }
    public String getDescription(){
        String t = ""+cost;
        return description + "  [Trainers: " + tags.getTrainers().toString()+" at Difficulty "+getDifficulty()+"] [Cost:"+t+" "+"]";
    }
    public String getName(){
        return name();
    }
    public String getClipName(){
        return name();
    }

    public int getCost() {
        return cost;
    }

    public int getDel() {
        return del;
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void update(Unit p){
    }

    public void onSet(Unit p){
    }

    public void onFinish(Unit p){
        if (del > 0) {
            p.stateSet(Idle);
//            p.clip.setSpeed(8);
        }
    }

    public void trigger(Unit p){
    }

    public void onInterrupt(Unit p){
    }

    public Tags getTags(){
        return tags;
    }

    public static ArrayList<Actions> getByTag(Tag p){
        ArrayList<Actions> list = new ArrayList<>();
        for (Actions i : values()){
            if (i.getTags().contains(p)) list.add(i);
        }
        return list;
    }

    public static Actions getByName(String p) {
        for (Actions i : values()) {
            if (i.getName().equals(p)){
                return i;
            }
        }
        return null;
    }
}