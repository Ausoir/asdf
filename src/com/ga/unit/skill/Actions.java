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
    }, Drop{
        public void onSet(Unit p){
            if (Global.map.fallSafe(p.lastLand, p.pos.getAdd(0,4))){
                p.setDepth(100);
                p.pos.y+=4;
            }
        }
    }, Use, WalkL, Hit(15), Dead(-1), Wield(18),
    Conjure, Wait(50), Cast, Consume(8), Exhausted, Interact(8), Down(80), Buff,
    Sprint(15,1,0,24,Dialog.get("mvsDscSprint")){
        public void update(Unit p, int level){
            p.vel.x = p.getSpeed() * 2 * p.dir;
        }
    },
    Duck(18,-1,0,6,Dialog.get("mvsDscDuck")),
    Guard(8,-1,0,0,Dialog.get("mvsDscGuard")),
    Lunge(0,-1,0,0,Dialog.get("mvsDscLunge")){
        public void trigger(Unit p, int level){
            p.attackSound();
            p.attack();
        }
    },
    Thrust(25,2,1f,0,Dialog.get("mvsDscThrust")){
        public void trigger(Unit p, int level){
            p.attackSound();
            p.attack();
            ((Man)p).weapon.rotateTo(p.target.getMult(p.dir, p.dir));
        }
        public void onSet(Unit p){
            if (p.vel.x < 0 != p.dir < 0) p.vel.x = 0;
        }
    },
    Slash(25,2,0,0,Dialog.get("mvsDscSlash")){
        public void onSet(Unit p){
            Item weapon = ((Man)p).weapon;
            float rot = -(weapon.dir * 10);
            weapon.rotation = rot;
        }
        public void update(Unit p, int level){
            if (p.delay.time <= 25 && p.delay.time > 12) {
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
                        p.attack();
                    }
                }
                item.rotation = rot;
            }
        }
    },
    Kick(3,-1,1,2, Dialog.get("mvsDscKick")){
        public void trigger(Unit p, int level){
            p.attackSound();
            p.attack();
        }
    },
    Sidekick(18,0,1,2,Dialog.get("mvsDscSidekick")){
        public void trigger(Unit p, int level){
            p.attackSound();
            p.attack();
        }
    },
    Whirlwind(35, 5, 0, 60,Dialog.get("mvsDscWhirlwind")){
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
    Stab(40,2,0,24,Dialog.get("mvsDscStab")){
        public void trigger(Unit p, int level){
            p.attackSound();
            p.attack();
        }
    },
    Dash(35,1,0,24,Dialog.get("mvsDscDash")){
        public void update(Unit p, int level) {
            if (Math.abs(p.vel.x) >= p.getSpeed()) p.attack();
        }
        public void trigger(Unit p, int level){
            p.attackSound();
            p.vel.set(p.getSpeed() * 3 * p.dir * (p.damageMod*2), -5);
            ((Man)p).weapon.rotateTo(p.target.getMult(p.dir, p.dir));
        }
    },
    Rest(0,-1,0,0,Dialog.get("mvsDscRest")){
        public void update(Unit p, int level) {
            if (p.stats.rest(4+level)) p.setState(Idle);
        }
    },
    Sneak(3,-1,0,0,Dialog.get("mvsDscSneak")){
        public void update(Unit p, int level) {
            p.stats.rest(2+level);
        }
        public void onSet(Unit p){
            p.clip.setShade(Shader.Gray);
        }
        public void onFinish(Unit p){
        }
        public void onInterrupt(Unit p){
            p.clip.setShade(Shader.XYLinearGradient);
        }
    },
    Underhand(32,0,0,0,Dialog.get("mvsDscUnderhand")){
        public void trigger(Unit p, int level){
            p.attackSound();
            p.vel.set(5*p.dir,-1);
            p.attack();
            ((Man)p).weapon.rotateTo(p.target.getMult(p.dir, p.dir));
        }
    },
    Throw(12,-1,0,0,Dialog.get("mvsDscThrow")){
        public void trigger(Unit p, int level){
            final Man t = (Man)p;
            if (!t.wieldingNone()) {
                int speed = 5;
                int damage = 0;
                if (t.weapon.weight != 0 && t.get("Stamina").require((int) (t.weapon.weight*1.5f))){
                    speed = 5+(15*t.get("Strength").get());
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
    Dart(12,0,0,1,Dialog.get("mvsDscDart")),
    Shoot(27,-1,0,0,Dialog.get("mvsDscShoot")){
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
    Snipe(27,-1,.5f,0,"Fire your weapon."){
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
    Triple(60,2,0,40,Dialog.get("mvsDscTriple")){
        public void trigger(Unit p, int level){
            Man t = (Man)p;
            t.attackSound();
            t.weapon.rotation = t.delay.time * -.3f * -t.weapon.dir;
            t.vel.set(t.dir * Global.cos(t.weapon.rotation)*15, -1);
            t.attack();
        }
    },
    Jump(47,1,3f,0,"Jump into the air."){
        public void onSet(Unit p){
            Sound.play("jump");
            p.vel.y -= 40;
            p.lastLand = new Line();
        }
    },
    Airstrike(27,1,1.5f,4,"Jump into the air while attacking."){
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
    Leap(40,2,3,60,Dialog.get("mvsDscLeap")){
        public void trigger(Unit p, int level){
            if (p.jump) {
                Sound.play("jump");
                p.vel.y -= 80;
                p.vel.x = p.getSpeed() * 10 * p.dir;
                p.lastLand = new Line();
            }
        }
    },
    Backstep(20,0,-1f,7,"Take a step backwards.");
    private String description;
    private int del, cooldown;
    private float cost, velx;
    Actions(){
        this(0);
    }
    Actions(int del){
        this(del,-1,0,0,"");
    }
    Actions(int del, float cost, float velx, int cooldown, String desc){
        this.del = del;
        this.cost = cost;
        this.velx = velx;
        this.cooldown = cooldown;
        this.description = desc;
    }
    public String getDescription(){
        String t = ""+((int)cost);
        return description + " [Cost:"+t+" "+"]";
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
    }

    @Override
    public void onSet(Unit p){
    }

    @Override
    public void onFinish(Unit p){
        p.setState(Idle);
    }

    public void trigger(Unit p, int p2){
    }

    public void onInterrupt(Unit p){
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