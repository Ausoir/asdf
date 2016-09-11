package com.ga.map;

import com.ga.tool.P;
import com.ga.tool.Word;
import com.ga.unit.Faction;
import com.ga.unit.intelligence.EnemyIntelligence;
import com.ga.unit.item.Armor;
import com.ga.unit.item.ItemFactory;
import com.ga.unit.Man;
import com.ga.unit.Merchant;
import com.ga.unit.item.Weapon;

/**
 * Created by Ga on 8/2/2016.
 */
public class ManFactory {
    enum ManType{
        Merchant(null, Faction.Neutral, "merchant", new String[]{"Fist"}, new String[]{"Cotton_Clothing"}, 0,0,0,0),
        Peasant(new String[]{"Man0","Man1","Man2","Man3","Man4","Man5","Man6"}, Faction.Neutral, "peasant", new String[]{"Stick1","Stick2","Stick3","Axe","Pick","Fist"}, new String[]{"Cotton_Clothing","Fabric_Clothing"}, 0,0,0,11110),
        Soldier(null, Faction.Guard, "soldier",new String[]{"Sword","Spear"}, new String[]{"Leather_Armor", "Chainmail"}, 20,2,8,5),
        Knight(null, Faction.Guard, "soldier",new String[]{"Sword","Mace","Katana"}, new String[]{"Platemail"}, 20,1,5,5),
        Phantom(null, Faction.Hostile, "",new String[]{"Greatsword","Halberd","Katana"}, new String[]{"Shadowrobe"}, 0,2,15,15),
        Hero(null, Faction.Player, "soldier",new String[]{"Greatsword","Halberd","Cleaver"}, new String[]{"Platemail"}, 50,2,15,15);
        String[] face, weapon, armor;
        String speech;
        int health,speed,stamina,energy;
        Faction faction;
        ManType(String[] face, Faction faction, String speech, String[] weapons, String[] armors, int hp, int spd, int arm, int leg){
            this.face = face;
            this.faction = faction;
            this.speech = speech;
            this.weapon = weapons;
            this.armor = armors;
            this.health = hp;
            this.speed = spd;
            this.stamina = arm;
            this.energy = leg;
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
            r.faction = faction;
            r.speechType = speech;
            r.setWeapon((Weapon) ItemFactory.produce(getWeapon(),-1));
            r.setArmor((Armor) ItemFactory.produce(getArmor(),-1));
            r.get("Health").addTotal(health);
            r.get("Speed").addTotal(speed);
            r.get("Energy").addTotal(energy);
            r.get("Stamina").addTotal(stamina);
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
    public static Man arm(Man p, String weapon, String armor){
        switch (p.name){
            case "Merchant":
                p.brain = new Merchant(p);
                break;
            default:
                p.brain = new EnemyIntelligence(p);
        }
        p.setWeapon((Weapon) ItemFactory.produce(weapon,-1));
        p.setArmor((Armor) ItemFactory.produce(armor,-1));
        return p;
    }
}
