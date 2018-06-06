package com.ga.unit.skill;

import com.ga.game.Dialog;
import com.ga.game.Menu;
import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.ShadedPicture;
import com.ga.map.Shader;
import com.ga.map.Terrain;
import com.ga.tool.*;
import com.ga.unit.*;
import com.ga.unit.item.Item;
import com.ga.unit.item.Projectile;
import com.ga.unit.item.Weapon;
import com.ga.unit.man.BodyHumanoid;
import com.ga.unit.man.Man;
import com.ga.unit.man.Player;

import java.awt.*;
import java.util.ArrayList;

import static com.ga.unit.man.BodyHumanoid.*;

/**
 * Created by Ga on 7/28/2016. asd
 */
public enum Actions {
    Idle {
        public void onSet(Unit p){
            p.clip.resetHeight();
        }
        public void onSet(Man host){
            if (!host.get("Strength").isFull()) host.clip.setState(Actions.Exhausted.getClipName());
            onSet((Unit) host);
        }
        public void update(Unit p){
            if (Math.abs(p.vel.x) < 1f) p.vel.x = 0;
        }
    }, Left {
        public void onSet(Unit p){
            p.face(-1, false);
        }
        public void onSet(Man host){
            float speed = host.getSpeed();
            if (speed <= 0) host.clip.setState(Actions.Exhausted.getClipName());
            else host.clip.setMoveSpeed((int) Global.clamp(speed-1,-20,4));
            host.weapon.rotation = host.dir/2f;
            onSet((Unit) host);
        }
        public void update(Unit p){
            p.vel.x += p.getSpeed()*p.dir;
        }
    }, Right {
        public void onSet(Unit p){
            p.face(1, false);
        }
        public void onSet(Man host){
            float speed = host.getSpeed();
            if (speed <= 0) host.clip.setState(Actions.Exhausted.getClipName());
            else host.clip.setMoveSpeed((int) Global.clamp(speed-1,-20,4));
            host.weapon.rotation = host.dir/2f;
            onSet((Unit) host);
        }
        public void update(Unit p){
            p.vel.x += p.getSpeed()*p.dir;
        }
    }, Drop(12){
        public void onSet(Unit host){
            Line checkedLand = host.map.checkLand(new Line(host.pos.getAdd(0,4), host.pos.getAdd(0,600)), host.lastLand.depth);
            if (host.lastLand.opt != 2 && checkedLand != null){
                host.setDepth(host.lastLand.depth+.001f);
                host.pos.y+=4;
            }
        }
    }, Hit(14){
        public void onSet(Unit p){
            p.clip.organs[p.clip.BODY].pt.y += 14;
        }
        public void onInterrupt(Unit p){
            onFinish(p);
        }
        public boolean onFinish(Unit p){
            p.clip.resetHeight();
            return super.onFinish(p);
        }
    }, Dead(999){
        public void onSet(final Unit p){
            p.clip.organs[p.clip.BODY].pt.y = 0;
        }
        public void onInterrupt(Unit p){
            p.clip.resetHeight(); //clip animation too short
            onFinish(p);
        }
        public boolean onFinish(Unit p){
            p.onInteract = null;
            p.remove();
            p.stateSet(Dead);
            return false;
        }
    }, Down(100){
        public void onSet(final Unit p){
            p.clip.organs[p.clip.BODY].pt.y = 0;
            p.delay.time *= 1 + p.stats.getEffectLevel(Buffs.Bleed) * .2f;
        }
        public void onInterrupt(Unit p){
            onFinish(p);
        }
        public boolean onFinish(Unit p){
            p.clip.resetHeight(); //clip animation too short
            return super.onFinish(p);
        }
    }, Wield(60){
        public boolean onFinish(Man host){
            Player player = (Player)host;
            if (player.input.selectingItem == -1) {
                player.sheathe();
            } else if (player.input.selectingItem >= 0 && player.input.selectingItem <= 6) {
                Sound.play(player.map, player.pos, "unwear");
                int piece = player.input.selectingItem;
                if (player.sheathe() && !player.armor.isBare(piece)) {
                    player.setWeapon(player.armor.armor[piece]);
                    player.armor.armor[piece].start();
                    player.armor.set(null, piece);
                }
            } else if (player.input.selectingItem-10 < player.inventory.getItemSize()) {
                Item item = player.inventory.getItem(player.input.selectingItem-10);
                player.setWeapon(item);
                player.inventory.remove(item);
                item.start();
                player.input.selectingItem = -1;
            }
            return true;
        }
    }, Activate(20){
        public String getClipName(){
            return "Wield";
        }
        public void onSet(Man host){
            if (!host.armor.isBare(3)){
                host.armor.armor[3].trigger(host);
            }
        }
    }, Conjure{
    }, Use{
        public int getRange(Unit host){
            return ((Man)host).weapon.range;
        }
        public void onSet(Man host){
            host.stateSet(Idle);
            if (host.weapon.isUsable(false, 1)) host.weapon.use(host);
        }
    }, Consume(8){
    }, Exhausted{
    }, Interact(8){
        public void onSet(final Man host){
            host.delay.performer = new Performer() {
                public void perform() {
                    P reach = host.getHand();
                    ArrayList<Prop> obj = host.map.updater.collideInteractive(reach);
                    if (obj.size() != 0) {
                        for (Prop i : obj) {
                            if (i.onInteract != null) {
                                i.onInteract.interact(host);
                                break;
                            }
                        }
                    }
                }
                };
        }
    }, Swap(4){
    },
    Sprint(15,0,5,1,new int[]{RLEG, LLEG},new Tags(){{
        add(Tag.Athletics);
    }}){
        public void onSet(Man host){
            float speed = host.getSpeed();
            if (speed <= 0) host.clip.setState(Actions.Exhausted.getClipName());
            else host.clip.setMoveSpeed((int) Global.clamp(speed-1,-20,4));
            onSet((Unit) host);
        }
        public void update(Unit host){
            float speed = host.getSpeed();
            host.vel.x += speed * host.dir * (2f + host.stateLevel*.2f);
            if (host.stateDuration % 60 == 0 && !host.get("Strength").require(1)) host.setState(Actions.Idle);
        }
        public boolean onFinish(Unit host){
            return false;
        }
    },
    Rest(0,0,0,0,new Tags(){{
        add(Tag.Athletics);
    }}){
        public int getRange(Unit p){
            return 99999;
        }
        public void onSet(Unit host){
            host.stats.restTimer.reset();
            if (host.get("Strength").isFull()) host.stateSet(Idle);
//            else host.get("Strength").add(-1, false);
        }
        public void update(Unit host) {
            host.stats.rest(4+host.stateLevel);
        }
    },
    Melee(24,1,0,0,new Tags(){{
    }}){
        public int getRange(Unit host){
            return (int) host.getReach();
        }
        public void trigger(Unit host){
            Sound.play(host.map, host.pos, "swing_normal");
            host.attack();
        }
        public void trigger(Man host){
            host.attackSound();
            Line hitbox = host.clip.organs[0].getAdd(host.pos);
            int damage = Global.r.nextInt(3);
            for (Unit i : host.map.updater.collideUnit(hitbox.ptd, (int) hitbox.getLength()+64, host.faction)) {
                if (i.hit(hitbox, damage)) {
                    Sound.play(host.map, host.pos, "hit_punch");
                    new Thing(host.map, "hit_1", -1, 150) {
                        private int n = 0;
                        public void draw(Graphics2D g) {
                            if (n <= 7) {
                                super.draw(g);
                                setImage("hit_" + (n++) / 2);
                                if (n > 7) remove();
                            }
                        }
                    }.start().pos.set(hitbox.ptd);
                    break;
                }
            }
        }
    },
    Grab(15,3,12,6,new Tags(){{
        add(Tag.Strength);
    }}){
        public String getClipName(){
            return "Interact";
        }
        public void trigger(Man host){
            ArrayList<Unit> target = host.map.updater.collideUnit(host.getHand(), 64, host.faction);
            if (target != null && target.size() > 0) {
                for (Unit victim : target) {
                    if (victim.state == Down || victim.state == Hit) { //down or stunned
                        Sound.play(host.map, host.pos, "wear");
                        Weapon holder = new Weapon(host.map, null, 0, new Item.Material[]{Item.Material.Flesh}, Item.ItemType.Bashing, 32, 0, -20, 0) {
                            public String forceToString() {
                                return "";
                            }
                            public void onUnequip(Man host) {
                                super.onUnequip(host);
                                save = false;
                                victim.mobile = true;
                                victim.delay.set(40);
                            }
                            public Line getLine(){
                                int len = dir * victim.getSize();
                                return new Line(pos, pos.getAdd(len*Global.cos(rotation), len*Global.sin(rotation)));
                            }
                            public void update() {
                                super.update();
                                victim.pos = pos;
                            }
                        };
                        holder.start();
                        holder.image = victim.image;
                        holder.onInteract = null;
                        holder.weight = Math.max(1, victim.getSize() / 8);
                        holder.save = false;
                        host.setWeapon(holder);
                        victim.mobile = false;
                        victim.stateSet(Down);
                        victim.delay.set(-1);
                        return;
                    }
                }
            }
        }
        public boolean onFinish(Unit host){
            return false;
        }
    },
    Charge(16,3,12,3,new Tags(){{
        add(Tag.Strength);
        add(Tag.Athletics);
    }}){
        public void onSet(Man host){
            host.delay.time += host.stateLevel*8;
            if (host.dir < 0) host.vel.x = Math.min(-4, host.vel.x);
            else host.vel.x = Math.max(4, host.vel.x);
        }
        public void update(Unit host){
            host.vel.x = Global.clamp(host.vel.x*1.1f, -12, 12);
            ArrayList<Unit> target = host.map.updater.collideUnit(host.getHand(), 64, host.faction);
            if (target != null && target.size() > 0){
                Sound.play(host.map, host.pos, "hit_bash");
                target.get(0).stateSet(Down);
            }
        }
    },
    Hold(45,3,3,3,new Tags(){{
        add(Tag.Strength);
    }}){
        public boolean onFinish(Unit host){
            return false;
        }
        public void onSet(Man host){
            host.damageMod = 0;
        }
        public void update(Man host){
            if (host.stateDuration > 12) {
                Unit victim = host.attack();
                if (victim != null) {
                    victim.stateSet(Down);
                    victim.vel.x = -victim.vel.x * 2;
                    victim.vel.y -= 8;
                    host.stateSet(Idle);
                }
            }
        }
    },
    Backstep(0,-1,0,0,new Tags(){{
    }}){
        public int getRange(Unit host){
            return 60;
        }
        public void update(Unit host){
            host.vel.x += host.getSpeed() * -host.dir * 1.2f;
        }
    },
    Thrust(26,1,0,0,new int[]{FIST}, new Tags(){{
        add(Tag.Strength);
    }}){
        public void onSet(Unit p){
            if (p.vel.x < 0 != p.dir < 0) p.vel.x = 0;
            p.damageMod = 1 + Math.abs(p.vel.x)/3;
        }
        public void trigger(Man host){
            host.attackSound();
            host.attack();
        }
    },
    Slash(25,1,0,0,new int[]{FIST},new Tags(){{
        add(Tag.Strength);
    }}){
        public void onSet(Man host){
            Item weapon = host.weapon;
            weapon.rotation = -(weapon.dir * 10);
        }
        public void update(Man host){
            if (host.delay.time <= 25) {
                if (host.delay.time == 17) host.vel.x = 4 * host.dir;
                Item item = host.weapon;
                int del = Math.max(0, 25 - host.delay.time);
                if (host.delay.time == 13) host.attackSound();
                float test = host.target.getMult(host.dir, host.dir).getAtan();
                float rot = test - ((item.dir * 10) + del * del * .03f * -item.dir);
                if (host.delay.time > 12) item.rotation = rot;
                if (Math.abs(rot) < 8) {
                    if (host.attack() != null) {
                        host.damageMod = 0;
                    }
                }
            }
        }
    },
    Kick(3,1,2,2,new int[]{RLEG},new Tags(){{
        add(Tag.Strength);
    }}){
        public void trigger(Man host){
            Sound.play(host.map, host.pos, "swing_normal");
            Line hitbox = host.clip.organs[1].getAdd(host.pos);
            for (Unit i : host.map.updater.collideUnit(host.pos, (int) hitbox.getLength()+64, host.faction)) {
                if (i.state != Actions.Down && i.hit(hitbox, 1)) {
                    i.stateSet(Actions.Hit);
                    Sound.play(host.map, host.pos, "hit_whip");
                    new Thing(host.map, "hit_0", -1, 150) {
                        private int n = 0;
                        public void draw(Graphics2D g) {
                            if (n <= 7) {
                                super.draw(g);
                                setImage("hit_" + (n++) / 2);
                                if (n > 7) remove();
                            }
                        }
                    }.start().pos = hitbox.ptd;
                    i.vel.x = (host.pos.x > i.pos.x ? -1 : 1) * (host.stateLevel*3);
                    i.vel.y -= host.stateLevel*6;
                    break;
                }
            }
        }
    },
    Sidekick(40,3,7,4,new int[]{RLEG},new Tags(){{
        add(Tag.Strength);
    }}){
        public void trigger(Man host){
            Sound.play(host.map, host.pos, "swing_normal");
            int power = 20+host.stateLevel*2;
            host.vel.y += power*Global.sin(host.target.getAtan());
            host.vel.x += power*Global.cos(host.target.getAtan());
        }
        public void update(Man host){
            if (host.delay.time > 14 && Math.abs(host.vel.x)>2){
                Line hitbox = host.clip.organs[2].getAdd(host.pos.getAdd(host.vel));
                for (Unit i : host.map.updater.collideUnit(host.pos, (int) hitbox.getLength() + 64, host.faction)) {
                    if (i.hit(hitbox, 7)) {
                        Sound.play(host.map, host.pos, "hit_bash");
                        new Thing(host.map, "hit_1", -1, 150) {
                            private int n = 0;
                            public void draw(Graphics2D g) {
                                if (n <= 7) {
                                    super.draw(g);
                                    setImage("hit_" + (n++) / 2);
                                    if (n > 7) remove();
                                }
                            }
                        }.start().pos = hitbox.ptd;
                        i.stateSet(Hit);
                        int force = 24;
                        i.vel = hitbox.getAtan(force);
                        Particle.explode(host.map, "Fire", i.getDepth(), hitbox.ptd, new P(), 6, 12, 0, false, 4);
                        host.stateSet(Idle);
                        host.clip.setState("Sidekick");
                        break;
                    }
                }
            }
        }
    },
    Punch(12,1,0,3,new int[]{FIST},new Tags(){{
        add(Tag.Strength);
    }}){
        public int getRange(Unit p){
            return 36;
        }
        public void trigger(Man host){
            Sound.play(host.map, host.pos, "swing_normal");
            Line hitbox = host.clip.organs[0].getAdd(host.pos);
            int damage = Global.r.nextInt(2+host.stateLevel);
            for (Unit i : host.map.updater.collideUnit(hitbox.ptd, (int) hitbox.getLength()+64, host.faction)) {
                if (i.hit(hitbox, damage)) {
                    Sound.play(host.map, host.pos, "hit_punch");
                    new Thing(host.map, "hit_1", -1, 150) {
                        private int n = 0;
                        public void draw(Graphics2D g) {
                            if (n <= 7) {
                                super.draw(g);
                                setImage("hit_" + (n++) / 2);
                                if (n > 7) remove();
                            }
                        }
                    }.start().pos.set(hitbox.ptd);
                    break;
                }
            }
        }
    },
    Whirlwind(35,3,6,7,new int[]{FIST, OFIST},new Tags(){{
        add(Tag.Strength);
    }}){
        public void onSet(final Unit p){
            p.delay.time += 8 * p.stateLevel;
            p.damageMod = 1 + ((float) p.stateLevel * .5f);
            p.delay.performer = new Performer(){
                public void perform(){
                    p.setState(Actions.Rest);
                }
            };
            p.buff(Buffs.Evade, 1);
        }
        public void update(Man host) {
            Faction tmp = host.faction;
            host.faction = Faction.Destructive;
            host.weapon.rotation = host.delay.time * -host.weapon.dir;
            host.attack();
            host.faction = tmp;
        }
        public void trigger(Unit p){
            if (p.delay.time % 4 == 0) p.attackSound();
        }
    },
    Stab(40,2,12,3,new int[]{FIST},new Tags(){{
        add(Tag.Strength);
    }}){
        public void onSet(Unit p){
            p.damageMod = 3 + p.stateLevel;
        }
        public void trigger(Unit p){
            p.attackSound();
            p.attack();
        }
    },
    Dash(35,2,24,2,new int[]{RLEG, LLEG},new Tags(){{
        add(Tag.Athletics);
        add(Tag.Strength);
    }}){
        public int getRange(Unit p){
            return 160;
        }
        public void onSet(Unit p){
            p.damageMod = p.stateLevel * .5f;
            p.vel.x -= p.getSpeed() * 4 * p.dir;
            p.delay.time += 2 * p.stateLevel;
            p.delay.max += 2 * p.stateLevel;
        }
        public void update(Unit p) {
            if (p.vel.x > 0 == p.dir > 0 && Math.abs(p.vel.x) >= p.getSpeed() * 2){
                if (p.attack() != null){
                    p.vel.x = 0;
                    p.damageMod = 0;
                }
            }
        }
        public void trigger(Man host){
            host.attackSound();
            host.vel.set(20 * host.dir * (1+(host.stateLevel*.2f)), -5);
        }
    },
    Lunge(24,1,12,5,new int[]{LLEG, RLEG, FIST},new Tags(){{
        add(Tag.Strength);
    }}){
        public void onSet(Man host){
            host.damageMod = 0;
        }
        public void trigger(Unit host){
            host.attackSound();
            host.vel.set((8+host.stateLevel*2)*host.dir,-4);
            Unit tgt = host.attack();
            if (tgt != null){
                tgt.buff(Buffs.Stun, 2);
            }
        }
    },
    Underhand(32,0,24,3,new int[]{LLEG, RLEG, FIST, OFIST},new Tags(){{
        add(Tag.Strength);
    }}){
        public void onSet(Man host){
            host.damageMod = 2 + host.stateLevel/2;
        }
        public void trigger(Man host){
            host.attackSound();
            host.vel.set(5*host.dir,-1);
            Unit tgt = host.attack();
            if (tgt != null){
                Particle.explode(host.map, "Fire", tgt.getDepth(), host.weapon.getLine().ptd, new P(), 6, 12, 0, false, 4);
            }
        }
    },
    Identify(1,-1,0,0,new Tags()){
        public String getClipName(){
            return "Mark";
        }
        public void onSet(Unit host){
            host.delay.set(80);
            if (host == Global.player) {
                ArrayList<Unit> t = host.map.updater.collideUnit(host.target.getAdd(host.pos), 40, Faction.Destructive);
                if (t.size() > 0){
                    Unit target = t.get(0);
                    Menu.menu.showDialog(target.name,target.getDescription());
                }
            }
        }
    },
    Throw(12,1,0,0,new int[]{FIST},new Tags(){{
        add(Tag.Strength);
    }}){
        public String getClipName(){
            return "Thrust";
        }
        public void trigger(final Man host){
            if (!host.wieldingNone()) {
                int speed = 5;
                int damage = Math.min(host.stateLevel, host.weapon.weight);
                if (damage > 0){
                    speed = Math.min(120, 10*Math.max(1,host.stateLevel - host.weapon.weight));
                }
                Projectile pj = new Projectile(host.map, null, host.pos.getAdd(host.clip.organs[FIST].ptd), host.pos.getAdd(host.target), speed) {
                    private Item wpn = host.weapon;
                    @Override
                    public void stuck() {
                        if (tangible) Particle.groundSlam(host.map, pos, getDepth());
                        wpn.gravity = true;
                        wpn.onField();
                        remove();
                    }
                    public void onAttack(Unit p) {
                        wpn.trigger(p);
                        Sound.play(host.map, host.pos, "hit_bash");
                        if (rand.nextInt(damage+1) > p.get("Strength").get()) p.stateSet(Down);
                    }
                    public void update(){
                        super.update();
                        wpn.rotation = rotation;
                    }
                };
                pj.setDepth(host.getDepth());
                pj.gravity = true;
                pj.rotSpeed = .03f;
                pj.faction = host.faction;
                pj.damage = damage;
                Item item = host.weapon;
                host.setWeapon(null);
                item.pos = pj.pos;
                item.gravity = false;
            }
        }
    },
    Dart(12,1,2,4,new int[]{FIST},new Tags(){{
        add(Tag.Strength);
    }}),
    Shoot_Arrow(120,1,0,1,new int[]{FIST, OFIST},new Tags(){{
        add(Tag.Strength);
    }}){
        public void onSet(Man host){
            host.weapon.rotateTo(host.target.getMult(host.dir, host.dir));
        }
        public void trigger(final Man host){
            P tip = host.weapon.getLine().ptd;
            final Item wpn = host.weapon;
            int speed = (int) (36 + (Math.min(6,host.get("Strength").get()) * 10) - (host.rand.nextFloat() * (60 / host.stateLevel)));
            float calcY = 0;//Math.abs(t.target.x / speed) * Settings.gravity;
            final Projectile pj = new Projectile(host.map, "Arrow", tip, host.pos.getAdd(host.target).add(0, -calcY), speed) {
                public void onField() {
                }
                public void onAttack(Unit p) {
                    wpn.trigger(p);
                    Sound.play(host.map, host.pos, "hit_bloody");
                    remove();
                }
                public void collide() {
                    super.collide();
                }
                public void update() {
                    super.update();
                    if (tangible || lastLand.depth == 0) rotateTo(vel);
                }
            };
            pj.setDepth(300);
            pj.lastLand = host.lastLand;
            pj.rotSpeed = 0;
            pj.rotation = pj.vel.getAtan();
            pj.gravity = true;
            pj.faction = host.faction;
            pj.damage = (int) ((host.weapon.getDamage() * 2) * (speed * .1f));
            host.weapon.rotation += host.dir * .3f;
            host.stateSet(Actions.Idle);
        }
    },
    Shoot_Handgun(27,0,2,0,new int[]{FIST},new Tags(){{
    }}){
        public void onSet(Man host){
            host.weapon.rotateTo(host.target.getMult(host.dir, host.dir));
            host.weapon.rotation += .1 * host.dir;
        }
        public void trigger(Man host){
            P tip = host.weapon.getLine().ptd;
            P target = host.pos.getAdd(host.target);
            final Projectile pj = new Projectile(host.map, "Bullet", tip, target, (int) (120 + (host.rand.nextFloat() * 40))) {
                @Override
                public void onCollide(Line l) {
//                    host.say(pos.toString() + "   " + l.getCrossPoint(new Line(pos, pos.getAdd(vel))).toString());
//                    Particle.smoke(map, new Line(pos, pos.getAdd(vel)).getCrossPoint(l), getDepth());
                    remove();
                    mobile = false;
                }
            };
            pj.delay = new EventTimer(3, false, new Performer() {
                public void perform() {
                    pj.remove();
                }
            });
            pj.rotSpeed = 0;
            pj.rotateTo(host.target.getSubtract(tip).add(host.pos));
            pj.faction = host.faction;
            pj.damage = host.weapon.getDamage();
            Particle.explode(host.map, "Spark", pj.getDepth(), tip, pj.vel, 5, 10, 15, false, 3);
            float start = host.rand.nextFloat();
            pj.pos.add(pj.vel.getMult(start, start));
            host.weapon.rotation += host.dir * -.3f;
        }
    },
    Shoot_Rifle(27,0,0,0,new int[]{FIST, OFIST},new Tags(){{
    }}){
        public void onSet(Man host){
            host.weapon.rotateTo(host.target.getMult(host.dir, host.dir));
            host.weapon.rotation += .1 * host.dir;
        }
        public void trigger(Man host){
            P tip = host.weapon.getLine().ptd;
            final Projectile pj = new Projectile(host.map, "Bullet", tip, host.pos.getAdd(host.target), (int) (150 + (host.rand.nextFloat() * 50))) {
                @Override
                public void onCollide(Line l) {
                    remove();
                }
            };
            pj.delay = new EventTimer(4, false, new Performer() {
                public void perform() {
                    pj.remove();
                }
            });
            pj.dir = host.dir;
            pj.rotSpeed = 0;
            pj.rotateTo(host.target.getSubtract(tip).add(host.pos));
            pj.faction = host.faction;
            pj.damage = host.weapon.getDamage();
            Particle.explode(host.map, "Spark", pj.getDepth(), tip, pj.vel, 5, 10, 15, false, 3);
            float start = host.rand.nextFloat();
            pj.pos.add(pj.vel.getMult(start, start));
            host.weapon.rotation += host.dir * -.3f;
        }
    },
    Jump(47,1,0,1,new int[]{LLEG, RLEG},new Tags(){{
        add(Tag.Athletics);
    }}){
        public void onSet(Unit host){
            float x = Math.abs(host.vel.x);
            float t = Math.max(0, 24 - x) + (float)(Math.log(host.stateLevel*2)/Math.log(2) * 4);
            Sound.play(host.map, host.pos, "jump");
            host.vel.y -= t;
            float speed = host.getSpeed();
            if (host.vel.x != 0) host.vel.x = Math.max(Math.abs(host.vel.x)*5, speed*40)*(host.vel.x>0?1:-1);
            if (Math.abs(host.vel.x) >= -host.vel.y) host.stateSet(Down);
        }
    },
    Mark(80,0,1,2,new Tags(){{
        add(Tag.Strength);
    }}){
        public void trigger(Unit host){
            ArrayList<Unit> t = host.map.updater.collideUnit(host.target.getAdd(host.pos),64,Faction.Destructive);
            if (t.size() > 0) t.get(0).buff(Buffs.Mark, host.stateLevel);
        }
    },
    Warcry(60,3,20,6,new Tags(){{
        add(Tag.Strength);
    }}){
        public void onSet(Unit host){
            ArrayList<Unit> t = host.map.updater.collideUnit(host.pos,240+(120*host.stateLevel),Faction.Destructive);
            for (Unit i : t){
                if (i != host) i.buff(Buffs.Stun,3+host.stateLevel);
            }
        }
    },
    Evade(40,1,6,3,new Tags(){{
        add(Tag.Strength);
    }}){
        public String getClipName(){
            return "Conjure";
        }
        public void onSet(Unit p){
            p.buff(Buffs.Evade,2);
        }
    },
    Airstrike(27,1,3,5,new int[]{LLEG, RLEG, FIST},new Tags(){{
        add(Tag.Strength);
    }}){
        protected int canSet(Unit p){
            if (p.state == Jump) {
                if (p.getCooldown(this).isReady()){
                    if ((p.stateLevel = p.getStateLevel(this)) > 0){
                        if (!p.get("Strength").require(getCost())) return 2;
                    } else return 5;
                } else return 3;
            } else return 1;
            return 0;
        }
        public void onSet(Unit p){
            p.damageMod = 1 + p.stateLevel * .5f;
            p.vel.y -= 20;
        }
        public void update(Man host){
            if (host.grounded){
                host.weapon.rotation = 1f*host.dir;
                host.attack();
                Stat stamina = host.get("Strength");
                int b4 = stamina.get();
                if (!stamina.require(2)){
                    stamina.set(0,false);
                    host.stateSet(Down);
                    host.delay.time = (4-b4)*16;
                } else {
                    host.stateSet(Idle);
                    for (Unit i : host.map.updater.collideUnit(host.pos, 64, host.faction)) {
                        Sound.play(host.map, host.pos, "hit_bash");
                        new Thing(host.map, "hit_1", -1, 150) {
                            private int n = 0;
                            public void draw(Graphics2D g) {
                                if (n <= 7) {
                                    super.draw(g);
                                    setImage("hit_" + (n++) / 2);
                                    if (n > 7) remove();
                                }
                            }
                        }.start().pos = host.weapon.getLine().ptd;
                        i.stateSet(Down);
                        Particle.explode(host.map, "Fire", i.getDepth(), host.weapon.getLine().ptd, new P(), 6, 12, 0, false, 4);
                        break;
                    }
                }
            } else {
                host.weapon.rotation = host.delay.time * -host.weapon.dir;
                if (host.delay.time % 4 == 0) host.attackSound();
                host.attack();
            }
        }
    },
    Downstrike(40,1,3,4,new int[]{LLEG, RLEG, FIST, OFIST},new Tags(){{
        add(Tag.Strength);
    }}){
        public String getClipName(){
            return "Wield";
        }
        protected int canSet(Unit p){
            if (!p.grounded) {
                if (p.getCooldown(this).isReady()){
                    if ((p.stateLevel = p.getStateLevel(this)) > 0){
                        if (!p.get("Strength").require(getCost())) return 2;
                    } else return 5;
                } else return 3;
            } else return 1;
            return 0;
        }
        public void onSet(Unit host){
            host.damageMod = 2;
        }
        public void update(Man host){
            if (host.grounded){
                for (Unit i : host.map.updater.collideUnit(host.pos, host.getSize(), host.faction)) {
                    if (i.hit(i.getPosLine(), 3)) {
                        i.stateSet(Actions.Down);
                        i.delay.time = 40;
                        Sound.play(host.map, host.pos, "hit_whip");
                        new Thing(host.map, "hit_0", -1, 150) {
                            private int n = 0;
                            public void draw(Graphics2D g) {
                                if (n <= 7) {
                                    super.draw(g);
                                    setImage("hit_" + (n++) / 2);
                                    if (n > 7) remove();
                                }
                            }
                        }.start().pos = i.pos;
                        i.vel.x = (host.pos.x > i.pos.x ? -1 : 1) * (host.stateLevel*3);
                        i.vel.y -= host.stateLevel*6;
                        break;
                    }
                }
                host.stateSet(Hit);
                host.clip.setState("Conjure");
            }
        }
    },
    Focus(1,0,13,2,new Tags(){{
        add(Tag.Strength);
        add(Tag.Energy);
    }}){
        public String getClipName(){
            return "Conjure";
        }
        public void trigger(Unit host){
            Sound.play(host.map, host.pos, "heal");
            host.get("Strength").heal();
            host.get("Agility").heal();
            host.get("Intelligence").heal();
            host.buff(Buffs.Bleed, 1);
        }
    },
    Leap(40,2,40,4,new int[]{LLEG, RLEG},new Tags(){{
        add(Tag.Strength);
    }}){
        public int getRange(Unit host){
            return 1200;
        }
        public void trigger(Unit host){
            Sound.play(host.map, host.pos, "jump");
            host.vel.y -= 80;
            host.vel.x = 80 * host.dir;
        }
    },
    Infuse(18,3,1,1,new int[]{FIST},new Tags(){{
        add(Tag.Conjuration);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void update(Man host){
            Particle.frozen(host.map, host.pos.getAdd(host.clip.organs[FIST].ptd), host.getDepth()+.01f);
            Particle.frozen(host.map, host.pos.getAdd(host.clip.organs[BodyHumanoid.OFIST].ptd), host.getDepth()+.01f);
        }
        public boolean onFinish(Unit host){
            return false;
        }
        public void trigger(final Man host){
            Sound.play(host.map, host.pos, "freeze");
            String target = host.getConversation();
            Prop t = Stock.createByNameFromReg(host.map, target, -1);
            if (host.weapon instanceof Weapon && host.traits.getTraitLevel(Traits.Trait.Infusion)>2 && host.traits.has(Traits.Trait.Enchanting) && host.requireMana(1000)){
                int quality = ((Weapon) host.weapon).getQuality();
                if (Global.r.nextInt(quality+1) == 0){
                    ((Weapon) host.weapon).enhance();
                    host.say(host.weapon.getFullName()+" was enchanted to +"+(quality+1)+"!");
                } else {
                    host.weapon.remove();
                    host.say(host.weapon.getFullName()+" shattered!");
                }
            } else if (host.weapon.material != Item.Material.Crystal){
                if (host.requireMana(100)) {
                    host.weapon.setMaterial(Item.Material.Crystal);
                    host.say(host.weapon.getFullName() + " was infused with mana.");
                } else host.say("Requires 100 mana to infuse items!");
            } else {
                if (t != null) {
                    if (host.requireMana(20)) {
                        t.start();
                        if (t instanceof Item) {
                            host.setWeapon((Item) t);
                            ((Item) t).setMaterial(Item.Material.Crystal);
                        } else {
                            t.pos.set(host.pos);
                            int ctype = t.image.getCenterType();
                            t.image = new ShadedPicture(t.rand, t.name, Shader.Color);
                            ((ShadedPicture) t.image).shade(new Color(48, 156, 200, 150).getRGB());
                            t.image.setCenterType(ctype);
                        }
                        if (host.traits.getTraitLevel(Traits.Trait.Infusion)<3 || host.stateLevel < 3) {
                            t.save = false;
                            Global.timers.add(new EventTimer(2 + host.stateLevel, false, new Performer() {
                                public void perform() {
                                    if (!(t instanceof Item) || host.inventory.remove((Item) t) == null) t.remove();
                                }
                            }));
                        }
                    } else host.say("Requires 20 mana to create items!");
                } else {
                    host.stateSet(Fracture);
                }
            }
        }
    },
    Fracture(20,1,0,1,new int[]{FIST},new Tags(){{
        add(Tag.Conjuration);
    }}){
        public String getClipName(){
            return "Mark";
        }
        public void onSet(Man host){
            if (host.requireMana(4)){
                Sound.play(host.map, host.pos, "shadowstep");
                final Terrain r = new Terrain(host.map, "_60,100,120,120", Shader.None, 50);
                int power = 40 + (40 * host.stateLevel);
                final P target = host.target.getAdd(host.pos);
                ArrayList<P> prefracture = new ArrayList<>();
                for (Line i : host.map.land) {
                    if (i.opt == 20 && i.pt.getSubtract(target).getDist() < 280) {
                        prefracture.add(i.pt);
                    }
                }
                P[] points = new P[3];
                Line[] lines = new Line[3];
                P highest = target.getAdd(0, power + 1);
                for (int i = 0; i < 3; i++) {
                    points[i] = target.disperse(power, power);
                    r.addPoint(points[i].x, points[i].y);
                    if (points[i].y < highest.y) highest = points[i];
                }
                boolean reverse = false;
                for (int i = 0; i < 3; i++) {
                    lines[i] = new Line(points[i], points[i + 1 < 3 ? i + 1 : 0]) {
                        public String toString() {
                            return "";
                        }
                    };
                    lines[i].depth = host.lastLand.depth;
                    lines[i].opt = 20;
                    if (!lines[i].clockwise() && (lines[i].pt == highest || lines[i].ptd == highest)) reverse = true;
                    host.map.land.add(lines[i]);
                }
                if (reverse) for (Line i : lines) i.reverse();
                int preSize = prefracture.size();
                Line[] contlines = new Line[4];
                if (preSize > 0) {
                    for (Line i : lines) prefracture.add(i.pt);
                    points = new P[4];
                    P top = new P(0, host.map.size.y + 1), left = new P(host.map.size.x + 1, 0), right = new P(-1, 0), bottom = new P(0, -1);
                    for (P t : prefracture) {
                        if (t.y <= top.y) top = t;
                        if (t.y >= bottom.y) bottom = t;
                        if (t.x <= left.x) left = t;
                        if (t.x >= right.x) right = t;
                    }
                    points[0] = top;
                    points[1] = right;
                    points[2] = bottom;
                    points[3] = left;
                    for (int i = 0; i < 4; i++) r.addPoint(points[i].x, points[i].y);
                    for (int i = 0; i < 4; i++) {
                        contlines[i] = new Line(points[i], points[i + 1 < 4 ? i + 1 : 0]) {
                            public String toString() {
                                return "";
                            }
                        };
                        contlines[i].depth = host.lastLand.depth;
                        contlines[i].opt = 20;
                        host.map.land.add(contlines[i]);
                    }
                }
                r.render();
                r.start();
                Global.timers.add(new EventTimer(6, false, new Performer() {
                    public void perform() {
                        for (int i = 0; i < 3; i++) {
                            Particle.frozen(host.map, lines[i].pt, lines[i].depth);
                            host.map.land.remove(lines[i]);
                        }
                        if (preSize > 0) {
                            for (int i = 0; i < 4; i++) {
                                Particle.frozen(host.map, contlines[i].pt, contlines[i].depth);
                                host.map.land.remove(contlines[i]);
                            }
                        }
                        host.map.updater.remove(r);
                    }
                }));
            }
        }
    },
    Shock(2,0,5,1,new int[]{FIST, OFIST},new Tags(){{
        add(Tag.Electric);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void trigger(final Unit host){
            Particle.explode(host.map, "Glare",host.getDepth(),host.getHand(),new P(0,-1),8,20,12,true,4);
            Sound.play(host.map, host.pos, "shadowstep");
            P origin = host.getHand();
            ArrayList<Unit> t = host.map.updater.collideUnit(origin,800,host.faction);
            int size = t.size();
            if (size > 0) {
                Unit target = t.get(Global.r.nextInt(size));
                P targetPos = target.getRandomPoint();
                host.target = targetPos.getSubtract(host.pos);
                int num = Global.r.nextInt(6) + 3;
                float div = host.target.getDist() / num;
                float atan = host.target.getAdd(host.pos.getSubtract(origin)).getAtan();
                P[] points = new P[num * 2 - 2];
                for (int ii = 0; ii < Global.r.nextInt(2) + 2; ii++) {
                    Terrain lightning = new Terrain(host.map, "_255,255,0,255", Shader.None, host.getDepth()){
                        public void draw(Graphics2D g) {
                            super.draw(g);
                            remove();
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
                    lightning.start();
                }
                Particle.explode(host.map, "Glare",target.getDepth(),targetPos,new P(0,-1),20,20,12,true,7);
                target.buff(Buffs.Shock, host.stateLevel);
                target.buff(Buffs.Stun, 1);
            }
        }
    },
    Lightning(32,0,17,1,new int[]{FIST, OFIST},new Tags(){{
        add(Tag.Electric);
        add(Tag.Channeling);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public boolean onFinish(Unit host){
            Particle.explode(host.map, "Glare",host.getDepth(),host.getHand(),new P(0,-1),20,20,12,true,4);
            return false;
        }
        public void trigger(Unit host){
            Particle.explode(host.map, "Glare",host.getDepth(),host.getHand(),new P(0,-1),20,20,12,true,4);
        }
        public void update(final Unit host){
            if (host.delay.time > 0) return;
            Sound.play(host.map, host.pos, "shadowstep");
            P origin = host.getHand();
            ArrayList<Unit> t = host.map.updater.collideUnit(origin,800,host.faction);
            int size = t.size();
            if (size > 0) {
                Unit target = t.get(Global.r.nextInt(size));
                P targetPos = target.getRandomPoint();
                host.target = targetPos.getSubtract(host.pos);
                int num = Global.r.nextInt(6) + 3;
                float div = host.target.getDist() / num;
                float atan = host.target.getAdd(host.pos.getSubtract(origin)).getAtan();
                P[] points = new P[num * 2 - 2];
                for (int ii=0;ii<2;ii++) {
                    Terrain lightning = new Terrain(host.map, "_255,255,0,255", Shader.None, host.getDepth()) {
                        public void draw(Graphics2D g) {
                            super.draw(g);
                            remove();
                        }
                    };
                    for (int i = 0; i < points.length; i++) {
                        if (i < num) {
                            float x = Global.cos(atan) * div * i, y = Global.sin(atan) * div * i;
                            if (i != 0 && i != num-1) {
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
                    lightning.start();
                }
                if (host.stateDuration % Math.max(1,4-host.stateLevel) == 0) target.buff(Buffs.Shock, 1);
            }
        }
    },
    Enchant(60,1,12,1,new int[]{FIST},new Tags(){{
        add(Tag.Enchant);
    }}){
        public void onSet(Unit host){
            if (host.stats.getEffectLevel(Buffs.Bleed) > 3) host.stateSet(Idle);
        }
        public int getRange(Unit host){
            return 900;
        }
        public String getClipName(){
            return "Buff";
        }
        public void trigger(Man host){
            Sound.play(host.map, host.pos, "spell");
            ArrayList<Buffs> buffs = Buffs.getByLevel(host.stateLevel);
            Tags holding = new Tags();
            holding.addAll(host.tags);
            holding.addAll(host.weapon.tags);
            holding.addAll(host.armor.armor[3].tags);
            for (int i=0;i<buffs.size();i++){
                Tags tags = buffs.get(i).tags;
                if (tags.isEmpty() || !holding.containsAll(tags)) buffs.remove(i--);
            }
            ArrayList<Unit> t = host.map.updater.collideUnit(host.target.getAdd(host.pos),64,Faction.Destructive);
            if (t.size() > 0) {
                if (buffs.size() == 1){ //Corona
                    host.buff(Buffs.Bleed, 1);
                    t.get(SyncRand.r.nextInt(t.size())).buff(buffs.get(0), host.stateLevel);
                } else {
                    String[] buffNames = new String[buffs.size()];
                    Performer[] performers = new Performer[buffs.size()];
                    for (int i = 0; i < buffNames.length - 1; i++) {
                        Buffs buff = buffs.get(i);
                        buffNames[i] = buff.name() + " - Power " + buff.value;
                        performers[i] = new Performer() {
                            public void perform() {
                                host.buff(Buffs.Bleed, 1);
                                t.get(SyncRand.r.nextInt(t.size())).buff(buff, host.stateLevel);
                            }
                        };
                    }
                    Menu.menu.showDialog("Available Enchantments", "", buffNames, performers);
                }
            }
        }
    },
    Calling(40,2,24,2,new Tags(){{
        add(Tag.Conjuration);
    }}){
        public int getRange(Unit host){
            return 900;
        }
        public String getClipName(){
            return "Buff";
        }
        public void trigger(Unit host){
            Sound.play(host.map, host.pos, "shadowstep");
            for (int i = 0; i < host.rand.nextInt(Math.max(1, host.stateLevel)); i++) {
                Unit p = (Unit) Stock.Rover.create(host.map, -1,-1);
                p.brain.setTask(Behavior.Hunt);
                p.brain.target = host.brain.target;
                p.pos.set(host.pos);
                p.dir = host.dir;
                p.buff(Buffs.Banishment, 2 + host.stateLevel);
                p.faction = host.faction;
                p.start();
            }
        }
    },
    Magic_Arrow(40,1,2,1,new Tags(){{
        add(Tag.Conjuration);
    }}){
        public int getRange(Unit host){
            return 600;
        }
        public String getClipName(){
            return "Shoot_Arrow";
        }
        public void trigger(final Man host){
            P tip = host.weapon.getLine().ptd;
            final Item wpn = host.weapon;
            final Projectile pj = new Projectile(host.map, "Arrow", tip, host.pos.getAdd(host.target), 80) {
                public void onField() {
                }
                public void onAttack(Unit p) {
                    Sound.play(host.map, host.pos, "hit_bloody");
                    remove();
                }
                public void collide() {
                    super.collide();
                }
                public void update() {
                    super.update();
                    if (tangible || lastLand.depth == 0) rotateTo(vel);
                }
            };
            pj.image = new ShadedPicture(pj.rand, pj.name, Shader.Color);
            ((ShadedPicture) pj.image).shade(new Color(255,255,255,255).getRGB());
            pj.setDepth(300);
            pj.lastLand = host.lastLand;
            pj.rotSpeed = 0;
            pj.rotation = pj.vel.getAtan();
            pj.gravity = true;
            pj.faction = host.faction;
            pj.damage = host.stateLevel;
            host.weapon.rotation += host.dir * .3f;
            host.stateSet(Actions.Idle);
        }
        public void update(Man host){
            Particle.sparkle(host.map, "Glare", host.getDepth(), host.getHand(), 4, 3);
        }
    },
    Shadowtwin(240,3,30,1,new Tags(){{
        add(Tag.Void);
        add(Tag.Conjuration);
    }}){
        public int getRange(Unit host){
            return 900;
        }
        public String getClipName(){
            return "Buff";
        }
        public void onSet(Unit host){
            host.buff(Buffs.Shock, 1);
        }
        public void trigger(final Unit host){
            Sound.play(host.map, host.pos, "shadowstep");
            for (int i=0;i<host.rand.nextInt(host.stateLevel+1);i++) {
                Man p = new Man(host.map, Stock.Man.ordinal()+"", host.seed) {
                    @Override
                    public void die() {
                        super.die();
                        remove();
                        Particle.explode(host.map, "Smoke", getDepth(), pos, new P(), 80, 10, 0, true, 80);
                    }
                };
                p.factoryId = Stock.Man.ordinal();
                p.subId = 0;
                p.load(host.toString());
                p.clip.organs[5].setImage("Player");
                p.brain = Intelligence.Hunter.make(p);
                p.buff(Buffs.Banishment,4+(int)(host.stateLevel*.4f));
                p.dir = -host.dir;
                p.faction = host.faction;
                p.clip.setState("Buff");
                p.start();
            }
        }
    },
    Blink(4,1,0,1,new Tags(){{
        add(Tag.Void);
    }}){
        public String getClipName(){
            return "Conjure";
        }
        public void onSet(Unit host){
            Sound.play(host.map, host.pos, "shadow");
            Particle.smoke(host.map, host.pos,host.getDepth());
            for (int i=0;i<24;i++) {
                int range = Global.r.nextInt(host.stateLevel * 320) + 128;
                float rot = Global.r.nextFloat() * 6.28318f - 3.14159f;
                Line to = new Line(host.pos, host.pos.getAdd(Global.cos(rot) * range, Global.sin(rot) * range));
                if (host.map.checkLand(new Line(to.ptd, to.ptd.getAdd(0, 400)), 0) != null) {
                    host.pos.set(to.ptd);
                    break;
                }
            }
        }
    },
    Shadowstep(4,1,16,2,new int[]{FIST},new Tags(){{
        add(Tag.Void);
        add(Tag.Electric);
    }}){
        public String getClipName(){
            return "Conjure";
        }
        public void onSet(Unit host){
            Sound.play(host.map, host.pos, "shadow");
            Particle.smoke(host.map, host.pos,host.getDepth());
            int power = 300 + host.stateLevel * 100;
            P target = host.target.getAdd(host.pos);
            target.x = Global.clamp(target.x, host.pos.x-power,host.pos.x+power);
            target.y = Global.clamp(target.y, host.pos.y-power,host.pos.y+power);
            Line pp = new Line(target, target.getAdd(0,1200));
            for (Line l : host.map.land){
                if (l.lineCross(pp)){
                    Particle.smoke(host.map, host.pos,host.getDepth());
                    Particle.smoke(host.map, target,host.getDepth());
                    if (host.map.checkLand(new Line(target,target.getAdd(0,800)), 0) != null) {
                        P origin = host.getHand();
                        int num = Global.r.nextInt(12) + 6;
                        float div = host.target.getDist() / num;
                        float atan = host.target.getAdd(host.pos.getSubtract(origin)).getAtan();
                        P[] points = new P[num * 2 - 2];
                        for (int ii=0;ii<2;ii++) {
                            Terrain lightning = new Terrain(host.map, "_20,20,20,180", Shader.None, host.getDepth() + 1) {
                                private int count = 2;
                                public void draw(Graphics2D g) {
                                    super.draw(g);
                                    if (count-- == 0) remove();
                                }
                            };
                            for (int i = 0; i < points.length; i++) {
                                if (i < num) {
                                    float x = Global.cos(atan) * div * i, y = Global.sin(atan) * div * i;
                                    if (i != 0 && i != num - 1) {
                                        float var = (float) (Math.random() * 128 - 64);
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
                            lightning.start();
                        }

                        Sound.play(host.map, host.pos, "shadowstep");
                        host.pos.set(target);
                        host.setState(Actions.Idle);
                        return;
                    }
                    return;
                }
            }
        }
    },
    Fireblast(60,0,24,1,new int[]{FIST, OFIST},new Tags(){{
        add(Tag.Channeling);
        add(Tag.Fire);
        add(Tag.Air);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public boolean onFinish(Unit host){
            return false;
        }
        public void update(final Unit host){
            if (host.delay.time > 0) return;
            Sound.play(host.map, host.pos, "fire");
            final int level = host.stateLevel;
            for (int i=0;i<3;i++) {
                final Projectile p = new Projectile(host.map, "Flame", host.getHand(), host.target.getAdd(host.pos)
                        .getAdd(Global.r.nextInt(50)-25, Global.r.nextInt(50)-25), Global.r.nextInt(10+level)+6+level) {
                    public void stuck() {
                        Particle.explode(host.map, "Flame", host.getDepth(), pos, new P(), 10, 10, 0, false, 4);
                        remove();
                    }
                    public void onAttack(Unit p) {
                        p.buff(Buffs.Burn, level);
                    }
                    public float getDepth(){
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
            if (host.stateDuration % (8+host.stateLevel*2) == 0 && !host.get("Strength").require(1))
                host.setState(Actions.Right);
        }
    },
    Freeze(40,0,0,1,new int[]{FIST},new Tags(){{
        add(Tag.Channeling);
        add(Tag.Air);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public boolean onFinish(Unit host){
            return false;
        }
        public void update(final Man host){
            if (host.delay.time > 0) return;
            if (host.stateDuration % 40 != 0 || host.requireMana(1)) {
                Sound.play(host.map, host.pos, "ice");
                final int speed = 8 + host.stateLevel * 8;
                for (int i = 0; i < 3; i++) {
                    final Projectile p = new Projectile(host.map, "Ice", host.getHand(), host.target.getAdd(host.pos).getAdd(Global.r.nextInt(150) - 75, Global.r.nextInt(150) - 75), speed) {
                        private int tick = 200;
                        public void onAttack(Unit p) {
                            if (Global.r.nextInt(Math.max(1, 6 - host.stateLevel)) == 0) p.buff(Buffs.Freeze, 1);
                            super.onAttack(p);
                        }
                        public void update() {
                            super.update();
                            vel.mult(.9f, .9f);
                            tick--;
                            if (tick == 0) remove();
                        }
                        public float getDepth() {
                            return host.getDepth();
                        }
                    };
                    if (i != 0) p.tangible = false;
                    p.faction = host.faction;
                    p.gravity = false;
                    p.delay.set(15);
                    p.delay.performer = new Performer() {
                        public void perform() {
                            p.stuck();
                        }
                    };
                }
            } else host.stateSet(Idle);
        }
    },
    Meteorform(40,1,14,1,new Tags(){{
        add(Tag.Fire);
        add(Tag.Mutation);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void trigger(final Unit host){
            final int level = host.stateLevel;
            final Projectile p = new Projectile(host.map, "Fireball", host.pos, host.target.getAdd(host.pos), 80+(level*20)){
                @Override
                public void stuck(){
                    attackHitSound();
                    Particle.explode(host.map, "Fire", host.getDepth(), pos, new P(), 85+(level*10), 10+(level*2), 0, false, 4);
                    Particle.smoke(host.map, pos, host.getDepth());
                    ArrayList<Unit> t = host.map.updater.collideUnit(pos,128,Faction.Destructive);
                    for (Unit i : t){
                        if (i != host) i.hit(i.getPosLine(), getDamage());
                    }
                    remove();
                    host.setVisible(true);
                }
                @Override
                public void update(){
                    super.update();
                    Particle.explode(host.map, "Fire", host.getDepth(), pos, new P(), 15, 10, 0, true, 4);
                }
                void attackHitSound(){
                    Sound.play(host.map, host.pos, "explosion_small");
                }
            };
            host.setVisible(false);
            p.gravity = true;
            p.image.setSize(p.image.getSize().getMult(3,3));
            p.faction = host.faction;
            p.damage = 12;
            p.setDepth(host.getDepth());
            p.delay.set(50);
            p.delay.performer = new Performer(){
                public void perform(){
                    p.stuck();
                }
            };
        }
    },
    Fireball(48,3,16,2,new int[]{FIST},new Tags(){{
        add(Tag.Fire);
        add(Tag.Conjuration);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void trigger(final Unit host){
            final int level = host.stateLevel;
            final Projectile p = new Projectile(host.map, "Fireball", host.getHand(), host.target.getAdd(host.pos), 36+(level*8)){
                @Override
                public void stuck(){
                    attackHitSound();
                    Particle.explode(host.map, "Fire", getDepth(), pos, new P(), 36, 16+(level*2), 0, false, 4+level);
                    Particle.smoke(host.map, pos, getDepth());
                    ArrayList<Unit> t = host.map.updater.collideUnit(pos,128+level*8,Faction.Destructive);
                    for (Unit i : t){
                        i.hit(i.getPosLine(), getDamage());
                    }
                    remove();
                }
                @Override
                public void update(){
                    super.update();
                    Particle.explode(host.map, "Fire", getDepth(), pos, new P(), 12, 10, 0, true, 4);
                }
                void attackHitSound(){
                    Sound.play(host.map, host.pos, "explosion_small");
                }
                public float getDepth(){
                    return host.getDepth();
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
    Blizzard(30,2,26,2,new int[]{FIST, OFIST},new Tags(){{
        add(Tag.Channeling);
        add(Tag.Air);
    }}){
        public String getClipName(){
            return "Buff";
        }
        public void update(final Unit host){
            final int level = host.stateLevel;
            if (Global.r.nextInt(40) > (level*8)-4) return;
            if (Math.random() < .2f) Sound.play(host.map, host.pos, "ice");
            final Projectile p = new Projectile(host.map, "Ice", host.pos.getAdd(Global.nextGaussian(500),-500), host.pos.getAdd(Global.nextGaussian(500),0), 8+Global.r.nextInt(6*level)){
                @Override
                public void stuck(){
                    Particle.explode(host.map, "Ice",getDepth(),pos,new P(), 30, 20, 0, false, 1);
                    ArrayList<Unit> t = host.map.updater.collideUnit(pos,96,host.faction);
                    for (Unit i : t){
                        i.hit(host.getPosLine(), getDamage());
                        i.buff(Buffs.Freeze, 16+Global.r.nextInt(16));
                    }
                    remove();
                }
                @Override
                public void update(){
                    super.update();
                    Particle.explode(host.map, "Ice",getDepth(),pos, new P(), 3, 10, 0, false, 1);
                }
            };
            p.faction = host.faction;
            p.damage = level;
            p.gravity = false;
            p.delay.set(150);
            p.delay.performer = new Performer(){
                @Override
                public void perform(){
                    p.stuck();
                }
            };
            if (host.stateDuration % (7+host.stateLevel) == 0 && !host.get("Strength").require(1))
                host.setState(Actions.Idle);
        }
        public boolean onFinish(Unit host){
            return false;
        }
    },
    Meditate(0,0,0,2,new Tags(){{
        add(Tag.Energy);
    }}){
        public String getClipName(){
            return "Rest";
        }
        public void onSet(Unit host){
            if (!host.get("Strength").isFull()) host.stateSet(Idle);
        }
        public void update(Man host) {
            Stat str = host.get("Strength");
            int maxStr = str.getMax();
            if (host.stateDuration > 120 && str.require(maxStr)) host.requireMana(-maxStr*100);
        }
    },
    Generate(0,0,0,2,new Tags(){{
        add(Tag.Energy);
    }}){
        public void onSet(Unit host){
            host.stats.restTimer.reset();
            if (host.get("Strength").isFull()) host.stateSet(Idle);
//            else host.get("Strength").add(-1, false);
        }
        public void update(Unit host) {
            host.stats.rest(4+host.stateLevel);
        }
    };
    private int del, cooldown, cost;
    private Tags tags, trainers;
    private int difficulty;
    private int[] organs = null;
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
        this.difficulty = difficulty;
        this.tags = tags;
        this.trainers = tags.getTrainers();
    }
    Actions(int del, int cost, int cooldown, int difficulty, int[] organs, Tags tags){
        this(del, cost, cooldown, difficulty, tags);
        this.organs = organs;
    }
    public String getDescription(){
        String r = Dialog.get("?"+name()) +
                "\n[Trainers: " + trainers.toString()+" at Difficulty "+getDifficulty()+"]" +
                "\n[Cost:"+cost+" "+"]";
        if (Global.player.getStateLevel(this) == 0)
            r += "\nYou lack the catalysts required for this skill.";
        return r;
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

    public int getRange(Unit p) {
        return 48;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int set(Unit p){
        int r = canSet(p);
        if (r == 0) p.stateSet(this);
        return r;
    }
    protected int canSet(Unit p){
        if (p.delay.over() && p.grounded && p.state != this) {
            if (organs == null || p.clip.hasOrgans(organs)) {
                if (p.getCooldown(this).isReady()) {
                    if ((p.stateLevel = p.getStateLevel(this)) > 0) {
                        if (!p.get("Strength").require(getCost())) return 2;
                    } else return 5;
                } else return 3;
            } else return 6;
        } else return 1;
        return 0;
    }

    public void update(Unit p){}
    public void onSet(Unit p){}
    public void trigger(Unit p){}
    public void onInterrupt(Unit p){}
    public boolean onFinish(Unit p){
        return true;
    }
    public void update(Man p){
        update((Unit) p);
    }
    public void onSet(Man p){
        p.weapon.rotateTo(p.target.getMult(p.dir, p.dir));
        onSet((Unit) p);
    }
    public void trigger(Man p){
        trigger((Unit) p);
    }
    public void onInterrupt(Man p){
        onInterrupt((Unit) p);
    }
    public boolean onFinish(Man p){
        return onFinish((Unit) p);
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
    public String getName(){
        return name();
    }
    public String getClipName(){
        return name();
    }
    public String toString(){
        return getName();
    }
}