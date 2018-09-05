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
import com.ga.unit.skill.Stat;

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
            public void load(T p){
                super.load(p);
                name = p.get(5).get();
            }
            public String toString(){
                return super.toString() + "," + name;
            }
        };
        product.name = "Travel";
        product.image.set(Picture.loadCache("Move"));
        product.manualDepth = true;
        product.mobile = false;
        product.image.visible = false;
        product.onInteract = new Interactive(){
            public void interact(com.ga.unit.man.Man p){
                if (p == Global.player) Global.server.requestChangeMap(product.name+","+seed);
            }
        };
        return product;
    }},
    Info{ Prop make(Map map, final int id, final int seed){
        Prop product = new Prop(map, null, seed){
            public void load(T p){
                super.load(p);
                name = p.get(5).get();
            }
            public String toString(){
                return super.toString() + "," + name;
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
        T data = FileManager.readLiteral("Data/raw/Object.txt", id);
        final Prop product;
        if (!data.get(6).equals("0")) {
            product = new Prop(map, data.get(0).get(), seed){
                public void onField(){
                    super.onField();
                    int offset = -data.get(6).getInt();
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
        } else product = new Prop(map, data.get(0).get(), seed);
        product.subId = id;
        product.gravity = data.get(2).equals("1");
        product.manualDepth = !product.gravity;
        product.mobile = data.get(3).equals("1");
        product.randomSize(new P(data.get(4)));
        if (data.get(5).equals("1")) product.dir = product.seed % 2 == 0 ? 1 : -1;
        product.image.setCenterType(data.get(1).getInt());
        return product;
    }},
    Beast{ Prop make(Map map, int id, int seed) {
        T units = FileManager.readLiteral("Data/raw/Beast.txt", id);
        Unit product = new Unit(map, null, seed){
            public void onCreate(){
                UnitFactory unitString = new UnitFactory(this, Faction.valueOf(units.get(3).get()), units.get(2));
                unitString.setMoveset("Melee");
            }
        };
        product.clip = new Body(product, units.get(0).get());
        product.speechType = units.get(1).get();
        product.subId = id;
        return product;
    }},
    Man{ Prop make(Map map, int id, int seed){
        T data = FileManager.readLiteral("Data/raw/Man.txt", id);
        com.ga.unit.man.Man product = new Man(map, data.get(0).get(), seed){
            public void onCreate(){
                UnitFactory unitString = new UnitFactory(this, Faction.valueOf(data.get(5).get()), data.get(4));
                unitString.setAI(data.get(6).get(0).getInt(), data.get(6).get(1).getInt());
                unitString.setMoveset(data.get(7).get());
                unitString.addInventory(data.get(11));
                unitString.setEquipment(data.get(8), data.get(9), data.get(10));
                traits.load(this, data.get(12));
            }
        };
        if (!data.get(2).get().isEmpty()) product.clip.setImage(data.get(2).get());
        if (!data.get(1).get().isEmpty()) product.setFace(data.get(1).get());
        else product.setFace("Man");
        product.speechType = data.get(3).get();
        product.subId = id;
        return product;
    }},
    Etc{ Prop make(Map map, int id, int seed){
        T data = FileManager.readLiteral("Data/raw/Etc.txt", id);
        Item product = new Item(map, data.get(0).get(), seed, null, Item.ItemType.Swinging){
            public void onCreate(){
                super.onCreate();
                imbueMaterial(Item.Material.load(data.get(4)));
            }
            public String getDescription(){
                String r = super.getDescription();
                if (data.size() > 6) r += data.get(6).get();
                return r;
            }
        };
        for (T i : data.get(5)) product.tags.add(Tag.valueOf(i.get()));
        product.weight = Integer.parseInt(data.get(2).get());
        product.value = Integer.parseInt(data.get(3).get());
        product.dir = product.seed % 2 == 0 ? 1 : -1;
        product.image.setCenterType(Integer.parseInt(data.get(1).get()));
        product.subId = id;
        return product;
    }},
    Weapon{ Prop make(Map map, int id, int seed){
        T data = FileManager.readLiteral("Data/raw/Weapon.txt", id);
        Item product = new Weapon(map, data.get(0).get(), seed, null, Item.ItemType.valueOf(data.get(1).get())){
            public void onCreate(){
                super.onCreate();
                imbueMaterial(Item.Material.load(data.get(2)));
            }
        };
        for (T i : data.get(3)) product.tags.add(Tag.valueOf(i.get()));
        product.subId = id;
        return product;
    }},
    Armor{ Prop make(Map map, int id, int seed){
        T data = FileManager.readLiteral("Data/raw/Armor.txt", id);
        int piece = Integer.parseInt(data.get(1).get());
        String name = data.get(0).get();
        if (piece == 1 || piece == 2) name = data.get(0).get().substring(0,data.get(0).get().lastIndexOf("_"));
        Prop product = new Armor(map, name, seed, null, piece,
                Integer.parseInt(data.get(2).get()), Integer.parseInt(data.get(3).get()), Integer.parseInt(data.get(5).get()), Integer.parseInt(data.get(6).get())){
            public void onCreate(){
                super.onCreate();
                imbueMaterial(Item.Material.load(data.get(4)));
            }
        };
        product.subId = id;
        return product;
    }},
    Food{ Prop make(Map map, int id, int seed){
        T data = FileManager.readLiteral("Data/raw/Food.txt", id);
        Prop product = new Food(map, data.get(0).get(), seed, data.get(1).getInt()){
            public void onCreate(){
                super.onCreate();
                set(data.get(2).getInt(),data.get(3).getInt(),data.get(4).getInt());
            }
        };
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
                UnitFactory unitString = new UnitFactory(this, Faction.Predator, new T("3,2,1"));
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
                UnitFactory unit = new UnitFactory(this, Faction.Predator, new T("1,1,1"));
                unit.setMoveset("Melee");
                stats.speed = 8;
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
                    stats.strength.heal();
                } else {
                    if (state == Actions.Melee) state = Actions.Shock;
                    super.stateSet(state);
                }
            }
            public void buff(Buffs buff, int level){
                super.buff(buff, level*2);
            }
            public void die(String p){
                Stock.createByName(map, "Bird_Meat", -1).start().pos.set(pos);
                super.die(p);
            }
            public void onCreate(){
                UnitFactory unitString = new UnitFactory(this, Faction.Predator, new T("3,5,3"));
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
                UnitFactory unit = new UnitFactory(this, Faction.Predator, new T("3,4,9"));
                stats.intelligence.setTotal(20, true);
                traits.learn(this, Traits.Trait.Affinity);
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
                UnitFactory unitString = new UnitFactory(this, Faction.Predator, new T("9,9,9"));
                stats.agility.setTotal(40, true);
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
                UnitFactory unitString = new UnitFactory(this, Faction.Predator, new T("7,10,2"));
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
                    pj.rotateTo(target.getSubtract(tip.getSubtract(pos)));
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
        Item product = new Weapon(map, name(),0, Item.Material.Organic, Item.ItemType.Throwing){
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
        Item product = new Item(map, name(),0, Item.Material.Gold, Item.ItemType.Bashing, 100000){
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
        Item product = new Item(map, name(), seed, Item.Material.Cloth, Item.ItemType.Scroll, 1){
            private Actions skill;
            public void onCreate(){
                super.onCreate();
                ArrayList<Actions> learnables = Actions.getLearnables();
                skill = learnables.get(seed % learnables.size());
            }
            public void load(T p){
                super.load(p);
                ArrayList<Actions> learnables = Actions.getLearnables();
                skill = learnables.get(seed % learnables.size());
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
                            if (host.stats.motivation.require(10)) {
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
        Item product = new Item(map, name(), seed, Item.Material.Cloth, Item.ItemType.Scroll, -1){
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
    Book{ Prop make(Map map, final int id, int seed){
        T data = FileManager.readLiteral("Data/raw/Book.txt", id);
        Item product = new Item(map, name(), seed, Item.Material.Cloth, Item.ItemType.Scroll, 4){
            public boolean use(Man host){
                if (super.use(host)){
                    String[] recipes = data.get(1).get().split(";");
                    boolean learned = false;
                    for (String i : recipes){
                        if (!host.crafting.contains(i)){
                            host.crafting.add(i);
                            learned = true;
                        }
                    }
                    if (learned) host.say("Learned new crafting recipes!");
                    return true;
                }
                return false;
            }
        };
        product.name = data.get(0).get();
        product.value = 2000;
        return product;
    }},
    Potion{ Prop make(Map map, int id, int seed){
        Item product = new Item(map, name(), seed, Item.Material.Glass, Item.ItemType.Consumable, 1) {
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
            public void load(T p){
                super.load(p);
                value = 100 + emod.value*ademod.level*100;
            }
        };
        product.range = 600;
        return product;
    }},
    Scroll{ Prop make(Map map, int id, int seed){
        Item product = new Item(map, name(), seed, Item.Material.Cloth, Item.ItemType.Scroll, 1){
            Actions spell;
            @Override
            public boolean use(com.ga.unit.man.Man host){
                if (super.use(host) && isUsable(true, 1)) {
                    if (spell != null){
                        host.buff(Buffs.Affinity, 1);
                        host.stateLevel = 3;
                        host.stateSet(spell);
                        return true;
                    }
                }
                return false;
            }
            private void makeSpell(){
                ArrayList<Actions> list = Actions.getScribables();
                spell = list.get(seed % list.size());
                value = 200 + 200*(spell.getDifficulty() * spell.getTrainers().size());
            }
            public void onCreate(){
                makeSpell();
            }
            public void load(T p){
                super.load(p);
                makeSpell();
            }
            public String getName(){
                if (!Global.player.traits.has(Traits.Trait.Literacy)) return super.getName();
                return super.getName()+" of "+spell.name();
            }
        };
        product.probability = 1;
        product.range = 400;
        return product;
    }},
    Ethersword{ Prop make(Map map, int id, int seed){
        Weapon product = new Weapon(map, name(), seed, Item.Material.Crystal, Item.ItemType.Sword){
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
            public void load(T p){
                super.load(p);
                name = p.get(5).get();
            }
            public String toString(){
                return super.toString() + "," + name;
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
        T data = FileManager.readLiteral("Data/raw/Loot.txt", id);
        final Prop product = new Prop(map, data.get(0).get(), seed){
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
                    boolean openable = data.get(1).get().isEmpty();
                    if (!openable){
                        L<Prop> items = p.getAvailableItems();
                        L<Prop> validItems = new L<>();
                        for (T i : data.get(1)) {
                            for (Prop item : items){
                                if (item.name.equals(i.get())) validItems.add(item);
                            }
                        }
                        Prop item = validItems.getFirst();
                        openable = item != null && ((Item)item).isUsable(true, 1);
                    }
                    if (openable) {
                        String loot = data.get(2).get(seed % data.get(2).size()).get();
                        Global.player.setWeapon((Item) createByName(map, loot, -1).start());
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
            public void load(T p){
                super.load(p);
                if (p.size() > 4) store = p.get(4).get();
            }
            public String toString(){
                String extra = store==null ? "" : ",[" + store + "]";
                return super.toString() + extra;
            }
        };
        product.image.setCenterType(2);
        return product;
    }},
    Scrap{ Prop make(Map map, int id, int seed){
        Item product = new Item(map,name(),seed,null,Item.ItemType.Throwing, 1){
            public void onCreate(){
                imbueMaterial(Item.Material.values());
            }
            public void setMaterial(Material p){
                super.setMaterial(p);
                name = "Scrap_"+p.name();
            }
        };
        product.disposable = false;
        return product;
    }},
    Repairkit{ Prop make(Map map, int id, int seed){
        int level = (seed % 9)+1;
        Item product = new Item(map, name(),seed,null,Item.ItemType.Scroll,20*level){
            public void onCreate(){
                imbueMaterial(Material.values());
            }
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
        int damage = 9;
        Item product = new Weapon(map, name(), seed, null, Item.ItemType.Shoot){
            public void onCreate(){
                imbueMaterial(new Item.Material[]{Item.Material.Wooden, Item.Material.Bone, Item.Material.Crystal, Item.Material.Titanium});
            }
            public String getDescription(){
                return super.getDescription() + " The projectile from this weapon deals "+(damage+getDamage())+" extra damage.";
            }
            public boolean use(final com.ga.unit.man.Man host){
                if (super.use(host)) {
                    Item thisItem = this;
                    if (isUsable(true, 1)) {
                        host.clip.setState("Shoot_Arrow");
                        host.delay.time = Math.max(0, 70 - host.getAttackSpeed());
                        host.weapon.rotateTo(host.target.getMult(host.dir, host.dir));
                        host.delay.performer = new Performer() {
                            public void perform() {
                                P tip = host.weapon.getLine().ptd;
                                final Item wpn = host.weapon;
                                int str = host.stats.strength.getMax();
                                int speed = (int) (40 + Global.clamp(str * 12 + 20, 48, 120) - (SyncRand.r.nextFloat() * (44 / str)));
                                final Projectile pj = new Projectile(host.map, "Arrow", tip, host.getHand().getAdd(host.target), speed) {
                                    public void onField() {}
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
                                pj.damage = damage + getDamage();
                                host.weapon.rotation += host.dir * .3f;
                                host.setState(Actions.Idle);
                            }
                        };
                        return true;
                    } else {
                        L<Prop> items = host.getAvailableItems();
                        items.removeIf(e -> !(e instanceof Item) || !e.name.equals("Quiver") || ((Item) e).uses == 0);
                        Item item = (Item) items.getFirst();
                        if (item != null && item.isUsable(true, 1)) {
                            host.clip.setState("Wield");
                            host.delay.time = 20;
                            host.delay.performer = new Performer() {
                                public void perform() {
                                    thisItem.isUsable(true, -1);
                                    host.stateSet(Actions.Idle);
                                }
                            };
                        } else host.say("Out of arrows!");
                    }
                }
                return false;
            }
        };
        product.durability = product.uses = 1;
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
        Item product = new Weapon(map, name, seed, Item.Material.Bronze, Item.ItemType.Shoot){
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
        product.range = clipRange*60;
        return product;
    }},
    Throwing_Knife{ Prop make(Map map, int id, int seed){
        Item product = new Item(map, "Tanto", seed, null, Item.ItemType.Darting, 10){
            public void onCreate(){
                imbueMaterial(new Item.Material[]{Item.Material.Steel, Item.Material.Bone, Item.Material.Copper, Item.Material.Iron, Item.Material.Titanium, Item.Material.Glass});
            }
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
        Item product = new Item(map, name(), seed, Item.Material.Stone, Item.ItemType.Throwing, 1) {
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
        Item product = new Item(map, name(), seed, Item.Material.Iron, Item.ItemType.Throwing, 5) {
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
        Item product = new Item(map, name(),seed,Item.Material.Copper,Item.ItemType.Consumable,10){
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
        return new Item(map, name,seed,Item.Material.Organic,Item.ItemType.Consumable,((seed % 5)*2)+1) {
            public String getDescription() {
                return super.getDescription() + "\nDrink to restore Energy into your body.";
            }
            @Override
            public boolean use(Man p) {
                if (super.use(p) && isUsable(true, 1)) {
                    p.stats.intelligence.heal();
                    return true;
                }
                return false;
            }
        };
    }},
    Bandage{ Prop make(Map map, int id, int seed){
        return new Item(map, name(), seed, Item.Material.Cloth, Item.ItemType.Consumable, 4) {
            public boolean use(com.ga.unit.man.Man host) {
                if (super.use(host) && isUsable(true, 1)){
                    host.status.triggerEffect(Buffs.Bleed);
                    return true;
                }
                return false;
            }
            public String getDescription(){
                return super.getDescription()+" Use to stop bleeding.";
            }
        };
    }},
    Vitamin{ Prop make(Map map, final int id, int seed){
        Item product = new Item(map, name(), seed, Item.Material.Organic, Item.ItemType.Consumable, 1){
            @Override
            public boolean use(com.ga.unit.man.Man host){
                if (host instanceof MainPlayer && super.use(host) && isUsable(true, 1)) {
                    host.stats.agility.add(1, false);
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
        Armor product = new Armor(map, "Jewelry",seed,null,4, level-1,0, 0, 0){
            public void onCreate(){
                imbueMaterial(new Item.Material[]{Item.Material.Copper, Item.Material.Stone, Item.Material.Glass, Item.Material.Crystal, Item.Material.Obsidian});
                super.onCreate();
            }
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
        Armor product = new Armor(map, "Jewelry",seed,null,4, level, 0, 0, 0){
            public void onCreate(){
                imbueMaterial(new Item.Material[]{Item.Material.Stone, Item.Material.Glass, Item.Material.Crystal, Item.Material.Obsidian});
            }
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
        Armor product = new Armor(map, "Jewelry",seed,Item.Material.Stone
                ,4,1,0, 20, 0){
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
        Armor product = new Armor(map, "Jewelry",seed,Item.Material.Other,4,1,0,0, 0){
            private String location;
            public void onCreate(){
                imbueMeta(true, false, 0);
            }
            public boolean trigger(Unit host){
                if (host == Global.player && isUsable(true, 1) && cooldown.isReady()) {
                    cooldown.reset();
                    String[] location = this.location.split(",");
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
                        Global.server.requestChangeMap(location[0] + ",0");
                    }
                    return true;
                }
                return false;
            }
            public boolean use(com.ga.unit.man.Man host){
                if (super.use(host)){
                    location = host.map.name+","+host.pos.toString();
                    return true;
                }
                return false;
            }
            public String getDescription(){
                return super.getDescription()+"\nThis trinket will remember the location of where you had put it on. Activate to return to the location.";
            }
            public void load(T p){
                super.load(p);
                location = p.get(8).get();
            }
            public String toString(){
                return super.toString()+","+location;
            }
        };
        product.name = "Memoryfoam_Necklace";
        return product;
    }},
    Rogue_Cloak{ Prop make(Map map, int id, int seed){
        int level = (seed % 2)+1;
        Armor product = new Armor(map, name(),seed,Item.Material.Silk,3,level*2-1,0, 0, 0){
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
        Armor product = new Armor(map, name(),seed,Item.Material.Silk,4,level*2-1,0,0, 0){
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
        Weapon product = new Weapon(map, "Knife", seed, Item.Material.Obsidian, Item.ItemType.Sword){
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
        Armor product = new Armor(map, "Jewelry",seed,Item.Material.Silk,3,4,0, 10, 0);
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
        Armor product = new Armor(map, "Jewelry",seed,Item.Material.Gold,4,4,0, 0, 0);
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
        Armor product = new Armor(map, "Jewelry",seed,Item.Material.Gold,0,4,0, 0, 0);
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
        if (p == null || p.isEmpty()) return null;
        Prop t;
        T s = new T(p);
        int index = s.get(0).getInt();
        int subId = s.get(1).getInt();
        int seed = Creation.getSeed(s.get(2).getInt());
        t = values()[index].make(map, subId, seed);
        t.manualDepth = false;
        t.factoryId = index;
        t.subId = subId;
        t.load(s);
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
        Global.p("Could not find Stock ["+p+"]");
        System.exit(1);
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
            T data = FileManager.readLiterals("Data/raw/"+raw+".txt");
            for (int i=0;i<data.size();i++) extRegistry.put(data.get(i).get(0).get(), raw+"@"+i);
        }
    }
}

class UnitFactory {
    Unit host;
    UnitFactory(Unit unit, Faction faction, T stats){
        host = unit;
        host.controlId = Global.getControlId();
        host.faction = faction;
        int str = stats.get(0).getInt(), agi = stats.get(1).getInt(), wis = stats.get(2).getInt();
        unit.stats.strength = new Stat(str);
        unit.stats.agility = new Stat(agi);
        unit.stats.intelligence = new Stat(wis);
        unit.stats.level = new Stat(str+agi+wis);
        unit.stats.speed = 18+host.rand.nextInt(8);
        unit.stats.attack = host.rand.nextInt(10)-8;
        setAI(2, 0);
    }
    void setEquipment(T weapon, T armor, T helms){
        Man man = (Man) host;
        if (!weapon.get().isEmpty()) man.setWeapon((Item) Stock.createByName(host.map, weapon.get(host.seed % weapon.size()).get(), -1));
        man.armor = new ArmorSet(man, getItem(host.map, helms.get(host.seed % helms.size()).get(),0),
                getItem(host.map, armor.get(host.seed % armor.size()).get(), 1),
                getItem(host.map, armor.get(host.seed % armor.size()).get(), 2),
                "", "");
    }
    void addInventory(T items){
        Man man = (Man) host;
        man.inventory.setMaxSize(items.size());
        for (T item : items) {
            man.inventory.addItem((Item) Stock.createByName(host.map, item.get(), -1));
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
    void setAI(int type, int subType){
        Intelligence.load(host, new T("[["+type+","+subType+"],[[1,0]]]"));
    }
    void setMoveset(String p){
        if (!p.isEmpty()) {
            String[] skills = p.split(",");
            StringBuilder r = new StringBuilder();
            for (String i : skills) {
                r.append("[").append(i).append(",0],");
            }
            Actions.load(host, new T(r.substring(0, r.length() - 1)));
        }
    }
}