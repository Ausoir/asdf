package com.ga.unit;

import java.util.ArrayList;

/**
 * Created by Ga on 8/9/2016.
 */
public class Inventory {
    public int[] pockets = new int[]{-1,-1,-1};
    public ArrayList<Item> items = new ArrayList<>();
    public int wielded = -1;
    private Player player;
    Inventory(Player player){
        this.player = player;
    }
    public int equip(int index){
        int result = 0;
        Item item = items.get(index);
        if (item instanceof Weapon){
            result = 1;
            player.setWeapon((Weapon) items.get(index));
            wielded = index;
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
            if (items.get(pocket) == item) return j;
        }
        return -1;
    }
    public void unequip(int index){
        Item item = items.get(index);
        if (item instanceof Weapon){
            if (player.weapon == item) player.setWeapon(ItemFactory.produceWeapon("Fist"));
            if (index == wielded) wielded = -1;
        } else {
            removePocket(item);
        }
    }
    public void removePocket(Item item){
        for (int i = 0; i < pockets.length; i++) {
            if (items.get(pockets[i]) == item) pockets[i] = -1;
        }
    }
    public void remove(Item item){
        removePocket(item);
        items.remove(item);
    }
    public void load(String p){
        String[] data = p.split(";");
        for (int i=0;i<data.length;i++){
            String[] pair = data[i].split("@");
            Item item = ItemFactory.produce(pair[0]);
            item.seed = Integer.parseInt(pair[2].split(",")[0]);
            item.uses = Integer.parseInt(pair[2].split(",")[1]);
            if (pair[1].equals("w")){
                wielded = i;
                player.setWeapon((Weapon) item);
            }
            else if (!pair[1].equals("")){
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
            else if (indexOf(item) != -1) prefix = ""+indexOf(item);
            r += item.name+"@"+prefix+"@"+item.seed+","+item.uses + ";";
        }
        if (r.length() > 0) r = r.substring(0,r.length()-1);
        return r;
    }
}