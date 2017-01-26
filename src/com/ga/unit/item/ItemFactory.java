package com.ga.unit.item;

import com.ga.tool.Performer;
import com.ga.unit.*;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Skills;
import com.ga.unit.skill.Stat;

/**
 * Created by Ga on 8/7/2016.
 */
public class ItemFactory {
    public enum FactoryWeapon{
        Mace(5,-1,2,10,-2, Item.Tag.Mace), Broadsword(4,600,2,6,0,Item.Tag.Sword), Stick(0,10,1,4,2, Item.Tag.Mace), Wooden_Sword(2,100,1,8,1, Item.Tag.Mace),
        Katana(3,100,1,12,1, Item.Tag.Sword), Spear(2,160,1,12,-4,Item.Tag.Spear), Axe(3,100,1,12,-2, Item.Tag.Sword), Dagger(1,80,1,8,3, Item.Tag.Dagger), Pick(4,100,1,12,-2,Item.Tag.Sword),
        Cleaver(4,600,2,8,0, Item.Tag.Sword), Lancesword(1,100,1,6,0,Item.Tag.Rapier), Cutlass(1,100,1,6,0,Item.Tag.Sword), Claymore(1,100,1,6,-1,Item.Tag.Sword),
        Halberd(6,100,1,36,-5, Item.Tag.Sword), Greatsword(7,400,2,12,-3, Item.Tag.Sword), Warhammer(7,-1,3,8,-3,Item.Tag.Mace), Claw(1,200,3,4,4,Item.Tag.Claw),
        Bamboo_Spear(1,24,1,6,0, Item.Tag.Spear),
        Rifle(4,200,1,6,-3, Item.Tag.Snipe), Pistol(2,30,1,2,5, Item.Tag.Shoot), Revolver(3,7,1,2,0, Item.Tag.Shoot);
        private int damageNum, damageSides, weight, uses, speed;
        private Item.Tag type;
        FactoryWeapon(int weight, int uses, int dmgNum, int dmgSide, int speed, Item.Tag type){
            this.weight = weight;
            this.damageNum = dmgNum;
            this.damageSides = dmgSide;
            this.uses = uses;
            this.speed = speed;
            this.type = type;
        }
        public static Weapon produce(String p, int seed){
            String t = p;
            Weapon r = null;
            for (FactoryWeapon w : FactoryWeapon.values()){
                if (w.name().equals(t)){
                    r = new Weapon(w.toString(), seed, w.weight, w.uses, w.damageNum, w.damageSides, w.type);
                    r.speed = w.speed;
                }
            }
            return r;
        }
    }
    public enum FactoryArmor{ //Body armor by default
        Cotton_Clothing(0, 50, 1, 1),
        Suit(0, 50, 1f, 2),
        Robe(0, 50, 1f, 2),
        Fabric_Clothing(1, 100, 1, 2),
        Adventurewear(1, 100, 2, 0),
        Leather_Armor(2, 500, 3f, 3),
        Steel_Armor(2, 200, 4f, 5),
        Chainmail(3, 200, 6f, 5),
        Platemail(4, 200, 8, 8),
        Shadowrobe(1, -1, 1, 0),
        Lace_Helm(1,-1,2,0,true),
        Full_Helm(2,1000,5,1,true),
        Legion_Helm(2,1000,8,1,true);
        private int weight, uses;
        private boolean helmet;
        private float armor;
        private int encumberance;
        FactoryArmor(int weight, int uses, float armor, int encumberance){
            this.uses = uses;
            this.armor = armor;
            this.weight = weight;
            this.encumberance = encumberance;
        }
        FactoryArmor(int weight, int uses, float armor, int encumberance, boolean helmet){
            this(weight, uses, armor, encumberance);
            this.helmet = helmet;
        }
        public static Armor produce(String p, int seed, int piece){
            String t = p;
            Armor r = null;
            if (t.equals("Underwear") || t.equals("Head")) return ArmorSet.bare[piece];
            for (FactoryArmor w : FactoryArmor.values()){
                if (w.name().equals(t)){
                    r = new Armor(w.toString(), seed, w.weight, w.uses, w.armor, w.encumberance, w.helmet?5:piece);
                    switch (w){
                        case Steel_Armor:
                        case Shadowrobe:
                            r.damage = 3;
                            break;
                        case Adventurewear:
                            if (piece == 1) r.pocket = 1;
                            else if (piece == 3) r.pocket = 1;
                            r.damage = 2;
                            break;
                    }
                }
            }
            return r;
        }
    }
    public static Item load(String p){
        if (p.equals("")) return null;
        String[] section = p.split(",");
        Item result = produce(section[0],Integer.parseInt(section[1]),section.length >= 6 ? Integer.parseInt(section[5]) : 0);
        result.uses = new Stat(section[2]+","+section[3]);
        result.admod = Item.AdverbMod.load(section[4]);
        return result;
    }
    public static Item produce(String p, int seed, int opt){
        Item r = produceItem(p, seed);
        if (r == null) r = FactoryWeapon.produce(p, seed);
        if (r == null) r = FactoryArmor.produce(p, seed, opt);
        return r;
    }
    protected static Item produceItem(String p, int seed) {
        Item item;
        switch (p){
            case "Blood": //amputated fist
                item = new Item(p, seed, 0, -1, 1, 0){
                    @Override
                    public boolean use(final Man host){
                        return false;
                    }
                };
                break;
            case "Fist":
                item = new Item(p, seed, 0, -1, 1, 0){
                    @Override
                    public boolean use(final Man host){
                        host.setState(Actions.Interact);
                        return false;
                    }
                };
                break;
            case "Spellbook":
                item = new Item(p, seed, 1, 1, 0, 1){
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
                break;
            case "Manual":
                item = new Item(p, seed, 1, 1, 0, 0){
                    public Skills skill;
                    @Override
                    public boolean use(Man host){
                        if (super.use(host)) {
                            ((Player)host).learnSkill(skill);
                            return true;
                        }
                        return false;
                    }
                    @Override
                    public String getFullName(){
                        return super.getFullName()+" of " + skill.name();
                    }
                    protected void init(){
                        skill = Skills.values()[rand.nextInt(Skills.values().length)];
                    }
                };
                item.setType(Item.Tag.Enchant);
                item.delay = 45;
                break;
            case "Potion":
                item = new Item(p, seed, 1, 1, 1, 1){
                    @Override
                    public boolean use(Man host){
                        if (super.use(host)) {
                            return true;
                        }
                        return false;
                    }
                };
                item.probability = 1;
                item.setType(Item.Tag.Scroll);
                item.delay = 45;
                break;
            case "Scroll":
                item = new Item(p, seed, 0, 1, 1, 1){
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
                break;
            case "Fireball":
                item = new Item(p, seed, 1, 3, 1, 0) {
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
                break;
            case "Tanto":
                item = new Item(p, seed, 1, 10, 0, .04f){
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
                break;
            default:
                item = null;
                break;
        }
        return item;
    }
}
