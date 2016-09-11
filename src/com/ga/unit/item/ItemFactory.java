package com.ga.unit.item;

import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.tool.Word;
import com.ga.unit.*;

import java.awt.*;

/**
 * Created by Ga on 8/7/2016.
 */
public class ItemFactory {
    private enum FactoryWeapon{
        Fist(1,-1,1,2, Item.Tag.Spear), Mace(4,1000,3,8, Item.Tag.Mace), Sword(3,600,2,6,Item.Tag.Spear), Stick1(1,10,1,6, Item.Tag.Mace), Stick2(1,10,1,6,Item.Tag.Spear), Stick3(1,10,1,6, Item.Tag.Sword),
        Katana(2,100,1,12, Item.Tag.Sword), Spear(3,160,1,12,Item.Tag.Spear), Axe(3,100,1,12, Item.Tag.Sword), Dagger(1,80,1,6, Item.Tag.Dagger), Pick(4,100,1,24,Item.Tag.Sword), Cleaver(6,600,2,12, Item.Tag.Sword),
        Halberd(6,100,1,36, Item.Tag.Spear), Greatsword(8,400,2,18, Item.Tag.Sword), Warhammer(9,400,3,12,Item.Tag.Mace);
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
        Underwear(0, -1, 1,  250,255,220,240,180,220),
        Cotton_Clothing(0, 50, 1,  30,255,50,150),
        Fabric_Clothing(1, 100, 1,  10,100,10,100,120,230),
        Leather_Armor(2, 500, 3f,  90,200,40,120,20,80,140,160,80,100,65,75),
        Chainmail(3, 200, 6f,  50,130,40,120),
        Platemail(4, 200, 8,  130,200,130,200,130,200),
        Shadowrobe(1, -1, 1,  10,50,10,50,10,50);
        private int weight, uses;
        private float armor;
        private int color1r = 0, color1rd = 255;
        private int color1g = 0, color1gd = 255;
        private int color1b = 0, color1bd = 255;
        private int color2r = 0, color2rd = 255;
        private int color2g = 0, color2gd = 255;
        private int color2b = 0, color2bd = 255;
        boolean uniform = true;
        FactoryArmor(int weight, int uses, float armor, int r1, int r1d, int g1, int g1d, int b1, int b1d){
            this.uses = uses;
            this.armor = armor;
            this.weight = weight;
            color1r = r1;
            color1rd = r1d;
            color1g = g1;
            color1gd = g1d;
            color1b = b1;
            color1bd = b1d;
        }
        FactoryArmor(int weight, int uses, float armor, int r1, int r1d, int g1, int g1d, int b1, int b1d, int r2, int r2d, int g2, int g2d, int b2, int b2d){
            this(weight, uses, armor, r1,r1d,g1,g1d,b1,b1d);
            uniform = false;
            color2r = r2;
            color2rd = r2d;
            color2g = g2;
            color2gd = g2d;
            color2b = b2;
            color2bd = b2d;
        }
        FactoryArmor(int weight, int uses, float armor, int c1, int c1d, int c2, int c2d){
            this(weight, uses, armor,c1,c1d,c1,c1d,c1,c1d,c2,c2d,c2,c2d,c2,c2d);
        }
        Color getColor1(){
            float ratio = Global.r.nextFloat();
            int r = (int) ((color1rd - color1r)*ratio)+color1r;
            int g = (int) ((color1gd - color1g)*ratio)+color1g;
            int b = (int) ((color1bd - color1b)*ratio)+color1b;
            return new Color(r,g,b);
        }
        Color getColor2(){
            float ratio = Global.r.nextFloat();
            int r = (int) ((color2rd - color2r)*ratio)+color2r;
            int g = (int) ((color2gd - color2g)*ratio)+color2g;
            int b = (int) ((color2bd - color2b)*ratio)+color2b;
            return new Color(r,g,b);
        }
    }
    public static Item produce(String p, int seed){
        String t = Word.wordify(p);
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
        Armor r = new Armor(ar.toString(), seed, ar.weight, ar.uses, ar.armor);
        r.color1 = ar.getColor1();
        if (ar.uniform) r.color2 = r.color1;
        else r.color2 = ar.getColor2();
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
                item.setType(Item.Tag.Scroll);
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
                            final Projectile p = new Projectile("item/Fireball", host.pos.getAdd(host.clip.organs[ClipMan.FIST].ptd), Global.player.mP.getAdd(host.pos), 20+(host.get("Stamina").get()*2)){
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
            case "Throwing_Knife":
                item = new Item("Dagger", seed, 1, 10, 20, .04f){
                    @Override
                    public boolean use(Man host){
                        if (super.use(host)) {
                            Projectile p = new Projectile("item/Dagger",host.pos.getAdd(host.clip.organs[ClipMan.OFIST].ptd),Global.player.mP.getAdd(host.pos),50){
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
                item.name = "Throwing_Knife";
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
