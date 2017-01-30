package com.ga.unit;

import com.ga.game.Display;
import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Moveset;

/**
 * Created by Ga on 12/29/2016.
 */
public class MainPlayer extends Player {
    public MainPlayer(String p) {
        super(p);
    }
    @Override
    public void say(String p){
        Display.menu.setVisible(false);
        if (p.startsWith("'") && p.endsWith("'")){
            memorized = p.substring(1,p.length()-1);
            p = "Memorized "+p;
        }
        super.say(p);
    }
    @Override
    public int setState(Moveset state) {
        if (state != Actions.Idle && state != Actions.Wait) {
            Display.menuShop.setVisible(false);
            if (state == Actions.Recall) say(memorized);
        }
        return super.setState(state);
    }
    public void onCollide(Line r){
        super.onCollide(r);
        if (state != Actions.Dead) {
            if (r.opt == 2) {
                if (state == Actions.Interact){
                    clip.setState(Actions.Wait);
                    vel.set(0, 0);
                    Global.map.teleport(pos, r);
                }
            } else if (r.opt >= 100) {
                if (r.opt >= 200){
                    if (state == Actions.Interact){
                        clip.setState(Actions.Wait);
                        Global.map.loadMap(r.opt - 200);
                    }
                } else Global.map.loadMap(r.opt - 100);
            }
        }
    }
    public void update(){
        super.update();
        if (state == Actions.LookUp) Global.mapEdgeCenter(Global.cameraTo = Global.cameraTo.getAdd(0,-50));
        else if (state == Actions.LookLeft) Global.mapEdgeCenter(Global.cameraTo = Global.cameraTo.getAdd(-50,0));
        else if (state == Actions.LookDown) Global.mapEdgeCenter(Global.cameraTo = Global.cameraTo.getAdd(0,50));
        else if (state == Actions.LookRight) Global.mapEdgeCenter(Global.cameraTo = Global.cameraTo.getAdd(50,0));
        else if (state != Actions.Idle) Global.mapEdgeCenter(Global.cameraTo = pos.getAdd(0,0));
    }
}
