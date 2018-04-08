package com.ga.unit;

import java.util.ArrayList;

/**
 * Created by Ga on 4/13/2017. asd
 */
public class Traits {
    public enum Trait {
        Fast_Learner(1,"Level twice as fast at the cost of triple motivation cost."), Haggler(1,"Get 30% off shop prices."),
        Tough(1,"Resist flinching from being hit."), Innovation(1,"Learn random skill every level."), Perception(1,"Learn to identify others."),
        Herbalism(1,"Pick materials from bushes and trees."), Rune_Literacy(1,"Identify, use, and create scrolls."),
        Alchemy(1,"Identify, use, and create potions."),
        Fire_Brand(1,"Brand yourself to obtain innate Fire catalyst."), Lightning_Brand(1,"Brand yourself to obtain innate Lightning catalyst."),

        Lung_Injury(2,"Rest time tripled."), Damaged_Liver(2,"Nutrition from food halved. Cannot drink potions."),
        Hearing_Loss(2,"Cannot talk."), Blindness(2,"Cannot read books or scrolls. Targeting range is gone."),
        Brain_Damage(2,"Lose access to random traits and skills. Some are permanently lost."),

        Stealth(3,"Learn to sneak for few seconds."),
        Attunement(3,"Utilize innate mana from your body."), Generation(3,"Learn mana generation methods."),

        Crafter(3,"Craft various items."), Armorsmith(3,"Craft, repair, and modify armor."), Weaponsmith(3,"Craft, repair, and modify weapons."),

        Pyromancy(3,"Learn the secrets of fire magic."), Infusion(3,"Create and infuse items."), Morphing(3,"Learn to reshape yourself."),
        Soulbinding(3,"Learn the secrets of controlling others."), Enchanting(3,"Learn to enchant allies and curse enemies.");

        public int type;
        private String description = "This is a trait.";
        Trait(int type, String desc){
            this.type = type;
            this.description = desc;
        }
        public String getDescription(){
            return description;
        }
        public static ArrayList<Trait> getByType(int p){
            ArrayList<Trait> traits = new ArrayList<>();
            for (Trait i : values()){
                if (i.type == p) traits.add(i);
            }
            return traits;
        }
        public static Trait getByName(String p){
            for (Trait i : values()){
                if (i.name().equals(p)) return i;
            }
            return null;
        }
    }
    public static int MAXLEVEL = 4;
    ArrayList<Trait> trait = new ArrayList<>();
    ArrayList<Integer> level = new ArrayList<>();

    public ArrayList<Trait> getTraits() {
        return trait;
    }
    boolean learn(Trait p){
        if (has(p)) {
            int lvl = trait.indexOf(p);
            if (level.get(lvl) == MAXLEVEL) return false;
            level.set(lvl, level.get(lvl) + 1);
        } else {
            trait.add(p);
            level.add(1);
        }
        return true;
    }
    public int getTraitLevel(Trait p){
        int index = trait.indexOf(p);
        if (index == -1) return 0;
        return level.get(index);
    }
    public boolean forget(Trait p){
        if (has(p)){
            int index = trait.indexOf(p);
            level.remove(index);
            trait.remove(index);
            return true;
        }
        return false;
    }
    public boolean has(Trait p){
        return trait.contains(p);
    }
    int getMemorySize(){
        int r = 0;
        for (int i=0;i<trait.size();i++){
            if (trait.get(i).type == 3) r += level.get(i);
        }
        return r;
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