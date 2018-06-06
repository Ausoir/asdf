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

        Lung_Injury(2,"Rest time quadrupled."){
            public void onSet(Unit host){
                host.stats.restTimer.max *= 4;
            }
            public void onRemove(Unit host){
                host.stats.restTimer.max *= .25f;
            }
        }, Damaged_Liver(2,"Nutrition from food halved. Cannot drink potions."),
        Blindness(2,"Cannot read books or scrolls. Cannot target properly."),
        Brain_Damage(2,"Lose all traits and skills. Cannot obtain new traits or skills."){
            public void onSet(Unit host){
                for (Trait i : host.traits.trait){
                    if (i.type != 2) host.traits.forget(host, i);
                }
                host.moveset.clear();
            }
        },

        Crafter(3,"Craft various items."),
        Attunement(3,"Utilize innate mana from your body."),
        Energy(3,"Learn mana generation methods."){
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
        Pyromancy(3,"Forge mana into roaring flames."){
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
        }, Alacritomancy(3, "Convert mana into electric energy."){
            Tag tag = Tag.Electric;
            public void onSet(Unit host){
                onAdd(host);
            }
            public void onAdd(Unit host){
                host.tags.add(tag);
            }
            public void onRemove(Unit host){
                host.tags.remove(tag);
            }
        }, Infusion(3,"Create matter and soul from mana."){
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
        }, Shadoweaving(3,"Learn the secrets of weaving dark energy."){
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
        }, Channeling(3,"Learn the techniques of continual mana drawing."){
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
        }, Aeromancy(3,"Generate air pressure from sources of mana."){
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
        }, Morphing(3,"Learn to shape one's own body."){
            Tag tag = Tag.Mutation;
            public void onSet(Unit host){
                onAdd(host);
            }
            public void onAdd(Unit host){
                host.tags.add(tag);
            }
            public void onRemove(Unit host){
                host.tags.remove(tag);
            }
        }, Enchanting(3,"Learn to enchant and curse entities."){
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
        private String description = "This is a trait.";
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
    ArrayList<Trait> trait = new ArrayList<>();
    ArrayList<Integer> level = new ArrayList<>();

    public ArrayList<Trait> getTraits() {
        return trait;
    }
    boolean learn(Unit host, Trait p){
        if (has(p)) {
            int lvl = trait.indexOf(p);
            if (level.get(lvl) == MAXLEVEL) return false;
            level.set(lvl, level.get(lvl) + 1);
            p.onAdd(host);
        } else {
            trait.add(p);
            level.add(1);
            p.onSet(host);
        }
        return true;
    }
    public int getTraitLevel(Trait p){
        int index = trait.indexOf(p);
        if (index == -1) return 0;
        return level.get(index);
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