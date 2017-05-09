package com.ga.unit;

import com.ga.game.Command;
import com.ga.game.Dialog;
import com.ga.map.Global;
import com.ga.map.MapEvent;
import com.ga.map.Updater;
import com.ga.tool.Interactive;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.intelligence.IntelligenceSocial;
import com.ga.unit.intelligence.MerchantIntelligence;
import com.ga.unit.intelligence.ResidentIntelligence;
import com.ga.unit.item.*;
import com.ga.unit.player.Player;
import com.ga.unit.skill.*;

import java.util.ArrayList;

/**
 * Created by Ga on 2/3/2017. asd
 */
public enum Stock {
    Spawn{ protected Prop make(int seed){
        Prop product = new Prop(getName(), seed, 999){
            public void onField(){}
        };
        if (!Updater.editor) product.image.visible = false;
        return process(product);
    }},
    Exit{ Prop make(final int seed){
        Prop product = new Prop("Spawn", seed, 999){
            public void onField(){}
        };
        if (!Updater.editor) product.image.visible = false;
        product.onInteract = new Interactive(){
            public void interact(Man p){
                if (p == Global.player) Global.map.loadMap(Global.map.maps[4]+"@"+seed);
            }
        };
        return process(product);
    }},
    Phone_Booth{ Prop make(int seed){
        Prop product = new Prop(getName(), seed, 5);
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
        Prop product = new Prop(getName(), seed, 5);
        product.onInteract = new Interactive(){
            public void interact(Man p){
                Global.display.menuMap.display();
            }
        };
        return process(product);
    }},
    Fist{ Prop make(int seed){
        Prop product = new Weapon(getName(),0,0,-1,1,4, Item.ItemType.Spear){
            public void use(final Man host) {
                host.setState(Actions.Interact);
            }
            public String forceToString() {
                return "";
            }
            public void drop(Man host) {}
            public void onUnequip(Man host){}
        };
        return process(product);
    }},
    Knuckle{ Prop make(int seed){
        Prop product = new Weapon("Fist",0,0,-1,1,2, Item.ItemType.Spear){
            public void drop(Man host) {}
        };
        product.image.setCenterType(3);
        return process(product);
    }},
    Stick{ Prop make(int seed){ //3
        Prop product = new Weapon(getName(),seed,1,10,1,2, Item.ItemType.Mace);
        return process(product);
    }},
    Wooden_Sword{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed,1,30,1,3, Item.ItemType.Mace);
        product.image.setCenterType(3);
        return process(product);
    }},
    Broadsword{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed,3,100,1,6, Item.ItemType.Sword);
        product.image.setCenterType(3);
        return process(product);
    }},
    Bamboo_Spear{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed,1,12,1,4, Item.ItemType.Spear);
        return process(product);
    }},
    Mace{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed,3,-1,1,8, Item.ItemType.Mace){
            public void randomSize(P p){}
        };
        return process(product);
    }},
    Katana{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed,2,40,1,6, Item.ItemType.Sword);
        product.image.setCenterType(3);
        return process(product);
    }},
    Claw{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed,1,60,1,4, Item.ItemType.Claw);
        product.image.setCenterType(3);
        return process(product);
    }},
    Spear{ Prop make(int seed){ //10
        Prop product = new Weapon(getName(),seed,2,100,1,4, Item.ItemType.Spear);
        return process(product);
    }},
    Axe{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed,2,100,1,4, Item.ItemType.Sword);
        return process(product);
    }},
    Dagger{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed,1,100,1,3, Item.ItemType.Dagger);
        product.image.setCenterType(3);
        return process(product);
    }},
    Tanto{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed,1,100,1,3, Item.ItemType.Dagger);
        product.image.setCenterType(3);
        return process(product);
    }},
    Pickaxe { Prop make(int seed){
        Prop product = new Weapon(getName(),seed,2,60,1,4, Item.ItemType.Sword);
        return process(product);
    }},
    Cleaver{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed,4,100,1,6, Item.ItemType.Sword);
        product.image.setCenterType(3);
        return process(product);
    }},
    Lancesword{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed,3,300,1,6, Item.ItemType.Spear);
        product.image.setCenterType(3);
        return process(product);
    }},
    Cutlass{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed,3,240,1,6, Item.ItemType.Sword);
        product.image.setCenterType(3);
        return process(product);
    }},
    Claymore{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed,4,200,1,8, Item.ItemType.Sword);
        product.image.setCenterType(3);
        return process(product);
    }},
    Halberd{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed,5,100,1,8, Item.ItemType.Sword);
        return process(product);
    }},
    Greatsword{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed,6,100,2,6, Item.ItemType.Sword);
        product.image.setCenterType(3);
        return process(product);
    }},
    Warhammer{ Prop make(int seed){ //20
        Prop product = new Weapon(getName(),seed,7,-1,2,8, Item.ItemType.Mace);
        return process(product);
    }},
    Bow{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed,1,6,1,6, Item.ItemType.Bow);
        return process(product);
    }},
    Revolver{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed,1,6,1,8, Item.ItemType.Shoot);
        product.image.setCenterType(3);
        return process(product);
    }},
    Pistol{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed,1,12,1,8, Item.ItemType.Shoot);
        product.image.setCenterType(3);
        return process(product);
    }},
    Rifle{ Prop make(int seed){
        Prop product = new Weapon(getName(),seed,3,2,2,6, Item.ItemType.Snipe);
        product.image.setCenterType(3);
        return process(product);
    }},
    Ethersword{ Prop make(int seed){
        Weapon product = new Weapon(getName(),seed,0,-1,1,4, Item.ItemType.Sword){
            public String toString(){
                return "";
            }
            public String getHitSound(){
                return "stun";
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
        product.breakable = true;
        return process(product);
    }},
    Cotton_Clothing_Legging{ Prop make(int seed){
        Armor product = new Armor("Cotton Clothing",seed,1,10,1,1,1);
        product.pocket = 1;
        return process(product);
    }},
    Cotton_Clothing_Torso{ Prop make(int seed){
        Armor product = new Armor("Cotton Clothing",seed,1,10,1,1,2);
        return process(product);
    }},
    Fabric_Clothing_Legging{ Prop make(int seed){
        Armor product = new Armor("Fabric Clothing",seed,1,10,1,1,1);
        product.pocket = 1;
        return process(product);
    }},
    Fabric_Clothing_Torso{ Prop make(int seed){
        Armor product = new Armor("Fabric Clothing",seed,1,10,1,1,2);
        return process(product);
    }},
    Suit_Legging{ Prop make(int seed){
        Armor product = new Armor("Suit",seed,1,10,0,1,1);
        product.pocket = 1;
        return process(product);
    }},
    Suit_Torso{ Prop make(int seed){
        Armor product = new Armor("Suit",seed,1,10,0,1,2);
        product.pocket = 1;
        return process(product);
    }},
    Robe_Legging{ Prop make(int seed){
        Armor product = new Armor("Robe",seed,0,10,0,0,1);
        product.pocket = 1;
        return process(product);
    }},
    Robe_Torso{ Prop make(int seed){
        Armor product = new Armor("Robe",seed,0,10,0,0,2);
        return process(product);
    }},
    Adventurewear_Legging{ Prop make(int seed){ //30
        Armor product = new Armor("Adventurewear",seed,1,10,1,1,1);
        product.pocket = 1;
        return process(product);
    }},
    Adventurewear_Torso{ Prop make(int seed){
        Armor product = new Armor("Adventurewear",seed,1,10,1,1,2);
        return process(product);
    }},
    Leather_Armor_Legging{ Prop make(int seed){
        Armor product = new Armor("Leather Armor",seed,1,10,1,1,1);
        product.pocket = 1;
        return process(product);
    }},
    Leather_Armor_Torso{ Prop make(int seed){
        Armor product = new Armor("Leather Armor",seed,1,10,1,1,2);
        return process(product);
    }},
    Steel_Armor_Legging{ Prop make(int seed){
        Armor product = new Armor("Steel Armor",seed,1,10,1,1,1);
        product.breakable = false;
        product.pocket = 1;
        return process(product);
    }},
    Steel_Armor_Torso{ Prop make(int seed){
        Armor product = new Armor("Steel Armor",seed,1,10,1,1,2);
        product.breakable = false;
        return process(product);
    }},
    Chainmail_Legging{ Prop make(int seed){
        Armor product = new Armor("Chainmail",seed,1,10,1,1,1);
        product.breakable = false;
        product.pocket = 1;
        return process(product);
    }},
    Chainmail_Torso{ Prop make(int seed){
        Armor product = new Armor("Chainmail",seed,1,10,1,1,2);
        product.breakable = false;
        return process(product);
    }},
    Platemail_Legging{ Prop make(int seed){
        Armor product = new Armor("Platemail",seed,1,10,1,1,1);
        product.breakable = false;
        product.pocket = 1;
        return process(product);
    }},
    Platemail_Torso{ Prop make(int seed){
        Armor product = new Armor("Platemail",seed,1,10,1,1,2);
        product.breakable = false;
        return process(product);
    }},
    Shadowrobe_Legging{ Prop make(int seed){
        Armor product = new Armor("Shadowrobe",seed,1,10,1,1,1);
        product.pocket = 1;
        return process(product);
    }},
    Shadowrobe_Torso{ Prop make(int seed){
        Armor product = new Armor("Shadowrobe",seed,1,10,1,1,2);
        return process(product);
    }},
    Bandana{ Prop make(int seed){
        Armor product = new Armor(getName(),seed,0,3,0,0,0){
            public void onCreate(){
                imbueMeta(true,true,.2f,true);
            }
        };
        product.breakable = true;
        return process(product);
    }},
    Mask{ Prop make(int seed){
        Armor product = new Armor(getName(),seed,0,3,0,0,0){
            public void onCreate(){
                imbueMeta(true,true,.2f,true);
            }
        };
        product.breakable = true;
        return process(product);
    }},
    Lace_Helm{ Prop make(int seed){
        Armor product = new Armor(getName(),seed,1,10,1,1,0);
        return process(product);
    }},
    Full_Helm{ Prop make(int seed){ //70
        Armor product = new Armor(getName(),seed,1,10,1,1,0);
        return process(product);
    }},
    Legion_Helm{ Prop make(int seed){
        Armor product = new Armor(getName(),seed,1,10,1,1,0);
        return process(product);
    }},
    Blood{ Prop make(int seed){
        Prop product = new Item(getName(), seed, 0, -1){
            public void use(final Man host){}
        };
        return process(product);
    }},
    Weed{ Prop make(int seed){
        final Prop product = new Prop(varies("Weed",seed,2), seed, 49);
        isNatural(product);
        return process(product);
    }},
    Root{ Prop make(int seed){
        Prop product = new Prop(varies("Root",seed,3), seed, 49);
        isNatural(product);
        return process(product);
    }},
    Vine{ Prop make(int seed){ //75
        Prop product = new Prop(varies("Vine",seed,2), seed, 49);
        isNatural(product);
        return process(product);
    }},
    Rock{ Prop make(int seed){
        Item product = new Item(varies("Rock",seed,2), seed, 4, -1);
        isNatural(product);
        return process(product);
    }},
    Stone{ Prop make(int seed){ //75
        Prop product = new Prop(varies("Stone",seed,3), seed, 49);
        isNatural(product);
        product.tags.add(Tag.Hideable);
        return process(product);
    }},
    Bush{ Prop make(int seed){
        Prop product = new Prop(varies("Bush",seed,2), seed, 49);
        isNatural(product);
        product.tags.add(Tag.Hideable);
        product.gravity = true;
        return process(product);
    }},
    XBush{ Prop make(int seed){
        Prop product = new Prop(varies("DeadBush",seed,2), seed, 49);
        isNatural(product);
        product.tags.add(Tag.Hideable);
        product.gravity = true;
        return process(product);
    }},
    Tree{ Prop make(int seed){
        Prop product = new Prop(varies("Tree",seed,4), seed, 49);
        isNatural(product);
        product.tags.add(Tag.Hideable);
        return process(product);
    }},
    Door{ Prop make(int seed){
        Prop product = new Prop(varies("Door",seed,2), seed, 49);
        product.gravity = false;
        return process(product);
    }},
    SteamHouse{ Prop make(final int seed){
        final Prop entrance = Stock.Spawn.create(Global.getHouseId());
        final Prop product = new Prop(varies("SteamHouse",seed,4), seed, 49);
        product.pos = new P(){
            public void load(String p){
                super.load(p);
                entrance.pos.add(x,y);
            }
        };
        entrance.onInteract = new Interactive(){
            public void interact(Man p){
                if (p == Global.player && p.lastLand.depth == product.getDepth()+2){
                    Global.map.loadMap("House_"+Global.map.id+"_"+entrance.seed+"/"+product.image.getSize().getAdd(64,64)+"&0@"+entrance.seed);
                }
            }
        };
        entrance.save = false;
        isFurniture(product);
        if (product.name.endsWith("1")) entrance.pos.set(product.dir*42,50);
        else if (product.name.endsWith("2")) entrance.pos.set(product.dir*72,66);
        else if (product.name.endsWith("3")) entrance.pos.set(product.dir*-116,56);
        else if (product.name.endsWith("4")) entrance.pos.set(product.dir*8,49);
        return process(product);
    }},
    TowerHouse{ Prop make(final int seed){
        final ArrayList<Prop> addons = new ArrayList<>();
        final Prop product = new Prop(varies("TowerHouse",seed,3), seed, 49){
            public void setDepth(int depth){
                super.setDepth(depth);
                for (Prop i : addons) i.setDepth(depth);
            }
        };
        product.pos = new P(){
            public void load(String p){
                super.load(p);
                for (Prop i : addons) i.pos.add(x,y);
            }
        };
        isFurniture(product);
        product.randomSize(new P(0,.5f));
        float baseSizeX = product.image.getSize().x-64;
        float baseSizeY = product.image.getSize().y-64;
        Prop door = makeAddon(varies("Door",seed,2));
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
                Prop window = makeAddon(varies("Window",seed,3));
                window.pos = new P(xmargin*(ix+1)-(baseSizeX/2), -ymargin*(iy+1)-door.image.getSize().y/2);
                addons.add(window);
            }
        }
        if (product.rand.nextBoolean()) {
            for (int i = 0; i < product.rand.nextInt(3); i++) {
                Prop appliance = makeAddon(varies("Appliance",seed,3));
                appliance.pos = new P(product.rand.nextInt((int) baseSizeX)-baseSizeX/2, -product.rand.nextInt((int) baseSizeY-64));
                addons.add(appliance);
            }
        }
        for (int i=0;i<product.rand.nextInt(4);i++) {
            final Prop clockwork = new Prop(varies("Clockwork",seed,6),seed,0){
                private int speed;
                public void onField(){}
                public String toString(){
                    return "";
                }
                public String[] load(String p){
                    String[] r = super.load(p);
                    speed = rand.nextInt(50)-25;
                    return r;
                }
                public void onCreate(){
                    super.onCreate();
                    speed = rand.nextInt(50)-25;
                }
                public void update(){
                    rotation+=speed/image.getSize().x;
                }
                public void randomSize(P sizeVariance){
                    P s = image.getSize();
                    float rx = rand.nextFloat() * (sizeVariance.x*2) + (1-sizeVariance.x);
                    image.setSize(s.x * rx, s.y * rx);
                }
            };
            clockwork.randomSize(new P(.5f,.5f));
            int clockworkY = (int) clockwork.image.getSize().y;
            clockwork.pos = new P(product.rand.nextInt((int) baseSizeX)-baseSizeX/2, product.rand.nextInt((int) baseSizeY-clockworkY/2)+clockworkY);
            addons.add(clockwork);
        }
        final Prop entrance = Stock.Spawn.create(Global.getHouseId());
        entrance.save = false;
        entrance.pos = doorPos.getAdd(0,-door.image.getSize().y/2);
        addons.add(entrance);
        entrance.onInteract = new Interactive(){
            public void interact(Man p){
                if (p == Global.player && p.lastLand.depth == product.getDepth()+2){
                    Global.map.loadMap("House_"+Global.map.id+"_"+entrance.seed+"/"+product.image.getSize().getAdd(64,64)+"&"+finalHouseType+"&"+ywindows+"@"+entrance.seed);
                }
            }
        };
        return process(product);
    }},
    TowerTip{ Prop make(int seed){
        final Prop fan = new Prop(varies("Propeller",seed,2),seed,0){
            private int speed;
            public void onField(){}
            public String toString(){
                return "";
            }
            public String[] load(String p){
                String[] r = super.load(p);
                speed = rand.nextInt(50);
                return r;
            }
            public void onCreate(){
                super.onCreate();
                speed = rand.nextInt(50);
            }
            public void update(){
                rotation+=speed/image.getSize().x;
            }
            public void randomSize(P sizeVariance){
                P s = image.getSize();
                float rx = rand.nextFloat() * (sizeVariance.x*2) + (1-sizeVariance.x);
                image.setSize(s.x * rx, s.y * rx);
            }
        };
        Prop product = new Prop(varies(getName(),seed,2), seed, 15){
            public void setDepth(int depth){
                super.setDepth(depth);
                fan.setDepth(depth+(seed%2==0?1:-1));
            }
        };
        product.pos = new P(){
            public void load(String p){
                super.load(p);
                fan.pos.add(x,y);
            }
        };
        isFurniture(product);
        if (seed % 3 != 0) fan.image.visible = false;
        fan.randomSize(new P(.5f,.5f));
        fan.pos = new P(0,-product.image.getSize().y+4);
        return process(product);
    }},
    Grass{ Prop make(int seed){
        Item product = new Item(getName(), -1, 0, 1){
            public void use(Man p){}
        };
        product.setType(Item.ItemType.Consumable);
        return process(product);
    }},
    Battery{ Prop make(int seed){
        Item product = new Item(getName(), seed, 1, 10){
            public void use(Man host){}
        };
        product.setType(Item.ItemType.Scroll);
        product.delay = 25;
        return process(product);
    }},
    Guidebook{ Prop make(int seed){
        Item product = new Item(getName(), seed, 0, -1){
            @Override
            public void use(Man host){
                super.use(host);
                String stage = "0";
                if (Global.map.event == MapEvent.Tutorial) stage = MapEvent.cookie;
                Global.display.menuDialog.show("Guidebook by Whu", Dialog.get("tutorial"+stage));
                if (stage.equals("4")) remove();
            }
        };
        product.setType(Item.ItemType.Scroll);
        product.delay = 25;
        return process(product);
    }},
    Manual{ Prop make(int seed){
        Item product = new Item(getName(), seed, 1, 1){
            private Actions skill;
            public void onCreate(){
                super.onCreate();
                skill = Actions.values()[seed % Actions.values().length];
            }
            public String[] load(String p){
                String[] r = super.load(p);
                skill = Actions.values()[seed % Actions.values().length];
                return r;
            }
            @Override
            public void use(Man host){
                if (host instanceof Player) {
                    super.use(host);
                    Command.order("rest");
                    ((Player)host).learning = skill;
                    host.say("Now learning "+skill.getName()+"!");
                }
            }
            public String getName(){
                return super.getName()+" of "+skill.getName();
            }
            public String getDescription(){
                return super.getDescription()+" "+Dialog.get("itemInsc",new String[]{skill.getName()});
            }
        };
        product.value = 94;
        product.setType(Item.ItemType.Enchant);
        product.delay = 45;
        return process(product);
    }},
    Bandage{ Prop make(int seed){
        Item product = new Item(getName(), seed, 1, 1) {
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
        product.setType(Item.ItemType.Consumable);
        product.delay = 45;
        return process(product);
    }},
    Potion{ Prop make(int seed){
        Item product = new Item(getName(), seed, 1, 1) {
            public void onCreate() {
                if (seed != 0) imbueMeta(false, false, 1, true);
            }
            public void use(Man host) {
                if (usable(1)){
                    super.use(host);
                    host.buff(emod, ademod.level);
                }
            }
            public String getName(){
                return super.getName()+" of "+emod.name();
            }
            public String getDescription(){
                return super.getDescription()+" "+Dialog.get("itemInsc",new String[]{emod.name()});
            }
        };
        product.setType(Item.ItemType.Consumable);
        product.delay = 45;
        return process(product);
    }},
    Scroll{ Prop make(int seed){
        Item product = new Item(getName(), seed, 0, 1){
            private Actions spell;
            @Override
            public void use(Man host){
                if (usable(1)) {
                    super.use(host);
                    if (spell != null && rand.nextFloat() < probability){
                        host.stateLevel = 3;
                        spell.trigger(host);
                    }
                }
            }
            public void onCreate(){
                super.onCreate();
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
                return super.getName()+" of "+spell.getName();
            }
            public String getDescription(){
                return super.getDescription()+" "+Dialog.get("itemInsc",new String[]{spell.getName()});
            }
        };
        product.breakable = true;
        product.value = 16;
        product.probability = 1;
        product.setType(Item.ItemType.Scroll);
        product.delay = 30;
        return process(product);
    }},
    Fireball{ Prop make(int seed){
        Item product = new Item(getName(), seed, 1, 3) {
            @Override
            public void use(final Man host) {
                super.use(host);
                host.delay.performer = new Performer() {
                    public void perform() {
                        final Projectile p = new Projectile("Fireball", host.pos.getAdd(host.clip.organs[BodyHumanoid.FIST].ptd), host.pos.getAdd(host.target), 20 + host.get("Stamina").get()) {
                            @Override
                            public void stuck() {
                                Particle.explosion(pos);
                                explode(60);
                                remove();
                            }

                            @Override
                            public void update() {
                                super.update();
                                Particle.dripFire(pos);
                            }
                        };
                        p.faction = host.faction;
                        p.damage = 50;
                        p.gravity = true;
                        p.delay.set(50);
                        p.delay.performer = new Performer() {
                            public void perform() {
                                p.stuck();
                            }
                        };
                    }
                };
            }
        };
        product.breakable = true;
        product.value = 16;
        product.delay = 10;
        product.setType(Item.ItemType.Darting);
        return process(product);
    }},
    Throwing_Knife{ Prop make(int seed){
        Item product = new Item(getName(), seed, 1, 10){
            @Override
            public void use(final Man host){
                super.use(host);
                host.delay.performer = new Performer(){
                    public void perform(){
                        final Projectile p = new Projectile("Throwing Knife",host.pos.getAdd(host.clip.organs[BodyHumanoid.OFIST].ptd),host.pos.getAdd(host.target),70){
                            @Override
                            public void stuck(){
                                if (tangible) Particle.explode("Fire", pos, vel, 20, 10, 15, true,3);
                                super.stuck();
                            }
                        };
                        p.rotSpeed = 0;
                        p.faction = host.faction;
                        p.damage = 15;
                        p.gravity = false;
                        p.delay.performer = new Performer(){
                            @Override
                            public void perform(){
                                p.stuck();
                            }
                        };
                    }
                };
            }
        };
        product.image.setCenterType(3);
        product.breakable = true;
        product.value = 16;
        product.delay = 10;
        product.setType(Item.ItemType.Darting);
        return process(product);
    }},
    Shell{ Prop make(int seed){
        Item product = new Item(getName(), seed, 0, 1);
        product.damage = 0;
        return process(product);
    }},
    Starfish{ Prop make(int seed){ //90
        Item product = new Item(getName(), seed, 0, 1);
        product.damage = 0;
        return process(product);
    }},
    Book{ Prop make(int seed){
        Item product = new Item(getName(), seed, 1, -1){
            @Override
            public void use(Man host){
                super.use(host);
                Global.display.menuDialog.show("Title","Content");
            }
        };
        product.setType(Item.ItemType.Scroll);
        product.delay = 45;
        return process(product);
    }},
    Meat{ Prop make(int seed){
        Food product = new Food(getName(), seed, 1, 1, "PPCOX", 0, 5, 0);
        product.weight = 3;
        product.setType(Item.ItemType.Food);
        product.value = 4;
        return process(product);
    }},
    Drumstick{ Prop make(int seed){
        Food product = new Food(getName(), seed, 1, 1, "PPCCOO", 0, 3, 2);
        product.weight = 3;
        product.setType(Item.ItemType.Food);
        product.value = 4;
        return process(product);
    }},
    Porkchop{ Prop make(int seed){
        Food product = new Food(getName(), seed, 1, 1, "PPPOO", 0, 3, 2);
        product.weight = 3;
        product.setType(Item.ItemType.Food);
        product.value = 4;
        return process(product);
    }},
    Meal{ Prop make(int seed){
        Food product = new Food(getName(), seed, 1, 5, "PPPCCCVO", 1, 10, 6);
        product.volume = 2;
        product.setType(Item.ItemType.Food);
        product.value = 12;
        return process(product);
    }},
    Burger{ Prop make(int seed){
        Food product = new Food(getName(), seed, 1, 5, "PPPCCOX", 1, 10, 10);
        product.setType(Item.ItemType.Food);
        product.value = 12;
        return process(product);
    }},
    Sandwich{ Prop make(int seed){
        Food product = new Food(getName(), seed, 1, 5, "PCCCCVO", 1, 10, 2);
        product.setType(Item.ItemType.Food);
        product.value = 9;
        return process(product);
    }},
    Ration{ Prop make(int seed){
        Food product = new Food(getName(), seed, 1, 6, "PPCCV", 6, 1, 1);
        product.setType(Item.ItemType.Food);
        product.value = 16;
        return process(product);
    }},
    Resident{ Prop make(int seed){
        Man product = getMan(seed,getStat(4,10,1),Faction.Helpless,"200/Sprint",
                null,
                new String[]{"Cotton_Clothing","Fabric_Clothing"},null);
        product.speechType = "resident";
        product.brain = new ResidentIntelligence(product);
        return process(product);
    }},
    Merchant{ Prop make(int seed){
        Man product = getMan(seed,getStat(4,10,1),Faction.Neutral,"200/Sprint",
                null,
                new String[]{"Suit"},null);
        product.speechType = "merchant";
        product.brain = new MerchantIntelligence(product, seed);
        return process(product);
    }},
    Guard{ Prop make(int seed){
        Man product = getMan(seed,getStat(9,20,3),Faction.Neutral,"10,20,800/Use&Dash&Sprint",
                new String[]{"Broadsword","Spear"},
                new String[]{"Leather_Armor"},new String[]{"Lace_Helm",null});
        product.speechType = "soldier";
        product.brain = new IntelligenceSocial(product);
        return process(product);
    }},
    Punk{ Prop make(int seed){
        Man product = getMan(seed,getStat(6,20,2),Faction.Punk,"10,80,200,1200/Use&Dash,Walk&Walk&Sprint",
                new String[]{"Dagger","Axe","Pickaxe"},
                new String[]{"Cotton_Clothing", "Fabric_Clothing"},null);
        product.speechType = "punk";
        product.brain = new IntelligenceSocial(product);
        return process(product);
    }},
    Yeller{ Prop make(int seed){
        Man product = getMan(seed,getStat(3,24,3),Faction.Punk,"10,80,700,1200/Use&Backstep&Calling&Sprint",
                new String[]{"Stick","Dagger",null},
                new String[]{"Cotton_Clothing"},new String[]{"Bandana","Mask"});
        product.speechType = "punk";
        product.brain = new IntelligenceSocial(product);
        return process(product);
    }},
    Phantom{ Prop make(int seed){
        Man product = getMan(seed,getStat(12,24,9),Faction.Evil,"10,20,120,1200/Use&Dash,Flameroll&Fireblast,Stun&Shadowstep,Meteor,Blizzard",
                new String[]{"Greatsword","Halberd","Warhammer"},
                new String[]{"Shadowrobe"},null);
        product.brain = new IntelligenceSocial(product);
        product.clip.organs[5].setImage("Phantom");
        return process(product);
    }},
    Dog { Prop make(int seed) {
        Unit product = new Unit(seed){
            public void stateSet(Actions state){
                super.stateSet(state);
                if (state == Actions.Thrust){
                    delay.time = 24;
                    vel.x += getSpeed()*12*dir;
                    vel.y -= 20;
                }
            }
            public boolean attack(){
                boolean r = super.attack();
                if (r) vel.x = 0;
                return r;
            }
            public void stateUpdate(){
                super.stateUpdate();
                if (state == Actions.Thrust) attack();
                if (state == Actions.Sprint) vel.x *= 2;
            }
            public void onCreate(){
                load(getUnit(name,seed,getStat(3,12,3),Faction.Evil,"10,140,1200/Kick&Thrust,Walk&Sprint"));
            }
        };
        processUnit(product);
        return process(product);
    }},
    Slime { Prop make(int seed) {
        Unit product = new Unit(seed){
            public void stateSet(Actions state) {
                super.stateSet(state);
                if (state == Actions.Kick) {
                    damageMod = 2;
                    moveTriggered(0);
                    delay.time = 24;
                } else if (state == Actions.Jump){
                    damageMod = 2;
                    vel.y -= 6;
                    get("Stamina").heal();
                }
            }
            public void onAttack(Unit p){
                p.buff(Buffs.Poison, 1);
            }
            public void stateUpdate(){
                super.stateUpdate();
                if (state == Actions.Jump && Math.abs(vel.y) < 32){
                    Particle.explode("Poison",pos,new P(),8,8,0,false,4);
                    explode(156);
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
                load(getUnit(getName(),seed,getStat(7,10,1),Faction.Evil,"5,800/Kick&Walk"));
            }
        };
        product.tags.remove(Tag.Combustible);
        processUnit(product);
        product.clip.organs[0].setSize((int) product.clip.organs[0].getImage().getSize().x);
        return process(product);
    }},
    Mantis { Prop make(int seed) {
        Unit product = new Unit(seed){
            public void stateSet(Actions state){
                super.stateSet(state);
                if (state == Actions.Thrust){
                    delay.time = 30;
                    vel.x += getSpeed()*3*dir;
                    vel.y -= 20;
                }
            }
            public void onCreate(){
                load(getUnit(getName(),seed,getStat(10,20,12),Faction.Evil,"10,800/Kick&Walk"));
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
                                vel.y += ((i.pt.y + (i.getSlope() * (pos.x + vel.x - i.pt.x))) - (pos.y + 200)) * .01f;
                                break;
                            }
                        }
                    }
                }
            }
            public void onCreate(){
                load(getUnit(getName(),seed,getStat(30,10,3),Faction.Evil,"300/Fireblast,Idle,Idle,Idle"));
            }
        };
        if (seed % 2 == 1) product.loadActions("200,800/Flamestride,Flameroll&Flameroll,Fireball");
        product.gravity = false;
        processUnit(product);
        return process(product);
    }},
    //Uniques
    Stomach_Full_Helm{ Prop make(int seed){
        Armor product = new Armor("Full Helm",seed,0,10,1,0,0){
            public String getDescription(){
                return super.getDescription()+" Something is odd about this helmet.";
            }
        };
        product.tags.add(Tag.Cunning);
        product.tags.add(Tag.Fire);
        product.tags.add(Tag.Air);
        return process(product);
    }};
    public static Prop get(String p){
        Prop t;
        String[] sections = p.split("@");
        int seed = Integer.parseInt(sections[1]);
        if (seed == -1) seed = Global.getSeed();
        if (sections[0].charAt(0) == '!'){
            t = getInstant(sections[0], seed);
        } else {
            int index = Integer.parseInt(sections[0]);
            t = values()[index].make(seed);
            t.load(p);
        }
        return t;
    }
    public static Prop getInstant(String p, int seed){
        Prop t = new Prop(p.substring(1), seed, 5);
        t.gravity = false;
        t.image.setCenterType(2);
        randomSide(t);
        return t;
    }
    abstract Prop make(int seed);
    public Prop create(int seed){
        if (seed == -1) seed = Global.getSeed();
        Prop r = make(seed);
        r.onCreate();
        return r;
    }
    public String getName(){
        return name().replace("_"," ");
    }
    public static Prop getByName(String p, int seed){
        for (Stock i : values()){
            if (i.name().equals(p)){
                return i.create(seed);
            }
        }
        return getInstant(p, seed);
//        try {
//            Stock a = valueOf(p);
//            Prop b = a.create(seed);
//            return b;
//        } catch (Exception e){
//            System.out.println("Stock could not find "+p);
//            return getInstant(p, seed);
//        }
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
        Prop r = new Prop(name,-1,0){
            public void onField(){}
            public String toString(){
                return "";
            }
        };
        isFurniture(r);
        return r;
    }
    static void randomSide(Prop p){
        p.dir = p.seed % 2 == 0 ? 1 : -1;
    }
    String varies(String img, int seed, int num){
        return img+(Math.abs(seed % num)+1);
    }
    String getStat(int stamina, int speed, int level){
        return "Stamina:"+stamina+"&Attack:0,0&Speed:"+speed+"&Strength:"+level+"&Level:"+level;
    }
    String getUnit(String name, int seed, String stat, Faction faction, String moves){
        return name+"@"+seed+"@0,0@0@"+stat+"@"+faction.name()+"@"+moves;
    }
    Unit processUnit(Unit p){
        p.clip = new Body(p, getName());
        p.brain = new IntelligenceSocial(p);
        return p;
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
                str += "#"+(pweapon==null?"": getItem(pweapon[seed % pweapon.length],-1));
                String helm = pface==null?"None": getItem(pface[seed % pface.length],0);
                String arm = parmor==null?null:parmor[seed % parmor.length];
                str += "#"+helm+"&"+ getItem(arm, 2)+"&"+ getItem(arm, 1);
                load(str);
            }
        };
        String[] manHeads = new String[]{"Man0","Man1","Man2","Man3","Man4","Man5","Man6"};
        m.setHead(manHeads[seed % manHeads.length]);
        return m;
    }
}