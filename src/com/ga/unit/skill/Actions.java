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
            p.delay.time += Global.r.nextInt(160);
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
            t.stateSet(Idle);
            t.weapon.use((Man) p);
        }
    }, WalkL{
    }, Consume(8){
    }, Exhausted{
    }, Interact(8){
    }, Grab(8){
    }, Discard(8){
    }, Swap(4){
    }, Buff{
        public void onFinish(Unit p){}
    }, Cast{
        public void onFinish(Unit p){}
    }, Sprint(15,0,5,0,new Tags(){{
        add(Tag.Athletics);
        add(Tag.Legs);
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
        add(Tag.Legs);
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
        add(Tag.Marksman);
    }}){
        public void onSet(Unit p){
            ((Man)p).weapon.rotation = p.target.getAtan();
            p.attackSpeed = -1;
        }
    },
    Thrust(25,2,0,0,new Tags(){{
        add(Tag.Combat);
        add(Tag.Marksman);
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
    Kick(3,-1,2,1,new Tags(){{
        add(Tag.Unarmed);
        add(Tag.Legs);
    }}){
        public void trigger(Unit host){
            host.attackSound();
            Man t = (Man)host;
            Line hitbox = t.clip.organs[1].getAdd(t.pos);
            for (Unit i : Updater.collideUnit(t.pos, (int) hitbox.getLength()+64, t.faction)) {
                if (i.checkHit(hitbox, 1, true)) {
                    i.stateSet(Actions.Hit);
                    Sound.play(host.pos, "hit_whip");
                    new Thing("hit_0", -1, 150) {
                        private int n = 0;
                        public void draw(Graphics2D g) {
                            if (n <= 7) {
                                super.draw(g);
                                setImage("hit_" + (n++) / 2);
                                if (n > 7) remove();
                            }
                        }
                    }.pos = hitbox.ptd;
                    i.vel.x = (host.pos.x > i.pos.x ? -1 : 1) * 6;
                    i.vel.y -= 6;
                    break;
                }
            }
        }
    },
    Sidekick(18,1,6,2,new Tags(){{
        add(Tag.Unarmed);
        add(Tag.Finesse);
        add(Tag.Legs);
    }}){
        public void trigger(Unit host){
            host.attackSound();
            Man t = (Man)host;
            Line hitbox = t.clip.organs[2].getAdd(t.pos);
            int damage = (int)(t.armor.armor[1].getDamage()+host.stateLevel*.5f)+1;
            for (Unit i : Updater.collideUnit(t.pos, (int) hitbox.getLength()+64, t.faction)) {
                if (i.checkHit(hitbox, damage, false)) {
                    Sound.play(host.pos, "hit_bash");
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
                    break;
                }
            }
        }
    },
    Punch(12,1,0,1,new Tags(){{
        add(Tag.Unarmed);
        add(Tag.Might);
    }}){
        public String getClipName(){
            return "Thrust";
        }
        public void trigger(Unit host){
            host.attackSound();
            Man t = (Man)host;
            Line hitbox = t.clip.organs[0].getAdd(t.pos);
            int damage = Global.r.nextInt((int) (t.armor.armor[2].getDamage()+host.stateLevel*.5f)+1);
            for (Unit i : Updater.collideUnit(hitbox.ptd, (int) hitbox.getLength()+64, t.faction)) {
                if (i.checkHit(hitbox, damage, true)) {
                    Sound.play(host.pos, "hit_punch");
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
                    break;
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
        add(Tag.Marksman);
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
        add(Tag.Marksman);
        add(Tag.Athletics);
        add(Tag.Finesse);
        add(Tag.Legs);
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
            p.get("Stamina").add(-4, false);
        }
        public void update(Unit p) {
            p.stats.rest(4+p.stateLevel);
            if (p.get("Stamina").isFull()) p.stateSet(Idle);
        }
    },
    Lunge(24,0,12,3,new Tags(){{
        add(Tag.Marksman);
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
        add(Tag.Marksman);
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
                    Global.display.menu.showDialog(target.name,target.getDescription());
                }
            }
        }
    },
    Throw(12,-1,0,0,new Tags(){{
        add(Tag.Marksman);
    }}){
        public String getClipName(){
            return "Thrust";
        }
        public void trigger(final Unit host){
            final Man t = (Man)host;
            t.damageMod = host.stateLevel*.3f;
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
                        if (p.checkHit(new Line(pos,pos.getAdd(vel)),damage, true)){
                            if (wpn.usable(1)) wpn.trigger(p);
                            Sound.play(host.pos, "hit_bash");
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
        add(Tag.Marksman);
    }}),
    Aim(120,2,0,3,new Tags(){{
        add(Tag.Marksman);
    }}){
        public void onSet(Unit p){
            Man t = (Man)p;
            t.weapon.rotateTo(t.target.getMult(t.dir,t.dir));
            t.weapon.rotation += .1 * t.dir;
        }
        public void trigger(final Unit host){
            Man t = (Man)host;
            P tip = t.weapon.getLine().ptd;
            final Item wpn = t.weapon;
            float flytime = t.target.getDist()/100;
            int speed = (int) (35+(host.get("Strength").get()*10)+(t.rand.nextFloat()*(60/host.stateLevel)));
            final Projectile pj = new Projectile("Arrow",tip,t.pos.getAdd(t.target), speed){
                public void onField(){}
                public void onAttack(Unit p){
                    wpn.trigger(p);
                    Sound.play(host.pos, "hit_bloody");
                    remove();
                }
                public void collide(){
                    if (damage > 0) super.collide();
                }
                public void update(){
                    super.update();
                    if (tangible || lastLand.depth == 0) rotateTo(vel);
                }
            };
            pj.setDepth(300);
            pj.rotSpeed = 0;
            pj.rotation = pj.vel.getAtan();
            pj.gravity = true;
            pj.faction = t.faction;
            pj.delay.set((int) flytime);
            final int damage = (t.weapon.getDamage() * 3)+2;
            pj.delay.performer = new Performer(){
                public void perform(){
                    pj.damage = damage;
                }
            };
            t.weapon.rotation += t.dir * .3f;
            host.stateSet(Actions.Idle);
        }
    },
    Shoot(27,-1,2,8,new Tags(){{
        add(Tag.Marksman);
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
        add(Tag.Marksman);
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
        public void onSet(Unit host){
            float x = Math.abs(host.vel.x);
            float t = Math.max(0, 36 + host.stateLevel*2 - x);
            Sound.play(host.pos, "jump");
            host.vel.y -= t;
            host.vel.x *= 5;
            if (Math.abs(host.vel.x) >= -host.vel.y) host.stateSet(Down);
            host.attackSpeed = -1;
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
    Airstrike(27,1,0,2,new Tags(){{
        add(Tag.Marksman);
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
                t.damageMod *= 2;
                t.attack();
                Stat stamina = p.get("Stamina");
                int b4 = stamina.get();
                if (!stamina.require(7)){
                    stamina.set(0,false);
                    p.stateSet(Down);
                    p.delay.time = (8-b4)*16;
                } else p.stateSet(Idle);
            } else {
                t.weapon.rotation = t.delay.time * -t.weapon.dir;
                if (p.delay.time % 4 == 0) p.attackSound();
                p.attack();
            }
        }
    },
    Leap(40,2,40,4,new Tags(){{
        add(Tag.Might);
        add(Tag.Legs);
    }}){
        public void trigger(Unit host){
            Sound.play(host.pos, "jump");
            host.vel.y -= 80;
            host.vel.x = 80 * host.dir;
        }
    },
    Backstep(20,1,12,0,new Tags(){{
        add(Tag.Athletics);
        add(Tag.Finesse);
        add(Tag.Legs);
    }}){
        public void onSet(Unit p){
            p.vel.x = -p.dir*20;
            p.vel.y -= 10;
        }
    },
    Innersword(18,3,21,1,new Tags(){{
        add(Tag.Crystal);
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
                Sound.play(host.pos, "shadowstep");
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
    Protrusion(18,3,1,6,new Tags(){{
        add(Tag.Runic);
        add(Tag.Crystal);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void onSet(Unit host){
            host.attackSpeed = 12;
        }
        public void trigger(final Unit host){
            Terrain rock = new Terrain("steel", Shader.None, 50){
                public void draw(Graphics2D g) {
                    super.draw(g);
                    Updater.remove(this);
                }
            };
            P origin = host.pos;
            float lastX = 0;
            for (int i = 0; i < Global.r.nextInt(6)+6; i++) {
                lastX = origin.x + host.dir*i*(Global.r.nextInt(80)+60);
                rock.addPoint(lastX, origin.y - (i % 2) * (Global.r.nextInt(80)+60));
            }
            rock.addPoint(lastX, origin.y);
            rock.render();
        }
    },
    Shock(40,4,7,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Energy);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
        }
        public void trigger(Unit host){
            Particle.explode("Glare",host.getHand(),new P(0,-1),8,20,12,true,4);
            Sound.play(host.pos, "shadowstep");
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
                target.buff(Buffs.Shock, host.stateLevel);
                target.buff(Buffs.Stun, 1);
            }
        }
    },
    Lightning(40,0,34,3,new Tags(){{
        add(Tag.Runic);
        add(Tag.Energy);
        add(Tag.Channeling);
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
            Sound.play(host.pos, "shadowstep");
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
                target.buff(Buffs.Shock, 1);
            }
            if (host.stateDuration % (12+host.stateLevel*2) == 0) host.buff(Buffs.Shock, 1);
        }
    },
    Materialize(40,4,4,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Crystal);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
        }
        public void trigger(Unit host){
            Sound.play(host.pos, "shadowstep");
            final Terrain r = new Terrain("water", Shader.None, 50);
            P[] points = new P[3];
            int power = 40+(40*host.stateLevel);
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
                if (!lines[i].clockwise()) lines[i].opt = -1;
                Global.map.land.add(lines[i]);
            }
            r.render();
            Global.timers.add(new EventTimer(3+host.stateLevel,false,new Performer(){
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
            Sound.play(host.pos, "heal");
            host.get("Stamina").add(2 + host.stateLevel * host.stateLevel, false);
        }
    },
    Rejuvenate(40, 1, 40, 1, new Tags(){{
        add(Tag.Buff);
        add(Tag.Mutation);
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
        add(Tag.Energy);
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
    Calling(40,2,4,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Conjuration);
    }}){
        public String getClipName(){
            return "Buff";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
        }
        public void update(Unit host){
            if (host.delay.time < 5) {
                Sound.play(host.pos, "shadowstep");
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
            Sound.play(host.pos, "shadowstep");
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
            Sound.play(host.pos, "shadow");
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
    Shadowstep(4,1,16,5,new Tags(){{
        add(Tag.Runic);
        add(Tag.Shadow);
        add(Tag.Cunning);
    }}){
        public String getClipName(){
            return "Conjure";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
            Sound.play(host.pos, "shadow");
            Particle.smoke(host.pos);
            int power = 300 + host.stateLevel * 100;
            P target = host.target.getAdd(host.pos);
            target.x = Global.clamp(target.x, host.pos.x-power,host.pos.x+power);
            target.y = Global.clamp(target.y, host.pos.y-power,host.pos.y+power);
            Line pp = new Line(target, target.getAdd(0,1200));
            for (Line i : Global.map.land){
                if (i.lineCross(pp)){
                    Particle.smoke(host.pos);
                    Particle.smoke(target);
                    if (Global.map.checkLand(new Line(target,target.getAdd(0,800)), 0) != null) {
                        Sound.play(host.pos, "shadowstep");
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
        public void update(final Unit host){
            if (host.delay.time > 0) return;
            Sound.play(host.pos, "fire");
            final int level = host.stateLevel;
            for (int i=0;i<3;i++) {
                final Projectile p = new Projectile("Flame", host.getHand(), host.target.getAdd(host.pos)
                        .getAdd(Global.r.nextInt(50)-25, Global.r.nextInt(50)-25), Global.r.nextInt(10+level)+6+level) {
                    public void stuck() {
                        Particle.explode("Flame", pos, new P(), 10, 10, 0, false, 4);
                        remove();
                    }
                    public void onAttack(Unit p) {
                        p.buff(Buffs.Burn, level);
                    }
                    public int getDepth(){
                        return host.getDepth();
                    }
                };
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
        public void update(final Unit host){
            if (host.delay.time > 0) return;
            Sound.play(host.pos, "ice");
            final int speed = 8+host.stateLevel*8;
            for (int i=0;i<3;i++) {
                final Projectile p = new Projectile("Ice", host.getHand(), host.target.getAdd(host.pos).getAdd(Global.r.nextInt(150)-75, Global.r.nextInt(150)-75), speed) {
                    private int tick = 200;
                    public void onAttack(Unit p) {
                        if (Global.r.nextBoolean()) p.buff(Buffs.Freeze, 1);
                        super.onAttack(p);
                    }
                    public void update(){
                        super.update();
                        vel.mult(.9f,.9f);
                        tick--;
                        if (tick == 0) remove();
                    }
                    public int getDepth(){
                        return host.getDepth();
                    }
                };
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
    Flameroll(40,5,40,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Fire);
        add(Tag.Mutation);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
        }
        public void trigger(final Unit host){
            final int level = host.stateLevel;
            final Projectile p = new Projectile("Fireball", host.pos, host.target.getAdd(host.pos), 64+(level*16)){
                @Override
                public void stuck(){
                    attackHitSound();
                    Particle.explode("Fire", pos, new P(), 85+(level*10), 10+(level*2), 0, false, 4);
                    Particle.smoke(pos);
                    ArrayList<Unit> t = Updater.collideUnit(pos,48+level*16,Faction.Destructive);
                    for (Unit i : t){
                        i.checkHit(pos, getDamage());
                    }
                    remove();
                    host.setVisible(true);
                }
                @Override
                public void update(){
                    super.update();
                    Particle.explode("Fire", pos, new P(), 15, 10, 0, true, 4);
                }
                void attackHitSound(){
                    Sound.play(host.pos, "explosion_small");
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
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void onSet(Unit host){
            host.attackSpeed = -1;
        }
        public void trigger(final Unit host){
            final int level = host.stateLevel;
            final Projectile p = new Projectile("Fireball", host.getHand(), host.target.getAdd(host.pos), 36+(level*8)){
                @Override
                public void stuck(){
                    attackHitSound();
                    Particle.explode("Fire", pos, new P(), 100+(level*15), 10+(level*2), 0, false, 4);
                    Particle.smoke(pos);
                    ArrayList<Unit> t = Updater.collideUnit(pos,48+level*16,Faction.Destructive);
                    for (Unit i : t){
                        i.checkHit(pos, getDamage());
                    }
                    remove();
                }
                @Override
                public void update(){
                    super.update();
                    Particle.explode("Fire", pos, new P(), 15, 10, 0, true, 4);
                }
                void attackHitSound(){
                    Sound.play(host.pos, "explosion_small");
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
            if (Math.random() < .2f) Sound.play(host.pos, "ice");
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
    private Tags tags, trainers;
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
        this.trainers = tags.getTrainers();
    }
    public String getDescription(){
        String t = ""+cost;
        return description + "  [Trainers: " + trainers.toString()+" at Difficulty "+getDifficulty()+"] [Cost:"+t+" "+"]";
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

    public Tags getTrainers(){
        return trainers;
    }

    public static ArrayList<Actions> getLearnables(){
        ArrayList<Actions> r = new ArrayList<>();
        for (Actions i : values()){
            if (i.getTrainers().size() > 0) r.add(i);
        }
        return r;
    }

    public boolean isLeg(){
        return tags.contains(Tag.Legs);
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