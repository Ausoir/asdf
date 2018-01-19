package com.ga.unit;

import java.util.ArrayList;

/**
 * Created by Ga on 4/13/2017. asd
 */
public class Traits {
    public enum Trait {
        Fast_Learner(1), Haggler(1), Tough(1), Lander(1),

        Lung_Injury,Damaged_Liver,Stomach_Injury,Hearing_Loss,Throat_Injury,Blindness,Brain_Damage,Depression,
        Armless,Left_Arm_Injury,Right_Arm_Injury,Legless,Left_Leg_Injury,Right_Leg_Injury,

        Herbalist(3), Enchanter(3), Alchemist(3);

        private int type;
        Trait(){
            this.type = 2;
        }
        Trait(int type){
            this.type = type;
        }
        public static Trait getByName(String p){
            for (Trait i : values()){
                if (i.name().equals(p)) return i;
            }
            return null;
        }
    }
    private ArrayList<Trait> traits = new ArrayList<>();

    public boolean has(Trait p){
        return traits.contains(p);
    }
    void load(String p){
        if (!p.equals("")){
            for (String i : p.split(",")){
                Trait trait = Trait.values()[Integer.parseInt(i)];
                this.traits.add(trait);
            }
        }
    }
    public String toString(){
        if (traits.size() == 0) return "";
        String r = "";
        for (Trait i : traits){
            r += i.ordinal()+",";
        }
        return r.substring(0,r.length()-1);
    }
}