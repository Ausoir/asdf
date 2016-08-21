package com.ga.unit;

import java.util.ArrayList;

/**
 * Created by Ga on 8/9/2016.
 */
public class Inventory {
    public Item[] pockets = new Item[3];
    public ArrayList<Item> items = new ArrayList<>();
    public int wielded = -1;
    Player player;
    public Inventory(Player player){
        this.player = player;
        items.add(ItemFactory.produceWeapon("Sword"));
        items.add(ItemFactory.produceWeapon("Spear"));
        items.add(ItemFactory.produceItem("Potion"));
        items.add(ItemFactory.produceItem("Fireball"));
        items.add(ItemFactory.produceItem("Fireball"));
        items.add(ItemFactory.produceItem("Potion"));
        items.add(ItemFactory.produceItem("Fireball"));
    }
    public int equip(int index){
        int result = 0;
        Item item = items.get(index);
        if (item instanceof Weapon){
            result = 1;
            player.setWeapon((Weapon) items.get(index));
            wielded = index;
        } else if (item instanceof Item){
            result = 10;
            int pocket = getEmptyPocket(item);
            if (pocket > -1){
                result += pocket;
                pockets[pocket] = item;
            }
        }
        return result;
    }
    public int getEmptyPocket(Item item){
        int r = 0;
        for (int i = pockets.length-1; i >= 0; i--) {
            if (pockets[i] == item) return i;
        }
        for (int i = pockets.length-1; i >= 0; i--) {
            if (pockets[i] == null) r = i;
        }
        return r;
    }
    public void unequip(int index){
        Item item = items.get(index);
        if (item instanceof Weapon){
            if (player.weapon == item) player.setWeapon(ItemFactory.produceWeapon("Fist"));
            wielded = -1;
        } else if (item instanceof Item){
            removePocket(item);
        }
    }
    public void removePocket(Item item){
        for (int i = 0; i < pockets.length; i++) {
            if (pockets[i] == item) pockets[i] = null;
        }
    }
    public void remove(Item item){
        removePocket(item);
        items.remove(item);
    }
}