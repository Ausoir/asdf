package com.ga.unit.item;

import com.ga.map.Shader;
import com.ga.tool.Performer;
import com.ga.unit.*;

/**
 * Created by Ga on 1/15/2017. asd
 */
public class ArmorSet {
    private static Armor[] bare = new Armor[]{
            new Armor(null, "Head", 0,0,0,0,new Item.Material[]{Item.Material.Flesh}){
                public int onHit(int d){ return d; }
            },
            new Armor(null, "Underwear", 0,1,0,0,new Item.Material[]{Item.Material.Flesh}){
                public int onHit(int d){ return d; }
            },
            new Armor(null, "Underwear", 0,1,0,0,new Item.Material[]{Item.Material.Flesh}){
                public int onHit(int d){ return d; }
            }
    };
    public Armor[] armor;
    private Organ[] organs;
    public Performer onChange;
    public Tags tags = new Tags();
    private Man host;
    public ArmorSet(Man host, String p){
        this.host = host;
        organs = host.clip.organs;
        armor = new Armor[3];
        load(p);
        for (Armor i : armor) i.owner = host;
    }
    public void set(Armor p, int piece){
        if (p == null || p == bare[piece]){
            armor[piece] = bare[piece];
            if (piece == 1) {
                organs[BodyHumanoid.LLEG].setImage("Underwear", 1);
                organs[BodyHumanoid.RLEG].setImage("Underwear", 1);
            } else if (piece == 2){
                organs[BodyHumanoid.FIST].setImage("Underwear", 2);
                organs[BodyHumanoid.OFIST].setImage("Underwear", 2);
                organs[BodyHumanoid.BODY].setImage("Underwear", 0, Shader.None);
            }
        } else {
            armor[piece] = p;
            if (piece == 1) {
                organs[BodyHumanoid.LLEG].setImage(p.name, 1);
                organs[BodyHumanoid.RLEG].setImage(p.name, 1);
            } else if (piece == 2){
                organs[BodyHumanoid.FIST].setImage(p.name, 2);
                organs[BodyHumanoid.OFIST].setImage(p.name, 2);
                organs[BodyHumanoid.BODY].setImage(p.name, 0);
            }
        }
        tags.clear();
        for (Armor i : armor){
            tags.addAll(i.tags);
        }
        if (onChange != null) onChange.perform();
    }
    public Armor getOrganArmor(int organ){
        if (organ == 5) return armor[0];
        if (organ == 1 || organ == 2) return armor[1];
        if (organ == 0 || organ == 3 || organ == 4) return armor[2];
        return null;
    }
    public boolean isBare(int piece){
        return armor[piece] == bare[piece];
    }
    public void remove(){
        for (int i=0;i<armor.length;i++){
            armor[i].remove();
            set(null, i);
        }
    }
    public void remove(Armor p){
        for (int i=0;i<armor.length;i++){
            if (armor[i] == p) set(null, i);
        }
    }
    public int getWeight(){
        int r = 0;
        for (Armor i : armor) r += i.weight;
        return r;
    }
    public int getEncumbrance(){
        int r = 0;
        for (Armor i : armor) r += i.encumbrance;
        return r;
    }
    public int getPockets(){
        int r = 0;
        for (Armor i : armor) r += i.pocket;
        return r;
    }
    public String getDescription(){
        if (isBare(0) && isBare(1) && isBare(2)) return "Nothing";
        String r = "";
        if (!isBare(0)) r += armor[0].getFullName()+", ";
        if (!isBare(1)) r += armor[1].getFullName()+", ";
        if (!isBare(2)) r += armor[2].getFullName()+", ";
        return r.substring(0,r.length()-2);
    }
    private void load(String p){
        String[] armors = p.split("&");
        for (int i=0;i<armor.length;i++){
            if (armors[i].equals("None")) armor[i] = bare[i];
            else armor[i] = (Armor) Stock.get(host.map, armors[i]);
        }
        for (int i=0;i<armor.length;i++) set(armor[i], i);
    }
    @Override
    public String toString(){
        String r = "";
        for (int i=0;i<armor.length;i++){
            if (armor[i] != bare[i]) r += armor[i].toString()+"&";
            else r += "None&";
        }
        return r.substring(0,r.length()-1);
    }
}