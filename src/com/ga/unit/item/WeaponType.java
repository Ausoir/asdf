package com.ga.unit.item;

import com.ga.unit.Actions;

/**
 * Created by Ga on 8/30/2016.
 */
enum WeaponType {
    SWING(Actions.SWING), STAB(Actions.PIERCE);
    Actions action;
    WeaponType(Actions action){
        this.action = action;
    }
    Actions get(){
        return action;
    }
}
