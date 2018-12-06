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
        public boolean isDynamicAnimation(){ return false; }
        public int canSet(Unit p){
            if (p.delay.over() && p.grounded && p.state != this) {
                return 0;
            } else return 1;
        }
        public void onSet(Unit p){
            p.clip.resetHeight();
        }
        public void onSet(Man host){
            if (!host.stats.strength.isFull() || !host.stats.intelligence.isFull()) host.clip.setState("Exhausted");
            onSet((Unit) host);
        }
        public void update(Unit p){
            if (Math.abs(p.vel.x) < 1f) p.vel.x = 0;
        }
        public void specialOrder(Unit host, float dist){
            host.delay.time = 40;
        }
    }, Left {
        public boolean isDynamicAnimation(){ return false; }
        public int canSet(Unit p){
            if (p.delay.over() && p.grounded && p.state != this) {
                return 0;
            } else return 1;
        }
        public void onSet(Unit p){
            p.face(-1, false);
        }
        public void onSet(Man host){
            float speed = host.getSpeed();
            if (speed <= 0) host.clip.setState("Exhausted");
            else host.clip.setMoveSpeed((int) Global.clamp(speed-1,-20,4));
            host.weapon.rotation = host.dir/2f;
            onSet((Unit) host);
        }
        public void update(Unit p){
            p.vel.x += p.getSpeed()*p.dir;
        }
        public void specialOrder(Unit host, float dist){
            host.delay.time = 40;
        }
    }, Right {
        public boolean isDynamicAnimation(){ return false; }
        public int canSet(Unit p){
            if (p.delay.over() && p.grounded && p.state != this) {
                return 0;
            } else return 1;
        }
        public void onSet(Unit p){
            p.face(1, false);
        }
        public void onSet(Man host){
            float speed = host.getSpeed();
            if (speed <= 0) host.clip.setState("Exhausted");
            else host.clip.setMoveSpeed((int) Global.clamp(speed-1,-20,4));
            host.weapon.rotation = host.dir/2f;
            onSet((Unit) host);
        }
        public void update(Unit p){
            p.vel.x += p.getSpeed()*p.dir;
        }
    }, Drop(12){
        public void onSet(Unit host){
            Line checkedLand = host.map.checkLand(new Line(host.pos.getAdd(0,4), host.pos.getAdd(0,900)), (e -> e.depth >= host.lastLand.depth));
            if (host.lastLand.droppable() && checkedLand != null){
                host.setDepth(host.lastLand.depth+.001f);
                host.pos.y+=4;
            }
        }
    }, Hit(14){
        public boolean isDynamicAnimation(){ return false; }
        public int canSet(Unit p){
            if (p.state != this && p.state != Dead) {
                return 0;
            } else return 1;
        }
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
    }, Dead(-1){
        public boolean isDynamicAnimation(){ return false; }
        public int canSet(Unit p){
            if (p.state != this) {
                return 0;
            } else return 1;
        }
        public void onSet(final Unit p){
            p.clip.organs[p.clip.BODY].pt.y = 8;
            p.status.clearBuffs();
        }
        public void onInterrupt(Unit p){
            onFinish(p);
        }
        public boolean onFinish(Unit p){
            p.clip.resetHeight();
            return super.onFinish(p);
        }
    }, Down(100){
        public boolean isDynamicAnimation(){ return false; }
        public int canSet(Unit p){
            if (p.state != this && p.state != Dead) {
                return 0;
            } else return 1;
        }
        public void onSet(final Unit p){
            p.clip.organs[p.clip.BODY].pt.y = 0;
        }
        public void onInterrupt(Unit p){
            onFinish(p);
        }
        public boolean onFinish(Unit p){
            p.clip.resetHeight(); //clip animation too short
            return super.onFinish(p);
        }
    }, Wield(30){
        public void onSet(Man host){
            if (Global.player.input.selectingItem == -1) {
                host.delay.time = Math.max(1, host.delay.time - host.weapon.getSpeed()*2);
                Global.player.sheathe();
            } else if (Global.player.input.selectingItem-10 < Global.player.inventory.getItemSize()) {
                Item item = Global.player.inventory.getItem(Global.player.input.selectingItem-10);
                if (item != null) host.delay.time = Math.max(1, host.delay.time - item.getSpeed()*2);
            }
        }
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
            if (!host.armor.isBare(4)){
                host.armor.armor[4].trigger(host);
            }
        }
    }, Use{
        public int canSet(Unit host){
            Man man = (Man) host;
            Actions action = man.weapon.type.action;
            if (host.delay.over() && host.grounded && host.state != action) {
                if (action.organs == null || host.clip.hasOrgans(action.organs)) {
                    if (host.getCooldown(action).isReady()) {
                        if (host.stats.strength.get() >= action.getCost()) return 0;
                        else return 2;
                    } else return 3;
                } else return 6;
            } else return 1;
        }
        public void onSet(Man host){
            host.stateSet(Idle);
            host.weapon.use(host);
        }
    }, Consume(20), Read(20){
        public String getClipName(){
            return "Mark";
        }
    }, Interact(16){
        public void onSet(final Man host){
            host.delay.performer = new Performer(()-> {
                P reach = host.getHand();
                L<Prop> result = new L<>();
                for (Prop i : host.map.updater.props){
                    if (i.onInteract != null) {
                        if (i.getInteractPoint().getSubtract(reach).getDist() <= 24) result.add(i);
                    }
                }
                for (Unit i : host.map.updater.unitsLoaded){
                    if (i.onInteract != null) {
                        if (i.getInteractPoint().getSubtract(reach).getDist() <= 24) result.add(i);
                    }
                }
                Prop obj = result.getFirst();
                if (obj != null) {
                    if (obj.onInteract != null) {
                        obj.onInteract.interact(host);
                        host.stateSet(Idle);
                        host.delay.time = 20;
                    }
                }
            });
        }
    }, Offhand(16) {
        public void onSet(final Man host){
            host.delay.performer = new Performer(()-> {
                P reach = host.pos.getAdd(host.clip.organs[BodyHumanoid.OFIST].ptd);
                L<Prop> result = new L<>();
                for (Prop i : host.map.updater.props){
                    if (i.onInteract != null) {
                        if (i.getInteractPoint().getSubtract(reach).getDist() <= 24) result.add(i);
                    }
                }
                for (Unit i : host.map.updater.unitsLoaded){
                    if (i.onInteract != null) {
                        if (i.getInteractPoint().getSubtract(reach).getDist() <= 24) result.add(i);
                    }
                }
                Prop obj = result.getFirst();
                if (obj != null) {
                    if (obj.onInteract != null) {
                        obj.onInteract.interact(host);
                        host.stateSet(Idle);
                        host.delay.time = 20;
                    }
                }
            });
        }
    }, Sprint(15,1,5,0,0,new int[]{RLEG, LLEG},new Tags(){{
    }}){
        public boolean isDynamicAnimation(){ return false; }
        public void onSet(Man host){
            float speed = host.getSpeed();
            if (speed <= 0) host.clip.setState("Exhausted");
            else host.clip.setMoveSpeed((int) Global.clamp(speed-1,-20,4));
            onSet((Unit) host);
        }
        public void update(Unit host){
            float speed = host.getSpeed();
            host.vel.x += speed * host.dir * (2.5f);
            if (checkEnemy(host) && host.stateDuration >= 60 && host.stateDuration % 60 == 0 && !host.stats.strength.require(1)) host.setState(Actions.Idle);
        }
        private boolean checkEnemy(Unit host){
            if (host == Global.player){
                for (Unit i : host.map.updater.unitsLoaded) {
                    if (i.map == Global.player.map && i.hittable(Global.player.faction) && i.brain.target == Global.player) {
                        return true;
                    }
                }
            }
            return false;
        }
        public boolean onFinish(Unit host){
            return false;
        }
    },
    Rest(70,0,0,0,0,new Tags(){{
    }}){
        public int getRange(Unit p){
            return 99999;
        }
        public void onSet(Unit host){
            if (host.stats.strength.isFull()) host.delay.time = 0;
        }
        public void update(Unit host) {
            int term = host.delay.max / host.stats.strength.getMax();
            if (host.stateDuration % term == term-12){
                if (!host.stats.strength.isFull()) {
                    host.stats.strength.add(1, false);
                }
            }
        }
    },
    Melee(24,1,0,0,0,new Tags(){{
    }}){
        public void trigger(Unit host){
            Sound.play(host.map, host.pos, "swing_normal");
            host.attack();
        }
        public void trigger(Man host){
            host.attackSound();
            int damage = Global.r.nextInt(3);
            for (Unit i : host.map.updater.collideUnit(host.getHand(), host.getSize()+32, host.faction)) {
                if (i.hit(i.getPosLine(), damage)) {
                    Sound.play(host.map, host.pos, "hit_punch");
                    new Thing(host.map, "hit","hit_1", -1, 150) {
                        private int n = 0;
                        public void draw(Graphics2D g) {
                            if (n <= 7) {
                                super.draw(g);
                                setImage("hit_" + (n++) / 2);
                                if (n > 7) remove();
                            }
                        }
                    }.start().pos.set(host.getHand());
                    break;
                }
            }
        }
    },
    Throw(26,0,0,0,0,new int[]{FIST},new Tags(){{
        add(Tag.Strength);
    }}){
        public String getClipName(){
            return "Thrust";
        }
        public void trigger(final Man host){
            if (!host.wieldingNone()) {
                int speed = 5;
                int damage = host.weapon.weight;
                if (damage > 0) speed = Math.min(64, 4 * Math.max(1, host.stateLevel - host.weapon.weight));
                boolean tangible = true;
                if (!host.stats.strength.require(1)){
                    damage = 0;
                    speed = 5;
                    tangible = false;
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
                    }
                    public void update(){
                        super.update();
                        wpn.rotation = rotation;
                    }
                };
                pj.setDepth(host.getDepth());
                pj.tangible = tangible;
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
    Jump(47,1,0,0,0,new int[]{LLEG, RLEG},new Tags(){{
    }}){
        public void onSet(Unit host){
            float x = Math.abs(host.vel.x);
            float t = Math.max(0, 24 - x) + 6;
            Sound.play(host.map, host.pos, "jump");
            host.vel.y -= t;
            float speed = host.getSpeed();
            float jumpDir;
            if (host.vel.x > 0) jumpDir = 1;
            else if (host.vel.x < 0) jumpDir = -1;
            else jumpDir = host.dir * .1f;
            host.vel.x = Math.min(speed*100, Math.max(Math.abs(host.vel.x)*5, speed*40))*jumpDir;
        }
    },
    Backstep(0,-1,0,0,0,new Tags(){{
    }}){
        public int getRange(Unit host){
            return 60;
        }
        public void update(Unit host){
            host.vel.x += host.getSpeed() * -host.dir * 1.2f;
        }
    },
    Thrust(26,1,0,0,0,new int[]{FIST}, new Tags(){{
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
    Slash(35,1,0,0,0,new int[]{FIST},new Tags(){{
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
                float rot = -((item.dir * 10) + del * del * .03f * -item.dir);
                if (host.delay.time > 12) item.rotation = rot;
                if (Math.abs(rot) < 8) {
                    if (host.attack() != null && !host.status.triggerEffect(Buffs.Might)) {
                        host.damageMod = 0;
                        host.stateSet(Idle);
                    }
                }
            }
        }
    },
    Hide(0,0,0,0,0,new Tags(){{
        add(Tag.Strength);
    }}){
        public String getClipName(){
            return "Conjure";
        }
        public int getRange(Unit p){
            return 2000;
        }
        public void trigger(Unit host){
            host.setVisible(false);
            Particle.smoke(host.map, host.pos, host.getDepth());
        }
        public void onInterrupt(Unit host){
            host.setVisible(true);
        }
        public boolean onFinish(Unit host){
            return false;
        }
        public void specialOrder(Unit host, float dist){
            host.delay.time = 200;
        }
    },
    Grab(15,3,12,7,0,new int[]{FIST},new Tags(){{
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
                        Weapon holder = new Weapon(host.map, "Victim",null, 0, Item.Material.Organic, Item.ItemType.Bashing) {
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
                        holder.weight = victim.stats.strength.getMax()/2 + 1;
                        holder.save = false;
                        host.setWeapon(holder);
                        victim.mobile = false;
                        victim.setState(Down);
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
    Charge(24,2,14,4,0,new int[]{RLEG, LLEG},new Tags(){{
        add(Tag.Strength);
    }}){
        public int getRange(Unit host){
            return 280;
        }
        public void onSet(Man host){
            if (host.dir < 0) host.vel.x = Math.min(-4, host.vel.x);
            else host.vel.x = Math.max(4, host.vel.x);
        }
        public void update(Unit host){
            host.vel.x = Global.clamp(host.vel.x*1.1f, -12, 12);
            Unit target = host.map.updater.collideUnit(host.getHand(), 64, host.faction).getFirst();
            if (target != null){
                host.stats.strength.require(1);
                Sound.play(host.map, host.pos, "hit_bash");
                if (target.state != Down){
                    target.vel.x += host.vel.x*2;
                    target.vel.y -= 20;
                    target.hit(target.getPosLine(),host.stateLevel);
                    target.setState(Down);
                }
            }
        }
    },
    Hold(65,1,14,2,0,new int[]{FIST, OFIST},new Tags(){{
        add(Tag.Strength);
    }}){
        public int getRange(Unit host){
            return 400;
        }
        public boolean onFinish(Unit host){
            return false;
        }
        public void onSet(Man host){
            host.damageMod = .5f;
            host.weapon.rotation = host.dir*.16f;
        }
        public void update(Man host){
            if (host.stateDuration > 12) {
                Unit victim = host.attack();
                if (victim != null) {
                    victim.setState(Down);
                    victim.delay.time += 12*host.stateLevel;
                    victim.vel.x = -victim.vel.x * 2;
                    victim.vel.y -= 8;
                    host.stateSet(Idle);
                }
            }
        }
        public void specialOrder(Unit host, float dist){
            host.delay.time = 200;
        }
    },
    Uppercut(35,2,12,1,0,new int[]{FIST},new Tags(){{
        add(Tag.Strength);
    }}){
        public String getClipName(){
            return "Lunge";
        }
        public int canSet(Unit p){
            if (p.state != this && p.grounded && p.state != Dead) {
                if (organs == null || p.clip.hasOrgans(organs)) {
                    if (p.getCooldown(this).isReady()) {
                        if ((p.stateLevel = p.getStateLevel(this)) > 0) {
                            if (!p.stats.strength.require(getCost())) return 2;
                        } else return 5;
                    } else return 3;
                } else return 6;
            } else return 1;
            return 0;
        }
        public void onSet(Man host){
            Item weapon = host.weapon;
            weapon.rotation = weapon.dir * 2;
        }
        public void update(Man host){
            if (host.delay.time <= 25) {
                if (host.delay.time == 17) host.vel.x = 4 * host.dir;
                Item item = host.weapon;
                int del = Math.max(0, 25 - host.delay.time);
                if (host.delay.time == 13) host.attackSound();
                float rot = ((item.dir * 2) + del * del * .024f * -item.dir);
                if (host.delay.time > 12){
                    item.rotation = rot;
                    if (host.delay.time < 18) {
                        Unit victim = host.attack();
                        if (victim != null) {
                            host.damageMod = 0;
                            host.stats.strength.add(1, false);
                            victim.vel.y -= 10;
                            victim.vel.x = host.dir * 10;
                        }
                    }
                }
            }
        }
    },
    Kick(40,2,7,3,0,new int[]{RLEG},new Tags(){{
        add(Tag.Strength);
    }}){
        public int getRange(Unit host){
            return 180;
        }
        public void trigger(Man host){
            Sound.play(host.map, host.pos, "swing_normal");
            int power = (int) (Math.log(host.stateLevel)*2);
            host.vel.y += (8+power)*Global.sin(host.target.getAtan());
            host.vel.x += (6+power)*Global.cos(host.target.getAtan());
        }
        public void update(Man host){
            if (host.delay.time > 14 && Math.abs(host.vel.x)>2){
                Line hitbox = host.clip.organs[BodyHumanoid.RLEG].getAdd(host.pos.getAdd(host.vel));
                for (Unit i : host.map.updater.collideUnit(host.pos, (int) hitbox.getLength() + 64, host.faction)) {
                    if (i.hit(hitbox, 5)) {
                        Sound.play(host.map, host.pos, "hit_bash");
                        new Thing(host.map, "hit","hit_1", -1, 150) {
                            private int n = 0;
                            public void draw(Graphics2D g) {
                                if (n <= 7) {
                                    super.draw(g);
                                    setImage("hit_" + (n++) / 2);
                                    if (n > 7) remove();
                                }
                            }
                        }.start().pos = hitbox.ptd;
                        if (i.stats.strength.get() == 0) i.setState(Down);
                        else i.setState(Hit);
                        int force = 24;
                        i.vel = hitbox.getAtan(force);
                        host.vel.x = 0;
                        Particle.explode(host.map, "Fire", i.getDepth(), hitbox.ptd, new P(), 6, 12, 0, false, 4);
                        break;
                    }
                }
            }
        }
    },
    Punch(12,1,0,2,0,new int[]{FIST},new Tags(){{
        add(Tag.Strength);
    }}){
        public int getRange(Unit p){
            return 36;
        }
        public void trigger(Man host){
            Sound.play(host.map, host.pos, "swing_normal");
            Line hitbox = host.clip.organs[BodyHumanoid.FIST].getAdd(host.pos);
            int damage = Global.r.nextInt(2+host.stateLevel);
            for (Unit i : host.map.updater.collideUnit(hitbox.ptd, (int) hitbox.getLength()+64, host.faction)) {
                if (i.hit(hitbox, damage)) {
                    Sound.play(host.map, host.pos, "hit_punch");
                    new Thing(host.map, "hit","hit_1", -1, 150) {
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
    Whirlwind(46,5,27,7,0,new int[]{FIST, OFIST},new Tags(){{
        add(Tag.Strength);
    }}){
        public void onSet(final Unit p){
            p.damageMod = .5f;
            p.delay.performer = new Performer(()->{
                p.setState(Actions.Rest);
                p.delay.set(80);
            });
        }
        public void update(Man host) {
            if (host.delay.time < 40) {
                Faction tmp = host.faction;
                host.faction = Faction.Destructive;
                host.weapon.rotation = host.delay.time * -host.weapon.dir;
                host.attack();
                host.faction = tmp;
            }
        }
        public void trigger(Unit p){
            if (p.delay.time % 6 == 0) p.attackSound();
        }
    },
    Stab(40,2,12,5,0,new int[]{FIST},new Tags(){{
        add(Tag.Strength);
    }}){
        public void onSet(Unit p){
            p.damageMod = 5;
        }
        public void trigger(Unit p){
            p.attackSound();
            p.attack();
        }
    },
    Dash(35,2,12,2,0,new int[]{RLEG, LLEG},new Tags(){{
        add(Tag.Strength);
    }}){
        public int getRange(Unit p){
            return 160;
        }
        public void onSet(Unit p){
            p.damageMod = 1.5f;
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
    Vault(4,1,7,3,0,new int[]{RLEG, LLEG},new Tags(){{
        add(Tag.Strength);
    }}){
        public String getClipName(){
            return "Down";
        }
        public int getRange(Unit p){
            return 160;
        }
        public int canSet(Unit p){
            if (p.state != this && (Math.abs(p.vel.x) > 4 || Math.abs(p.vel.y) > 4) && p.state != Dead) {
                if (organs == null || p.clip.hasOrgans(organs)) {
                    if (p.getCooldown(this).isReady()) {
                        if ((p.stateLevel = p.getStateLevel(this)) > 0) {
                            if (!p.stats.strength.require(getCost())) return 2;
                        } else return 5;
                    } else return 3;
                } else return 6;
            } else return 1;
            return 0;
        }
        public void onSet(Unit host){
            int power = 32+host.stateLevel*4;
            host.vel.y += power*Global.sin(host.target.getAtan());
            host.vel.x += power*Global.cos(host.target.getAtan());
        }
    },
    Lunge(24,1,12,3,0,new int[]{LLEG, RLEG, FIST},new Tags(){{
        add(Tag.Strength);
    }}){
        public void onSet(Man host){
            super.onSet(host);
            host.damageMod = 0;
        }
        public void update(Unit p) {
            if (p.vel.x > 0 == p.dir > 0 && Math.abs(p.vel.x) >= 8){
                Unit tgt = p.attack();
                if (tgt != null){
                    p.vel.x = 0;
                    p.damageMod = 0;
                    tgt.buff(Buffs.Stun, 1);
                }
            }
        }
        public void trigger(Unit host){
            host.attackSound();
            host.vel.set((8+host.stateLevel*2)*host.dir,-4);
        }
    },
    Underhand(32,4,13,5,0,new int[]{LLEG, RLEG, FIST, OFIST},new Tags(){{
        add(Tag.Strength);
    }}){
        public void trigger(Man host){
            host.attackSound();
            host.vel.set(5*host.dir,-1);
            Unit tgt = host.attack();
            if (tgt != null){
                if (tgt instanceof Man) Throw.trigger((Man) tgt);
                Particle.explode(host.map, "Fire", tgt.getDepth(), host.weapon.getLine().ptd, new P(), 6, 12, 0, false, 4);
            }
        }
    },
    Identify(1,0,1,0,0,new Tags()){
        public String getClipName(){
            return "Mark";
        }
        public void onSet(Unit host){
            host.delay.set(80);
            if (host == Global.player) {
                ArrayList<Unit> t = host.map.updater.collideUnit(host.target.getAdd(host.pos), 64, Faction.Destructive);
                if (t.size() > 0){
                    Unit target = t.get(0);
                    Menu.menu.showDialog(target.name,target.getDescription());
                }
            }
        }
    },
    Dart(12,1,2,1,0,new int[]{FIST},new Tags(){{
        add(Tag.Strength);
    }}),
    Mark(80,0,1,2,1,new Tags(){{
        add(Tag.Strength);
    }}){
        public void trigger(Unit host){
            Unit t = host.map.updater.collideUnit(host.target.getAdd(host.pos),84,Faction.Destructive).getFirst();
            if (t != null) t.buff(Buffs.Mark, host.stateLevel);
        }
    },
    Warcry(60,3,20,6,0,new Tags(){{
        add(Tag.Strength);
    }}){
        public void onSet(Unit host){
            ArrayList<Unit> t = host.map.updater.collideUnit(host.pos,240+(120*host.stateLevel),Faction.Destructive);
            for (Unit i : t){
                if (i != host) i.buff(Buffs.Stun,3+host.stateLevel);
            }
        }
    },
    Evade(40,1,6,3,0,new Tags(){{
        add(Tag.Strength);
    }}){
        public String getClipName(){
            return "Conjure";
        }
        public void onSet(Unit p){
            p.buff(Buffs.Evade,1);
        }
    },
    Airstrike(27,1,11,3,0,new int[]{LLEG, RLEG, FIST},new Tags(){{
        add(Tag.Strength);
    }}){
        public boolean isDynamicAnimation(){ return false; }
        public int canSet(Unit p){
            if (p.state == Jump) {
                if (p.getCooldown(this).isReady()){
                    if ((p.stateLevel = p.getStateLevel(this)) > 0){
                        if (!p.stats.strength.require(getCost())) return 2;
                    } else return 5;
                } else return 3;
            } else return 1;
            return 0;
        }
        public void onSet(Unit p){
            p.damageMod = 2 + p.stateLevel * .2f;
            p.vel.y -= 20;
        }
        public void update(Man host){
            if (host.grounded){
                host.weapon.rotation = 1f*host.dir;
                host.attack();
                for (Unit i : host.map.updater.collideUnit(host.pos, 64, host.faction)) {
                    Sound.play(host.map, host.pos, "hit_bash");
                    new Thing(host.map, "hit","hit_1", -1, 150) {
                        private int n = 0;
                        public void draw(Graphics2D g) {
                            if (n <= 7) {
                                super.draw(g);
                                setImage("hit_" + (n++) / 2);
                                if (n > 7) remove();
                            }
                        }
                    }.start().pos = host.weapon.getLine().ptd;
                    i.setState(Down);
                    Particle.explode(host.map, "Fire", i.getDepth(), host.weapon.getLine().ptd, new P(), 6, 12, 0, false, 4);
                }
                host.stateSet(Idle);
            } else {
                host.weapon.rotation = host.delay.time * -host.weapon.dir;
                if (host.delay.time % 4 == 0) host.attackSound();
                Unit victim = host.attack();
                if (victim != null){
                    host.stateSet(Drop);
                    host.delay.time = 40;
                    host.vel.set(0, 0);
                    victim.setState(Hit);
                }
            }
        }
    },
    Focus(3,0,26,3,0,new Tags(){{
        add(Tag.Strength);
        add(Tag.Energy);
    }}){
        public String getClipName(){
            return "Conjure";
        }
        public int canSet(Unit p){
            if (p.state != this && p.grounded && p.state != Dead) {
                if (organs == null || p.clip.hasOrgans(organs)) {
                    if (p.getCooldown(this).isReady()) {
                        if ((p.stateLevel = p.getStateLevel(this)) > 0) {
                            if (!p.stats.strength.require(getCost())) return 2;
                        } else return 5;
                    } else return 3;
                } else return 6;
            } else return 1;
            return 0;
        }
        public void trigger(Unit host){
            Sound.play(host.map, host.pos, "heal");
            if (host.stats.agility.require(1)) host.stats.strength.heal();
        }
    },
    Leap(40,2,40,8,0,new int[]{LLEG, RLEG},new Tags(){{
        add(Tag.Strength);
    }}){
        public int getRange(Unit host){
            return 1600;
        }
        public void trigger(Unit host){
            Sound.play(host.map, host.pos, "jump");
            host.vel.y -= 80;
            host.vel.x = 80 * host.dir;
        }
    },
    Fracture(20,1,0,1,1,new int[]{FIST},new Tags(){{
        add(Tag.Conjuration);
    }}){
        public String getClipName(){
            return "Mark";
        }
        public void onSet(Man host){
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
        }
    },
    Glowshot(40,1,2,1,1,new Tags(){{
        add(Tag.Conjuration);
    }}){
        public boolean isDynamicAnimation(){
            return false;
        }
        public int getRange(Unit host){
            return 600;
        }
        public String getClipName(){
            return "Shoot_Arrow";
        }
        public void trigger(final Man host){
            P tip = host.weapon.getLine().ptd;
            final Projectile pj = new Projectile(host.map, "Arrow", tip, host.getHand().getAdd(host.target), 80) {
                public void onAttack(Unit p) {
                    Sound.play(host.map, host.pos, "hit_bloody");
                    super.onAttack(p);
                }
                public void stuck(){
                    Particle.holyExplosion(map, pos, getDepth());
                    remove();
                }
                public void update() {
                    super.update();
                    if (tangible || lastLand.depth == 0) rotateTo(vel);
                }
            };
            pj.image = new ShadedPicture(pj.rand, pj.image.image, Shader.Color);
            ((ShadedPicture) pj.image).shade(new Color(255,255,255,255).getRGB());
            pj.setDepth(300);
            pj.lastLand = host.lastLand;
            pj.rotSpeed = 0;
            pj.rotation = pj.vel.getAtan();
            pj.gravity = true;
            pj.faction = host.faction;
            pj.damage = 2+host.stateLevel*2;
            host.weapon.rotation += host.dir * .3f;
            host.setState(Actions.Idle);
        }
        public void update(Man host){
            Particle.sparkle(host.map, "Glare", host.getDepth(), host.getHand(), 4, 3);
        }
    },
    Ethersword(4,0,2,2,1,new Tags(){{
        add(Tag.Conjuration);
    }}){
        public String getClipName(){
            return "Buff";
        }
        public void trigger(final Man host){
            Item weapon = (Item) Stock.Ethersword.create(host.map, -1, -1).start();
            host.setWeapon(weapon);
            weapon.uses = weapon.durability = host.stateLevel;
        }
    },
    Calling(40,2,24,3,1,new Tags(){{
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
                Unit p = (Unit) Stock.Dog.create(host.map, -1,-1);
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
    Infuse(18,3,10,4,10,new int[]{FIST},new Tags(){{
        add(Tag.Conjuration);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public void update(Man host){
            Particle.frozen(host.map, host.pos.getAdd(host.clip.organs[FIST].ptd), host.getDepth()+.01f);
            Particle.frozen(host.map, host.pos.getAdd(host.clip.organs[BodyHumanoid.OFIST].ptd), host.getDepth()+.01f);
            if (host.stateDuration == 400){
                Sound.play(host.map, host.pos, "freeze");
                String target = host.getConversation();
                Prop t = Stock.createByNameFromReg(host.map, target, -1);
                if (t != null) {
                    t.start();
                    if (t instanceof Item) {
                        host.setWeapon((Item) t);
                        ((Item) t).setMaterial(Item.Material.Crystal);
                        if (t.tags.contains(Tag.Currency) || t.tags.contains(Tag.Energy)){
                            ((Item)t).uses = 0;
                        }
                    } else {
                        t.pos.set(host.pos);
                        int ctype = t.image.getCenterType();
                        t.image = new ShadedPicture(t.rand, t.image.image, Shader.Color);
                        ((ShadedPicture) t.image).shade(new Color(48, 156, 200, 150).getRGB());
                        t.image.setCenterType(ctype);
                    }
                } else host.rechargeMana(20);
                host.setState(Down);
            }
        }
        public boolean onFinish(Unit host){
            return false;
        }
    },
    Fireball(48,1,4,1,2,new int[]{FIST},new Tags(){{
        add(Tag.Fire);
        add(Tag.Conjuration);
    }}){
        public int getRange(Unit host){
            return 500;
        }
        public String getClipName(){
            return "Cast";
        }
        public void trigger(final Unit host){
            final int level = host.stateLevel;
            final Projectile p = new Projectile(host.map, "Bomb", host.getHand(), host.target.getAdd(host.pos), 36+(level*8)){
                @Override
                public void stuck(){
                    attackHitSound();
                    Particle.explode(host.map, "Flame", getDepth(), pos, new P(), 7, 36, 0, false, 7);
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
                    Particle.explode(host.map, "Flame", getDepth(), pos, new P(), 3, 16, 0, false, 6);
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
            p.damage = level*30;
            p.delay.set(2);
            p.delay.performer = new Performer(()-> p.faction = Faction.Destructive);
        }
    },
    Meteorform(40,3,17,1,2,new Tags(){{
        add(Tag.Fire);
        add(Tag.Enchant);
    }}){
        public int getRange(Unit host){
            return 500;
        }
        public String getClipName(){
            return "Cast";
        }
        public void trigger(final Unit host){
            final Projectile p = new Projectile(host.map, "Bomb", host.pos, host.target.getAdd(host.pos), 80 + (host.stateLevel * 16)) {
                @Override
                public void stuck() {
                    attackHitSound();
                    Particle.explode(host.map, "Fire", host.getDepth(), pos, new P(), 7, 16, 0, false, 4);
                    Particle.smoke(host.map, pos, host.getDepth());
                    ArrayList<Unit> t = host.map.updater.collideUnit(pos, 128, Faction.Destructive);
                    for (Unit i : t) {
                        if (i != host) i.hit(i.getPosLine(), getDamage());
                    }
                    remove();
                    host.setVisible(true);
                }

                @Override
                public void update() {
                    super.update();
                    Particle.explode(host.map, "Fire", host.getDepth(), pos, new P(), 4, 10, 0, true, 4);
                }

                void attackHitSound() {
                    Sound.play(host.map, host.pos, "explosion_small");
                }
            };
            host.setVisible(false);
            host.buff(Buffs.Firewalk, 2);
            p.gravity = true;
            p.image.setSize(p.image.getSize().getMult(3, 3));
            p.faction = host.faction;
            p.damage = host.stateLevel*30;
            p.setDepth(host.getDepth());
            p.delay.set(50);
            p.delay.performer = new Performer(p::stuck);
        }
    },
    Fireblast(60,1,7,1,1,new int[]{FIST, OFIST},new Tags(){{
        add(Tag.Channeling);
        add(Tag.Fire);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public boolean onFinish(Unit host){
            return false;
        }
        public void update(final Man host){
            ((BodyHumanoid) host.clip).setAppendages(getClipName());
            super.update(host);
        }
        public void update(final Unit host){
            Sound.play(host.map, host.pos, "fire");
            final int level = host.stateLevel;
            Particle.explode(host.map, "Flame", host.getDepth(), host.getHand(), host.target, 4, 12, 20, false, level*8);
            if (host.stateDuration % 12 == 0) {
                final Projectile p = new Projectile(host.map, "Flame", host.getHand(), host.target.getAdd(host.pos)
                        .getAdd(Global.r.nextInt(50) - 25, Global.r.nextInt(50) - 25), 30) {
                    public void stuck() {
                        remove();
                    }

                    public void onAttack(Unit p) {
                        p.buff(Buffs.Burn, level);
                    }

                    public float getDepth() {
                        return host.getDepth();
                    }
                };
                p.damage = level;
                p.gravity = false;
                p.delay.set(4 + level);
                p.delay.performer = new Performer(p::stuck);
                float atan = host.target.getAtan();
                host.vel.add(Global.cos(atan) * -.1f, Global.sin(atan) * -level);
                if (host.stateDuration % 84 == 0 && !host.requireMana(1)) host.stateSet(Idle);
            }
        }
    },
    Firehail(40,1,23,1,1,new Tags(){{
        add(Tag.Fire);
        add(Tag.Air);
        add(Tag.Channeling);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public boolean onFinish(Unit host){
            return false;
        }
        public void update(final Unit host){
            if (host.stateDuration % ((8/host.stateLevel)+1) == 0) {
                P origin = host.target.getAdd(host.getHand());
                origin.y = Math.min(origin.y, host.pos.y - 400);
                final Projectile p = new Projectile(host.map, "Flame", origin.disperse(80, 80), new P(), 0) {
                    public void onAttack(Unit p) {
                        p.buff(Buffs.Burn, 1);
                        Particle.explode(host.map, "Fire", host.getDepth(), pos, new P(0, -20), 6, 10, 5, false, 4);
                        remove();
                    }
                    public void stuck() {
                        Particle.explode(host.map, "Flame", host.getDepth(), pos, new P(0, -20), 3, 10, 5, false, 6);
                        ArrayList<Unit> t = host.map.updater.collideUnit(pos, 64, Faction.Destructive);
                        for (Unit i : t) i.hit(i.getPosLine(), getDamage());
                        remove();
                    }
                    public void update() {
                        Particle.explode(host.map, "Smoke", host.getDepth(), pos, new P(0, -20), 1, 10, 30, false, 4);
                        super.update();
                        vel.y *= .8f;
                    }
                    public float getDepth() {
                        return host.getDepth();
                    }
                };
                p.faction = Faction.Destructive;
                p.gravity = true;
                p.vel.x = SyncRand.getInt(40) - 20;
                p.vel.y = SyncRand.getInt(40) - 20;
                p.damage = 3;
            }
            if (host.stateDuration % 100 == 0 && !host.requireMana(1)) host.stateSet(Idle);
        }
    },
    Blink(4,0,4,1,0,new Tags(){{
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
                if (host.map.checkLand(new Line(to.ptd, to.ptd.getAdd(0, 400)), Line::landable) != null) {
                    host.pos.set(to.ptd);
                    break;
                }
            }
        }
    },
    Swap(12,1,4,1,0,new Tags(){{
        add(Tag.Void);
    }}){
        public String getClipName(){
            return "Conjure";
        }
        public void trigger(Unit host){
            Sound.play(host.map, host.pos, "shadow");
            Particle.smoke(host.map, host.pos,host.getDepth());
            Unit unit = host.map.updater.collideUnit(host.target.getAdd(host.pos),600/host.stateLevel,Faction.Destructive).getFirst();
            if (unit != null) {
                P diff = unit.pos.getSubtract(host.pos);
                unit.pos.add(host.pos.getSubtract(unit.pos));
                host.pos.add(diff);
            }
        }
    },
    Shadowtwin(240,1,30,2,1,new Tags(){{
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
            host.buff(Buffs.Bleed, 1);
        }
        public void trigger(final Unit host){
            if (host.stats.agility.require(1)) {
                Sound.play(host.map, host.pos, "shadowstep");
                Man p = new Man(host.map, "Shadowtwin", host.seed) {
                    @Override
                    public boolean die(int cause, String desc) {
                        if (super.die(cause, desc)){
                            remove();
                            Particle.explode(host.map, "Smoke", getDepth(), pos, new P(), 16, 10, 0, true, 80);
                            return true;
                        }
                        return false;
                    }
                };
                p.factoryId = Stock.Man.ordinal();
                p.subId = 0;
                p.load(new T(host.toString()));
                p.moveset.addAll(PlayerInput.basicMoves);
                p.brain = Intelligence.Hunter.make(p);
                p.brain.setTask(Behavior.Idle);
                p.buff(Buffs.Banishment, 1+host.stateLevel);
                p.dir = -host.dir;
                p.faction = host.faction;
                p.clip.setState("Buff");
                p.start();
            } else host.say(Dialog.get("!a"));
            host.setState(Down);
        }
    },
    Shadowstep(4,1,12,1,0,new int[]{FIST},new Tags(){{
        add(Tag.Void);
        add(Tag.Air);
    }}){
        public String getClipName(){
            return "Conjure";
        }
        public int getRange(Unit host){
            return 300 + host.stateLevel * 100;
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
                    if (host.map.checkLand(new Line(target,target.getAdd(0,800)), Line::landable) != null) {
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
    Disarm(4,1,24,3,4,new int[]{FIST},new Tags(){{
        add(Tag.Void);
    }}){
        public String getClipName(){
            return "Conjure";
        }
        public int getRange(Unit host){
            return 300 + host.stateLevel * 100;
        }
        public void trigger(Unit host){
            Sound.play(host.map, host.pos, "shadow");
            Particle.smoke(host.map, host.pos,host.getDepth());
            Unit unit = host.map.updater.collideUnit(host.target.getAdd(host.pos),600/host.stateLevel,Faction.Destructive).getFirst();
            if (unit instanceof Man){
                Man man = (Man) unit;
                for (int i=0;i<4;i++) {
                    if (!man.armor.isBare(i)) {
                        man.armor.armor[i].pos = man.pos.getAdd(0,-4);
                        man.armor.armor[i].start();
                    }
                }
                man.armor.remove();
            }
        }
    },
    Erase(4,1,24,4,5,new int[]{FIST},new Tags(){{
        add(Tag.Void);
    }}){
        public String getClipName(){
            return "Conjure";
        }
        public int getRange(Unit host){
            return 300 + host.stateLevel * 100;
        }
        public void trigger(Unit host){
            Sound.play(host.map, host.pos, "shadow");
            Particle.smoke(host.map, host.pos,host.getDepth());
            Unit unit = host.map.updater.collideUnit(host.target.getAdd(host.pos),600/host.stateLevel,Faction.Destructive).getFirst();
            if (unit != null){
                unit.buff(Buffs.Banishment, 1);
            } else {
                Prop prop = host.map.updater.collideProp(host.target.getAdd(host.pos), 600/host.stateLevel).getFirst();
                if (prop instanceof Item) prop.remove();
            }
        }
    },
    Shock(2,1,9,1,1,new int[]{FIST, OFIST},new Tags(){{
        add(Tag.Air);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public int getRange(Unit host){
            return 800;
        }
        public void trigger(final Unit host){
            Particle.explode(host.map, "Glare",host.getDepth(),host.getHand(),new P(0,-1),8,20,12,true,4);
            Sound.play(host.map, host.pos, "shadowstep");
            P origin = host.getHand();
            ArrayList<Unit> t = host.map.updater.collideUnit(host.target.getAdd(origin),600/host.stateLevel,Faction.Destructive);
            if (t.size() > 0) {
                Unit target = t.get(Global.r.nextInt(t.size()));
                P targetPos = target.getRandomPoint();
                host.target = targetPos.getSubtract(host.pos);
                int num = Global.r.nextInt(6) + 3;
                float div = host.target.getDist() / num;
                float atan = host.target.getAdd(host.pos.getSubtract(origin)).getAtan();
                P[] points = new P[num * 2 - 2];
                for (int ii = 0; ii < Global.r.nextInt(2) + 2; ii++) {
                    Terrain lightning = new Terrain(host.map, "/255/255/0/255", Shader.None, host.getDepth()){
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
                target.buff(Buffs.Stun, host.stateLevel);
            }
        }
    },
    Freeze(40,0,0,1,0,new int[]{FIST},new Tags(){{
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
            ((BodyHumanoid) host.clip).setAppendages(getClipName());
            Sound.play(host.map, host.pos, "ice");
            final int speed = 8 + host.stateLevel * 8;
            for (int i = 0; i < 3; i++) {
                final Projectile p = new Projectile(host.map, "Ice", host.getHand(), host.target.getAdd(host.pos).getAdd(Global.r.nextInt(150) - 75, Global.r.nextInt(150) - 75), speed) {
                    private int tick = 200;
                    public void onAttack(Unit p) {
                        p.buff(Buffs.Freeze, host.stateLevel);
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
                p.faction = Faction.Destructive;
                p.gravity = false;
                p.delay.set(15);
                p.delay.performer = new Performer(p::stuck);
            }
        }
    },
    Blizzard(30,0,4,1,1,new int[]{FIST, OFIST},new Tags(){{
        add(Tag.Channeling);
        add(Tag.Air);
    }}){
        public String getClipName(){
            return "Buff";
        }
        public void update(final Unit host){
            if (Math.random() < .2f) Sound.play(host.map, host.pos, "ice");
            if (host.stateDuration % (host.stateLevel+2) != 0) {
                final Projectile p = new Projectile(host.map, "Ice", host.pos.getAdd(Global.nextGaussian(500), -500), host.pos.getAdd(Global.nextGaussian(500), 0), 8 + SyncRand.getInt(6 * host.stateLevel)) {
                    @Override
                    public void stuck() {
                        Particle.explode(host.map, "Ice", getDepth(), pos, new P(), 30, 20, 0, false, 1);
                        ArrayList<Unit> t = host.map.updater.collideUnit(pos, 96, host.faction);
                        for (Unit i : t) {
                            i.hit(host.getPosLine(), getDamage());
                            i.buff(Buffs.Freeze, 16 + Global.r.nextInt(16));
                        }
                        remove();
                    }

                    @Override
                    public void update() {
                        super.update();
                        Particle.explode(host.map, "Ice", getDepth(), pos, new P(), 3, 10, 0, false, 1);
                    }
                };
                p.faction = host.faction;
                p.damage = 4;
                p.gravity = false;
                p.delay.set(150);
                p.delay.performer = new Performer(p::stuck);
            }
        }
        public boolean onFinish(Unit host){
            return false;
        }
    },
    Lightning(32,2,4,3,1,new int[]{FIST, OFIST},new Tags(){{
        add(Tag.Air);
        add(Tag.Channeling);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public int getRange(Unit host){
            return 600;
        }
        public boolean onFinish(Unit host){
            Particle.explode(host.map, "Glare",host.getDepth(),host.getHand(),new P(0,-1),20,20,12,true,4);
            return false;
        }
        public void trigger(Unit host){
            Particle.explode(host.map, "Glare",host.getDepth(),host.getHand(),new P(0,-1),20,20,12,true,4);
        }
        public void update(final Man host){
            if (host.delay.time > 0) return;
            Sound.play(host.map, host.pos, "shadowstep");
            P origin = host.getHand();
            ArrayList<Unit> t = host.map.updater.collideUnit(origin, 600, host.faction);
            int size = t.size();
            if (size > 0) {
                Unit target = t.get(Global.r.nextInt(size));
                P targetPos = target.getRandomPoint();
                host.target = targetPos.getSubtract(host.pos);
                int num = Global.r.nextInt(6) + 3;
                float div = host.target.getDist() / num;
                float atan = host.target.getAdd(host.pos.getSubtract(origin)).getAtan();
                P[] points = new P[num * 2 - 2];
                for (int ii = 0; ii < 2; ii++) {
                    Terrain lightning = new Terrain(host.map, "/255/255/0/255", Shader.None, host.getDepth()) {
                        int turn = 0;
                        public void draw(Graphics2D g) {
                            super.draw(g);
                            if (turn++ > 1) remove();
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
                if (host.stateDuration % 4 == 0) {
                    target.buff(Buffs.Shock, 1);
                    if (host.stateDuration % 80 == 0 && !target.stats.agility.require(1)) target.die(Unit.DEATH_ELECTROCUTION, "Electrocution");
                }
            }
            if (host.stateDuration % 160 == 0 && !host.requireMana(1)) host.stateSet(Idle);
        }
    },
    Enchant(40,1,9,1,0,new int[]{FIST},new Tags(){{
        add(Tag.Enchant);
    }}){
        public int getRange(Unit host){
            return 900;
        }
        public String getClipName(){
            return "Buff";
        }
        public void trigger(Man host){
            Sound.play(host.map, host.pos, "spell");
            int hex = host.status.getEffectLevel(Buffs.Hex);
            if (hex > 0){
                Buffs buff = Buffs.values()[hex];
                Unit t = host.map.updater.collideUnit(host.target.getAdd(host.pos), 64, Faction.Destructive).getFirst();
                if (t != null) t.buff(buff, Math.max(1, host.stateLevel - buff.value));
                host.status.triggerEffect(Buffs.Hex);
            } else if (host == Global.player) {
                ArrayList<Buffs> buffs = Buffs.getByLevel(host.stateLevel);
                Tags holding = new Tags();
                holding.addAll(host.tags);
                holding.addAll(host.weapon.tags);
                holding.addAll(host.armor.armor[4].tags);
                for (int i = 0; i < buffs.size(); i++) {
                    Tags tags = buffs.get(i).tags;
                    if (tags.isEmpty() || !holding.containsAll(tags)) buffs.remove(i--);
                }
                String[] buffNames = new String[buffs.size()];
                Performer[] performers = new Performer[buffs.size()];
                for (int i = 0; i < buffNames.length - 1; i++) {
                    Buffs buff = buffs.get(i);
                    buffNames[i] = "[Enchant Level " + buff.value + "] " + buff.name();
                    performers[i] = new Performer(()-> {
                        if (host.status.getEffectLevel(Buffs.Bleed) < 4) {
                            host.buff(Buffs.Bleed, 1);
                            host.buff(Buffs.Hex, buff.ordinal());
                        } else {
                            Particle.explode(host.map, "Blood", host.getDepth(), host.getHand(), P.blank, 5, 24, 0, true, 28);
                            host.setState(Down);
                        }
                    });
                }
                Global.player.delay.time = 100;
                Menu.menu.showDialog("Available Enchantments", "", buffNames, performers);
            } else host.buff(Buffs.Hex, Buffs.getByLevel(host.stateLevel).get(host.seed % Buffs.getByLevel(host.stateLevel).size()).ordinal());
        }
        public void update(Unit host){
            Particle.implode(host.map, "Blood", host.getDepth(), host.getHand(), 2, 10, false, 5);
        }
    },
    Cleanse(40,0,14,3,1,new Tags(){{
        add(Tag.Enchant);
    }}){
        public String getClipName(){
            return "Cast";
        }
        public int canSet(Unit p){
            if (p.delay.over() && p.grounded && p.state != this) {
                if (p.getCooldown(this).isReady()) {
                    if ((p.stateLevel = p.getStateLevel(this)) > 0) {
                        if (!(p instanceof Player) || p.stats.nutrition.require(1)){
                            if (p.requireMana(1)) {
                                return 0;
                            } else {
                                p.stats.nutrition.add(1, false);
                                return 8;
                            }
                        } else return 2;
                    } else return 5;
                } else return 3;
            } else return 1;
        }
        public void trigger(Man host){
            host.status.clearBuffs();
        }
    },
    Combust(24,0,1,1,0,new Tags(){{
        add(Tag.Energy);
        add(Tag.Fire);
    }}){
        public String getClipName(){
            return "Mark";
        }
        public void trigger(Man host) {
            if (host.weapon.getMaterial() == Item.Material.Wooden || host.weapon.getMaterial() == Item.Material.Organic){
                Particle.smoke(host.map, host.getHand(), host.getDepth());
                host.weapon.isUsable(true, 4);
                if (host.weapon.uses == 0 && !host.weapon.disposable) host.weapon.remove();
                host.rechargeMana(1);
            } else {
                host.buff(Buffs.Burn, 1);
                host.rechargeMana(host.stateLevel);
            }
        }
    },
    Immolate(240,0,0,1,1,new Tags(){{
        add(Tag.Energy);
        add(Tag.Curse);
    }}){
        public String getClipName(){
            return "Buff";
        }
        public void onSet(Man host){
            P dest = host.target.getAdd(host.pos);
            new Projectile(host.map, "Mark", dest, P.blank, 0) {
                private int tick = 200+SyncRand.getInt(400);
                public void update() {
                    super.update();
                    vel.mult(.9f, .9f);
                    tick--;
                    if (tick % 24 == 0){
                        Particle.explode(host.map, "Blood", host.getDepth(), pos, P.blank, 7, 40, 0, true, 32);
                    }
                    if (tick == 0){
                        Unit a = map.updater.collideUnit(pos, 84, Faction.Destructive).getFirst();
                        if (a != null){
                            a.buff(Buffs.Bleed, 2);
                            a.clip.organs[a.seed % a.clip.organs.length].amputate(a.lastLand,host.stateLevel*2);
                            host.rechargeMana(1+host.stateLevel);
                        }
                        remove();
                    }
                }
                public float getDepth() {
                    return host.getDepth();
                }
            };
        }
    },
    Sacrifice(60,3,7,1,0,new Tags(){{
        add(Tag.Energy);
        add(Tag.Curse);
    }}){
        public String getClipName(){
            return "Conjure";
        }
        public void update(Man host) {
            if (host.stateDuration > 200){
                host.buff(Buffs.Bleed, 1);
                host.rechargeMana(1);
            }
            host.setState(Idle);
        }
    },
    Equalize(4,0,12,1,0,new Tags(){{
        add(Tag.Energy);
        add(Tag.Void);
        add(Tag.Air);
    }}){
        public String getClipName(){
            return "Conjure";
        }
        public void trigger(Man host) {
            if (host == Global.player){
                new Projectile(host.map, "Smoke", host.pos.getAdd(0,0), P.blank, 0) {
                    private int tick = 160;
                    public void update() {
                        super.update();
                        vel.mult(.9f, .9f);
                        tick--;
                        if (tick % 4 == 0){
                            Particle.implode(host.map, "Smoke", host.getDepth(), pos.getAdd(0, -20), 6, 2, false, 36);
                        }
                        if (tick == 0){
                            P diff = pos.getSubtract(host.pos);
                            host.pos.add(diff);
                            int value = (int) (diff.getDist() / 240) - 2;
                            if (value > 0) host.rechargeMana(value);
                            else host.requireMana(-value);
                            Particle.explode(host.map, "Smoke", host.getDepth(), pos.getAdd(0, -20), pos.getAdd(0, -40), 8, 15, 25, true, 36);
                            remove();
                        }
                    }
                    public float getDepth() {
                        return host.getDepth();
                    }
                };
            }
        }
    },
    Assimilate(60,3,7,2,0,new Tags(){{
        add(Tag.Energy);
    }}){
        public String getClipName(){
            return "Conjure";
        }
        public boolean onFinish(Unit host){
            return false;
        }
        public void update(Man host){
            if (host.stateDuration % 4 == 0) {
                Particle.implode(host.map, "Dust", host.getDepth(), host.getHand(), 12, 2, false, 20);
                Particle.implode(host.map, "Smoke", host.getDepth(), host.getHand(), 12, 2, false, 60);
                P origin = host.getHand();
                ArrayList<Prop> t = host.map.updater.collideProp(origin, 400);
                t.removeIf(e -> !(e instanceof Item) || e.tags.contains(Tag.Energy));
                int size = t.size();
                if (size > 0) {
                    Sound.play(host.map, host.pos, "drink");
                    Item target = (Item) t.get(Global.r.nextInt(size));
                    host.target = target.pos.getSubtract(host.pos);
                    int num = Global.r.nextInt(6) + 3;
                    float div = host.target.getDist() / num;
                    float atan = host.target.getAdd(host.pos.getSubtract(origin)).getAtan();
                    P[] points = new P[num * 2 - 2];
                    Terrain lightning = new Terrain(host.map, "_40,80,200,255", Shader.None, host.getDepth()) {
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
                    if (host.stateDuration % 12 == 0)
                        Particle.explode(host.map, "Ice", target.getDepth(), target.pos, host.pos.getSubtract(target.pos), 8, 2, 8, false, 20);
                    target.isUsable(true, Math.min(target.uses, 4*host.stateLevel));
                    if (target.uses == 0){
                        host.rechargeMana(4*host.stateLevel);
                        target.remove();
                    }
                } else host.stateSet(Idle);
            }
        }
    },
    Meditate(200,0,0,3,0,new Tags(){{
        add(Tag.Energy);
    }}){
        public String getClipName(){
            return "Rest";
        }
        public void trigger(Man host) {
            Stat str = host.stats.strength;
            int usedStr = str.get();
            if (usedStr > 0 && str.require(usedStr)){
                host.rechargeMana(usedStr);
            }
        }
    };
    private int del, cooldown, cost, energy;
    private Tags tags, trainers;
    private int difficulty;
    protected int[] organs = null;
    Actions(){
        this(0);
    }
    Actions(int del){
        this(del,-1,0,0,0,new Tags());
    }
    Actions(int del, int cost, int cooldown, int difficulty, int energy, Tags tags){
        this.del = del;
        this.cost = cost;
        this.cooldown = cooldown;
        this.difficulty = difficulty;
        this.energy = energy;
        this.tags = tags;
        this.trainers = tags.getTrainers();
    }
    Actions(int del, int cost, int cooldown, int difficulty, int energy, int[] organs, Tags tags){
        this(del, cost, cooldown, difficulty, energy, tags);
        this.organs = organs;
    }
    public String getDescription(){
        String r = Dialog.get("?"+name()) +
                "\n[Trainers: " + trainers.toString()+" at Difficulty "+getDifficulty()+"]" +
                "\n[Cooldown:"+cooldown+" "+"]" +
                "\n[Stamina Cost:"+cost+" "+"]" +
                "\n[Energy Cost:"+energy+" "+"]";
        int status = canSet(Global.player);
        int cd = Global.player.getCooldown(this).time - Global.time;
        int stamina = cost - Global.player.stats.strength.get();
        int mana = energy - Global.player.getMana();
        if (status == 5) r += "\nYou lack the catalysts required for this skill.";
        if (status == 3) r += "\nYou need to wait "+(cd+"")+" seconds to use this skill.";
        if (status == 2) r += "\nYou need "+(stamina+"")+" more Stamina to use this skill.";
        if (status == 8) r += "\nYou need "+(mana+"")+" more Energy to use this skill.";
        if (status == 6) r += "\nYou lack body parts needed to use this skill.";
        return r;
    }

    public int getCost() {
        return cost;
    }

    public int getEnergy() {
        return energy;
    }

    public int getDel() {
        return del;
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getRange(Unit p) {
        return (int) p.getReach();
    }

    public int getDifficulty() {
        return difficulty;
    }

    public boolean isDynamicAnimation(){
        return true;
    }

    public int set(Unit p){
        int r = canSet(p);
        if (r == 0){
            p.stateSet(this);
        }
        return r;
    }
    public int canSet(Unit p){
        if (p.delay.over() && p.grounded && p.state != this) {
            if (organs == null || p.clip.hasOrgans(organs)) {
                if (p.getCooldown(this).isReady()) {
                    if ((p.stateLevel = p.getStateLevel(this)) > 0) {
                        if (p.stats.strength.require(getCost())){
                            if (energy == 0 || p.requireMana(energy)) {
                                return 0;
                            } else {
                                p.stats.strength.add(getCost(), false);
                                return 8;
                            }
                        } else return 2;
                    } else return 5;
                } else return 3;
            } else return 6;
        } else return 1;
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
    public void specialOrder(Unit host, float dist){}

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
    public static ArrayList<Actions> getScribables(){
        ArrayList<Actions> r = new ArrayList<>();
        for (Actions i : values()){
            if (i.getTrainers().size() > 0) r.add(i);
        }
        r.removeIf(e -> e.getTags().contains(Tag.Strength));
        return r;
    }
    public static Actions getByName(String p) {
        for (Actions i : values()) {
            if (i.name().equals(p)){
                return i;
            }
        }
        return null;
    }
    public String getClipName(){
        return name();
    }
    public static void load(Unit host, T p){
        for (T i : p){
            Actions action = getByName(i.get(0).get());
            if (action != null){
                host.learnSkill(action);
                Cooldown cd = new Cooldown(action.getCooldown());
                host.cooldown.put(action, cd);
                cd.time = i.get(1).getInt();
            }
        }
    }
    public String toString(){
        return name();
    }
}