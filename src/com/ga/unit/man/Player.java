package com.ga.unit.man;

import com.ga.map.Map;
import com.ga.unit.PlayerInput;
import com.ga.unit.Prop;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Buffs;

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

    public int setState(Actions p){
        if (status.getEffectLevel(Buffs.Morph) > 0){
            if (!clip.isStateValid(p.name())) p = Actions.Melee;
        }
        return super.setState(p);
    }

    public void rest(){
        int level = 4;
        ArrayList<Prop> props = getAvailableProps(e -> true);
        for (Prop i : props){
            if (i.name.equals("Bed")){
                level = 24;
                break;
            } else if (i.name.equals("Chair") || i.name.equals("Shortchair") || i.name.equals("Bench")) level = 12;
        }
        stats.fatigue.add(level, false);
        map.updater.sleep(1, true);
    }
}