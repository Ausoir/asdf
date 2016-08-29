package com.ga.unit.item;

import com.ga.unit.Player;

import java.util.ArrayList;

/**
 * Created by Ga on 8/9/2016.
 */
public class Inventory {
    public int[] pockets = new int[]{-1,-1,-1};
    private ArrayList<Item> items = new ArrayList<>();
    public int wielded = -1, worn = -1, weight = 0;
    private Player player;
    public int maxSize = 5;
    public Inventory(Player player){
        this.player = player;
    }
    public int equip(int index){
        int result;
        Item item = items.get(index);
        if (item instanceof Weapon) {
            result = 1;
            player.setWeapon((Weapon) items.get(index));
            wielded = index;
        } else if (item instanceof Armor){
            result = 2;
            player.setArmor((Armor) items.get(index));
            worn = index;
        } else {
            result = 10;
            int pocket = getEmptyPocket(item);
            if (pocket > -1){
                result += pocket;
                pockets[pocket] = index;
            }
        }
        return result;
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
    public void unequip(int index){
        Item item = items.get(index);
        if (item instanceof Weapon) {
            if (player.weapon == item) player.setWeapon((Weapon) ItemFactory.produce("Fist", 0));
            if (index == wielded){
                ((Weapon) item).organ = null;
                wielded = -1;
            }
        } else if (item instanceof Armor){
            if (player.armor == item) player.setArmor((Armor) ItemFactory.produce("Underwear", 0));
            if (index == worn) worn = -1;
        } else {
            removePocket(item);
        }
    }
    public void removePocket(Item item){
        for (int i = 0; i < pockets.length; i++) {
            if (pockets[i] != -1 && items.get(pockets[i]) == item) pockets[i] = -1;
        }
    }
    public Item remove(int p){
        Item r = items.get(p);
        removePocket(r);
        if (wielded > p) wielded--;
        if (worn > p) worn--;
        items.remove(p);
        return r;
    }
    public Item remove(Item item){
        int i = items.indexOf(item);
        return remove(i);
    }
    public void load(String p){
        String[] data = p.split(";");
        for (int i=0;i<data.length;i++){
            String[] pair = data[i].split("@");
            Item item = ItemFactory.produce(pair[0],Integer.parseInt(pair[2].split(",")[0]));
            item.uses.setTotal(Integer.parseInt(pair[2].split(",")[2]));
            item.uses.set(Integer.parseInt(pair[2].split(",")[1]));
            weight += item.weight;
            if (pair[1].equals("w")){
                wielded = i;
                player.setWeapon((Weapon) item);
            } else if (pair[1].equals("e")){
                worn = i;
                player.setArmor((Armor) item);
            } else if (!pair[1].equals("")){
                pockets[Integer.parseInt(pair[1])] = i;
            }
            items.add(item);
        }
    }
    public String toString(){
        String r = "";
        for (int i = 0; i < items.size(); i++) {
            String prefix = "";
            Item item = items.get(i);
            if (wielded == i) prefix = "w";
            else if (worn == i) prefix = "e";
            else if (indexOf(item) != -1) prefix = ""+indexOf(item);
            r += item.name+"@"+prefix+"@"+item.seed+","+item.uses + ";";
        }
        if (r.length() > 0) r = r.substring(0,r.length()-1);
        return r;
    }
}