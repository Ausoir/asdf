package com.ga.unit;

/**
 * Created by Ga on 9/5/2016.
 */
public enum Faction {
    Player(new boolean[]{true,true,false,false,false}), Neutral(new boolean[]{true,true,true,true,true}), Guard(new boolean[]{true,true,true,false,false}),
    Evil(new boolean[]{false,false,false,true,false}), Hostile(new boolean[]{false,false,false,false,false});
    private boolean[] allied;
    Faction(boolean[] allied) {
        this.allied = allied;
    }
    public boolean canAttack(Faction p){
        return !allied[p.ordinal()];
    }
}
