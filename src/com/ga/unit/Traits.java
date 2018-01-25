package com.ga.unit;

import com.ga.tool.ToolString;

import java.util.ArrayList;

/**
 * Created by Ga on 4/13/2017. asd
 */
public class Traits {
    public enum Trait {
        Fast_Learner(2), Haggler(1), Tough(1), Lander(1), Innovation(1), Perception(1),

        Lung_Injury,Damaged_Liver,Stomach_Injury,Hearing_Loss,Throat_Injury,Blindness,Brain_Damage,Depression,
        Armless,Left_Arm_Injury,Right_Arm_Injury,Legless,Left_Leg_Injury,Right_Leg_Injury,

        Herbalism(null), Rune_Literacy(null), Alchemy(null), Stealth(null),
        Magic(null), Casting(new Trait[]{Magic}), Attunement(new Trait[]{Magic}), Generation(new Trait[]{Attunement}),
        Soulbuffer(new Trait[]{Attunement}), Blood_Magic(null), Mastery(null),

        Fire_Brand(new Trait[]{Rune_Literacy}), Lightning_Brand(new Trait[]{Rune_Literacy}),
        Pyromancy(new Trait[]{Casting}), Infusion(new Trait[]{Casting}), Wizardry(new Trait[]{Attunement, Casting}),
        Morphing(new Trait[]{Magic}), Soulbinding(new Trait[]{Magic}), Enchanting(new Trait[]{Blood_Magic}),
        Infusion_Mastery(new Trait[]{Infusion});

        private int type, value;
        private Trait[] requirements;
        Trait(){
            this.type = 2;
        }
        Trait(int value){
            this.type = 1;
            this.value = value;
        }
        Trait(Trait[] requirements){
            this.type = 3;
            this.requirements = requirements;
        }
        public static Trait getByName(String p){
            for (Trait i : values()){
                if (i.name().equals(p)) return i;
            }
            return null;
        }
    }
    ArrayList<Trait> traits = new ArrayList<>();

    boolean isLearnable(Trait p){
        if (has(p)) return false;
        if (p.requirements != null) {
            for (Trait i : p.requirements) {
                if (!traits.contains(i)) return false;
            }
        }
        return true;
    }
    public boolean has(Trait p){
        return traits.contains(p);
    }
    void load(Unit host, String p){
        if (!p.equals("")){
            for (String i : p.split(",")){
                Trait trait = Trait.valueOf(i);
                host.learnTrait(trait);
            }
        }
    }
    public String toString(){
        return ToolString.tosArrayList(traits);
    }
}