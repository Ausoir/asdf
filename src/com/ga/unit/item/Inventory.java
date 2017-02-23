package com.ga.unit.item;

import com.ga.unit.Player;
import com.ga.unit.Updater;
import com.ga.unit.factory.Stock;

import java.util.ArrayList;

/**
 * Created by Ga on 8/9/2016.
 */
public class Inventory {
    private ArrayList<Item> items = new ArrayList<>();
    public int weight = 0;
    private int maxSize = 0;
    private Player host;
    public Inventory(Player host){
        this.host = host;
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
    public void setMaxSize(int p){
        while (items.size() > p){
            Item t = items.remove(0);
            t.drop(host);
        }
        maxSize = p;
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
            Item item = (Item) Stock.get(data[i].split("@"));
            weight += item.weight;
            Updater.cancelAdd(item);
            items.add(item);
        }
    }
    public void onMapChange(){
        for (int i=0;i<items.size();i++){
            Item t = items.get(i);
            if (t instanceof Food) ((Food) t).onMapChange();
        }
    }
    public String toString(){
        String r = ";";
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            r += item.toString()+";";
        }
        return r;
    }
}