package com.ga.game;

import com.ga.map.*;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

class EditorInput implements Input, Updatable, Drawable {
    private enum Tool {
        NONE, CREATE, DELETE, REMOVE, MAN, UNIT, OBJECT
    }
    private String cunit = "Player";
    private String texture = "Dirt";
    private boolean control = false;
    private boolean shift = false;
    private boolean lmb = false;
    private P lmbP = new P();
    private ArrayList<Line> history = new ArrayList<>();
    private HashMap<Line, Integer> lineConnection = new HashMap<>();
    private Line current = new Line(0, 0, 0, 0);
    private Thing currentU = new Thing(null);
    private boolean connected = false;
    private Tool tool = Tool.NONE;
    private P v = new P();
    private int speed = 50;
    private String map = "Map";
    private boolean placingTp = false;
    private int nextPolygon = 0;
    private Color landColor = Color.pink;

    protected EditorInput() {
        Display.input = this;
        Updater.add(this);
        if (Global.camera == null) Global.camera = new P();
        else Global.camera = new P(Global.camera);
        scanPolygon();
        Global.margin = -36;
        new Line(0,36,Global.map.size.x,36);
    }

    private void scanPolygon(){
        for (Line i : Global.map.land){
            if (i.next != -1) nextPolygon = i.next+1;
        }
    }

    private void add(Line p) {
        Global.map.land.add(p);
        history.add(p);
    }

    private void remove(Line p) {
        Global.map.land.remove(p);
        history.remove(p);
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(landColor);
        g.setStroke(new BasicStroke(10));
        for (Line i : Global.map.land) i.draw(g,new P());
    }

    @Override
    public int getDepth() {
        return 99;
    }

    @Override
    public void chat(String p, int x, int y){
        if (p.equals("rope")) {
            current.opt = 1;
        } else if (p.equals("tp")) {
            Global.p("Teleporter");
            current.opt = 2;
            placingTp = true;
        } else if (p.startsWith("tp")) { //100 = force, 200 = input required, #0 = mapnum, # = posnum
            current.opt = Integer.parseInt(p.substring(2));
            Global.p("Level Teleporter: "+current.opt);
        } else if (p.startsWith("size")){
            String[] size = p.substring(5).split("x");
            Global.map.map.image.size.set(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
        } else if (p.equals("green")){
            landColor = Color.green;
        } else if (p.equals("pink")){
            landColor = Color.pink;
        } else if (p.equals("white")){
            landColor = Color.white;
        } else if (p.equals("black")) {
            landColor = Color.black;
        } else if (p.equals("invisible")) {
            landColor = new Color(0,0,0,0);
        } else if (p.equals("margin")) {
            if (Global.margin == 0) Global.margin = -36;
            else Global.margin = 0;
        } else if (p.startsWith("tx")) {
            texture = p.substring(2);
        } else if (p.equals("render")){
            Global.map.setTexture();
        } else if (p.equals("shader")){
            Global.map.setShader();
        } else if (p.startsWith("save")){
            save("map/"+p.substring(5));
        } else if (p.startsWith("load")){
            Updater.changeMap = p.substring(5);
            map = Updater.changeMap;
            Updater.changeMap(true);
            Display.input = this;
            Updater.add(this);
            Global.camera = new P(Global.camera);
        } else {
            try {
                cunit = p;
                currentU.remove();
                selectTool(tool);
            } catch (Exception e) {
                Global.p(p + " Not found");
            }
        }
    }

    private void makePolygon(Line p){
        Line i = p;
        ArrayList<Line> result = new ArrayList<>();
        while (lineConnection.get(i) != null){
            result.add(i);
            i = Global.map.land.get(lineConnection.get(i));
            if (i == p) {
                for (Line ii : result){
                    ii.next = nextPolygon;
                    lineConnection.remove(ii);
                }
                nextPolygon++;
                return;
            }
        }
    }

    @Override
    public void input(int mode, int x, int y) {
        if (mode < 0) v.set(0, 0);
        if (current != null){
            if (shift) {
                boolean snap = false;
                for (int i = 0; i < Global.map.land.size(); i++) {
                    Line l = Global.map.land.get(i);
                    if (l != current) {
                        final int snapThreshold = 24;
                        if (Math.abs(l.pt.x - x) < snapThreshold && Math.abs(l.pt.y - y) < snapThreshold) {
                            x = (int) l.pt.x;
                            y = (int) l.pt.y;
                            lineConnection.put(current, i);
                            snap = true;
                            if (connected) makePolygon(current);
                            break;
                        } else if (Math.abs(l.ptd.x - x) < snapThreshold && Math.abs(l.ptd.y - y) < snapThreshold) {
                            x = (int) l.ptd.x;
                            y = (int) l.ptd.y;
                            lineConnection.put(current, i);
                            snap = true;
                            break;
                        }
                    }
                }
                if (!snap){
                    if (Math.abs(current.getSlope()) <= 1.5f) y = (int) current.pt.y;
                    else x = (int) current.pt.x;
                }
            } else if (current.opt == 1){
                x = (int) current.pt.x;
            }
        }
        switch (mode) {
            case 0:
                if (tool == Tool.CREATE) {
                    if (connected) {
                        current.ptd.set(x, y);
                    }
                } else if (tool == Tool.MAN || tool == Tool.UNIT || tool == Tool.OBJECT) {
                    currentU.pos.set(x, y);
                } else if (tool == Tool.REMOVE && lmb){
                    ArrayList<Matter> matters = Updater.collide(new P(x,y),100);
                    for (Matter i : matters) {
                        String name = i.name;
                        try{i.remove();}catch(Exception e){Global.p("Error deleting "+name);}
                        Global.p("Deleted "+name);
                    }
                }
                break;
            case 1:
                lmb = true;
                lmbP = new P(x,y);
                if (tool == Tool.CREATE) {
                    if (connected) {
                        current.ptd = new P(x, y);
                        lineConnection.put(Global.map.land.get(Global.map.land.size()-1), Global.map.land.size());
                    }
                    current = new Line(x, y, x, y);
                    if (placingTp){
                        placingTp = false;
                        current.opt = -1;
                    }
                    add(current);
                    connected = true;
                } else if (tool == Tool.MAN) {
                    currentU = makeMan(cunit, new P(x, y));
                } else if (tool == Tool.UNIT) {
                    currentU = makeUnit(cunit, new P(x, y));
                } else if (tool == Tool.OBJECT) {
                    currentU = makeObject(cunit, new P(x, y));
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
                            Global.p(i.toString()+" slope:"+i.getSlope()+" "+(i.clockwise()?"clockwise":"counterclockwise"));
                            if (!texture.equals("") && i.next != -1) Global.map.formTextures.put(i.next, texture);
                        }
                    }
                }
                lmb = false;
                break;
            case KeyEvent.VK_Z:
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
            case KeyEvent.VK_ENTER:
                Display.chatbox.requestFocus();
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
                currentU.remove();
                currentU = makeMan(cunit, new P(x, y));
                break;
            case KeyEvent.VK_6:
                if (!shift) selectTool(Tool.UNIT);
                currentU.remove();
                currentU = makeUnit(cunit, new P(x, y));
                break;
            case KeyEvent.VK_7:
                if (!shift) selectTool(Tool.OBJECT);
                currentU.remove();
                currentU = makeObject(cunit, new P(x, y));
                break;
            case KeyEvent.VK_W:
                setSpeed(0,-1);
                break;
            case KeyEvent.VK_S:
                setSpeed(0,1);
                if (control) {
                    save("map/"+map);
                }
                break;
            case KeyEvent.VK_A:
                setSpeed(-1,0);
                break;
            case KeyEvent.VK_D:
                setSpeed(1,0);
                break;
            case KeyEvent.VK_UP:
                currentU.pos.add(0,-1);
                break;
            case KeyEvent.VK_DOWN:
                currentU.pos.add(0,1);
                break;
            case KeyEvent.VK_LEFT:
                currentU.pos.add(-1,0);
                break;
            case KeyEvent.VK_RIGHT:
                currentU.pos.add(1,0);
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
                    FileManager.loadPlayer(true, "0");
                    Display.input = Global.player;
                    Global.player.host.pos.set(Global.camera);
                }
                break;
        }
    }
    private Matter makeObject(String name, P p){
        Matter r;
        int seed = 10*Global.map.magnify;
        if (name.contains(":")){
            seed = Integer.parseInt(name.split(":")[1]);
            name = name.substring(0,name.indexOf(":"));
        }
        r = ObjectFactory.produce(name, p, seed);
        return r;
    }
    private Matter makeUnit(String name, P p){
        Unit r;
        r = BeastFactory.produce(name, p, -1);
        return r;
    }
    private Matter makeMan(String name, P p){
        Unit r;
        r = ManFactory.produce(name, p, -1);
        if (name.equals("Player")) r = new Player(r.toString().substring(4));
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
        if (current != null && connected) remove(current);
        if (currentU != null) currentU.remove();
        connected = false;
    }

    public void update() {
        Global.cameraTo.add(v);
        Global.mapEdgeCenter(Global.cameraTo);
    }

    private void save(String p){
        Global.p("Saved ("+p+")");
        currentU.remove();
        FileManager.saveMap(p);
    }
}
