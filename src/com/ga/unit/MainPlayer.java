package com.ga.unit;

import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.tool.Performer;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Moveset;
import com.ga.unit.skill.Stat;

/**
 * Created by Ga on 12/29/2016.
 */
public class MainPlayer extends Player {
    public MainPlayer(String p) {
        super(p);
        final Stat hunger = get("Hunger");
        hunger.onChange = new Performer(){
            public void perform(){
                int hung = hunger.get();
                if (hung < 0){
                    get("Health").add(hung);
                    say("I'm starving!");
                } else if (hung < 10) say("I'm hungry!");
            }
        };
    }
    @Override
    public void say(String p){
        if (p.startsWith("'") && p.endsWith("'")){
            memorized = p.substring(1,p.length()-1);
            p = "Memorized "+p;
        }
        super.say(p);
    }
    @Override
    public int setState(Moveset state) {
        if (state != Actions.Idle && state != Actions.Wait) {
            if (state == Actions.Recall) say(memorized);
        }
        return super.setState(state);
    }
    public void onCollide(Line r){
        super.onCollide(r);
        if (r.opt >= 2) {
            if (state != Actions.Dead) {
                if (r.opt >= 200){
                    if (state == Actions.Interact){
                        clip.setState(Actions.Wait);
                        Global.map.loadMap(r.opt - 200);
                    }
                } else if (r.opt >= 100) Global.map.loadMap(r.opt - 100);
                else if (r.opt == 2){
                    if (state == Actions.Interact){
                        clip.setState(Actions.Wait);
                        vel.set(0, 0);
                        Global.map.teleport(pos, r);
                    }
                }
            }
        }
    }
    public void onMapChange(){
        reset();
        get("Hunger").add(-2, true);
        inventory.onMapChange();
    }
    public void update(){
        int edge = Global.mapEdge(pos.getAdd(vel));
        if (edge % 2 == 0) Global.map.loadMap(edge, pos.y);
        super.update();
        if (state == Actions.LookUp) Global.mapEdgeCenter(Global.cameraTo = Global.cameraTo.getAdd(0,-80));
        else if (state == Actions.LookLeft) Global.mapEdgeCenter(Global.cameraTo = Global.cameraTo.getAdd(-80,0));
        else if (state == Actions.LookDown) Global.mapEdgeCenter(Global.cameraTo = Global.cameraTo.getAdd(0,80));
        else if (state == Actions.LookRight) Global.mapEdgeCenter(Global.cameraTo = Global.cameraTo.getAdd(80,0));
        else if (state != Actions.Idle) Global.mapEdgeCenter(Global.cameraTo = pos.getAdd(0,0));
    }
    public void load(String p){
        super.load(p);
        String[] other = p.split("%")[1].split(";");
        Global.setTime(Integer.parseInt(other[3]));
        Global.setHour(Integer.parseInt(other[4]));
    }
}
