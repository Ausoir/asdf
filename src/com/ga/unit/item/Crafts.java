package com.ga.unit.item;

import com.ga.game.FileManager;
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
    public static final HashMap<String,ArrayList<String>> recipes = FileManager.readDialogue("dialog/Recipe.txt");
    public ArrayList<String> knowledge = new ArrayList<>();
    private Player host;
    public Crafts(Player player) {
        host = player;
    }

    public void craft(String p) {
        ArrayList<String> ingredients = (ArrayList<String>) recipes.get(p).clone();
        for (Prop i : host.getAvailableItems()){
            if (i instanceof Item && ingredients.remove(i.name)){
                i.remove();
            }
        }
        if (ingredients.size() == 0){
            if (host.wieldingNone()) host.setWeapon((Item) Stock.getByName(p,-1));
            else Stock.getByName(p,-1).pos.set(host.pos);
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
