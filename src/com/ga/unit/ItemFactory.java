package com.ga.unit;

import com.ga.map.Global;

import java.awt.*;

/**
 * Created by Ga on 8/7/2016.
 */
public class ItemFactory {
    enum FactoryWeapon{
        Fist(3,0), Mace(30,6), Sword(15,3), Stick1(7,1), Stick2(7,1), Stick3(9,2), Katana(14,2), Spear(15,3), Axe(12,3);
        private int damage, weight;
        FactoryWeapon(int dmg, int weight){
            this.damage = dmg;
            this.weight = weight;
        }
    }
    enum FactoryArmor{
        Cloth(1, 1,  0,255,0,125),
        Leather(2, 5f,  90,200,40,120,20,80,140,160,80,100,65,75),
        Chainmail(3, 8f,  50,130,40,120),
        Platemail(4, 15,  130,200,130,200,130,200);
        private int weight;
        private float armor;
        private int color1r = 0, color1rd = 255;
        private int color1g = 0, color1gd = 255;
        private int color1b = 0, color1bd = 255;
        private int color2r = 0, color2rd = 255;
        private int color2g = 0, color2gd = 255;
        private int color2b = 0, color2bd = 255;
        boolean uniform = true;
        FactoryArmor(int weight, float armor, int r1, int r1d, int g1, int g1d, int b1, int b1d){
            this.armor = armor;
            this.weight = weight;
            color1r = r1;
            color1rd = r1d;
            color1g = g1;
            color1gd = g1d;
            color1b = b1;
            color1bd = b1d;
        }
        FactoryArmor(int weight, float armor, int r1, int r1d, int g1, int g1d, int b1, int b1d, int r2, int r2d, int g2, int g2d, int b2, int b2d){
            this(weight, armor, r1,r1d,g1,g1d,b1,b1d);
            uniform = false;
            color2r = r2;
            color2rd = r2d;
            color2g = g2;
            color2gd = g2d;
            color2b = b2;
            color2bd = b2d;
        }
        FactoryArmor(int weight, float armor, int c1, int c1d, int c2, int c2d){
            this(weight, armor,c1,c1d,c1,c1d,c1,c1d,c2,c2d,c2,c2d,c2,c2d);
        }
        Color getColor1(){
            float ratio = Global.r.nextFloat();
            int r = (int) ((color1rd - color1r)*ratio)+color1r;
            int g = (int) ((color1gd - color1g)*ratio)+color1g;
            int b = (int) ((color1bd - color1b)*ratio)+color1b;
            Color c1 = new Color(r,g,b);
            return c1;
        }
        Color getColor2(){
            float ratio = Global.r.nextFloat();
            int r = (int) ((color2rd - color2r)*ratio)+color2r;
            int g = (int) ((color2gd - color2g)*ratio)+color2g;
            int b = (int) ((color2bd - color2b)*ratio)+color2b;
            Color c2 = new Color(r,g,b);
            return c2;
        }
    }
    public static Weapon produceWeapon(String p) {
        FactoryWeapon wp;
        try {
            wp = FactoryWeapon.valueOf(p);
        } catch (Exception e) {
            Global.p("Can't find weapon "+p);
            wp = FactoryWeapon.Fist;
        }
        Weapon r = new Weapon(wp.toString());
        r.damage = wp.damage;
        r.weight = wp.weight;
        return r;
    }
    public static Armor produceArmor(String p) {
        FactoryArmor ar;
        try {
            ar = FactoryArmor.valueOf(p);
        } catch (Exception e) {
            Global.p("Can't find armor "+p);
            ar = FactoryArmor.Cloth;
        }
        Armor r = new Armor(ar.toString());
        r.armor = ar.armor;
        r.color1 = ar.getColor1();
        if (ar.uniform) r.color2 = r.color1;
        else r.color2 = ar.getColor2();
        r.weight = ar.weight;
        return r;
    }
    public static Item produceItem(String p) {
        Item item;
        if (p.equals("Potion")){
            item = new Item(p){
                @Override
                public boolean use(Man host){
                    if (super.use(host)) {
                        host.get("health").add(100);
                        host.clip.setState(Actions.CONSUME);
                        return true;
                    }
                    return false;
                }
            };
            item.delay = 70;
            item.cooldown = 1000;
            item.uses = 3;
        } else if (p.equals("Fireball")){
            item = new Item(p){
                @Override
                public boolean use(Man host){
                    if (super.use(host)) {
                        new Projectile("item/Fireball",host.pos.getAdd(host.dir*60,-20),Global.player.mP.getAdd(host.pos),5).faction = 1;
                        return true;
                    }
                    return false;
                }
            };
            item.delay = 10;
            item.action = Actions.CAST;
            item.cooldown = 1000;
            item.uses = 1;
        } else {
            item = new Item("None");
        }
        return item;
    }
}
