package com.ga.unit;

/**
 * Created by Ga on 9/5/2016. a
 */
public enum Faction {
    Player(1),
    Helpless(1),
    Neutral(1),
    Merchant(1),
    Guard(1),
    Punk(2),
    Evil(3),
    Predator(4),
    Rogue(5),
    Temporary(0),
    Destructive(-1);
    private int side;
    Faction(int side) {
        this.side = side;
    }
    public boolean canAttack(Faction p){
        return side == -1 || side != p.side;
    }
}