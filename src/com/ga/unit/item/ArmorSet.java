package com.ga.unit.item;

import com.ga.map.Global;
import com.ga.map.Shader;
import com.ga.tool.Performer;
import com.ga.unit.*;

/**
 * Created by Ga on 1/15/2017. asd
 */
public class ArmorSet {
    private static Armor[] bare = new Armor[]{
            (Armor) new Armor("Head", 0, 0, 0, 0, 0){
                void onHit(){}
            }.cancelAdd(),
            (Armor) new Armor("Underwear", 0, 0, 0, 0, 1){
                void onHit(){}
            }.cancelAdd(),
            (Armor) new Armor("Underwear", 0, 0, 0, 0, 2){
                void onHit(){}
            }.cancelAdd()
    };
    public Armor[] armor;
    private Organ[] organs;
    public Performer onChange;
    public Tags tags = new Tags();
    public ArmorSet(Man host, String p){
        organs = host.clip.organs;
        armor = new Armor[3];
        load(p);
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
        if (getEncumberance() <= 4) tags.add(Tag.Agility);
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
        for (Armor i : armor) i.onHit();
    }
    public void remove(Armor p){
        for (int i=0;i<armor.length;i++){
            if (armor[i] == p) set(null, i);
        }
    }
    public int getEncumberance(){
        int r = 0;
        for (Armor i : armor) r += i.encumberance;
        return r;
    }
    public int getSpeed(){
        int r = 0;
        for (Armor i : armor) r += i.speed;
        return r;
    }
    public int getWeight(){
        int r = 0;
        for (Armor i : armor) r += i.weight;
        return r;
    }
    public float getArmor(int p){
        return armor[organToPiece(p)].getArmor();
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
        if (!isBare(1)) r += armor[1].getFullName()+" Legging, ";
        if (!isBare(2)) r += armor[2].getFullName()+" Torso, ";
        return r.substring(0,r.length()-2);
    }
    private void load(String p){
        String[] armors = p.split("&");
        for (int i=0;i<armor.length;i++){
            if (armors[i].equals("None")) armor[i] = bare[i];
            else armor[i] = (Armor) Stock.get(armors[i]);
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