package com.ga.unit.skill;

import com.ga.game.FileManager;
import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.Shader;
import com.ga.map.Terrain;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.tool.SyncRand;
import com.ga.unit.*;
import com.ga.unit.item.Projectile;
import com.ga.unit.man.MainPlayer;
import com.ga.unit.man.Man;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by Ga on 9/1/2016. asd
 */
public enum Buffs {
    Hex(0,new Tags(){{
        add(Tag.Enchant);
        add(Tag.Curse);
    }}){ Buff makeLocal(int level){
        return new Buff(level, 0) {
            public boolean onTrigger(){
                var = 1;
                return true;
            }
            public boolean update() {
                if (var == 1) return true;
                time = Global.time;
                return false;
            }
            public String getDescription(){
                return buff.name() + " [Prepared:"+Buffs.values()[var].name()+"]";
            }
        };
    }},
    Alert(1,new Tags(){{
        add(Tag.Curse);
    }}){ Buff makeLocal(int level) {
        return new Buff(level, 1) {
            public void onSet() {
                Particle.sparkle(host.map, "Fire", host.getDepth(), host.getRandomPoint(), 8, 5);
                for (Unit i : host.map.updater.collideUnit(host.pos,1600*level, Faction.Destructive)){
                    i.brain.target = host;
                    i.brain.onHit();
                }
            }
            public void onStack(int level){
                this.level = level;
                onSet();
            }

            public boolean update() {
                Particle.sparkle(host.map, "Fire", host.getDepth(), host.getRandomPoint(), 8, 5);
                return super.update();
            }
        };
    }},
    Cleave(1,new Tags(){{
        add(Tag.Enchant);
    }}){ Buff makeLocal(int level){
        return new Buff(level, 10) {
            public void onSet(){
                if (!(host instanceof Man)) time = Global.time;
            }
            public boolean update() {
                boolean r = super.update();
                if (!r) {
                    if (((Man) host).wieldingNone()) r = true;
                    Particle.dripHoly(host.map, ((Man) host).weapon.getLine().ptd, host.getDepth());
                }
                return r;
            }
        };
    }},
    Slowfall(1,new Tags(){{
        add(Tag.Enchant);
        add(Tag.Air);
    }}){ Buff makeLocal(int level){
        return new Buff(level, 4) {
            private int turn = 0;
            public boolean update() {
                if (host.vel.y > 0){
                    host.vel.y /= Math.log(Math.E+level);
                    if (host.lastLand == Line.blank && turn++ % 20 == 0) host.clip.setState("Jump");
                }
                return super.update();
            }
        };
    }},
    Shock(1,new Tags(){{
        add(Tag.Curse);
        add(Tag.Air);
    }}){ Buff makeLocal(int level){
        return new Buff(level, 4) {
            public void onSet(){
                Particle.shock(host.map, host.getRandomPoint(), host.getDepth());
                spike();
            }
            public void onStack(int level) {
                super.onStack(level);
                Particle.shock(host.map, host.getRandomPoint(), host.getDepth());
                time = Global.time + 20;
                spike();
            }
            private void spike(){
                if (!host.get("Strength").require(1)){
                    Particle.holyExplosion(host.map, host.pos, host.getDepth());
                    host.setState(Actions.Down);
                    host.stats.clearBuffs();
                }
            }
            public boolean update() {
                Particle.sparkle(host.map, "Glare", host.getDepth(),host.getRandomPoint(),20+host.getSize(),4);
                if (var++ % 60 == 0){
                    host.setState(Actions.Hit);
                    host.get("Strength").add(-1,false);
                }
                return super.update();
            }
        };
    }},
    Burn(1,new Tags(){{
        add(Tag.Curse);
        add(Tag.Fire);
    }}){ Buff makeLocal(int level){
        return new Buff(level, 4) {
            public boolean isDangerous() { return true; }
            public void onSet() {
                Sound.play(host.map, host.pos, "fire");
            }
            public boolean update() {
                if (var % 20 == 0 && Global.r.nextBoolean()) {
                    host.hit(host.getPosLine(), level);
                    if (level > 1) {
                        for (Unit i : host.map.updater.collideUnit(host.pos, host.getSize() + 40, Faction.Destructive)) {
                            if (Global.r.nextBoolean()) i.buff(Burn, 1);
                        }
                    }
                }
                var++;
                Particle.smoke(host.map, host.getRandomPoint(), host.getDepth()+.01f);
                Particle.burn(host.map, host.getRandomPoint(), host.getDepth()+.01f);
                return super.update();
            }
        };
    }},
    Freeze(1,new Tags(){{
        add(Tag.Curse);
        add(Tag.Air);
    }}){ Buff makeLocal(int level){
        return new Buff(level, 1) {
            boolean frozen = false;
            private Terrain ice;
            private Line[] lines;
            private final static int freezeThreshold = 24;
            public void onSet() {
                Sound.play(host.map, host.pos, "freeze");
                var = host.get("Speed").add(-Math.round(host.getSpeed()*30), false);
                host.get("Attack").add(-15, true);
                host.stats.triggerEffect(Buffs.Bleed);
                host.stats.triggerEffect(Buffs.Burn);
                onLoad();
            }
            public void onLoad(){
                if (time-Global.time >= freezeThreshold) freeze();
            }
            public void onStack(int level){
                if (!frozen) {
                    super.onStack(level);
                    var += host.get("Speed").add(-level, false);
                    onLoad();
                }
            }
            private void freeze(){
                frozen = true;
                int size = host.getSize();
                ice = new Terrain(host.map, "_130,180,255,140", Shader.None, 500);
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
                ice.start();
                time = Global.time + freezeThreshold + Global.r.nextInt(freezeThreshold) + (freezeThreshold/2);
            }
            private void melt(){
                host.mobile = true;
                host.tangible = true;
                frozen = false;
                host.map.land.removeAll(Arrays.asList(lines));
                host.map.updater.remove(ice);
                ice.remove();
                time = Global.time + 16;
            }
            public void onFinish() {
                if (frozen) melt();
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
                    Particle.frozen(host.map, host.pos, host.getDepth()+.01f);
                    level = (time - Global.time) / 8 + 1;
                } else {
                    if (time - Global.time < freezeThreshold) melt();
                    host.delay.set(2);
                }
                return super.update();
            }
        };
    }},
    Mark(1,new Tags(){{
        add(Tag.Curse);
    }}){ Buff makeLocal(int level){
        return new Buff(level, 4) {
            private Thing mark;
            public void onLoad(){
                mark = new Thing(host.map, "Mark", host.seed, host.getDepth()+1);
                mark.image.setCenterType(2);
                mark.start();
                mark.save = false;
            }
            public void onSet() {
                time += level;
                Sound.play(host.map, host.pos, "freeze");
                mark = new Thing(host.map, "Mark", host.seed, host.getDepth()+1);
                mark.image.setCenterType(2);
                mark.start();
                mark.save = false;
            }
            public boolean onTrigger(){
                time = Global.time;
                return mark != null;
            }
            public void onFinish() {
                if (mark != null) mark.remove();
            }
            public boolean update(){
                mark.pos = host.getHead();
                return super.update();
            }
        };
    }},
    Bleed(1,new Tags(){{
        add(Tag.Curse);
    }}){
        final int[] bleedRate = new int[]{999, 80, 24, 8, 2, 2};
        Buff makeLocal(int level){
            return new Buff(level, 0) {
                private int tick = 0;
                public void onSet(){
                    time = Global.time + 80;
                    if (level >= 5) host.die("Bleeding");
                }
                public void onStack(int level){
                    this.level += level;
                    onSet();
                }
                public boolean onTrigger(){
                    if (level > 0) level--;
                    return true;
                }
                public boolean update() {
                    if (tick++ % bleedRate[(int) Global.clamp(level, 0, 5)] == 0) {
                        Particle.explode(host.map, "Blood", host.getDepth()+.01f, host.getRandomPoint(), new P(), (int) (Math.random() * level + 1), 5, 0, true, 24);
                    }
                    if (super.update()){
                        onTrigger();
                        time += 80;
                    }
                    return level == 0;
                }
            };
        }},
    Firewalk(2,new Tags(){{
        add(Tag.Enchant);
        add(Tag.Fire);
    }}){ Buff makeLocal(int level){
        return new Buff(level, 6) {
            private int tick = 1;
            public void onSet() {
                Sound.play(host.map, host.pos, "fire");
            }
            public boolean update() {
                tick++;
                if (tick % 8 == 0) {
                    final Projectile p = new Projectile(host.map, "Flame", host.pos.getAdd(0, -5), new P(), 0) {
                        @Override
                        public void onAttack(Unit p) {
                            p.buff(Buffs.Burn, level);
                            Particle.explode(host.map, "Flame", host.getDepth(), pos, new P(0, -20), 3, 10, 25, true, 6);
                            Particle.explode(host.map, "Smoke", host.getDepth(), pos, new P(0, -20), 3, 15, 25, false, 6);
                            remove();
                        }
                        @Override
                        public void stuck() {
                            vel.set(0, 0);
                        }
                        @Override
                        public void update(){
                            Particle.explode(host.map, "Flame", host.getDepth(), pos, new P(0, -20), 1, 10, 8, false, 2);
                            super.update();
                        }
                    };
                    p.faction = host.faction;
                    p.gravity = true;
                    p.delay.set(50);
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
    Infestation(2, new Tags(){{
        add(Tag.Curse);
        add(Tag.Conjuration);
    }}){ Buff makeLocal(int level){
        return new Buff(level, 9) {
            public void onSet() {
                Sound.play(host.map, host.pos, "stun");
            }
            public boolean update() {
                Particle.implode(host.map, "Poison", host.getDepth(), host.pos.getAdd(0, -24), 1, 10, false, 6);
                if (var++ % Math.max(1, 200/level) == 0) Stock.Slime.create(host.map,-1,-1).start().pos.set(host.pos);
                return super.update();
            }
        };
    }},
    Haste(2,new Tags(){{
        add(Tag.Enchant);
        add(Tag.Athletics);
    }}){ Buff makeLocal(int level) {
        return new Buff(level, 10) {
            public void onSet() {
                var = 8 + level;
                host.get("Attack").add(var, true);
            }
            public void onStack(int level){}
            public void onFinish() {
                host.get("Attack").add(-var, true);
            }
            public boolean update() {
                Particle.sparkle(host.map, "Fire", host.getDepth()+.01f, host.getRandomPoint(), 8, 3);
                return super.update();
            }
        };
    }},
    Mutilation(2, new Tags(){{
        add(Tag.Curse);
    }}){ Buff makeLocal(int level){
        return new Buff(level, 8) {
            public void onSet() {
                Sound.play(host.map, host.pos, "stun");
                ArrayList<Organ> amputables = new ArrayList<>();
                for (Organ organ : host.clip.organs){
                    if (!organ.vital && !organ.amputated) amputables.add(organ);
                }
                var = -1;
                if (amputables.size() > 0){
                    var = SyncRand.r.nextInt(amputables.size());
                    host.clip.organs[var].amputated = true;
                } else {
                    host.buff(Bleed, 1);
                    host.setState(Actions.Hit);
                }
            }
            public void onFinish() {
                if (var > 0) host.clip.organs[var].amputated = false;
            }
        };
    }},
    Root(2,new Tags(){{
        add(Tag.Curse);
        add(Tag.Conjuration);
    }}){ Buff makeLocal(int level){
        return new Buff(level, 10) {
            public void onSet() {
                Sound.play(host.map, host.pos, "stun");
                var = host.get("Speed").get();
                host.get("Speed").set(0, true);
            }
            public void onFinish(){
                host.get("Speed").add(var, true);
            }
            public boolean update() {
                Particle.implode(host.map, "Glare", host.getDepth(), host.pos.getAdd(0, -24), 1, 10, false, 6);
                return super.update();
            }
        };
    }},
    Destabilize(2,new Tags(){{
        add(Tag.Curse);
        add(Tag.Void);
    }}){ Buff makeLocal(int level){
        return new Buff(level, 9) {
            private int turn = 0;
            public boolean update() {
                Particle.implode(host.map, "Smoke", host.getDepth(), host.getRandomPoint(), 3, 10, false, 1);
                if (turn++ % (100/level) == 0) Actions.Blink.onSet(host);
                return super.update();
            }
        };
    }},
    Stun(2,new Tags(){{
        add(Tag.Curse);
        add(Tag.Air);
    }}){ Buff makeLocal(int level){
        return new Buff(level, 3) {
            public void onSet() {
                Sound.play(host.map, host.pos, "stun");
                host.vel.x = 0;
                host.setState(Actions.Hit);
                host.delay.set((time-Global.time) * 40);
                host.clip.setSpeed(99);
            }
            public void onLoad(){
                host.setState(Actions.Hit);
                host.delay.set((time-Global.time) * 40);
                host.clip.setSpeed(99);
            }
            public boolean update() {
                Particle.implode(host.map, "Glare", host.getDepth(), host.pos.getAdd(0, -24), 1, 10, false, 6);
                return super.update();
            }
        };
    }},
    Windwalk(2,new Tags(){{
        add(Tag.Enchant);
        add(Tag.Air);
    }}){ Buff makeLocal(int level) {
        return new Buff(level, 16) {
            public void onSet() {
                var = Math.round(host.get("Speed").getMax());
                host.get("Speed").add(var, true);
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
    Morph(2,new Tags(){{
        add(Tag.Curse);
        add(Tag.Enchant);
        add(Tag.Channeling);
    }}){
        private String[] targets = new String[]{"Bird", "Slime", "Dog"};
        private Body origForm;
        Buff makeLocal(int level){
            return new Buff(level, 0) {
                public void onSet() {
                    this.time = Global.time + 20;
                    Sound.play(host.map, host.pos, "cast");
                    morph();
                }
                public void onLoad(){
                    morph();
                    host.clip.load(host.toString().split("@",-1)[7]);
                }
                private void morph(){
                    origForm = host.clip;
                    host.clip = new Body(host, targets[level % targets.length]);
                    if (host instanceof Man) ((Man) host).armor.drop();
                }
                public void onFinish(){
                    host.clip = origForm;
                }
            };
    }},
    Evade(3,new Tags(){{
        add(Tag.Enchant);
        add(Tag.Strength);
    }}){ Buff makeLocal(int level){
        return new Buff(level, 3) {
            public void onSet() {
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
    Rejuvenate(3,new Tags(){{
        add(Tag.Enchant);
        add(Tag.Strength);
    }}){ Buff makeLocal(int level){
        return new Buff(level, 9){
            private int tick = 0;
            public boolean update(){
                if (tick++ % Math.max(10, 200/level) == 0) host.get("Strength").add(1, false);
                return super.update();
            }
        };
    }},
    Poison(3,new Tags(){{
        add(Tag.Curse);
    }}){ Buff makeLocal(int level){
        return new Buff(level, 100) {
            private int tick = 0;
            public boolean isDangerous() { return true; }
            public void onSet() {
                var = 0;
            }
            public void onStack(int level){}
            public void onFinish(){
                if (var == 0) host.die("Poisoning");
            }
            public boolean onTrigger(){
                if (level > 0) level--;
                if (level == 0) {
                    var = 1;
                    time = Global.time;
                }
                return true;
            }
            public boolean update() {
                if (tick++ % (level+2) == 0){
                    Particle.explode(host.map, "Poison", host.getDepth(),host.getRandomPoint(),new P(), 1,5,0,true,24);
                    return super.update();
                }
                return false;
            }
        };
    }},
    Frenzy(3,new Tags(){{
        add(Tag.Enchant);
    }}){ Buff makeLocal(int level){
        return new Buff(level, 4) {
            private int turn = 0;
            public boolean update() {
                if (turn++ % Math.max(1,40/level) == 0) {
                    for (Map.Entry pair : host.cooldown.entrySet()) {
                        ((Cooldown) pair.getValue()).time--;
                    }
                    Particle.explode(host.map, "Spark", host.getDepth(), host.getHead(), new P(0,-1), 6, 10,20,true, 6);
                }
                return super.update();
            }
        };
    }},
    Attunement(3,new Tags(){{
        add(Tag.Enchant);
        add(Tag.Channeling);
        add(Tag.Energy);
    }}){ Buff makeLocal(int level) {
        return new Buff(level, 16) {
            public boolean onTrigger(){
                if (level > 1) level--;
                else time = Global.time;
                return true;
            }
        };
    }},
    Invisibility(4,new Tags(){{
        add(Tag.Enchant);
        add(Tag.Void);
    }}){ Buff makeLocal(int level){
        return new Buff(level, 4) {
            public void onSet() {
                Sound.play(host.map, host.pos, "shadowstep");
                for (Unit i : host.map.updater.unitsLoaded){
                    if (i.brain.target == host) i.brain.target = null;
                }
                onLoad();
            }
            public void onLoad(){
                host.setVisible(false);
            }
            public void onFinish(){
                host.setVisible(true);
            }
        };
    }},
    Shadowcurse(4,new Tags(){{
        add(Tag.Curse);
        add(Tag.Conjuration);
    }}){ Buff makeLocal(int level) {
        return new Buff(level, 1) {
            private Man unit;
            public void onSet() {
                unit = (Man) Stock.createByName(host.map, "Phantom",-1).start();
                unit.brain.target = host;
                unit.brain.setTask(Behavior.Hunt);
                unit.pos.set(host.pos.x, host.pos.y);
                unit.buff(Banishment, level);
            }
        };
    }},
    Banishment(4,new Tags(){{
        add(Tag.Curse);
        add(Tag.Void);
    }}){ Buff makeLocal(int level) {
        return new Buff(level, 8) {
            private int turn = 0;
            public boolean isDangerous() { return true; }
            public void onFinish() {
                if (host instanceof MainPlayer){
                    Global.server.requestChangeMap(FileManager.makeDirectory("ins/_")+(SyncRand.r.nextInt(20*level)-(10*level))+"@1");
                    Global.server.afterMapChange = new Performer(){
                        public void perform(){
                            host.pos.set(Global.r.nextInt((int) host.map.size.x-64)+36, 48);
                        }
                    };
                } else host.remove();
            }
            public boolean update() {
                if (turn++ % 2 == 0)
                    Particle.explode(host.map, "Smoke", host.getDepth(), host.getRandomPoint(), new P(), 1, 10, 0, true, 12);
                return super.update();
            }
        };
    }};
    public Tags tags;
    public int value;
    Buffs(int value, Tags tags){
        this.value = value;
        this.tags = tags;
    }
    Buff makeLocal(int level){
        return null;
    }
    public Buff make(Unit host, int level){
        Buff r = makeLocal(level);
        r.host = host;
        r.buff = this;
        return r;
    }
    public static ArrayList<Buffs> getByLevel(int p){
        ArrayList<Buffs> list = new ArrayList<>();
        for (Buffs i : values()){
            if (i.value <= p) list.add(i);
        }
        return list;
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
    private int increment;
    int level, time, var = 0;
    Buffs buff;
    Buff(int level, int time){
        this.increment = time;
        this.time = Global.time + (increment * level);
        this.level = level;
    }
    public void onSet(){}
    public void onLoad(){}
    public boolean onTrigger(){
        return true;
    }
    public void onFinish(){}
    public void onStack(int level){
        this.level = level;
        time = Global.time + (increment * level);
    }
    public boolean isDangerous(){
        return false;
    }
    public boolean update(){
        return Global.time >= time;
    }
    public String getDescription(){
        return buff.name() + " (Level " + level + ") - " + (time - Global.time) + "s";
    }
    public String toString(){
        return buff.ordinal() + "&" + level + "&" + (time - Global.time) + "&" + var;
    }
}