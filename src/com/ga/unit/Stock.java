package com.ga.unit;

import com.ga.game.FileManager;
import com.ga.game.Sound;
import com.ga.map.Creation;
import com.ga.map.Global;
import com.ga.map.Updater;
import com.ga.tool.Interactive;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.item.*;
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
    Spawn{ protected Prop make(int seed){
        Prop product = new Prop(getName(), seed);
        product.manualDepth = true;
        product.mobile = false;
        product.image.visible = false;
        return process(product);
    }},
    Travel{ Prop make(final int seed){
        Prop product = new Prop(getName(), seed);
        product.manualDepth = true;
        product.mobile = false;
        product.image.visible = false;
        product.onInteract = new Interactive(){
            public void interact(Man p){
                if (p == Global.player) Global.map.loadMap(seed/100+"@"+Math.abs(seed)%100);
            }
        };
        return process(product);
    }},
    Weapon{ Prop make(int seed){
        String[] data = FileManager.readText("raw/Weapon.txt");
        final String[] v = data[seed % data.length].split(";");
        Prop product = new Weapon(v[0], Creation.getSeed(seed), Item.ItemType.valueOf(v[1]),
                Integer.parseInt(v[2]), Item.Material.load(v[3].split(",")));
        product.factoryId = ordinal();
        return process(product);
    }},
    Armor{ Prop make(int seed){
        String[] data = FileManager.readText("raw/Armor.txt");
        final String[] v = data[seed % data.length].split(";");
        int piece = Integer.parseInt(v[1]);
        if (piece == 1 || piece == 2) v[0] = v[0].substring(0,v[0].indexOf("_"));
        Prop product = new Armor(v[0], Creation.getSeed(seed), piece,
                Integer.parseInt(v[2]), Integer.parseInt(v[3]), Item.Material.load(v[4].split(",")));
        product.factoryId = ordinal();
        return process(product);
    }},
    Object{ Prop make(int seed){
        String[] data = FileManager.readText("raw/Object.txt");
        final String[] v = data[seed % data.length].split(";");
        final Prop product = new Prop(v[0], seed);
        product.gravity = v[2].equals("1");
        product.mobile = v[3].equals("1");
        product.randomSize(new P(v[4]));
        if (v[5].equals("1")) product.dir = product.seed % 2 == 0 ? 1 : -1;
        product.image.setCenterType(Integer.parseInt(v[1]));
        product.factoryId = ordinal();
        return process(product);
    }},
    Man{ Prop make(int seed){
        String[] units = FileManager.readText("raw/Man.txt");
        final String[] unit = units[seed % units.length].split(";");
        Man product = new Man(unit[0], Creation.getSeed(seed)){
            public void onCreate(){
                String str = getUnit(name,seed,"111111",getStat(Integer.parseInt(unit[2]),Integer.parseInt(unit[3]),Integer.parseInt(unit[4])),
                        Faction.valueOf(unit[5]),unit[6]+",0,0",unit[7]);
                String[] weapon = unit[8].split(",");
                String[] armor = unit[9].split(",");
                String[] face = unit[10].split(",");
                str += "#"+getItem(weapon[seed % weapon.length],-1);
                String helm = getItem(face[seed % face.length],0);
                String arm = armor[seed % armor.length];
                str += "#"+helm+"&"+getItem(arm, 2)+"&"+getItem(arm, 1);
                load(str);
            }
        };
        product.speechType = unit[1];
        product.factoryId = ordinal();
        return process(product);
    }},
    Beast{ Prop make(int seed) {
        String[] units = FileManager.readText("raw/Beast.txt");
        String[] unit = units[seed % units.length].split(";");
        Unit product = new Unit(null, Creation.getSeed(seed));
        product.clip = new Body(product, unit[0]);
        product.load(getUnit(unit[0],product.seed,product.clip.toString(),getStat(Integer.parseInt(unit[2]),Integer.parseInt(unit[3]),
                Integer.parseInt(unit[4])), Faction.valueOf(unit[5]), unit[6]+",0,0", unit[7]));
        product.speechType = unit[1];
        product.factoryId = Beast.ordinal();
        return process(product);
    }},
    Food{ Prop make(int seed){
        String[] data = FileManager.readText("raw/Food.txt");
        final String[] v = data[seed % data.length].split(";");
        Prop product = new Food(v[0], Creation.getSeed(seed), Integer.parseInt(v[1]),
                Integer.parseInt(v[2]), v[3], Integer.parseInt(v[4]), Integer.parseInt(v[5]));
        product.factoryId = ordinal();
        return process(product);
    }},
    Manual{ Prop make(int seed){
        Item product = new Item(getName(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Scroll){
            private Actions skill;
            private int level;
            public void onCreate(){
                super.onCreate();
                ArrayList<Actions> learnables = Actions.getLearnables();
                skill = learnables.get(seed % learnables.size());
                level = seed % 4;
            }
            public String[] load(String p){
                String[] r = super.load(p);
                ArrayList<Actions> learnables = Actions.getLearnables();
                skill = learnables.get(seed % learnables.size());
                level = seed % 4;
                return r;
            }
            @Override
            public void use(Man host){
                if (host instanceof Player) {
                    super.use(host);
                    Player player = (Player) host;
                    int difficulty = skill.getDifficulty();
                    if (player.getStatePower(skill) >= difficulty) {
                        if (player.skillTree.getSkillLevel(skill) == level) {
                            if (Updater.sleep(100) == 100) {
                                ((Player) host).learning = skill;
                                host.say("Now learning " + skill.getName() + "!");
                            }
                        } else host.say("Inappropriate manual level!");
                    } else host.say("Not skilled enough to learn "+skill.getName()+"!");
                }
            }
            public String getName(){
                String prefix = "Expert Level "+(level-2);
                if (level == 0) prefix = "Novice";
                else if (level == 1) prefix = "Intermediate";
                else if (level == 2) prefix = "Advanced";
                else if (level == 3) prefix = "Expert";
                return prefix + " " + super.getName()+" of "+skill.getName();
            }
            public String getDescription(){
                int difficulty = skill.getDifficulty();
                int power = Global.player.getStatePower(skill);
                int skillLevel = Global.player.skillTree.getSkillLevel(skill);
                String req = "Fields of study: ";
                for (Tag i : skill.getTrainers()) req += "[" + i.name() + "] ";
                req += "x Difficulty "+difficulty+".";
                req += " Your current level of expertise in this field: "+power+".";
                if (power >= difficulty) req += " You are capable of performing this action.";
                else req += " You lack the expertise to perform the technique described in this manual.";
                req += " Your current knowledge level of "+skill.name()+": "+skillLevel+".";
                req += " The book teaches level "+(level+1)+" "+skill.name();
                return super.getDescription()+" "+req;
            }
        };
        product.value = 94;
        product.delay = 45;
        return process(product);
    }},
    Bandage{ Prop make(int seed){
        Item product = new Item(getName(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Consumable) {
            public void use(Man host) {
                if (usable(1)){
                    super.use(host);
                    host.stats.triggerEffect(Buffs.Bleed);
                }
            }
            public String getDescription(){
                return super.getDescription()+" Use to stop bleeding.";
            }
        };
        product.delay = 45;
        return process(product);
    }},
    Potion{ Prop make(int seed){
        Item product = new Item(getName(), seed, new Item.Material[]{Item.Material.Glass}, Item.ItemType.Consumable) {
            public void onCreate() {
                if (seed != 0) imbueMeta(false, false, 1);
            }
            public void use(Man host) {
                if (usable(1)){
                    super.use(host);
                    host.buff(emod, ademod.level);
                }
            }
            public String getFullName(){
                if (!Global.player.hasTrait(Trait.Alchemist)) return getName();
                return super.getFullName();
            }
        };
        product.delay = 45;
        return process(product);
    }},
    Scroll{ Prop make(int seed){
        Item product = new Item(getName(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Scroll){
            private Actions spell;
            @Override
            public void use(Man host){
                if (usable(1)) {
                    super.use(host);
                    if (spell != null){
                        host.stateLevel = 3;
                        host.stateSet(spell);
                    }
                }
            }
            public void onCreate(){
                ArrayList<Actions> list = Actions.getByTag(Tag.Runic);
                spell = list.get(rand.nextInt(list.size()));
            }
            public String[] load(String p){
                String[] r = super.load(p);
                ArrayList<Actions> list = Actions.getByTag(Tag.Runic);
                spell = list.get(rand.nextInt(list.size()));
                return r;
            }
            public String getName(){
                if (!Global.player.hasTrait(Trait.Enchanter)) return super.getName();
                return super.getName()+" of "+spell.getName();
            }
        };
        product.value = 16;
        product.probability = 1;
        product.delay = 30;
        return process(product);
    }},
    Ethersword{ Prop make(int seed){
        Weapon product = new Weapon(getName(), seed, Item.ItemType.Sword, 0, new Item.Material[]{Item.Material.Crystal}){
            public String toString(){
                return "";
            }
            public void remove(){
                super.remove();
                Particle.holyExplosion(pos,getDepth());
            }
            public void trigger(Unit host) {
                super.trigger(host);
                Particle.holyExplosion(pos,getDepth());
            }
            public int getDamage(){
                return 3;
            }
        };
        product.weight = 0;
        product.image.setCenterType(3);
        return process(product);
    }},
    Phone_Booth{ Prop make(int seed){
        Prop product = new Prop(getName(), seed);
        product.onInteract = new Interactive(){
            public void interact(Man p){
                String t = p.getConversation();
                if (t.equals("")){
                    Global.server.online();
                    p.say("Waiting for a call...");
                } else if (t.toLowerCase().startsWith("call")) {
                    Global.server.requestConnection(p.getConversation().substring(5));
                    p.say("Connecting...");
                }
                p.setState(Actions.Hit);
            }
        };
        return process(product);
    }},
    Map{ Prop make(int seed){
        Prop product = new Prop(getName(), seed);
        product.onInteract = new Interactive(){
            public void interact(Man p){
//                Global.display.menu.showMap();
            }
        };
        return process(product);
    }},
    Fist{ Prop make(int seed){
        Prop product = new Weapon(getName(),0, Item.ItemType.Bashing, 0, new Item.Material[]{Item.Material.Flesh}){
            public void use(final Man host) {
                host.setState(Actions.Interact);
            }
            public String forceToString() {
                return "";
            }
            public void drop(Man host) {}
            public void onUnequip(Man host){}
            public void onCreate(){}
        };
        ((Item)product).setMaterial(Item.Material.Flesh);
        product.image.setCenterType(3);
        product.onInteract = null;
        return process(product);
    }},
    Signpost{ Prop make(int seed){
        final Prop travel = Stock.Travel.create(seed);
        travel.save = false;
        Prop product = new Prop(getName(), seed){
            public void onField(){
                super.onField();
                travel.pos.add(pos.getAdd(0,-image.getSize().y+image.getSize().y/8));
            }
        };
        isFurniture(product);
        return process(product);
    }},
    Dungeon{ Prop make(final int seed){
        final Prop entrance = Stock.Spawn.create(Global.houseId++);
        final Prop product = new Prop(getName(), seed){
            public void onField(){
                super.onField();
                entrance.pos.add(pos.x, pos.y);
            }
        };
        entrance.onInteract = new Interactive(){
            public void interact(Man p){
                if (p == Global.player && p.lastLand.depth == product.getDepth()+2){
                    Creation.prepareRandom(Integer.parseInt(Global.map.id+""+entrance.seed));
                    Global.map.loadMap("D_"+Global.map.id+"_"+entrance.seed+"@"+entrance.seed+"@0");
                }
            }
        };
        entrance.save = false;
        entrance.pos.set(0,-product.image.getSize().y/2);
        isFurniture(product);
        return process(product);
    }},
    SteamHouse{ Prop make(final int seed){
        final Prop spawn = Stock.Spawn.create(Global.houseId++);
        final Prop entrance = Stock.Travel.create(-(spawn.seed*100));
        final Prop product = new Prop(getName(), seed){
            public void onField(){
                super.onField();
                spawn.pos.add(pos.x, pos.y);
                entrance.pos.add(pos.x, pos.y);
                P s = image.getSize().getMult(.5f, 1).add(-12,-16);
                P top = new P(Math.sin(rotation)*s.y, Math.cos(rotation)*s.y);
                P width = new P(Math.sin(rotation)*s.x, Math.cos(rotation)*s.x);
                Global.map.land.add(new Line(pos.x+top.x-width.y, pos.y-top.y-width.x,
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
        return process(product);
    }},
    TowerHouse{ Prop make(final int seed){
        final ArrayList<Prop> addons = new ArrayList<>();
        final Prop entrance = Stock.Spawn.create(Global.houseId++);
        final Prop product = new Prop("TowerHouse", seed){
            public void onField(){
                super.onField();
                Updater.addQueue.add(entrance);
                for (Prop i : addons){
                    i.onField();
                    i.pos.add(pos.x, pos.y);
                }
                P s = image.getSize().getMult(.5f, 1).add(-16,-4);
                Global.map.land.add(new Line(pos.x-s.x, pos.y-s.y,
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
        Prop door = makeAddon("Door", product.rand);
        int doorWidth = (int) (baseSizeX-door.image.getSize().x);
        P doorPos = new P(product.rand.nextInt(doorWidth)-(doorWidth)/2, -8);
        door.pos = doorPos;
        addons.add(door);
        int houseType = product.rand.nextInt(16);
        if (houseType == 1){
            Prop sign = makeAddon("Sign_Food", product.rand);
            sign.pos.set(doorPos.x<0?doorPos.x+128:doorPos.x-128, doorPos.y);
            addons.add(sign);
        } else if (houseType == 2){
            Prop sign = makeAddon("Sign_Shop", product.rand);
            sign.pos.set(doorPos.x<0?doorPos.x+128:doorPos.x-128, doorPos.y);
            addons.add(sign);
        } else if (houseType == 3){
            Prop sign = makeAddon("Sign_Guild", product.rand);
            sign.pos.set(doorPos.x<0?doorPos.x+128:doorPos.x-128, doorPos.y);
            addons.add(sign);
        } else if (houseType == 4){
            Prop sign = makeAddon("Sign_Training", product.rand);
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
                Prop window = makeAddon("Window", product.rand);
                window.pos = new P(xmargin*(ix+1)-(baseSizeX/2), -ymargin*(iy+1)-door.image.getSize().y/2);
                addons.add(window);
            }
        }
        if (product.rand.nextBoolean()) {
            for (int i = 0; i < product.rand.nextInt(3); i++) {
                Prop appliance = makeAddon("Appliance", product.rand);
                appliance.pos = new P(product.rand.nextInt((int) baseSizeX)-baseSizeX/2, -product.rand.nextInt((int) baseSizeY-64));
                addons.add(appliance);
            }
        }
        for (int i=0;i<product.rand.nextInt(3);i++) {
            Prop clockwork = new Prop("Clockwork", seed){
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
                    Global.map.loadMap("I_"+Global.map.id+"_"+entrance.seed+"@"+entrance.seed+"@"+product.image.getSize().getAdd(64,64)+"@"+finalHouseType+"@"+ywindows);
                }
            }
        };
        return process(product);
    }},
    TowerTip{ Prop make(int seed){
        final Prop fan = new Prop("Propeller",seed){
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
        Prop product = new Prop(getName(), seed){
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
        return process(product);
    }},
    Stack{ Prop make(int seed) {
        Prop product = new Prop(getName(), seed){
            public void onField(){
                super.onField();
                P s = image.getSize().getMult(.5f, 1).add(-8,-4);
                P top = new P(Math.sin(rotation)*s.y, Math.cos(rotation)*s.y);
                P width = new P(Math.sin(rotation)*s.x, Math.cos(rotation)*s.x);
                Global.map.land.add(new Line(pos.x+top.x-width.y, pos.y-top.y-width.x,
                        pos.x+top.x+width.y, pos.y-top.y+width.x, -1, 0, lastLand.depth){
                    public String toString(){
                        return "";
                    }
                });
            }
        };
        product.image.setCenterType(2);
        return process(product);
    }},
    Barrel{ Prop make(int seed) {
        Prop product = new Prop(getName(), seed){
            public void onField(){
                super.onField();
                P s = image.getSize().getMult(.5f, 1).add(0,-4);
                P top = new P(Math.sin(rotation)*s.y, Math.cos(rotation)*s.y);
                P width = new P(Math.sin(rotation)*s.x, Math.cos(rotation)*s.x);
                Global.map.land.add(new Line(pos.x+top.x-width.y, pos.y-top.y-width.x,
                        pos.x+top.x+width.y, pos.y-top.y+width.x, -1, 0, lastLand.depth-1){
                    public String toString(){
                        return "";
                    }
                });
            }
        };
        product.image.setCenterType(2);
        return process(product);
    }},
    Crate{ Prop make(int seed){
        if (seed < 100000) seed *= 100000;
        final Prop product = new Prop(getName(), seed){
            public void onField(){
                super.onField();
                P s = image.getSize().getMult(.5f, 1).add(0,-4);
                P top = new P(Math.sin(rotation)*s.y, Math.cos(rotation)*s.y);
                P width = new P(Math.sin(rotation)*s.x, Math.cos(rotation)*s.x);
                Global.map.land.add(new Line(pos.x+top.x-width.y, pos.y-top.y-width.x,
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
                            products[i] = ((Item) getByName(item, itemSeed)).cancelAdd();
                            final int current = i;
                            actions[i] = new Performer() {
                                public void perform() {
                                    String newIndex = finalIndex.substring(0, current) + '2' + finalIndex.substring(current + 1);
                                    product.seed = Integer.parseInt(productSeed.substring(0, productSeed.length() - 5) + newIndex);
                                    Updater.addQueue.add(products[current]);
                                }
                            };
                        }
                    }
                    Global.display.menu.showContainer("Crate", products, actions);
                }
            }
        };
        product.image.setCenterType(2);
        return process(product);
    }},
    Throwing_Knife{ Prop make(int seed){
        Item product = new Item("Tanto", seed,
                new Item.Material[]{Item.Material.Steel, Item.Material.Bone, Item.Material.Bronze, Item.Material.Titanium, Item.Material.Glass},
                Item.ItemType.Darting){
            @Override
            public void use(final Man host){
                if (usable(1)) {
                    super.use(host);
                    host.delay.performer = new Performer() {
                        public void perform() {
                            final Projectile p = new Projectile("Tanto", host.pos.getAdd(host.clip.organs[BodyHumanoid.OFIST].ptd), host.pos.getAdd(host.target), 70) {
                                @Override
                                public void stuck() {
                                    if (tangible) Particle.explode("Fire", getDepth(), pos, vel, 20, 10, 15, true, 3);
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
                }
            }
        };
        product.image.setCenterType(3);
        product.value = 16;
        product.delay = 10;
        return process(product);
    }},
    Fireball{ Prop make(int seed){
        Item product = new Item(getName(), seed, new Item.Material[]{Item.Material.Stone}, Item.ItemType.Throwing) {
            @Override
            public void use(final Man host) {
                if (usable(1)) {
                    super.use(host);
                    host.delay.performer = new Performer() {
                        public void perform() {
                            final Projectile p = new Projectile(getName(), host.pos.getAdd(host.clip.organs[BodyHumanoid.FIST].ptd),
                                    host.pos.getAdd(host.target), 0) {
                                @Override
                                public void stuck() {
                                    Particle.explosion(pos, getDepth());
                                    ArrayList<Unit> t = Updater.collideUnit(pos,96,Faction.Destructive);
                                    for (Unit i : t){
                                        i.checkHit(pos, getDamage());
                                    }
                                    remove();
                                }

                                @Override
                                public void update() {
                                    super.update();
                                    Particle.explode("Fire", getDepth(), pos, new P(0, -1), 6, 1, 4, true, 3);
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
                }
            }
        };
        product.value = 16;
        product.delay = 10;
        return process(product);
    }},
    Threadball{ Prop make(int seed){
        Item product = new Item(getName(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Darting) {
            @Override
            public void use(final Man host) {
                if (usable(1)) {
                    super.use(host);
                    final Item me = this;
                    host.delay.performer = new Performer() {
                        public void perform() {
                            final Projectile p = new Projectile(getName(), host.pos.getAdd(host.clip.organs[BodyHumanoid.FIST].ptd),
                                    host.pos.getAdd(host.target), 48+host.get("Strength").get()*16) {
                                private BasicStroke stroke = new BasicStroke(4);
                                public void onAttack(Unit p){
                                    pos = p.pos;
                                }
                                public void stuck() {
                                    super.stuck();
                                    Particle.groundSlam(pos, getDepth());
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
                }
            }
        };
        product.value = 16;
        product.delay = 10;
        return process(product);
    }},
    Book{ Prop make(int seed){
        Item product = new Item(getName(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Scroll){
            @Override
            public void use(Man host){
                super.use(host);
                Global.display.menu.showDialog("Title","Content");
            }
        };
        product.delay = 45;
        return process(product);
    }},
    Merchant{ Prop make(int seed){
        Man product = getMan(getName(),seed,getStat(4,10,1),Faction.Neutral,"2,0,0","200/Sprint",
                new String[]{null},
                new String[]{"Suit"},new String[]{null});
        product.speechType = "merchant";
        return process(product);
    }},
    Phantom{ Prop make(int seed){
        Man product = getMan("Phantom",seed,getStat(30,28,5),Faction.Evil,"3,0,0","10,20,120,1200/Use,Dash&Flameroll,Fireblast,Shadowstep&Blizzard",
                new String[]{"Greatsword","Halberd","Warhammer"},
                new String[]{"Shadowrobe"},new String[]{null});
        product.clip.organs[5].setImage("Phantom");
        return process(product);
    }},
    Rover{ Prop make(int seed) {
        Unit product = new Unit(getName(), seed){
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
                ArrayList<Unit> t = Updater.collideUnit(pos,48,faction);
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
                load(getUnit(name,seed,clip.toString(),getStat(3,12,3),Faction.Evil,"3,0,0","140,1200/Melee&Walk,Walk&Sprint"));
            }
        };
        processUnit(product);
        return process(product);
    }},
    Slime { Prop make(int seed) {
        Unit product = new Unit(getName(), seed){
            public void stateSet(Actions state) {
                super.stateSet(state);
                if (state == Actions.Melee) {
                    delay.time = 60;
                }
            }
            public void moveTriggered(int p){
                super.moveTriggered(p);
                if (state == Actions.Melee){
                    Particle.explode("Poison",getDepth(),pos,new P(),8,8,0,false,4);
                    ArrayList<Unit> t = Updater.collideUnit(pos, (int) getReach(), Faction.Destructive);
                    for (Unit i : t){
                        i.checkHit(pos, getDamage());
                        i.buff(Buffs.Poison, 1);
                        i.vel.x = (i.pos.x > i.pos.x ? -1 : 1)*6;
                        i.vel.y -= 16;
                        Sound.play(pos, "drink");
                    }
                }
            }
            public int getDamageReduction(int p){
                return super.getDamageReduction(p)+3;
            }
            public void buff(Buffs buff, int level){
                if (buff == Buffs.Bleed) return;
                if (buff.tags.has(Tag.Cold)) level *= 8;
                super.buff(buff, level);
            }
            public void onCreate(){
                load(getUnit(getName(),seed,clip.toString(),getStat(7,6,1),Faction.Evil,"3,0,0","8,800/Melee,Walk"));
            }
        };
        product.loot = new Item[]{(Item) Threadball.create(-1)};
        product.tags.remove(Tag.Combustible);
        processUnit(product);
        product.clip.organs[0].setSize((int) product.clip.organs[0].getImage().getSize().x);
        return process(product);
    }},
    Bird { Prop make(int seed) {
        Unit product = new Unit(getName(), seed){
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
                if (buff.tags.has(Tag.Cold)) level *= 8;
                if (buff.tags.has(Tag.Fire)) level *= 8;
                super.buff(buff, level);
            }
            public void onCreate(){
                load(getUnit(getName(),seed,clip.toString(),getStat(14,10,1),Faction.Evil,"3,0,0","80,800/Shock&Jump,Jump"));
            }
        };
        processUnit(product);
        product.clip.organs[0].setSize((int) product.clip.organs[0].getImage().getSize().x);
        return process(product);
    }},
    Beholder { Prop make(int seed) {
        Unit product = new Unit(getName(), seed){
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
                    for (Line i : Global.map.land) {
                        if (i.lineCross(new Line(pos, pos.getAdd(0, 200)))) {
                            vel.y -= .16f;
                            break;
                        }
                    }
                }
            }
            public void onCreate(){
                load(getUnit(getName(),seed,clip.toString(),getStat(30,10,3),Faction.Evil,"3,0,0","300/Fireblast&Idle&Idle&Idle"));
            }
        };
        if (seed % 2 == 1) product.loadActions("200,800/Flameroll,Flameroll&Fireball");
        product.gravity = false;
        processUnit(product);
        return process(product);
    }},
    //Uniques
    Stomach_Full_Helm{ Prop make(int seed){
        Armor product = new Armor("Full Helm",seed,0,0,0,new Item.Material[]{Item.Material.Titanium}){
            public String getDescription(){
                return super.getDescription()+" Something is odd about this helmet.";
            }
        };
        product.tags.add(Tag.Cunning);
        product.tags.add(Tag.Fire);
        product.tags.add(Tag.Air);
        return process(product);
    }};
    private static HashMap<String, String> extRegistry = new HashMap<>();
    public static Prop get(String p){
        Prop t;
        String[] sections = p.split("@");
        int seed = Creation.getSeed(Integer.parseInt(sections[1]));
        char firstchar = sections[0].charAt(0);
        if (firstchar >= 48 && firstchar <= 57){
            int index = Integer.parseInt(sections[0]);
            t = values()[index].make(seed);
            t.load(p);
        } else {
            t = getInstant(sections[0], seed);
            t.load(p);
        }
        return t;
    }
    public static Prop getByName(String p, int seed){ // Return to reflection
        for (Stock i : values()){
            if (i.name().equals(p.replace(" ","_"))){
                return i.create(seed);
            }
        }
        String name = extRegistry.get(p);
        if (name != null){
            String[] data = name.split("@");
            String[] units = FileManager.readText("raw/"+data[0]+".txt");
            return Stock.valueOf(data[0]).create(Creation.getSeed(-1, units.length, Integer.parseInt(data[1])));
        }
        return getInstant(p, seed);
    }
    public static int getRawStock(String raw, String p){
        String[] units = FileManager.readText("raw/"+raw+".txt");
        for (int i=0;i<units.length;i++){
            if (units[i].substring(0,units[i].indexOf(";")).equals(p)){
                return Creation.getSeed(-1, units.length, i);
            }
        }
        return -1;
    }
    public Prop create(int seed){
        Prop r = make(Creation.getSeed(seed));
        r.onCreate();
        return r;
    }
    public static Prop getInstant(String p, int seed){
        Prop t = new Prop(p, Creation.getSeed(seed));
        t.gravity = false;
        t.image.setCenterType(2);
        randomSide(t);
        return t;
    }
    abstract Prop make(int seed);
    public String getName(){
        return name().replace("_"," ");
    }
    public static String getFactoryName(int id){
        return id==-1?"":values()[id].getName();
    }
    Prop process(Prop product){
        product.factoryId = ordinal();
        return product;
    }
    void isFurniture(Prop product){
        product.image.setCenterType(2);
        product.gravity = true;
        product.mobile = false;
        randomSide(product);
    }
    Prop makeAddon(String name, Random rand){
        return makeAddon(new Prop(name,rand.nextInt(9999)));
    }
    Prop makeAddon(Prop p){
        p.manualDepth = true;
        p.save = false;
        Updater.addQueue.remove(p);
        isFurniture(p);
        return p;
    }
    static void randomSide(Prop p){
        p.dir = p.seed % 2 == 0 ? 1 : -1;
    }
    static String getStat(int stamina, int speed, int level){
        return "Stamina:"+stamina+"&Attack:0,0&Speed:"+speed+"&Strength:"+level+"&Level:"+level;
    }
    static String getUnit(String name, int seed, String clip, String stat, Faction faction, String intel, String moves){
        return name+"@"+seed+"@0,0@0@"+clip+"@"+stat+"@"+faction.name()+"@"+intel+"@"+moves;
    }
    void processUnit(Unit p){
        p.clip = new Body(p, getName());
    }
    static Man getMan(String name, int seed, final String stat, final Faction pfaction, final String intel, final String moves, final String[] pweapon, final String[] parmor, final String[] pface){
        Man m = new Man(name, seed){
            public void onCreate(){
                String str = getUnit(name,seed,"111111",stat,pfaction,intel,moves);
                str += "#"+getItem(pweapon[seed % pweapon.length],-1);
                String helm = getItem(pface[seed % pface.length],0);
                String arm = parmor[seed % parmor.length];
                str += "#"+helm+"&"+getItem(arm, 2)+"&"+getItem(arm, 1);
                load(str);
            }
        };
        return m;
    }
    private static String getItem(String p, int piece){
        if (p == null || p.equals("None")){
            if (piece == -1) return "";
            else return "None";
        }
        String pieceName = "";
        if (piece == 1) pieceName = "_Legging";
        else if (piece == 2) pieceName = "_Torso";
        Item r = (Item) Stock.getByName(p+pieceName,-1);
        r.cancelAdd();
        return r.toString();
    }
    public static void init(){
        for (String raw : new String[]{"Object","Man","Beast","Weapon","Armor","Food"}){
            String[] data = FileManager.readText("raw/"+raw+".txt");
            for (int i=0;i<data.length;i++)
                extRegistry.put(data[i].substring(0,data[i].indexOf(";")), raw+"@"+i);
        }
    }
}