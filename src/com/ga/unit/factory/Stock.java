package com.ga.unit.factory;

import com.ga.game.Dialog;
import com.ga.game.Display;
import com.ga.map.Global;
import com.ga.map.MapEvent;
import com.ga.tool.Interactive;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.*;
import com.ga.unit.intelligence.UnitIntelligence;
import com.ga.unit.item.*;
import com.ga.unit.skill.*;

/**
 * Created by Ga on 2/3/2017. asd
 */
public class Stock {
    private static Factory[] factory = new Factory[]{
            new Factory("PlayerSpawn"){ public Matter make(int seed){
                return new Matter(name, seed, Updater.editor?10:0);
            }},
            new Factory("Phone_Booth"){ public Matter make(int seed){
                Matter r = new Matter(name, seed, 5);
                r.onInteract = new Interactive(20){
                    public void interact(Unit p){
                        String t = p.getConversation();
                        if (t.equals("")){
                            Global.server.online();
                            p.say("Waiting for a call...");
                        } else if (t.toLowerCase().startsWith("call")) {
                            Global.server.requestConnection(p.getConversation().substring(5));
                            p.say("Connecting...");
                        }
                        p.setState(Actions.Wait);
                    }
                };
                return r;
            }},
            new Factory("Map"){ public Matter make(int seed){
                Matter r = new Matter(name, seed, 5);
                r.onInteract = new Interactive(20){
                    public void interact(final Unit p){
                        Display.menuMap.display();
                    }
                };
                return r;
            }},
            new Factory("Stick"){ public Matter make(int seed){ //3
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Mace); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Wooden Sword"){ public Matter make(int seed){
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Mace); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Broadsword"){ public Matter make(int seed){
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Sword); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Bamboo Spear"){ public Matter make(int seed){
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Spear); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Mace"){ public Matter make(int seed){
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Mace); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Katana"){ public Matter make(int seed){
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Sword); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Claw"){ public Matter make(int seed){
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Claw); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Spear"){ public Matter make(int seed){ //10
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Spear); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Axe"){ public Matter make(int seed){
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Sword); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Dagger"){ public Matter make(int seed){
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Dagger); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Pick"){ public Matter make(int seed){
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Sword); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Cleaver"){ public Matter make(int seed){
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Sword); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Lancesword"){ public Matter make(int seed){ //15
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Sword); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Cutlass"){ public Matter make(int seed){
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Sword); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Claymore"){ public Matter make(int seed){
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Sword); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Halberd"){ public Matter make(int seed){
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Sword); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Greatsword"){ public Matter make(int seed){
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Sword); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Warhammer"){ public Matter make(int seed){ //20
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Mace); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Revolver"){ public Matter make(int seed){
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Shoot); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Pistol"){ public Matter make(int seed){
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Shoot); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Rifle"){ public Matter make(int seed){
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Snipe); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Cotton Clothing Legging"){ public Matter make(int seed){
                Armor r = new Armor("Cotton Clothing",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Cotton Clothing Torso"){ public Matter make(int seed){
                return new Armor("Cotton Clothing",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Fabric Clothing Legging"){ public Matter make(int seed){
                Armor r = new Armor("Fabric Clothing",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Fabric Clothing Torso"){ public Matter make(int seed){
                return new Armor("Fabric Clothing",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Suit Legging"){ public Matter make(int seed){
                Armor r = new Armor("Suit",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Suit Torso"){ public Matter make(int seed){
                return new Armor("Suit",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Adventurewear Legging"){ public Matter make(int seed){ //30
                Armor r = new Armor("Adventurewear",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Adventurewear Torso"){ public Matter make(int seed){
                return new Armor("Adventurewear",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Leather Armor Legging"){ public Matter make(int seed){
                Armor r = new Armor("Leather Armor",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Leather Armor Torso"){ public Matter make(int seed){
                return new Armor("Leather Armor",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Steel Armor Legging"){ public Matter make(int seed){
                Armor r = new Armor("Steel Armor",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Steel Armor Torso"){ public Matter make(int seed){
                return new Armor("Steel Armor",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Chainmail Legging"){ public Matter make(int seed){
                Armor r = new Armor("Chainmail",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Chainmail Torso"){ public Matter make(int seed){
                return new Armor("Chainmail",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Platemail Legging"){ public Matter make(int seed){
                Armor r = new Armor("Platemail",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Platemail Torso"){ public Matter make(int seed){
                return new Armor("Platemail",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Shadowrobe Legging"){ public Matter make(int seed){
                Armor r = new Armor("Shadowrobe",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Shadowrobe Torso"){ public Matter make(int seed){
                return new Armor("Shadowrobe",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Lace Helm"){ public Matter make(int seed){
                return new Armor(name,seed,1,10,1,1,0); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Full Helm"){ public Matter make(int seed){ //70
                return new Armor(name,seed,1,10,1,1,0); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Legion Helm"){ public Matter make(int seed){
                return new Armor(name,seed,1,10,1,1,0); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Blood"){ public Matter make(int seed){
                return new Item(name, seed, 0, -1, 1, 0){
                    @Override
                    public boolean use(final Man host){
                        return false;
                    }
                };
            }},
            new Factory("Weed"){ public Matter make(int seed){
                Item item = new Item(name, seed, 0, 10, 0, 0){
                    public void update(){}
                };
                item.randomSize();
                item.onInteract = null;
                return item;
            }},
            new Factory("Root"){ public Matter make(int seed){
                Item item = new Item(null, seed, 0, 10, 0, 0){
                    public void update(){}
                };
                item.randomSize();
                item.name = "Root"+((item.seed % 3)+1);
                item.setImage(item.name);
                item.gravity = false;
                item.onInteract = null;
                return item;
            }},
            new Factory("Vine"){ public Matter make(int seed){ //75
                Item item = new Item(null, seed, 0, 10, 0, 0){
                    public void update(){}
                };
                item.randomSize();
                item.name = "Vine"+((item.seed % 2)+1);
                item.setImage(item.name);
                item.gravity = false;
                item.onInteract = null;
                return item;
            }},
            new Factory("Rock"){ public Matter make(int seed){
                Item item = new Item(name, seed, 4, -1, 0, 0){
                    @Override
                    public boolean use(final Man host){
                        return false;
                    }
                };
                item.randomSize();
                return item;
            }},
            new Factory("Bush"){ public Matter make(int seed){
                Item item = new Item(name, seed, 6, 50, 0, 0){
                    @Override
                    public boolean use(final Man host){
                        return false;
                    }
                };
                item.randomSize();
                item.onInteract = null;
                return item;
            }},
            new Factory("XBush"){ public Matter make(int seed){
                Item item = new Item(null, seed, 6, 50, 0, 0){
                    @Override
                    public boolean use(final Man host){
                        return false;
                    }
                };
                item.randomSize();
                item.name = "DeadBush"+((item.seed % 2)+1);
                item.setImage(item.name);
                item.onInteract = null;
                return item;
            }},
            new Factory("TreeD"){ public Matter make(int seed){
                Item item = new Item(null, seed, 20, 100, 0, 0){
                    public void update(){}
                };
                item.randomSize();
                item.name = "TreeDec"+((item.seed % 4)+1);
                item.setImage(item.name);
                item.onInteract = null;
                return item;
            }},
            new Factory("Bridge1"){ public Matter make(int seed){ //80
                Matter r = new Matter(name, seed, 15){
                    public void update(){}
                };
                return r;
            }},
            new Factory("Fist"){ public Matter make(int seed){
                Item r = new Item(name, 0, 0, -1, 0, 0){
                    @Override
                    public boolean use(final Man host){
                        host.setState(Actions.Interact);
                        return false;
                    }
                    @Override
                    public String forceToString(){
                        return "";
                    }
                };
                r.save = false;
                return r;
            }},
            new Factory("Guidebook"){ public Matter make(int seed){
                Item r = new Item(name, seed, 0, -1, 0, 0){
                    @Override
                    public boolean use(Man host){
                        if (super.use(host)) {
                            String stage = "0";
                            if (Global.map.event == MapEvent.Tutorial) stage = MapEvent.cookie;
                            Display.menuDialog.show("Guidebook by Whu", Dialog.get("tutorial"+stage));
                            if (stage.equals("4")) remove();
                            return true;
                        }
                        return false;
                    }
                };
                r.setType(Item.Tag.Scroll);
                r.delay = 25;
                return r;
            }},
            new Factory("Spellbook"){ public Matter make(int seed){
                Item item = new Item(name, seed, 1, 1, 0, 1){
                    @Override
                    public boolean use(Man host){
                        if (super.use(host)) {
                            ((Player)host).learnSkill(emod);
                            return true;
                        }
                        return false;
                    }
                };
                item.setType(Item.Tag.Enchant);
                item.delay = 45;
                return item;
            }},
            new Factory("Manual"){ public Matter make(int seed){
                Item item = new Item(name, seed, 1, 1, 0, 0){
                    @Override
                    public boolean use(Man host){
                        if (super.use(host)) {
                            ((Player)host).learnSkill(Skills.Slash);
                            return true;
                        }
                        return false;
                    }
                };
                item.setType(Item.Tag.Enchant);
                item.delay = 45;
                return item;
            }},
            new Factory("Potion"){ public Matter make(int seed){
                Item item = new Item(name, seed, 1, 1, 1, 1);
                item.probability = 1;
                item.setType(Item.Tag.Consumable);
                item.delay = 45;
                return item;
            }},
            new Factory("Scroll"){ public Matter make(int seed){
                Item item = new Item(name, seed, 0, 1, 1, 1){
                    @Override
                    public boolean use(Man host){
                        if (super.use(host)) {
                            host.clip.setState(Skills.Guard);
                            if (uses.get() == 0) remove();
                            return true;
                        }
                        return false;
                    }
                };
                item.probability = 1;
                item.setType(Item.Tag.Scroll);
                item.delay = 30;
                return item;
            }},
            new Factory("Fireball"){ public Matter make(int seed){
                Item item = new Item(name, seed, 1, 3, 1, 0) {
                    @Override
                    public boolean use(Man host) {
                        if (super.use(host)) {
                            final Projectile p = new Projectile("Fireball", host.pos.getAdd(host.clip.organs[BodyHumanoid.FIST].ptd), host.pos.getAdd(host.target), 20+host.get("Stamina").get()){
                                @Override
                                public void stuck(){
                                    Particle.explosion(pos);
                                    explode(60);
                                    remove();
                                }
                                @Override
                                public void update(){
                                    super.update();
                                    Particle.dripFire(pos);
                                }
                            };
                            p.faction = host.faction;
                            p.damage = 30;
                            p.gravity = true;
                            p.delay.set(50);
                            p.delay.performer = new Performer(){
                                public void perform(){
                                    p.stuck();
                                }
                            };
                            return true;
                        }
                        return false;
                    }
                };
                item.delay = 10;
                item.setType(Item.Tag.Throwing);
                return item;
            }},
            new Factory("Tanto"){ public Matter make(int seed){
                Item item = new Item(name, seed, 1, 10, 0, .04f){
                    @Override
                    public boolean use(final Man host){
                        if (super.use(host)) {
                            host.delay.performer = new Performer(){
                                public void perform(){
                                    final Projectile p = new Projectile("Tanto",host.pos.getAdd(host.clip.organs[BodyHumanoid.OFIST].ptd),host.pos.getAdd(host.target),70){
                                        @Override
                                        public void stuck(){
                                            if (tangible) Particle.explode("Fire", pos, vel, 20, 10, 15, true,3);
                                            super.stuck();
                                        }
                                    };
                                    p.rotSpeed = 0;
                                    p.faction = host.faction;
                                    p.damage = 10;
                                    p.gravity = false;
                                    p.delay.performer = new Performer(){
                                        @Override
                                        public void perform(){
                                            p.stuck();
                                        }
                                    };
                                }
                            };
                            return true;
                        }
                        return false;
                    }
                };
                item.delay = 10;
                item.setType(Item.Tag.Darting);
                return item;
            }},
            new Factory("Shell"){ public Matter make(int seed){
                Item item = new Item(name, seed, 0, 1, 0, .1f);
                item.damage = 0;
                return item;
            }},
            new Factory("Starfish"){ public Matter make(int seed){ //90
                Item item = new Item(name, seed, 0, 1, 0, 0);
                item.randomSize();
                item.damage = 0;
                return item;
            }},
            new Factory("Book"){ public Matter make(int seed){
                Item r = new Item(name, seed, 1, -1, 0, 0){
                    @Override
                    public boolean use(Man host){
                        if (super.use(host)) {
                            Display.menuDialog.show("Title","Content");
                            return true;
                        }
                        return false;
                    }
                };
                r.setType(Item.Tag.Scroll);
                r.delay = 45;
                return r;
            }},
            new Factory("Meat"){ public Matter make(int seed){
                Food item = new Food(null, seed, 1, 1, 10, 0, 3);
                item.weight = 3;
                if (seed % 4 == 0) item.name = "Drumstick";
                else if (seed % 3 == 0) item.name = "Porkchop";
                else item.name = "Meat";
                item.randomSize();
                item.setImage(item.name);
                item.setType(Item.Tag.Food);
                return item;
            }},
            new Factory("Meal"){ public Matter make(int seed){
                Food item = new Food(null, seed, 1, 5, 4, 1, 10);
                item.volume = 2;
                if (seed % 4 == 0) item.name = "Burger";
                else if (seed % 3 == 0) item.name = "Sandwich";
                else item.name = "Meal";
                item.setImage(item.name);
                item.setType(Item.Tag.Food);
                return item;
            }},
            new Factory("Ration"){ public Matter make(int seed){
                Food item = new Food(null, seed, 1, 6, 2, 6, 1);
                if (seed % 4 == 0) item.name = "Ration";
                else if (seed % 3 == 0) item.name = "Ration";
                else item.name = "Ration";
                item.randomSize();
                item.setImage(item.name);
                item.setType(Item.Tag.Food);
                return item;
            }},
            new ManFactory("Punk"){ public Matter make(int seed){
                Man r = getMan(seed,getStat(12,40,40,10,0),Faction.Punk,"-5,50/Kick&Swing,Pierce&Dash",
                        new String[]{"Stick","Axe","Pick","Broadsword"},
                        new String[]{"Cotton Clothing","Fabric Clothing"});
                return r;
            }},
            new ManFactory("Phantom"){ public Matter make(int seed){
                Man r = getMan(seed,getStat(12,40,40,10,0),Faction.Punk,"-5,50/Kick&Swing,Pierce&Dash",
                        new String[]{"Stick","Axe","Pick","Broadsword"},
                        new String[]{"Cotton Clothing","Fabric Clothing"});
                return r;
            }},
            new UnitFactory("Dog") { Matter make(int seed) {
                Unit r = new Unit(getUnit(name,seed,getStat(3,60,30,10,3),Faction.Evil,"5,140/Kick&Pierce")){
                    public void stateSet(Moveset state){
                        super.stateSet(state);
                        if (state == Skills.Pierce){
                            delay.time = 24;
                            vel.x += getSpeed()*3*dir;
                            vel.y -= 20;
                        }
                    }
                    public void stateUpdate(){
                        super.stateUpdate();
                        if (state == Skills.Pierce) attack();
                    }
                };
                process(r);
                return r;
            }},
            new UnitFactory("Slime") { Matter make(int seed) {
                Unit r = new Unit(getUnit(name,seed,getStat(1,10,12,10,1),Faction.Evil,"-5/Kick")){
                    public void stateSet(Moveset state) {
                        super.stateSet(state);
                        if (state == Skills.Kick) {
                            moveTriggered(0);
                            delay.time = 24;
                        }
                    }
                };
                process(r);
                return r;
            }},
            new UnitFactory("Mantis") { Matter make(int seed) {
                Unit r = new Unit(getUnit(name,seed,getStat(10,100,40,10,3),Faction.Evil,"-5/Kick")){
                    public void stateSet(Moveset state){
                        super.stateSet(state);
                        if (state == Skills.Pierce){
                            delay.time = 30;
                            vel.x += getSpeed()*3*dir;
                            vel.y -= 20;
                        }
                    }
                };
                r.clip.organs[1].pt.y -= r.clip.organs[0].ptd.y;
                process(r);
                return r;
            }},
            new UnitFactory("Beholder") { Matter make(int seed) {
                Unit r = new Unit(getUnit(name,seed,getStat(30,10,1,60,3),Faction.Evil,"300/Fireblast,Idle,Idle,Idle")){
                    public void collide(){
                        super.collide();
                        jump = true;
                    }
                    public P getHand(){
                        return pos.getAdd(0,getReach()+40);
                    }
                    public void stateSet(Moveset state) {
                        boolean spell = state instanceof Spells;
                        super.stateSet(state);
                        if (spell) delay.set(1);
                    }
                    public void update(){
                        super.update();
                        vel.y += .1f;
                        if (state != Actions.Dead) {
                            if (pos.y+vel.y+200 >= Global.map.size.y){
                                vel.y += (Global.map.size.y - (pos.y + 200)) * .01f;
                            } else {
                                for (Line i : Global.map.land) {
                                    if (i.lineCross(new Line(pos, pos.getAdd(0, 200)))) {
                                        vel.y += ((i.pt.y + (i.getSlope() * (pos.x + vel.x - i.pt.x))) - (pos.y + 200)) * .01f;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                };
                if (seed % 2 == 1) r.loadMoveset("200,800/Flamestride,Flameroll&Flameroll,Fireball");
                r.gravity = false;
                process(r);
                return r;
            }},
            new Factory("Propeller"){ public Matter make(int seed){
                final int side = seed % 2 == 0 ? -1 : 1;
                return new Matter(name, seed, 5){
                    public void update(){
                        super.update();
                        rotation+=(10f*side)/image.getSize().x;
                    }
                };
            }}
    };
    public static Matter get(String[] p){
        Matter t;
        int seed = Integer.parseInt(p[1]);
        if (p[0].charAt(0) == '!'){
            t = getInstant(p[0].substring(1), seed);
        } else {
            int index = Integer.parseInt(p[0]);
            t = factory[index].make(seed);
            t.factoryId = index;
            if (t instanceof Item){
                Item item = (Item)t;
                if (p.length > 3) item.uses = new Stat(p[3]);
                if (p.length > 4) item.loadAdmod(p[4]);
                if (p.length > 5) item.rotation = Float.parseFloat(p[5]);
                if (p.length > 6){
                    if (t instanceof Food) ((Food) item).perish = Integer.parseInt(p[6]);
                }
            } else if (t instanceof Unit) {
                if (p.length > 3) {
                    int id = Integer.parseInt(p[3]);
                    if (id >= Updater.id) Updater.id = id + 1;
                }
            }
        }
        if (p.length > 2) t.pos.load(p[2]);
        return t;
    }
    private static Matter getInstant(String p, int seed){
        final int side = seed % 2 == 0 ? -1 : 1;
        Matter t = new Matter(p, seed, 5){
            public void update(){}
        };
        P size = t.image.getSize();
        size.x *= side;
        t.image.setSize(size.x,size.y);
        return t;
    }
    public static Matter getByName(String p, int seed){
        for (int i=0;i<factory.length;i++){
            if (factory[i].name.equals(p)){
                Matter r = factory[i].make(seed);
                r.factoryId = i;
                return r;
            }
        }
        return getInstant(p, seed);
    }
}
abstract class Factory{
    protected String name;
    Factory(String name){
        this.name = name;
    }
    abstract Matter make(int seed);
}
abstract class UnitFactory extends Factory{
    UnitFactory(String name) {
        super(name);
    }
    String getStat(int stamina, int speed, int proficiency, int health, int level){
        return "Stamina:"+stamina+"&Speed:"+speed+"&Proficiency:"+proficiency+"&Health:"+health+"&Level:"+level;
    }
    String getUnit(String name, int seed, String stat, Faction faction, String moves){
        return name+"@"+seed+"@0,0@"+Updater.getId()+"@"+stat+"@"+faction.name()+"@"+moves;
    }
    Unit process(Unit p){
        p.brain = new UnitIntelligence(p);
        p.clip = new Body(p, p.name);
        return p;
    }
}
abstract class ManFactory extends UnitFactory{
    private static String[] manHeads = new String[]{"Man0","Man1","Man2","Man3","Man4","Man5","Man6"};
    ManFactory(String name) {
        super(name);
    }
    private String getFace(){
        return manHeads[Global.r.nextInt(manHeads.length)];
    }
    private Armor getArmor(String p, int piece){
        String pieceName = "";
        if (piece == 1) pieceName = "Legging";
        else if (piece == 2) pieceName = "Torso";
        return (Armor) Stock.getByName(p+" "+pieceName,-1);
    }
    Man getMan(int seed, String stat, Faction faction, String moves, String[] weapon, String[] armor){
        String r = getUnit(getFace(),seed,stat,faction,moves);
        r += "##None&None&None";
        String arm = armor[Global.r.nextInt(armor.length)];
        Man m = new Man(r);
        m.setWeapon((Item) Stock.getByName(weapon[Global.r.nextInt(weapon.length)],-1));
        m.armor.set(getArmor(arm,1),1);
        m.armor.set(getArmor(arm,2),2);
        return m;
    }
}