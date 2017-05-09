package com.ga.unit.skill;

import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.*;
import com.ga.unit.item.Projectile;

/**
 * Created by Ga on 9/1/2016. asd
 */
public enum Buffs {
    Banishment(new Tags(){{
        add(Tag.Debuff);
        add(Tag.Shadow);
    }}){ Buff makeLocal() {
        return new Buff(8) {
            public void onSet() { //higher level = longer till banish
                time = level * 8;
            }

            public void onFinish() {
                host.die();
            }

            public boolean update() {
                Particle.smoke(host.getRandomPoint());
                return super.update();
            }
        };
    }},
    Haste(new Tags(){{
        add(Tag.Buff);
        add(Tag.Air);
    }}){ Buff makeLocal() {
        return new Buff(15) {
            public void onSet() {
                var = Math.round(host.get("Speed").getMax());
                host.get("Speed").add(var, true);
                time += level * 15;
            }

            public void onFinish() {
                host.get("Speed").add(-var, true);
            }

            public boolean update() {
                if (Math.random() < .2f) {
                    Particle.cleanse(host.pos);
                }
                return super.update();
            }
        };
    }},
    Frenzy(new Tags(){{
        add(Tag.Buff);
        add(Tag.Martial);
    }}){ Buff makeLocal() {
        return new Buff(10) {
            public void onSet() {
                time += level * 10;
                var = 8 + level;
                host.get("Attack").add(var, true);
            }

            public void onFinish() {
                host.get("Attack").add(-var, true);
            }

            public boolean update() {
                Particle.sparkle("Fire", host.getRandomPoint(), 8, 3);
                return super.update();
            }
        };
    }},
    Rejuvenate(new Tags(){{
        add(Tag.Buff);
        add(Tag.Biology);
    }}){ Buff makeLocal(){
        return new Buff(20){
            private int tick = 0;
            public void onSet(){
                time += level*2;
                var = Math.max(1,10-level);
            }
            public boolean update(){
                if (tick++ % var == 0) host.get("Stamina").add(1);
                return super.update();
            }
        };
    }},
    Cleave(new Tags(){{
        add(Tag.Buff);
        add(Tag.Strong);
    }}){ Buff makeLocal(){
        return new Buff(40) {
            public void onSet() {
                time += level*10;
            }
            public boolean update() {
                Particle.dripHoly(((Man) host).weapon.getLine().ptd);
                return super.update();
            }
        };
    }},
    Bleed(new Tags(){{
        add(Tag.Debuff);
    }}){ Buff makeLocal(){
        return new Buff(100) {
            private int tick = 0;
            public void onSet() {
                var = 0;
            }
            public void onStack(){
                if (level < 18 && host.rand.nextBoolean()) level++;
            }
            public void onFinish(){
                if (var == 0) host.die(); }

            public boolean onTrigger(){
                if (level > 0) level--;
                if (level == 0) {
                    var = 1;
                    time = Global.getTime()[0];
                }
                return true;
            }
            public boolean update() {
                if (tick++ % (Math.max(1,20-level)) == 0){
                    Particle.explode("Blood",host.getRandomPoint(),new P(), (int) (Math.random()*level+1),5,0,true,24);
                    return super.update();
                }
                return false;
            }
        };
    }},
    Pain(new Tags(){{
        add(Tag.Debuff);
    }}){ Buff makeLocal(){
        return new Buff(20) {
            public void onSet() {
                Stat speed = host.get("Speed");
                var = Math.round(speed.get() * .1f);
                speed.add(-var, true);
            }
            public void onStack(){
                Stat speed = host.get("Speed");
                if (level < 18 && host.rand.nextBoolean()) level++;
                int tvar = Math.round(speed.get() * .1f);
                if (speed.get() >= tvar){
                    speed.add(-tvar, false);
                    var += tvar;
                } else {
                    var += speed.get();
                    speed.set(0, false);
                }
                time = Math.min(40, time + 10);
            }
            public boolean onTrigger(){
                time = Global.getTime()[0];
                return true;
            }
            public void onFinish() {
                host.get("Speed").add(var, false);
            }
        };
    }},
    Invincible(new Tags(){{
        add(Tag.Buff);
    }}){ Buff makeLocal(){
        return new Buff(8) {
            public void onSet() {
                time += 8*level;
            }
            public boolean update() {
                Particle.sparkle("Glare", host.getRandomPoint(),40, 6);
                return super.update();
            }
        };
    }},
    Evade(new Tags(){{
        add(Tag.Buff);
        add(Tag.Shadow);
    }}){ Buff makeLocal(){
        return new Buff(120) {
            public void onSet() {
                var = level*3;
            }
            public void onStack(){
                var+=level;
            }
            public boolean onTrigger(){
                if (var > 0) var--;
                else return false;
                if (var <= 0) time = Global.getTime()[0];
                return true;
            }
            public boolean update() {
                Particle.smoke(new P(host.pos));
                return super.update();
            }
        };
    }},
    Burn(new Tags(){{
        add(Tag.Debuff);
        add(Tag.Fire);
    }}){ Buff makeLocal(){
        return new Buff(100) {
            public void onSet() {
                sound("fire");
            }
            public boolean update() {
                Particle.smoke(host.getRandomPoint());
                Particle.burn(host.getRandomPoint());
                return super.update();
            }
        };
    }},
    Freeze(new Tags(){{
        add(Tag.Debuff);
        add(Tag.Cold);
    }}){ Buff makeLocal(){
        return new Buff(8) {
            public void onSet() {
                sound("freeze");
                var = Math.round(host.getSpeed());
                host.get("Speed").add(-var, false);
                host.stats.triggerEffect(Pain);
                time += level*24;
            }

            public void onFinish() {
                host.get("Speed").add(var, false);
            }

            public boolean update() {
                Particle.frozen(host.pos);
                return super.update();
            }
        };
    }},
    Slow(new Tags(){{
        add(Tag.Debuff);
        add(Tag.Agility);
    }}){ Buff makeLocal(){
        return new Buff(40) {
            public void onSet() {
                var = Math.round(host.getSpeed() * .5f);
                host.get("Speed").add(-var, false);
            }
            public void onFinish() {
                host.get("Speed").add(var, false);
            }
        };
    }},
    Mark(new Tags(){{
        add(Tag.Debuff);
        add(Tag.Martial);
    }}){ Buff makeLocal(){
        return new Buff(500) {
            private Thing mark;
            public void onLoad(){
                mark = new Thing("Mark", 150);
                mark.pos = host.pos;
                mark.save = false;
            }
            public void onSet() {
                sound("freeze");
                mark = new Thing("Mark", 150);
                mark.pos = host.pos;
                mark.save = false;
            }
            public boolean onTrigger(){
                time = Global.getTime()[0];
                if (mark != null) {
                    return true;
                }
                return false;
            }
            public void onFinish() {
                if (mark != null) mark.remove();
            }
        };
    }},
    Exhaust(new Tags(){{
        add(Tag.Debuff);
        add(Tag.Biology);
    }}){ Buff makeLocal(){
        return new Buff(100) {
            public void onSet() {
                sound("freeze");
                var = level;
                host.get("Stamina").add(-var, false);
            }

            public void onFinish() {
                host.get("Stamina").add(var, false);
            }

            public boolean update() {
                Particle.implode("Dust", host.pos.getAdd(0, -25), new P(), 1, 10, 1, false, 6);
                return super.update();
            }
        };
    }},
    Poison(new Tags(){{
        add(Tag.Debuff);
        add(Tag.Biology);
    }}){ Buff makeLocal(){
        return new Buff(100) {
            private int tick = 1;
            public void onSet() {
                sound("poison");
            }
            public boolean update() {
                tick--;
                if (tick == 0) {
                    host.get("Stamina").add(-level);
                    tick = Math.max(1,30-(level*2));
                    Particle.explode("Poison", host.getRandomPoint(), new P(host.dir*15,-15), 10, 3, 10, true, 16);
                }
                return super.update();
            }
        };
    }},
    Glacialwalk(new Tags(){{
        add(Tag.Buff);
        add(Tag.Cold);
    }}){ Buff makeLocal(){
        return new Buff(100) {
            public void onSet() {
                sound("freeze");
            }
            public boolean update() {
                if (Global.getTime()[0] % 3 == 0) {
                    final Projectile p = new Projectile("Ice", host.pos.getAdd(0, -5), new P(), 0) {
                        @Override
                        protected void onAttack(Unit p) {
                            p.buff(Buffs.Freeze,level);
                            Particle.explode("Ice", pos, new P(0,-20), 30, 10, 5, true, 10);
                            remove();
                        }
                        @Override
                        public void stuck() {
                            vel.set(0, 0);
                        }
                        @Override
                        public void update() {
                            super.update();
                            Particle.explode("Ice", pos, new P(0, -5), 1, 3, 2, false, 6);
                        }
                        @Override
                        public int getCollisionSize(){
                            return 30;
                        }
                    };
                    p.faction = host.faction;
                    p.gravity = true;
                    p.delay.set(50+(level*150));
                    p.delay.performer = new Performer(){
                        public void perform(){
                            p.remove();
                        }
                    };
                }
                return super.update();
            }
        };
    }},
    Flamestride(new Tags(){{
        add(Tag.Buff);
        add(Tag.Fire);
    }}){ Buff makeLocal(){
        return new Buff(100) {
            private int tick = 1;
            public void onSet() {
                sound("freeze");
            }
            public boolean update() {
                tick++;
                if (tick % 2 == 0) {
                    if (tick % 6 == 0) Sound.play("fire");
                    final Projectile p = new Projectile("Flamecluster", host.pos.getAdd(0, -5), new P(), 0) {
                        @Override
                        protected void onAttack(Unit p) {
                            super.onAttack(p);
                            Particle.explode("Flame", pos, new P(), 30, 20, 0, false, 2);
                            remove();
                        }
                        @Override
                        public void stuck() {
                            vel.set(0, 0);
                        }
                        @Override
                        public void update() {
                            super.update();
                            Particle.explode("Flame", pos, new P(0, -5), 1, 12, 3, false, 3);
                        }
                        @Override
                        public int getCollisionSize(){
                            return 30;
                        }
                    };
                    p.faction = host.faction;
                    p.damage = 9;
                    p.gravity = true;
                    p.delay.set(level*20);
                    p.delay.performer = new Performer(){
                        @Override
                        public void perform(){
                            p.remove();
                        }
                    };
                }
                return super.update();
            }
        };
    }},
    Stun(new Tags(){{
        add(Tag.Debuff);
        add(Tag.Cunning);
    }}){ Buff makeLocal(){
        return new Buff(40) {
            public void onSet() {
                sound("stun");
                time += 40*level;
                host.vel.x = 0;
                host.stateSet(Actions.Hit);
                host.delay.set(40*level);
                host.clip.setSpeed(999);
            }
            public boolean update() {
                Particle.implode("Glare", host.pos.getAdd(0, -25), new P(), 1, 10, 1, false, 6);
                return super.update();
            }
        };
    }};
    public Tags tags;
    Buffs(Tags tags){
        this.tags = tags;
    }
    Buff makeLocal(){
        return null;
    }
    public Buff make(Unit host, int level){
        Buff r = makeLocal();
        r.host = host;
        r.level = level;
        r.buff = this;
        return r;
    }
}
abstract class Buff{
    Unit host;
    int level;
    int time;
    int var;
    Buffs buff;
    Buff(int time){
        this.time = Global.getTime()[0]+time;
    }
    public void onSet(){}
    public void onLoad(){}
    public boolean onTrigger(){
        return true;
    }
    public void onFinish(){}
    public void onStack(){}
    public boolean update(){
        return Global.getTime()[0] >= time;
    }
    void sound(String p){
        Sound.play(p);
    }
    public String getDescription(){
        return buff.name() + " (Level " + level + ") - " + (time - Global.getTime()[0]) + "s";
    }
    public String toString(){
        return buff.ordinal() + "&" + level + "&" + time + "&" + var;
    }
}