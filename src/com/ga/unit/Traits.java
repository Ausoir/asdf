package com.ga.unit;

import com.ga.tool.T;

import java.util.ArrayList;

/**
 * Created by Ga on 4/13/2017. asd
 */
public class Traits {
    public enum Trait {
        Painless(0,"Does not flinch."),
        Bloodless(0,"Immune to bleeding."),
        Inorganic(0,"Immune to burning damage."),
        Restless(0,"Does not require Stamina."),
        Overcharged(0,"Does not require Energy."),

        Learner(1,"Gain 1 motivation every hour."),
        Instinct(1,"Evade decapitation using a Vitality point."),
        Tattoo(1,"Gain 1 extra memory slot."),
        Literacy(1,"Learn to read runes. You may now identify scrolls."),
        Alchemy(1,"Gain familiarity with alchemy. You may now identify potions."),

        // ----- Type 2 Unimplemented
        Lung_Injury(2,"Rest time quadrupled."),
        Damaged_Liver(2,"Nutrition from food -1. Potions do no effect."),
        Blindness(2,"Cannot target with cursor. Cannot read books or scrolls."),
        Depression(2,"Lose 1 motivation every hour."),
        Brain_Damage(2,"Lose all traits and skills."),
        // -----

        Energy(3,"Learn energy generation methods."){
            Tag tag = Tag.Energy;
            public void onSet(Unit host){
                onAdd(host);
            }
            public void onAdd(Unit host){
                host.tags.add(tag);
            }
            public void onRemove(Unit host){
                host.tags.remove(tag);
            }
        },
        Channeling(3,"Learn the techniques of persistent energy release."){
            Tag tag = Tag.Channeling;
            public void onSet(Unit host){
                onAdd(host);
            }
            public void onAdd(Unit host){
                host.tags.add(tag);
            }
            public void onRemove(Unit host){
                host.tags.remove(tag);
            }
        },
        Pyromancy(3,"Forge energy into damaging fire."){
            Tag tag = Tag.Fire;
            public void onSet(Unit host){
                onAdd(host);
            }
            public void onAdd(Unit host){
                host.tags.add(tag);
            }
            public void onRemove(Unit host){
                host.tags.remove(tag);
            }
        },
        Infusion(3,"Conjure matter from energy."){
            Tag tag = Tag.Conjuration;
            public void onSet(Unit host){
                onAdd(host);
            }
            public void onAdd(Unit host){
                host.tags.add(tag);
            }
            public void onRemove(Unit host){
                host.tags.remove(tag);
            }
        },
        Aeromancy(3,"Manipulate air with energy."){
            Tag tag = Tag.Air;
            public void onSet(Unit host){
                onAdd(host);
            }
            public void onAdd(Unit host){
                host.tags.add(tag);
            }
            public void onRemove(Unit host){
                host.tags.remove(tag);
            }
        },
        Shadoweaving(3,"Learn the secrets of weaving dark energy."){
            Tag tag = Tag.Void;
            public void onSet(Unit host){
                onAdd(host);
            }
            public void onAdd(Unit host){
                host.tags.add(tag);
            }
            public void onRemove(Unit host){
                host.tags.remove(tag);
            }
        },
        Bloodcraft(3,"Use blood to manipulate entities with enchantments and curses."){
            Tag tag = Tag.Enchant;
            Tag tag2 = Tag.Curse;
            public void onSet(Unit host){
                onAdd(host);
            }
            public void onAdd(Unit host){
                host.tags.add(tag);
                host.tags.add(tag2);
            }
            public void onRemove(Unit host){
                host.tags.remove(tag);
                host.tags.remove(tag2);
            }
        };

        public int type;
        private String description;
        Trait(int type, String desc){
            this.type = type;
            this.description = desc;
        }
        public String getDescription(){
            return description;
        }
        public void onSet(Unit host){
        }
        public void onAdd(Unit host){
        }
        public void onRemove(Unit host){
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
    private ArrayList<Trait> trait = new ArrayList<>();
    private ArrayList<Integer> level = new ArrayList<>();

    public ArrayList<Trait> getTraits() {
        return trait;
    }
    void learn(Unit host, Trait p){
        if (has(p)) {
            int lvl = trait.indexOf(p);
            level.set(lvl, level.get(lvl) + 1);
            p.onAdd(host);
        } else {
            trait.add(p);
            level.add(1);
            p.onSet(host);
        }
    }
    boolean canLearn(Trait p){
        if (has(p)) {
            if (p.type != 3) return false;
            int lvl = trait.indexOf(p);
            return level.get(lvl) != MAXLEVEL;
        }
        return true;
    }
    public int getTraitLevel(Trait p){
        int index = trait.indexOf(p);
        if (index == -1) return 0;
        return level.get(index);
    }
    void forgetAll(Unit host){
        for (Trait i : trait){
            if (i.type == 3) forget(host, i);
        }
    }
    public boolean forget(Unit host, Trait p){
        if (has(p)){
            int index = trait.indexOf(p);
            level.remove(index);
            trait.remove(index);
            p.onRemove(host);
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
    void load(Unit host, T p){
        for (T i : p){
            Trait trait = Trait.valueOf(i.get(0).get());
            for (int lvl=0;lvl<i.get(1).getInt();lvl++) host.learnTrait(trait);
        }
    }
    public String toString(){
        if (trait.size() == 0) return "";
        StringBuilder r = new StringBuilder();
        for (int i=0;i<trait.size();i++){
            r.append("[").append(trait.get(i).toString()).append(",").append(level.get(i)).append("],");
        }
        return "["+r.substring(0,r.length()-1)+"]";
    }
}