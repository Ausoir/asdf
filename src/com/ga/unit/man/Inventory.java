package com.ga.unit.man;

import com.ga.tool.L;
import com.ga.tool.T;
import com.ga.unit.item.Item;

/**
 * Created by Ga on 8/9/2016. asd
 */
public class Inventory {
    private L<Item> items = new L<>();
    private int maxSize = 0;
    private Man host;
    Inventory(Man host){
        this.host = host;
    }
    public String toString(){
        if (items.size() == 0) return "";
        StringBuilder r = new StringBuilder();
        for (Item item : items) r.append(T.wrap(item.toString())).append(",");
        return T.wrap(r.substring(0,r.length()-1));
    }
    public boolean addItem(Item p){
        if (vacant()) {
            items.add(p);
            return true;
        }
        return false;
    }
    public boolean vacant(){
        return items.size() < maxSize;
    }
    public Item getItem(int p){
        return items.get(p);
    }
    public L<Item> getItems(){
        return items;
    }
    public int getItemSize(){
        return items.size();
    }
    public void setMaxSize(int p){
        while (items.size() > p){
            Item t = items.remove(0);
            t.drop(host);
            t.start();
        }
        maxSize = p;
    }
    public Item remove(int p){
        Item r = items.get(p);
        items.remove(p);
        return r;
    }
    public Item remove(Item item){
        int i = items.indexOf(item);
        if (i == -1) return null;
        return remove(i);
    }
}