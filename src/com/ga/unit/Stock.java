package com.ga.unit;

import com.ga.game.Dialog;
import com.ga.game.*;
import com.ga.game.Menu;
import com.ga.map.*;
import com.ga.tool.*;
import com.ga.unit.item.*;
import com.ga.unit.man.BodyHumanoid;
import com.ga.unit.man.MainPlayer;
import com.ga.unit.man.Man;
import com.ga.unit.man.Player;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Buffs;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ga on 2/3/2017. asd
 */
public enum Stock {
    Spawn{ protected Prop make(Map map, int id, int seed){
        Prop product = new Prop(map, null, seed);
        product.name = "Spawn";
        product.manualDepth = true;
        product.mobile = false;
        product.image.visible = false;
        product.image.set(Picture.loadCache("POI"));
        return product;
    }},
    Travel{ Prop make(Map map, final int id, final int seed){
        Prop product = new Prop(map, null, seed){
            public String[] load(String p){
                String[] r = super.load(p);
                name = r[5];
                return r;
            }
            public String toString(){
                return super.toString() + "@" + name;
            }
        };
        product.name = "Travel";
        product.image.set(Picture.loadCache("Move"));
        product.manualDepth = true;
        product.mobile = false;
        product.image.visible = false;
        product.onInteract = new Interactive(){
            public void interact(com.ga.unit.man.Man p){
                if (p == Global.player) Global.server.requestChangeMap(product.name+"@"+seed);
            }
        };
        return product;
    }},
    Info{ Prop make(Map map, final int id, final int seed){
        Prop product = new Prop(map, null, seed){
            public String[] load(String p){
                String[] r = super.load(p);
                name = r[5];
                return r;
            }
            public String toString(){
                return super.toString() + "@" + name;
            }
        };
        product.name = "Info";
        product.image.set(Picture.loadCache("Describe"));
        product.manualDepth = true;
        product.mobile = false;
        product.image.visible = false;
        product.onInteract = new Interactive(){
            public void interact(com.ga.unit.man.Man p){
                if (p == Global.player) Menu.menu.showDialog("Info", product.name);
            }
        };
        return product;
    }},
    Object{ Prop make(Map map, int id, int seed){
        String[] data = FileManager.readText("Data/raw/Object.txt");
        final String[] v = data[id].split(";");
        final Prop product;
        if (!v[6].equals("0")) {
            product = new Prop(map, v[0], seed){
                public void onField(){
                    super.onField();
                    int offset = -Integer.parseInt(v[6]);
                    P s = image.getSize().getMult(.5f, image.getCenterType() == 1 ? .5f : 1).add(0,offset);
                    P top = new P(Global.sin(rotation) * s.y, Global.cos(rotation) * s.y);
                    P width = new P(Global.sin(rotation) * s.x, Global.cos(rotation) * s.x);
                    map.land.add(new Line(pos.x + top.x - width.y, pos.y - top.y - width.x,
                            pos.x + top.x + width.y, pos.y - top.y + width.x, -1, 0, lastLand.depth - OFFSET_PROP) {
                        public String toString() {
                            return "";
                        }
                    });
                }
            };
        } else product = new Prop(map, v[0], seed);
        product.subId = id;
        product.gravity = v[2].equals("1");
        product.manualDepth = !product.gravity;
        product.mobile = v[3].equals("1");
        product.randomSize(new P(v[4]));
        if (v[5].equals("1")) product.dir = product.seed % 2 == 0 ? 1 : -1;
        product.image.setCenterType(Integer.parseInt(v[1]));
        return product;
    }},
    Beast{ Prop make(Map map, int id, int seed) {
        String[] units = FileManager.readText("Data/raw/Beast.txt");
        String[] unit = units[id].split(";");
        Unit product = new Unit(map, null, seed){
            public void onCreate(){
                UnitFactory unitString = new UnitFactory(this, Faction.valueOf(unit[3]), unit[2].split(","));
                unitString.setMoveset("Melee");
            }
        };
        product.clip = new Body(product, unit[0]);
        product.speechType = unit[1];
        product.subId = id;
        return product;
    }},
    Man{ Prop make(Map map, int id, int seed){
        String[] units = FileManager.readText("Data/raw/Man.txt");
        final String[] unit = units[id].split(";",-1);
        com.ga.unit.man.Man product = new Man(map, unit[0], seed){
            public void onCreate(){
                UnitFactory unitString = new UnitFactory(this, Faction.valueOf(unit[5]), unit[4].split(","));
                unitString.setAI(Integer.parseInt(unit[6].split(":")[0]), Integer.parseInt(unit[6].split(":")[1]));
                unitString.setMoveset(unit[7]);
                unitString.addInventory(unit[11].split(","));
                unitString.setEquipment(unit[8].split(","), unit[9].split(","), unit[10].split(","));
                traits.load(this, unit[12]);
            }
        };
        if (!unit[2].isEmpty()) product.clip.setImage(unit[2]);
        if (!unit[1].isEmpty()) product.setFace(unit[1]);
        else product.setFace("Man");
        product.speechType = unit[3];
        product.subId = id;
        return product;
    }},
    Etc{ Prop make(Map map, int id, int seed){

        String[] data = FileManager.readText("Data/raw/Etc.txt");
        final String[] v = data[id].split(";");
        Item product = new Item(map, v[0], seed, Item.Material.load(v[4].split(",")), Item.ItemType.Swinging){
            public String getDescription(){
                String r = super.getDescription();
                if (v.length > 5) r += v[5];
                return r;
            }
        };
        product.weight = Integer.parseInt(v[2]);
        product.value = Integer.parseInt(v[3]);
        product.dir = product.seed % 2 == 0 ? 1 : -1;
        product.image.setCenterType(Integer.parseInt(v[1]));
        product.subId = id;
        return product;
    }},
    Weapon{ Prop make(Map map, int id, int seed){
        String[] data = FileManager.readText("Data/raw/Weapon.txt");
        final String[] v = data[id].split(";");
        Item product = new Weapon(map, v[0], seed, Item.Material.load(v[3].split(",")), Item.ItemType.valueOf(v[1]));
        int range = Integer.parseInt(v[2]);
        if (range != -1) product.range = range;
        product.subId = id;
        return product;
    }},
    Armor{ Prop make(Map map, int id, int seed){
        String[] data = FileManager.readText("Data/raw/Armor.txt");
        final String[] v = data[id].split(";");
        int piece = Integer.parseInt(v[1]);
        if (piece == 1 || piece == 2) v[0] = v[0].substring(0,v[0].lastIndexOf("_"));
        Prop product = new Armor(map, v[0], seed, Item.Material.load(v[4].split(",")), piece,
                Integer.parseInt(v[2]), Integer.parseInt(v[3]), Integer.parseInt(v[5]), Integer.parseInt(v[6]));
        product.subId = id;
        return product;
    }},
    Food{ Prop make(Map map, int id, int seed){
        String[] data = FileManager.readText("Data/raw/Food.txt");
        final String[] v = data[id].split(";");
        Prop product = new Food(map, v[0], seed, Integer.parseInt(v[1]), v[2]);
        product.subId = id;
        return product;
    }},
    Player{ Prop make(Map map, int id, int seed){
        if (id == 1) return new MainPlayer(map, seed);
        return new Player(map, seed);
    }},
    Dog{ Prop make(Map map, int id, int seed) {
        Unit product = new Unit(map, name(), seed){
            public float getReach(){
                return 240;
            }
            public void stateSet(Actions state){
                super.stateSet(state);
                if (state == Actions.Melee){
                    delay.time = 24;
                    vel.x = 32*dir;
                    vel.y -= 20;
                }
            }
            public Unit attack(){
                ArrayList<Unit> t = map.updater.collideUnit(pos,48,faction);
                if (t.size() > 0){
                    Unit i = t.get(0);
                    i.hit(new Line(pos.getSubtract(vel), pos.getAdd(vel)), getDamage());
                    vel.x *= -.7f;
                    stateSet(Actions.Idle);
                    return i;
                }
                return null;
            }
            public void stateUpdate(){
                super.stateUpdate();
                if (state == Actions.Melee) attack();
            }
            public void die(String p){
                Stock.createByName(map, "Meat", -1).start().pos.set(pos);
                super.die(p);
            }
            public void onCreate(){
                UnitFactory unitString = new UnitFactory(this, Faction.Predator, new String[]{3+"",2+"",1+""});
                unitString.setMoveset("Melee");
            }
        };
        product.clip = new Body(product, name());
        return product;
    }},
    Slime{ Prop make(Map map, int id, int seed) {
        Unit product = new Unit(map, name(), seed){
            public void stateSet(Actions state) {
                super.stateSet(state);
                if (state == Actions.Melee) {
                    delay.time = 60;
                }
            }
            public void moveTriggered(int p){
                super.moveTriggered(p);
                if (state == Actions.Melee){
                    Particle.explode(map, "Poison",getDepth(),pos,new P(),8,8,0,false,4);
                    ArrayList<Unit> t = map.updater.collideUnit(pos, (int) getReach(), faction);
                    for (Unit i : t){
                        i.hit(getFaceLine(), getDamage());
                        i.buff(Buffs.Poison, 1);
                        i.vel.x = (i.pos.x > i.pos.x ? -1 : 1)*6;
                        i.vel.y -= 16;
                        Sound.play(map, pos, "drink");
                    }
                }
            }
            public void buff(Buffs buff, int level){
                if (buff == Buffs.Bleed || buff == Buffs.Poison) return;
                super.buff(buff, level);
            }
            public void onCreate(){
                UnitFactory unit = new UnitFactory(this, Faction.Predator, new String[]{1+"",1+"",1+""});
                unit.setStat("Speed", 8);
                unit.setMoveset("Melee");
            }
            public void die(String p){
                Stock.createByName(map, "Resin", -1).start().pos.set(pos);
                super.die(p);
            }
        };
        product.clip = new Body(product, name());
        return product;
    }},
    Bird{ Prop make(Map map, int id, int seed) {
        Unit product = new Unit(map, name(), seed){
            public void collide(){
                super.collide();
                grounded = true;
            }
            public float getReach(){
                return 200;
            }
            public void stateSet(Actions state) {
                if (SyncRand.r.nextBoolean()) state = Actions.Jump;
                if (state == Actions.Jump){
                    vel.y = -Settings.gravity*8;
                    vel.x += dir * 4;
                    delay.time = 4;
                    buff(Buffs.Slowfall, 3);
                    get("Strength").heal();
                } else {
                    if (state == Actions.Melee) state = Actions.Shock;
                    super.stateSet(state);
                }
            }
            public void buff(Buffs buff, int level){
                super.buff(buff, level*2);
            }
            public void die(String p){
                Stock.createByName(map, "Birdmeat", -1).start().pos.set(pos);
                super.die(p);
            }
            public void onCreate(){
                UnitFactory unitString = new UnitFactory(this, Faction.Predator, new String[]{3+"",5+"",3+""});
                unitString.setMoveset("Melee");
            }
        };
        product.clip = new Body(product, name());
        product.clip.organs[0].setSize((int) product.clip.organs[0].getImage().getSize().x);
        return product;
    }},
    Beholder{ Prop make(Map map, int id, int seed) {
        Unit product = new Unit(map, name(), seed){
            public void collide(){
                super.collide();
                grounded = true;
            }
            public P getHand(){
                return pos.getAdd(0,getReach()+40);
            }
            public void stateSet(Actions state) {
                super.stateSet(state);
            }
            public void update(){
                super.update();
                vel.y += .1f;
                if (state != Actions.Dead) {
                    for (Line i : map.land) {
                        if (i.lineCross(new Line(pos, pos.getAdd(0, 200)))) {
                            vel.y -= .16f;
                            break;
                        }
                    }
                }
            }
            public void onCreate(){
                String moves;
                if (seed % 2 == 1) moves = "Meteorform,Fireball";
                else moves = "Fireblast";
                UnitFactory unit = new UnitFactory(this, Faction.Predator, new String[]{3+"",4+"",9+""});
                unit.setStat("Intelligence", 20);
                traits.learn(this, Traits.Trait.Attunement);
                for (int i=0;i<8;i++) traits.learn(this, Traits.Trait.Pyromancy);
                unit.setMoveset(moves);
            }
        };
        product.gravity = false;
        product.clip = new Body(product, name());
        return product;
    }},
    Wyvern{ Prop make(Map map, int id, int seed) {
        Unit product = new Unit(map, name(), seed){
            public void onCreate(){
                UnitFactory unitString = new UnitFactory(this, Faction.Predator, new String[]{9+"",9+"",9+""});
                unitString.setStat("Agility",80);
                unitString.setMoveset("Melee");
            }
        };
        product.gravity = false;
        product.clip = new Body(product, name());
        return product;
    }},
    Crawler{ Prop make(Map map, int id, int seed) {
        Unit product = new Unit(map, name(), seed){
            public void onCreate(){
                UnitFactory unitString = new UnitFactory(this, Faction.Predator, new String[]{7+"",10+"",2+""});
                unitString.setMoveset("Melee");
                unitString.setMoveset("Backstep");
            }
            public float getReach(){
                return 280;
            }
            public void moveTriggered(int p){
                super.moveTriggered(p);
                if (state == Actions.Melee){
                    P tip = getHead();
                    final Projectile pj = new Projectile(map, "Bullet", tip, tip.getAdd(target), 60) {
                        public void stuck() {
                            Particle.explode(map, "Fire", getDepth(), tip, vel, 4, 12, 20, false, 3);
                            remove();
                        }
                        public void onAttack(Unit p){
                            super.onAttack(p);
                            p.setState(Actions.Hit);
                        }
                    };
                    pj.delay = new EventTimer(9, false, new Performer() {
                        public void perform() {
                            pj.remove();
                        }
                    });
                    pj.dir = dir;
                    pj.rotSpeed = 0;
                    pj.blunt = false;
                    pj.rotateTo(target.getSubtract(tip).add(getHand()));
                    pj.faction = faction;
                    pj.damage = 5;
                    Particle.smoke(map, pj.pos, pj.getDepth());
                    Particle.explode(map, "Fire", pj.getDepth(), tip, pj.vel, 3, 12, 20, false, 3);
                }
            }
            public void die(String p){
                Stock.createByName(map, "Ammunition", 0).start().pos.set(pos);
                super.die(p);
            }
        };
        product.clip = new Body(product, name());
        return product;
    }},
    Fist{ Prop make(Map map, int id, int seed){
        Item product = new Weapon(map, name(),0, new Item.Material[]{Item.Material.Organic}, Item.ItemType.Throwing){
            public boolean use(final com.ga.unit.man.Man host) {
                host.setState(Actions.Interact);
                return true;
            }
            public String getFullName(){
                return "Fist";
            }
            public String forceToString() {
                return "";
            }
            public void drop(com.ga.unit.man.Man host) {}
            public void onUnequip(com.ga.unit.man.Man host){}
            public void onCreate(){}
            public void remove(){}
            public int getDamage() {
                return 1;
            }
        };
        product.range = 48;
        product.image.setCenterType(3);
        product.onInteract = null;
        product.save = false;
        return product;
    }},
    Moneybag{ Prop make(Map map, int id, int seed){
        Item product = new Item(map, name(),0, new Item.Material[]{Item.Material.Gold}, Item.ItemType.Bashing, 100000){
            public boolean use(final com.ga.unit.man.Man host) {
                int gold = host.getGold();
                if (gold > uses){
                    host.requireGold(gold);
                    host.obtainGold(gold);
                    return true;
                }
                return false;
            }
            public void onCreate(){
                isUsable(true, 100);
            }
            public int getValue(){
                return uses;
            }
            public String getDescription(){
                return super.getDescription()+"This is your standard currency. Contains "+Dialog.money(uses)+".";
            }
        };
        product.tags.add(Tag.Currency);
        return product;
    }},
    Manual{ Prop make(Map map, int id, int seed){
        Item product = new Item(map, name(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Scroll, 1){
            private Actions skill;
            public void onCreate(){
                super.onCreate();
                ArrayList<Actions> learnables = Actions.getLearnables();
                skill = learnables.get(seed % learnables.size());
            }
            public String[] load(String p){
                String[] r = super.load(p);
                ArrayList<Actions> learnables = Actions.getLearnables();
                skill = learnables.get(seed % learnables.size());
                return r;
            }
            private boolean checkIngredients(Unit host){
                ArrayList<Prop> t = map.updater.collideProp(pos, 240);
                Tags r = new Tags();
                r.addAll(skill.getTrainers());
                for (Prop i : t) {
                    if (r.removeAll(i.tags)){
                        i.remove();
                    }
                }
                r.addAll(host.getTags());
                return r.size() == 0;
            }
            @Override
            public boolean use(com.ga.unit.man.Man host){
                if (host instanceof Player && super.use(host)) {
                    Player player = (Player) host;
                    if (player.getStateLevel(skill) > 0) {
                        if (checkIngredients(host)) {
                            if (host.get("Motivation").require(10)) {
                                if (map.updater.sleep(1, false)) {
                                    if (!player.learnSkill(skill)) host.say("Failed to learn " + skill.name() + ".");
                                }
                            } else host.say("Need at least 10 Motivation...");
                        } else host.say("I lack the catalysts required to learn this action!");
                    } else host.say("Not skilled enough to learn "+skill.name()+"!");
                }
                return false;
            }
            public String getName(){
                return super.getName()+" of "+skill.name();
            }
            public String getDescription(){
                int difficulty = skill.getDifficulty();
                int power = Global.player.getStateLevel(skill);
                StringBuilder req = new StringBuilder("Fields of study: ");
                for (Tag i : skill.getTrainers()) req.append("[").append(i.name()).append("] ");
                req.append("x Difficulty ").append(difficulty).append(".");
                req.append(" Your current level of expertise in this field: ").append(power).append(".");
                if (power > 0) req.append(" You are capable of performing this action.");
                else req.append(" You are incapable of performing this action.");
                return super.getDescription()+" "+req;
            }
        };
        product.value = 94;
        return product;
    }},
    Textbook{ Prop make(Map map, final int id, int seed){
        Item product = new Item(map, name(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Scroll, -1){
            private Traits.Trait trait = Traits.Trait.getByName(Tags.trainables[seed % Tags.trainables.length].name());
            public void onCreate(){
                super.onCreate();
                if (id == -1) subId = rand.nextInt(4)+1;
            }
            @Override
            public boolean use(com.ga.unit.man.Man host){
                if (host instanceof MainPlayer && super.use(host)) {
                    int level = Global.player.traits.getTraitLevel(trait);
                    if (level == subId) {
                        if (host.traits.canLearn(trait) && map.updater.sleep(subId+1, true)) {
                            if (host.learnTrait(trait)){
                                host.say("Trained up "+trait.name()+"!");
                                return true;
                            }
                        }
                    } else {
                        if (level < subId) host.say("This textbook is beyond my understanding!");
                        else host.say("This textbook is too easy!");
                    }
                }
                return false;
            }
            public String getName(){
                String prefix = "Expert";
                if (subId == 0) prefix = "Novice";
                else if (subId == 1) prefix = "Intermediate";
                else if (subId == 2) prefix = "Advanced";
                return prefix + " " + super.getName();
            }
            public String getDescription(){
                String req = " This book contains information for those with understanding of "+trait.name()+" level "+(subId+1)+".";
                int understanding = Global.player.traits.getTraitLevel(trait);
                req += " Your current "+trait.name()+" level: "+understanding+".";
                if (understanding == subId) req += " You may learn from the contents of this book.";
                else if (understanding < subId) req += " This book is beyond your understanding.";
                else req += " You won't learn anything from this book.";
                return super.getDescription()+req+"\nTakes "+(subId+1)+" hours to read.";
            }
        };
        product.value = 9400;
        return product;
    }},
    Bandage{ Prop make(Map map, int id, int seed){
        return new Item(map, name(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Consumable, 4) {
            public boolean use(com.ga.unit.man.Man host) {
                if (super.use(host) && isUsable(true, 1)){
                    host.stats.triggerEffect(Buffs.Bleed);
                    return true;
                }
                return false;
            }
            public String getDescription(){
                return super.getDescription()+" Use to stop bleeding.";
            }
        };
    }},
    Potion{ Prop make(Map map, int id, int seed){
        Item product = new Item(map, name(), seed, new Item.Material[]{Item.Material.Glass}, Item.ItemType.Consumable, 1) {
            public void onCreate() {
                if (seed != 0) imbueMeta(false, false, 1);
            }
            public boolean use(com.ga.unit.man.Man host) {
                if (super.use(host) && isUsable(true, 1)){
                    host.buff(emod, ademod.level*2);
                    return true;
                }
                return false;
            }
            public String getFullName(){
                if (!Global.player.traits.has(Traits.Trait.Alchemy)) return getName();
                return super.getFullName();
            }
            public String[] load(String p){
                String[] r = super.load(p);
                value = 100 + emod.value*ademod.level*100;
                return r;
            }
        };
        product.range = 600;
        return product;
    }},
    Scroll{ Prop make(Map map, int id, int seed){
        Item product = new Item(map, name(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Scroll, 1){
            Actions spell;
            @Override
            public boolean use(com.ga.unit.man.Man host){
                if (super.use(host) && isUsable(true, 1)) {
                    if (spell != null){
                        host.buff(Buffs.Attunement, 1);
                        host.stateLevel = 3;
                        host.stateSet(spell);
                        return true;
                    }
                }
                return false;
            }
            private void makeSpell(){
                Tags physical = new Tags();
                physical.add(Tag.Strength);
                physical.add(Tag.Athletics);
                ArrayList<Actions> list = Actions.getLearnables();
                list.removeIf(e -> e.getTags().containsAny(physical));
                spell = list.get(rand.nextInt(list.size()));
                value = 200 + 200*(spell.getDifficulty() * spell.getTrainers().size());
            }
            public void onCreate(){
                makeSpell();
            }
            public String[] load(String p){
                String[] r = super.load(p);
                makeSpell();
                return r;
            }
            public String getName(){
                if (!Global.player.traits.has(Traits.Trait.Rune_Literacy)) return super.getName();
                return super.getName()+" of "+spell.name();
            }
        };
        product.probability = 1;
        product.range = 400;
        return product;
    }},
    Ethersword{ Prop make(Map map, int id, int seed){
        Weapon product = new Weapon(map, name(), seed, new Item.Material[]{Item.Material.Crystal}, Item.ItemType.Sword){
            public void remove(){
                super.remove();
                Particle.holyExplosion(map, pos,getDepth());
            }
            public boolean trigger(Unit host) {
                Particle.holyExplosion(map, pos,getDepth());
                return super.trigger(host);
            }
            public void onUnequip(Man host) {
                super.onUnequip(host);
                remove();
            }
            public int getDamage(){
                return 6;
            }
        };
        product.disposable = true;
        product.weight = 0;
        return product;
    }},
    Booth{ Prop make(Map map, int id, int seed){
        Prop product = new Prop(map, name(), seed);
        product.onInteract = new Interactive(){
            public void interact(com.ga.unit.man.Man p){
                String t = p.getConversation();
                if (t.toLowerCase().startsWith("call")) {
                    Global.server.startClient(p.getConversation().substring(5));
                    p.say("Connecting...");
                } else if (t.equals("")) {
                    Global.server.startServer();
                    p.say("Waiting for a call...");
                }
                p.setState(Actions.Hit);
            }
        };
        return product;
    }},
    Map{ Prop make(Map map, int id, int seed){ //unused
        Prop product = new Prop(map, name(), seed);
        product.onInteract = new Interactive(){
            public void interact(com.ga.unit.man.Man p){
            }
        };
        return product;
    }},
    Clockwork{ Prop make(Map map, int id, final int seed){ //unused
        return new Prop(map, name(), seed){
            private float revolve = (seed % 100 - 50) * .01f;
            public void update(){
                super.update();
                rotation += revolve;
            }
        };
    }},
    Spawner{ Prop make(Map map, final int id, int seed) {
        Prop product = new Prop(map, name(), seed){
            public String[] load(String p){
                String[] r = super.load(p);
                name = r[5];
                return r;
            }
            public String toString(){
                return super.toString() + "@" + name;
            }
            public void onField(){
                timeUpdate();
            }
            public void onTimeChange(){
                timeUpdate();
            }
            private void timeUpdate(){
                if (!Global.editor && (Global.hour >= rotation)) {
                    int count = 0;
                    for (Unit unit : map.updater.unitsLoaded){
                        if (unit.name.equals(name)) count++;
                    }
                    for (Prop prop : map.updater.props){
                        if (prop.factoryId != factoryId && prop.name.equals(name)) count++;
                    }
                    if (count < id){
                        for (int i=0;i<Math.min(id-count, ((Global.hour-rotation)/seed)+1);i++) {
                            Stock.createByName(map, name, -1).start().pos.set(pos);
                        }
                    }
                    rotation = Global.hour + seed;
                }
            }
        };
        product.mobile = false;
        product.image.visible = false;
        return product;
    }},
    Container { Prop make(final Map map, int id, final int seed){
        String[] data = FileManager.readText("Data/raw/Loot.txt");
        String[] div = data[Math.max(0,id)].split(":");
        final Prop product = new Prop(map, div[0], seed){
            public void onField(){
                super.onField();
                P s = image.getSize().getMult(.5f, 1).add(0,-8);
                P top = new P(Global.sin(rotation)*s.y, Global.cos(rotation)*s.y);
                P width = new P(Global.sin(rotation)*s.x, Global.cos(rotation)*s.x);
                map.land.add(new Line(pos.x+top.x-width.y, pos.y-top.y-width.x,
                        pos.x+top.x+width.y, pos.y-top.y+width.x, -1, 0, lastLand.depth - OFFSET_PROP){
                    public String toString(){
                        return "";
                    }
                });
            }
            public P getInteractPoint(){
                return pos.getAdd(0, -image.getSize().y/2);
            }
        };
        product.onInteract = new Interactive(){
            public void interact(com.ga.unit.man.Man p){
                if (seed > 0) {
                    boolean openable = div[1].equals("");
                    if (!openable){
                        L<Prop> items = p.getAvailableItems();
                        items.removeIf(e -> !e.name.equals(div[1]));
                        Prop item = items.getFirst();
                        openable = item != null && ((Item)item).isUsable(true, 1);
                    }
                    if (openable) {
                        String[] v = div[2].split(",");
                        String lootName = v[seed % v.length];
                        Global.player.setWeapon((Item) createByName(map, lootName, -1).start());
                        Global.player.say("Found " + Global.player.weapon.getFullName() + "!");
                        product.seed = 0;
                        product.image.modify(Shader.Tint, new Color(160,0,0,255).getRGB());
                    }
                }
            }
        };
        if (seed == 0){
            product.image.modify(Shader.Tint, new Color(160,0,0,255).getRGB());
        }
        product.image.setCenterType(2);
        return product;
    }},
    Safe{ Prop make(Map map, int id, int seed){
        final Prop product = new Prop(map, name(), seed){
            String store;
            public P getInteractPoint(){
                return pos.getAdd(0, -image.getSize().y/2);
            }
            public void onField(){
                super.onField();
                P s = image.getSize().getMult(.5f, 1).add(0,-8);
                P top = new P(Global.sin(rotation)*s.y, Global.cos(rotation)*s.y);
                P width = new P(Global.sin(rotation)*s.x, Global.cos(rotation)*s.x);
                map.land.add(new Line(pos.x+top.x-width.y, pos.y-top.y-width.x,
                        pos.x+top.x+width.y, pos.y-top.y+width.x, -1, 0, lastLand.depth - OFFSET_PROP){
                    public String toString(){
                        return "";
                    }
                });
                onInteract = new Interactive(){
                    public void interact(com.ga.unit.man.Man p){
                        if (p == Global.player){
                            if (subId < Global.hour) {
                                Menu.menu.showDialog("Safe", "How long would you like to use this storage? " + Dialog.money(100) + " per hour." +
                                                "\nWARNING - The safe is not responsible for any items stolen after hours are over.",
                                        new String[]{"1 hour", "1 day (6 hours)", "1 week (42 hours)", "1 month (180 hours)"}, new Performer[]{
                                            new Performer() {
                                                public void perform() {
                                                    if (p.requireGold(100)) subId = 1 + Global.hour;
                                                }
                                            }, new Performer() {
                                                public void perform() {
                                                    if (p.requireGold(600)) subId = 6 + Global.hour;
                                                }
                                            }, new Performer() {
                                                public void perform() {
                                                    if (p.requireGold(4200)) subId = 42 + Global.hour;
                                                }
                                            }, new Performer() {
                                            public void perform() {
                                                    if (p.requireGold(18000)) subId = 180 + Global.hour;
                                            }
                                        }
                                        });
                            } else if (store == null) {
                                L<Item> items = Global.player.inventory.getItems();
                                String[] itemList = new String[items.size()];
                                Performer[] perf = new Performer[itemList.length];
                                for (int i = 0; i < perf.length; i++) {
                                    Item item = items.get(i);
                                    itemList[i] = "Store " + item.getFullName();
                                    perf[i] = new Performer() {
                                        public void perform() {
                                            Global.player.say("Stored "+item.getFullName()+"!");
                                            store = item.forceToString();
                                            Global.player.dropItem(item);
                                            item.remove();
                                        }
                                    };
                                }
                                Menu.menu.showDialog("Store Item","Select an item to store for "+(subId-Global.hour)+" hours.", itemList, perf);
                            } else {
                                Global.player.setWeapon((Item) Stock.get(map, store).start());
                                Global.player.say("Reclaimed "+Global.player.weapon.getFullName()+"!");
                                store = null;
                            }
                        }
                    }
                };
            }
            public String[] load(String p){
                String[] storage = p.split("#");
                String[] r = super.load(storage[0]);
                if (storage.length > 1) store = storage[1];
                return r;
            }
            public String toString(){
                String extra = store==null?"":"#" + store;
                return super.toString() + extra;
            }
        };
        product.image.setCenterType(2);
        return product;
    }},
    Scrap{ Prop make(Map map, int id, int seed){
        Item product = new Item(map,name(),seed,new Item.Material[]{Item.Material.values()[seed % Item.Material.values().length]},Item.ItemType.Throwing, 1){
            public String getName(){
                return super.getName() + " " + getMaterial().name();
            }
        };
        product.disposable = false;
        return product;
    }},
    Repairkit{ Prop make(Map map, int id, int seed){
        int level = (seed % 9)+1;
        Item product = new Item(map, name(),seed,new Item.Material[]{Item.Material.values()[seed % Item.Material.values().length]},Item.ItemType.Scroll,20*level){
            public boolean use(Man host){
                if (host == Global.player && super.use(host)) {
                    final Item repairkit = this;
                    L<Prop> items = Global.player.getAvailableItems();
                    items.addAll(host.armor.armor);
                    items.removeIf(o -> (!(o instanceof Armor) && !(o instanceof Weapon)) || ((Item) o).isInfinite() || ((Item) o).getMaterial() != getMaterial() || o == this);
                    String[] itemList = new String[items.size()];
                    Performer[] perf = new Performer[itemList.length];
                    for (int i = 0; i < itemList.length; i++) {
                        Item item = (Item) items.get(i);
                        itemList[i] = "Repair " + item.getFullName();
                        perf[i] = new Performer() {
                            public void perform() {
                                if (isUsable(false, 1) && cooldown.isReady() && host.map.updater.sleep(1, true)) {
                                    cooldown.reset();
                                    int fixAmount = Math.min(item.durability - item.uses, repairkit.uses);
                                    item.isUsable(true, -fixAmount);
                                    repairkit.isUsable(true, fixAmount);
                                    host.armor.refresh();
                                }
                            }
                        };
                    }
                    Menu.menu.showDialog("Use Repairkit", "Select a weapon or an armor to repair", itemList, perf);
                }
                return true;
            }
            public String getName(){
                return getMaterial().name() + " " + super.getName();
            }
            public String getDescription(){
                return super.getDescription()+"Use this trinket to repair items made of same material. Takes 1 hour.";
            }
        };
        product.disposable = false;
        return product;
    }},
    Bow{ Prop make(Map map, int id, int seed){
        Item product = new Weapon(map, name(), seed, new Item.Material[]{Item.Material.Wooden, Item.Material.Bone, Item.Material.Crystal, Item.Material.Titanium}, Item.ItemType.Shoot){
            public String getDescription(){
                return super.getDescription() + " The projectile from this weapon deals "+(3+getDamage())+" extra damage.";
            }
            public boolean use(final com.ga.unit.man.Man host){
                if (super.use(host) && isUsable(true, 1)) {
                    host.clip.setState("Shoot_Arrow");
                    host.delay.time = Math.max(0, 70-host.getAttackSpeed());
                    host.weapon.rotateTo(host.target.getMult(host.dir, host.dir));
                    host.delay.performer = new Performer() {
                        public void perform() {
                            P tip = host.weapon.getLine().ptd;
                            final Item wpn = host.weapon;
                            int str = host.get("Strength").getMax();
                            int speed = (int) (40 + Global.clamp(str * 12 + 20,48,120) - (SyncRand.r.nextFloat() * (44 / str)));
                            final Projectile pj = new Projectile(host.map, "Arrow", tip, host.getHand().getAdd(host.target), speed) {
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
                            pj.vel.y -= 4;
                            pj.setDepth(300);
                            pj.blunt = false;
                            pj.lastLand = host.lastLand;
                            pj.rotSpeed = 0;
                            pj.rotation = pj.vel.getAtan();
                            pj.gravity = true;
                            pj.faction = host.faction;
                            pj.damage = 3+getDamage();
                            host.weapon.rotation += host.dir * .3f;
                            host.setState(Actions.Idle);
                        }
                    };
                    return true;
                }
                return false;
            }
        };
        product.weight = 1;
        product.range = 500;
        return product;
    }},
    Firearm{ Prop make(Map map, int id, int seed){
        String name, clip;
        int clipRange, shootSpeed, ammoDamage, magazine;
        if (seed % 5 == 0){
            name = "Machine_Gun";
            clip = "Shoot_Rifle";
            clipRange = 3;
            shootSpeed = 18;
            ammoDamage = 1;
            magazine = 30;
        } else if (seed % 4 == 0){
            name = "Pistol";
            clip = "Shoot_Handgun";
            clipRange = 4;
            shootSpeed = 12;
            ammoDamage = 4;
            magazine = 12;
        } else if (seed % 3 == 0){
            name = "Rifle";
            clip = "Shoot_Rifle";
            clipRange = 10;
            shootSpeed = -20;
            ammoDamage = 14;
            magazine = 1;
        } else if (seed % 2 == 0){
            name = "Shotgun";
            clip = "Shoot_Rifle";
            clipRange = 3;
            shootSpeed = 0;
            ammoDamage = 20;
            magazine = 2;
        } else {
            name = "Revolver";
            clip = "Shoot_Handgun";
            clipRange = 5;
            shootSpeed = -7;
            ammoDamage = 6;
            magazine = 6;
        }
        Item product = new Weapon(map, name, seed, new Item.Material[]{Item.Material.Bronze}, Item.ItemType.Shoot){
            public String getDescription(){
                return super.getDescription() + " The projectile from this weapon deals "+ammoDamage+" extra damage.";
            }
            public boolean use(final com.ga.unit.man.Man host) {
                if (super.use(host)) {
                    Item thisItem = this;
                    if (isUsable(true, 1)) {
                        host.clip.setState(clip);
                        host.delay.time = Math.max(0, 27 - shootSpeed);
                        host.weapon.rotateTo(host.target.getMult(host.dir, host.dir));
                        host.weapon.rotation += .1 * host.dir;
                        host.delay.performer = new Performer() {
                            public void perform() {
                                host.vel.x = host.dir * -1;
                                P tip = host.weapon.getLine().ptd;
                                final Projectile pj = new Projectile(host.map, "Bullet", tip, host.getHand().getAdd(host.target), (int) (40 + (thisItem.rand.nextFloat() * 40))) {
                                    public void stuck() {
                                        Particle.explode(host.map, "Fire", getDepth(), tip, vel, 4, 12, 20, false, 3);
                                        remove();
                                    }
                                    public void onAttack(Unit p){
                                        super.onAttack(p);
                                        p.setState(Actions.Hit);
                                        thisItem.trigger(p);
                                    }
                                };
                                pj.delay = new EventTimer(clipRange, false, new Performer() {
                                    public void perform() {
                                        pj.remove();
                                    }
                                });
                                pj.dir = host.dir;
                                pj.rotSpeed = 0;
                                pj.blunt = false;
                                pj.rotateTo(host.target.getSubtract(tip).add(host.getHand()));
                                pj.faction = host.faction;
                                pj.damage = getDamage()+ammoDamage;
                                Particle.smoke(host.map, pj.pos, pj.getDepth());
                                Particle.explode(host.map, "Fire", pj.getDepth(), tip, pj.vel, 3, 12, 20, false, 3);
                                float start = host.rand.nextFloat();
                                pj.pos.add(pj.vel.getMult(start, start));
                                host.weapon.rotation += host.dir * -.3f;
                                host.setState(Actions.Idle);
                            }
                        };
                        return true;
                    } else {
                        L<Prop> items = host.getAvailableItems();
                        items.removeIf(e -> !(e instanceof Item) || !e.name.equals("Ammunition") || ((Item)e).uses == 0);
                        Item item = (Item) items.getFirst();
                        if (item != null && item.isUsable(false, 1)) {
                            int ammos = Math.min(item.uses, magazine);
                            item.isUsable(true, ammos);
                            host.clip.setState("Wield");
                            host.delay.time = 80;
                            host.delay.performer = new Performer(){
                                public void perform(){
                                    thisItem.isUsable(true, -ammos);
                                    host.stateSet(Actions.Idle);
                                }
                            };
                        } else host.say("Out of ammo!");
                    }
                }
                return false;
            }
        };
        product.durability = magazine;
        product.uses = magazine;
        product.weight = 1;
        product.range = clipRange*60;
        return product;
    }},
    Throwing_Knife{ Prop make(Map map, int id, int seed){
        Item product = new Item(map, "Tanto", seed,
                new Item.Material[]{Item.Material.Steel, Item.Material.Bone, Item.Material.Copper, Item.Material.Iron, Item.Material.Titanium, Item.Material.Glass},
                Item.ItemType.Darting, 10){
            @Override
            public boolean use(final com.ga.unit.man.Man host){
                if (!host.clip.organs[BodyHumanoid.FIST].amputated && super.use(host) && isUsable(true, 1)) {
                    host.delay.performer = new Performer() {
                        public void perform() {
                            final Projectile p = new Projectile(map, "Tanto", host.pos.getAdd(host.clip.organs[BodyHumanoid.OFIST].ptd), host.pos.getAdd(host.target), 70) {
                                @Override
                                public void stuck() {
                                    if (tangible) Particle.explode(map, "Fire", getDepth(), pos, vel, 20, 10, 15, true, 3);
                                    super.stuck();
                                }
                            };
                            p.rotSpeed = 0;
                            p.faction = host.faction;
                            p.damage = getDamage();
                            p.gravity = false;
                            p.setDepth(host.getDepth());
                            p.delay.set(8);
                            p.delay.performer = new Performer() {
                                @Override
                                public void perform() {
                                    p.gravity = true;
                                }
                            };
                        }
                    };
                    return true;
                }
                return false;
            }
        };
        product.range = 500;
        product.image.setCenterType(3);
        product.value = 1600;
        return product;
    }},
    Bomb{ Prop make(Map map, int id, int seed){
        Item product = new Item(map, name(), seed, new Item.Material[]{Item.Material.Stone}, Item.ItemType.Throwing, 1) {
            @Override
            public boolean use(final com.ga.unit.man.Man host) {
                if (super.use(host) && isUsable(true, 1)) {
                    final int explosionDamage = rand.nextInt(36)+24;
                    host.delay.time = 10;
                    host.delay.performer = new Performer() {
                        public void perform() {
                            final Projectile p = new Projectile(map, getName(), host.pos.getAdd(host.clip.organs[BodyHumanoid.FIST].ptd),
                                    host.pos.getAdd(host.target), 48) {
                                @Override
                                public void stuck() {
                                    Particle.explosion(map, pos, getDepth());
                                    ArrayList<Unit> t = map.updater.collideUnit(pos,96,Faction.Destructive);
                                    for (Unit i : t){
                                        i.hit(i.getPosLine(), explosionDamage);
                                    }
                                    remove();
                                }

                                @Override
                                public void update() {
                                    super.update();
                                    Particle.explode(map, "Fire", getDepth(), pos, new P(0, -1), 6, 1, 4, true, 3);
                                }
                            };
                            p.faction = host.faction;
                            p.damage = 0;
                            p.gravity = true;
                            p.delay.set(50);
                            p.delay.performer = new Performer() {
                                public void perform() {
                                    p.stuck();
                                }
                            };
                            p.pos = pos;
                        }
                    };
                    return true;
                }
                return false;
            }
        };
        product.range = 200;
        product.value = 1000;
        return product;
    }},
    Grappler{ Prop make(Map map, int id, int seed){
        Item product = new Item(map, name(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Throwing, 5) {
            @Override
            public boolean use(final com.ga.unit.man.Man host) {
                if (super.use(host) && isUsable(true, 1)) {
                    host.stateSet(Actions.Dart);
                    final Item me = this;
                    host.delay.performer = new Performer() {
                        public void perform() {
                            final Projectile p = new Projectile(map, getName(), host.pos.getAdd(host.clip.organs[BodyHumanoid.FIST].ptd),
                                    host.pos.getAdd(host.target), 56) {
                                private BasicStroke stroke = new BasicStroke(4);
                                public void onAttack(Unit p){
                                    pos = p.pos;
                                }
                                public void stuck() {
                                    super.stuck();
                                    Particle.groundSlam(map, pos, getDepth());
                                    host.stateSet(Actions.Jump);
                                    host.delay.set(999);
                                    host.mobile = false;
                                    gravity = false;
                                    mobile = false;
                                }
                                public void update(){
                                    super.update();
                                    if (!tangible) {
                                        if (host.pos.getSubtract(pos).getDist() < 16){
                                            host.lastLand = new Line();
                                            host.pos.add(0,-24);
                                            host.stateSet(Actions.Jump);
                                            host.vel.set(0,0);
                                            host.mobile = true;
                                            remove();
                                            me.image.visible = true;
                                        } else host.pos.add(pos.getSubtract(host.pos).mult(.1f));
                                    }
                                }
                                public void draw(Graphics2D g){
                                    super.draw(g);
                                    g.setColor(Color.darkGray);
                                    g.setStroke(stroke);
                                    P from = host.getHand().add(Global.rel);
                                    g.drawLine(from.x(), from.y(), (int) (pos.x+Global.rel.x), (int) (pos.y+Global.rel.y));
                                }
                                public float getDepth(){
                                    return host.getDepth();
                                }
                            };
                            p.faction = host.faction;
                            p.gravity = true;
                            me.image.visible = false;
                        }
                    };
                    return true;
                }
                return false;
            }
        };
        product.range = 1200;
        product.value = 16;
        return product;
    }},
    Battery{ Prop make(Map map, int id, int seed){
        Item product = new Item(map, name(),seed,new Item.Material[]{Item.Material.Copper}
            ,Item.ItemType.Consumable,10){
            public String getDescription(){
                return super.getDescription()+"\nThis item can store Energy for magical applications.";
            }
        };
        product.disposable = false;
        product.tags.add(Tag.Energy);
        return product;
    }},
    Alcohol{ Prop make(Map map, int id, int seed){
        String name = new String[]{"Beer","Cocktail","Mead","Rum","Grog"}[seed % 5];
        return new Item(map, name,seed,new Item.Material[]{Item.Material.Organic}
                ,Item.ItemType.Consumable,((seed % 5)*2)+1) {
            public String getDescription() {
                return super.getDescription() + "\nDrink to restore Energy into your body.";
            }
            @Override
            public boolean use(Man p) {
                if (super.use(p) && isUsable(true, 1)) {
                    p.get("Intelligence").heal();
                    return true;
                }
                return false;
            }
        };
    }},
    Kitchenware{ Prop make(Map map, final int id, int seed){
        String name = new String[]{"Butcher_Knife","Pot","Frying_Pan","Grill","Smoking_Rack","Drying_Rack","Spice"}[seed % 7];
        Food.Ingredient method = com.ga.unit.item.Food.Ingredient.getByType(2).get(seed % 7);
        Item product = new Item(map, name, seed, new Item.Material[]{Item.Material.Steel, Item.Material.Glass, Item.Material.Iron, Item.Material.Copper}, Item.ItemType.Bashing, 12){
            @Override
            public boolean use(com.ga.unit.man.Man host){
                if (host instanceof MainPlayer && super.use(host) && host.traits.has(Traits.Trait.Cooking) && isUsable(true, 1)) {
                    ArrayList<Prop> items = host.getAvailableItems();
                    ArrayList<Food> foods = new ArrayList<>();
                    for (Prop i : items){
                        if (i instanceof Food){
                            foods.add((com.ga.unit.item.Food) i);
                        }
                    }
                    if (foods.size() > 0){
                        Food prev = null;
                        for (Food i : foods){
                            i.cook(method);
                            Particle.smoke(map, i.pos, i.getDepth());
                            if (prev != null){
                                i.combine(prev, method);
                                i.name = String.format("%s and %s", i.name, prev.name);
                            }
                            prev = i;
                        }
                        prev.name = method.name() + " " + prev.name;
                        Sound.play(map, host.pos, "fire");
                        host.say("Cooking complete!");
                        return true;
                    }
                }
                return false;
            }
            public String getDescription(){
                return super.getDescription()+" This item can be used to cook food if you have the Cooking trait.";
            }
        };
        product.weight /= 2;
        product.value = 2000;
        return product;
    }},
    Vitamin{ Prop make(Map map, final int id, int seed){
        Item product = new Item(map, name(), seed, new Item.Material[]{Item.Material.Organic}, Item.ItemType.Consumable, 1){
            @Override
            public boolean use(com.ga.unit.man.Man host){
                if (host instanceof MainPlayer && super.use(host) && isUsable(true, 1)) {
                    host.get("Agility").add(1, false);
                    host.say("Restored 1 Vitality!");
                    return true;
                }
                return false;
            }
            public String getDescription(){
                return super.getDescription()+" This drug will restore Vitality by 1.";
            }
        };
        product.value = 2800;
        return product;
    }},
    Energy_Necklace{ Prop make(Map map, int id, int seed){
        int level = seed % 3 + 1;
        Armor product = new Armor(map, "Jewelry",seed,new Item.Material[]{Item.Material.Copper, Item.Material.Stone, Item.Material.Glass, Item.Material.Crystal, Item.Material.Obsidian}
                ,4, level-1,0, 0, 0){
            public String getFullName(){
                return "Level " + level + " " + super.getFullName();
            }
            public String getDescription(){
                return super.getDescription()+"\nThis item can store Energy for magical applications.";
            }
        };
        product.name = "Energy_Necklace";
        product.durability = level * level * 4;
        product.disposable = false;
        product.tags.add(Tag.Energy);
        return product;
    }},
    Power_Necklace{ Prop make(Map map, int id, int seed){
        int level = seed % 3 + 1;
        Armor product = new Armor(map, "Jewelry",seed,new Item.Material[]{Item.Material.Stone, Item.Material.Glass, Item.Material.Crystal, Item.Material.Obsidian}
                ,4, level, 0, 0, 0){
            public String getFullName(){
                return "Level " + level + " " + super.getFullName();
            }
            public boolean trigger(Unit host){
                if (cooldown.isReady()) {
                    if (isUsable(true, 1)) {
                        if (host == Global.player){
                            if (host.map.isSnowing()) {
                                if (host.map.updater.sleep(1, true)){
                                    Particle.sparkle(host.map, "Glare", host.getDepth(), host.getHand(), 24, 6);
                                    Global.player.rechargeMana(10);
                                    cooldown.reset();
                                    return true;
                                }
                            } else host.say("I need to be in freezing temperature to generate energy!");
                        }
                    }
                } else host.say("");
                return false;
            }
            public String getDescription(){
                return super.getDescription()+"\nThis item can store Energy for magical applications. This item can also be activated to generate energy in cold environments.";
            }
        };
        product.name = "Power_Necklace";
        product.durability = level * level * 4;
        product.disposable = false;
        product.tags.add(Tag.Energy);
        return product;
    }},
    Swordcollar{ Prop make(Map map, int id, int seed){
        int level = seed % 8 + 1;
        Armor product = new Armor(map, "Jewelry",seed,new Item.Material[]{Item.Material.Stone}
                ,4,1,0, 0, 0){
            public boolean trigger(Unit host){
                if (cooldown.isReady()) {
                    if (isUsable(true, 1)) {
                        Particle.sparkle(host.map, "Glare", host.getDepth(), host.getHand(), 24, 6);
                        cooldown.reset();
                        host.stateSet(Actions.Ethersword);
                        host.stateLevel = level;
                        return true;
                    }
                } else host.say("");
                return false;
            }
            public String getFullName(){
                return "Level "+level+" "+super.getFullName();
            }
            public String getDescription(){
                return super.getDescription()+"\nUsing this trinket will cast level "+level+" Ethersword.";
            }
        };
        product.name = "Swordcollar";
        product.disposable = false;
        return product;
    }},
    Memoryfoam_Necklace{ Prop make(Map map, int id, int seed){
        Armor product = new Armor(map, "Jewelry",seed,new Item.Material[]{Item.Material.Other}
                ,4,1,0,0, 0){
            private String location;
            public void onCreate(){
                imbueMeta(true, false, 0);
            }
            public boolean trigger(Unit host){
                if (host == Global.player && isUsable(true, 1) && cooldown.isReady()) {
                    cooldown.reset();
                    String[] location = this.location.split("&");
                    if (host.map.name.toString().equals(location[0])){
                        host.pos.add(new P(location[1]).getSubtract(host.pos));
                        Particle.holyExplosion(host.map, host.getHead(), host.getDepth());
                    } else {
                        Global.server.afterMapChange = new Performer() {
                            public void perform() {
                                Global.player.pos = new P(location[1]);
                                Particle.holyExplosion(host.map, host.getHead(), host.getDepth());
                            }
                        };
                        Global.server.requestChangeMap(location[0] + "@0");
                    }
                    return true;
                }
                return false;
            }
            public boolean use(com.ga.unit.man.Man host){
                if (super.use(host)){
                    location = host.map.name+"&"+host.pos.toString();
                    return true;
                }
                return false;
            }
            public String getDescription(){
                return super.getDescription()+"\nThis trinket will remember the location of where you had put it on. Activate to return to the location.";
            }
            public String[] load(String p){
                String[] r = super.load(p);
                location = r[8];
                return r;
            }
            public String toString(){
                return super.toString()+"@"+location;
            }
        };
        product.name = "Memoryfoam_Necklace";
        return product;
    }},
    Rogue_Cloak{ Prop make(Map map, int id, int seed){
        int level = (seed % 2)+1;
        Armor product = new Armor(map, name(),seed,new Item.Material[]{Item.Material.Silk}
                ,3,level*2-1,0, 0, 0){
            public String getFullName(){
                if (level == 1) return super.getFullName();
                else return "Master "+super.getFullName();
            }
        };
        for (int i=0;i<level;i++) product.tags.add(Tag.Athletics);
        return product;
    }},
    Rogue_Bracelet{ Prop make(Map map, int id, int seed){
        int level = (seed % 2)+1;
        Armor product = new Armor(map, name(),seed,new Item.Material[]{Item.Material.Silk}
                ,4,level*2-1,0,0, 0){
            public String getFullName(){
                if (level == 1) return super.getFullName();
                else return "Master "+super.getFullName();
            }
        };
        for (int i=0;i<level;i++) product.tags.add(Tag.Athletics);
        return product;
    }},
    Rogue_Dagger{ Prop make(Map map, int id, int seed){
        int level = (seed % 2)+1;
        Weapon product = new Weapon(map, "Knife", seed, new Item.Material[]{Item.Material.Obsidian}, Item.ItemType.Sword){
            public boolean trigger(Unit target){
                boolean r = super.trigger(target);
                for (Item item : owner.armor.armor) item.isUsable(true, -level);
                owner.armor.refresh();
                return r;
            }
            public String getFullName(){
                if (level == 1) return super.getFullName();
                else return "Master "+super.getFullName();
            }
        };
        product.name = name();
        return product;
    }},
    Jewel_Scarf{ Prop make(Map map, int id, int seed){
        Armor product = new Armor(map, "Jewelry",seed,new Item.Material[]{Item.Material.Silk},3,4,0, 10, 0);
        product.name = "Jewel_Scarf";
        product.tags.add(Tag.Channeling);
        product.tags.add(Tag.Conjuration);
        product.tags.add(Tag.Fire);
        product.tags.add(Tag.Enchant);
        product.tags.add(Tag.Curse);
        product.tags.add(Tag.Void);
        return product;
    }},
    Jewel_Bracelet{ Prop make(Map map, int id, int seed){
        Armor product = new Armor(map, "Jewelry",seed,new Item.Material[]{Item.Material.Gold},4,4,0, 0, 0);
        product.name = "Jewel_Bracelet";
        product.tags.add(Tag.Channeling);
        product.tags.add(Tag.Conjuration);
        product.tags.add(Tag.Fire);
        product.tags.add(Tag.Curse);
        product.tags.add(Tag.Air);
        product.tags.add(Tag.Void);
        return product;
    }},
    Jewel_Crown{ Prop make(Map map, int id, int seed){
        Armor product = new Armor(map, "Jewelry",seed,new Item.Material[]{Item.Material.Gold},0,4,0, 0, 0);
        product.name = "Jewel_Crown";
        product.tags.add(Tag.Channeling);
        product.tags.add(Tag.Conjuration);
        product.tags.add(Tag.Fire);
        product.tags.add(Tag.Curse);
        product.tags.add(Tag.Air);
        product.tags.add(Tag.Void);
        return product;
    }};
    private static HashMap<String, String> extRegistry = new HashMap<>();
    public static Prop get(com.ga.map.Map map, String p){
        if (p == null) return null;
        Prop t;
        String[] sections = p.split("@",-1);
        int subId = Integer.parseInt(sections[1]);
        int seed = Creation.getSeed(Integer.parseInt(sections[2]));
        int index = Integer.parseInt(sections[0]);
        t = values()[index].make(map, subId, seed);
        t.manualDepth = false;
        t.factoryId = index;
        t.subId = subId;
        t.load(p);
        return t;
    }
    public static Prop createByName(Map map, String p, int id, int seed){ // Return to reflection
        for (Stock i : values()){
            if (i.name().equals(p.replace(" ","_"))){
                return i.create(map, id, seed);
            }
        }
        return createByNameFromReg(map, p, seed);
    }
    public static Prop createByName(Map map, String p, int seed){ // Return to reflection
        return createByName(map, p, -1, seed);
    }
    public static Prop createByNameFromReg(Map map, String p, int seed){
        String name = extRegistry.get(p);
        if (name != null){
            String[] data = name.split("@");
            return Stock.valueOf(data[0]).create(map, Integer.parseInt(data[1]), seed);
        }
        assert false;
        return null;
    }
    public Prop create(Map map, int subId, int seed){
        Prop r = make(map, subId, Creation.getSeed(seed));
        r.factoryId = ordinal();
        r.subId = subId;
        r.onCreate();
        return r;
    }
    abstract Prop make(Map map, int subid, int seed);
    public static void init(){
        for (String raw : new String[]{"Object","Etc","Man","Beast","Weapon","Armor","Food"}){
            String[] data = FileManager.readText("Data/raw/"+raw+".txt");
            for (int i=0;i<data.length;i++)
                extRegistry.put(data[i].substring(0,data[i].indexOf(";")), raw+"@"+i);
        }
    }
}

class UnitFactory {
    Unit host;

    UnitFactory(Unit unit, Faction faction, String[] stats){
        host = unit;
        host.controlId = Global.getControlId();
        host.faction = faction;
        int str = Integer.parseInt(stats[0]), agi = Integer.parseInt(stats[1]), wis = Integer.parseInt(stats[2]);
        unit.stats.addStat("Strength", str);
        unit.stats.addStat("Agility", agi);
        unit.stats.addStat("Intelligence", wis);
        unit.stats.addStat("Level", str+agi+wis);
        unit.stats.addStat("Speed", 18+host.rand.nextInt(8));
        unit.stats.addStat("Attack", host.rand.nextInt(10)-8);
        setAI(2, 0);
    }
    void setEquipment(String[] weapon, String[] armor, String[] helms){
        Man man = (Man) host;
        if (weapon != null && weapon.length > 0) man.setWeapon((Item) Stock.createByName(host.map, weapon[host.seed % weapon.length], -1));
        man.armor = new ArmorSet(man, getItem(host.map, helms[host.seed % helms.length],0),
                getItem(host.map, armor[host.seed % armor.length], 1),
                getItem(host.map, armor[host.seed % armor.length], 2),
                getItem(host.map, armor[host.seed % armor.length], 3),
                getItem(host.map, armor[host.seed % armor.length], 4));
    }
    void addInventory(String[] items){
        Man man = (Man) host;
        man.inventory.setMaxSize(items.length);
        for (String item : items) {
            man.setWeapon((Item) Stock.createByName(host.map, item, -1));
            man.sheathe();
        }
    }
    private static String getItem(Map map, String p, int piece){
        if (p == null || p.equals("")) return "";
        String pieceName = "";
        if (piece == 1) pieceName = "_Pants";
        else if (piece == 2) pieceName = "_Torso";
        Prop product = Stock.createByName(map, p+pieceName,-1);
        if (product instanceof Item) {
            Item r = (Item) product;
            return r.toString();
        } else {
            Global.p("["+p+"] is not an Item");
            return "";
        }
    }
    void setStat(String stat, int value){
        host.get(stat).setTotal(value, false);
    }
    void setAI(int type, int subType){
        Intelligence.load(type+":"+subType+","+"1:0", host);
    }
    void setMoveset(String p){
        if (!p.isEmpty()) {
            String[] skills = p.split(",");
            StringBuilder r = new StringBuilder();
            for (String i : skills) {
                r.append(i).append(":0,");
            }
            Actions.load(host, r.substring(0, r.length() - 1));
        }
    }
}