package com.ga.map;

import com.ga.tool.P;
import com.ga.unit.Armor;
import com.ga.unit.ItemFactory;
import com.ga.unit.Man;
import com.ga.unit.Weapon;

import java.awt.*;
import java.util.HashMap;

/**
 * Created by Ga on 8/2/2016.
 */
public class ManFactory {
    static final HashMap<String, Integer> manType = new HashMap<String,Integer>(){{
        put("Player",0);
        put("Man",1);
        put("Soldier",2);
        put("Knight",3);
    }};
    static final String[][] weaponType = new String[][]{
            new String[]{ "Fist" },
            new String[]{ "Stick1", "Stick2", "Stick3", "Axe" },
            new String[]{ "Sword", "Spear" },
            new String[]{ "Sword", "Mace", "Katana" },
    };
    static final String[][] armorType = new String[][]{
            new String[]{ "Cloth" },
            new String[]{ "Cloth" },
            new String[]{ "Leather", "Chainmail" },
            new String[]{ "Platemail" },
    };
    static final String[] speechType = new String[]{
            "", "peasant", "soldier", "soldier"
    };
    public static Man produce(String name, P pt){
        Man p = new Man(name, pt);
        if (name.startsWith("Man")) name = "Man";
        Integer type = manType.get(name);
        if (type == -1) type = Global.r.nextInt(4);
        String weapon = weaponType[type][Global.r.nextInt(weaponType[type].length)];
        String armor = armorType[type][Global.r.nextInt(armorType[type].length)];
        p.setWeapon(ItemFactory.produceWeapon(weapon));
        p.setArmor(ItemFactory.produceArmor(armor));
        if (type == 1) {
            p.get("health").addTotal(10);
            p.get("speed").addTotal(1);
            p.get("legs").set(Global.r.nextInt(2)+1);
            p.get("arms").set(Global.r.nextInt(2)+1);
        } else if (type == 2) {
            p.get("health").addTotal(10);
            p.get("speed").addTotal(1);
            p.get("arms").addTotal(Global.r.nextInt(10)+5);
            p.get("legs").addTotal(Global.r.nextInt(4)+5);
        } else if (type == 3) {
            p.get("health").addTotal(50);
            p.get("arms").addTotal(Global.r.nextInt(5)+20);
            p.get("legs").addTotal(Global.r.nextInt(5)+20);
        }
        p.speechType = speechType[type];
        return p;
    }
}
