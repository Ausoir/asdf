package com.ga.unit.item;

import com.ga.unit.Man;
import com.ga.unit.Organ;

/**
 * Created by Ga on 1/15/2017.
 */
public class ArmorSet {
    private Man host;
    public Armor[] armor;
    private Organ[] organs;
    public ArmorSet(Man host, String p){
        this.host = host;
        organs = host.clip.organs;
        armor = new Armor[organs.length];
        load(p);
        for (int i=0;i<armor.length;i++) set(armor[i], i);
    }
    public void set(Armor p, int piece){
        if (p == null){
            if (piece == 5){
                p = ItemFactory.FactoryArmor.produce("Head",0,piece);
                p.setImage(host.name);
            } else p = ItemFactory.FactoryArmor.produce("Underwear",0,piece);
        }
        armor[piece] = p;
        if (piece == 5){
            if (!p.name.equals("Head")) organs[piece].setImage(p.name);
        } else organs[piece].setImage(p.name, organs[piece].type-1);
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
        for (int i=0;i<armor.length;i++) armor[i] = (Armor) ItemFactory.load(armors[i]);
    }
    public String toItemString(){
        String r = "";
        for (Armor i : armor) r += i.toItemString()+"&";
        return r.substring(0,r.length()-1);
    }
}