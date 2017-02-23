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

import java.awt.*;

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
                r.onInteract = new Interactive(80){
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
                r.onInteract = new Interactive(80){
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
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Mace); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Dagger"){ public Matter make(int seed){
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Dagger); //name,seed,weight,uses,dmgNum,dmgSide,type
            }},
            new Factory("Pick"){ public Matter make(int seed){
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Spear); //name,seed,weight,uses,dmgNum,dmgSide,type
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
                return new Weapon(name,seed,1,10,1,6,Item.Tag.Spear); //name,seed,weight,uses,dmgNum,dmgSide,type
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
            new Factory("Cotton Clothing Right Arm"){ public Matter make(int seed){
                return new Armor("Cotton Clothing",seed,1,10,1,1,0); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Cotton Clothing Legging"){ public Matter make(int seed){ //25
                Armor r = new Armor("Cotton Clothing",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Cotton Clothing Legging"){ public Matter make(int seed){
                Armor r = new Armor("Cotton Clothing",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Cotton Clothing Body"){ public Matter make(int seed){
                return new Armor("Cotton Clothing",seed,1,10,1,1,3); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Cotton Clothing Left Arm"){ public Matter make(int seed){
                return new Armor("Cotton Clothing",seed,1,10,1,1,4); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Fabric Clothing Right Arm"){ public Matter make(int seed){
                return new Armor("Fabric Clothing",seed,1,10,1,1,0); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Fabric Clothing Legging"){ public Matter make(int seed){ //30
                Armor r = new Armor("Fabric Clothing",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Fabric Clothing Legging"){ public Matter make(int seed){
                Armor r = new Armor("Fabric Clothing",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Fabric Clothing Body"){ public Matter make(int seed){
                return new Armor("Fabric Clothing",seed,1,10,1,1,3); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Fabric Clothing Left Arm"){ public Matter make(int seed){
                return new Armor("Fabric Clothing",seed,1,10,1,1,4); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Suit Right Arm"){ public Matter make(int seed){
                return new Armor("Suit",seed,1,10,1,1,0); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Suit Legging"){ public Matter make(int seed){ //35
                Armor r = new Armor("Suit",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Suit Legging"){ public Matter make(int seed){
                Armor r = new Armor("Suit",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Suit Body"){ public Matter make(int seed){
                return new Armor("Suit",seed,1,10,1,1,3); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Suit Left Arm"){ public Matter make(int seed){
                return new Armor("Suit",seed,1,10,1,1,4); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Adventurewear Right Arm"){ public Matter make(int seed){
                return new Armor("Adventurewear",seed,1,10,1,1,0); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Adventurewear Legging"){ public Matter make(int seed){ //40
                Armor r = new Armor("Adventurewear",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Adventurewear Legging"){ public Matter make(int seed){
                Armor r = new Armor("Adventurewear",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Adventurewear Body"){ public Matter make(int seed){
                return new Armor("Adventurewear",seed,1,10,1,1,3); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Adventurewear Left Arm"){ public Matter make(int seed){
                return new Armor("Adventurewear",seed,1,10,1,1,4); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Leather Armor Right Arm"){ public Matter make(int seed){
                return new Armor("Leather Armor",seed,1,10,1,1,0); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Leather Armor Legging"){ public Matter make(int seed){ //45
                Armor r = new Armor("Leather Armor",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Leather Armor Legging"){ public Matter make(int seed){
                Armor r = new Armor("Leather Armor",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Leather Armor Body"){ public Matter make(int seed){
                return new Armor("Leather Armor",seed,1,10,1,1,3); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Leather Armor Left Arm"){ public Matter make(int seed){
                return new Armor("Leather Armor",seed,1,10,1,1,4); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Steel Armor Right Arm"){ public Matter make(int seed){
                return new Armor("Steel Armor",seed,1,10,1,1,0); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Steel Armor Legging"){ public Matter make(int seed){ //50
                Armor r = new Armor("Steel Armor",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Steel Armor Legging"){ public Matter make(int seed){
                Armor r = new Armor("Steel Armor",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Steel Armor Body"){ public Matter make(int seed){
                return new Armor("Steel Armor",seed,1,10,1,1,3); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Steel Armor Left Arm"){ public Matter make(int seed){
                return new Armor("Steel Armor",seed,1,10,1,1,4); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Chainmail Right Arm"){ public Matter make(int seed){
                return new Armor("Chainmail",seed,1,10,1,1,0); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Chainmail Legging"){ public Matter make(int seed){ //55
                Armor r = new Armor("Chainmail",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Chainmail Legging"){ public Matter make(int seed){
                Armor r = new Armor("Chainmail",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Chainmail Body"){ public Matter make(int seed){
                return new Armor("Chainmail",seed,1,10,1,1,3); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Chainmail Left Arm"){ public Matter make(int seed){
                return new Armor("Chainmail",seed,1,10,1,1,4); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Platemail Right Arm"){ public Matter make(int seed){
                return new Armor("Platemail",seed,1,10,1,1,0); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Platemail Legging"){ public Matter make(int seed){ //60
                Armor r = new Armor("Platemail",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Platemail Legging"){ public Matter make(int seed){
                Armor r = new Armor("Platemail",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Platemail Body"){ public Matter make(int seed){
                return new Armor("Platemail",seed,1,10,1,1,3); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Platemail Left Arm"){ public Matter make(int seed){
                return new Armor("Platemail",seed,1,10,1,1,4); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Shadowrobe Right Arm"){ public Matter make(int seed){
                return new Armor("Shadowrobe",seed,1,10,1,1,0); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Shadowrobe Legging"){ public Matter make(int seed){ //65
                Armor r = new Armor("Shadowrobe",seed,1,10,1,1,1); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Shadowrobe Legging"){ public Matter make(int seed){
                Armor r = new Armor("Shadowrobe",seed,1,10,1,1,2); //name,seed,weight,uses,armor,encumberance,piece
                r.pocket = 1;
                return r;
            }},
            new Factory("Shadowrobe Body"){ public Matter make(int seed){
                return new Armor("Shadowrobe",seed,1,10,1,1,3); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Shadowrobe Left Arm"){ public Matter make(int seed){
                return new Armor("Shadowrobe",seed,1,10,1,1,4); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Lace Helm"){ public Matter make(int seed){
                return new Armor(name,seed,1,10,1,1,5); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Full Helm"){ public Matter make(int seed){ //70
                return new Armor(name,seed,1,10,1,1,5); //name,seed,weight,uses,armor,encumberance,piece
            }},
            new Factory("Legion Helm"){ public Matter make(int seed){
                return new Armor(name,seed,1,10,1,1,5); //name,seed,weight,uses,armor,encumberance,piece
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
                Item r = new Item(name, seed, 0, 10, 0, 0){
                    public void update(){}
                };
                r.onInteract = null;
                return r;
            }},
            new Factory("Root"){ public Matter make(int seed){
                Item item = new Item(null, seed, 0, 10, 0, 0){
                    public void update(){}
                };
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
                item.name = "Vine"+((item.seed % 2)+1);
                item.setImage(item.name);
                item.gravity = false;
                item.onInteract = null;
                return item;
            }},
            new Factory("Rock"){ public Matter make(int seed){
                Item r = new Item(name, seed, 4, -1, 0, 0){
                    @Override
                    public boolean use(final Man host){
                        return false;
                    }
                };
                return r;
            }},
            new Factory("Bush"){ public Matter make(int seed){
                Item item = new Item(name, seed, 6, 50, 0, 0){
                    @Override
                    public boolean use(final Man host){
                        return false;
                    }
                };
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
                item.name = "DeadBush"+((item.seed % 2)+1);
                item.setImage(item.name);
                item.onInteract = null;
                return item;
            }},
            new Factory("TreeD"){ public Matter make(int seed){
                Item item = new Item(null, seed, 20, 100, 0, 0){
                    public void update(){}
                };
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
                Item r = new Item(name, seed, 1, 1, 0, 1){
                    @Override
                    public boolean use(Man host){
                        if (super.use(host)) {
                            ((Player)host).learnSkill(emod);
                            return true;
                        }
                        return false;
                    }
                };
                r.setType(Item.Tag.Enchant);
                r.delay = 45;
                return r;
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
                                    Updater.collide(this,10000);
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
                                    final Projectile p = new Projectile("Tanto",host.pos.getAdd(host.clip.organs[BodyHumanoid.OFIST].ptd),host.pos.getAdd(host.target),50){
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
                item.delay = 11;
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
            new Factory("Ration"){ public Matter make(int seed){
                Food item = new Food(name, seed, 1, 3, 16){
                    public boolean use(Man host){
                        if (super.use(host)){
                            host.get("Hunger").add(8);
                            return true;
                        }
                        return false;
                    }
                };
                item.setType(Item.Tag.Food);
                return item;
            }},
            new Factory("Jerky"){ public Matter make(int seed){
                Food item = new Food(name, seed, 1, 4, 8){
                    public boolean use(Man host){
                        if (super.use(host)){
                            host.get("Hunger").add(2);
                            return true;
                        }
                        return false;
                    }
                };
                item.setType(Item.Tag.Food);
                return item;
            }},
            new Factory("Meal"){ public Matter make(int seed){
                Food item = new Food(name, seed, 1, 1, 2){
                    public boolean use(Man host){
                        if (super.use(host)){
                            host.get("Hunger").add(14);
                            return true;
                        }
                        return false;
                    }
                };
                item.setType(Item.Tag.Food);
                return item;
            }},
            new Factory("Meat"){ public Matter make(int seed){
                Food item = new Food(name, seed, 1, 6, 1){
                    public boolean use(Man host){
                        if (super.use(host)){
                            host.get("Hunger").add(8);
                            return true;
                        }
                        return false;
                    }
                };
                item.setType(Item.Tag.Food);
                return item;
            }},
            new ManFactory("Punk"){ public Matter make(int seed){
                String r = getMan(seed,getStat(12,60,40,10,0),Faction.Punk,"-5,50/Kick&Swing,Pierce&Dash",
                        new String[]{"Stick","Axe","Pick","Broadsword"},
                        new String[]{"Cotton Clothing","Fabric Clothing"});
                return new Man(r);
            }},
            new ManFactory("Phantom"){ public Matter make(int seed){
                String r = getMan(seed,getStat(12,60,40,10,0),Faction.Punk,"-5,50/Kick&Swing,Pierce&Dash",
                        new String[]{"Stick","Axe","Pick","Broadsword"},
                        new String[]{"Cotton Clothing","Fabric Clothing"});
                return new Man(r);
            }},
            new UnitFactory("Dog") { Matter make(int seed) {
                Unit r = new Unit(getUnit(name,seed,getStat(1,10,30,10,3),Faction.Evil,"-5,50/Kick&Pierce")){
                    public void stateSet(Moveset state){
                        super.stateSet(state);
                        if (state == Skills.Pierce){
                            delay.time = 30;
                            vel.x += getSpeed()*3*dir;
                            vel.y -= 20;
                        }
                    }
                };
                process(r);
                return r;
            }},
            new UnitFactory("Slime") { Matter make(int seed) {
                Unit r = new Unit(getUnit(name,seed,getStat(1,10,12,10,1),Faction.Evil,"5/Kick")){
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
    public Factory(String name){
        this.name = name;
    }
    abstract Matter make(int seed);
}
abstract class UnitFactory extends Factory{
    public UnitFactory(String name) {
        super(name);
    }
    protected String getStat(int stamina, int speed, int proficiency, int health, int level){
        return "Stamina:"+stamina+"&Speed:"+speed+"&Proficiency:"+proficiency+"&Health:"+health+"&Level:"+level;
    }
    protected String getUnit(String name, int seed, String stat, Faction faction, String moves){
        return name+"@"+seed+"@0,0@"+Updater.getId()+"@"+stat+"@"+faction.name()+"@"+moves;
    }
    protected Unit process(Unit p){
        p.brain = new UnitIntelligence(p);
        p.clip = new Body(p, p.name);
        return p;
    }
}
abstract class ManFactory extends UnitFactory{
    private static String[] manHeads = new String[]{"Man0","Man1","Man2","Man3","Man4","Man5","Man6"};
    public ManFactory(String name) {
        super(name);
    }
    private String getFace(){
        return manHeads[Global.r.nextInt(manHeads.length)];
    }
    private Armor getArmor(String p, int piece){
        String pieceName = "";
        if (piece == 0) pieceName = "Right Arm";
        else if (piece == 1) pieceName = "Legging";
        else if (piece == 2) pieceName = "Legging";
        else if (piece == 3) pieceName = "Body";
        else if (piece == 4) pieceName = "Left Arm";
        Armor r = (Armor) Stock.getByName(p+" "+pieceName,-1);
        return r;
    }
    protected String getMan(int seed, String stat, Faction faction, String moves, String[] weapon, String[] armor){
        String r = getUnit(getFace(),seed,stat,faction,moves);
        r += "#"+Stock.getByName(weapon[Global.r.nextInt(weapon.length)],-1);
        String arm = armor[Global.r.nextInt(armor.length)];
        r += "#"+getArmor(arm,0);
        for (int i=1;i<5;i++) r += "&"+getArmor(arm,i);
        r += "&None";
        return r;
    }
}