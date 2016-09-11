package com.ga.game;

import com.ga.unit.*;
import com.ga.unit.intelligence.PlayerInput;
import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.tool.P;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Ga on 7/31/2016.
 */
public class EditorInput implements Input, Observer {
    enum Tool {
        NONE, CREATE, DELETE, REMOVE, MAN
    }
    String cunit = "Player";
    boolean control = false;
    boolean shift = false;
    boolean lmb = false;
    P lmbP = new P();
    ArrayList<Line> history = new ArrayList<>();
    Line current = new Line(0, 0, 0, 0);
    Thing currentU;
    boolean connected = false;
    Tool tool = Tool.NONE;
    P v = new P();
    int speed = 50;
    String map = "Map";
    boolean placingTp = false;

    public EditorInput() {
        Display.input = this;
        Updater.editor = true;
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
            placingTp = true;
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
            FileManager.saveMap("map/"+p.substring(5));
        } else if (p.startsWith("load")){
            Updater.changeMap = p.substring(5);
            map = Updater.changeMap;
            Updater.changeMap(true);
            Display.input = this;
            Updater.other.add(this);
            Global.camera = new P(Global.camera);
        } else {
            try {
                cunit = p;
                currentU = makeUnit(cunit, currentU.pos);
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
        if (shift && current != null){
            boolean snap = false;
            for (Line i : Global.map.land){
                if (i != current) {
                    if (Math.abs(i.pt.x - x) < 30 && Math.abs(i.pt.y - y) < 30) {
                        x = (int) i.pt.x;
                        y = (int) i.pt.y;
                        snap = true;
                        break;
                    } else if (Math.abs(i.ptd.x - x) < 30 && Math.abs(i.ptd.y - y) < 30) {
                        x = (int) i.ptd.x;
                        y = (int) i.ptd.y;
                        snap = true;
                        break;
                    }
                }
            }
            if (!snap) y = (int) current.pt.y;
        }
        switch (mode) {
            case 0:
                if (tool == Tool.CREATE) {
                    if (connected) {
                        current.ptd.set(x, y);
                    }
                } else if (tool == Tool.MAN) {
                    currentU.pos.set(x, y);
                } else if (tool == Tool.REMOVE && lmb){
                    Iterator it = Updater.updatee.iterator();
                    Matter i;
                    while (it.hasNext()){
                        i = (Matter) it.next();
                        String name = i.name;
                        if (800 - (Math.pow(x - i.pos.x, 2) + Math.pow(y - i.pos.y, 2)) > 0){
                            try{i.remove();}catch(Exception e){Global.p("Error deleting "+name);}
                            Global.p("Deleted "+name);
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
                    if (placingTp){
                        placingTp = false;
                        current.opt = -1;
                    }
                    add(current);
                    connected = true;
                } else if (tool == Tool.MAN) {
                    currentU = makeUnit(cunit, new P(x, y));
                }
                break;
            case -1:
                if (tool == Tool.DELETE && lmb){
                    Iterator it = Global.map.land.iterator();
                    Line i;
                    while (it.hasNext()){
                        i = (Line) it.next();
                        if (i.lineCross(new Line(new P(x,y),lmbP))){
                            it.remove();
                        }
                    }
                } else if (tool == Tool.NONE && lmb){
                    Iterator it = Global.map.land.iterator();
                    Line i;
                    while (it.hasNext()){
                        i = (Line) it.next();
                        if (i.lineCross(new Line(new P(x,y),lmbP))){
                            Global.p(i.toString());
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
                currentU = makeUnit(cunit, new P(x, y));
                break;
            case KeyEvent.VK_W:
                setSpeed(0,-1);
                break;
            case KeyEvent.VK_S:
                setSpeed(0,1);
                if (control) {
                    Global.p("Saved");
                    FileManager.saveMap("map/"+map);
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
                    Global.player = new PlayerInput(new Player(new P(Global.camera),0));
                    Global.player.host.save.time = -1;
                    Display.input = Global.player;
                    Global.camera = Global.player.host.pos;
                }
                break;
        }
    }
    private Matter makeUnit(String name, P p){
        Matter r = new Matter("unit/"+name,-1,p,9);
        r.name = name;
        return r;
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
        if (current != null && connected) remove(current);
        if (currentU != null) currentU.remove();
    }

    public void update() {
        Global.camera.add(v);
        Global.mapEdge(Global.camera);
    }
}
