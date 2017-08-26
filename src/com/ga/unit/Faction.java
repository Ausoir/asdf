package com.ga.unit;

import com.ga.map.Global;
import com.ga.map.Updater;

import java.util.ArrayList;

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
    Evil(3){
        void updateArmy(){
            super.updateArmy();
            if (armySize > 0){
            }
        }
    },
    Predator(4),
    Rogue(5),
    Temporary(0),
    Destructive(-1);
    private int side;
    int armySize;
    ArrayList<Integer> controlIds;
    Faction(int side) {
        this.side = side;
    }
    public boolean canAttack(Faction p){
        return side == -1 || side != p.side;
    }
    void updateArmy(){
        int prevArmySize = armySize;
        controlIds = new ArrayList<>();
        for (Unit i : Updater.units){
            if (i.faction == this){
//                if (i.brain != null && !i.map.equals(Global.map.name)){
//                    i.brain.updateOutside();
//                    if (i.cmap.equals(Global.map.name)) i.start();
//                }
//                armySize++;
//                controlIds.add(i.controlId);
            }
        }
    }
    public static void update(){
        Evil.updateArmy();
        Neutral.updateArmy();
    }
}