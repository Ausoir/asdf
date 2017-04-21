package com.ga.unit.player;

import com.ga.unit.item.Food;
import com.ga.unit.item.Item;
import com.ga.map.Updater;
import com.ga.unit.Stock;

import java.util.ArrayList;

/**
 * Created by Ga on 8/9/2016. asd
 */
public class Inventory {
    private ArrayList<Item> items = new ArrayList<>();
    public int weight = 0;
    private int volume = 0;
    private int maxSize = 0;
    private Player host;
    public Inventory(Player host){
        this.host = host;
    }
    private boolean addItem(Item p){
        if (volume+p.volume > maxSize) return false;
        items.add(p);
        weight += p.weight;
        volume += p.volume;
        return true;
    }
    boolean sheathe(Item p){
        return addItem(p);
    }
    boolean eat(int p){
        for (Item i : items){
            if (i instanceof Food){
                i.use(host);
                return true;
            }
        }
        return false;
    }
    public boolean vacant(){
        return items.size() < maxSize;
    }
    public Item getItem(int p){
        return items.get(p);
    }
    ArrayList<Item> getItems(){
        return items;
    }
    public int getItemSize(){
        return items.size();
    }
    void setMaxSize(int p){
        while (items.size() > p){
            Item t = items.remove(0);
            t.drop(host);
        }
        maxSize = p;
    }
    public Item remove(int p){
        Item r = items.get(p);
        weight -= r.weight;
        volume -= r.volume;
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
        for (String aData : data) {
            Item item = (Item) Stock.get(aData);
            weight += item.weight;
            Updater.cancelAdd(item);
            items.add(item);
        }
    }
    public String toString(){
        String r = ";";
        for (Item item : items) {
            r += item.toString() + ";";
        }
        return r;
    }
}