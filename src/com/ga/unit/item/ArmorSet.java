package com.ga.unit.item;

import com.ga.map.Global;
import com.ga.map.Picture;
import com.ga.map.Shader;
import com.ga.tool.T;
import com.ga.unit.Organ;
import com.ga.unit.Tags;
import com.ga.unit.man.Man;

import java.awt.*;

/**
 * Created by Ga on 1/15/2017. asd
 */
public class ArmorSet {
    private static Armor[] bare = new Armor[]{
            new Armor(null, "Head","Head", 0,Item.Material.Organic,0,0,0,0,0){
                public boolean checkBreak(boolean blunt, int d){ return true; }
            },
            new Armor(null, "Man_skin","Man_skin", 0,Item.Material.Organic,1,0,0,0,0){
                public boolean checkBreak(boolean blunt, int d){ return true; }
            },
            new Armor(null, "Man_skin","Man_skin", 0,Item.Material.Organic,2,0,0,0,0){
                public boolean checkBreak(boolean blunt, int d){ return true; }
            },
            new Armor(null, "Man_skin","Man_skin", 0,Item.Material.Organic,3,0,0,0,0){
                public boolean checkBreak(boolean blunt, int d){ return true; }
            },
            new Armor(null, "Man_skin","Man_skin", 0,Item.Material.Organic,4,0,0,0,0){
                public boolean checkBreak(boolean blunt, int d){ return true; }
            },
    };
    public Armor[] armor;
    public Tags tags = new Tags();
    private Man host;
    public Organ[] display;
    public ArmorSet(Man host, Item head, Item body, Item legs, Item back, Item etc){
        this.host = host;
        Organ[] organs = host.clip.organs;
        display = new Organ[organs.length];
        for (int i = 0; i< organs.length; i++){
            if (i == 5){
                display[i] = new Organ(null, true){
                    public void draw(Graphics2D g) {
                        float x = host.pos.x+pt.x, y = host.pos.y+pt.y;
                        if (image.visible && Global.inCamera(x,y,0,0))
                            image.draw(g, x, y, getAtan()+1.57f, host.dir);
                    }
                };
            } else display[i] = new Organ(null, true);
            display[i].host = host;
            display[i].pt = organs[i].pt;
            display[i].ptd = organs[i].ptd;
            display[i].setSize(organs[i].getSize());
            display[i].type = organs[i].type;
            display[i].setImage(null);
            display[i].image.visible = false;
        }
        armor = new Armor[5];
        Item[] armors = new Item[]{head,body,legs,back,etc};
        for (int i=0;i<armor.length;i++){
            if (armors[i] == null) armor[i] = bare[i];
            else armor[i] = (Armor) armors[i];
            armor[i].owner = host;
        }
        for (int i=0;i<armor.length;i++) set(armor[i], i);
    }
    public void set(Armor p, int piece){
        if (p == null) p = bare[piece];
        armor[piece] = p;
        if (p == bare[piece]) {
            if (piece == 0) display[5].setImage(null);
            else if (piece == 1) {
                display[0].setImage(null);
                display[3].setImage(null);
                display[4].setImage(null);
            } else if (piece == 2){
                display[1].setImage(null);
                display[2].setImage(null);
            }
        } else {
            if (piece == 0){
                display[5].setImage(p.image.image);
                display[5].image.modify(Shader.Tatter, Math.round(armor[piece].getPercentage() * 10));
            } else if (piece == 1) {
                display[0].setImage(Picture.load("/"+p.imageName+"/32/64/1/0", null));
                display[3].setImage(p.image.image);
                display[4].setImage(Picture.load("/"+p.imageName+"/32/64/1/0", null));
                display[0].image.modify(Shader.Tatter, Math.round(armor[piece].getPercentage() * 10));
                display[3].image.modify(Shader.Tatter, Math.round(armor[piece].getPercentage() * 10));
                display[4].image.modify(Shader.Tatter, Math.round(armor[piece].getPercentage() * 10));
            } else if (piece == 2){
                display[1].setImage(p.image.image);
                display[2].setImage(p.image.image);
                display[1].image.modify(Shader.Tatter, Math.round(armor[piece].getPercentage() * 10));
                display[2].image.modify(Shader.Tatter, Math.round(armor[piece].getPercentage() * 10));
            }
        }
        updateStatus();
    }
    private void updateStatus(){
        tags.clear();
        for (Armor i : armor){
            tags.addAll(i.tags);
        }
        host.inventory.setMaxSize(getPockets());
    }
    public void refresh(){
        set(armor[0], 0);
        set(armor[1], 1);
        set(armor[2], 2);
    }
    public int getOrganArmor(int organ){
        if (organ == 0 || organ == 3 || organ == 4) return 1;
        else if (organ == 1 || organ == 2) return 2;
        assert organ == 5;
        return 0;
    }
    public boolean isBare(){
        return isBare(0) && isBare(1) && isBare(2);
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
    public void drop(){
        for (int i=0;i<4;i++) {
            if (!isBare(i)) {
                armor[i].onUnequip(host);
                armor[i].start();
                set(null, i);
            }
        }
    }
    public void setVisible(boolean visible){
        for (Organ i : display) i.image.visible = visible;
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
    public int getWarmth(){
        int r = 0;
        for (Armor i : armor) r += i.getWarmth();
        return r;
    }
    public String getDescription(){
        if (isBare(0) && isBare(1) && isBare(2)) return "Nothing";
        StringBuilder r = new StringBuilder();
        for (int i=0;i<armor.length;i++) {
            if (!isBare(i)) r.append(armor[i].getFullName()).append(", ");
        }
        return r.substring(0,r.length()-2);
    }
    @Override
    public String toString(){
        StringBuilder r = new StringBuilder();
        for (int i=0;i<armor.length;i++){
            if (armor[i] != bare[i]) r.append(T.wrap(armor[i].toString())).append(",");
            else r.append(",");
        }
        return r.substring(0,r.length()-1);
    }
}