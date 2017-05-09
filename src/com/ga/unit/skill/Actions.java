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
import com.ga.unit.intelligence.IntelligenceSocial;
import com.ga.unit.item.Item;
import com.ga.unit.item.Projectile;

import java.awt.*;
import java.util.ArrayList;

import static com.ga.unit.BodyHumanoid.OFIST;

/**
 * Created by Ga on 7/28/2016. asd
 */
public enum Actions {
    Idle(0,-1,0,0,new Tags()){
        public void onSet(Unit p){
            p.attackSpeed = 0;
        }
        public void update(Unit p){
            if (p.stats.rest(1)) p.stateSet(Idle);
            if (Math.abs(p.vel.x) < .5f) p.vel.x = 0;
        }
    }, Walk{
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
            p.attackSpeed = 0;
            if (p.lastLand.opt != 2 && Global.map.checkLand(new Line(p.pos.getAdd(0,4), p.pos.getAdd(0,800)), p.lastLand.depth) != null){
                p.setDepth(100);
                p.pos.y+=4;
            }
        }
    }, Hit(14){
        public void onSet(Unit p){
            p.attackSpeed = 0;
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
            p.attackSpeed = 0;
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
            p.attackSpeed = 0;
            p.clip.organs[p.clip.BODY].pt.y = 0;
        }
        public void onInterrupt(Unit p){
            onFinish(p);
        }
        public void onFinish(Unit p){
            p.clip.setHeight(); //clip animation too short
            super.onFinish(p);
        }
    }, Wield(18){
    }, Skill{
    }, Mastery{
    }, Conjure{
    }, Use{
        public void onFinish(Unit p){
            Man t = (Man)p;
            t.weapon.use((Man) p);
            if (t.weapon.uses == 0 && t.weapon.breakable) t.setWeapon(null);
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

    }, Sprint(15,3,5,3,new Tags(){{
        add(Tag.Athletics);
        add(Tag.Agility);
    }}){
        public void onSet(Unit p){
            p.attackSpeed = 0;
        }
        public void update(Unit p){
            if (Updater.time.time % ((p.stateLevel+2)*2) == 0 && !p.get("Stamina").require(1)) p.setState(Actions.Walk);
            else p.vel.x += p.getSpeed() * 2 * p.dir;
        }
        public void onFinish(Unit p){}
    },
    Duck(24,-1,6,1,new Tags(){{
        add(Tag.Agility);
    }}){
        public void onSet(Unit p){
            p.attackSpeed = 0;
        }
    },
    Guard(8,-1,0,8,new Tags(){{
        add(Tag.Swinging);
        add(Tag.Martial);
    }}){
        public void onSet(Unit p){
            ((Man)p).weapon.rotation = p.target.getAtan();
            p.attackSpeed = 0;
        }
    },
    Lunge(40,5,0,8,new Tags(){{
        add(Tag.Swinging);
        add(Tag.Martial);
        add(Tag.Agility);
    }}){
        public void onSet(Unit p){
            p.buff(Buffs.Evade, 1);
            p.damageMod = .5f;
        }
        public void trigger(Unit p){
            p.attackSound();
            p.attack();
        }
    },
    Thrust(25,4,0,0,new Tags(){{
        add(Tag.Swinging);
    }}){
        public void onSet(Unit p){
            if (p.vel.x < 0 != p.dir < 0) p.vel.x = 0;
            p.damageMod = 1 + (Math.abs(p.vel.x) / 20) + (p.stateLevel*.5f);
        }
        public void trigger(Unit p){
            p.attackSound();
            p.attack();
            ((Man)p).weapon.rotateTo(p.target.getMult(p.dir, p.dir));
        }
    },
    Slash(25,4,0,0,new Tags(){{
        add(Tag.Swinging);
    }}){
        public void onSet(Unit p){
            Item weapon = ((Man)p).weapon;
            weapon.rotation = -(weapon.dir * 10);
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
                        if (p.attack()) break;
                    }
                }
                item.rotation = rot;
            }
        }
    },
    Kick(3,-1,2,0,new Tags(){{
        add(Tag.Athletics);
    }}){
        public void onSet(Unit p){
            p.attackSpeed = 0;
        }
        public void trigger(Unit p){
            p.attackSound();
            p.attack();
        }
    },
    Sidekick(18,0,2,5,new Tags(){{
        add(Tag.Swinging);
        add(Tag.Athletics);
    }}){
        public void onSet(Unit p){
            p.attackSpeed = 0;
        }
        public void trigger(Unit p){
            p.attackSound();
            p.attack();
        }
    },
    Whirlwind(35, 8, 40,14,new Tags(){{
        add(Tag.Swinging);
        add(Tag.Martial);
        add(Tag.Strong);
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
    Stab(40,2,24,14,new Tags(){{
        add(Tag.Swinging);
        add(Tag.Martial);
        add(Tag.Cunning);
    }}){
        public void onSet(Unit p){
            p.damageMod = 2 + p.stateLevel;
        }
        public void trigger(Unit p){
            p.attackSound();
            p.attack();
        }
    },
    Dash(35,6,24,11,new Tags(){{
        add(Tag.Swinging);
        add(Tag.Jumping);
        add(Tag.Athletics);
        add(Tag.Martial);
    }}){
        public void onSet(Unit p){
            p.damageMod = p.stateLevel * .5f;
            p.vel.x -= p.getSpeed() * 6 * p.dir;
            p.delay.time += 2 * p.stateLevel;
            p.delay.max += 2 * p.stateLevel;
        }
        public void update(Unit p) {
            if (Math.abs(p.vel.x) >= p.getSpeed()) p.attack();
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
            p.get("Stamina").add(-4);
        }
        public void update(Unit p) {
            p.stats.rest(4);
            if (p.get("Stamina").isFull()) p.stateSet(Idle);
        }
    },
    Sneak(3,-1,0,6,new Tags(){{
        add(Tag.Cunning);
    }}){
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
    Underhand(32,0,0,12,new Tags(){{
        add(Tag.Swinging);
        add(Tag.Martial);
        add(Tag.Agility);
    }}){
        public void onSet(Unit p){
            p.damageMod = 1 + (p.stateLevel * .5f);
            p.buff(Buffs.Evade, 1);
        }
        public void trigger(Unit p){
            p.attackSound();
            p.vel.set(5*p.dir,-1);
            p.attack();
            ((Man)p).weapon.rotateTo(p.target.getMult(p.dir, p.dir));
        }
    },
    Mark(80,-1,17,17,new Tags(){{
        add(Tag.Swinging);
        add(Tag.Martial);
        add(Tag.Cunning);
    }}){
        public void onSet(Unit p){
            ArrayList<Unit> t = Updater.collideUnit(p.target.getAdd(p.pos),40,Faction.Destructive);
            if (t.size() > 0) t.get(0).buff(Buffs.Mark,1);
        }
    },
    Identify(1,-1,0,4,new Tags(){{
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
        add(Tag.Swinging);
    }}){
        public void trigger(Unit p){
            final Man t = (Man)p;
            t.damageMod = p.stateLevel-.5f;
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
                    protected void onAttack(Unit p) {
                        if (p.checkHit(new Line(pos,pos.getAdd(vel)),damage)){
                            if (wpn.usable(1)) wpn.trigger(p);
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
    Dart(12,0,1,7,new Tags(){{
        add(Tag.Swinging);
    }}),
    Aim(120,2,0,2,new Tags(){{
        add(Tag.Martial);
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
    Shoot(27,-1,2,21,new Tags(){{
        add(Tag.Swinging);
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
    Snipe(27,-1,0,30,new Tags(){{
        add(Tag.Swinging);
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
    }}){
        public void onSet(Unit p){
            float x = Math.abs(p.vel.x);
            float t = Math.max(0, 36 + p.stateLevel*4 - x);
            Sound.play("jump");
            p.vel.y -= t;
            p.vel.x *= 5;
            if (Math.abs(p.vel.x) >= -p.vel.y) p.stateSet(Down);
            p.attackSpeed = 0;
        }
    },
    Warcry(60,10,40,24,new Tags(){{
        add(Tag.Strong);
        add(Tag.Mastery);
    }}){
        public void onSet(Unit p){
            ArrayList<Unit> t = Updater.collideUnit(p.pos,240+(120*p.stateLevel),Faction.Destructive);
            for (Unit i : t){
                if (i != p) i.buff(Buffs.Stun,3+p.stateLevel);
            }
        }
    },
    Evade(40,4,27,17,new Tags(){{
        add(Tag.Shadow);
        add(Tag.Agility);
    }}){
        public void onSet(Unit p){
            p.buff(Buffs.Evade,1+p.stateLevel);
        }
    },
    Cleave(40,-1,27,6,new Tags(){{
        add(Tag.Buff);
        add(Tag.Strong);
    }}){
        public void onSet(Unit p){
            p.buff(Buffs.Cleave,p.stateLevel);
        }
    },
    Airstrike(27,1,4,9,new Tags(){{
        add(Tag.Swinging);
        add(Tag.Jumping);
        add(Tag.Martial);
    }}){
        public void onSet(Unit p){
            p.attackSpeed = 0;
            p.vel.y -= 20;
        }
        public void update(Unit p){
            if (p.jump){
                ((Man)p).weapon.rotation = 1f*p.dir;
                p.delay.time = Math.min(p.delay.time, 4);
            } else {
                Man t = (Man)p;
                t.weapon.rotation = t.delay.time * -t.weapon.dir;
                if (p.delay.time % 4 == 0) p.attackSound();
                p.attack();
            }
        }
    },
    Leap(40,2,40,18,new Tags(){{
        add(Tag.Jumping);
        add(Tag.Strong);
    }}){
        public void trigger(Unit p){
            Sound.play("jump");
            p.vel.y -= 80;
            p.vel.x = 80 * p.dir;
        }
    },
    Backstep(20,1,12,15,new Tags(){{
        add(Tag.Athletics);
        add(Tag.Agility);
    }}){
        public void onSet(Unit p){
            p.vel.x = -p.dir*p.getSpeed()*3;
        }
    },
    Innersword(18,3,21,3,new Tags(){{
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
        add(Tag.Conjuration);
        add(Tag.Air);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void onSet(Unit host){
            host.attackSpeed = 0;
        }
        public void trigger(Unit host){
            Sound.play("shadowstep");
            P origin = host.getHand();
            ArrayList<Unit> t = Updater.collideUnit(origin,800,Faction.Destructive);
            int size = t.size();
            if (size > 0) {
                Unit target = t.get(size-1);
                P targetPos = target.pos;
                host.target = targetPos.getSubtract(host.pos);
                final Terrain[] lightning = new Terrain[Global.r.nextInt(3) + 2];
                int num = Global.r.nextInt(6) + 3;
                float div = host.target.getDist() / (num - 1);
                float atan = host.target.getAdd(host.pos.getSubtract(origin)).getAtan();
                P[] points = new P[num * 2 - 2];
                for (int ii = 0; ii < lightning.length; ii++) {
                    lightning[ii] = new Terrain("lightning", Shader.None, 50);
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
                        lightning[ii].addPoint(origin.x + points[i].x, origin.y + points[i].y);
                    }
                    lightning[ii].render();
                }
                Particle.holyExplosion(targetPos);
                target.hit(host.pos.x>targetPos.x?-1:1, .5f+host.stateLevel*.1f);
                host.delay.performer = new Performer() {
                    public void perform() {
                        for (Terrain i : lightning) Updater.remove(i);
                    }
                };
            }
        }
    },
    Shockfield(40,4,7,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Conjuration);
        add(Tag.Air);
        add(Tag.Mastery);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void onSet(Unit host){
            host.attackSpeed = 0;
        }
        public void trigger(Unit host){
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
            host.attackSpeed = 0;
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
    Focus(15, -1, 40, 1, new Tags(){{
        add(Tag.Buff);
        add(Tag.Martial);
    }}){
        public String getClipName(){
            return "Conjure";
        }
        public void onSet(Unit host){
            host.attackSpeed = 0;
        }
        public void trigger(Unit host){
            Sound.play("heal");
            host.get("Stamina").add(2 + host.stateLevel * host.stateLevel);
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
            host.attackSpeed = 0;
        }
        public void trigger(Unit host){
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
            host.attackSpeed = 0;
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
            host.attackSpeed = 0;
        }
        public void trigger(Unit host){
            host.buff(Buffs.Haste, host.stateLevel);
        }
    },
    Flamestride(10,4,20,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Buff);
        add(Tag.Fire);
    }}){
        public String getClipName(){
            return "Buff";
        }
        public void onSet(Unit host){
            host.attackSpeed = 0;
        }
        public void trigger(Unit host){
            host.buff(Buffs.Flamestride, host.stateLevel);
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
            host.attackSpeed = 0;
        }
        public void trigger(Unit host){
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
            host.attackSpeed = 0;
        }
        public void trigger(Unit host){
            ArrayList<Unit> t = Updater.collideUnit(host.target.getAdd(host.pos),40,Faction.Destructive);
            if (t.size() > 0) t.get(0).buff(Buffs.Stun,host.stateLevel);
        }
    },
    Slow(40,4,40,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Debuff);
        add(Tag.Agility);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void onSet(Unit host){
            host.attackSpeed = 0;
        }
        public void trigger(Unit host){
            ArrayList<Unit> t = Updater.collideUnit(host.target.getAdd(host.pos),40,Faction.Destructive);
            if (t.size() > 0) t.get(0).buff(Buffs.Slow,host.stateLevel);
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
            host.attackSpeed = 0;
        }
        public void trigger(Unit host){
            Sound.play("shadowstep");
            for (int i=0;i<host.rand.nextInt(host.stateLevel+1);i++) {
                Unit p = (Unit) Stock.getByName("Dog",-1);
                p.pos.set(host.pos);
                p.brain = new IntelligenceSocial(p);
                p.dir = host.dir;
                if (host.brain != null){
                    p.brain.target = host.brain.target;
                }
                p.buff(Buffs.Banishment,5+(int)(host.stateLevel*.4f));
                p.faction = host.faction;
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
            host.attackSpeed = 0;
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
                p.stats.setStat("expire",new Stat(500+(host.stateLevel*100)));
                p.brain = new IntelligenceSocial(p);
                p.buff(Buffs.Banishment,5+(int)(host.stateLevel*.4f));
                p.dir = -host.dir;
                p.faction = host.faction;
                p.clip.setState("Buff");
            }
        }
    },
    Shadowstep(20,1,40,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Shadow);
        add(Tag.Cunning);
    }}){
        public String getClipName(){
            return "Conjure";
        }
        public void onSet(Unit host){
            host.attackSpeed = 0;
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
    Fireworks(40,0,1,1,new Tags(){{
        add(Tag.Channel);
        add(Tag.Fire);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void onSet(Unit host){
            host.attackSpeed = 0;
        }
        public void onFinish(Unit host){}
        public void update(Unit host){
            if (host.delay.time > 0) return;
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
        }
    },
    Fireblast(40,1,60,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Channel);
        add(Tag.Fire);
        add(Tag.Air);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void onSet(Unit host){
            host.attackSpeed = 0;
        }
        public void onFinish(Unit host){}
        public void update(Unit host){
            if (host.delay.time > 0) return;
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
        }
    },
    Freeze(40,1,24,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Channel);
        add(Tag.Cold);
        add(Tag.Air);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void onSet(Unit host){
            host.attackSpeed = 0;
        }
        public void onFinish(Unit host){}
        public void update(Unit host){
            if (host.delay.time > 0) return;
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
            if (Updater.time.time % ((host.stateLevel+2)*2) == 0 && !host.get("Stamina").require(1))
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
            host.attackSpeed = 0;
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
            host.attackSpeed = 0;
        }
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
    Blizzard(30,2,240,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Channel);
        add(Tag.Cold);
        add(Tag.Air);
        add(Tag.Mastery);
    }}){
        public String getClipName(){
            return "Buff";
        }
        public void onSet(Unit host){
            host.attackSpeed = 0;
        }
        public void update(final Unit host){
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
                    if (Updater.time.time % ((host.stateLevel+2)*2) == 0 && !host.get("Stamina").require(1))
                        host.setState(Actions.Idle);
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
        }
        public void onFinish(Unit host){}
    },
    Meteor(40,2,240,1,new Tags(){{
        add(Tag.Runic);
        add(Tag.Channel);
        add(Tag.Fire);
        add(Tag.Conjuration);
        add(Tag.Mastery);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void onSet(Unit host){
            host.attackSpeed = 0;
        }
        public void update(final Unit host){
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
            if (Updater.time.time % ((host.stateLevel+2)*2) == 0 && !host.get("Stamina").require(1))
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