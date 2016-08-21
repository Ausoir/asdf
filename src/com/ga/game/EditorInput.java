package com.ga.game;

import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Man;
import com.ga.unit.Player;
import com.ga.unit.Updater;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Ga on 7/31/2016.
 */
public class EditorInput implements Input, Observer {
    enum Tool {
        NONE, CREATE, PLAYER, DELETE, REMOVE, MAN
    }
    String map = "Map";
    String cunit = "Player";
    boolean control = false;
    boolean shift = false;
    boolean lmb = false;
    P lmbP = new P();
    ArrayList<Line> history = new ArrayList<>();
    Line current = new Line(0, 0, 0, 0);
    Man currentU;
    boolean connected = false;
    Tool tool = Tool.NONE;
    P v = new P();
    int speed = 50;

    public EditorInput() {
        Updater.other.add(this);
        if (Global.camera == null) Global.camera = new P();
        else Global.camera = new P(Global.camera);
    }

    private void add(Line p) {
        Global.map.land.add(p);
        history.add(p);
    }

    private void remove(Line p) {
        Global.map.land.remove(p);
        Updater.drawee.remove(p);
        history.remove(p);
    }

    @Override
    public void chat(String p){
        if (p.equals("c")) {
            Global.p("Ceiling");
            current.opt = 1;
        } else if (p.equals("tp")) {
            Global.p("Teleporter");
            current.opt = 2;
        } else if (p.startsWith("tp")) {
            current.opt = Integer.parseInt(p.substring(2));
            Global.p("Level Teleporter: "+current.opt);
        } else if (p.equals("green")){
            Updater.landColor = Color.green;
        } else if (p.equals("pink")){
            Updater.landColor = Color.pink;
        } else if (p.equals("white")){
            Updater.landColor = Color.white;
        } else if (p.equals("black")) {
            Updater.landColor = Color.black;
        } else if (p.startsWith("save")){
            save(p.substring(5));
        } else if (p.startsWith("load")){
            Updater.changeMap = p.substring(5);
            map = Updater.changeMap;
            Updater.changeMap();
            Display.input = this;
            Updater.other.add(this);
            Global.camera = new P(Global.camera);
        } else {
            try {
                cunit = p;
                currentU = new Man(cunit, currentU.pos);
            } catch (Exception e) {
                Global.p(p + " Not found");
            }
        }
    }

    @Override
    public void input(boolean key, int mode, int x, int y) {
        if (!key) {
            x = (int) (x - Settings.centerX + Global.camera.x);
            y = (int) (y - Settings.centerY + Global.camera.y);
        }
        if (mode < -3) v.set(0, 0);
        if (shift) y = (int) current.pt.y;
        switch (mode) {
            case 0:
                if (tool == Tool.CREATE) {
                    if (connected) {
                        current.ptd.set(x, y);
                    }
                } else if (tool == Tool.MAN || tool == Tool.PLAYER) {
                    currentU.pos.set(x, y);
                } else if (tool == Tool.REMOVE && lmb){
                    Iterator it = Updater.updatee.iterator();
                    Man i;
                    while (it.hasNext()){
                        i = (Man) it.next();
                        if (800 - (Math.pow(x - i.pos.x, 2) + Math.pow(y - i.pos.y, 2)) > 0){
                            try{i.remove();}catch(Exception e){}
                        }
                    }
                }
                break;
            case 1:
                lmb = true;
                lmbP = new P(x,y);
                if (tool == Tool.CREATE) {
                    if (connected) {
                        current.ptd = new P(x, y);
                        Global.p(current.toString());
                    }
                    current = new Line(x, y, x, y);
                    add(current);
                    connected = true;
                } else if (tool == Tool.MAN) {
                    currentU = new Man(cunit, new P(x, y));
                } else if (tool == Tool.PLAYER){
                    currentU = new Player(new P(x, y));
                }
                break;
            case -1:
                if (tool == Tool.DELETE && lmb){
                    Iterator it = Global.map.land.iterator();
                    Line i = null;
                    while (it.hasNext()){
                        i = (Line) it.next();
                        if (i.lineCross(new Line(new P(x,y),lmbP))){
                            it.remove();
                        }
                    }
                }
                lmb = false;
                break;
            case 3:
                Global.p("Undo");
                if (tool == Tool.CREATE) {
                    if (history.size() > 0) {
                        remove(history.get(history.size() - 1));
                        if (connected) current = history.get(history.size() - 1);
                        else current = null;
                        connected = false;
                    }
                }
                break;
            case KeyEvent.VK_1:
                if (!shift) selectTool(Tool.NONE);
                break;
            case KeyEvent.VK_2:
                if (!shift) selectTool(Tool.CREATE);
                break;
            case KeyEvent.VK_3:
                if (!shift) selectTool(Tool.DELETE);
                break;
            case KeyEvent.VK_4:
                if (!shift) selectTool(Tool.REMOVE);
                break;
            case KeyEvent.VK_5:
                if (!shift) selectTool(Tool.MAN);
                currentU = new Man(cunit, new P(x, y));
                break;
            case KeyEvent.VK_W:
                setSpeed(0,-1);
                break;
            case KeyEvent.VK_S:
                setSpeed(0,1);
                if (control) {
                    Global.p("Saved");
                    save(map);
                }
                break;
            case KeyEvent.VK_A:
                setSpeed(-1,0);
                break;
            case KeyEvent.VK_D:
                setSpeed(1,0);
                break;
            case KeyEvent.VK_CONTROL:
                control = true;
                break;
            case -KeyEvent.VK_CONTROL:
                control = false;
                break;
            case KeyEvent.VK_SHIFT:
                shift = true;
                break;
            case -KeyEvent.VK_SHIFT:
                shift = false;
                break;
            case KeyEvent.VK_INSERT:
                if (Updater.editor) {
                    Updater.editor = false;
                    Display.input = Global.player;
                    Global.camera = Global.player.host.pos;
                }
                break;
        }
    }
    private void setSpeed(int x, int y){
        v.x = x*speed;
        v.y = y*speed;
        if (shift){
            v.mult(10,10);
        }
    }
    private void selectTool(Tool tool) {
        this.tool = tool;
        Global.p(tool.toString());
        if (current != null) remove(current);
        if (currentU != null) currentU.remove();
    }

    public void update() {
        Global.camera.add(v);
        Global.mapEdge();
    }

    private void save(String name) {
        String result = "" + Global.map.toString();
        result += "\n";
        result += Updater.toMapString();
        FileManager.save("map/"+name+".map", result);
        Global.p(result);
    }
}
