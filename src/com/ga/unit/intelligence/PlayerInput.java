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
 * Created by Ga on 7/25/2016. asd
 */
public class PlayerInput implements Input {
    private HashMap<Integer, String> keyMap = new HashMap<Integer, String>(){{
        put(-1, Actions.Idle.getCodeName());
        put(KeyEvent.VK_W, Skills.Jump.getCodeName());
        put(KeyEvent.VK_A, Actions.WalkL.getCodeName());
        put(KeyEvent.VK_S, Skills.Rest.getCodeName());
        put(KeyEvent.VK_D, Actions.Walk.getCodeName());
        put(KeyEvent.VK_R, Actions.Wield.getCodeName());
        put(1, Actions.Use.getCodeName());
        put(KeyEvent.VK_UP, Actions.LookUp.getCodeName());
        put(KeyEvent.VK_LEFT, Actions.LookLeft.getCodeName());
        put(KeyEvent.VK_DOWN, Actions.LookDown.getCodeName());
        put(KeyEvent.VK_RIGHT, Actions.LookRight.getCodeName());
        put(KeyEvent.VK_BACK_QUOTE, Actions.Recall.getCodeName());
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
            keyMap.put(key, event.getCodeName());
        }
    }
    public String getMapping(Moveset move){
        String r = null;
        for (Map.Entry<Integer, String> i : keyMap.entrySet()) {
            if (i.getValue().equals(move.getCodeName())){
                r = Word.getKeyName(i.getKey());
            }
        }
        return r;
    }
    public void input(int mode, int x, int y){
        x = (int) (x-host.pos.x);
        y = (int) (y-host.pos.y);
        if (mode == KeyEvent.VK_ENTER) Display.chatbox.requestFocus();
        else if (mode == KeyEvent.VK_TAB) Display.menu.toggle();
        else if (mode == 0) inputDirect(null,x,y);
        else if (keyMap.containsKey(mode)){
            inputDirect(keyMap.get(mode),x,y);
        } else if (mode >= 49 && mode < 55) {
            inputDirect("I"+(mode-49),x,y);
        } else inputDirect("O"+mode+"", x, y);
        //Global.server.sendFromLocal("IN>"+p + "@" + tx + "@" + ty);
    }
    public void inputDirect(String p, int x, int y){
        host.target.set(x,y);
        if (p != null) {
            String t = p.substring(1);
            if (p.startsWith("O")) {
                if (t.equals(KeyEvent.VK_SPACE + "")) host.setState(Skills.Guard);
            } else if (p.startsWith("I")) {
                host.selectingItem = Integer.parseInt(t);
                host.setState(Actions.Wield);
            } else {
                if (t.equals("Idle")) moving = 0;
                else if (t.equals("WalkL")) {
                    moving = -1;
                    p = "AWalk";
                } else if (t.equals("Walk")) moving = 1;
                if (t.equals("Use")) {
                    if (host.state == Skills.Rest) host.setState(Skills.Slash);
                    else if (host.state == Actions.Walk) host.setState(Skills.Pierce);
                    else if (host.state == Skills.Jump && host.delay.time > 32) host.stateSet(Skills.Airstrike);
                } else if (t.equals("Jump") && host.state == Skills.Rest) host.setState(Skills.Leap);
                else if (t.equals("Rest") && host.state == Actions.Walk) host.setState(Skills.Sprint);
                Moveset move = SkillTree.getByCodeName(p);
                if (move != null) host.setState(move);
            }
        }
    }
    public void load(String p){
        if (p.length() > 1) {
            String[] data = p.substring(1).split(";");
            for (String i : data) {
                String[] pair = i.split(":");
                Moveset move = SkillTree.getByCodeName(pair[1]);
                if (host.skillTree.hasSkill(move))
                    keyMap.put(Integer.parseInt(pair[0]), move.getCodeName());
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
