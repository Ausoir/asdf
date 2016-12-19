package com.ga.unit.intelligence;

import com.ga.game.Command;
import com.ga.game.Display;
import com.ga.game.Input;
import com.ga.game.Settings;
import com.ga.map.Global;
import com.ga.map.Updatable;
import com.ga.tool.P;
import com.ga.tool.Word;
import com.ga.unit.Player;
import com.ga.unit.SkillTree;
import com.ga.unit.Updater;
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
public class PlayerInput implements Input, Updatable {
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
        put(49, Actions.Pull1);
        put(50, Actions.Pull2);
        put(51, Actions.Pull3);
        put(1, Skills.Use);
        put(3, Actions.Idle);
        put(KeyEvent.VK_SHIFT, Skills.Rest);
    }};
    private String key;
    public Player host;
    public P mP = new P();
    public float mR;
    public int tick = -1;
    private String resend;
    public PlayerInput(Player unit){
        host = unit;
        unit.brain = null;
        unit.input = this;
        Updater.addQueue.add(this);
    }
    public void chat(String p){
        if (p.startsWith("/")) Command.order(p.substring(1));
        else Global.server.sendFromLocal("IN@"+p);
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
    public void inputEcho(int when, String action, int x, int y){
        tick = when;
        mP.x = x;
        mP.y = y;
        mR = (float) Math.atan2(mP.y,mP.x);
        key = action;
    }
    public void input(int mode, int x, int y){
        if (mode == KeyEvent.VK_ENTER) Display.chatbox.requestFocus();
        else if (mode == KeyEvent.VK_TAB) Display.menu.toggle();
        else if (keyMap.containsKey(mode) && key == null){
            tick = Updater.tick + Settings.latency;
            resend = "IN@"+tick+","+keyMap.get(mode).getCodeName() + "," + x + "," + y;
            Global.server.sendFromLocal(resend);
        }
    }
    public void inputDirect(String p, float x, float y){
        String t = p.substring(1);
        host.target = new P(x,y).getSubtract(host.pos);
        host.face(host.target.x > 0 ? 1 : -1);
        if (p.startsWith("A")) host.setState(Actions.getByName(t));
        else if (p.startsWith("K")) host.setState(Skills.getByName(t));
        else if (p.startsWith("M")) host.setState(Spells.getByName(t));
        else if (p.startsWith("I")) host.selectItem(Integer.parseInt(t));
    }
    public void update(){
        if (Updater.tick == tick) {
            if (key == null){
                Updater.pause = true;
                Global.server.sendFromLocal(resend);
            } else {
                Updater.pause = false;
                inputDirect(key, mP.x, mP.y);
                key = null;
            }
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
