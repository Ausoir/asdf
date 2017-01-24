package com.ga.map;

import com.ga.tool.P;
import com.ga.tool.Word;
import com.ga.unit.Faction;
import com.ga.unit.Man;
import com.ga.unit.Updater;
import com.ga.unit.item.Armor;
import com.ga.unit.item.ItemFactory;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Moveset;
import com.ga.unit.skill.Skills;

import java.util.HashMap;

/**
 * Created by Ga on 8/2/2016.
 */
public class ManFactory {
    private static String[] manHeads = new String[]{"Man0","Man1","Man2","Man3","Man4","Man5","Man6"};
    enum ManType{
        Merchant(new String[]{"Merchant"}, Faction.Merchant, new String[]{"Head"},
                new String[]{"Fist"}, new String[]{"Cotton_Clothing"}, 1,10,20,0,0,
                "0/Idle"),
        Peasant(manHeads, Faction.Neutral, new String[]{"Head"},
                new String[]{"Stick","Axe","Pick","Fist"}, new String[]{"Cotton_Clothing","Fabric_Clothing"}, 1,60,40,8,0,
                "-5/Swing"),
        Punk(manHeads, Faction.Punk, new String[]{"Head"},
                new String[]{"Stick","Axe","Pick","Broadsword"}, new String[]{"Cotton_Clothing","Fabric_Clothing"}, 3,60,40,5,0,
                "-5,50/Swing,Pierce&Dash"),
        Soldier(manHeads, Faction.Guard, new String[]{"Lace_Helm"},
                new String[]{"Broadsword","Spear"}, new String[]{"Leather_Armor", "Chainmail"}, 5,120,50,8,2,
                "-5,50/Swing,Pierce&Dash"),
        Knight(manHeads, Faction.Evil, new String[]{"Full_Helm","Legion_Helm"},
                new String[]{"Broadsword","Mace","Katana"}, new String[]{"Platemail"}, 9,300,50,5,5,
                "0/Swing,Pierce,Slash,Whirlwind"),
        Phantom(new String[]{"Phantom"}, Faction.Evil, new String[]{"Head"},
                new String[]{"Greatsword","Halberd","Katana"}, new String[]{"Shadowrobe"}, 4,5,70,15,15,
                "-5/Swing");
        String[] face, weapon, armor, helmet;
        int level,health,speed,stamina,proficiency;
        String rangeMove;
        Faction faction;
        ManType(String[] face, Faction faction, String[] helmet, String[] weapons, String[] armors, int lvl, int hp, int spd, int arm, int prof, String rangeMove){
            this.face = face;
            this.faction = faction;
            this.weapon = weapons;
            this.armor = armors;
            this.helmet = helmet;
            this.level = lvl;
            this.health = hp;
            this.speed = spd;
            this.stamina = arm;
            this.proficiency = prof;
            this.rangeMove = rangeMove;
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
        String getHelmet(){
            return helmet[Global.r.nextInt(helmet.length)];
        }
        Man get(int seed, P p){
            String face = getFace();
            String stat = "Stamina:"+stamina+"&Speed:"+speed+"&Proficiency:"+proficiency+"&Health:"+health+"&Level:1";
            String t = face+"@"+seed+"@"+p+"@"+ Updater.getId()+"@"+stat+"@"+faction.name()+"@"+rangeMove;
            t += "@"+ItemFactory.produce(getWeapon(),-1,0).toItemString()+"@";
            Armor armor = ItemFactory.FactoryArmor.produce(getArmor(),-1,0);
            for (int i=0;i<5;i++){
                armor.piece = i;
                t += armor.toItemString()+"&";
            }
            t += ItemFactory.FactoryArmor.produce(getHelmet(),-1,5).toItemString();
            Man r = new Man(t);
            return r;
        }
        private static Man getByName(String name, int seed, P p){
            for (ManType i : values()){
                if (i.name().equals(name)) return i.get(seed, p);
            }
            return null;
        }
    }
    public static Man produce(String name, P pt, int seed){
        Man result;
        result = ManType.getByName(name, seed, pt);
        return result;
    }
}
