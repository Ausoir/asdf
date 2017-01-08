package com.ga.unit.item;

import com.ga.unit.Player;

import java.util.ArrayList;

/**
 * Created by Ga on 8/9/2016.
 */
public class Inventory {
    private ArrayList<Item> items = new ArrayList<>();
    public int weight = 0;
    private Player player;
    public int maxSize = 5;
    public int wielded = -1;
    public Inventory(Player player){
        this.player = player;
    }
    public boolean addItem(Item p){
        if (items.size() >= maxSize) return false;
        items.add(p);
        weight += p.weight;
        return true;
    }
    public boolean sheathe(Item p){
        boolean r = addItem(p);
        wielded = -1;
        return r;
    }
    public boolean vacant(){
        return items.size() < maxSize;
    }
    public Item getItem(int p){
        return items.get(p);
    }
    public int getItemSize(){
        return items.size();
    }
    public Item remove(int p){
        Item r = items.get(p);
        weight -= r.weight;
        items.remove(p);
        return r;
    }
    public Item remove(Item item){
        int i = items.indexOf(item);
        if (i == -1) return null;
        return remove(i);
    }
    public void load(String p){
        if (p.equals(";")) return;
        String[] wield = p.substring(1).split("]");
        wielded = Integer.parseInt(wield[0]);
        String[] data = wield[1].split(";");
        for (int i=0;i<data.length;i++){
            Item item = ItemFactory.load(data[i]);
            weight += item.weight;
            items.add(item);
        }
    }
    public String toString(){
        String r = ";"+wielded+"]";
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            r += item.toItemString()+";";
        }
        return r;
    }
}