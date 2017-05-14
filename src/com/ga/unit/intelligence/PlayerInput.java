package com.ga.unit.intelligence;

import com.ga.game.Command;
import com.ga.game.Input;
import com.ga.game.Keybinding;
import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.unit.player.Player;
import com.ga.unit.skill.Actions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Ga on 7/25/2016. asd
 */
public class PlayerInput implements Input {
    private HashMap<Integer, String> keyMap = new HashMap<Integer, String>(){{
        put(-1, Actions.Idle.getName());
        put(Keybinding.get("W"), Actions.Jump.getName());
        put(Keybinding.get("A"), Actions.WalkL.getName());
        put(Keybinding.get("S"), Actions.Drop.getName());
        put(Keybinding.get("D"), Actions.Walk.getName());
        put(Keybinding.get("SHIFT"), Actions.Rest.getName());
        put(Keybinding.get("SPACE"), Actions.Kick.getName());
        put(Keybinding.get("R"), Actions.Wield.getName());
        put(Keybinding.get("M1"), Actions.Use.getName());
        put(Keybinding.get("M2"), Actions.Throw.getName());
        put(Keybinding.get("M3"), Actions.Grab.getName());
        put(Keybinding.get("Q"), Actions.Throw.getName());
        put(Keybinding.get("GRAVE"), "_Recall");
        put(Keybinding.get(","), "_Rest");
    }};
    public P movement = new P();
    public Player host;
    public int moving = 0;
    public boolean cameraMode = false;
    public PlayerInput(Player unit){
        host = unit;
        unit.brain = null;
        unit.input = this;
    }
    public void chat(String p, int x, int y){
        if (p.startsWith("/") && Global.server.server) Global.server.order(p.substring(1));
        else if (!p.equals("")){
            int tx = (int) (x-host.pos.x);
            int ty = (int) (y-host.pos.y);
            host.target.set(tx,ty);
            Global.server.sendFromLocal("CT>"+p + "@" + tx + "@" + ty);
            host.say(p);
        }
    }
    public void chatDirect(String p, int x, int y){
        host.target.set(x,y);
        host.say(p);
    }
    public void changeMapping(int key, Actions event){
        keyMap.remove(key);
        Iterator i = keyMap.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            if (e.getValue() == event) i.remove();
        }
        keyMap.put(key, event.getName());
    }
    public String getMapping(Actions move){
        String r = null;
        for (Map.Entry<Integer, String> i : keyMap.entrySet()) {
            if (i.getValue().equals(move.getName())){
                int key = i.getKey();
                String keyname = ""+((char)key);
                if (key == 1) keyname = "LMB";
                else if (key == 3) keyname = "RMB";
                else if (key == 32) keyname = "SPACE";
                else if (key == 16) keyname = "SHIFT";
                r = keyname;
            }
        }
        return r;
    }
    private void moveCamera(float x, float y){
        cameraMode = true;
        movement.set(x, y);
    }
    public void input(int mode, int x, int y){
        x = (int) (x-host.pos.x);
        y = (int) (y-host.pos.y);
        if (mode == 0) inputDirect(null,x,y); //mousemove for better targetting
        else if (mode == Keybinding.get("ENTER")) Global.display.chatbox.requestFocus();
        else if (mode == Keybinding.get("TAB")) Global.display.menu.toggle();
        else if (mode == Keybinding.get("UP")) moveCamera(0,-80);
        else if (mode == Keybinding.get("RIGHT")) moveCamera(80,0);
        else if (mode == Keybinding.get("LEFT")) moveCamera(-80,0);
        else if (mode == Keybinding.get("DOWN")) moveCamera(0,80);
        else if (mode == Keybinding.get(",")) Global.sleep(50);
        else if (keyMap.containsKey(mode)){
            inputDirect(keyMap.get(mode),x,y);
        } else if (mode >= 49 && mode < 55) {
            inputDirect("_0"+(mode-49),x,y);
        } else inputDirect("_"+mode+"", x, y);
        //Global.server.sendFromLocal("IN>"+p + "@" + tx + "@" + ty);
    }
    public void inputDirect(String p, int x, int y){
        if (host.delay.over()) host.target.set(x,y);
        if (p != null) {
            movement.set(0,0);
            if (p.startsWith("_")) {
                String t = p.substring(1);
                if (t.length() == 2) {
                    host.selectingItem = Integer.parseInt(t);
                    host.setState(Actions.Wield);
                } else {
                    if (t.equals("Recall")) host.say(host.memorized);
                }
            } else {
                Actions move = Actions.getByName(p);
                if (move == Actions.Idle){
                    moving = 0;
                    if (host.state != Actions.Walk) return;
                } else if (move == Actions.WalkL) {
                    moving = -1;
                    move = Actions.Walk;
                } else if (move == Actions.Walk) moving = 1;
                else if (move == Actions.Wield) host.selectingItem = -1;
                if (move != null){
                    host.setState(move);
                }
            }
        }
    }
    public void load(String p){
        if (p.length() > 1) {
            String[] data = p.substring(1).split(";");
            for (String i : data) {
                String[] pair = i.split(":");
                Actions move = Actions.getByName(pair[1]);
                if (host.skillTree.hasSkill(move))
                    keyMap.put(Integer.parseInt(pair[0]), move.getName());
            }
        }
    }
    @Override
    public String toString(){
        String r = ";";
        for (Map.Entry<Integer, String> i : keyMap.entrySet()){
            r += i.getKey()+":"+i.getValue()+";";
        }
        return r;
    }
}
