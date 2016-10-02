package com.ga.unit.item;

import com.ga.map.Global;
import com.ga.tool.Performer;
import com.ga.tool.Word;
import com.ga.unit.*;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Skills;
import com.ga.unit.skill.Stat;

/**
 * Created by Ga on 8/7/2016.
 */
public class ItemFactory {
    private enum FactoryWeapon{
        Fist(1,-1,1,2, Item.Tag.Spear), Mace(4,1000,3,8, Item.Tag.Mace), Sword(3,600,2,6,Item.Tag.Spear), Stick1(1,10,1,6, Item.Tag.Mace), Stick2(1,10,1,6,Item.Tag.Spear), Stick3(1,10,1,6, Item.Tag.Sword),
        Katana(2,100,1,12, Item.Tag.Sword), Spear(3,160,1,12,Item.Tag.Spear), Axe(3,100,1,12, Item.Tag.Sword), Dagger(1,80,1,6, Item.Tag.Dagger), Pick(4,100,1,24,Item.Tag.Sword), Cleaver(6,600,2,12, Item.Tag.Sword),
        Halberd(6,100,1,36, Item.Tag.Spear), Greatsword(8,400,2,18, Item.Tag.Sword), Warhammer(9,400,3,12,Item.Tag.Mace), Claw(1,200,2,3,Item.Tag.Claw);
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
        Leather_Armor(2, 500, 3f, -.1f),
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
        Item result = ItemFactory.produce(p.split(",")[0],Integer.parseInt(p.split(",")[1]));
        result.uses = new Stat(p.split(",")[2]+","+p.split(",")[3]);
        return result;
    }
    public static Item produce(String p, int seed){
        String t = p;
        for (FactoryWeapon w : FactoryWeapon.values()){
            if (w.name().equals(t)) return produceWeapon(t, seed);
        }
        for (FactoryArmor w : FactoryArmor.values()){
            if (w.name().equals(t)) return produceArmor(t, seed);
        }
        return produceItem(t, seed);
    }
    private static Weapon produceWeapon(String p, int seed) {
        FactoryWeapon wp;
        try {
            wp = FactoryWeapon.valueOf(p);
        } catch (Exception e) {
            Global.p("Can't find weapon "+p);
            wp = FactoryWeapon.Fist;
        }
        Weapon r = new Weapon(wp.toString(), seed, wp.weight, wp.uses, wp.damageNum, wp.damageSides, wp.type);
        return r;
    }
    private static Armor produceArmor(String p, int seed) {
        FactoryArmor ar;
        try {
            ar = FactoryArmor.valueOf(p);
        } catch (Exception e) {
            Global.p("Can't find armor "+p);
            ar = FactoryArmor.Underwear;
        }
        Armor r = new Armor(ar.toString(), seed, ar.weight, ar.uses, ar.armor, ar.comfort);
        return r;
    }
    private static Item produceItem(String p, int seed) {
        Item item;
        switch (p){
            case "Potion":
                item = new Item(p, seed, 1, 1, 1000, 1){
                    @Override
                    public boolean use(Man host){
                        if (super.use(host)) {
                            host.clip.setState(Actions.Consume);
                            return true;
                        }
                        return false;
                    }
                };
                item.delay = 45;
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
                item = new Item(p, seed, 1, 3, 10, 0) {
                    @Override
                    public boolean use(Man host) {
                        if (super.use(host)) {
                            final Projectile p = new Projectile("Fireball", host.pos.getAdd(host.clip.organs[BodyHumanoid.FIST].ptd), Global.player.mP.getAdd(host.pos), 20+(host.get("Stamina").get()*2)){
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
            case "Throwing Knife":
                item = new Item("Throwing Knife", seed, 1, 10, 20, .04f){
                    @Override
                    public boolean use(Man host){
                        if (super.use(host)) {
                            Projectile p = new Projectile("Dagger",host.pos.getAdd(host.clip.organs[BodyHumanoid.OFIST].ptd),Global.player.mP.getAdd(host.pos),50){
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
                            return true;
                        }
                        return false;
                    }
                };
                item.delay = 8;
                item.setType(Item.Tag.Shooting);
                break;
            default:
                Global.p("Can't find item "+p);
                item = null;
                break;
        }
        return item;
    }
}
