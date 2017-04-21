package com.ga.map;

import com.ga.game.FileManager;
import com.ga.game.Input;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.player.Player;
import com.ga.unit.Prop;
import com.ga.unit.Stock;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;

public class EditorInput implements Input, Updatable, Drawable {
    private enum Tool {
        NONE, CREATE, DELETE, REMOVE, OBJECT
    }
    private enum CreateTool {
        LINE, RECTANGLE
    }
    private final int SNAP = 48;
    private String cunit = "Player";
    private String texture = null;
    private String auxTexture = null;
    private boolean control = false;
    private boolean shift = false;
    private boolean lmb = false;
    private P lmbP = new P();
    private ArrayList<Line> history = new ArrayList<>();
    private ArrayList<Line> lineConnection = new ArrayList<>();
    private Line current = new Line(0, 0, 0, 0);
    private Prop currentU = new Prop();
    private boolean connected = false;
    private Tool tool = Tool.NONE;
    private CreateTool subtool = CreateTool.LINE;
    private P v = new P();
    private int speed = 50;
    private String map;
    private boolean placingTp = false;
    private boolean reverse = false;
    private int nextPolygon = 0;
    private Color landColor = Color.pink;
    private boolean polygonReady = false;

    public EditorInput() {
        Global.display.input = this;
        Updater.addUpdatee(this);
        Updater.add(this);
        if (Global.camera == null) Global.camera = new P();
        else Global.camera = new P(Global.camera);
        scanPolygon();
        Global.margin = -36;
        map = Global.player.cmap;
    }

    private void scanPolygon(){
        int high = -1;
        for (Line i : Global.map.land){
            if (i.next > high){
                high = i.next;
                nextPolygon = i.next+1;
            }
        }
    }

    private void add(Line p) {
        Global.map.land.add(p);
        history.add(p);
    }

    private void remove(Line p) {
        Global.map.land.remove(p);
        Global.map.formTextures.put(p.next, null);
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
        return 999;
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
            Global.map.size.set(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
        } else if (p.equals("reverse")){
            reverse = true;
        } else if (p.equals("unreverse")){
            reverse = false;
        } else if (p.equals("green")){
            landColor = Color.green;
        } else if (p.equals("pink")){
            map = Updater.changeMap;
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
        } else if (p.startsWith("pp")) {
            auxTexture = p.substring(2);
        } else if (p.startsWith("tx")) {
            texture = p.substring(2);
        } else if (p.equals("render")){
            Updater.unrender();
            Global.map.setTexture();
        } else if (p.startsWith("save")){
            save("map/"+p.substring(5));
        } else if (p.startsWith("load")){
            Updater.changeMap = p.substring(5)+"@0";
            Updater.changeMap(true);
            Global.display.input = this;
            Updater.add(this);
            Global.camera = new P(Global.camera);
            map = p.substring(5);
            scanPolygon();
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

    private void reverse(int p){
        for (int i = 0; i < Global.map.land.size(); i++) {
            Line l = Global.map.land.get(i);
            if (l.next == p){
                P t = l.pt;
                l.pt = l.ptd;
                l.ptd = t;
            }
        }
    }

    private void makePolygon(P orig, Line p){
        for (int i = 0; i < Global.map.land.size(); i++) {
            Line l = Global.map.land.get(i);
            if (p.ptd.x == l.pt.x && p.ptd.y == l.pt.y){
                lineConnection.add(p);
                Global.map.formTextures.remove(l.next);
                p.next = nextPolygon;
                Global.map.land.remove(i);
                if (l.pt != orig) makePolygon(orig, l);
                else{
                    Global.map.land.addAll(lineConnection);
                    lineConnection.clear();
                }
                break;
            }
        }
        scanPolygon();
    }

    private void removeTerrain(int next) {
        Iterator it = Global.map.land.iterator();
        Line i;
        while (it.hasNext()) {
            i = (Line) it.next();
            if (i.next == next) {
                it.remove();
            }
        }
        Global.map.formTextures.remove(next);
    }

    @Override
    public void input(int mode, int x, int y) {
        polygonReady = false;
        if (mode < 0) v.set(0, 0);
        if (tool == Tool.CREATE) {
            x = ((x + SNAP / 2) / SNAP) * SNAP;
            y = ((y + SNAP / 2) / SNAP) * SNAP;
            if (current != null) {
                if (shift) {
                    boolean snap = false;
                    for (int i = 0; i < Global.map.land.size(); i++) {
                        Line l = Global.map.land.get(i);
                        if (l != current) {
                            final int snapThreshold = 24;
                            if (Math.abs(l.pt.x - x) < snapThreshold && Math.abs(l.pt.y - y) < snapThreshold) {
                                x = (int) l.pt.x;
                                y = (int) l.pt.y;
                                snap = true;
                                polygonReady = true;
                                break;
                            } else if (Math.abs(l.ptd.x - x) < snapThreshold && Math.abs(l.ptd.y - y) < snapThreshold) {
                                x = (int) l.ptd.x;
                                y = (int) l.ptd.y;
                                snap = true;
                                break;
                            }
                        }
                    }
                    if (!snap) {
                        if (Math.abs(current.getSlope()) <= 1.5f) y = (int) current.pt.y;
                        else x = (int) current.pt.x;
                    }
                } else if (current.opt == 1) {
                    x = (int) current.pt.x;
                }
            }
        }
        switch (mode) {
            case 0:
                if (tool == Tool.CREATE) {
                    if (connected) {
                        current.ptd.set(x, y);
                    }
                } else if (tool == Tool.OBJECT) {
                    currentU.pos.set(x, y);
                } else if (tool == Tool.REMOVE && lmb){
                    ArrayList<Prop> Props = Updater.collideProp(new P(x,y),100);
                    for (Prop i : Props) {
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
                        if (subtool == CreateTool.LINE) {
                            current.ptd = new P(x, y);
                            if (polygonReady) {
                                polygonReady = false;
                                makePolygon(current.pt, current);
                            }
                        } else if (subtool == CreateTool.RECTANGLE){
                            subtool = CreateTool.LINE;
                            Line t = new Line(current.pt.x, current.pt.y, x, current.pt.y);
                            Global.map.land.add(t);
                            Global.map.land.add(new Line(x, current.pt.y, x, y));
                            Global.map.land.add(new Line(x, y, current.pt.x, y));
                            Global.map.land.add(new Line(current.pt.x, y, current.pt.x, current.pt.y));
                            Global.map.land.remove(current);
                            makePolygon(t.pt, t);
                            break;
                        }
                    }
                    current = new Line(x, y, x, y);
                    if (placingTp){
                        placingTp = false;
                        current.opt = -1;
                    }
                    add(current);
                    connected = true;
                } else if (tool == Tool.OBJECT) {
                    currentU = make(cunit, new P(x, y));
                }
                break;
            case -1:
                if (tool == Tool.DELETE && lmb){
                    if (shift){
                        Iterator it = Global.map.land.iterator();
                        Line i;
                        int n = -1;
                        while (it.hasNext()) {
                            i = (Line) it.next();
                            if (i.lineCross(new Line(new P(x, y), lmbP))) {
                                n = i.next;
                            }
                        }
                        if (n != -1) removeTerrain(n);
                    } else {
                        Iterator it = Global.map.land.iterator();
                        Line i;
                        while (it.hasNext()) {
                            i = (Line) it.next();
                            if (i.lineCross(new Line(new P(x, y), lmbP))) {
                                Global.map.formTextures.remove(i.next);
                                it.remove();
                            }
                        }
                    }
                } else if (tool == Tool.NONE && lmb){
                    Iterator it = Global.map.land.iterator();
                    Line i;
                    while (it.hasNext()){
                        i = (Line) it.next();
                        if (i.lineCross(new Line(new P(x,y),lmbP))){
                            if (auxTexture != null){
                                String[] aux = auxTexture.split("@");
                                for (int ii=0;ii<Integer.parseInt(aux[0]);ii++) Global.map.placeProp(i, aux[1]);
                                auxTexture = null;
                            } else if (texture != null) {
                                if (texture.equals("copy")) texture = Global.map.formTextures.get(i.next);
                                else if (i.next != -1){
                                    Global.map.formTextures.put(i.next, texture);
                                    texture = null;
                                }
                            }
                            if (reverse){
                                reverse(i.next);
                                reverse = false;
                            }
                            Global.p(i.toString()+" slope:"+i.getSlope()+" "+(i.clockwise()?"cw":"cc")+" "+Global.map.formTextures.get(i.next));
                        }
                    }
                }
                lmb = false;
                break;
            case KeyEvent.VK_R:
                subtool = CreateTool.RECTANGLE;
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
                Global.display.chatbox.requestFocus();
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
                if (!shift) selectTool(Tool.OBJECT);
                currentU.remove();
                currentU = make(cunit, new P(x, y));
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
                    Global.display.input = Global.player.input;
                    Global.player.pos.set(Global.camera);
                }
                break;
        }
    }
    private Prop make(String name, P p){
        int seed = -1;
        if (name.contains(":")){
            seed = Integer.parseInt(name.split(":")[1]);
            name = name.substring(0,name.indexOf(":"));
        }
        Prop r = Stock.getByName(name, seed);
        if (name.equals("Player")) r = new Player(0);
        r.pos = p;
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
