package com.ga.unit;

import com.ga.game.Command;
import com.ga.game.Input;
import com.ga.game.Menu;
import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.unit.man.Player;
import com.ga.unit.skill.Actions;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Ga on 7/25/2016. asd
 */
public class PlayerInput implements Input {
    private HashMap<Integer, String> keyActionMap = new HashMap<>();
    public static ArrayList<Actions> basicMoves = new ArrayList<>() {{
        add(Actions.Use);
        add(Actions.Idle);
        add(Actions.Left);
        add(Actions.Right);
        add(Actions.Rest);
        add(Actions.Jump);
        add(Actions.Throw);
        add(Actions.Wield);
        add(Actions.Drop);
    }};
    private ArrayList<Integer> pressed = new ArrayList<>();
    public Player host;
    public boolean ctrl = false;
    public int selectingItem = -1;
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

    @Override
    public void onFocus(boolean focus) {
        if (!focus) pressed.clear();
    }

    @Override
    public String getKeyMap(int p) {
        return keyActionMap.get(p);
    }

    public void changeMapping(int key, String event){
        keyActionMap.remove(key);
        Iterator i = keyActionMap.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            if (e.getValue().equals(event)){
                i.remove();
                break;
            }
        }
        keyActionMap.put(key, event);
    }
    public void changeMapping(int key, Actions event){
        keyActionMap.remove(key);
        Iterator i = keyActionMap.entrySet().iterator();
        int oldkey = -1;
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            if (event != Actions.Idle && e.getValue().equals(event.name())){
                i.remove();
                oldkey = (int) e.getKey();
                break;
            }
        }
        keyActionMap.put(key, event.name());
        if (event == Actions.Left || event == Actions.Right){
            if (oldkey != -1) keyActionMap.remove(-oldkey);
            keyActionMap.remove(-key);
            keyActionMap.put(-key, "Idle");
        }
    }
    public String getMapping(String name){
        String r = null;
        for (Map.Entry<Integer, String> i : keyActionMap.entrySet()) {
            if (i.getValue().equals(name)){
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
        if (mode == 0) inputCoord(x, y); //mousemove for better targeting
        else {
            if (mode < 0) {
                pressed.remove((Object)(-mode));
                if (mode == -1) inputDirect("Idle", x, y);
            } else if (mode > 9){
                if (!pressed.contains(mode)) pressed.add(mode);
            }
            String action = keyActionMap.get(mode);
            if (action != null){
                inputDirect(action, x, y);
                if (action.startsWith("_")) pressed.remove((Object)(mode));
            }
        }
    }
    private void inputCoord(int x, int y){
        P hand = host.getHand();
        x = x-hand.x();
        y = y-hand.y();
        if (host.delay.over()) host.target.set(x,y);
    }
    public void inputDirect(String p, int x, int y){
        if (x != -1 || y != -1) inputCoord(x,y);
        if (p.startsWith("_")) {
            if (p.startsWith("_EXIT")) System.exit(1);
            else if (p.startsWith("_CTRL")) ctrl = !ctrl;
            else if (p.startsWith("_SAY")) Global.display.startChat();
            else if (p.startsWith("_MENU")) Menu.menu.showMenu();
            else if (p.startsWith("_WI")) {
                selectingItem = Integer.parseInt(p.substring(3));
                host.setState(Actions.Wield);
            }
        } else {
            Actions move = Actions.getByName(p);
            if (move == Actions.Wield) selectingItem = -1;
            if (move != null){
                if (basicMoves.contains(move) || host.moveset.contains(move)) {
                    host.setState(move);
                }
            }
        }
    }
    public void update(){
        int length = pressed.size();
        if (length == 0) input(-1, -1, -1);
        else {
            for (int i=0;i<length;i++) {
                input(pressed.get(i), -1, -1);
                length = pressed.size(); //Removal adjust
            }
        }
    }
    public void load(String p){
        if (!p.isEmpty()) {
            String[] data = p.split(";",-1);
            for (String i : data) {
                String[] pair = i.split(":");
                if (pair[1].startsWith("_")){
                    keyActionMap.put(Integer.parseInt(pair[0]), pair[1]);
                } else {
                    Actions move = Actions.getByName(pair[1]);
                    if (move != null) changeMapping(Integer.parseInt(pair[0]), move);
                }
            }
        } else { //reset keymap
            keyActionMap.put(32, Actions.Use.name());
            keyActionMap.put(65, Actions.Left.name());
            keyActionMap.put(68, Actions.Right.name());
            keyActionMap.put(16, Actions.Rest.name());
            keyActionMap.put(87, Actions.Jump.name());
            keyActionMap.put(83, Actions.Drop.name());
            keyActionMap.put(81, Actions.Throw.name());
            keyActionMap.put(82, Actions.Wield.name());
            keyActionMap.put(17, "_CTRL");
            keyActionMap.put(10, "_SAY");
            keyActionMap.put(9, "_MENU");
            keyActionMap.put(38, "_UP");
        }
    }
    @Override
    public String toString(){
        if (keyActionMap.size() == 0) return "";
        String r = "";
        for (Map.Entry<Integer, String> i : keyActionMap.entrySet()){
            r += i.getKey() + ":" + i.getValue() + ";";
        }
        return r.substring(0,r.length()-1);
    }
}
