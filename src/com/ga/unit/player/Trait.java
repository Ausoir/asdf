package com.ga.unit.player;

import com.ga.unit.Organ;

/**
 * Created by Ga on 4/13/2017. asd
 */
public enum Trait {
    Fragile(1), Bulky(1), Fast_Learner(1), Stout(1){
        void onSet(Player host){
            for (Organ i : host.clip.organs) i.health += 5;
        }
    }, Healthy(1), Unhealthy(1), Optimistic(1), Pessimistic(1), Sickly(1), Sharp(1), Dull(1),
    Abrasive(1), Lucky(1), Unlucky(1), Greedy(1), Abusive(1), Fast_Metabolism(1),

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
    void onSet(Player host){

    }
    public static Trait getByName(String p){
        for (Trait i : values()){
            if (i.name().equals(p)) return i;
        }
        return null;
    }
}