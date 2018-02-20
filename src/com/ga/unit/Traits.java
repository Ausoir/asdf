package com.ga.unit;

import java.util.ArrayList;

/**
 * Created by Ga on 4/13/2017. asd
 */
public class Traits {
    public enum Trait {
        Fast_Learner(2), Haggler(1), Tough(1), Innovation(1), Perception(1),

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
    ArrayList<Trait> trait = new ArrayList<>();
    ArrayList<Integer> level = new ArrayList<>();

    private boolean isLearnable(Trait p){
        if (p.requirements != null) {
            for (Trait i : p.requirements) {
                if (!trait.contains(i)) return false;
            }
        }
        return true;
    }
    boolean learn(Trait p){
        if (isLearnable(p)) {
            if (has(p)) {
                int lvl = trait.indexOf(p);
                level.set(lvl, level.get(lvl) + 1);
            } else {
                trait.add(p);
                level.add(1);
            }
            return true;
        }
        return false;
    }
    public boolean has(Trait p){
        return trait.contains(p);
    }
    void load(Unit host, String p){
        if (!p.equals("")){
            for (String i : p.split(",")){
                Trait trait = Trait.valueOf(i.split(":")[0]);
                for (int lvl=0;lvl<Integer.parseInt(i.split(":")[1]);lvl++) host.learnTrait(trait);
            }
        }
    }
    public String toString(){
        if (trait.size() == 0) return "";
        String r = "";
        for (int i=0;i<trait.size();i++){
            r += trait.get(i).toString()+":"+level.get(i)+",";
        }
        return r.substring(0,r.length()-1);
    }
}