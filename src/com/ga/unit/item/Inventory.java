package com.ga.unit.item;

import com.ga.unit.Player;

import java.util.ArrayList;

/**
 * Created by Ga on 8/9/2016.
 */
public class Inventory {
    public int[] pockets = new int[]{-1,-1,-1};
    private ArrayList<Item> items = new ArrayList<>();
    public int weight = 0;
    private Player player;
    public int maxSize = 5;
    public Inventory(Player player){
        this.player = player;
    }
    public int equip(int index){
        int result = 0;
        Item item = items.get(index);
        int pocket = getEmptyPocket(item);
        if (pocket > -1){
            result += pocket;
            pockets[pocket] = index;
        }
        return result;
    }
    public void unequip(int index){
        Item item = items.get(index);
        item.unequip(player);
        removePocket(item);
    }
    public boolean addItem(Item p){
        if (items.size() >= maxSize) return false;
        items.add(p);
        weight += p.weight;
        return true;
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
    private int getEmptyPocket(Item item){
        int r = 0;
        for (int i = 0; i < pockets.length; i++) {
            if (pockets[i] != -1 && items.get(pockets[i]) == item) return i;
        }
        for (int i = pockets.length-1; i >= 0; i--) {
            if (pockets[i] == -1) r = i;
        }
        return r;
    }
    private int indexOf(Item item){
        for (int j = 0; j < pockets.length; j++) {
            int pocket = pockets[j];
            if (pocket != -1 && items.size() > pocket && items.get(pocket) == item) return j;
        }
        return -1;
    }
    public void removePocket(Item item){
        for (int i = 0; i < pockets.length; i++) {
            if (pockets[i] != -1 && items.get(pockets[i]) == item) pockets[i] = -1;
        }
    }
    public Item remove(int p){
        Item r = items.get(p);
        removePocket(r);
        for (int i=0;i<pockets.length;i++){
            if (pockets[i] > p) pockets[i]--;
        }
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
            String[] prefix = data[i].split(":");
            Item item = ItemFactory.load(prefix[0]);
            weight += item.weight;
            if (!prefix[1].equals("x")){
                pockets[Integer.parseInt(prefix[1])] = i;
            }
            items.add(item);
        }
    }
    public String toString(){
        String r = ";";
        for (int i = 0; i < items.size(); i++) {
            String prefix = "x";
            Item item = items.get(i);
            if (indexOf(item) != -1) prefix = "" + indexOf(item);
            r += item.toItemString()+":"+prefix+";";
        }
        return r;
    }
}