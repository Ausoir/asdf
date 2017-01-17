package com.ga.unit.intelligence;

import com.ga.game.Display;
import com.ga.game.Input;
import com.ga.map.Global;
import com.ga.tool.Word;
import com.ga.unit.Player;
import com.ga.unit.SkillTree;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Moveset;
import com.ga.unit.skill.Skills;
import com.ga.unit.skill.Spells;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Ga on 7/25/2016.
 */
public class PlayerInput implements Input {
    private HashMap<Integer, Moveset> keyMap = new HashMap<Integer, Moveset>(){{
        put(-1, Actions.Idle);
        put(KeyEvent.VK_W, Actions.Interact);
        put(KeyEvent.VK_A, Actions.WalkL);
        put(KeyEvent.VK_S, Actions.Down);
        put(KeyEvent.VK_D, Actions.WalkR);
        put(KeyEvent.VK_R, Actions.Wield);
        put(1, Skills.Use);
        put(KeyEvent.VK_UP, Actions.LookUp);
        put(KeyEvent.VK_LEFT, Actions.LookLeft);
        put(KeyEvent.VK_DOWN, Actions.LookDown);
        put(KeyEvent.VK_RIGHT, Actions.LookRight);
        put(KeyEvent.VK_SHIFT, Skills.Rest);
        put(KeyEvent.VK_BACK_QUOTE, Actions.Recall);
    }};
    public Player host;
    public int moving = 0;
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
    public void changeMapping(int key, Moveset event){
        if (!(event instanceof Spells)) {
            keyMap.remove(key);
            Iterator i = keyMap.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry) i.next();
                if (e.getValue() == event) i.remove();
            }
            keyMap.put(key, event);
        }
    }
    public String getMapping(Moveset move){
        String r = null;
        for (Map.Entry<Integer, Moveset> i : keyMap.entrySet()) {
            if (i.getValue() == move){
                r = Word.getKeyName(i.getKey());
            }
        }
        return r;
    }
    public void input(int mode, int x, int y){
        if (mode == KeyEvent.VK_ENTER) Display.chatbox.requestFocus();
        else if (mode == KeyEvent.VK_TAB) Display.menu.toggle();
        else if (keyMap.containsKey(mode)){
            input(keyMap.get(mode).getCodeName(),x,y);
        } else if (mode >= 49 && mode < 55) {
            input("I"+(mode-49),x,y);
        }
    }
    public void input(String p, int x, int y){
        int tx = (int) (x-host.pos.x);
        int ty = (int) (y-host.pos.y);
        Global.server.sendFromLocal("IN>"+p + "@" + tx + "@" + ty);
        inputDirect(p, tx, ty);
    }
    public void inputDirect(String p, int x, int y){
        String t = p.substring(1);
        host.target.set(x,y);
        if (p.startsWith("A")){
            if (t.equals("Idle")) moving = 0;
            else if (t.equals("WalkL")) moving = -1;
            else if (t.equals("WalkR")) moving = 1;
            host.selectingItem = -1;
            host.setState(Actions.getByName(t));
        }
        else if (p.startsWith("K")) host.setState(Skills.getByName(t));
        else if (p.startsWith("I")){
            host.selectingItem = Integer.parseInt(t);
            host.setState(Actions.Wield);
        }
    }
    public void load(String p){
        if (p.length() > 1) {
            String[] data = p.substring(1).split(";");
            for (String i : data) {
                String[] pair = i.split(":");
                Moveset move = SkillTree.getByName(pair[1]);
                if (host.skillTree.hasSkill(move))
                    keyMap.put(Integer.parseInt(pair[0]), move);
            }
        }
    }
    @Override
    public String toString(){
        String r = ";";
        for (Map.Entry<Integer, Moveset> i : keyMap.entrySet()){
            r += i.getKey()+":"+i.getValue()+";";
        }
        return r;
    }
}
