package com.ga.unit;

import com.ga.game.Command;
import com.ga.game.Input;
import com.ga.map.Global;
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
    private HashMap<Integer, String> keyActionMap = new HashMap<Integer, String>(){{
        put(-1,"Idle");
    }};
    public static ArrayList<Actions> basicMoves = new ArrayList<Actions>(){{
        add(Actions.Idle);
        add(Actions.Use);
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

    public void changeMapping(int key, Actions event){
        keyActionMap.remove(key);
        Iterator i = keyActionMap.entrySet().iterator();
        int oldkey = -1;
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            if (event != Actions.Idle && e.getValue().equals(event.getName())){
                i.remove();
                oldkey = (int) e.getKey();
                break;
            }
        }
        keyActionMap.put(key, event.getName());
        if (event == Actions.Left || event == Actions.Right){
            if (oldkey != -1) keyActionMap.remove(-oldkey);
            keyActionMap.remove(-key);
            keyActionMap.put(-key, "Idle");
        }
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
        if (mode == 0) inputCoord(x, y); //mousemove for better targeting
        else {
            if (mode < 0) pressed.remove((Object)(-mode));
            else if (mode > 9){
                if (!pressed.contains(mode)) pressed.add(mode);
            }
            String action = keyActionMap.get(mode);
            if (action != null){
                inputDirect(keyActionMap.get(mode),x,y);
                if (action.startsWith("_")) pressed.remove((Object)(mode));
            }
        }
    }
    private void inputCoord(int x, int y){
        x = x-host.pos.x();
        y = y-host.pos.y();
        if (host.delay.over()) host.target.set(x,y);
    }
    public void inputDirect(String p, int x, int y){
        if (x != -1 || y != -1) inputCoord(x,y);
        if (p.startsWith("_")) {
            if (p.startsWith("_EXIT")) System.exit(1);
            else if (p.startsWith("_CTRL")) ctrl = !ctrl;
            else if (p.startsWith("_SAY")) Global.display.chatbox.requestFocus();
            else if (p.startsWith("_MENU")) Global.display.menu.showMenu();
            else if (p.startsWith("_WI")) {
                host.selectingItem = Integer.parseInt(p.substring(3));
                host.setState(Actions.Wield);
            }
        } else {
            Actions move = Actions.getByName(p);
            if (move == Actions.Wield) host.selectingItem = -1;
            if (move != null){
                if (basicMoves.contains(move) || host.moveset.contains(move)) {
                    host.setState(move);
                }
            }
        }
    }
    public void update(){
        if (pressed.size() == 0) input(-1, -1, -1);
        else {
            ArrayList<Integer> presseds = (ArrayList<Integer>) pressed.clone();
            for (Integer i : presseds) {
                input(i, -1, -1);
            }
        }
    }
    public void load(String p){
        if (p.length() > 1) {
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
        }
    }
    @Override
    public String toString(){
        if (keyActionMap.size() == 0) return "";
        String r = "";
        for (Map.Entry<Integer, String> i : keyActionMap.entrySet()){
            if (!i.getValue().equals("Idle")) r += i.getKey() + ":" + i.getValue() + ";";
        }
        return r.substring(0,r.length()-1);
    }
}
