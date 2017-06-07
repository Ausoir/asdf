package com.ga.unit;

import com.ga.game.Dialog;
import com.ga.game.Sound;
import com.ga.map.Creation;
import com.ga.map.Global;
import com.ga.map.MapEvent;
import com.ga.map.Updater;
import com.ga.tool.Interactive;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.intelligence.Intelligence;
import com.ga.unit.intelligence.MerchantIntelligence;
import com.ga.unit.intelligence.ResidentIntelligence;
import com.ga.unit.item.*;
import com.ga.unit.player.Player;
import com.ga.unit.player.Trait;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Buffs;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Ga on 2/3/2017. asd
 */
public enum Stock {
    Spawn{ protected Prop make(int seed){
        Prop product = new Prop(getName(), seed){
            public void onField(){}
        };
        product.image.visible = false;
        return process(product);
    }},
    Exit{ Prop make(final int seed){
        Prop product = new Prop("Spawn", seed){
            public void onField(){}
        };
        product.image.visible = false;
        product.onInteract = new Interactive(){
            public void interact(Man p){
                if (p == Global.player) Global.map.loadMap(Global.map.maps[4]+"@"+seed);
            }
        };
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
        Prop product = new Weapon(getName(),0, Item.ItemType.Bashing){
            public void use(final Man host) {
                host.setState(Actions.Interact);
            }
            public String forceToString() {
                return "";
            }
            public void drop(Man host) {}
            public void onUnequip(Man host){}
        };
        product.image.setCenterType(3);
        product.onInteract = null;
        return process(product);
    }},
    Stick{ Prop make(int seed){ //3
        Prop product = new Weapon(getName(),seed, Item.ItemType.Bashing);
        return process(product);
    }},
    Broadsword{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed, Item.ItemType.Sword);
        product.image.setCenterType(3);
        return process(product);
    }},
    Bamboo_Spear{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed, Item.ItemType.Spear);
        return process(product);
    }},
    Mace{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed, Item.ItemType.Bashing){
            public void randomSize(P p){}
        };
        return process(product);
    }},
    Katana{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed, Item.ItemType.Sword);
        product.image.setCenterType(3);
        return process(product);
    }},
    Claw{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed, Item.ItemType.Knife);
        product.image.setCenterType(3);
        return process(product);
    }},
    Spear{ Prop make(int seed){ //10
        Prop product = new Weapon(getName(),seed, Item.ItemType.Spear);
        return process(product);
    }},
    Axe{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed, Item.ItemType.Sword);
        return process(product);
    }},
    Dagger{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed, Item.ItemType.Knife);
        product.image.setCenterType(3);
        return process(product);
    }},
    Tanto{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed, Item.ItemType.Knife);
        product.image.setCenterType(3);
        return process(product);
    }},
    Pickaxe { Prop make(int seed){
        Prop product = new Weapon(getName(),seed, Item.ItemType.Sword);
        return process(product);
    }},
    Cleaver{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed, Item.ItemType.Sword);
        product.image.setCenterType(3);
        return process(product);
    }},
    Lancesword{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed, Item.ItemType.Spear);
        product.image.setCenterType(3);
        return process(product);
    }},
    Cutlass{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed, Item.ItemType.Sword);
        product.image.setCenterType(3);
        return process(product);
    }},
    Claymore{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed, Item.ItemType.Sword);
        product.image.setCenterType(3);
        return process(product);
    }},
    Halberd{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed, Item.ItemType.Spear);
        return process(product);
    }},
    Greatsword{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed, Item.ItemType.Bashing);
        product.image.setCenterType(3);
        return process(product);
    }},
    Warhammer{ Prop make(int seed){ //20
        Prop product = new Weapon(getName(),seed, Item.ItemType.Bashing);
        return process(product);
    }},
    Bow{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed, Item.ItemType.Bow);
        return process(product);
    }},
    Revolver{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed, Item.ItemType.Shooting);
        product.image.setCenterType(3);
        return process(product);
    }},
    Pistol{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed, Item.ItemType.Shooting);
        product.image.setCenterType(3);
        return process(product);
    }},
    Rifle{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed, Item.ItemType.Rifle);
        product.image.setCenterType(3);
        return process(product);
    }},
    Ethersword{ Prop make(int seed){
        Weapon product = new Weapon(getName(),seed, Item.ItemType.Sword){
            public String toString(){
                return "";
            }
            public void remove(){
                super.remove();
                Particle.holyExplosion(pos);
            }
            public void trigger(Unit host) {
                super.trigger(host);
                Particle.holyExplosion(pos);
            }
        };
        product.image.setCenterType(3);
        return process(product);
    }},
    Comfortwear_Legging{ Prop make(int seed){
        Armor product = new Armor("Comfortwear",seed, new Item.Material[]{Item.Material.Cloth},.8f,1, 1);
        return process(product);
    }},
    Comfortwear_Torso{ Prop make(int seed){
        Armor product = new Armor("Comfortwear",seed, new Item.Material[]{Item.Material.Cloth},.8f,0, 2);
        return process(product);
    }},
    Dustset_Legging{ Prop make(int seed){
        Item.Material[] mat = new Item.Material[]{Item.Material.Cloth, Item.Material.Leather};
        Armor product = new Armor("Dustset",seed, mat,.8f,2, 1);
        return process(product);
    }},
    Dustset_Torso{ Prop make(int seed){
        Item.Material[] mat = new Item.Material[]{Item.Material.Cloth, Item.Material.Leather};
        Armor product = new Armor("Dustset",seed, mat,1,1, 2);
        return process(product);
    }},
    Toughwear_Legging{ Prop make(int seed){
        Item.Material[] mat = new Item.Material[]{Item.Material.Cloth, Item.Material.Leather};
        Armor product = new Armor("Toughwear",seed, mat,1,1, 1);
        return process(product);
    }},
    Toughwear_Torso{ Prop make(int seed){
        Item.Material[] mat = new Item.Material[]{Item.Material.Cloth, Item.Material.Leather};
        Armor product = new Armor("Toughwear",seed, mat,1,0, 2);
        return process(product);
    }},
    Suit_Legging{ Prop make(int seed){
        Armor product = new Armor("Suit",seed, new Item.Material[]{Item.Material.Cloth},1,2, 1);
        product.pocket = 1;
        return process(product);
    }},
    Suit_Torso{ Prop make(int seed){
        Armor product = new Armor("Suit",seed, new Item.Material[]{Item.Material.Cloth},1,1, 2);
        product.pocket = 1;
        return process(product);
    }},
    Robe_Legging{ Prop make(int seed){
        Armor product = new Armor("Robe",seed, new Item.Material[]{Item.Material.Cloth},1,0, 1);
        product.pocket = 1;
        return process(product);
    }},
    Robe_Torso{ Prop make(int seed){
        Armor product = new Armor("Robe",seed, new Item.Material[]{Item.Material.Cloth},.6f,2, 2);
        return process(product);
    }},
    Adventurewear_Legging{ Prop make(int seed){ //30
        Armor product = new Armor("Adventurewear",seed, new Item.Material[]{Item.Material.Leather},.8f,2, 1);
        product.pocket = 1;
        return process(product);
    }},
    Adventurewear_Torso{ Prop make(int seed){
        Item.Material[] mat = new Item.Material[]{Item.Material.Leather, Item.Material.Bone};
        Armor product = new Armor("Adventurewear",seed, mat,.8f,1, 2);
        return process(product);
    }},
    Chainmail_Legging{ Prop make(int seed){
        Item.Material[] mat = new Item.Material[]{Item.Material.Iron, Item.Material.Bronze, Item.Material.Copper,
                Item.Material.Silver, Item.Material.Steel};
        Armor product = new Armor("Chainmail",seed, mat,.6f,1, 1);
        product.pocket = 1;
        return process(product);
    }},
    Chainmail_Torso{ Prop make(int seed){
        Item.Material[] mat = new Item.Material[]{Item.Material.Iron, Item.Material.Bronze, Item.Material.Copper,
                Item.Material.Silver, Item.Material.Steel};
        Armor product = new Armor("Chainmail",seed, mat,.5f,0, 2);
        return process(product);
    }},
    Platemail_Legging{ Prop make(int seed){
        Armor product = new Armor("Platemail",seed, Armor.plateMaterials,.8f,1, 1);
        return process(product);
    }},
    Platemail_Torso{ Prop make(int seed){
        Armor product = new Armor("Platemail",seed, Armor.plateMaterials,.8f,0, 2);
        return process(product);
    }},
    Battlewear_Legging{ Prop make(int seed){
        Armor product = new Armor("Battlewear",seed, Armor.plateMaterials,1,1, 1);
        product.pocket = 1;
        return process(product);
    }},
    Battlewear_Torso{ Prop make(int seed){
        Armor product = new Armor("Battlewear",seed, Armor.plateMaterials,1,1, 2);
        return process(product);
    }},
    Shadowrobe_Legging{ Prop make(int seed){
        Armor product = new Armor("Shadowrobe",seed, new Item.Material[]{Item.Material.Cloth},1,0, 1);
        product.pocket = 1;
        return process(product);
    }},
    Shadowrobe_Torso{ Prop make(int seed){
        Armor product = new Armor("Shadowrobe",seed, new Item.Material[]{Item.Material.Cloth},1,0, 2);
        return process(product);
    }},
    Bandana{ Prop make(int seed){
        Armor product = new Armor(getName(),seed, new Item.Material[]{Item.Material.Cloth},.3f,0, 0){
            public void onCreate(){
                imbueMeta(true,true,.2f);
            }
        };
        return process(product);
    }},
    Mask{ Prop make(int seed){
        Armor product = new Armor(getName(),seed, new Item.Material[]{Item.Material.Cloth},.6f,0, 0){
            public void onCreate(){
                imbueMeta(true,true,.2f);
            }
        };
        return process(product);
    }},
    Lace_Helm{ Prop make(int seed){
        Item.Material[] mat = new Item.Material[]{Item.Material.Steel, Item.Material.Brass,
                Item.Material.Bronze, Item.Material.Copper, Item.Material.Bone};
        Armor product = new Armor(getName(),seed, mat,.7f,0, 0);
        return process(product);
    }},
    Full_Helm{ Prop make(int seed){
        Item.Material[] mat = new Item.Material[]{Item.Material.Steel, Item.Material.Brass,
                Item.Material.Bronze, Item.Material.Copper, Item.Material.Bone};
        Armor product = new Armor(getName(),seed, mat,.9f,0, 0);
        return process(product);
    }},
    Legion_Helm{ Prop make(int seed){
        Item.Material[] mat = new Item.Material[]{Item.Material.Steel, Item.Material.Brass,
                Item.Material.Bronze, Item.Material.Copper, Item.Material.Bone};
        Armor product = new Armor(getName(),seed, mat,.8f,0, 0);
        return process(product);
    }},
    Blood{ Prop make(int seed){
        Prop product = new Item(getName(), seed, new Item.Material[]{Item.Material.Other}, Item.ItemType.Consumable){
            public void use(final Man host){}
        };
        return process(product);
    }},
    Weed{ Prop make(int seed){
        final Prop product = new Prop("Weed", seed);
        isNatural(product);
        return process(product);
    }},
    Root{ Prop make(int seed){
        Prop product = new Prop("Root", seed);
        isNatural(product);
        return process(product);
    }},
    Vine{ Prop make(int seed){ //75
        Prop product = new Prop("Vine", seed);
        isNatural(product);
        return process(product);
    }},
    Rock{ Prop make(int seed){
        Item product = new Item("Rock", seed, new Item.Material[]{Item.Material.Stone}, Item.ItemType.Bashing);
        isNatural(product);
        return process(product);
    }},
    Stone{ Prop make(int seed){ //75
        Prop product = new Prop("Stone", seed);
        isNatural(product);
        product.tags.add(Tag.Hideable);
        return process(product);
    }},
    Bush{ Prop make(int seed){
        Prop product = new Prop("Bush", seed);
        isNatural(product);
        product.tags.add(Tag.Hideable);
        product.gravity = true;
        return process(product);
    }},
    XBush{ Prop make(int seed){
        Prop product = new Prop("DeadBush", seed);
        isNatural(product);
        product.tags.add(Tag.Hideable);
        product.gravity = true;
        return process(product);
    }},
    Tree{ Prop make(int seed){
        Prop product = new Prop("Tree", seed);
        isNatural(product);
        product.tags.add(Tag.Hideable);
        return process(product);
    }},
    Door{ Prop make(int seed){
        Prop product = new Prop("Door", seed);
        product.gravity = false;
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
        final Prop entrance = Stock.Spawn.create(Global.houseId++);
        final Prop product = new Prop(getName(), seed){
            public void onField(){
                super.onField();
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
        entrance.onInteract = new Interactive(){
            public void interact(Man p){
                if (p == Global.player && p.lastLand.depth == product.getDepth()+2){
                    Global.map.loadMap("I_"+Global.map.id+"_"+entrance.seed+"@"+entrance.seed+"@"+product.image.getSize().getAdd(64,64)+"@0@1");
                }
            }
        };
        entrance.save = false;
        isFurniture(product);
        if (product.image.multiple == 0) entrance.pos.set(product.dir*42,-64);
        else if (product.image.multiple == 1) entrance.pos.set(product.dir*72,-90);
        else if (product.image.multiple == 2) entrance.pos.set(product.dir*-116,-84);
        else if (product.image.multiple == 3) entrance.pos.set(product.dir*8,-97);
        return process(product);
    }},
    TowerHouse{ Prop make(final int seed){
        final ArrayList<Prop> addons = new ArrayList<>();
        final Prop product = new Prop("TowerHouse", seed){
            public void onField(){
                super.onField();
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
        Prop door = makeAddon("Door");
        int doorWidth = (int) (baseSizeX-door.image.getSize().x);
        P doorPos = new P(product.rand.nextInt(doorWidth)-(doorWidth)/2, -8);
        door.pos = doorPos;
        addons.add(door);
        int houseType = product.rand.nextInt(16);
        if (houseType == 1){
            Prop sign = makeAddon("Sign_Food");
            sign.pos.set(doorPos.x<0?doorPos.x+128:doorPos.x-128, doorPos.y);
            addons.add(sign);
        } else if (houseType == 2){
            Prop sign = makeAddon("Sign_Shop");
            sign.pos.set(doorPos.x<0?doorPos.x+128:doorPos.x-128, doorPos.y);
            addons.add(sign);
        } else if (houseType == 3){
            Prop sign = makeAddon("Sign_Guild");
            sign.pos.set(doorPos.x<0?doorPos.x+128:doorPos.x-128, doorPos.y);
            addons.add(sign);
        } else if (houseType == 4){
            Prop sign = makeAddon("Sign_Training");
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
                Prop window = makeAddon("Window");
                window.pos = new P(xmargin*(ix+1)-(baseSizeX/2), -ymargin*(iy+1)-door.image.getSize().y/2);
                addons.add(window);
            }
        }
        if (product.rand.nextBoolean()) {
            for (int i = 0; i < product.rand.nextInt(3); i++) {
                Prop appliance = makeAddon("Appliance");
                appliance.pos = new P(product.rand.nextInt((int) baseSizeX)-baseSizeX/2, -product.rand.nextInt((int) baseSizeY-64));
                addons.add(appliance);
            }
        }
        for (int i=0;i<product.rand.nextInt(3);i++) {
            Prop clockwork = new Prop("Clockwork",seed){
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
        final Prop entrance = Stock.Spawn.create(Global.houseId++);
        entrance.pos = doorPos.getAdd(0,-door.image.getSize().y/2);
        addons.add(entrance);
        entrance.onInteract = new Interactive(){
            public void interact(Man p){
                if (p == Global.player && p.lastLand.depth == product.getDepth()+2){
                    Global.map.loadMap("I_"+Global.map.id+"_"+entrance.seed+"@"+entrance.seed+"@"+product.image.getSize().getAdd(64,64)+"@"+finalHouseType+"@"+ywindows);                }
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
    Crate{ Prop make(int seed){
        if (seed < 100000) seed *= 100000;
        final Prop product = new Prop(getName(), seed);
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
                                    Updater.add(products[current]);
                                }
                            };
                        }
                    }
                    Global.display.menu.showContainer("Container", products, actions);
                }
            }
        };
        product.image.setCenterType(2);
        return process(product);
    }},
    Training_Dummy{ Prop make(int seed){
        Prop product = new Prop(getName(), seed){
        };
        return process(product);
    }},
    Grass{ Prop make(int seed){
        Item product = new Item(getName(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Consumable){
            public void use(Man p){}
        };
        product.tags.add(Tag.Fiber);
        return process(product);
    }},
    Battery{ Prop make(int seed){
        Item product = new Item(getName(), seed, new Item.Material[]{Item.Material.Brass, Item.Material.Copper}, Item.ItemType.Bashing){
            public void use(Man host){}
        };
        product.delay = 25;
        return process(product);
    }},
    Guidebook{ Prop make(int seed){
        Item product = new Item(getName(), seed, new Item.Material[]{Item.Material.Cloth}, Item.ItemType.Scroll){
            @Override
            public void use(Man host){
                super.use(host);
                String stage = "0";
                if (Global.map.event == MapEvent.Tutorial) stage = MapEvent.cookie;
                Global.display.menu.showDialog("Guidebook by Whu", Dialog.get("tutorial"+stage));
                if (stage.equals("4")) remove();
            }
        };
        product.delay = 25;
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
                if (seed != 0) imbueMeta(false, false, 1);
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
                                    if (tangible) Particle.explode("Fire", pos, vel, 20, 10, 15, true, 3);
                                    super.stuck();
                                }
                            };
                            p.rotSpeed = 0;
                            p.faction = host.faction;
                            p.damage = 15;
                            p.blunt = false;
                            p.gravity = false;
                            p.setDepth(host.depth);
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
                                    Particle.explosion(pos);
                                    ArrayList<Unit> t = Updater.collideUnit(pos,96,Faction.Destructive);
                                    for (Unit i : t){
                                        i.checkHit(pos, getDamage());
                                    }
                                    remove();
                                }

                                @Override
                                public void update() {
                                    super.update();
                                    Particle.explode("Fire", pos, new P(0, -1), 6, 1, 4, true, 3);
                                }
                            };
                            p.faction = host.faction;
                            p.damage = 16;
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
                                    host.pos.getAdd(host.target), 64+host.get("Strength").get()*16) {
                                private BasicStroke stroke = new BasicStroke(4);
                                public void stuck() {
                                    super.stuck();
                                    Particle.groundSlam(pos);
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
    Shell{ Prop make(int seed){
        Item product = new Item(getName(), seed, new Item.Material[]{Item.Material.Bone}, Item.ItemType.Consumable);
        return process(product);
    }},
    Starfish{ Prop make(int seed){
        Item product = new Item(getName(), seed, new Item.Material[]{Item.Material.Flesh}, Item.ItemType.Consumable);
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
    Meat{ Prop make(int seed){
        Food product = new Food(getName(), seed, 1, 1, "PPCOX", 100, 0);
        product.weight = 3;
        return process(product);
    }},
    Drumstick{ Prop make(int seed){
        Food product = new Food(getName(), seed, 1, 1, "PPCCOO", 100, 2);
        product.weight = 3;
        return process(product);
    }},
    Porkchop{ Prop make(int seed){
        Food product = new Food(getName(), seed, 1, 1, "PPPOO", 100, 2);
        product.weight = 3;
        return process(product);
    }},
    Meal{ Prop make(int seed){
        Food product = new Food(getName(), seed, 1, 5, "PPPCCCVO", 200, 6);
        product.volume = 2;
        return process(product);
    }},
    Burger{ Prop make(int seed){
        Food product = new Food(getName(), seed, 1, 5, "PPPCCOX", 200, 10);
        return process(product);
    }},
    Sandwich{ Prop make(int seed){
        Food product = new Food(getName(), seed, 1, 5, "PCCCCVO", 350, 2);
        return process(product);
    }},
    Ration{ Prop make(int seed){
        Food product = new Food(getName(), seed, 1, 6, "PPCCV", 8000, 1);
        return process(product);
    }},
    Dummy{ Prop make(int seed){
        Man product = getMan(seed,getStat(6,20,2),Faction.Punk,"10/Use",
                new String[]{"Dagger","Axe","Pickaxe"},
                new String[]{"Comfortwear", "Toughwear"},new String[]{null});
        product.speechType = "punk";
        product.brain = null;
        return process(product);
    }},
    Resident{ Prop make(int seed){
        Man product = getMan(seed,getStat(4,10,1),Faction.Helpless,"200/Sprint",
                new String[]{null,null,null,null,null,null,null,null,null,"Stick","Scroll","Potion","Battery","Bandage"},
                new String[]{"Comfortwear","Comfortwear","Comfortwear","Dustset","Toughwear","Suit","Robe","Adventurewear"},
                new String[]{null});
        product.speechType = "resident"+product.rand.nextInt(8);
        product.brain = new ResidentIntelligence(product);
        return process(product);
    }},
    Merchant{ Prop make(int seed){
        Man product = getMan(seed,getStat(4,10,1),Faction.Neutral,"200/Sprint",
                new String[]{null},
                new String[]{"Suit"},new String[]{null});
        product.speechType = "merchant";
        product.brain = new MerchantIntelligence(product, seed);
        return process(product);
    }},
    Guard{ Prop make(int seed){
        Man product = getMan(seed,getStat(9,20,3),Faction.Neutral,"10,20,800/Use,Dash,Sprint",
                new String[]{"Broadsword","Spear"},
                new String[]{"Chainmail","Battlewear","Toughwear"},new String[]{"Lace_Helm",null});
        product.speechType = "soldier";
        product.brain = new Intelligence(product);
        return process(product);
    }},
    Punk{ Prop make(int seed){
        Man product = getMan(seed,getStat(6,20,2),Faction.Punk,"10,80,200,1200/Thrust&Slash,Dash&Walk,Walk,Sprint",
                new String[]{"Dagger","Axe","Pickaxe"},
                new String[]{"Comfortwear", "Dustset", "Toughwear"},new String[]{null});
        product.speechType = "punk";
        product.brain = new Intelligence(product);
        return process(product);
    }},
    Yeller{ Prop make(int seed){
        Man product = getMan(seed,getStat(3,24,3),Faction.Punk,"10,80,700,1200/Use,Backstep,Calling,Sprint",
                new String[]{"Stick","Dagger",null},
                new String[]{"Comfortwear"},new String[]{"Bandana","Mask"});
        product.speechType = "punk";
        product.brain = new Intelligence(product);
        return process(product);
    }},
    Phantom{ Prop make(int seed){
        Man product = getMan(seed,getStat(12,24,9),Faction.Evil,"10,20,120,1200/Use,Dash&Flameroll,Fireblast,Shadowstep&Blizzard",
                new String[]{"Greatsword","Halberd","Warhammer"},
                new String[]{"Shadowrobe"},new String[]{null});
        product.brain = new Intelligence(product);
        product.clip.organs[5].setImage("Phantom");
        return process(product);
    }},
    Dog { Prop make(int seed) {
        Unit product = new Unit(seed){
            public void stateSet(Actions state){
                super.stateSet(state);
                if (state == Actions.Melee){
                    delay.time = 24;
                    vel.x = 32*dir;
                    vel.y -= 20;
                }
            }
            public Unit attack(){
                Unit r = super.attack();
                if (r != null) vel.x = 0;
                return r;
            }
            public void stateUpdate(){
                super.stateUpdate();
                if (state == Actions.Melee) attack();
            }
            public void onCreate(){
                load(getUnit(name,seed,getStat(3,12,3),Faction.Evil,"140,1200/Melee&Walk,Walk&Sprint"));
            }
        };
        processUnit(product);
        return process(product);
    }},
    Slime { Prop make(int seed) {
        Unit product = new Unit(seed){
            public void stateSet(Actions state) {
                super.stateSet(state);
                if (state == Actions.Melee) {
                    delay.time = 60;
                }
            }
            public void moveTriggered(int p){
                super.moveTriggered(p);
                if (state == Actions.Melee){
                    Particle.explode("Poison",pos,new P(),8,8,0,false,4);
                    ArrayList<Unit> t = Updater.collideUnit(pos,getSize()+36,Faction.Destructive);
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
                load(getUnit(getName(),seed,getStat(7,6,1),Faction.Evil,"8,800/Melee,Walk"));
            }
        };
        product.tags.remove(Tag.Combustible);
        processUnit(product);
        product.clip.organs[0].setSize((int) product.clip.organs[0].getImage().getSize().x);
        return process(product);
    }},
    Bird { Prop make(int seed) {
        Unit product = new Unit(seed){
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
                load(getUnit(getName(),seed,getStat(14,10,1),Faction.Evil,"80,800/Shock&Jump,Jump"));
            }
        };
        processUnit(product);
        product.clip.organs[0].setSize((int) product.clip.organs[0].getImage().getSize().x);
        return process(product);
    }},
    Mantis { Prop make(int seed) {
        Unit product = new Unit(seed) {
            public void stateSet(Actions state) {
                super.stateSet(state);
                if (state == Actions.Thrust){
                    delay.time = 30;
                    vel.x += getSpeed()*3*dir;
                    vel.y -= 20;
                }
            }
            public void onCreate(){
                load(getUnit(getName(),seed,getStat(10,20,12),Faction.Evil,"48,800/Attack,Walk"));
            }
        };
        processUnit(product);
        return process(product);
    }},
    Beholder { Prop make(int seed) {
        Unit product = new Unit(seed){
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
                    if (pos.y+vel.y+200 >= Global.map.size.y){
                        vel.y += (Global.map.size.y - (pos.y + 200)) * .01f;
                    } else {
                        for (Line i : Global.map.land) {
                            if (i.lineCross(new Line(pos, pos.getAdd(0, 200)))) {
                                vel.y -= .2f;
                                break;
                            }
                        }
                    }
                }
            }
            public void onCreate(){
                load(getUnit(getName(),seed,getStat(30,10,3),Faction.Evil,"300/Fireblast&Idle&Idle&Idle"));
            }
        };
        if (seed % 2 == 1) product.loadActions("200,800/Flameroll,Flameroll&Fireball");
        product.gravity = false;
        processUnit(product);
        return process(product);
    }},
    //Uniques
    Stomach_Full_Helm{ Prop make(int seed){
        Armor product = new Armor("Full Helm",seed,new Item.Material[]{Item.Material.Titanium},1,0,0){
            public String getDescription(){
                return super.getDescription()+" Something is odd about this helmet.";
            }
        };
//        product.durability = -1;
        product.tags.add(Tag.Cunning);
        product.tags.add(Tag.Fire);
        product.tags.add(Tag.Air);
        return process(product);
    }};
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
        return getInstant(p, seed);
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
    void isNatural(Prop product){
        product.image.setCenterType(2);
        product.dynamic = true;
        product.gravity = true;
        product.mobile = false;
        product.randomSize(new P(.3f,.3f));
        randomSide(product);
    }
    void isFurniture(Prop product){
        product.image.setCenterType(2);
        product.gravity = true;
        product.mobile = false;
        randomSide(product);
    }
    Prop makeAddon(String name){
        return makeAddon(new Prop(name,-1));
    }
    Prop makeAddon(Prop p){
        p.manualDepth = true;
        p.save = false;
        Updater.cancelAdd(p);
        isFurniture(p);
        return p;
    }
    static void randomSide(Prop p){
        p.dir = p.seed % 2 == 0 ? 1 : -1;
    }
    String getStat(int stamina, int speed, int level){
        return "Stamina:"+stamina+"&Attack:0,0&Speed:"+speed+"&Strength:"+level+"&Level:"+level;
    }
    String getUnit(String name, int seed, String stat, Faction faction, String moves){
        return name+"@"+seed+"@0,0@0@"+stat+"@"+faction.name()+"@"+moves;
    }
    void processUnit(Unit p){
        p.clip = new Body(p, getName());
        p.brain = new Intelligence(p);
    }
    private String getItem(String p, int piece){
        if (p == null){
            if (piece == 0) return "None";
            else if (piece == -1) return "";
        }
        String pieceName = "";
        if (piece == 1) pieceName = "_Legging";
        else if (piece == 2) pieceName = "_Torso";
        Item r = (Item) Stock.getByName(p+pieceName,-1);
        r.cancelAdd();
        return r.toString();
    }
    Man getMan(int seed, final String stat, final Faction pfaction, final String moves, final String[] pweapon, final String[] parmor, final String[] pface){
        Man m = new Man(seed){
            public void onCreate(){
                String str = getUnit(getName(),seed,stat,pfaction,moves);
                str += "#"+getItem(pweapon[seed % pweapon.length],-1);
                String helm = getItem(pface[seed % pface.length],0);
                String arm = parmor[seed % parmor.length];
                str += "#"+helm+"&"+ getItem(arm, 2)+"&"+ getItem(arm, 1);
                load(str);
            }
        };
        return m;
    }
}