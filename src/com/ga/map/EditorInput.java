package com.ga.map;

import com.ga.game.FileManager;
import com.ga.game.Input;
import com.ga.game.Observer;
import com.ga.game.Settings;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.AI;
import com.ga.unit.Prop;
import com.ga.unit.Stock;
import com.ga.unit.Unit;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;

public class EditorInput implements Input, Updatable, Drawable {
    private enum Tool {
        NONE, LINE, RECTANGLE, MODIFY, DELETE,
        SELECT, OBJECT, RECLAIM, REMOVE
    }
    private final int SNAP = 48;
    private final int SCROLLSPEED = 50;
    private Stroke basicStroke = new BasicStroke(10);
    private String cunit = "Slime";
    private String genProp = null;
    private String txDepth = "1";
    private String txTexture = "dirt";
    private String txShader = "grass";
    private int lastSelected = -1;
    private boolean control = false;
    private boolean shift = false;
    private boolean lmb = false;
    private P lmbP = new P();
    private ArrayList<Line> history = new ArrayList<>();
    private Line current = new Line(0, 0, 0, 0);
    private Prop currentO;
    private boolean connected = false;
    private Tool tool = Tool.NONE;
    private P v = new P();
    private String map;
    private boolean reverse = false;
    private Color landColor = Color.pink;
    private String message = "";

    public EditorInput() {
        Global.display.input = this;
        Updater.addUpdatee(this);
        Updater.addQueue.add(this);
        if (Global.camera == null) Global.camera = new P();
        else Global.camera = new P(Global.camera);
        Global.margin = -3600;
        map = Global.player.cmap;
        currentO = new Prop(null,-1);
        currentO.save = false;
        Prop prop = Stock.Spawn.create(9999);
        prop.pos.set(Global.player.pos);
        prop.save = false;
        prop.image.visible = true;
        for (int i=0;i<Updater.addQueue.size();i++){
            Observer obs = Updater.addQueue.get(i);
            if (obs instanceof AI) Updater.addQueue.remove(i);
            else if (obs instanceof Unit) ((Unit)obs).say(((Unit) obs).name);
            else if (obs instanceof Prop){
                if (((Prop)obs).factoryId == Stock.Spawn.ordinal() || ((Prop)obs).factoryId == Stock.Travel.ordinal()){
                    ((Prop)obs).image.visible = true;
                    ((Prop)obs).setDepth(999);
                }
            }
        }
        Global.map.land.addAll(Global.map.landX);
        Global.map.landX.clear();
    }

    private void remove(Line p) {
        for (Line i : Global.map.land){
            if (i.next == p.next){
                Global.map.land.remove(i);
                history.remove(i);
            }
        }
        Global.map.formTextures.put(p.next, null);
    }

    @Override
    public int getDepth() {
        return 999;
    }

    @Override
    public void chat(String p, int x, int y){
        if (p.startsWith("size")){
            String[] size = p.substring(5).split("x");
            Global.map.size.set(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
        } else if (p.equals("reverse")){
            reverse = true;
        } else if (p.equals("unreverse")){
            reverse = false;
        } else if (p.equals("pink")){
            landColor = Color.pink;
        } else if (p.equals("white")){
            landColor = Color.white;
        } else if (p.equals("black")) {
            landColor = Color.black;
        } else if (p.equals("none")) {
            landColor = new Color(0,0,0,0);
        } else if (p.equals("margin")) {
            if (Global.margin == 0) Global.margin = -3600;
            else Global.margin = 0;
        } else if (p.startsWith("spawn")) {
            int to = Integer.parseInt(p.substring(6));
            Line target = Global.map.checkLand(new Line(x,y,x,y+600));
            P pos = new P(x, target.getCrossPointY(x)+1);
            Stock.Spawn.create(to).pos.set(pos);
            message("Spawn id ["+to+"] spawned at ["+x + ", "+y+"]");
        } else if (p.startsWith("travel")) {
            int to = Integer.parseInt(p.substring(7));
            Line target = Global.map.checkLand(new Line(x,y,x,y+600));
            P pos = new P(x, target.getCrossPointY(x)+1);
            Stock.Signpost.create(to).pos.set(pos);
            message("Travel to ["+(to/100)+" @ "+(Math.abs(to)%100)+"] spawned at ["+x + ", "+y+"]");
        } else if (p.startsWith("prop")) {
            genProp = p.substring(5);
        } else if (p.startsWith("copy")) {
            String[] texs = message.split(" ")[3].split("&");
            txDepth = texs[0];
            txTexture = texs[1];
            txShader = texs[2];
        } else if (p.startsWith("depth")) {
            txDepth = p.substring(6);
            if (lastSelected != -1){
                String[] data = Global.map.formTextures.get(lastSelected).split("&");
                Global.map.formTextures.put(lastSelected, txDepth+"&"+data[1]+"&"+data[2]);
            }
            chat("show",0,0);
        } else if (p.startsWith("texture")) {
            txTexture = p.substring(8);
            if (lastSelected != -1){
                String[] data = Global.map.formTextures.get(lastSelected).split("&");
                Global.map.formTextures.put(lastSelected, data[0]+"&"+txTexture+"&"+data[2]);
            }
            chat("show",0,0);
        } else if (p.startsWith("shader")) {
            txShader = p.substring(7);
            if (lastSelected != -1){
                String[] data = Global.map.formTextures.get(lastSelected).split("&");
                Global.map.formTextures.put(lastSelected, data[0]+"&"+data[1]+"&"+txShader);
            }
            chat("show",0,0);
        } else if (p.startsWith("seedO")){
            currentO.seed = Integer.parseInt(p.substring(6));
        } else if (p.startsWith("changeO")){
            currentO.load(p.substring(8));
        } else if (p.equals("showO")){
            message(currentO.toString());
        } else if (p.equals("show")){
            message("Depth: "+txDepth+", Texture: "+txTexture+", Shader: "+txShader);
        } else if (p.equals("bg")) {
            if (lastSelected != -1){
                for (Line l : Global.map.land){
                    if (l.next == lastSelected){
                        l.opt = 1;
                        message("Intangibility Applied");
                    }
                }
            }
        } else if (p.equals("grow")){
            Global.map.generateProps();
        } else if (p.equals("gen land")){
            Generator result = new GenerateHighlands().create('-', Global.map.size);
            result.finish();
        } else if (p.equals("gen town")){
            Generator result = new GenerateTown().create(Global.map.size);
            result.finish();
        } else if (p.equals("clear")){
            Updater.clear();
            Global.map = new Map(Global.map.id+"", Global.map.size);
            Updater.addUpdatee(this);
            Updater.addQueue.add(this);
        } else if (p.equals("refresh")){
            Updater.unrender();
            Global.map.setTexture();
        } else if (p.startsWith("save")){
            save("map/"+p.substring(5));
        } else if (p.startsWith("load")){
            Updater.changeMap = p.substring(5)+"@0";
            Updater.changeMap(true);
            Global.display.input = this;
            Updater.addQueue.add(this);
            Global.camera = new P(Global.camera);
            map = p.substring(5);
            Global.map.land.addAll(Global.map.landX);
            Global.map.landX.clear();
        } else if (p.equals("exit")){
            System.exit(0);
        } else if (p.startsWith("o")){
            p = p.substring(2);
            try {
                cunit = p;
                currentO.remove();
                selectTool(tool);
            } catch (Exception e) {
                message(p + " Not found");
            }
        }
        lastSelected = -1;
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

    private String getTexture(){
        return txDepth+"&"+txTexture+"&"+txShader;
    }

    private void makePolygon(Line p){
        int high = -1;
        int nextPolygon = 0;
        for (Line i : Global.map.land){
            if (i.next > high){
                high = i.next;
                nextPolygon = i.next+1;
            }
        }
        if (p.ptd.is(history.get(0).pt)) {
            for (Line i : history) {
                i.next = nextPolygon;
            }
            history.clear();
            connected = false;
            Global.map.formTextures.put(nextPolygon, getTexture());
            Global.map.render(nextPolygon);
        }
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
        Updater.remove(Global.map.terrains.get(next));
    }

    @Override
    public void input(int mode, int x, int y) {
        if (mode == -2) v.set(0, 0);
        if (tool == Tool.LINE || tool == Tool.RECTANGLE) {
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
                        else x = current.pt.x();
                    }
                } else if (current.opt == 1) {
                    x = current.pt.x();
                }
            }
        }
        switch (mode) {
            case 0:
                if (tool == Tool.LINE || tool == Tool.RECTANGLE) {
                    if (connected) current.ptd.set(x, y);
                } else if (tool == Tool.OBJECT) {
                    currentO.pos.set(x, y);
                } else if (tool == Tool.REMOVE && lmb){
                    ArrayList<Prop> props = Updater.collideProp(new P(x,y),40);
                    for (Prop i : props) {
                        String name = i.name;
                        try{i.remove();}catch(Exception e){message("Error deleting "+name);}
                        message("Deleted "+name);
                    }
                }
                break;
            case 1:
                lmb = true;
                lmbP = new P(x,y);
                if (tool == Tool.LINE) {
                    if (connected) {
                        current.ptd = new P(x, y);
                        makePolygon(current);
                    }
                } else if (tool == Tool.RECTANGLE){
                    if (connected) {
                        Global.map.land.remove(current);
                        history.remove(current);
                        Line[] lines = new Line[]{new Line(current.pt.x, current.pt.y, x, current.pt.y),
                                new Line(x, current.pt.y, x, y),
                                new Line(x, y, current.pt.x, y),
                                new Line(current.pt.x, y, current.pt.x, current.pt.y)};
                        for (Line l : lines) {
                            Global.map.land.add(l);
                            history.add(l);
                        }
                        makePolygon(lines[3]);
                        break;
                    }
                } else if (tool == Tool.OBJECT) {
                    currentO = make(cunit, new P(x, y));
                } else if (tool == Tool.SELECT) {
                    final ArrayList<Prop> list = Updater.collideProp(new P(x, y), 16);
                    final String[] listString = new String[list.size()];
                    Performer[] performers = new Performer[listString.length];
                    for (int i=0;i<listString.length;i++){
                        listString[i] = i+" - "+list.get(i).name;
                        final int finalI = i;
                        performers[i] = new Performer(){
                            public void perform(){
                                selectTool(Tool.OBJECT);
                                currentO.remove();
                                currentO = list.get(finalI);
                                message("["+listString[finalI] + "] was selected");
                            }
                        };
                    }
                    Global.display.menu.showDialog("List of Props","", listString, performers);
                }
                break;
            case -1:
                if (tool == Tool.DELETE){
                    for (int i=0;i<Global.map.land.size();i++){
                        Line l = Global.map.land.get(i);
                        if (l.lineCross(new Line(new P(x, y), lmbP))) {
                            removeTerrain(l.next);
                        }
                    }
                } else if (tool == Tool.MODIFY){
                    for (int i=0;i<Global.map.land.size();i++){
                        Line l = Global.map.land.get(i);
                        if (l.lineCross(new Line(new P(x, y), lmbP))) {
                            Updater.remove(Global.map.terrains.get(l.next));
                            Global.map.formTextures.put(l.next, getTexture());
                            Global.map.render(l.next);
                            message(l.toString()+" slope:"+l.getSlope()+" "+(l.clockwise()?"cw":"cc")+" "+Global.map.formTextures.get(l.next));
                            break;
                        }
                    }
                } else if (tool == Tool.NONE){
                    Iterator it = Global.map.land.iterator();
                    Line i;
                    while (it.hasNext()){
                        i = (Line) it.next();
                        if (i.lineCross(new Line(new P(x,y),lmbP))){
                            if (genProp != null){
                                String[] aux = genProp.split("@");
                                for (int ii=0;ii<Integer.parseInt(aux[0]);ii++) Global.map.placeProp(i, aux[1], -1);
                                genProp = null;
                            }
                            if (reverse){
                                reverse(i.next);
                                reverse = false;
                            }
                            message(i.toString()+" slope:"+i.getSlope()+" "+(i.clockwise()?"cw":"cc")+" "+Global.map.formTextures.get(i.next));
                            lastSelected = i.next;
                        }
                    }
                }
                lmb = false;
                break;
            case KeyEvent.VK_Z:
                message("Undo");
                if (history.size() > 0) {
                    remove(history.get(history.size() - 1));
                    if (connected) current = history.get(history.size() - 1);
                    else current = null;
                    connected = false;
                }
                break;
            case KeyEvent.VK_ENTER:
                Global.display.chatbox.requestFocus();
                break;
            case KeyEvent.VK_1:
                if (!shift) selectTool(Tool.NONE);
                else selectTool(Tool.SELECT);
                break;
            case KeyEvent.VK_2:
                if (!shift) selectTool(Tool.LINE);
                else {
                    selectTool(Tool.OBJECT);
                    currentO.remove();
                    currentO = make(cunit, new P(x, y));
                }
                break;
            case KeyEvent.VK_3:
                if (!shift) selectTool(Tool.MODIFY);
                else selectTool(Tool.RECLAIM);
                break;
            case KeyEvent.VK_4:
                if (!shift) selectTool(Tool.DELETE);
                else {
                    currentO.remove();
                    selectTool(Tool.REMOVE);
                }
                break;
            case KeyEvent.VK_5:
                selectTool(Tool.RECTANGLE);
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
            case -KeyEvent.VK_W:
            case -KeyEvent.VK_S:
            case -KeyEvent.VK_A:
            case -KeyEvent.VK_D:
                setSpeed(0,0);
                break;
            case KeyEvent.VK_UP:
                currentO.pos.add(0,-1);
                break;
            case KeyEvent.VK_DOWN:
                currentO.pos.add(0,1);
                break;
            case KeyEvent.VK_LEFT:
                currentO.pos.add(-1,0);
                break;
            case KeyEvent.VK_RIGHT:
                currentO.pos.add(1,0);
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
                    FileManager.loadPlayer(-1, "0");
                    Global.display.input = Global.player.input;
                    Global.player.pos.set(Global.camera);
                    Global.player.say("Beware the intangibility");
                }
                break;
            case KeyEvent.VK_F2:
                Updater.editor = !Updater.editor;
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
        if (r instanceof Unit){
            ((Unit) r).say(name);
            Updater.addQueue.remove(((Unit) r).brain);
            chat("test",0,0);
        }
        r.pos = p;
        return r;
    }
    private void setSpeed(int x, int y){
        v.x = x * SCROLLSPEED;
        v.y = y * SCROLLSPEED;
        if (shift){
            v.mult(10,10);
        }
    }
    private void selectTool(Tool tool) {
        message(tool.name());
        this.tool = tool;
        if (current != null && connected){
            Global.map.land.remove(current);
            history.remove(current);
        }
        currentO.remove();
        connected = false;
    }

    public void update() {
        Global.cameraTo.add(v);
        Global.mapEdgeCenter(Global.cameraTo);
    }

    private void message(String p){
        this.message = p;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(landColor);
        g.setStroke(basicStroke);
        for (Line i : Global.map.land) i.draw(g, P.zero);
        g.setColor(Color.GREEN);
        g.setStroke(new BasicStroke(2));
        g.drawLine(Global.rel.x(), Global.rel.y(), (int) (Global.map.size.x+Global.rel.x), Global.rel.y());
        g.drawLine((int) (Global.map.size.x+Global.rel.x), Global.rel.y(), (int) (Global.map.size.x+Global.rel.x), (int) (Global.map.size.y+Global.rel.y));
        g.drawLine((int) (Global.map.size.x+Global.rel.x), (int) (Global.map.size.y+Global.rel.y), Global.rel.x(), (int) (Global.map.size.y+Global.rel.y));
        g.drawLine(Global.rel.x(), (int) (Global.map.size.y+Global.rel.y), Global.rel.x(), Global.rel.y());
        g.setColor(Color.MAGENTA);
        g.setFont(Global.font);
        g.drawString(message, Settings.centerX-96, Settings.screenY-64);
    }

    private void save(String p){
        message("Saved ("+p+")");
        currentO.remove();
        FileManager.saveMap(p);
    }
}