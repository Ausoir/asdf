package com.ga.unit.skill;

import com.ga.game.Sound;
import com.ga.map.*;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.*;
import com.ga.unit.item.Projectile;
import com.ga.unit.player.MainPlayer;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Ga on 9/1/2016. asd
 */
public enum Buffs {
    Banishment(10, new Tags(){{
        add(Tag.Debuff);
        add(Tag.Shadow);
    }}){ Buff makeLocal() {
        return new Buff(4) {
            public void onSet() { //higher level = longer till banish
                time += level * 4;
            }

            public void onFinish() {
                if (host instanceof MainPlayer){
                    host.pos.set(Global.r.nextInt((int) host.map.size.x-64)+36, Global.r.nextInt((int) host.map.size.y-64)+36);
                } else host.remove();
            }

            public boolean update() {
                Particle.smoke(host.map, host.getRandomPoint(),host.getDepth());
                return super.update();
            }
        };
    }},
    Shadowcurse(100,new Tags(){{
        add(Tag.Debuff);
        add(Tag.Shadow);
        add(Tag.Conjuration);
    }}){ Buff makeLocal() {
        return new Buff(1) {
            private Man unit;
            public void onSet() {
                unit = (Man) Stock.getByName(host.map, "Phantom",-1);
                unit.weapon.remove();
                unit.pos.set(host.pos.x, host.pos.y);
            }

            public boolean update() {
                unit.remove();
                return super.update();
            }
        };
    }},
    Alert(1,new Tags(){{
        add(Tag.Debuff);
        add(Tag.Air);
    }}){ Buff makeLocal() {
        return new Buff(1) {
            public void onSet() {
                Particle.sparkle(host.map, "Fire", host.getDepth(), host.getRandomPoint(), 8, 5);
                for (Unit i : host.map.updater.collideUnit(host.pos,1600*level, Faction.Destructive)){
                    i.brain.target = host;
                    i.brain.onHit();
                }
            }

            public boolean update() {
                Particle.sparkle(host.map, "Fire", host.getDepth(), host.getRandomPoint(), 8, 5);
                return super.update();
            }
        };
    }},
    Haste(5,new Tags(){{
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
                    Particle.cleanse(host.map, host.pos, host.getDepth());
                }
                return super.update();
            }
        };
    }},
    Frenzy(5,new Tags(){{
        add(Tag.Buff);
        add(Tag.Might);
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
                Particle.sparkle(host.map, "Fire", host.getDepth()+1, host.getRandomPoint(), 8, 3);
                return super.update();
            }
        };
    }},
    Rejuvenate(5,new Tags(){{
        add(Tag.Buff);
        add(Tag.Mutation);
    }}){ Buff makeLocal(){
        return new Buff(20){
            private int tick = 0;
            public void onSet(){
                time += level*2;
                var = Math.max(1,10-level);
            }
            public boolean update(){
                if (tick++ % var == 0) host.get("Stamina").add(1, false);
                return super.update();
            }
        };
    }},
    Cleave(5,new Tags(){{
        add(Tag.Buff);
        add(Tag.Might);
    }}){ Buff makeLocal(){
        return new Buff(40) {
            public void onSet() {
                time += level*10;
            }
            public boolean update() {
                Particle.dripHoly(host.map, ((Man) host).weapon.getLine().ptd, host.getDepth());
                return super.update();
            }
        };
    }},
    Metabolism(2,new Tags(){{
    }}){ Buff makeLocal(){
        return new Buff(100) {
            public void onStack(int level){
                this.level+= level;
            }
            public boolean update() {
                if (super.update()) {
                    host.stats.triggerEffect(Bleed);
                    if (host instanceof MainPlayer){
                        time += level * 10;
                        Global.player.metabolise();
                    }
                }
                return super.update();
            }
        };
    }},
    Bleed(5,new Tags(){{
        add(Tag.Debuff);
    }}){ Buff makeLocal(){
        return new Buff(800) {
            private int tick = 0;
            public void onStack(int level){
                this.level += level;
                time -= 40;
            }
            public boolean onTrigger(){
                if (level > 0) level--;
                return true;
            }
            public boolean update() {
                if (tick++ % (Math.max(1,20-level)) == 0) {
                    Particle.explode(host.map, "Blood", host.getDepth()+1, host.getRandomPoint(), new P(), (int) (Math.random() * level + 1), 5, 0, true, 24);
                }
                if (super.update()) host.die();
                return level == 0;
            }
        };
    }},
    Evade(5,new Tags(){{
        add(Tag.Buff);
        add(Tag.Shadow);
    }}){ Buff makeLocal(){
        return new Buff(2) {
            public void onSet() {
                time += level*2;
                host.tangible = false;
            }
            public void onLoad(){
                host.tangible = false;
            }
            public boolean onTrigger(){
                time = Global.time;
                return true;
            }
            public void onFinish(){
                host.tangible = true;
            }
            public boolean update() {
                Particle.smoke(host.map, new P(host.pos), host.getDepth());
                return super.update();
            }
        };
    }},
    Shock(1,new Tags(){{
        add(Tag.Debuff);
        add(Tag.Air);
    }}){ Buff makeLocal(){
        return new Buff(7) {
            public void onSet(){
                Particle.shock(host.map, host.getRandomPoint(), host.getDepth());
                if (!host.get("Stamina").require(level)){
                    Particle.holyExplosion(host.map, host.pos, host.getDepth());
                    host.stateSet(Actions.Down);
                }
            }
            public void onStack(int level) {
                Particle.shock(host.map, host.getRandomPoint(), host.getDepth());
                time = Global.time + 7;
                if (!host.get("Stamina").require(1)){
                    Particle.holyExplosion(host.map, host.pos, host.getDepth());
                    host.stateSet(Actions.Down);
                }
            }
            public boolean update() {
                Particle.sparkle(host.map, "Glare", host.getDepth(),host.getRandomPoint(),20+host.getSize(),4);
                if (var++ % 40 == 0) host.get("Stamina").add(-1,false);
                return super.update();
            }
        };
    }},
    Burn(3,new Tags(){{
        add(Tag.Debuff);
        add(Tag.Fire);
    }}){ Buff makeLocal(){
        return new Buff(5) {
            public void onSet() {
                Sound.play(host.pos, "fire");
                time += level*3;
            }
            public boolean update() {
                if (var % 10 == 0 && Global.r.nextBoolean()) {
                    if (var % 50 == 0){
                        host.checkHit(host.pos, level);
                        if (host.stats.getEffectLevel(Bleed) > 2){
                            host.stats.getEffect(Bleed).time += Global.r.nextInt(100);
                        }
                    }
                    for (Unit i : host.map.updater.collideUnit(host.pos, host.getSize() + 40, Faction.Destructive)) {
                        if (Global.r.nextBoolean()) i.buff(Burn, 1);
                    }
                    host.stateSet(Actions.Sprint);
                    host.delay.time = 24;
                }
                var++;
                Particle.smoke(host.map, host.getRandomPoint(), host.getDepth()+1);
                Particle.burn(host.map, host.getRandomPoint(), host.getDepth()+1);
                return super.update();
            }
        };
    }},
    Freeze(3,new Tags(){{
        add(Tag.Buff);
        add(Tag.Debuff);
        add(Tag.Ice);
    }}){ Buff makeLocal(){
        return new Buff(0) {
            boolean frozen = false;
            private Terrain ice;
            private Line[] lines;
            private final static int freezeThreshold = 24;
            public void onSet() {
                Sound.play(host.pos, "freeze");
                var = host.get("Speed").add(-Math.round(host.getSpeed()*30), false);
                host.get("Attack").add(-15, true);
                time += level;
                host.stats.triggerEffect(Buffs.Bleed);
                host.stats.triggerEffect(Buffs.Burn);
                host.stats.triggerEffect(Buffs.Shock);
                onLoad();
            }
            public void onLoad(){
                if (time-Global.time >= freezeThreshold) freeze();
            }
            public void onStack(int level){
                if (!frozen) {
                    var += host.get("Speed").add(-level, false);
                    time += level;
                    onLoad();
                }
            }
            private void freeze(){
                frozen = true;
                int size = host.getSize();
                ice = new Terrain(host.map, "_130,180,255,140", Shader.None, host.getDepth()+1);
                int num = Global.r.nextInt(3)+5;
                P[] points = new P[num];
                float gap = 6.28f/num;
                for (int i=0;i<num;i++){
                    float currentArc = gap*i+Global.r.nextFloat()*gap/6-gap/3;
                    points[i] = host.pos.getAdd(Global.cos(currentArc)*(size+16), Global.sin(currentArc)*(size+16)-size/2);
                    ice.addPoint(points[i].x, points[i].y);
                }
                lines = new Line[num];
                for (int i=0;i<num;i++) {
                    lines[i] = new Line(points[i], points[i]) {
                        public String toString() {
                            return "";
                        }
                    };
                    lines[i].depth = host.getDepth();
                    if (!lines[i].clockwise()) lines[i].opt = -1;
                    host.map.land.add(lines[i]);
                }
                host.mobile = false;
                host.tangible = false;
                ice.render();
                time = Global.time + freezeThreshold + Global.r.nextInt(freezeThreshold) + (freezeThreshold/2);
            }
            private void melt(){
                host.mobile = true;
                host.tangible = true;
                frozen = false;
                host.map.land.removeAll(Arrays.asList(lines));
                host.map.updater.remove(ice);
                time = Global.time + 16;
            }
            public void onFinish() {
                host.get("Speed").add(-var, true);
                host.get("Attack").add(15, true);
            }
            public boolean onTrigger(){
                if (level > 1) level--;
                else time = Global.time;
                return true;
            }
            public boolean update() {
                if (!frozen) {
                    Particle.frozen(host.map, host.pos, host.getDepth()+1);
                    level = (time - Global.time) / 8 + 1;
                } else {
                    if (time - Global.time < freezeThreshold) melt();
                    host.delay.set(2);
                }
                return super.update();
            }
        };
    }},
    Mark(7,new Tags(){{
        add(Tag.Debuff);
        add(Tag.Shadow);
    }}){ Buff makeLocal(){
        return new Buff(4) {
            private Thing mark;
            public void onLoad(){
                mark = new Thing(host.map, "Mark", host.seed, 150);
                mark.image.setCenterType(2);
                mark.pos = host.pos;
                mark.save = false;
            }
            public void onSet() {
                time += level*6;
                Sound.play(host.pos, "freeze");
                mark = new Thing(host.map, "Mark", host.seed, 150);
                mark.image.setCenterType(2);
                mark.pos = host.pos;
                mark.save = false;
            }
            public boolean onTrigger(){
                time = Global.time;
                return mark != null;
            }
            public void onFinish() {
                if (mark != null) mark.remove();
            }
        };
    }},
    Poison(7,new Tags(){{
        add(Tag.Debuff);
        add(Tag.Mutation);
    }}){ Buff makeLocal(){
        return new Buff(100) {
            private int tick = 0;
            public void onSet() {
                var = 0;
            }
            public void onStack(int level){
            }
            public void onFinish(){
                if (var == 0) host.die(); }

            public boolean onTrigger(){
                if (level > 0) level--;
                if (level == 0) {
                    var = 1;
                    time = Global.time;
                }
                return true;
            }
            public boolean update() {
                if (tick++ % (Math.max(1,20-level)) == 0){
                    Particle.explode(host.map, "Poison", host.getDepth(),host.getRandomPoint(),new P(), level+1,5,0,true,24);
                    return super.update();
                }
                return false;
            }
        };
    }},
    Glacialwalk(5,new Tags(){{
        add(Tag.Buff);
        add(Tag.Ice);
    }}){ Buff makeLocal(){
        return new Buff(4) {
            private int tick = 1;
            public void onSet() {
                Sound.play(host.pos, "freeze");
            }
            public boolean update() {
                tick++;
                if (tick % 2 == 0) {
                    final Projectile p = new Projectile(host.map, "Ice", host.pos.getAdd(0, -5), new P(), 0) {
                        @Override
                        public void onAttack(Unit p) {
                            p.buff(Buffs.Freeze, level);
                            Particle.explode(host.map, "Ice", host.getDepth(), pos, new P(0, -20), 30, 10, 5, true, 10);
                            remove();
                        }

                        @Override
                        public void stuck() {
                            vel.set(0, 0);
                        }

                        @Override
                        public void update() {
                            super.update();
                            Particle.explode(host.map, "Ice", host.getDepth(), pos, new P(0, -5), 1, 3, 2, false, 6);
                        }

                        @Override
                        public int getCollisionSize() {
                            return 30;
                        }
                    };
                    p.faction = host.faction;
                    p.gravity = true;
                    p.delay.set(50 + (level * 150));
                    p.delay.performer = new Performer() {
                        public void perform() {
                            p.remove();
                        }
                    };
                }
                return super.update();
            }
        };
    }},
    Stun(8,new Tags(){{
        add(Tag.Debuff);
        add(Tag.Agility);
    }}){ Buff makeLocal(){
        return new Buff(3) {
            public void onSet() {
                Sound.play(host.pos, "stun");
                time += level*2;
                host.vel.x = 0;
                host.stateSet(Actions.Hit);
                host.delay.set((time-Global.time) * 40);
                host.clip.setSpeed(99);
            }
            public void onLoad(){
                host.stateSet(Actions.Hit);
                host.delay.set((time-Global.time) * 40);
                host.clip.setSpeed(99);
            }
            public boolean update() {
                Particle.implode(host.map, "Glare", host.getDepth(), host.pos.getAdd(0, -24), new P(), 1, 10, 1, false, 6);
                return super.update();
            }
        };
    }},
    Slowfall(2,new Tags(){{
    }}){ Buff makeLocal(){
        return new Buff(4) {
            public void onSet(){
                time += 8*level;
            }
            public boolean update() {
                host.vel.y /= level+1;
                return super.update();
            }
        };
    }};
    public Tags tags;
    public float value;
    Buffs(float value, Tags tags){
        this.value = value;
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
    public static ArrayList<Buffs> getByTag(Tag p){
        ArrayList<Buffs> list = new ArrayList<>();
        for (Buffs i : values()){
            if (i.tags.contains(p)) list.add(i);
        }
        return list;
    }
}
abstract class Buff{
    Unit host;
    int level, time, var;
    Buffs buff;
    Buff(int time){
        this.time = Global.time+time;
    }
    public void onSet(){}
    public void onLoad(){}
    public boolean onTrigger(){
        return true;
    }
    public void onFinish(){}
    public void onStack(int level){}
    public boolean update(){
        return Global.time >= time;
    }
    public String getDescription(){
        return buff.name() + " (Level " + level + ") - " + (time - Global.time) + "s";
    }
    public String toString(){
        return buff.ordinal() + "&" + level + "&" + time + "&" + var;
    }
}