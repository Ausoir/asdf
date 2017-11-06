package com.ga.unit;

import com.ga.game.Command;
import com.ga.game.Input;
import com.ga.game.Keybinding;
import com.ga.unit.player.Player;
import com.ga.unit.skill.Actions;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Ga on 7/25/2016. asd
 */
public class PlayerInput implements Input {
    private HashMap<Integer, String> keyActionMap = new HashMap();
    public Player host;
    public PlayerInput(Player unit){
        host = unit;
        unit.input = this;
    }
    public void chat(String p, int x, int y){
        if (p.startsWith("/")) Command.order(p.substring(1));
        else if (!p.equals("")){
            int tx = (int) (x-host.pos.x);
            int ty = (int) (y-host.pos.y);
            host.target.set(tx,ty);
            host.say(p);
        }
    }
    public void changeMapping(int key, Actions event){
        keyActionMap.remove(key);
        Iterator i = keyActionMap.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            if (e.getValue().equals(event.getName())) i.remove();
        }
        keyActionMap.put(key, event.getName());
    }
    public String getMapping(Actions move){
        String r = null;
        for (Map.Entry<Integer, String> i : keyActionMap.entrySet()) {
            if (i.getValue().equals(move.getName())){
                int key = i.getKey();
                String keyname = KeyEvent.getKeyText(key);
                if (key == 1) keyname = "LMB";
                else if (key == 3) keyname = "RMB";
                r = keyname;
            }
        }
        return r;
    }
    public void input(int mode, int x, int y){
        if (mode == 0) inputDirect(null,x,y); //mousemove for better targetting
        else if (mode == Keybinding.get(",")) host.map.updater.sleep(50);
        else if (keyActionMap.containsKey(mode)){
            inputDirect(keyActionMap.get(mode),x,y);
        } else if (mode >= 49 && mode < 55) {
            inputDirect("_0"+(mode-49),x,y);
        } else if (mode > 9) inputDirect("_"+mode, x, y);
        //Global.server.sendFromLocal("IN>"+p + "@" + tx + "@" + ty);
    }
    public void inputDirect(String p, int x, int y){
        x = (int) (x-host.pos.x);
        y = (int) (y-host.pos.y);
        if (host.delay.over()) host.target.set(x,y);
        if (p != null) {
            if (p.startsWith("_")) {
                String t = p.substring(1);
                if (Character.digit(t.charAt(0),10) >= 0) {
                    host.selectingItem = Integer.parseInt(t);
                    host.setState(Actions.Wield);
                } else {
                    if (t.equals("Recall")) host.say(host.memorized);
                }
            } else {
                Actions move = Actions.getByName(p);
                if (move == Actions.Wield) host.selectingItem = -1;
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
                if (move != null) keyActionMap.put(Integer.parseInt(pair[0]), move.getName());
            }
        }
    }
    @Override
    public String toString(){
        String r = ";";
        for (Map.Entry<Integer, String> i : keyActionMap.entrySet()){
            r += i.getKey()+":"+i.getValue()+";";
        }
        return r;
    }
}
