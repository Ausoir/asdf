package com.ga.unit.skill;

import com.ga.game.Dialog;
import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.Shader;
import com.ga.map.Updater;
import com.ga.tool.EventTimer;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.*;
import com.ga.unit.item.Item;
import com.ga.unit.item.Projectile;

import java.awt.*;
import java.util.ArrayList;

import static com.ga.unit.BodyHumanoid.OFIST;

/**
 * Created by Ga on 7/28/2016. asd
 */
public enum Actions implements Moveset {
    Idle{
        public void update(Unit p, int level){
            if (p.stats.rest(1)) p.stateSet(Idle);
        }
    }, Walk{
        public void update(Unit p, int level){
            p.vel.x = p.getSpeed()*p.dir;
        }
    }, Climb{
        public void update(Unit p, int level){
            if (p.clip.organs[0].ptd.y + p.pos.y < ((Man)p).rope.pt.y){
                p.jump = true;
                p.vel.x = p.getSpeed()*p.dir;
                p.stateSet(Jump);
            } else p.vel.y = -p.getSpeed();
        }
    }, Drop(12){
        public void onSet(Unit p, int level){
            if (p.lastLand.opt != 2 && Global.map.fallSafe(p.pos.getAdd(0,4), p.lastLand.depth) != null){
                p.setDepth(100);
                p.pos.y+=4;
            }
        }
    }, Hit(14){
        public void onSet(Unit p, int level){
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
        public void onSet(final Unit p, int level){
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
        public void onSet(final Unit p, int level){
            p.clip.organs[p.clip.BODY].pt.y = 0;
        }
        public void onInterrupt(Unit p){
            onFinish(p);
        }
        public void onFinish(Unit p){
            p.clip.setHeight(); //clip animation too short
            super.onFinish(p);
        }
    }, Wield(18), Skill, Mastery,
    Conjure, Use, WalkL, Wait(50), Consume(8), Exhausted, Interact(8),
    Buff{
        public void onFinish(Unit p){}
    }, Cast{
        public void onFinish(Unit p){}
    }, Sprint(15,1,0,14,3,new Tags(){{
        add(Tag.Athletics);
        add(Tag.Agility);
    }}){
        public void update(Unit p, int level){
            if (Updater.time.time % (level*4) == 0 && !p.get("Stamina").require(1)) p.setState(Actions.Walk);
            else p.vel.x = p.getSpeed() * 2 * p.dir;
        }
        public void onFinish(Unit p){}
    },
    Duck(24,-1,0,6,4,new Tags(){{
        add(Tag.Agility);
    }}),
    Guard(8,-1,0,0,8,new Tags(){{
        add(Tag.Swinging);
        add(Tag.Martial);
    }}),
    Lunge(40,5,0,0,8,new Tags(){{
        add(Tag.Swinging);
        add(Tag.Martial);
        add(Tag.Agility);
    }}){
        public void onSet(Unit p, int level){
            p.buff(Buffs.Evade, 1);
        }
        public void trigger(Unit p, int level){
            p.attackSound();
            p.attack();
        }
    },
    Thrust(25,4,1f,0,0,new Tags(){{
        add(Tag.Swinging);
    }}){
        public void trigger(Unit p, int level){
            p.attackSound();
            p.attack();
            ((Man)p).weapon.rotateTo(p.target.getMult(p.dir, p.dir));
        }
        public void onSet(Unit p, int level){
            if (p.vel.x < 0 != p.dir < 0) p.vel.x = 0;
        }
    },
    Slash(25,4,0,0,0,new Tags(){{
        add(Tag.Swinging);
    }}){
        public void onSet(Unit p, int level){
            Item weapon = ((Man)p).weapon;
            weapon.rotation = -(weapon.dir * 10);
        }
        public void update(Unit p, int level){
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
    Kick(3,-1,1,2,0,new Tags(){{
        add(Tag.Athletics);
    }}){
        public void trigger(Unit p, int level){
            p.attackSound();
            p.attack();
        }
    },
    Sidekick(18,0,1,2,5,new Tags(){{
        add(Tag.Swinging);
        add(Tag.Athletics);
    }}){
        public void trigger(Unit p, int level){
            p.attackSound();
            p.attack();
        }
    },
    Whirlwind(35, 5, 0, 60,14,new Tags(){{
        add(Tag.Swinging);
        add(Tag.Martial);
        add(Tag.Strong);
    }}){
        public void update(Unit p, int level) {
            if (p.delay.time < 36) {
                Man t = (Man) p;
                t.weapon.rotation = p.delay.time * -t.weapon.dir;
                p.attack();
            }
        }
        public void trigger(Unit p, int level){
            if (p.delay.time % 4 == 0) p.attackSound();
        }
    },
    Stab(40,2,0,24,14,new Tags(){{
        add(Tag.Swinging);
        add(Tag.Martial);
        add(Tag.Cunning);
    }}){
        public void trigger(Unit p, int level){
            p.attackSound();
            p.attack();
        }
    },
    Dash(35,1,0,24,11,new Tags(){{
        add(Tag.Swinging);
        add(Tag.Jumping);
        add(Tag.Athletics);
        add(Tag.Martial);
    }}){
        public void update(Unit p, int level) {
            if (Math.abs(p.vel.x) >= p.getSpeed()) p.attack();
        }
        public void trigger(Unit p, int level){
            p.attackSound();
            p.vel.set(p.getSpeed() * 3 * p.dir * (p.damageMod*2), -5);
            ((Man)p).weapon.rotateTo(p.target.getMult(p.dir, p.dir));
        }
    },
    Rest(0,-1,0,0,0,new Tags(){{
    }}){
        public void update(Unit p, int level) {
            if (Updater.time.time % 8 == 0) p.get("Stamina").add(1);
        }
    },
    Sneak(3,-1,0,0,6,new Tags(){{
        add(Tag.Cunning);
    }}){
        public void update(Unit p, int level) {
            p.stats.rest(2+level);
        }
        public void onSet(Unit p, int level){
            p.clip.setShade(Shader.Color,new Color(0, 0, 0, 180).getRGB());
        }
        public void onFinish(Unit p){
        }
        public void onInterrupt(Unit p){
            p.clip.setShade(Shader.XYLinearGradient,0);
        }
    },
    Underhand(32,0,0,0,12,new Tags(){{
        add(Tag.Swinging);
        add(Tag.Martial);
        add(Tag.Agility);
    }}){
        public void onSet(Unit p, int level){
            p.buff(Buffs.Evade, 1);
        }
        public void trigger(Unit p, int level){
            p.attackSound();
            p.vel.set(5*p.dir,-1);
            p.attack();
            ((Man)p).weapon.rotateTo(p.target.getMult(p.dir, p.dir));
        }
    },
    Mark(80,-1,0,17,17,new Tags(){{
        add(Tag.Swinging);
        add(Tag.Martial);
        add(Tag.Cunning);
    }}){
        public void onSet(Unit p, int level){
            ArrayList<Unit> t = Updater.collideUnit(p.target.getAdd(p.pos),40,Faction.Destructive);
            if (t.size() > 0) t.get(0).buff(Buffs.Mark,1);
        }
    },
    Identify(1,-1,0,0,4,new Tags(){{
    }}){
        public void onSet(Unit p, int level){
            if (p == Global.player) {
                ArrayList<Unit> t = Updater.collideUnit(p.target.getAdd(p.pos), 40, Faction.Destructive);
                if (t.size() > 0){
                    Unit target = t.get(0);
                    Global.display.menuDialog.show(target.name,target.getDescription());
                }
            }
        }
    },
    Throw(12,-1,0,0,0,new Tags(){{
        add(Tag.Swinging);
    }}){
        public void trigger(Unit p, int level){
            final Man t = (Man)p;
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
                        if (p.checkHit(new Line(pos,pos.getAdd(vel)),damage,damageType)){
                            if (wpn.use(1)) wpn.trigger(p);
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
    Dart(12,0,0,1,7,new Tags(){{
        add(Tag.Swinging);
    }}),
    Shoot(27,-1,0,0,21,new Tags(){{
        add(Tag.Swinging);
    }}){
        public void trigger(Unit p, int level){
            Man t = (Man)p;
            P tip = t.weapon.getPoint();
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
            pj.delay.set(2);
            pj.dir = t.dir;
            pj.rotSpeed = 0;
            pj.rotateTo(t.target.getSubtract(tip).add(t.pos));
            pj.faction = t.faction;
            pj.damage = (t.weapon.getDamage()*10)+4;
            Particle.explode("Spark", tip, pj.vel, 5, 10, 15, false,3);
            float start = t.rand.nextFloat();
            pj.pos.add(pj.vel.getMult(start, start));
            t.weapon.rotation += t.dir * -.3f;
        }
    },
    Snipe(27,-1,.5f,0,30,new Tags(){{
        add(Tag.Swinging);
    }}){
        public void trigger(Unit p, int level){
            Man t = (Man)p;
            P tip = t.weapon.getPoint();
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
            pj.damage = (t.weapon.getDamage()*10)+4;
            Particle.explode("Spark", tip, pj.vel, 5, 10, 15, false,3);
            float start = t.rand.nextFloat();
            pj.pos.add(pj.vel.getMult(start, start));
            t.weapon.rotation += t.dir * -.3f;
        }
    },
    Triple(60,2,0,40,21,new Tags(){{
        add(Tag.Swinging);
        add(Tag.Mastery);
    }}){
        public void trigger(Unit p, int level){
            Man t = (Man)p;
            t.attackSound();
            t.weapon.rotation = t.delay.time * -.3f * -t.weapon.dir;
            t.vel.set(t.dir * Global.cos(t.weapon.rotation)*15, -1);
            t.attack();
        }
    },
    Jump(47,1,3f,0,0,new Tags(){{
    }}){
        public void onSet(Unit p, int level){
            Sound.play("jump");
            p.vel.y -= 40;
            p.lastLand = new Line();
        }
    },
    Warcry(60,10,0,40,24,new Tags(){{
        add(Tag.Strong);
        add(Tag.Mastery);
    }}){
        public void onSet(Unit p, int level){
            ArrayList<Unit> t = Updater.collideUnit(p.pos,240+(120*level),Faction.Destructive);
            for (Unit i : t){
                if (i != p) i.buff(Buffs.Stun,3+level);
            }
        }
    },
    Evade(40,4,0,27,17,new Tags(){{
        add(Tag.Shadow);
        add(Tag.Agility);
    }}){
        public void onSet(Unit p, int level){
            p.buff(Buffs.Evade,1+level);
        }
    },
    Empower(40,-1,0,27,14,new Tags(){{
        add(Tag.Buff);
        add(Tag.Martial);
    }}){
        public void onSet(Unit p, int level){
            p.buff(Buffs.Empower,level);
        }
    },
    Cleave(40,-1,0,27,6,new Tags(){{
        add(Tag.Buff);
        add(Tag.Strong);
    }}){
        public void onSet(Unit p, int level){
            p.buff(Buffs.Empower,level);
        }
    },
    Airstrike(27,1,1.5f,4,9,new Tags(){{
        add(Tag.Swinging);
        add(Tag.Jumping);
        add(Tag.Martial);
    }}){
        public void update(Unit p, int level){
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
    Leap(40,2,3,40,18,new Tags(){{
        add(Tag.Jumping);
        add(Tag.Strong);
    }}){
        public void trigger(Unit p, int level){
            if (p.jump) {
                Sound.play("jump");
                p.vel.y -= 80;
                p.vel.x = p.getSpeed() * 10 * p.dir;
                p.lastLand = new Line();
            }
        }
    },
    Backstep(20,1,0,12,15,new Tags(){{
        add(Tag.Athletics);
        add(Tag.Agility);
    }}){
        public void onSet(Unit p, int level){
            p.vel.x = -p.dir*24;
        }
    };
    private String description;
    private int del, cooldown;
    private float cost, velx;
    private Tags tags;
    private int difficulty;
    Actions(){
        this(0);
    }
    Actions(int del){
        this(del,-1,0,0,0, new Tags());
    }
    Actions(int del, float cost, float velx, int cooldown, int difficulty, Tags tags){
        this.del = del;
        this.cost = cost;
        this.velx = velx;
        this.cooldown = cooldown;
        this.description = Dialog.get("mvsDsc"+name());
        this.difficulty = difficulty;
        this.tags = tags;
    }
    public String getDescription(){
        String t = ""+((int)cost);
        return description + "  [Trainers: " + tags.getTrainers().toString()+" at Difficulty "+getDifficulty()+"] [Cost:"+t+" "+"]";
    }
    public String getName(){
        return name();
    }
    public String getCodeName(){
        return "A"+name();
    }

    public float getCost() {
        return cost;
    }

    @Override
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
    public int getDifficulty() {
        return difficulty;
    }



    @Override
    public void update(Unit p, int level){
    }

    @Override
    public void onSet(Unit p, int level){
    }

    @Override
    public void onFinish(Unit p){
        if (del > 0) {
            p.stateSet(Idle);
//            p.clip.setSpeed(8);
        }
    }

    @Override
    public void trigger(Unit p, int organ){
    }

    public void onInterrupt(Unit p){
    }

    public Tags getTags(){
        return tags;
    }

    public static Moveset getByName(String p) {
        for (Actions s : values()) {
            if (s.getName().equals(p)){
                return s;
            }
        }
        return null;
    }
}