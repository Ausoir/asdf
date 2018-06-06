package com.ga.unit.man;

import com.ga.map.Map;
import com.ga.unit.PlayerInput;

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
}