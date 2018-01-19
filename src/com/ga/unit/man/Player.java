package com.ga.unit.man;

import com.ga.map.Map;
import com.ga.unit.PlayerInput;
import com.ga.unit.skill.Actions;

/**
 * Created by Ga on 7/30/2016. asd
 */
public class Player extends Man {
    public PlayerInput input;
    public Player(Map map, int seed) {
        super(map, null, seed);
        input = new PlayerInput(this);
        save = false;
        name = "Player";
        speechType = "p";
    }
    @Override
    public int setState(Actions state){
        int r = super.setState(state);
        if (r != 0){
            if (r == 2) {
                if (state.getCost() > get("Stamina").getMax()) say("Not enough Stamina!");
            } else if (r == 4) {
                say("Cannot cast!");
            } else if (r == 5) say("This action is beyond my abilities!");
        }
        return r;
    }
    public void stateSet(Actions state){
        if (state.getCost() >= 0){
            int difficulty = state.getDifficulty();
            if (getVacancy() < difficulty){
                if (difficulty > 0) say("Too encumbering! This action requires "+state.getDifficulty()+" unencumbrance.");
                else say("Too encumbering!");
                state = Actions.Hit;
            }
        }
        super.stateSet(state);
    }
}