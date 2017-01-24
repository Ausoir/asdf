package com.ga.unit.item;

import com.ga.unit.Man;
import com.ga.unit.Organ;

/**
 * Created by Ga on 1/15/2017.
 */
public class ArmorSet {
    private Man host;
    protected static Armor[] bare = new Armor[]{
        new Armor("Underwear", 0, 0, -1, 0, 0, 0),
        new Armor("Underwear", 0, 0, -1, 0, 0, 1),
        new Armor("Underwear", 0, 0, -1, 0, 0, 2),
        new Armor("Underwear", 0, 0, -1, 0, 0, 3),
        new Armor("Underwear", 0, 0, -1, 0, 0, 4),
        new Armor("Head", 0, 0, -1, 0, 0, 5)
    };
    public Armor[] armor;
    private Organ[] organs;
    public ArmorSet(Man host, String p){
        this.host = host;
        organs = host.clip.organs;
        armor = new Armor[organs.length];
        load(p);
        for (int i=0;i<armor.length;i++) set(armor[i], i, false);
    }
    public void set(Armor p, int piece, boolean loop){
        if (p == null || p == bare[piece]){
            armor[piece] = bare[piece];
            if (piece == 5) organs[piece].setImage(host.name);
            else organs[piece].setImage("Underwear", organs[piece].type-1);
        } else {
            armor[piece] = p;
            if (piece == 5) organs[piece].setImage(p.name);
            else organs[piece].setImage(p.name, organs[piece].type-1);
        }
        if (loop) {
            if (piece == 1) set(p, 2, false);
            else if (piece == 2) set(p, 1, false);
        }
    }
    public boolean isBare(int piece){
        return armor[piece] == bare[piece];
    }
    public void onHit(){
        for (int i=0;i<armor.length;i++) armor[i].onHit(host);
    }
    public int getEncumberance(){
        int r = 0;
        for (int i=0;i<armor.length;i++) r += armor[i].encumberance;
        return r;
    }
    public int getSpeed(){
        int r = 0;
        for (int i=0;i<armor.length;i++) r += armor[i].speed;
        return r;
    }
    public int getWeight(){
        int r = 0;
        for (int i=0;i<armor.length;i++) r += armor[i].weight;
        return r;
    }
    public float getArmor(int p){
        return armor[p].getArmor();
    }
    public float getTotalArmor(){
        float r = 0;
        for (int i=0;i<armor.length;i++) r += armor[i].getArmor();
        return r;
    }
    private void load(String p){
        String[] armors = p.split("&");
        for (int i=0;i<armor.length;i++){
            if (armors[i].equals("None")) armor[i] = bare[i];
            else armor[i] = (Armor) ItemFactory.load(armors[i]);
        }
    }
    public String toItemString(){
        String r = "";
        for (int i=0;i<armor.length;i++){
            if (armor[i] != bare[i]) r += armor[i].toItemString()+"&";
            else r += "None&";
        }
        return r.substring(0,r.length()-1);
    }
}