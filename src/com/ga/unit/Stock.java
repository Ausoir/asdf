package com.ga.unit;

import com.ga.game.FileManager;
import com.ga.game.Sound;
import com.ga.map.Creation;
import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.tool.Interactive;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.item.*;
import com.ga.unit.man.*;
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
        Prop product = new Prop(map, name(), seed);
        product.manualDepth = true;
        product.mobile = false;
        product.image.visible = false;
        return product;
    }},
    Travel{ Prop make(Map map, final int id, final int seed){
        Prop product = new Prop(map, "Travel", seed){
            public String[] load(String p){
                String[] r = super.load(p);
                name = r[5];
                return r;
            }
            public String toString(){
                return super.toString() + "@" + name;
            }
        };
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
    Entrance{ Prop make(Map map, final int id, final int seed){ //Obsolete
        return new Prop(map, name(), seed);
    }},
    Object{ Prop make(Map map, int id, int seed){
        String[] data = FileManager.readText("raw/Object.txt");
        final String[] v = data[id].split(";");
        final Prop product;
        if (!v[6].equals("0")) {
            product = new Prop(map, v[0], seed){
                public void onField(){
                    super.onField();
                    int offset = -Integer.parseInt(v[6]);
                    P s = image.getSize().getMult(.5f, image.getCenterType() == 1 ? .5f : 1).add(0,offset);
                    P top = new P(Math.sin(rotation) * s.y, Math.cos(rotation) * s.y);
                    P width = new P(Math.sin(rotation) * s.x, Math.cos(rotation) * s.x);
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
        String[] units = FileManager.readText("raw/Beast.txt");
        String[] unit = units[id].split(";");
        Unit product = new Unit(map, null, seed){
            public void onCreate(){
                String str = getUnit(map, unit[0],seed,clip.toString(),makeStat(rand,Integer.parseInt(unit[2])),Faction.valueOf(unit[3]),unit[4]+"/1:0", unit[5], "");
                load(str);
            }
        };
        product.clip = new Body(product, unit[0]);
        product.speechType = unit[1];
        product.subId = id;
        return product;
    }},
    Man{ Prop make(Map map, int id, int seed){
        String[] units = FileManager.readText("raw/Man.txt");
        final String[] unit = units[id].split(";",-1);
        com.ga.unit.man.Man product = new Man(map, unit[0], seed){
            public void onCreate(){
                String str = getUnit(map,name,seed,"111111",makeStat(rand,Integer.parseInt(unit[2])),Faction.valueOf(unit[3]),unit[4]+"/1:0",unit[5],"");
                str += "@Battery@;";
                String[] weapon = unit[7].split(",");
                String[] armor = unit[8].split(",");
                String[] helms = unit[9].split(",");
                str += "#"+unit[6]+";"+getItem(map, weapon[seed % weapon.length],-1);
                String helm = getItem(map, helms[seed % helms.length],0);
                String arm = armor[seed % armor.length];
                str += ";"+helm+";"+getItem(map, arm, 2)+";"+getItem(map, arm, 1)+";";
                load(str);
            }
        };
        product.speechType = unit[1];
        product.subId = id;
        return product;
    }},
    Etc{ Prop make(Map map, int id, int seed){
        String[] data = FileManager.readText("raw/Etc.txt");
        final String[] v = data[id].split(";");
        Item product = new Item(map, v[0], seed, Item.Material.load(v[4].split(",")), Item.ItemType.Bashing, 100);
        product.weight = Integer.parseInt(v[2]);
        product.value = Integer.parseInt(v[3]);
        product.dir = product.seed % 2 == 0 ? 1 : -1;
        product.image.setCenterType(Integer.parseInt(v[1]));
        product.subId = id;
        return product;
    }},
    Weapon{ Prop make(Map map, int id, int seed){
        String[] data = FileManager.readText("raw/Weapon.txt");
        final String[] v = data[id].split(";");
        Item product = new Weapon(map, v[0], seed, Item.Material.load(v[4].split(",")), Item.ItemType.valueOf(v[1]),
                40, Integer.parseInt(v[2]));
        int range = Integer.parseInt(v[3]);
        if (range != -1) product.range = range;
        product.subId = id;
        return product;
    }},
    Armor{ Prop make(Map map, int id, int seed){
        String[] data = FileManager.readText("raw/Armor.txt");
        final String[] v = data[id].split(";");
        int piece = Integer.parseInt(v[1]);
        if (piece == 1 || piece == 2) v[0] = v[0].substring(0,v[0].indexOf("_"));
        Prop product = new Armor(map, v[0], seed, Item.Material.load(v[4].split(",")), piece,
                Integer.parseInt(v[2]), Integer.parseInt(v[3]), Integer.parseInt(v[5]));
        product.subId = id;
        return product;
    }},
    Food{ Prop make(Map map, int id, int seed){
        String[] data = FileManager.readText("raw/Food.txt");
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
                    cooldown.put(Actions.Melee, new int[]{Global.time+2});
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
            public void onCreate(){
                load(getUnit(map,name,seed,clip.toString(),makeStat(rand, 3),Faction.Predator,"2:0/1:0","Melee",""));
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
                    ArrayList<Unit> t = map.updater.collideUnit(pos, (int) getReach(), Faction.Destructive);
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
                if (buff == Buffs.Bleed) return;
                if (buff.tags.contains(Tag.Ice)) level *= 8;
                super.buff(buff, level);
            }
            public void onCreate(){
                load(getUnit(map,name(),seed,clip.toString(),makeStat(rand,1),Faction.Predator,"2:0/1:0","Melee",""));
            }
            public void die(){
                Threadball.create(map, -1, -1).pos.set(pos);
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
                jump = true;
            }
            public void stateSet(Actions state) {
                if (state == Actions.Jump){
                    vel.y = -80;
                    vel.x += dir * 4;
                    delay.time = 8;
                    buff(Buffs.Slowfall, 2);
                    get("Stamina").heal();
                } else {
                    super.stateSet(state);
                    if (state == Actions.Shock) {
                        damageMod = 2;
                        moveTriggered(0);
                        delay.time = 2;
                        if (rand.nextBoolean()) cooldown.remove(Actions.Shock);
                    }
                }
            }
            public void buff(Buffs buff, int level){
                if (buff == Buffs.Bleed) level += 2;
                if (buff.tags.contains(Tag.Ice)) level *= 8;
                if (buff.tags.contains(Tag.Fire)) level *= 8;
                super.buff(buff, level);
            }
            public void onCreate(){
                load(getUnit(map,name(),seed,clip.toString(),makeStat(rand,2),Faction.Predator,"2:0/1:0","Shock,Jump",""));
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
                jump = true;
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
                if (seed % 2 == 1) moves = "Flameroll,Fireball";
                else moves = "Fireblast";
                load(getUnit(map,name(),seed,clip.toString(),makeStat(rand,7),Faction.Predator,"3/1:0",moves,""));
            }
        };
        product.gravity = false;
        product.clip = new Body(product, name());
        return product;
    }},
    Golem { Prop make(Map map, int id, int seed) {
        Unit product = new Unit(map, name(), seed){
            public boolean damageBreakthrough(int p, int damage){
                return false;
            }
            public void onCreate(){
                load(getUnit(map,name(),seed,clip.toString(),makeStat(rand,12),Faction.Predator,"3/1:0","Melee",""));
            }
        };
        product.gravity = false;
        product.clip = new Body(product, name());
        return product;
    }},
    Wyvern { Prop make(Map map, int id, int seed) {
        Unit product = new Unit(map, name(), seed){
            public void onCreate(){
                load(getUnit(map,name(),seed,clip.toString(),makeStat(rand,16),Faction.Predator,"3/1:0","Melee",""));
            }
        };
        product.gravity = false;
        product.clip = new Body(product, name());
        return product;
    }},
    Fist{ Prop make(Map map, int id, int seed){
        Item product = new Weapon(map, name(),0, new Item.Material[]{Item.Material.Flesh}, Item.ItemType.Bashing, -1, 0){
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
        };
        product.range = -1;
        product.image.setCenterType(3);
        product.onInteract = null;
        product.save = false;
        return product;
    }},
    Goldbag{ Prop make(Map map, int id, int seed){
        Item product = new Item(map, name(),0, new Item.Material[]{Item.Material.Gold}, Item.ItemType.Bashing, 200){
            public boolean use(final com.ga.unit.man.Man host) {
                host.setState(Actions.Interact);
                return true;
            }
            public void onCreate(){
                isUsable(100);
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
            @Override
            public boolean use(com.ga.unit.man.Man host){
                if (host instanceof Player && super.use(host)) {
                    Player player = (Player) host;
                    if (player.getStateLevel(skill) > 0) {
                        if (map.updater.sleep(100) == 100) {
                            if (!player.learnSkill(skill)) host.say("Failed to learn "+skill.getName()+".");
                        }
                    } else host.say("Not skilled enough to learn "+skill.getName()+"!");
                }
                return false;
            }
            public String getName(){
                return super.getName()+" of "+skill.getName();
            }
            public String getDescription(){
                int difficulty = skill.getDifficulty();
                int power = Global.player.getStateLevel(skill);
                String req = "Fields of study: ";
                for (Tag i : skill.getTrainers()) req += "[" + i.name() + "] ";
                req += "x Difficulty "+difficulty+".";
                req += " Your current level of expertise in this field: "+power+".";
                if (power > 0) req += " You are capable of performing this action.";
                else req += " You lack the expertise to perform the technique described in this manual.";
                return super.getDescription()+" "+req;
            }
        };
        product.value = 94;
        product.delay = 45;
        return product;
    }},
    Textbook{ Prop make(Map map, final int id, int seed){
        Item product = new Item(map, name(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Scroll, -1){
            private String stat;
            public void onCreate(){
                super.onCreate();
                stat = Tags.trainers[seed % Tags.trainers.length].name();
                if (id == -1) subId = rand.nextInt(4);
            }
            public String[] load(String p){
                String[] r = super.load(p);
                stat = Tags.trainers[seed % Tags.trainers.length].name();
                return r;
            }
            @Override
            public boolean use(com.ga.unit.man.Man host){
                if (host instanceof MainPlayer && super.use(host)) {
                    if (Global.player.learnable()) {
                        if (Global.player.get(stat).get() / 100 == subId) {
                            ArrayList<Prop> t = map.updater.collideProp(pos, 240);
                            Prop dummy = null;
                            for (Prop i : t) {
                                if (i.name.equals("Training_Dummy")) {
                                    dummy = i;
                                }
                            }
                            if (dummy != null) {
                                if (map.updater.sleep(100) == 100) {
                                    if (host.get("Motivation").require(10)) {
                                        host.get(stat).add(rand.nextInt((400 - host.get(stat).get()) / 10) + 1, true);
                                        host.say("Trained up " + stat + ".");
                                        dummy.remove();
                                        return true;
                                    } else host.say("No Motivation...");
                                }
                            } else host.say("No Training Dummies nearby!");
                        } else {
                            if (Global.player.get(stat).get() / 100 < subId)
                                host.say("This textbook is beyond my understanding!");
                            else
                                host.say("This textbook is too easy!");
                        }
                    } else host.say("Not enough Intelligence stat!");
                }
                return false;
            }
            public String getName(){
                String prefix = "Unknown";
                if (subId == 0) prefix = "Beginner's";
                else if (subId == 1) prefix = "Intermediate";
                else if (subId == 2) prefix = "Advanced";
                else if (subId == 3) prefix = "Expert";
                return prefix + " " + super.getName()+" of "+stat;
            }
            public String getDescription(){
                String req = "This book teaches level "+(subId+1)+" "+stat+".";
                int understanding = Global.player.get(stat).get()/100;
                req += " Your current level of understanding of "+stat+": "+understanding+".";
                if (understanding == subId) req += " You may exploit the contents of this book.";
                else if (understanding < subId) req += " This book is beyond your understanding.";
                else if (understanding > subId) req += " You won't learn anything from this book.";
                return super.getDescription()+" "+req;
            }
        };
        product.value = 94;
        product.delay = 45;
        return product;
    }},
    Bandage{ Prop make(Map map, int id, int seed){
        Item product = new Item(map, name(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Consumable, 4) {
            public boolean use(com.ga.unit.man.Man host) {
                if (isUsable(1) && super.use(host)){
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
                if (isUsable(1) && super.use(host)){
                    host.buff(emod, ademod.level);
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
                if (isUsable(1) && super.use(host)) {
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
                return super.getName()+" of "+spell.getName();
            }
        };
        product.probability = 1;
        product.delay = 30;
        return product;
    }},
    Ethersword{ Prop make(Map map, int id, int seed){
        Weapon product = new Weapon(map, name(), seed, new Item.Material[]{Item.Material.Crystal}, Item.ItemType.Sword, -1, 0){
            public String toString(){
                return "";
            }
            public void remove(){
                super.remove();
                Particle.holyExplosion(map, pos,getDepth());
            }
            public void trigger(Unit host) {
                super.trigger(host);
                Particle.holyExplosion(map, pos,getDepth());
            }
            public int getDamage(){
                return 3;
            }
        };
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
    Dungeon{ Prop make(Map map, int id, final int seed){
        final Prop entrance = Stock.Spawn.create(map, -1, 0);
        final Prop product = new Prop(map, name(), seed){
            public void onField(){
                super.onField();
                entrance.pos.add(pos.x, pos.y);
            }
        };
        entrance.onInteract = new Interactive(){
            public void interact(com.ga.unit.man.Man p){
                if (p == Global.player && p.lastLand.depth == product.getDepth()+2){
//                    Creation.prepareRandom(Integer.parseInt(map.name+""+entrance.seed));
//                    map.loadMap("D_"+map.name+"_"+entrance.seed+"@"+entrance.seed+"@0");
                }
            }
        };
        entrance.save = false;
        entrance.pos.set(0,-product.image.getSize().y/2);
        isFurniture(product);
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
                P top = new P(Math.sin(rotation)*s.y, Math.cos(rotation)*s.y);
                P width = new P(Math.sin(rotation)*s.x, Math.cos(rotation)*s.x);
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
                    String[] possibleLoot = new String[]{"Bandage","Threadball","Battery","Potion","Scroll"};
                    final int itemNum = tRand.nextInt(5);
                    final Item[] products = new Item[itemNum];
                    Performer[] actions = new Performer[itemNum];
                    for (int i=0;i<itemNum;i++){
                        String item = possibleLoot[tRand.nextInt(possibleLoot.length)];
                        int itemSeed = tRand.nextInt(9999);
                        if (finalIndex.charAt(i) != '2') {
                            products[i] = ((Item) getByName(map, item, itemSeed));
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
                    Global.display.menu.showContainer("Crate", products, actions);
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
                if (host.clip.getHandStatus() == 1 && isUsable(1) && super.use(host)) {
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
                if (isUsable(1) && super.use(host)) {
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
    Threadball{ Prop make(Map map, int id, int seed){
        Item product = new Item(map, name(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Throwing, 5) {
            @Override
            public boolean use(final com.ga.unit.man.Man host) {
                if (isUsable(1) && super.use(host)) {
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
                                            host.pos = pos.getAdd(0,-24);
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
                    Global.display.menu.showDialog("Title","Content");
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
                ,3,0,0,8){
        };
        product.disposable = false;
        product.weight = 1;
        product.tags.add(new Tag[]{Tag.Strength,Tag.Athletics}[product.rand.nextInt(2)]);
        return product;
    }},
    Jewelry{ Prop make(Map map, int id, int seed){
        Armor product = new Armor(map, "Battery",seed,Item.Material.values()
                ,3,0,0,1000){
        };
        product.name = "Jewelry";
        product.disposable = false;
        return product;
    }},
    Manastone_Necklace{ Prop make(Map map, int id, int seed){
        Armor product = new Armor(map, "Battery",seed,new Item.Material[]{Item.Material.Stone, Item.Material.Glass, Item.Material.Crystal, Item.Material.Obsidian}
                ,3,0,0,1000){
            public String getDescription(){
                return super.getDescription()+"\nThis item is used to store mana for magical applications.";
            }
        };
        product.name = "Manastone_Necklace";
        product.disposable = false;
        product.tags.add(Tag.Mana);
        product.tags.add(Tag.Electric);
        return product;
    }},
    Manastone{ Prop make(Map map, int id, int seed){
        Item product = new Item(map, "Battery",seed,new Item.Material[]{Item.Material.Stone, Item.Material.Glass, Item.Material.Crystal, Item.Material.Obsidian}
            ,Item.ItemType.Consumable,2400){
            public String getDescription(){
                return super.getDescription()+"\nThis item is used to store mana for magical applications.";
            }
        };
        product.name = "Manastone";
        product.disposable = false;
        product.tags.add(Tag.Mana);
        product.tags.add(Tag.Electric);
        return product;
    }},
    Stomach_Full_Helm{ Prop make(Map map, int id, int seed){
        Armor product = new Armor(map, "Full_Helm",seed,new Item.Material[]{Item.Material.Titanium},0,0,0,-1){
            public String getDescription(){
                return super.getDescription()+" Something is odd about this helmet.";
            }
        };
        product.tags.add(Tag.Channeling);
        product.tags.add(Tag.Conjuration);
        product.tags.add(Tag.Electric);
        product.tags.add(Tag.Fire);
        product.tags.add(Tag.Enchant);
        product.tags.add(Tag.Air);
        return product;
    }},
    Hexdoll{ Prop make(Map map, int id, int seed){
        Weapon product = new Weapon(map,name(),seed,new Item.Material[]{Item.Material.Flesh, Item.Material.Bone, Item.Material.Wooden, Item.Material.Cloth}
            ,Item.ItemType.Consumable, 100, 0){
            public String getDescription(){
                return super.getDescription()+"\nThis item is used as a catalyst for curses.";
            }
            public void onCreate(){
                isUsable(getUses()-1);
            }
            private void collectBlood(com.ga.unit.man.Man host){
                boolean hostSaved = false, collected = false;
                ArrayList<Unit> t = map.updater.collideUnit(pos,96,Faction.Destructive);
                for (Unit i : t){
                    if (i.state == Actions.Down || i.stats.getEffectLevel(Buffs.Bleed)>0){
                        if (i == host){
                            hostSaved = true;
                            continue;
                        }
                        host.say("Stained "+getFullName());
                        isUsable(getUses());
                        recharge(10 + 10*i.rand.nextInt(i.get("Level").get()+1));
                        i.buff(Buffs.Mark,1);
                        i.buff(Buffs.Bleed,7);
                        collected = true;
                        break;
                    }
                }
                if (!collected && hostSaved){
                    host.say("Stained "+getFullName());
                    isUsable(getUses());
                    recharge(10 + 10*host.rand.nextInt(host.get("Level").get()+1));
                    host.buff(Buffs.Mark,1);
                    host.buff(Buffs.Bleed,1);
                }
            }
            public boolean use(com.ga.unit.man.Man host){
                if (host.traits.has(Traits.Trait.Enchanting)) {
                    if (super.use(host)) {
                        int bloodQuality = (getUses()+1)/10;
                        final ArrayList<Buffs> buffs = Buffs.getByLevel(quality+2);
                        final String[] listString = new String[buffs.size() + 1];
                        Performer[] performers = new Performer[listString.length + 1];
                        listString[0] = "Collect blood";
                        performers[0] = new Performer() {
                            public void perform() {
                                collectBlood(host);
                            }
                        };
                        int power = host.get("Intelligence").get()+bloodQuality; //not max
                        if (isUsable(1)) {
                            for (int i = 0; i < listString.length - 1; i++) {
                                Buffs buff = buffs.get(i);
                                listString[i + 1] = buff.name() + " - Power "+buff.value;
                                final int finalI = i + 1;
                                performers[i + 1] = new Performer() {
                                    public void perform() {
                                        ArrayList<Unit> t = map.updater.collideUnit(pos, 800, Faction.Destructive);
                                        for (Unit i : t) {
                                            if (i.stats.getEffectLevel(Buffs.Mark) > 0) {
                                                if (i.get("Intelligence").get()+buff.value < power) {
                                                    i.buff(buff, bloodQuality);
                                                    isUsable(buff.value);
                                                } else {
                                                    host.buff(buff, bloodQuality);
                                                    host.say(listString[finalI]+" backfired!");
                                                }
                                            }
                                        }
                                    }
                                };
                            }
                        }
                        Global.display.menu.showDialog("List of Enchantments",
                                "Blood: " + getUses() + " drops left\nBlood Quality: "+bloodQuality
                                        +"\nCatalyst Level: "+quality+"\nHex Power: "+power, listString, performers);
                        return true;
                    } else {
                        collectBlood(host);
                    }
                }
                return false;
            }
        };
        product.disposable = false;
        product.tags.add(Tag.Curse);
        product.tags.add(Tag.Curse);
        return product;
    }};
    private static HashMap<String, String> extRegistry = new HashMap<>();
    public static Prop get(com.ga.map.Map map, String p){
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
    public static Prop getByName(Map map, String p, int seed){ // Return to reflection
        for (Stock i : values()){
            if (i.name().equals(p.replace(" ","_"))){
                return i.create(map, -1, seed);
            }
        }
        Prop r = getByNameFromReg(map, p, seed);
        if (r != null) return r;
        return Stock.Object.create(map, 0,0);
    }
    public static Prop getByNameFromReg(Map map, String p, int seed){
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
    Prop makeAddon(Map map, String name, Random rand){
        return makeAddon(new Prop(map, name,rand.nextInt(9999)));
    }
    Prop makeAddon(Prop p){
        p.save = false;
        isFurniture(p);
        return p;
    }
    static void randomSide(Prop p){
        p.dir = p.seed % 2 == 0 ? 1 : -1;
    }
    static String makeStat(Random rand, int level){
        int str,dex,sta,wis,intel,spd;
        str = dex = sta = 3+level;
        spd = 18+rand.nextInt(8);
        wis = rand.nextInt(1+level*2);
        intel = rand.nextInt(1+level*2);
        return "Level:"+level+"&Strength:"+str+"&Agility:"+dex+"&Stamina:"+sta+"&Intelligence:"+intel+"&Speed:"+spd+"&Attack:0";
    }
    static String getUnit(Map map, String name, int seed, String clip, String stat, Faction faction, String intel, String moves, String traits){
        return name+"@-1@"+seed+"@0,0@0@"+Global.getControlId()+"@"+map.name+"@"+clip+"@"+stat+"@"+faction.name()+"@"+intel+"@"+moves+"@"+traits;
    }
    private static String getItem(Map map, String p, int piece){
        if (p == null || p.equals("")) return "";
        String pieceName = "";
        if (piece == 1) pieceName = "_Pants";
        else if (piece == 2) pieceName = "_Torso";
        Prop product = Stock.getByName(map, p+pieceName,-1);
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
            String[] data = FileManager.readText("raw/"+raw+".txt");
            for (int i=0;i<data.length;i++)
                extRegistry.put(data[i].substring(0,data[i].indexOf(";")), raw+"@"+i);
        }
    }
}