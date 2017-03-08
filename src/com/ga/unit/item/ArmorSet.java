package com.ga.unit.item;

import com.ga.tool.Performer;
import com.ga.unit.BodyHumanoid;
import com.ga.unit.Man;
import com.ga.unit.Organ;
import com.ga.unit.factory.Stock;

/**
 * Created by Ga on 1/15/2017.
 */
public class ArmorSet {
    private Man host;
    protected static Armor[] bare = new Armor[]{
            (Armor) new Armor("Head", 0, 0, -1, 0, 0, 0).cancelAdd(),
            (Armor) new Armor("Underwear", 0, 0, -1, 0, 0, 1).cancelAdd(),
            (Armor) new Armor("Underwear", 0, 0, -1, 0, 0, 2).cancelAdd()
    };
    public Armor[] armor;
    private Organ[] organs;
    public Performer onChange;
    public ArmorSet(Man host, String p){
        this.host = host;
        organs = host.clip.organs;
        armor = new Armor[3];
        load(p);
    }
    public void set(Armor p, int piece){
        if (p == null || p == bare[piece]){
            armor[piece] = bare[piece];
            if (piece == 0) organs[5].setImage(host.name);
            else if (piece == 1) {
                organs[BodyHumanoid.LLEG].setImage("Underwear", 1);
                organs[BodyHumanoid.RLEG].setImage("Underwear", 1);
            } else if (piece == 2){
                organs[BodyHumanoid.FIST].setImage("Underwear", 2);
                organs[BodyHumanoid.OFIST].setImage("Underwear", 2);
                organs[BodyHumanoid.BODY].setImage("Underwear", 0);
            }
        } else {
            armor[piece] = p;
            if (piece == 0) organs[5].setImage(p.name);
            else if (piece == 1) {
                organs[BodyHumanoid.LLEG].setImage(p.name, 1);
                organs[BodyHumanoid.RLEG].setImage(p.name, 1);
            } else if (piece == 2){
                organs[BodyHumanoid.FIST].setImage(p.name, 2);
                organs[BodyHumanoid.OFIST].setImage(p.name, 2);
                organs[BodyHumanoid.BODY].setImage(p.name, 0);
            }
        }
        if (onChange != null) onChange.perform();
    }
    private int organToPiece(int organ){
        if (organ == 5) return 0;
        if (organ == 1 || organ == 2) return 1;
        if (organ == 0 || organ == 3 || organ == 4) return 2;
        return -1;
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
        return armor[organToPiece(p)].getArmor();
    }
    public int getPockets(){
        int r = 0;
        for (int i=0;i<armor.length;i++) r += armor[i].pocket;
        return r;
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
            else armor[i] = (Armor) ((Item) Stock.get(armors[i].split("@"))).cancelAdd();
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