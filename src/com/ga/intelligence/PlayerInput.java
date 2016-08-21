package com.ga.intelligence;

import com.ga.game.Command;
import com.ga.game.Input;
import com.ga.game.Observer;
import com.ga.game.Settings;
import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.unit.Actions;
import com.ga.unit.Player;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ga on 7/25/2016.
 */
public class PlayerInput implements Input, Observer {
    private static HashMap<Integer, Actions> keyMap = new HashMap<Integer, Actions>(){{
        put(-1, Actions.IDLE);
        put(KeyEvent.VK_W, Actions.ENTER);
        put(KeyEvent.VK_A, Actions.WALKL);
        put(KeyEvent.VK_S, Actions.DUCK);
        put(KeyEvent.VK_D, Actions.WALKR);
        put(KeyEvent.VK_SPACE, Actions.JUMP);
        put(KeyEvent.VK_C, Actions.SUMMON);
        put(KeyEvent.VK_F, Actions.KICK);
        put(KeyEvent.VK_E, Actions.SKILL);
        put(KeyEvent.VK_Q, Actions.SKILL1);
        put(KeyEvent.VK_R, Actions.SKILL2);
        put(KeyEvent.VK_G, Actions.DROP);
        put(1, Actions.ATTACK);
        put(3, Actions.SWING);
        put(KeyEvent.VK_SHIFT, Actions.WHIRLWIND);
    }};
    private ArrayList<Integer> keyPressed = new ArrayList<>();
    public Player host;
    public P mP = new P();
    public float mR;
    public PlayerInput(Player unit){
        host = unit;
        host.faction = 1;
        unit.brain = null;
        Global.camera = unit.pos;
        Global.player = this;
    }
    public void chat(String p){
        Command.speechOrder(p);
        host.say(p);
    }
    public void input(boolean key, int mode, int x, int y){
//        Global.p(mode+"");
        if (!key) {
            mP.x = x - Settings.centerX;
            mP.y = y - Settings.centerY;
            mR = (float) Math.atan2(mP.y,mP.x);
        }
        if (keyMap.containsKey(mode))
            if (mode > 0 && keyPressed.indexOf(mode) == -1) keyPressed.add(mode);
        if (mode < 0)
            keyPressed.remove((Object) (-mode));
        if (mode > 48 && mode < 57) host.useItem(mode-49);
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
                if (keyMap.get(i) == Actions.ENTER) return true;
            }
        return false;
    }
}
