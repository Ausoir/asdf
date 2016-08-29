package com.ga.map;

import com.ga.tool.P;
import com.ga.tool.Word;
import com.ga.unit.intelligence.EnemyIntelligence;
import com.ga.unit.item.Armor;
import com.ga.unit.item.ItemFactory;
import com.ga.unit.Man;
import com.ga.unit.Merchant;
import com.ga.unit.item.Weapon;

import java.util.HashMap;

/**
 * Created by Ga on 8/2/2016.
 */
public class ManFactory {
    enum ManType{
        Merchant(null, new String[]{"Fist"}, new String[]{"Cotton_Clothing"}, 0,0,0,0),
        Peasant(new String[]{"Man0","Man1","Man2","Man3","Man4","Man5","Man6"}, new String[]{"Stick1","Stick2","Stick3","Axe","Fist"}, new String[]{"Cotton_Clothing","Fabric_Clothing"}, 0,0,0,0),
        Soldier(null,new String[]{"Sword","Spear"}, new String[]{"Leather_Armor", "Chainmail"}, 20,2,8,5),
        Knight(null,new String[]{"Sword","Mace","Katana"}, new String[]{"Platemail"}, 20,1,5,5);
        String[] face, weapon, armor;
        int health,speed,arms,legs;
        ManType(String[] face, String[] weapons, String[] armors, int hp, int spd, int arm, int leg){
            this.face = face;
            this.weapon = weapons;
            this.armor = armors;
            this.health = hp;
            this.speed = spd;
            this.arms = arm;
            this.legs = leg;
        }
        String getFace(){
            if (face == null) return name();
            return face[Global.r.nextInt(face.length)];
        }
        String getWeapon(){
            return weapon[Global.r.nextInt(weapon.length)];
        }
        String getArmor(){
            return armor[Global.r.nextInt(armor.length)];
        }
        Man get(P p, int seed){
            Man r = new Man(getFace(),p,seed);
            r.setWeapon((Weapon) ItemFactory.produce(getWeapon(),-1));
            r.setArmor((Armor) ItemFactory.produce(getArmor(),-1));
            r.get("health").addTotal(health);
            r.get("speed").addTotal(speed);
            r.get("legs").addTotal(legs);
            r.get("arms").addTotal(arms);
            return r;
        }
    }
    public static Man produce(String name, P pt, int seed){
        Man result;
        String t = Word.wordify(name);
        try {
            result = ManType.valueOf(t).get(pt, seed);
        } catch (Exception e) {
            Global.p("Can't find man ["+t+"]");
            result = ManType.Peasant.get(pt, seed);
        }
        switch (t){
            case "Merchant":
                result.brain = new Merchant(result);
                break;
            default:
                result.brain = new EnemyIntelligence(result);
        }
        return result;
    }
}
