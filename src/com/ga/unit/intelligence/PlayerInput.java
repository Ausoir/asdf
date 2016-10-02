package com.ga.unit.intelligence;

import com.ga.game.Command;
import com.ga.game.Input;
import com.ga.game.Observer;
import com.ga.game.Settings;
import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.tool.Word;
import com.ga.unit.Player;
import com.ga.unit.SkillTree;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Moveset;
import com.ga.unit.skill.Skills;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Ga on 7/25/2016.
 */
public class PlayerInput implements Input, Observer {
    private static HashMap<Integer, Moveset> keyMap = new HashMap<Integer, Moveset>(){{
        put(-1, Actions.Idle);
        put(KeyEvent.VK_W, Actions.Enter);
        put(KeyEvent.VK_A, Actions.WalkL);
        put(KeyEvent.VK_S, Actions.Duck);
        put(KeyEvent.VK_D, Actions.WalkR);
        put(KeyEvent.VK_SPACE, Skills.Jump);
        put(KeyEvent.VK_C, Actions.Conjure);
        put(KeyEvent.VK_F, Skills.Kick);
        put(KeyEvent.VK_Z, Actions.Pickup);
        put(1, Skills.Weapon);
        put(3, Actions.Idle);
        put(KeyEvent.VK_SHIFT, Skills.Rest);
    }};
    private ArrayList<Integer> keyPressed = new ArrayList<>();
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
    public void input(boolean key, int mode, int x, int y){
        if (!key) {
            mP.x = x - Settings.centerX;
            mP.y = y - Settings.centerY;
            mR = (float) Math.atan2(mP.y,mP.x);
        }
        if (keyMap.containsKey(mode))
            if (mode > 0 && keyPressed.indexOf(mode) == -1) keyPressed.add(mode);
        if (mode < 0)
            keyPressed.remove((Object) (-mode));
        if (mode > 48 && mode < 57) host.selectItem(mode-49);
    }
    public void command(int mode){
        host.setState(keyMap.get(mode));
    }
    @Override
    public void update(){
        if (keyPressed.size() > 0)
            for (Integer i : keyPressed){
                command(i);
            }
        else command(-1);
    }
    public boolean teleportReady(){
        if (keyPressed.size() > 0)
            for (Integer i : keyPressed){
                if (keyMap.get(i) == Actions.Enter) return true;
            }
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
