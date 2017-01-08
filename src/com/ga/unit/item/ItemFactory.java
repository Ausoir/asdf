package com.ga.unit.item;

import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.*;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Skills;
import com.ga.unit.skill.Stat;

/**
 * Created by Ga on 8/7/2016.
 */
public class ItemFactory {
    private enum FactoryWeapon{
        Mace(4,-1,3,8, Item.Tag.Mace), Sword(3,600,2,6,Item.Tag.Sword), Stick1(1,10,1,6, Item.Tag.Mace), Stick2(1,10,1,6,Item.Tag.Spear), Stick3(1,10,1,6, Item.Tag.Sword),
        Katana(2,100,1,12, Item.Tag.Sword), Spear(3,160,1,12,Item.Tag.Spear), Axe(3,100,1,12, Item.Tag.Sword), Dagger(1,80,1,6, Item.Tag.Dagger), Pick(4,100,1,24,Item.Tag.Sword), Cleaver(6,600,2,12, Item.Tag.Sword),
        Halberd(6,100,1,36, Item.Tag.Spear), Greatsword(8,400,2,18, Item.Tag.Sword), Warhammer(10,-1,3,10,Item.Tag.Mace), Claw(1,200,2,3,Item.Tag.Claw),
        Rifle(4,200,1,4, Item.Tag.Snipe), Pistol(2,30,1,2, Item.Tag.Shoot), Revolver(3,7,1,2, Item.Tag.Shoot);
        private int damageNum, damageSides, weight, uses;
        private Item.Tag type;
        FactoryWeapon(int weight, int uses, int dmgNum, int dmgSide, Item.Tag type){
            this.weight = weight;
            this.damageNum = dmgNum;
            this.damageSides = dmgSide;
            this.uses = uses;
            this.type = type;
        }
    }
    private enum FactoryArmor{
        Underwear(0, -1, 1, .3f),
        Cotton_Clothing(0, 50, 1, .1f),
        Fabric_Clothing(1, 100, 1, .1f),
        Suit(1, 50, 2f, -.1f),
        Leather_Armor(2, 500, 3f, -.1f),
        Steel_Armor(2, 200, 4f, -.2f),
        Chainmail(3, 200, 6f, -.2f),
        Platemail(4, 200, 8, -.3f),
        Shadowrobe(1, -1, 1, .2f);
        private int weight, uses;
        private float armor, comfort;
        FactoryArmor(int weight, int uses, float armor, float comfort){
            this.uses = uses;
            this.armor = armor;
            this.weight = weight;
            this.comfort = comfort;
        }
    }
    public static Item load(String p){
        if (p.equals("")) return null;
        String[] section = p.split(",");
        Item result = ItemFactory.produce(section[0],Integer.parseInt(section[1]));
        result.uses = new Stat(section[2]+","+section[3]);
        result.admod = Item.AdverbMod.load(section[4]);
        return result;
    }
    public static Item produce(String p, int seed){
        String t = p;//p.substring(0,1).toUpperCase()+p.substring(1).toLowerCase();
        for (FactoryWeapon w : FactoryWeapon.values()){
            if (w.name().equals(t)) return produceWeapon(w, seed);
        }
        for (FactoryArmor w : FactoryArmor.values()){
            if (w.name().equals(t)) return produceArmor(w, seed);
        }
        return produceItem(t, seed);
    }
    public static Item produce(String p, int seed, P pos){
        Item t = produce(p, seed);
        t.pos = pos.getAdd(0,-1);
        t.field();
        t.rotation = (float) Global.r.nextInt(628)/100;
        return t;
    }
    private static Weapon produceWeapon(FactoryWeapon wp, int seed) {
        Weapon r = new Weapon(wp.toString(), seed, wp.weight, wp.uses, wp.damageNum, wp.damageSides, wp.type);
        return r;
    }
    private static Armor produceArmor(FactoryArmor ar, int seed) {
        Armor r = new Armor(ar.toString(), seed, ar.weight, ar.uses, ar.armor, ar.comfort);
        return r;
    }
    private static Item produceItem(String p, int seed) {
        Item item;
        switch (p){
            case "Fist":
                item = new Item(p, seed, 0, 1, 30, 1){
                    @Override
                    public boolean use(final Man host){
                        host.setState(Actions.Interact);
                        return false;
                    }
                };
                break;
            case "Potion":
                item = new Item(p, seed, 1, 1, 30, 1){
                    @Override
                    public boolean use(final Man host){
                        host.setState(Actions.Consume);
                        host.delay.performer = new Performer(){
                            public boolean perform(){
                                if (canUse()) {
                                    Global.cooldown(lastused, cooldown);
                                    if (uses.get() != -1) uses.add(-1);
                                    emod.getEvent().start(host, ademod.level);
                                    emod.getEvent().perform();
                                }
                                return true;
                            }
                        };
                        host.delay.set(15);
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
            case "Scroll":
                item = new Item(p, seed, 1, 1, 100, 1){
                    @Override
                    public boolean use(Man host){
                        if (super.use(host)) {
                            return true;
                        }
                        return false;
                    }
                };
                item.setType(Item.Tag.Scroll);
                item.delay = 45;
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
                                @Override
                                public boolean perform(){
                                    p.stuck();
                                    return true;
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
                                public boolean perform(){
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
                                        public boolean perform(){
                                            p.stuck();
                                            return true;
                                        }
                                    };
                                    return true;
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
                Global.p("Can't find item "+p);
                item = null;
                break;
        }
        return item;
    }
}
