package com.ga.unit;

import com.ga.game.Dialog;
import com.ga.game.FileManager;
import com.ga.game.Menu;
import com.ga.game.Settings;
import com.ga.game.Sound;
import com.ga.map.Creation;
import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.map.Picture;
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
import java.util.Random;

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
                UnitFactory unitString = new UnitFactory(this, Faction.valueOf(unit[3]), Integer.parseInt(unit[2]));
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
                UnitFactory unitString = new UnitFactory(this, Faction.valueOf(unit[5]), Integer.parseInt(unit[4]));
                unitString.setAI(Integer.parseInt(unit[6].split(":")[0]), Integer.parseInt(unit[6].split(":")[1]));
                unitString.setMoveset(unit[7]);
                unitString.setEquipment(unit[8].split(","), unit[9].split(","), unit[10].split(","));
                traits.load(this, unit[11]);
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
        Item product = new Item(map, v[0], seed, Item.Material.load(v[4].split(",")), Item.ItemType.Bashing, 100){
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
        Item product = new Weapon(map, v[0], seed, Item.Material.load(v[3].split(",")), Item.ItemType.valueOf(v[1]),
                40, Integer.parseInt(v[4]), Integer.parseInt(v[5]));
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
    Rover{ Prop make(Map map, int id, int seed) {
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
            public void die(){
                Stock.createByName(map, "Meat", -1).start().pos.set(pos);
                super.die();
            }
            public void onCreate(){
                UnitFactory unitString = new UnitFactory(this, Faction.Predator, 3);
                unitString.setMoveset("Melee");
            }
        };
        product.clip = new Body(product, name());
        return product;
    }},
    Slime { Prop make(Map map, int id, int seed) {
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
                UnitFactory unit = new UnitFactory(this, Faction.Predator, 1);
                unit.setStat("Speed", 8);
                unit.setMoveset("Melee");
            }
            public void die(){
                Stock.createByName(map, "Resin", -1).start().pos.set(pos);
                super.die();
            }
        };
        product.clip = new Body(product, name());
        product.clip.organs[0].setSize((int) (product.clip.organs[0].getImage().getSize().x * (product.rand.nextFloat()*.5f+.75f)));
        return product;
    }},
    Bird { Prop make(Map map, int id, int seed) {
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
            public void die(){
                Stock.createByName(map, "Birdmeat", -1).start().pos.set(pos);
                super.die();
            }
            public void onCreate(){
                UnitFactory unitString = new UnitFactory(this, Faction.Predator, 2);
                unitString.setMoveset("Melee");
            }
        };
        product.clip = new Body(product, name());
        product.clip.organs[0].setSize((int) product.clip.organs[0].getImage().getSize().x);
        return product;
    }},
    Beholder { Prop make(Map map, int id, int seed) {
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
                UnitFactory unit = new UnitFactory(this, Faction.Predator, 5);
                unit.setStat("Intelligence", 20);
                for (int i=0;i<8;i++) traits.learn(this, Traits.Trait.Attunement);
                for (int i=0;i<8;i++) traits.learn(this, Traits.Trait.Pyromancy);
                unit.setMoveset(moves);
            }
        };
        product.gravity = false;
        product.clip = new Body(product, name());
        return product;
    }},
    Wyvern { Prop make(Map map, int id, int seed) {
        Unit product = new Unit(map, name(), seed){
            public void onCreate(){
                UnitFactory unitString = new UnitFactory(this, Faction.Predator, 16);
                unitString.setStat("Agility",80);
                unitString.setMoveset("Melee");
            }
        };
        product.gravity = false;
        product.clip = new Body(product, name());
        return product;
    }},
    Wheeler { Prop make(Map map, int id, int seed) {
        Unit product = new Unit(map, name(), seed){
            public void onCreate(){
                UnitFactory unitString = new UnitFactory(this, Faction.Predator, 20);
                unitString.setStat("Agility",80);
                unitString.setMoveset("Shoot_Rifle");
            }
        };
        product.gravity = false;
        product.clip = new Body(product, name());
        return product;
    }},
    Fist{ Prop make(Map map, int id, int seed){
        Item product = new Weapon(map, name(),0, new Item.Material[]{Item.Material.Organic}, Item.ItemType.Bashing, -1, 0, 0){
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
    Goldbag{ Prop make(Map map, int id, int seed){
        Item product = new Item(map, name(),0, new Item.Material[]{Item.Material.Gold}, Item.ItemType.Bashing, 8000){
            public boolean use(final com.ga.unit.man.Man host) {
                host.setState(Actions.Interact);
                return true;
            }
            public void onCreate(){
                isUsable(true, 100);
            }
            public String getDescription(){
                return super.getDescription()+"This is your standard currency. Contains "+Dialog.money(getUses())+".";
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
            private boolean checkIngredients(){
                ArrayList<Prop> t = map.updater.collideProp(pos, 240);
                Tags r = new Tags();
                r.addAll(skill.getTrainers());
                for (Prop i : t) {
                    if (r.removeAll(i.tags)){
                        i.remove();
                    }
                }
                return r.size() == 0;
            }
            @Override
            public boolean use(com.ga.unit.man.Man host){
                if (host instanceof Player && super.use(host)) {
                    Player player = (Player) host;
                    if (player.getStateLevel(skill) > 0) {
                        if (checkIngredients()) {
                            if (host.get("Motivation").require(100)) {
                                if (map.updater.sleep(1, false)) {
                                    if (!player.learnSkill(skill)) host.say("Failed to learn " + skill.name() + ".");
                                }
                            } else host.say("No motivation...");
                        } else host.say("Catalysts required!");
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
                String req = "Fields of study: ";
                for (Tag i : skill.getTrainers()) req += "[" + i.name() + "] ";
                req += "x Difficulty "+difficulty+".";
                req += " Your current level of expertise in this field: "+power+".";
                if (power > 0) req += " You are capable of performing this action.";
                else req += " You are incapable of performing this action.";
                return super.getDescription()+" "+req;
            }
        };
        product.value = 94;
        product.delay = 45;
        return product;
    }},
    Textbook{ Prop make(Map map, final int id, int seed){
        Item product = new Item(map, name(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Scroll, -1){
            public void onCreate(){
                super.onCreate();
                if (id == -1) subId = rand.nextInt(12);
            }
            public String[] load(String p){
                String[] r = super.load(p);
                return r;
            }
            @Override
            public boolean use(com.ga.unit.man.Man host){
                if (host instanceof MainPlayer && super.use(host)) {
                    if (Global.player.get("Intelligence").get() == subId) {
                        if (map.updater.sleep(1, false)) {
                            host.get("Intelligence").addTotal(1);
                            host.say("Trained up Intelligence.");
                            return true;
                        }
                    } else {
                        if (Global.player.get("Intelligence").get() < subId) host.say("This textbook is beyond my understanding!");
                        else host.say("This textbook is too easy!");
                    }
                }
                return false;
            }
            public String getName(){
                String prefix = "Unknown";
                if (subId < 3) prefix = "Beginner's";
                else if (subId < 6) prefix = "Intermediate";
                else if (subId < 9) prefix = "Advanced";
                else if (subId < 12) prefix = "Expert";
                return prefix + " " + super.getName();
            }
            public String getDescription(){
                String req = "This book contains information for those with intellectual level of "+(subId+1)+".";
                int understanding = Global.player.get("Intelligence").get();
                req += " Your current Intelligence score: "+understanding+".";
                if (understanding == subId) req += " You may learn the contents of this book.";
                else if (understanding < subId) req += " This book is beyond your understanding.";
                else req += " You won't learn anything from this book.";
                return super.getDescription()+" "+req;
            }
        };
        product.value = 9400;
        product.delay = 45;
        return product;
    }},
    Bandage{ Prop make(Map map, int id, int seed){
        Item product = new Item(map, name(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Consumable, 4) {
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
        product.delay = 45;
        return product;
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
        };
        product.range = 600;
        product.delay = 45;
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
                range = spell.getRange(null);
                value = 100 + 100*(spell.getDifficulty() * spell.getTrainers().size());
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
        product.delay = 30;
        return product;
    }},
    Ethersword{ Prop make(Map map, int id, int seed){
        Weapon product = new Weapon(map, name(), seed, new Item.Material[]{Item.Material.Crystal}, Item.ItemType.Sword, 1, 0, 0){
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
        product.image.setCenterType(3);
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
    Something{ Prop make(Map map, int id, final int seed){ //unused
        final Prop product = new Prop(map, name(), seed){
        };
        return product;
    }},
    Spawner{ Prop make(Map map, final int id, int seed) {
        Prop product = new Prop(map, name(), seed){
            public void onField(){
                super.onField();
                rotation = Math.min(Global.time, rotation);
                if (Global.time - rotation > 10) {
                    boolean exist = false;
                    for (Unit i : map.updater.unitsLoaded){
                        if (i.brain.factoryId == id && i.seed == seed){
                            exist = true; //counts the fresh dead
                            break;
                        }
                    }
                    if (!exist){
                        Unit man = (Unit) Man.create(map, id, seed);
                        man.pos.set(pos);
                        man.start();
                        rotation = Global.time;
                    }
                }
            }
        };
        product.mobile = false;
        product.image.visible = false;
        return product;
    }},
    Crate{ Prop make(final Map map, int id, int seed){
        if (seed < 100000) seed *= 100000;
        final Prop product = new Prop(map, name(), seed){
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
        };
        product.onInteract = new Interactive(){
            public void interact(com.ga.unit.man.Man p){
                if (p == Global.player){
                    String index;
                    final String productSeed = product.seed + "";
                    index = productSeed.substring(productSeed.length()-5);
                    final String finalIndex = index;
                    Random tRand = new Random(Integer.parseInt(productSeed.substring(0, productSeed.length() - 5)));
                    String[] possibleLoot = new String[]{"Candle","Cup","Chair","Comfortwear","Threadball"};
                    final int itemNum = tRand.nextInt(5);
                    final Item[] products = new Item[itemNum];
                    Performer[] actions = new Performer[itemNum];
                    for (int i=0;i<itemNum;i++){
                        String item = possibleLoot[tRand.nextInt(possibleLoot.length)];
                        int itemSeed = tRand.nextInt(9999);
                        if (finalIndex.charAt(i) != '2') {
                            products[i] = ((Item) createByName(map, item, itemSeed));
                            final int current = i;
                            actions[i] = new Performer() {
                                public void perform() {
                                    String newIndex = finalIndex.substring(0, current) + '2' + finalIndex.substring(current + 1);
                                    product.seed = Integer.parseInt(productSeed.substring(0, productSeed.length() - 5) + newIndex);
                                    map.updater.addQueue.add(products[current]);
                                }
                            };
                        }
                    }
                    Menu.menu.showContainer("Crate", products, actions);
                }
            }
        };
        product.image.setCenterType(2);
        return product;
    }},
    Throwing_Knife{ Prop make(Map map, int id, int seed){
        Item product = new Item(map, "Tanto", seed,
                new Item.Material[]{Item.Material.Steel, Item.Material.Bone, Item.Material.Bronze, Item.Material.Titanium, Item.Material.Glass},
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
        product.encumbrance = 2;
        product.range = 500;
        product.image.setCenterType(3);
        product.value = 16;
        product.delay = 10;
        return product;
    }},
    Fireball{ Prop make(Map map, int id, int seed){
        Item product = new Item(map, name(), seed, new Item.Material[]{Item.Material.Stone}, Item.ItemType.Throwing, 1) {
            @Override
            public boolean use(final com.ga.unit.man.Man host) {
                if (super.use(host) && isUsable(true, 1)) {
                    final int explosionDamage = rand.nextInt(36)+24;
                    host.delay.performer = new Performer() {
                        public void perform() {
                            final Projectile p = new Projectile(map, getName(), host.pos.getAdd(host.clip.organs[BodyHumanoid.FIST].ptd),
                                    host.pos.getAdd(host.target), 0) {
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
        product.value = 16;
        product.delay = 10;
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
                                    host.pos.getAdd(host.target), 48) {
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
        product.delay = 10;
        return product;
    }},
    Book{ Prop make(Map map, int id, int seed){
        Item product = new Item(map, name(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Scroll, -1){
            @Override
            public boolean use(com.ga.unit.man.Man host){
                if (super.use(host)){
                    Menu.menu.showDialog("Title","Content");
                    return true;
                }
                return false;
            }
        };
        product.delay = 45;
        return product;
    }},
    Runnerkit{ Prop make(Map map, int id, int seed){
        Armor product = new Armor(map, name(),seed,Item.Material.values()
                ,3,0,0,8, 0){
        };
        product.disposable = false;
        product.weight = 1;
        product.tags.add(new Tag[]{Tag.Strength,Tag.Athletics}[product.rand.nextInt(2)]);
        return product;
    }},
    Jewelry{ Prop make(Map map, int id, int seed){
        Armor product = new Armor(map, "Fiery_Necklace",seed,Item.Material.values()
                ,3,0,0,1000, 0){
        };
        product.name = "Jewelry";
        product.disposable = false;
        return product;
    }},
    Energy_Necklace{ Prop make(Map map, int id, int seed){
        Armor product = new Armor(map, "Battery",seed,new Item.Material[]{Item.Material.Stone, Item.Material.Glass, Item.Material.Crystal, Item.Material.Obsidian}
                ,3,0,0,20, 0){
            public String getDescription(){
                return super.getDescription()+"\nThis item is used to store Energy for magical applications.";
            }
        };
        product.name = "Energy_Necklace";
        product.disposable = false;
        product.tags.add(Tag.Energy);
        return product;
    }},
    Battery{ Prop make(Map map, int id, int seed){
        Item product = new Item(map, name(),seed,new Item.Material[]{Item.Material.Copper, Item.Material.Lead}
            ,Item.ItemType.Consumable,20){
            public String getDescription(){
                return super.getDescription()+"\nThis item is used to store Energy for magical applications.";
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
        String name = new String[]{"Butcher_Knife","Pot","Frying_Pan","Grill","Smoking_Rack","Drying_Rack","Spice","Vacuum_Sealer"}[seed % 8];
        Food.Ingredient method = com.ga.unit.item.Food.Ingredient.getByType(2).get(seed % 8);
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
                                i.name += " and "+prev.name;
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
    Steroid{ Prop make(Map map, final int id, int seed){
        Item product = new Item(map, name(), seed, new Item.Material[]{Item.Material.Organic}, Item.ItemType.Consumable, 1){
            @Override
            public boolean use(com.ga.unit.man.Man host){
                if (host instanceof MainPlayer && super.use(host) && isUsable(true, 1)) {
                    if (host.get("Agility").require(1)) {
                        host.get("Strength").addTotal(1);
                        host.say("Increased Strength by 1!");
                        if (seed % 2 == 0) host.buff(Buffs.Poison, 1);
                    } else {
                        host.say("I need at least 1 Vitality to use this!");
                    }
                    return true;
                }
                return false;
            }
            public String getDescription(){
                return super.getDescription()+" This drug will permanently increase your Strength by 1.";
            }
        };
        product.value = 12800;
        product.delay = 45;
        return product;
    }},
    Purse{ Prop make(Map map, int id, int seed){
        Armor product = new Armor(map, "Fannypack",seed,new Item.Material[]{Item.Material.Cloth, Item.Material.Leather}
                ,3,3,2,10000, 0){
            public String getDescription(){
                return super.getDescription()+"Contains "+Dialog.money(getUses())+".";
            }
        };
        product.name = "Purse";
        product.tags.add(Tag.Currency);
        product.disposable = false;
        return product;
    }},
    Stomach_Full_Helm{ Prop make(Map map, int id, int seed){
        Armor product = new Armor(map, "Full_Helm",seed,new Item.Material[]{Item.Material.Titanium},0,0,0,-1, 0){
            public String getDescription(){
                return super.getDescription()+" Something is odd about this helmet.";
            }
        };
        product.tags.add(Tag.Channeling);
        product.tags.add(Tag.Conjuration);
        product.tags.add(Tag.Fire);
        product.tags.add(Tag.Enchant);
        product.tags.add(Tag.Air);
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
    public static Prop createByName(Map map, String p, int seed){ // Return to reflection
        for (Stock i : values()){
            if (i.name().equals(p.replace(" ","_"))){
                return i.create(map, -1, seed);
            }
        }
        Prop r = createByNameFromReg(map, p, seed);
        if (r != null) return r;
        return null;//Stock.Object.create(map, 0,0);
    }
    public static Prop createByNameFromReg(Map map, String p, int seed){
        String name = extRegistry.get(p);
        if (name != null){
            String[] data = name.split("@");
            return Stock.valueOf(data[0]).create(map, Integer.parseInt(data[1]), -1);
        }
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
    public static String getFactoryName(int id){
        return id==-1?"":values()[id].name();
    }
    void isFurniture(Prop product){
        product.image.setCenterType(2);
        product.gravity = true;
        product.mobile = false;
        randomSide(product);
    }
    Prop makeAddon(Prop p){
        p.save = false;
        isFurniture(p);
        return p;
    }
    static void randomSide(Prop p){
        p.dir = p.seed % 2 == 0 ? 1 : -1;
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

    UnitFactory(Unit unit, Faction faction, int level){
        host = unit;
        host.faction = faction;
        unit.stats.addStat("Level", level);
        unit.stats.addStat("Strength", 3+level);
        unit.stats.addStat("Agility", 3+level);
        unit.stats.addStat("Intelligence", 3+level);
        unit.stats.addStat("Speed", 18+host.rand.nextInt(8));
        unit.stats.addStat("Attack", host.rand.nextInt(10)-8);
        setAI(2, 0);
    }
    void setEquipment(String[] weapon, String[] armor, String[] helms){
        Man man = (Man) host;
        if (weapon != null && weapon.length > 0) man.setWeapon((Item) Stock.createByName(host.map, weapon[host.seed % weapon.length], -1));
        man.armor = new ArmorSet(man, getItem(host.map, helms[host.seed % helms.length],0),
                getItem(host.map, armor[host.seed % armor.length], 2),
                getItem(host.map, armor[host.seed % armor.length], 1),
                getItem(host.map, armor[host.seed % armor.length], 3));
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
        host.get(stat).setMax(value);
    }
    void setAI(int type, int subType){
        Intelligence.get(type+":"+subType+";"+"1:0", host);
    }
    void setMoveset(String p){
        if (!p.isEmpty()) {
            String[] skills = p.split(",");
            String r = "";
            for (String i : skills) {
                r += i + ":0,";
            }
            Actions.load(host, r.substring(0, r.length() - 1));
        }
    }
}