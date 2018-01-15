package com.ga.map;

import com.ga.game.FileManager;
import com.ga.game.Input;
import com.ga.game.Processes;
import com.ga.game.Settings;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.Prop;
import com.ga.unit.Stock;
import com.ga.unit.Unit;
import com.ga.unit.item.Item;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class EditorInput extends Drawable implements Input, Updatable {
    private enum Tool {
        NONE, REPOSITION, MODIFY, LINE, RECTANGLE, DELETE,
        SELECT, OBJECT, REMOVE
    }
    private final int SNAP = 24;
    private final int SCROLLSPEED = 50;
    private Stroke basicStroke = new BasicStroke(10);
    private String txDepth = "1";
    private String txTexture = "dirt";
    private String txShader = "grass";
    private String lineChange = "";
    private int lastSelected = -1;
    private boolean control = false;
    private boolean shift = false;
    private boolean lmb = false;
    private P lmbP = new P();
    private ArrayList<Line> history = new ArrayList<>();
    private Line current = new Line(0, 0, 0, 0);
    private Prop currentO;
    private boolean connected = false, repos = false;
    private Tool tool = Tool.NONE;
    private P v = new P();
    private Map map;
    private Color landColor = Color.pink;
    private String message = "";
    private String lastCommand = "";
    private String lastSaved = "";
    private int smooth = 0;

    public EditorInput() {
        init();
        currentO = new Prop(map, "Error", 0);
        Global.updates.time = -1;
        lastSaved = map.name;
    }

    private void init(){
        Updater.editor = true;
        map = Global.player.map;
        map.updater.addUpdatee(this);
        Global.display.input = this;
        map.updater.addQueue.add(this);
        Global.camera = new P(map.size.getMult(.5f,.5f));
        currentO = new Prop(map, null,-1);
        currentO.save = false;
        map.updater.update();
        for (Prop i : map.updater.props){
            if (i.factoryId == Stock.Spawn.ordinal() || i.factoryId == Stock.Travel.ordinal()
                    || i.factoryId == Stock.Spawner.ordinal()){
                i.image.visible = true;
            }
        }
        for (Unit i : map.updater.unitsLoaded) i.say(i.name);
        map.updater.controllee.clear();
        Processes.editorMode();
    }

    @Override
    public float getDepth() {
        return 999;
    }

    @Override
    public void chat(String p, int x, int y) {
        lastCommand = p;
        command(p, x, y);
    }
    private void command(String p, int x, int y){
        if (p.equals("mapname")) {
            message(lastSaved);
        } else if (p.equals("size")) {
            message(map.size.toString());
        } else if (p.startsWith("size")){
            String[] size = p.substring(5).split(",");
            map.size.set(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
        } else if (p.startsWith("camera")){
            Global.cameraTo.set(new P(p.substring(7)));
        } else if (p.equals("reverse")){
            reverse(lastSelected);
        } else if (p.equals("pink")){
            landColor = Color.pink;
        } else if (p.equals("white")){
            landColor = Color.white;
        } else if (p.equals("black")) {
            landColor = Color.black;
            basicStroke = new BasicStroke(2);
        } else if (p.equals("none")) {
            landColor = new Color(0,0,0,0);
        } else if (p.equals("margin")) {
            if (map.margin == 0) map.margin = -3600;
            else map.margin = 0;
        } else if (p.equals("spawn")) {
            String msg = "";
            for (Prop i : map.updater.props){
                if (i.factoryId == Stock.Spawn.ordinal()){
                    msg += " ["+i.seed+"@"+i.pos+"] ";
                }
            }
            message(msg);
        } else if (p.startsWith("spawn")) {
            int to = Integer.parseInt(p.substring(6));
            selectTool(Tool.OBJECT);
            currentO = Stock.Spawn.create(map, -1, to);
            make(currentO);
            message("Spawn id ["+to+"] spawned at ["+x+", "+y+"]");
        } else if (p.equals("travel")) {
            String msg = "";
            for (Prop i : map.updater.props){
                if (i.factoryId == Stock.Travel.ordinal()){
                    msg += " ["+i.subId+"@"+i.seed+"@"+i.pos+"] ";
                }
            }
            message(msg);
        } else if (p.startsWith("travel")) {
            String[] to = p.substring(7).split("@");
            if (to.length == 1) to = new String[]{to[0], "-1"};
            selectTool(Tool.OBJECT);
            currentO = Stock.Travel.create(map, Integer.parseInt(to[0]), Integer.parseInt(to[1]));
            make(currentO);
            message("Travel to ["+to[0]+" @ "+to[1]+"] spawned");
        } else if (p.startsWith("spawner")) {
            String[] to = p.substring(8).split("@");
            if (to.length == 1) to = new String[]{to[0], "-1"};
            selectTool(Tool.OBJECT);
            currentO = Stock.Spawner.create(map, Integer.parseInt(to[0]), Integer.parseInt(to[1]));
            make(currentO);
            message("Spawner ["+to[0]+" @ "+to[1]+"] spawned");
        } else if (p.startsWith("prop")) {
            String[] aux = p.substring(5).split("@");
            for (int ii=0;ii<Integer.parseInt(aux[0]);ii++){
                int rx = (int) (Math.random()*map.size.x);
                for (Line l : map.landRaw) {
                    if (l.tid == lastSelected && l.clockwise() && l.lineCross(new Line(rx,0,rx,map.size.y))) {
                        map.placeProp(l, aux[1], -1);
                    }
                }
            }
        } else if (p.startsWith("copy")) {
            String[] texs = message.split(" ")[3].split("&");
            txDepth = texs[0];
            txTexture = texs[1];
            txShader = texs[2];
        } else if (p.startsWith("depth")) {
            txDepth = p.substring(6);
        } else if (p.startsWith("texture")) {
            txTexture = p.substring(8);
        } else if (p.startsWith("shader")) {
            txShader = p.substring(7);
        } else if (p.startsWith("cdepth")) {
            String[] data = map.formTextures.get(lastSelected).split("&");
            map.formTextures.put(lastSelected, p.substring(7)+"&"+data[1]+"&"+data[2]);
            map.updater.remove(map.terrains.get(lastSelected));
            map.render(lastSelected);
            command("show",0,0);
        } else if (p.startsWith("ctexture")) {
            String[] data = map.formTextures.get(lastSelected).split("&");
            map.formTextures.put(lastSelected, data[0]+"&"+p.substring(9)+"&"+data[2]);
            map.updater.remove(map.terrains.get(lastSelected));
            map.render(lastSelected);
            command("show",0,0);
        } else if (p.startsWith("cshader")) {
            String[] data = map.formTextures.get(lastSelected).split("&");
            map.formTextures.put(lastSelected, data[0]+"&"+data[1]+"&"+p.substring(8));
            map.updater.remove(map.terrains.get(lastSelected));
            map.render(lastSelected);
            command("show",0,0);
        } else if (p.startsWith("idO")){
            currentO.subId = Integer.parseInt(p.substring(4));
        } else if (p.startsWith("seedO")){
            currentO.seed = Integer.parseInt(p.substring(6));
        } else if (p.startsWith("changeO")){
            currentO.load(p.substring(8));
        } else if (p.equals("showO")){
            message(currentO.toString());
        } else if (p.equals("show")){
            message("Depth: "+txDepth+", Texture: "+txTexture+", Shader: "+txShader);
        } else if (p.equals("climb")) {
            for (Line l : map.landRaw){
                if (l.tid == lastSelected){
                    if (l.vertical() || !l.clockwise()) l.opt = 0;
                    else l.opt = 1;
                    message("Climbable");
                }
            }
        } else if (p.startsWith("line")) {
            lineChange = p.substring(5);
        } else if (p.startsWith("opt")) {
            int option = Integer.parseInt(p.substring(4));
            for (Line l : map.landRaw){
                if (l.tid == lastSelected){
                    l.opt = option;
                    message("Option changed to "+option);
                }
            }
        } else if (p.equals("grow")){
            map.generateProps();
        } else if (p.startsWith("smooth")){
            smooth = Integer.parseInt(p.substring(7));
            message("Select a land to smooth");
        } else if (p.startsWith("clean")){
            map.clean();
        } else if (p.startsWith("normalize")){
            int marginX = 4, marginY = 4;
            String[] margins = p.substring(10).split(",");
            if (margins.length > 1){
                marginX = Integer.parseInt(margins[0]);
                marginY = Integer.parseInt(margins[1]);
            }
            map.normalize(marginX, marginY);
        } else if (p.startsWith("gen land")){
            int depth = Integer.parseInt(p.substring(9));
            Creation.prepareRandom(-(int) (Math.random()*1000));
            Generator gen = new Generator(map, map.getNextTerrain());
            gen.generateLand(depth, map.size);
        } else if (p.startsWith("gen town")){
            Creation.prepareRandom(-(int) (Math.random()*1000));
            Generator gen = new Generator(map, map.getNextTerrain());
            gen.generateTown();
        } else if (p.startsWith("blocker")){
            Generator gen = new Generator(map, map.getNextTerrain());
            gen.generateBlocker(Integer.parseInt(p.substring(8)), lastSelected);
        } else if (p.startsWith("move")){
            P m = new P(p.substring(5));
            for (Line l : map.landRaw){
                l.pt.add(m);
                l.ptd.add(m);
            }
            for (Prop i : map.updater.props) i.pos.add(m);
        } else if (p.equals("clearO")){
            map.updater.props.clear();
        } else if (p.equals("clear")){
            map.clear();
            map = new Map(map.name);
            map.updater.addQueue.add(this);
        } else if (p.equals("refresh")){
//            map.updater.unrender();
            map.setTexture();
            map.render();
        } else if (p.startsWith("save")){
            lastSaved = p.substring(5);
            save(lastSaved);
        } else if (p.startsWith("load")){
            Updater.editor = false;
            Global.server.requestChangeMap(p.substring(5)+"@0");
            lastSaved = p.substring(5);
            new Timer().schedule(new TimerTask() {
                public void run() {
                    Global.display.input = new EditorInput();
                }
            },100);
        } else if (p.equals("exit")){
            System.exit(0);
        } else if (p.startsWith("build")){
            new Generator(Global.player.map, map.getNextTerrain()).generateBuilding(p.substring(6), new P(x, y), 1);
        } else if (p.startsWith("o")){
            try {
                selectTool(Tool.OBJECT);
                currentO.remove();
                currentO = make(p.substring(2), new P(x,y));
            } catch (Exception e) {
                message(p + " Not found");
            }
        }
    }

    private void reverse(int p){
        for (Line l : map.landRaw) {
            if (l.tid == p) {
                P t = l.pt;
                l.pt = l.ptd;
                l.ptd = t;
            }
        }
    }

    private String getTexture(){
        return txDepth+"&"+txTexture+"&"+txShader;
    }

    private void alignObject(int x, int y, Prop obj, int offset){
        Line check = new Line(x,y,x,y+128);
        for (Line i : map.landRaw) {
            if (check.lineCross(i)) {
                obj.pos.set(x, i.getCrossPointY(x)+offset);
                obj.rotation = i.getAtan() + (control?3.14f:0);
                break;
            }
        }
    }

    private void makePolygon(Line p) {
        int nextPolygon = map.getNextTerrain();
        if (p.ptd.is(history.get(0).pt)) {
            for (Line i : history) {
                i.tid = nextPolygon;
            }
            connected = false;
            map.formTextures.put(nextPolygon, getTexture());
            map.render(nextPolygon);
            history.clear();
        }
    }

    private void removeTerrain(int next) {
        Iterator it = map.landRaw.iterator();
        Line i;
        while (it.hasNext()) {
            i = (Line) it.next();
            if (i.tid == next) {
                it.remove();
                history.remove(i);
            }
        }
        map.formTextures.remove(next);
        map.updater.remove(map.terrains.get(next));
    }

    @Override
    public void input(int mode, int x, int y) {
        if (mode == -2) v.set(0, 0);
        if (tool == Tool.LINE || tool == Tool.RECTANGLE || tool == Tool.REPOSITION) {
            if (!control) {
                x = ((x + SNAP / 2) / SNAP) * SNAP;
                y = ((y + SNAP / 2) / SNAP) * SNAP;
            }
            if (current != null) {
                if (shift) {
                    boolean snap = false;
                    for (int i = 0; i < map.landRaw.size(); i++) {
                        Line l = map.landRaw.get(i);
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
                } else if (tool == Tool.REPOSITION) {
                    if (repos) current.pt.set(x, y);
                } else if (tool == Tool.OBJECT) {
                    currentO.pos.set(x, y);
                } else if (tool == Tool.REMOVE && lmb) {
                    ArrayList<Prop> props = map.updater.collideAll(new P(x,y),24);
                    for (Prop i : props) {
                        String name = i.name;
                        try{i.remove();}catch(Exception e){
                            e.printStackTrace();
                            message("Error deleting "+name);}
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
                    current = new Line(x, y, x, y);
                    map.landRaw.add(current);
                    history.add(current);
                    connected = true;
                } else if (tool == Tool.RECTANGLE){
                    if (connected) {
                        map.landRaw.remove(current);
                        history.remove(current);
                        Line[] lines = new Line[]{new Line(current.pt.x, current.pt.y, x, current.pt.y),
                                new Line(x, current.pt.y, x, y),
                                new Line(x, y, current.pt.x, y),
                                new Line(current.pt.x, y, current.pt.x, current.pt.y)};
                        for (Line l : lines) {
                            map.landRaw.add(l);
                            history.add(l);
                        }
                        makePolygon(lines[3]);
                    }
                    current = new Line(x, y, x, y);
                    map.landRaw.add(current);
                    history.add(current);
                    connected = true;
                } else if (tool == Tool.REPOSITION) {
                    if (!repos){
                        P pt = new P();
                        for (Line i : map.landRaw){
                            if (i.pt.getSubtract(new P(x,y)).getDist() < 24){
                                pt = i.pt;
                                break;
                            } else if (i.ptd.getSubtract(new P(x,y)).getDist() < 24){
                                pt = i.ptd;
                                break;
                            }
                        }
                        current = new Line(pt, new P());
                        repos = true;
                    } else {
                        current = new Line();
                        repos = false;
                    }
                } else if (tool == Tool.OBJECT) {
                    if (shift) alignObject(x,y,currentO,currentO instanceof Item ? -2 : -1);
                    currentO = make(currentO.name, new P(x, y));
                } else if (tool == Tool.SELECT) {
                    final ArrayList<Prop> list = map.updater.collideProp(new P(x, y), 16);
                    final String[] listString = new String[list.size()];
                    Performer[] performers = new Performer[listString.length];
                    for (int i=0;i<listString.length;i++){
                        listString[i] = i+" - "+list.get(i).name+" (id:"+list.get(i).subId+", seed:"+list.get(i).seed+")";
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
                    for (int i = 0; i< map.landRaw.size(); i++){
                        Line l = map.landRaw.get(i);
                        if (l.lineCross(new Line(new P(x, y), lmbP))) {
                            if (shift){
                                history.remove(l);
                                map.landRaw.remove(l);
                            } else removeTerrain(l.tid);
                        }
                    }
                } else if (tool == Tool.MODIFY){
                    for (int i = 0; i < map.landRaw.size(); i++) {
                        Line l = map.landRaw.get(i);
                        if (l.lineCross(new Line(new P(x, y), lmbP))) {
                            map.updater.remove(map.terrains.get(l.tid));
                            map.formTextures.put(l.tid, getTexture());
                            map.render(l.tid);
                            message(l.toString() + " slope:" + l.getSlope() + " " + (l.clockwise() ? "cw" : "cc") + " " + map.formTextures.get(l.tid));
                            break;
                        }
                    }
                } else if (tool == Tool.NONE){
                    if (smooth > 0){
                        int order = -1;
                        ArrayList<Line> lines = new ArrayList<>();
                        for (int i = 0; i < map.landRaw.size(); i++) {
                            Line l = map.landRaw.get(i);
                            if (l.lineCross(new Line(new P(x, y), lmbP))) {
                                lines.add(l);
                                map.landRaw.remove(l);
                                order = i;
                                break;
                            }
                        }
                        int next = lines.get(0).tid;
                        new Generator(map,-1).divideLand(lines, 0, smooth);
                        for (Line i : lines) i.tid = next;
                        map.landRaw.addAll(order, lines);
                        smooth = 0;
                    } else if (!lineChange.equals("")) {
                        for (int i = 0; i < map.landRaw.size(); i++) {
                            Line l = map.landRaw.get(i);
                            if (l.lineCross(new Line(new P(x, y), lmbP))) {
                                l.load(lineChange+","+l.tid+","+l.opt);
                                break;
                            }
                        }
                        lineChange = "";
                    } else {
                        Iterator it = map.landRaw.iterator();
                        Line i;
                        while (it.hasNext()) {
                            i = (Line) it.next();
                            if (i.lineCross(new Line(new P(x, y), lmbP))) {
                                message(i.toString() + " slope:" + i.getSlope() + " " + (i.clockwise() ? "cw" : "cc") + " " + map.formTextures.get(i.tid));
                                lastSelected = i.tid;
                            }
                        }
                    }
                }
                lmb = false;
                break;
            case KeyEvent.VK_ENTER:
                Global.display.chatbox.requestFocus();
                break;
            case KeyEvent.VK_1:
                selectTool(Tool.NONE);
                break;
            case KeyEvent.VK_2:
                selectTool(Tool.REPOSITION);
                break;
            case KeyEvent.VK_3:
                selectTool(Tool.MODIFY);
                break;
            case KeyEvent.VK_4:
                selectTool(Tool.LINE);
                break;
            case KeyEvent.VK_5:
                selectTool(Tool.RECTANGLE);
                break;
            case KeyEvent.VK_6:
                selectTool(Tool.DELETE);
                break;
            case KeyEvent.VK_7:
                selectTool(Tool.SELECT);
                break;
            case KeyEvent.VK_8:
                selectTool(Tool.OBJECT);
                break;
            case KeyEvent.VK_9:
                selectTool(Tool.REMOVE);
                break;
            case KeyEvent.VK_W:
                setSpeed(0,-1);
                break;
            case KeyEvent.VK_S:
                setSpeed(0,1);
                if (control) save(lastSaved);
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
            case KeyEvent.VK_Z:
                if (control) command("load "+lastSaved, 0, 0);
                break;
            case KeyEvent.VK_SPACE:
                command(lastCommand, Global.camera.x(), Global.camera.y());
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
                Global.player.pos.set(Global.cameraTo);
                map = null;
                FileManager.loadPlayer();
                Global.server.requestChangeMap(map.name+"@0");
                break;
        }
    }
    private Prop make(String name, P p){
        if (name == null) return new Prop(map, "Error", 0);
        int seed = -1;
        if (name.contains(":")){
            seed = Integer.parseInt(name.split(":")[1]);
            name = name.substring(0,name.indexOf(":"));
        }
        Prop r = Stock.getByName(map, name, seed);
        make(r);
        r.pos = p;
        return r;
    }
    private Prop make(Prop p){
        if (p instanceof Unit){
            ((Unit) p).say(p.name);
            map.updater.addQueue.remove(((Unit) p).brain);
        }
        p.setDepth(999);
        p.image.visible = true;
        p.start();
        return p;
    }
    private void setSpeed(int x, int y){
        v.x = x * SCROLLSPEED;
        v.y = y * SCROLLSPEED;
        if (shift) v.mult(10,10);
    }
    private void selectTool(Tool tool) {
        String test = "";
        for (Tool i : Tool.values()){
            if (tool == i) test += " ["+i.name()+"] ";
            else test += " "+i.name()+" ";
        }
        message(test);
        this.tool = tool;
        if (current != null && connected){
            map.landRaw.remove(current);
        }
        history.clear();
        currentO.remove();
        connected = false;
        repos = false;
    }

    public void update() {
        Global.cameraTo.add(v);
        map.mapEdgeCenter(Global.cameraTo);
    }

    private void message(String p){
        this.message = p;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(landColor);
        g.setStroke(basicStroke);
        for (Line i : map.landRaw) i.draw(g, P.zero);
        g.setColor(Color.GREEN);
        g.setStroke(new BasicStroke(2));
        g.drawLine(Global.rel.x(), Global.rel.y(), (int) (map.size.x+Global.rel.x), Global.rel.y());
        g.drawLine((int) (map.size.x+Global.rel.x), Global.rel.y(), (int) (map.size.x+Global.rel.x), (int) (map.size.y+Global.rel.y));
        g.drawLine((int) (map.size.x+Global.rel.x), (int) (map.size.y+Global.rel.y), Global.rel.x(), (int) (map.size.y+Global.rel.y));
        g.drawLine(Global.rel.x(), (int) (map.size.y+Global.rel.y), Global.rel.x(), Global.rel.y());
        g.setColor(Color.MAGENTA);
        g.setFont(Global.font);
        g.drawString(message, Settings.centerX-96-(message.length()*6), Settings.screenY-64);
        super.draw(g);
    }

    private void save(String p){
        currentO.remove();
        message("Saved ("+p+")");
        map.clean();
        FileManager.saveMap(p, map);
        FileManager.saveWorld();
    }
}