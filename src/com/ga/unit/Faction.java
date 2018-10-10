package com.ga.unit;

/**
 * Created by Ga on 9/5/2016. a
 */
public enum Faction {
    Player(1),
    Player2(11),
    Helpless(1),
    Neutral(1),
    Punk(2),
    Evil(3),
    Predator(4),
    Rogue(5),
    Destructive(0);
    private int side;
    Faction(int side) {
        this.side = side;
    }
    public boolean canAttack(Faction p){
        return side == Destructive.side || side != p.side;
    }
}