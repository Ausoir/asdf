package com.ga.unit.item;

import java.util.ArrayList;

/**
 * Created by Ga on 8/9/2016.
 */
public class Inventory {
    private ArrayList<Item> items = new ArrayList<>();
    public int weight = 0;
    public int maxSize = 2;
    public Inventory(){
    }
    public boolean addItem(Item p){
        if (items.size() >= maxSize) return false;
        items.add(p);
        weight += p.weight;
        return true;
    }
    public boolean sheathe(Item p){
        boolean r = addItem(p);
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
        String[] data = p.substring(1).split(";");
        for (int i=0;i<data.length;i++){
            Item item = ItemFactory.load(data[i]);
            weight += item.weight;
            items.add(item);
        }
    }
    public String toString(){
        String r = ";";
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            r += item.toItemString()+";";
        }
        return r;
    }
}