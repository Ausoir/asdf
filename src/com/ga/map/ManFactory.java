package com.ga.map;

import com.ga.tool.P;
import com.ga.tool.Word;
import com.ga.unit.Faction;
import com.ga.unit.Man;
import com.ga.unit.Updater;
import com.ga.unit.item.ItemFactory;

/**
 * Created by Ga on 8/2/2016.
 */
public class ManFactory {
    enum ManType{
        Player(null, Faction.Player, new String[]{"Fist"}, new String[]{"Cotton_Clothing"}, 0,0,0,0),
        Merchant(new String[]{"Merchant"}, Faction.Merchant, new String[]{"Fist"}, new String[]{"Cotton_Clothing"}, 10,20,0,0),
        Peasant(new String[]{"Man0","Man1","Man2","Man3","Man4","Man5","Man6"}, Faction.Neutral, new String[]{"Stick","Axe","Pick","Fist"}, new String[]{"Cotton_Clothing","Fabric_Clothing"}, 60,40,8,0),
        Punk(new String[]{"Man0","Man1","Man2","Man3","Man4","Man5","Man6"}, Faction.Punk, new String[]{"Stick","Axe","Pick","Broadsword"}, new String[]{"Cotton_Clothing","Fabric_Clothing"}, 30,40,5,0),
        Soldier(new String[]{"Lace_Helm"}, Faction.Guard, new String[]{"Broadsword","Spear"}, new String[]{"Leather_Armor", "Chainmail"}, 20,50,8,2),
        Knight(new String[]{"Legion_Helm"}, Faction.Evil, new String[]{"Broadsword","Mace","Katana"}, new String[]{"Platemail"}, 30,50,5,5),
        Phantom(null, Faction.Evil, new String[]{"Greatsword","Halberd","Katana"}, new String[]{"Shadowrobe"}, 5,70,15,15);
        String[] face, weapon, armor;
        int health,speed,stamina,proficiency;
        Faction faction;
        ManType(String[] face, Faction faction, String[] weapons, String[] armors, int hp, int spd, int arm, int prof){
            this.face = face;
            this.faction = faction;
            this.weapon = weapons;
            this.armor = armors;
            this.health = hp;
            this.speed = spd;
            this.stamina = arm;
            this.proficiency = prof;
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
        Man get(int seed, P p){
            String face = getFace();
            String stat = "Stamina:"+stamina+"&Speed:"+speed+"&Proficiency:"+proficiency+"&Health:"+health+"&Strength:1";
            String t = face+"@"+seed+"@"+p+"@"+ Updater.getId()+"@"+stat+"@"+faction.name();
            t += "@"+ItemFactory.produce(getWeapon(),-1).toItemString();
            t += "@"+ItemFactory.produce(getArmor(),-1).toItemString();
            Man r = new Man(t);
            return r;
        }
    }
    public static Man produce(String name, P pt, int seed){
        Man result;
        String t = Word.wordify(name);
        try {
            result = ManType.valueOf(t).get(seed, pt);
        } catch (Exception e) {
            Global.p("Can't find man ["+t+"]");
            result = ManType.Peasant.get(seed, pt);
        }
        return result;
    }
}
