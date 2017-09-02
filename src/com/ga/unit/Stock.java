package com.ga.unit;

import com.ga.game.FileManager;
import com.ga.game.Sound;
import com.ga.map.Building;
import com.ga.map.Creation;
import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.tool.Interactive;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.item.*;
import com.ga.unit.player.MainPlayer;
import com.ga.unit.player.Player;
import com.ga.unit.player.Trait;
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
        Prop product = new Prop(map, getName(), seed);
        product.manualDepth = true;
        product.mobile = false;
        product.image.visible = false;
        return product;
    }},
    Travel{ Prop make(Map map, final int id, final int seed){
        Prop product = new Prop(map, getName(), seed);
        product.manualDepth = true;
        product.mobile = false;
        product.image.visible = false;
        product.onInteract = new Interactive(){
            public void interact(Man p){
                p.changeMap(id+"@"+seed);
            }
        };
        return product;
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
                            pos.x + top.x + width.y, pos.y - top.y + width.x, -1, 0, lastLand.depth - 1) {
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
        Unit product = new Unit(map, null, Creation.getSeed(seed));
        product.clip = new Body(product, unit[0]);
        product.load(getUnit(unit[0],product.seed,product.clip.toString(),unit[2], Faction.valueOf(unit[3]), unit[4]+"/1&0", unit[5]));
        product.speechType = unit[1];
        product.subId = id;
        return product;
    }},
    Man{ Prop make(Map map, int id, int seed){
        String[] units = FileManager.readText("raw/Man.txt");
        final String[] unit = units[id].split(";");
        Man product = new Man(map, unit[0], Creation.getSeed(seed)){
            public void onCreate(){
                String str = getUnit(name,seed,"111111",unit[2],Faction.valueOf(unit[3]),unit[4]+"/1&0",unit[5]);
                String[] weapon = unit[7].split(",");
                String[] armor = unit[8].split(",");
                String[] helms = unit[9].split(",");
                str += "#"+unit[6]+"#"+getItem(map, weapon[seed % weapon.length],-1);
                String helm = getItem(map, helms[seed % helms.length],0);
                String arm = armor[seed % armor.length];
                str += "#"+helm+"&"+getItem(map, arm, 2)+"&"+getItem(map, arm, 1);
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
        Item product = new Item(map, v[0], Creation.getSeed(seed), Item.Material.load(v[4].split(",")), Item.ItemType.Bashing);
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
        Item product = new Weapon(map, v[0], Creation.getSeed(seed), Item.ItemType.valueOf(v[1]),
                Integer.parseInt(v[2]), Item.Material.load(v[4].split(",")));
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
        Prop product = new Armor(map, v[0], Creation.getSeed(seed), piece,
                Integer.parseInt(v[2]), Integer.parseInt(v[3]), Item.Material.load(v[4].split(",")));
        product.subId = id;
        return product;
    }},
    Food{ Prop make(Map map, int id, int seed){
        String[] data = FileManager.readText("raw/Food.txt");
        final String[] v = data[id].split(";");
        Prop product = new Food(map, v[0], Creation.getSeed(seed), Integer.parseInt(v[1]),
                Integer.parseInt(v[2]), v[3], Integer.parseInt(v[4]), Integer.parseInt(v[5]));
        product.subId = id;
        return product;
    }},
    Player{ Prop make(Map map, int id, int seed){
        if (id == 1) return new MainPlayer(map, seed);
        return new Player(map, seed);
    }},
    Construct{ Prop make(Map map, int id, int seed){
        Building product = new Building(map, id+"", 0);
        product.subId = id;
        return product;
    }},
    Manual{ Prop make(Map map, int id, int seed){
        Item product = new Item(map, getName(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Scroll){
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
            public boolean use(Man host){
                if (host instanceof Player && super.use(host)) {
                    Player player = (Player) host;
                    int difficulty = skill.getDifficulty();
                    if (player.getStatePower(skill) >= difficulty) {
                        if (map.updater.sleep(100) == 100) {
                            if (player.get("Memory").require(1)) {
                                if (difficulty == 0) host.get("Progress").setMax(1);
                                else host.get("Progress").setMax(difficulty * 25);
                                host.get("Progress").set(0, true);
                                ((Player) host).learning = skill;
                                host.say("Now learning " + skill.getName() + "!");
                                return true;
                            } else host.say("Not enough Memory points!");
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
                int power = Global.player.getStatePower(skill);
                String req = "Fields of study: ";
                for (Tag i : skill.getTrainers()) req += "[" + i.name() + "] ";
                req += "x Difficulty "+difficulty+".";
                req += " Your current level of expertise in this field: "+power+".";
                if (power >= difficulty) req += " You are capable of performing this action.";
                else req += " You lack the expertise to perform the technique described in this manual.";
                return super.getDescription()+" "+req;
            }
        };
        product.value = 94;
        product.delay = 45;
        return product;
    }},
    Textbook{ Prop make(Map map, final int id, int seed){
        Item product = new Item(map, getName(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Scroll){
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
            public boolean use(Man host){
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
                    } else host.say("Not enough Memory stat!");
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
        Item product = new Item(map, getName(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Consumable) {
            public boolean use(Man host) {
                if (usable(1) && super.use(host)){
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
        Item product = new Item(map, getName(), seed, new Item.Material[]{Item.Material.Glass}, Item.ItemType.Consumable) {
            public void onCreate() {
                if (seed != 0) imbueMeta(false, false, 1);
            }
            public boolean use(Man host) {
                if (usable(1) && super.use(host)){
                    host.buff(emod, ademod.level);
                    return true;
                }
                return false;
            }
            public String getFullName(){
                if (!Global.player.hasTrait(Trait.Alchemist)) return getName();
                return super.getFullName();
            }
        };
        product.range = 600;
        product.delay = 45;
        return product;
    }},
    Scroll{ Prop make(Map map, int id, int seed){
        Item product = new Item(map, getName(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Scroll){
            Actions spell;
            @Override
            public boolean use(Man host){
                if (usable(1) && super.use(host)) {
                    if (spell != null){
                        host.stateLevel = 3;
                        host.stateSet(spell);
                        return true;
                    }
                }
                return false;
            }
            private void makeSpell(){
                ArrayList<Actions> list = Actions.getLearnables();
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
                if (!Global.player.hasTrait(Trait.Enchanter)) return super.getName();
                return super.getName()+" of "+spell.getName();
            }
        };
        product.probability = 1;
        product.delay = 30;
        return product;
    }},
    Ethersword{ Prop make(Map map, int id, int seed){
        Weapon product = new Weapon(map, getName(), seed, Item.ItemType.Sword, 0, new Item.Material[]{Item.Material.Crystal}){
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
    Phone_Booth{ Prop make(Map map, int id, int seed){
        Prop product = new Prop(map, getName(), seed);
        product.onInteract = new Interactive(){
            public void interact(Man p){
                String t = p.getConversation();
                if (t.equals("")){
                    Global.server.startServer();
                    p.say("Waiting for a call...");
                } else if (t.toLowerCase().startsWith("call")) {
                    Global.server.startClient(p.getConversation().substring(5));
                    p.say("Connecting...");
                }
                p.setState(Actions.Hit);
            }
        };
        return product;
    }},
    Map{ Prop make(Map map, int id, int seed){ //unused
        Prop product = new Prop(map, getName(), seed);
        product.onInteract = new Interactive(){
            public void interact(Man p){
            }
        };
        return product;
    }},
    Fist{ Prop make(Map map, int id, int seed){
        Item product = new Weapon(map, getName(),0, Item.ItemType.Bashing, 0, new Item.Material[]{Item.Material.Flesh}){
            public boolean use(final Man host) {
                host.setState(Actions.Interact);
                return true;
            }
            public String forceToString() {
                return "";
            }
            public void drop(Man host) {}
            public void onUnequip(Man host){}
            public void onCreate(){}
        };
        product.setMaterial(Item.Material.Flesh);
        product.range = -1;
        product.image.setCenterType(3);
        product.onInteract = null;
        return product;
    }},
    Dungeon{ Prop make(Map map, int id, final int seed){
        final Prop entrance = Stock.Spawn.create(map, -1, 0);
        final Prop product = new Prop(map, getName(), seed){
            public void onField(){
                super.onField();
                entrance.pos.add(pos.x, pos.y);
            }
        };
        entrance.onInteract = new Interactive(){
            public void interact(Man p){
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
    SteamHouse{ Prop make(Map map, int id, final int seed){
        final Prop spawn = Stock.Spawn.create(map, -1, 0);
        final Prop entrance = Stock.Travel.create(map, -1, -(spawn.seed*100));
        final Prop product = new Prop(map, getName(), seed){
            public void onField(){
                super.onField();
                spawn.pos.add(pos.x, pos.y);
                entrance.pos.add(pos.x, pos.y);
                P s = image.getSize().getMult(.5f, 1).add(-12,-16);
                P top = new P(Math.sin(rotation)*s.y, Math.cos(rotation)*s.y);
                P width = new P(Math.sin(rotation)*s.x, Math.cos(rotation)*s.x);
                map.land.add(new Line(pos.x+top.x-width.y, pos.y-top.y-width.x,
                        pos.x+top.x+width.y, pos.y-top.y+width.x, -1, 0, lastLand.depth){
                    public String toString(){
                        return "";
                    }
                });
            }
        };
        spawn.save = false;
        entrance.save = false;
        isFurniture(product);
        if (product.image.multiple == 0) spawn.pos.set(product.dir*42,-64);
        else if (product.image.multiple == 1) spawn.pos.set(product.dir*72,-90);
        else if (product.image.multiple == 2) spawn.pos.set(product.dir*-116,-84);
        else if (product.image.multiple == 3) spawn.pos.set(product.dir*8,-97);
        entrance.pos.set(spawn.pos);
        return product;
    }},
    TowerHouse{ Prop make(Map map, int id, final int seed){
        final ArrayList<Prop> addons = new ArrayList<>();
        final Prop entrance = Stock.Spawn.create(map, -1, 0);
        final Prop product = new Prop(map, "TowerHouse", seed){
            public void onField(){
                super.onField();
                map.updater.addQueue.add(entrance);
                for (Prop i : addons){
                    i.onField();
                    i.pos.add(pos.x, pos.y);
                }
                P s = image.getSize().getMult(.5f, 1).add(-16,-4);
                map.land.add(new Line(pos.x-s.x, pos.y-s.y,
                        pos.x+s.x, pos.y-s.y, -1, 0, lastLand.depth){
                    public String toString(){
                        return "";
                    }
                });
            }
            public void draw(Graphics2D g){
                super.draw(g);
                for (Prop i : addons) i.draw(g);
            }
            public void update(){
                for (Prop i : addons) i.update();
            }
        };
        isFurniture(product);
        product.randomSize(new P(0,.5f));
        float baseSizeX = product.image.getSize().x-64;
        float baseSizeY = product.image.getSize().y-64;
        Prop door = makeAddon(map, "Door", product.rand);
        int doorWidth = (int) (baseSizeX-door.image.getSize().x);
        P doorPos = new P(product.rand.nextInt(doorWidth)-(doorWidth)/2, -8);
        door.pos = doorPos;
        addons.add(door);
        int houseType = product.rand.nextInt(16);
        if (houseType == 1){
            Prop sign = makeAddon(map, "Sign_Food", product.rand);
            sign.pos.set(doorPos.x<0?doorPos.x+128:doorPos.x-128, doorPos.y);
            addons.add(sign);
        } else if (houseType == 2){
            Prop sign = makeAddon(map, "Sign_Shop", product.rand);
            sign.pos.set(doorPos.x<0?doorPos.x+128:doorPos.x-128, doorPos.y);
            addons.add(sign);
        } else if (houseType == 3){
            Prop sign = makeAddon(map, "Sign_Guild", product.rand);
            sign.pos.set(doorPos.x<0?doorPos.x+128:doorPos.x-128, doorPos.y);
            addons.add(sign);
        } else if (houseType == 4){
            Prop sign = makeAddon(map, "Sign_Training", product.rand);
            sign.pos.set(doorPos.x<0?doorPos.x+128:doorPos.x-128, doorPos.y);
            addons.add(sign);
        } else houseType = 0;
        final int finalHouseType = houseType;
        int xwindows = product.rand.nextInt(4)+1;
        final int ywindows = product.rand.nextInt((int) Math.max(1,(baseSizeY-door.image.getSize().y)/100));
        float xmargin = baseSizeX / (xwindows+1);
        float ymargin = baseSizeY / (ywindows+1);
        for (int ix = 0; ix < xwindows; ix++) {
            for (int iy = 0; iy < ywindows; iy++) {
                Prop window = makeAddon(map, "Window", product.rand);
                window.pos = new P(xmargin*(ix+1)-(baseSizeX/2), -ymargin*(iy+1)-door.image.getSize().y/2);
                addons.add(window);
            }
        }
        if (product.rand.nextBoolean()) {
            for (int i = 0; i < product.rand.nextInt(3); i++) {
                Prop appliance = makeAddon(map, "Appliance", product.rand);
                appliance.pos = new P(product.rand.nextInt((int) baseSizeX)-baseSizeX/2, -product.rand.nextInt((int) baseSizeY-64));
                addons.add(appliance);
            }
        }
        for (int i=0;i<product.rand.nextInt(3);i++) {
            Prop clockwork = new Prop(map, "Clockwork", seed){
                float speed;
                public void onField(){
                    speed = (float)(rand.nextInt(50)-25)/image.getSize().x;
                }
                public void update(){
                    rotation += speed;
                }
                public void randomSize(P sizeVariance){
                    P s = image.getSize();
                    float rx = rand.nextFloat() * (sizeVariance.x*2) + (1-sizeVariance.x);
                    image.setSize(s.x * rx, s.y * rx);
                }
            };
            makeAddon(clockwork);
            clockwork.randomSize(new P(.5f,.5f));
            clockwork.image.setCenterType(1);
            clockwork.pos = new P(product.rand.nextInt((int) baseSizeX)-baseSizeX/2, -product.rand.nextInt((int) baseSizeY-64));
            addons.add(clockwork);
        }
        makeAddon(entrance);
        entrance.pos = doorPos.getAdd(0,-door.image.getSize().y/2);
        addons.add(entrance);
        entrance.onInteract = new Interactive(){
            public void interact(Man p){
                if (p == Global.player && p.lastLand.depth == product.getDepth()+2){
//                    Global.map.loadMap("I_"+Global.map.name+"_"+entrance.seed+"@"+entrance.seed+"@"+product.image.getSize().getAdd(64,64)+"@"+finalHouseType+"@"+ywindows);
                }
            }
        };
        return product;
    }},
    TowerTip{ Prop make(Map map, int id, int seed){
        final Prop fan = new Prop(map, "Propeller",seed){
            float speed;
            public void onField(){
                speed = (float)(rand.nextInt(50)-25)/image.getSize().x;
            }
            public void update(){
                rotation+=speed;
            }
            public void randomSize(P sizeVariance){
                P s = image.getSize();
                float rx = rand.nextFloat() * (sizeVariance.x*2) + (1-sizeVariance.x);
                image.setSize(s.x * rx, s.y * rx);
            }
        };
        makeAddon(fan);
        Prop product = new Prop(map, getName(), seed){
            public void setDepth(int depth){
                super.setDepth(depth);
                fan.setDepth(depth+(seed%2==0?1:-1));
            }
            public void onField(){
                super.onField();
                fan.pos.add(pos.x, pos.y);
            }
        };
        isFurniture(product);
        if (seed % 3 != 0) fan.image.visible = false;
        fan.randomSize(new P(.5f,.5f));
        fan.pos = new P(0,-product.image.getSize().y+4);
        return product;
    }},
    Spawner{ Prop make(Map map, final int id, int seed) {
        Prop product = new Prop(map, getName(), seed){
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
        final Prop product = new Prop(map, getName(), seed){
            public void onField(){
                super.onField();
                P s = image.getSize().getMult(.5f, 1).add(0,-4);
                P top = new P(Math.sin(rotation)*s.y, Math.cos(rotation)*s.y);
                P width = new P(Math.sin(rotation)*s.x, Math.cos(rotation)*s.x);
                map.land.add(new Line(pos.x+top.x-width.y, pos.y-top.y-width.x,
                        pos.x+top.x+width.y, pos.y-top.y+width.x, -1, 0, lastLand.depth-1){
                    public String toString(){
                        return "";
                    }
                });
            }
        };
        product.onInteract = new Interactive(){
            public void interact(Man p){
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
                Item.ItemType.Darting){
            @Override
            public boolean use(final Man host){
                if (host.clip.getHandStatus() == 1 && usable(1) && super.use(host)) {
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
                            p.blunt = false;
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
        Item product = new Item(map, getName(), seed, new Item.Material[]{Item.Material.Stone}, Item.ItemType.Throwing) {
            @Override
            public boolean use(final Man host) {
                if (usable(1) && super.use(host)) {
                    host.delay.performer = new Performer() {
                        public void perform() {
                            final Projectile p = new Projectile(map, getName(), host.pos.getAdd(host.clip.organs[BodyHumanoid.FIST].ptd),
                                    host.pos.getAdd(host.target), 0) {
                                @Override
                                public void stuck() {
                                    Particle.explosion(map, pos, getDepth());
                                    ArrayList<Unit> t = map.updater.collideUnit(pos,96,Faction.Destructive);
                                    for (Unit i : t){
                                        i.checkHit(pos, getDamage());
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
                            p.damage = getDamage()+2;
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
        Item product = new Item(map, getName(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Throwing) {
            @Override
            public boolean use(final Man host) {
                if (usable(1) && super.use(host)) {
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
                                            host.pos = pos.getAdd(0,0);
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
                                public int getDepth(){
                                    return host.getDepth()+1;
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
        Item product = new Item(map, getName(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Scroll){
            @Override
            public boolean use(Man host){
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
    Rover{ Prop make(Map map, int id, int seed) {
        Unit product = new Unit(map, getName(), seed){
            public void stateSet(Actions state){
                super.stateSet(state);
                if (state == Actions.Melee){
                    delay.time = 24;
                    vel.x = 32*dir;
                    vel.y -= 20;
                    if (rand.nextBoolean()) cooldown.put(Actions.Melee, new int[]{Global.time+2});
                }
            }
            public Unit attack(){
                ArrayList<Unit> t = map.updater.collideUnit(pos,48,faction);
                if (t.size() > 0){
                    Unit i = t.get(0);
                    i.checkHit(pos, getDamage());
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
                load(getUnit(name,seed,clip.toString(),"Stamina:3&Speed:12&Attack:0&Level:3",Faction.Predator,"3/1&0","Melee"));
            }
        };
        product.clip = new Body(product, getName());
        return product;
    }},
    Slime { Prop make(Map map, int id, int seed) {
        Unit product = new Unit(map, getName(), seed){
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
                        i.checkHit(pos, getDamage());
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
                load(getUnit(getName(),seed,clip.toString(),"Stamina:7&Speed:6&Attack:0&Level:1",Faction.Predator,"3/1&0","Melee"));
            }
            public void die(){
                Threadball.create(map, -1, -1).pos.set(pos);
            }
        };
        product.clip = new Body(product, getName());
        product.clip.organs[0].setSize((int) product.clip.organs[0].getImage().getSize().x);
        return product;
    }},
    Bird { Prop make(Map map, int id, int seed) {
        Unit product = new Unit(map, getName(), seed){
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
                load(getUnit(getName(),seed,clip.toString(),"Stamina:14&Speed:12&Attack:0&Level:2",Faction.Predator,"3/1&0","Shock,Jump"));
            }
        };
        product.clip = new Body(product, getName());
        product.clip.organs[0].setSize((int) product.clip.organs[0].getImage().getSize().x);
        return product;
    }},
    Beholder { Prop make(Map map, int id, int seed) {
        Unit product = new Unit(map, getName(), seed){
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
                load(getUnit(getName(),seed,clip.toString(),"Stamina:30&Speed:10&Attack:0&Level:7",Faction.Predator,"3/1&0",moves));
            }
        };
        product.gravity = false;
        product.clip = new Body(product, getName());
        return product;
    }},
    Golem { Prop make(Map map, int id, int seed) {
        Unit product = new Unit(map, getName(), seed){
            public int processDamage(int p, int damage){
                return Math.max(0,damage-5);
            }
            public void onCreate(){
                load(getUnit(getName(),seed,clip.toString(),"Stamina:30&Speed:10&Attack:0&Level:12",Faction.Predator,"3/1&0","Melee"));
            }
        };
        product.gravity = false;
        product.clip = new Body(product, getName());
        return product;
    }},
    Wyvern { Prop make(Map map, int id, int seed) {
        Unit product = new Unit(map, getName(), seed){
            public int processDamage(int p, int damage){
                return Math.max(0,damage-4);
            }
            public void onCreate(){
                load(getUnit(getName(),seed,clip.toString(),"Stamina:30&Speed:30&Attack:0&Level:16",Faction.Predator,"3/1&0","Melee"));
            }
        };
        product.gravity = false;
        product.clip = new Body(product, getName());
        return product;
    }},
    //Uniques
    Stomach_Full_Helm{ Prop make(Map map, int id, int seed){
        Armor product = new Armor(map, "Full_Helm",seed,0,0,0,new Item.Material[]{Item.Material.Titanium}){
            public String getDescription(){
                return super.getDescription()+" Something is odd about this helmet.";
            }
        };
        product.tags.add(Tag.Shadow);
        product.tags.add(Tag.Fire);
        product.tags.add(Tag.Air);
        return product;
    }};
    private static HashMap<String, String> extRegistry = new HashMap<>();
    public static Prop get(com.ga.map.Map map, String p){
        Prop t;
        String[] sections = p.split("@");
        int subId = Integer.parseInt(sections[1]);
        int seed = Creation.getSeed(Integer.parseInt(sections[2]));
        int index = Integer.parseInt(sections[0]);
        t = values()[index].make(map, subId, seed);
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
        String name = extRegistry.get(p);
        if (name != null){
            String[] data = name.split("@");
            return Stock.valueOf(data[0]).create(map, Integer.parseInt(data[1]), -1);
        }
        return Stock.Object.create(map, 0,0);
    }
    public Prop create(Map map, int subId, int seed){
        Prop r = make(map, subId, Creation.getSeed(seed));
        r.factoryId = ordinal();
        r.subId = subId;
        r.onCreate();
        return r;
    }
    abstract Prop make(Map map, int subid, int seed);
    public String getName(){
        return name().replace("_"," ");
    }
    public static String getFactoryName(int id){
        return id==-1?"":values()[id].getName();
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
        p.manualDepth = true;
        p.save = false;
        isFurniture(p);
        return p;
    }
    static void randomSide(Prop p){
        p.dir = p.seed % 2 == 0 ? 1 : -1;
    }
    static String getUnit(String name, int seed, String clip, String stat, Faction faction, String intel, String moves){
        return name+"@-1@"+seed+"@0,0@0@"+Global.getControlId()+"@"+Global.player.map.name+"@"+clip+"@"+stat+"@"+faction.name()+"@"+intel+"@"+moves;
    }
    private static String getItem(Map map, String p, int piece){
        if (p == null || p.equals("None")){
            if (piece == -1) return "";
            else return "None";
        }
        String pieceName = "";
        if (piece == 1) pieceName = "_Legging";
        else if (piece == 2) pieceName = "_Torso";
        Prop product = Stock.getByName(map, p+pieceName,-1);
        if (product instanceof Item) {
            Item r = (Item) product;
            return r.toString();
        } else {
            Global.p("["+p+"] is not an Item");
            return "None";
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