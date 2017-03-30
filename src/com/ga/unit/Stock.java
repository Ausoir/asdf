package com.ga.unit;

import com.ga.game.Dialog;
import com.ga.game.Display;
import com.ga.map.Global;
import com.ga.map.MapEvent;
import com.ga.tool.Interactive;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.intelligence.IntelligenceSocial;
import com.ga.unit.intelligence.MerchantIntelligence;
import com.ga.unit.intelligence.ResidentIntelligence;
import com.ga.unit.item.*;
import com.ga.unit.skill.*;

import java.util.ArrayList;

/**
 * Created by Ga on 2/3/2017. asd
 */
public class Stock {
    private static Factory[] factory = new Factory[]{
            new Factory("Spawn"){ protected Prop make(int seed){
                Prop product = new Prop(name, seed, 999){
                    public void onField(){}
                };
                if (!Updater.editor) product.image.visible = false;
                return product;
            }},
            new Factory("Exit"){ Prop make(final int seed){
                Prop product = new Prop("Spawn", seed, 999){
                    public void onField(){}
                };
                if (!Updater.editor) product.image.visible = false;
                product.onInteract = new Interactive(){
                    public void interact(Man p){
                        if (p == Global.player) Global.map.loadMap(Global.map.maps[4]+"@"+seed);
                    }
                };
                return product;
            }},
            new Factory("Phone_Booth"){ Prop make(int seed){
                Prop r = new Prop(name, seed, 5);
                r.onInteract = new Interactive(){
                    public void interact(Man p){
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
            new Factory("Map"){ Prop make(int seed){
                Prop r = new Prop(name, seed, 5);
                r.onInteract = new Interactive(){
                    public void interact(Man p){
                        Display.menuMap.display();
                    }
                };
                return r;
            }},
            new Factory("Stick"){ Prop make(int seed){ //3
                return new Weapon(name,seed,1,10,6,1,Item.Tag.Mace); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Wooden Sword"){ Prop make(int seed){
                return new Weapon(name,seed,1,30,6,1,Item.Tag.Mace); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Broadsword"){ Prop make(int seed){
                return new Weapon(name,seed,3,100,6,1,Item.Tag.Sword); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Bamboo Spear"){ Prop make(int seed){
                return new Weapon(name,seed,1,12,6,1,Item.Tag.Spear); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Mace"){ Prop make(int seed){
                return new Weapon(name,seed,4,-1,4,2,Item.Tag.Mace); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Katana"){ Prop make(int seed){
                return new Weapon(name,seed,2,40,6,1,Item.Tag.Sword); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Claw"){ Prop make(int seed){
                return new Weapon(name,seed,1,60,6,1,Item.Tag.Claw); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Spear"){ Prop make(int seed){ //10
                return new Weapon(name,seed,2,100,6,1,Item.Tag.Spear); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Axe"){ Prop make(int seed){
                return new Weapon(name,seed,4,100,6,1,Item.Tag.Sword); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Dagger"){ Prop make(int seed){
                return new Weapon(name,seed,1,100,6,1,Item.Tag.Dagger); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Pick"){ Prop make(int seed){
                return new Weapon(name,seed,3,60,16,1,Item.Tag.Sword); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Cleaver"){ Prop make(int seed){
                return new Weapon(name,seed,5,100,6,3,Item.Tag.Sword); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Lancesword"){ Prop make(int seed){
                return new Weapon(name,seed,3,300,20,1,Item.Tag.Spear); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Cutlass"){ Prop make(int seed){
                return new Weapon(name,seed,4,240,12,2,Item.Tag.Sword); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Claymore"){ Prop make(int seed){
                return new Weapon(name,seed,6,200,12,3,Item.Tag.Sword); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Halberd"){ Prop make(int seed){
                return new Weapon(name,seed,7,100,20,3,Item.Tag.Sword); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Greatsword"){ Prop make(int seed){
                return new Weapon(name,seed,8,100,20,4,Item.Tag.Sword); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Warhammer"){ Prop make(int seed){ //20
                return new Weapon(name,seed,10,-1,20,4,Item.Tag.Mace); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Revolver"){ Prop make(int seed){
                return new Weapon(name,seed,1,6,20,2,Item.Tag.Shoot); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Pistol"){ Prop make(int seed){
                return new Weapon(name,seed,1,12,20,2,Item.Tag.Shoot); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Rifle"){ Prop make(int seed){
                return new Weapon(name,seed,3,2,20,4,Item.Tag.Snipe); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Ethersword"){ Prop make(int seed){
                Weapon product = new Weapon(name,seed,0,1,6,2,Item.Tag.Sword){
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
                    public boolean use(int p) {
                        boolean r = super.use(p);
                        if (r && getOrgan() != null) {
                            Particle.holyExplosion(pos.getAdd(getOrgan().ptd));
                        }
                        return r;
                    }
                    public void update(){
                        super.update();
                        Particle.explode("Glare",pos,new P(0,-1),10,24,20,false,7);
                        if (Math.random() < .03f) remove();
                    }
                };
                product.breakable = true;
                return product;
            }},
            new Factory("Cotton Clothing Legging"){ Prop make(int seed){
                Armor product = new Armor("Cotton Clothing",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                product.pocket = 1;
                return product;
            }},
            new Factory("Cotton Clothing Torso"){ Prop make(int seed){
                return new Armor("Cotton Clothing",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Fabric Clothing Legging"){ Prop make(int seed){
                Armor product = new Armor("Fabric Clothing",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                product.pocket = 1;
                return product;
            }},
            new Factory("Fabric Clothing Torso"){ Prop make(int seed){
                return new Armor("Fabric Clothing",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Suit Legging"){ Prop make(int seed){
                Armor product = new Armor("Suit",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                product.pocket = 1;
                return product;
            }},
            new Factory("Suit Torso"){ Prop make(int seed){
                return new Armor("Suit",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Adventurewear Legging"){ Prop make(int seed){ //30
                Armor product = new Armor("Adventurewear",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                product.pocket = 1;
                return product;
            }},
            new Factory("Adventurewear Torso"){ Prop make(int seed){
                return new Armor("Adventurewear",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Leather Armor Legging"){ Prop make(int seed){
                Armor product = new Armor("Leather Armor",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                product.pocket = 1;
                return product;
            }},
            new Factory("Leather Armor Torso"){ Prop make(int seed){
                return new Armor("Leather Armor",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Steel Armor Legging"){ Prop make(int seed){
                Armor product = new Armor("Steel Armor",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                product.breakable = false;
                product.pocket = 1;
                return product;
            }},
            new Factory("Steel Armor Torso"){ Prop make(int seed){
                Armor product = new Armor("Steel Armor",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
                product.breakable = false;
                return product;
            }},
            new Factory("Chainmail Legging"){ Prop make(int seed){
                Armor product = new Armor("Chainmail",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                product.breakable = false;
                product.pocket = 1;
                return product;
            }},
            new Factory("Chainmail Torso"){ Prop make(int seed){
                Armor product = new Armor("Chainmail",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
                product.breakable = false;
                return product;
            }},
            new Factory("Platemail Legging"){ Prop make(int seed){
                Armor product = new Armor("Platemail",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                product.breakable = false;
                product.pocket = 1;
                return product;
            }},
            new Factory("Platemail Torso"){ Prop make(int seed){
                Armor product = new Armor("Platemail",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
                product.breakable = false;
                return product;
            }},
            new Factory("Shadowrobe Legging"){ Prop make(int seed){
                Armor product = new Armor("Shadowrobe",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                product.pocket = 1;
                return product;
            }},
            new Factory("Shadowrobe Torso"){ Prop make(int seed){
                return new Armor("Shadowrobe",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Lace Helm"){ Prop make(int seed){
                Armor product = new Armor(name,seed,1,10,1,1,0); //name,seed,weight,uses,armor,encumberance,piece
                product.breakable = false;
                return product;
            }},
            new Factory("Full Helm"){ Prop make(int seed){ //70
                return new Armor(name,seed,1,10,1,1,0); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Legion Helm"){ Prop make(int seed){
                return new Armor(name,seed,1,10,1,1,0); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Blood"){ Prop make(int seed){
                return new Item(name, seed, 0, -1){
                    @Override
                    public boolean use(final Man host){
                        return false;
                    }
                };
            }},
            new Factory("Weed"){ Prop make(int seed){
                final Prop product = new Prop(varies("Weed",seed,2), seed, 49);
                isNatural(product);
                product.onInteract = new Interactive(){
                    public void interact(Man p){
                        p.setWeapon((Item) getByName("Grass",-1));
                        product.remove();
                    }
                };
                return product;
            }},
            new Factory("Root"){ Prop make(int seed){
                Prop product = new Prop(varies("Root",seed,3), seed, 49);
                isNatural(product);
                return product;
            }},
            new Factory("Vine"){ Prop make(int seed){ //75
                Prop product = new Prop(varies("Vine",seed,2), seed, 49);
                isNatural(product);
                return product;
            }},
            new Factory("Rock"){ Prop make(int seed){
                Item product = new Item(varies("Rock",seed,2), seed, 4, -1){
                    public boolean use(final Man host){
                        return false;
                    }
                };
                isNatural(product);
                return product;
            }},
            new Factory("Stone"){ Prop make(int seed){ //75
                Prop product = new Prop(varies("Stone",seed,3), seed, 49);
                isNatural(product);
                product.tags.add(Tag.Hideable);
                return product;
            }},
            new Factory("Bush"){ Prop make(int seed){
                Prop product = new Prop(varies("Bush",seed,2), seed, 49);
                isNatural(product);
                product.tags.add(Tag.Hideable);
                product.gravity = true;
                return product;
            }},
            new Factory("XBush"){ Prop make(int seed){
                Prop product = new Prop(varies("DeadBush",seed,2), seed, 49);
                isNatural(product);
                product.tags.add(Tag.Hideable);
                product.gravity = true;
                return product;
            }},
            new Factory("Tree"){ Prop make(int seed){
                Prop product = new Prop(varies("Tree",seed,4), seed, 49);
                isNatural(product);
                product.tags.add(Tag.Hideable);
                product.gravity = false;
                return product;
            }},
            new Factory("SteamHouse"){ Prop make(final int seed){
                final Prop entrance = Stock.getByName("Spawn",Global.getTempHouseId());
                final Prop product = new Prop(varies("SteamHouse",seed,4), seed, 49){
                    public void adjustToLand(){
                        super.adjustToLand();
                        if (Math.abs(rotation) > .1f) rotation = 0;
                    }
                };
                product.pos = new P(){
                    public void load(String p){
                        super.load(p);
                        entrance.pos.add(x,y);
                    }
                };
                entrance.onInteract = new Interactive(){
                    public void interact(Man p){
                        if (p == Global.player && p.lastLand.depth == product.getDepth()+2){
                            Global.map.loadMap("House_"+entrance.seed+"/"+product.image.getSize().getAdd(64,64)+"@"+entrance.seed);
                        }
                    }
                };
                entrance.save = false;
                isFurniture(product);
                if (product.name.endsWith("1")) entrance.pos.set(product.dir*42,50);
                else if (product.name.endsWith("2")) entrance.pos.set(product.dir*72,66);
                else if (product.name.endsWith("3")) entrance.pos.set(product.dir*-116,56);
                else if (product.name.endsWith("4")) entrance.pos.set(product.dir*8,49);
                return product;
            }},
            new Factory("TowerHouse"){ Prop make(final int seed){
                final ArrayList<Prop> addons = new ArrayList<>();
                final Prop product = new Prop(varies("TowerHouse",seed,3), seed, 49){
                    public void adjustToLand(){
                        super.adjustToLand();
                        if (Math.abs(rotation) > .1f) rotation = 0;
                    }
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
                P doorPos = new P(product.rand.nextInt(doorWidth)-(doorWidth)/2, baseSizeY/2-(door.image.getSize().y/2)-2);
                door.pos = doorPos;
                addons.add(door);
                final Prop entrance = Stock.getByName("Spawn",Global.getTempHouseId());
                entrance.save = false;
                entrance.pos = doorPos.getAdd(0,0);
                entrance.onInteract = new Interactive(){
                    public void interact(Man p){
                        if (p == Global.player && p.lastLand.depth == product.getDepth()+2){
                            Global.map.loadMap("House_"+entrance.seed+"/"+product.image.getSize().getAdd(64,64)+"@"+entrance.seed);
                        }
                    }
                };
                addons.add(entrance);
                int xwindows = product.rand.nextInt(4)+1;
                int ywindows = product.rand.nextInt((int) Math.max(1,(baseSizeY-door.image.getSize().y)/100));
                float xmargin = baseSizeX / (xwindows+1);
                float ymargin = baseSizeY / (ywindows+1);
                for (int ix = 0; ix < xwindows; ix++) {
                    for (int iy = 0; iy < ywindows; iy++) {
                        Prop window = makeAddon(varies("Window",seed,3));
                        window.pos = new P(xmargin*(ix+1)-(baseSizeX/2), ymargin*(iy+1)-(baseSizeY/2)-door.image.getSize().y/2);
                        addons.add(window);
                    }
                }
                if (product.rand.nextBoolean()) {
                    for (int i = 0; i < product.rand.nextInt(3); i++) {
                        Prop appliance = makeAddon(varies("Appliance",seed,3));
                        appliance.pos = new P(product.rand.nextInt((int) baseSizeX)-baseSizeX/2, product.rand.nextInt((int) baseSizeY)-baseSizeY/2);
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
                        public void onLoad(){
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
                    clockwork.pos = new P(product.rand.nextInt((int) baseSizeX)-baseSizeX/2, product.rand.nextInt((int) baseSizeY-clockworkY/2)-baseSizeY/2+clockworkY);
                    addons.add(clockwork);
                }
                return product;
            }},
            new Factory("TowerTip"){ Prop make(int seed){
                final Prop fan = new Prop(varies("Propeller",seed,2),seed,0){
                    private int speed;
                    public void onField(){}
                    public String toString(){
                        return "";
                    }
                    public void onLoad(){
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
                Prop product = new Prop(varies(name,seed,2), seed, 15){
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
                if (seed % 3 != 0) fan.image.visible = false;
                fan.randomSize(new P(.5f,.5f));
                fan.pos = new P(0,-product.image.getSize().y/2);
                return product;
            }},
            new Factory("Bridge1"){ Prop make(int seed){
                return new Prop(name, seed, 15);
            }},
            new Factory("Grass"){ Prop make(int seed){
                Item product = new Item(name, -1, 0, 1){
                    public boolean use(Man p){
                        return true;
                    }
                };
                product.setType(Item.Tag.Consumable);
                return product;
            }},
            new Factory("Fist"){ Prop make(int seed){
                Item product = new Item(name, 0, 0, -1) {
                    @Override
                    public boolean use(final Man host) {
                        host.setState(Actions.Interact);
                        return false;
                    }

                    @Override
                    public String forceToString() {
                        return "";
                    }
                    public void drop(Man host) {
                    }
                    public void field() {
                    }
                };
                product.save = false;
                return product;
            }},
            new Factory("Battery"){ Prop make(int seed){
                Item product = new Item(name, seed, 1, 10){
                    public boolean use(Man host){
                        return true;
                    }
                };
                product.setType(Item.Tag.Scroll);
                product.delay = 25;
                return product;
            }},
            new Factory("Guidebook"){ Prop make(int seed){
                Item product = new Item(name, seed, 0, -1){
                    @Override
                    public boolean use(Man host){
                        super.use(host);
                        String stage = "0";
                        if (Global.map.event == MapEvent.Tutorial) stage = MapEvent.cookie;
                        Display.menuDialog.show("Guidebook by Whu", Dialog.get("tutorial"+stage));
                        if (stage.equals("4")) remove();
                        return true;
                    }
                };
                product.setType(Item.Tag.Scroll);
                product.delay = 25;
                return product;
            }},
            new Factory("Spellbook"){ Prop make(int seed){
                Item product = new Item(name, seed, 1, 1){
                    private Spells spell;
                    public void onLoad(){
                        spell = Spells.values()[rand.nextInt(Spells.values().length)];
                    }
                    @Override
                    public boolean use(Man host){
                        if (super.use(host)) {
                            ((Player)host).learnSkill(spell);
                            return true;
                        }
                        return false;
                    }
                    public String getName(){
                        return super.getName()+" of "+spell.getName();
                    }
                    public String getDescription(){
                        return super.getDescription()+" The runes inscribed on this item mean '"+spell.getName()+"'";
                    }
                };
                product.value = 128;
                product.setType(Item.Tag.Enchant);
                product.delay = 45;
                return product;
            }},
            new Factory("Manual"){ Prop make(int seed){
                Item product = new Item(name, seed, 1, 1){
                    @Override
                    public boolean use(Man host){
                        if (super.use(host)) {
                            ((Player)host).learnSkill(Actions.Triple);
                            return true;
                        }
                        return false;
                    }
                };
                product.value = 128;
                product.setType(Item.Tag.Enchant);
                product.delay = 45;
                return product;
            }},
            new Factory("Potion"){ Prop make(int seed){
                Item product = new Item(name, seed, 1, 1) {
                    public void onCreate() {
                        imbueMeta(false, false, 1, true);
                    }
                    public boolean use(Man host) {
                        if (use(1)){
                            super.use(host);
                            host.stats.addEffect(emod, Integer.parseInt(ademod.toString()));
                            return true;
                        }
                        return false;
                    }
                };
                product.setType(Item.Tag.Consumable);
                product.delay = 45;
                return product;
            }},
            new Factory("Scroll"){ Prop make(int seed){
                Item product = new Item(name, seed, 0, 1){
                    private Spells spell;
                    @Override
                    public boolean use(Man host){
                        if (use(1)) {
                            super.use(host);
                            if (spell != null && rand.nextFloat() < probability){
                                spell.getEvent().perform(host, 3);
                                host.state = spell;
                            }
                        }
                        return false;
                    }
                    public void onLoad(){
                        spell = Spells.values()[rand.nextInt(Spells.values().length)];
                    }
                    public String getName(){
                        return super.getName()+" of "+spell.getName();
                    }
                    public String getDescription(){
                        return super.getDescription()+" The runes inscribed on this item mean '"+spell.getName()+"'";
                    }
                };
                product.breakable = true;
                product.value = 16;
                product.probability = 1;
                product.setType(Item.Tag.Scroll);
                product.delay = 30;
                return product;
            }},
            new Factory("Fireball"){ Prop make(int seed){
                Item product = new Item(name, seed, 1, 3) {
                    @Override
                    public boolean use(final Man host) {
                        if (super.use(host)) {
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
                            return true;
                        }
                        return false;
                    }
                };
                product.breakable = true;
                product.value = 16;
                product.delay = 10;
                product.setType(Item.Tag.Darting);
                return product;
            }},
            new Factory("Tanto"){ Prop make(int seed){
                Item product = new Item(name, seed, 1, 10){
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
                            return true;
                        }
                        return false;
                    }
                };
                product.breakable = true;
                product.value = 16;
                product.delay = 10;
                product.setType(Item.Tag.Darting);
                return product;
            }},
            new Factory("Shell"){ Prop make(int seed){
                Item product = new Item(name, seed, 0, 1);
                product.damage = 0;
                return product;
            }},
            new Factory("Starfish"){ Prop make(int seed){ //90
                Item product = new Item(name, seed, 0, 1);
                product.damage = 0;
                return product;
            }},
            new Factory("Book"){ Prop make(int seed){
                Item product = new Item(name, seed, 1, -1){
                    @Override
                    public boolean use(Man host){
                        if (super.use(host)) {
                            Display.menuDialog.show("Title","Content");
                            return true;
                        }
                        return false;
                    }
                };
                product.setType(Item.Tag.Scroll);
                product.delay = 45;
                return product;
            }},
            new Factory("Meat"){ Prop make(int seed){
                Food product = new Food(null, seed, 1, 1, 10, 0, 3);
                product.weight = 3;
                if (seed % 4 == 0) product.name = "Drumstick";
                else if (seed % 3 == 0) product.name = "Porkchop";
                else product.name = "Meat";
                product.setImage(product.name);
                product.setType(Item.Tag.Food);
                product.value = 4;
                return product;
            }},
            new Factory("Meal"){ Prop make(int seed){
                Food product = new Food(null, seed, 1, 5, 4, 1, 10);
                product.volume = 2;
                if (seed % 4 == 0) product.name = "Burger";
                else if (seed % 3 == 0) product.name = "Sandwich";
                else product.name = "Meal";
                product.setImage(product.name);
                product.setType(Item.Tag.Food);
                product.value = 12;
                return product;
            }},
            new Factory("Ration"){ Prop make(int seed){
                Food product = new Food(null, seed, 1, 6, 2, 6, 1);
                if (seed % 4 == 0) product.name = "Ration";
                else if (seed % 3 == 0) product.name = "Ration";
                else product.name = "Ration";
                product.setImage(product.name);
                product.setType(Item.Tag.Food);
                product.value = 16;
                return product;
            }},
            new ManFactory("Resident"){ Prop make(int seed){
                Man product = getMan(seed,getStat(4,30,0,20,1),Faction.Helpless,"200/Sprint",
                        null,
                        new String[]{"Cotton Clothing","Fabric Clothing"});
                product.speechType = "resident";
                product.brain = new ResidentIntelligence(product);
                return product;
            }},
            new ManFactory("Merchant"){ Prop make(int seed){
                Man product = getMan(seed,getStat(4,30,0,20,1),Faction.Neutral,"200/Sprint",
                        null,
                        new String[]{"Suit"});
                product.speechType = "merchant";
                product.brain = new MerchantIntelligence(product);
                return product;
            }},
            new ManFactory("Guard"){ Prop make(int seed){
                Man product = getMan(seed,getStat(9,40,5,40,3),Faction.Neutral,"-5,20,800/Slash,Thrust&Dash&Sprint",
                        new String[]{"Broadsword"},
                        new String[]{"Leather Armor","Fabric Clothing"});
                product.armor.set((Armor) getByName("Lace Helm",-1),0);
                product.speechType = "soldier";
                product.brain = new IntelligenceSocial(product);
                return product;
            }},
            new ManFactory("Punk"){ Prop make(int seed){
                Man product = getMan(seed,getStat(5,40,0,40,2),Faction.Punk,"-5,80,200,1200/Slash,Thrust&Dash,Walk&Walk&Sprint",
                        new String[]{"Stick","Axe","Pick"},
                        new String[]{"Cotton Clothing","Fabric Clothing"});
                product.speechType = "punk";
                product.brain = new IntelligenceSocial(product);
                return product;
            }},
            new ManFactory("Phantom"){ Prop make(int seed){
                Man product = getMan(seed,getStat(12,40,10,50,6),Faction.Punk,"-5,20,120,800/Slash,Thrust&Dash,Flameroll&Fireblast,Stun&Shadowstep,Meteor",
                        new String[]{"Stick","Axe","Pick","Broadsword"},
                        new String[]{"Shadowrobe"});
                product.clip.organs[5].setImage("Phantom");
                product.brain = new IntelligenceSocial(product);
                return product;
            }},
            new UnitFactory("Dog") { Prop make(int seed) {
                Unit product = new Unit(getUnit(name,seed,getStat(3,20,30,10,3),Faction.Evil,"5,140,1200/Kick&Thrust,Walk&Sprint")){
                    public void stateSet(Moveset state){
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
                };
                process(product);
                return product;
            }},
            new UnitFactory("Slime") { Prop make(int seed) {
                Unit product = new Unit(getUnit(name,seed,getStat(1,10,12,10,1),Faction.Evil,"-5,800/Kick&Walk")){
                    public void stateSet(Moveset state) {
                        super.stateSet(state);
                        if (state == Actions.Kick) {
                            moveTriggered(0);
                            delay.time = 24;
                        } else if (state == Actions.Jump){
                            vel.y -= 6;
                        }
                    }
                    public void stateUpdate(){
                        super.stateUpdate();
                        if (state == Actions.Jump && Math.abs(vel.y) < 32){
                            Particle.explode("Poison",pos,new P(),8,16,0,false,4);
                            explode(156);
                        }
                    }
                    public void stateDone(){
                        super.stateDone();
                        get("Stamina").heal();
                    }
                };
                process(product);
                return product;
            }},
            new UnitFactory("Mantis") { Prop make(int seed) {
                Unit product = new Unit(getUnit(name,seed,getStat(10,100,40,10,3),Faction.Evil,"-5/Kick")){
                    public void stateSet(Moveset state){
                        super.stateSet(state);
                        if (state == Actions.Thrust){
                            delay.time = 30;
                            vel.x += getSpeed()*3*dir;
                            vel.y -= 20;
                        }
                    }
                };
                product.clip.organs[1].pt.y -= product.clip.organs[0].ptd.y;
                process(product);
                return product;
            }},
            new UnitFactory("Beholder") { Prop make(int seed) {
                Unit product = new Unit(getUnit(name,seed,getStat(30,10,1,60,3),Faction.Evil,"300/Fireblast,Idle,Idle,Idle")){
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
                if (seed % 2 == 1) product.loadMoveset("200,800/Flamestride,Flameroll&Flameroll,Fireball");
                product.gravity = false;
                process(product);
                return product;
            }},
            //Uniques
            new Factory("Stomach Full Helm"){ Prop make(int seed){
                return new Armor("Full Helm",seed,0,10,1,0,0){
                    public String getDescription(){
                        return super.getDescription()+" Something is odd about this helmet.";
                    }
                };
            }},
    };
    public static Prop get(String[] p){
        Prop t;
        int seed = Integer.parseInt(p[1]);
        if (seed == -1) seed = Global.getSeed();
        if (p[0].charAt(0) == '!'){
            t = getInstant(p[0].substring(1), seed);
        } else {
            int index = Integer.parseInt(p[0]);
            t = factory[index].make(seed);
            t.factoryId = index;
            if (t instanceof Item){
                Item item = (Item)t;
                if (p.length > 3) item.uses = new Stat(p[3]);
                if (p.length > 4) item.loadMeta(p[4]);
                if (p.length > 5) item.rotation = Float.parseFloat(p[5]);
                if (p.length > 6){
                    if (t instanceof Food) ((Food) item).perish = Integer.parseInt(p[6]);
                }
            } else if (t instanceof Unit) {
                if (p.length > 3) {
                    int id = Integer.parseInt(p[3]);
                    if (id >= Global.objectId) Global.objectId = id + 1;
                }
            }
        }
        if (p.length > 2) t.pos.load(p[2]);
        return t;
    }
    private static Prop getInstant(String p, int seed){
        Prop t = new Prop(p, seed, 5);
        Factory.randomSide(t);
        return t;
    }
    public static Prop getByName(String p, int seed){
        for (int i=0;i<factory.length;i++){
            if (factory[i].name.equals(p)){
                Prop r = get(new String[]{""+i,""+seed,"0,0"});
                r.onCreate();
                r.factoryId = i;
                return r;
            }
        }
        return getInstant(p, seed);
    }
    static String getFactoryName(int id){
        return id==-1?"":factory[id].name;
    }
}
abstract class Factory{
    protected String name;
    Factory(String name){
        this.name = name;
    }
    abstract Prop make(int seed);
    void isNatural(Prop product){
        product.gravity = false;
        product.dynamic = true;
        product.randomSize(new P(.3f,.3f));
        randomSide(product);
    }
    void isFurniture(Prop product){
        product.gravity = false;
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
}
abstract class UnitFactory extends Factory{
    UnitFactory(String name) {
        super(name);
    }
    String getStat(int stamina, int speed, int proficiency, int health, int level){
        return "Stamina:"+stamina+"&Speed:"+speed+"&Proficiency:"+proficiency+"&Health:"+health+"&Level:"+level;
    }
    String getUnit(String name, int seed, String stat, Faction faction, String moves){
        return name+"@"+seed+"@0,0@"+Global.getObjectId()+"@"+stat+"@"+faction.name()+"@"+moves;
    }
    Unit process(Unit p){
        p.brain = new IntelligenceSocial(p);
        p.clip = new Body(p, p.name);
        return p;
    }
}
abstract class ManFactory extends UnitFactory{
    private static String[] manHeads = new String[]{"Man0","Man1","Man2","Man3","Man4","Man5","Man6"};
    ManFactory(String name) {
        super(name);
    }
    private Armor getArmor(String p, int piece){
        String pieceName = "";
        if (piece == 1) pieceName = "Legging";
        else if (piece == 2) pieceName = "Torso";
        Armor r = (Armor) Stock.getByName(p+" "+pieceName,-1);
        r.cancelAdd();
        return r;
    }
    Man getMan(int seed, String stat, Faction faction, String moves, String[] weapon, String[] armor){
        String r = getUnit("Man",seed,stat,faction,moves);
        r += "##None&None&None";
        String arm = armor[Global.r.nextInt(armor.length)];
        Man m = new Man(r);
        m.clip.organs[5].setImage(manHeads[m.rand.nextInt(manHeads.length)]);
        if (weapon != null) m.setWeapon((Item) Stock.getByName(weapon[Global.r.nextInt(weapon.length)],-1));
        m.armor.set(getArmor(arm,1),1);
        m.armor.set(getArmor(arm,2),2);
        return m;
    }
}