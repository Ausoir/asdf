package com.ga.unit.item;

import com.ga.unit.Organ;
import com.ga.unit.Stock;
import com.ga.unit.Tags;
import com.ga.unit.man.Man;

import java.awt.*;

/**
 * Created by Ga on 1/15/2017. asd
 */
public class ArmorSet {
    private static Armor[] bare = new Armor[]{
            new Armor(null, "Head", 0,new Item.Material[]{Item.Material.Flesh},0,0,0, -1){
                public boolean checkBreak(int d){ return true; }
            },
            new Armor(null, "Underwear", 0,new Item.Material[]{Item.Material.Flesh},1,0,0, -1){
                public boolean checkBreak(int d){ return true; }
            },
            new Armor(null, "Underwear", 0,new Item.Material[]{Item.Material.Flesh},2,0,0, -1){
                public boolean checkBreak(int d){ return true; }
            },
            new Armor(null, "Underwear", 0,new Item.Material[]{Item.Material.Flesh},3,0,0, -1){
                public boolean checkBreak(int d){ return true; }
            },
    };
    public Armor[] armor;
    private Organ[] organs;
    public Tags tags = new Tags();
    private Man host;
    public Organ[] display;
    public ArmorSet(Man host, String head, String body, String legs, String etc){
        this.host = host;
        organs = host.clip.organs;
        display = new Organ[organs.length];
        for (int i=0;i<organs.length;i++){
            if (i == 5){
                display[i] = new Organ(null, true){
                    public void draw(Graphics2D g) {
                        image.draw(g, host.pos.x+pt.x, host.pos.y+pt.y, getAtan()+1.57f, host.dir);
                    }
                };
            } else display[i] = new Organ(null, true);
            display[i].host = host;
            display[i].pt = organs[i].pt;
            display[i].ptd = organs[i].ptd;
            display[i].setSize(organs[i].getSize());
            display[i].type = organs[i].type;
            display[i].setImage(null);
            display[i].getImage().visible = false;
        }
        armor = new Armor[4];
        String[] armors = new String[]{head,body,legs,etc};
        for (int i=0;i<armor.length;i++){
            if (armors[i].equals("")) armor[i] = bare[i];
            else armor[i] = (Armor) Stock.get(host.map, armors[i]);
            armor[i].owner = host;
        }
        for (int i=0;i<armor.length;i++) set(armor[i], i);
    }
    public void set(Armor p, int piece){
        if (p == null) p = bare[piece];
        armor[piece] = p;
        if (p == bare[piece]) {
            if (piece == 0) display[5].getImage().visible = false;
            else if (piece == 1) {
                display[1].getImage().visible = false;
                display[2].getImage().visible = false;
            } else if (piece == 2){
                display[0].getImage().visible = false;
                display[3].getImage().visible = false;
                display[4].getImage().visible = false;
            }
        } else {
            if (piece == 0) display[5].setImage(p.name);
            else if (piece == 1) {
                display[1].setImage(p.name, 1);
                display[2].setImage(p.name, 1);
            } else if (piece == 2){
                display[0].setImage(p.name, 2);
                display[3].setImage(p.name, 0);
                display[4].setImage(p.name, 2);
            }
        }
        tags.clear();
        for (Armor i : armor){
            tags.addAll(i.tags);
        }
        host.inventory.setMaxSize(getPockets());
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
            if (armor[i] != bare[i]){
                armor[i].remove();
                set(null, i);
            }
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
    @Override
    public String toString(){
        String r = "";
        for (int i=0;i<armor.length;i++){
            if (armor[i] != bare[i]) r += armor[i].toString()+";";
            else r += ";";
        }
        return r.substring(0,r.length()-1);
    }
}