package com.ga.unit;

/**
 * Created by Ga on 9/5/2016.
 */
public enum Faction {
    Player(1),
    Player2(10),
    Neutral(1),
    Merchant(1),
    Guard(1),
    Punk(2),
    Evil(3),
    Destructive(0);
    private int side;
    Faction(int side) {
        this.side = side;
    }
    public boolean canAttack(Faction p){
        return side == 0 || side != p.side;
    }
}
