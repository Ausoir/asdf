package com.ga.unit.item;

import com.ga.unit.Man;

/**
 * Created by Ga on 2/17/2017.
 */
public class Food extends Item {
    private int perish = 10;
    public Food(String name, int seed, int weight, int uses, int perish) {
        super(name, seed, weight, uses, 0, 0);
        this.perish = perish;
    }
    public void onMapChange(){
        if (perish > 0) perish--;
    }
    @Override
    public boolean use(Man p){
        if (perish == 0) return false;
        boolean r = super.use(p);
        if (r && uses.get() == 0) remove();
        return r;
    }
}
