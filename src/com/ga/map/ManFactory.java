package com.ga.map;

import com.ga.tool.P;
import com.ga.tool.Word;
import com.ga.unit.*;
import com.ga.unit.item.Armor;
import com.ga.unit.item.ItemFactory;

/**
 * Created by Ga on 8/2/2016.
 */
public class ManFactory {
    enum ManType{
        Player(null, Faction.Player, new String[]{"Fist"}, new String[]{"Cotton_Clothing"}, 0,0,0,0,0),
        Merchant(new String[]{"Merchant"}, Faction.Merchant, new String[]{"Fist"}, new String[]{"Cotton_Clothing"}, 10,60,0,0,0),
        Peasant(new String[]{"Man0","Man1","Man2","Man3","Man4","Man5","Man6"}, Faction.Neutral, new String[]{"Stick1","Stick2","Stick3","Axe","Pick","Fist"}, new String[]{"Cotton_Clothing","Fabric_Clothing"}, 60,60,20,5,0),
        Punk(new String[]{"Man0","Man1","Man2","Man3","Man4","Man5","Man6"}, Faction.Punk, new String[]{"Stick1","Stick2","Stick3","Axe","Pick","Fist"}, new String[]{"Cotton_Clothing","Fabric_Clothing"}, 30,60,999,999,0),
        Soldier(null, Faction.Guard, new String[]{"Sword","Spear"}, new String[]{"Leather_Armor", "Chainmail"}, 20,50,8,5,2),
        Knight(null, Faction.Evil, new String[]{"Sword","Mace","Katana"}, new String[]{"Platemail"}, 30,50,5,5,5),
        Phantom(null, Faction.Evil, new String[]{"Greatsword","Halberd","Katana"}, new String[]{"Shadowrobe"}, 5,70,15,15,15);
        String[] face, weapon, armor;
        int health,speed,stamina,energy,proficiency;
        Faction faction;
        ManType(String[] face, Faction faction, String[] weapons, String[] armors, int hp, int spd, int arm, int leg, int prof){
            this.face = face;
            this.faction = faction;
            this.weapon = weapons;
            this.armor = armors;
            this.health = hp;
            this.speed = spd;
            this.stamina = arm;
            this.energy = leg;
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
            String stat = "Stamina:"+stamina+"&Speed:"+speed+"&Energy:"+energy+"&Proficiency:"+proficiency+"&Health:"+health;
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
