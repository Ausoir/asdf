package com.ga.unit.item;

import com.ga.game.FileManager;
import com.ga.map.Updater;
import com.ga.unit.Tag;
import com.ga.unit.player.Player;
import com.ga.unit.Prop;
import com.ga.unit.Stock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by Ga on 3/26/2017. asd
 */
public class Crafts {
    public static final HashMap<String,ArrayList<String>> recipes = FileManager.readDialogue("raw/Recipe.txt");
    public ArrayList<String> knowledge = new ArrayList<>();
    private Player host;
    public Crafts(Player player) {
        host = player;
    }

    public void craft(String p) {
        ArrayList<String> ingredients = new ArrayList<>();
        ArrayList<String> usage = new ArrayList<>();
        for (String i : recipes.get(p)){
            String[] v = i.split(":");
            ingredients.add(v[0]);
            usage.add(v[1]);
        }
        for (Prop i : host.getAvailableItems()){
            int index = ingredients.indexOf(i.name);
            if (index == -1) {
                for (Tag tag : i.tags){
                    index = ingredients.indexOf(tag.name());
                    if (index != -1) break;
                }
            }
            if (index != -1){
                ingredients.remove(index);
                String use = usage.remove(index);
                if (use.equals("*")) i.remove();
                else if (!use.equals("0") && i instanceof Item) ((Item) i).usable(Integer.parseInt(use));
            }
        }
        if (ingredients.size() == 1){
            int time = Integer.parseInt(usage.get(0));
            if (Updater.sleep(time) == time) {
                Prop result = Stock.getByName(p, -1);
                if (host.wieldingNone()) host.setWeapon((Item) result);
                else result.pos.set(host.pos);
            }
        }
    }

    public void load(String p) {
        if (p.equals(";")) return;
        String[] data = p.substring(1).split(";");
        Collections.addAll(knowledge, data);
    }

    public String toString(){
        String r = ";";
        for (String aKnowledge : knowledge) {
            r += aKnowledge + ";";
        }
        return r;
    }
}
