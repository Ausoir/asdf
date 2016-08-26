package com.ga.unit;

import com.ga.map.Global;

/**
 * Created by Ga on 8/5/2016.
 */
public class Item extends Thing {
    int weight = 1;
    int cooldown = 100;
    int delay = 10;
    Actions action = Actions.CONSUME;
    private int[] lastused = new int[]{0};
    int uses = -1;
    public Item(String name) {
        super(name);
    }
    public boolean use(Man host){
        if (uses != 0 && Global.cooldown(lastused, cooldown)) {
            uses--;
            return true;
        }
        return false;
    }
    public String toString(){
        return "";
    }
}