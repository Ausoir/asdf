package com.ga.unit.item;

import com.ga.game.FileManager;
import com.ga.unit.Prop;
import com.ga.unit.Stock;
import com.ga.unit.Tag;
import com.ga.unit.Unit;
import com.ga.unit.man.Man;
import com.ga.unit.skill.Buffs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Ga on 3/26/2017. asd
 */
public enum Crafts {
    Patch_Wound(new String[]{"Time:0", "Rock:0", "Tree:1", "Weed:*"}){
        public void craft(Man host, int time){
            host.say("Wound patch applied.");
            host.stats.triggerEffect(Buffs.Bleed);
        }
    }, Turret(new String[]{"Time:2", "Rock:*", "Rock:*", "Rock:*", "Bush:*", "Pot:*"}){
        public void craft(Man host, int time){
            host.say("Turret built.");
            Stock.createByName(host.map, "Resident", -1).start().pos.set(host.pos.getAdd(host.target));
        }
    };

    private static HashMap<String,ArrayList<String>> recipes = FileManager.readDialogue("Data/raw/Recipe.txt");
    private String[] ingreds;
    Crafts(String[] ingredients){
        this.ingreds = ingredients;
    }
    public static String[] getRecipe(String p){
        Crafts craft = getByName(p);
        if (craft != null) return craft.ingreds;
        ArrayList<String> r = recipes.get(p);
        if (r != null) return r.toArray(new String[]{});
        return null;
    }
    public static void craftByName(Man host, String p) {
        int time = canCraft(host, p);
        if (time >= 0){
            Crafts craft = getByName(p);
            if (craft != null) craft.craft(host, time);
            else craftRaw(host, p, time);
        }
    }
    private static void craftRaw(Man host, String p, int time) {
        Prop result = Stock.createByName(host.map, p, -1);
        if (result != null && host.map.updater.sleep(time, true)) {
            result.start();
            if (host.wieldingNone()) host.setWeapon((Item) result);
            else result.pos.set(host.pos);
        }
    }
    public static int canCraft(Man host, String p) {
        String[] recipes = getRecipe(p);
        if (recipes != null){
            ArrayList<String> ingredients = new ArrayList<>();
            ArrayList<String> usage = new ArrayList<>();
            for (String i : recipes){
                String[] v = i.split(":");
                ingredients.add(v[0]);
                usage.add(v[1]);
            }
            ArrayList<Prop> availables = host.getAvailableItems();
            availables.addAll(host.getAvailableProps());
            for (Prop i : availables){
                int index = ingredients.indexOf(i.name);
                if (index == -1) {
                    for (Tag tag : i.tags){
                        index = ingredients.indexOf(tag.name());
                        if (index != -1) break;
                    }
                } else {
                    ingredients.remove(index);
                    String use = usage.remove(index);
                    if (use.equals("*")) i.remove();
                    else if (!use.equals("0") && i instanceof Item) ((Item) i).isUsable(true, Integer.parseInt(use));
                }
            }
            if (ingredients.size() == 1){
                int time = Integer.parseInt(usage.get(0));
                if (host.get("Nutrition").get() >= time) return time;
            }
        }
        return -1;
    }
    public void craft(Man host, int time) {
    }

    public static Crafts getByName(String p) {
        for (Crafts i : values()) {
            if (i.name().equals(p)){
                return i;
            }
        }
        return null;
    }
    public static void load(Man host, String p){
        if (!p.equals("")) {
            host.crafting.addAll(Arrays.asList(p.split(",")));
        }
    }

    public String toString(){
        return name();
    }
}
