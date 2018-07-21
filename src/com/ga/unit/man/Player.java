package com.ga.unit.man;

import com.ga.map.Map;
import com.ga.unit.PlayerInput;
import com.ga.unit.Prop;

import java.util.ArrayList;

/**
 * Created by Ga on 7/30/2016. asd
 */
public class Player extends Man {
    public PlayerInput input;

    public Player(Map map, int seed) {
        super(map, null, seed);
        input = new PlayerInput(this);
        save = false;
        name = "Man";
        speechType = "p";
    }

    public void rest(){
        int level = 2;
        ArrayList<Prop> props = getAvailableProps();
        for (Prop i : props){
            if (i.name.equals("Bed")){
                level = 4;
                break;
            } else if (i.name.equals("Chair") || i.name.equals("Shortchair") || i.name.equals("Bench")) level = 3;
        }
        get("Fatigue").add(level, false);
        map.updater.sleep(1, true);
    }
}