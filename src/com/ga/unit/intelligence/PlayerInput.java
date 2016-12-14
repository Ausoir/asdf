package com.ga.unit.intelligence;

import com.ga.game.*;
import com.ga.map.Global;
import com.ga.tool.P;
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
public class PlayerInput implements Input, Observer {
    private HashMap<Integer, Moveset> keyMap = new HashMap<Integer, Moveset>(){{
        put(-1, Actions.Idle);
        put(-65, Actions.Idle);
        put(-68, Actions.Idle);
        put(KeyEvent.VK_W, Actions.Wait);
        put(KeyEvent.VK_A, Actions.WalkL);
        put(KeyEvent.VK_S, Actions.Duck);
        put(KeyEvent.VK_D, Actions.WalkR);
        put(KeyEvent.VK_F, Skills.Kick);
        put(KeyEvent.VK_R, Actions.Sheathe);
        put(KeyEvent.VK_Z, Actions.Interact);
        put(1, Skills.Use);
        put(3, Actions.Idle);
        put(KeyEvent.VK_SHIFT, Skills.Rest);
    }};
    private String key;
    public Player host;
    public P mP = new P();
    public float mR;
    public PlayerInput(Player unit){
        Global.display.input = this;
        host = unit;
        unit.brain = null;
        Global.player = this;
    }
    public void chat(String p){
        Command.speechOrder(p);
        host.say(p);
    }
    public void changeMapping(int key, Moveset event){
        keyMap.remove(key);
        Iterator i = keyMap.entrySet().iterator();
        while (i.hasNext()){
            Map.Entry e = (Map.Entry) i.next();
            if (e.getValue() == event) i.remove();
        }
        keyMap.put(key, event);
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
    public void inputEcho(String action, int x, int y){
        mP.x = x - Settings.centerX;
        mP.y = y - Settings.centerY;
        mR = (float) Math.atan2(mP.y,mP.x);
        host.face(mP.x > host.pos.x-Global.camera.x ? 1 : -1);
        key = action;
    }
    public void input(int mode, int x, int y){
        if (mode == KeyEvent.VK_ENTER) Display.chatbox.requestFocus();
        else if (mode == KeyEvent.VK_TAB) Display.menu.toggle();
        else if (mode > 48 && mode < 57) {
            Global.server.send("IN@I" + (mode-49) + "," + x + "," + y);
        } else if (keyMap.containsKey(mode)){
            Global.server.send("IN@" + keyMap.get(mode).getCodeName() + "," + x + "," + y);
        }
    }
    @Override
    public void update(){
        if (key != null) {
            String t = key.substring(1);
            if (key.startsWith("A")) host.setState(Actions.getByName(t));
            else if (key.startsWith("K")) host.setState(Skills.getByName(t));
            else if (key.startsWith("M")) host.setState(Spells.getByName(t));
            else if (key.startsWith("I")) host.selectItem(Integer.parseInt(t));
            key = null;
        }
    }
    public boolean teleportReady(){
        if (keyMap.get(key) == Actions.Wait) return true;
        return false;
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
