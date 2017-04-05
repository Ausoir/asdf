package com.ga.unit;

import com.ga.game.FileManager;
import com.ga.game.Observer;
import com.ga.map.Drawable;
import com.ga.map.Global;
import com.ga.map.Terrain;
import com.ga.map.Updatable;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.tool.Timer;
import com.ga.unit.intelligence.Intelligence;
import com.ga.unit.skill.Actions;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ga on 5/6/2016. asd
 */
@SuppressWarnings("SuspiciousMethodCalls")
public class Updater {
    public static boolean editor = false;
    private static ArrayList<Drawable> drawee = new ArrayList<>();
    private static ArrayList<Effect> effects = new ArrayList<>();
    private static ArrayList<Updatable> updatee = new ArrayList<>();
    private static ArrayList<Prop> props = new ArrayList<>();
    private static ArrayList<Intelligence> controllee = new ArrayList<>();
    public static HashMap<Integer, Unit> unitList = new HashMap<>();
    private static ArrayList<Observer> addQueue = new ArrayList<>();
    private static ArrayList<Observer> removeQueue = new ArrayList<>();
    public static String changeMap = null;
    public static Performer changeMapEvent = null;
    private static Timer time = new Timer(40,true);
    private static void clear(){
        drawee.clear();
        effects.clear();
        addQueue.clear();
        removeQueue.clear();
        unitList.clear();
        controllee.clear();
        props.clear();
        Global.map.clear();
        Global.display.removeSpeech();
    }
    public static void changeMap(boolean editor){
        clear();
        if (!editor) {
            boolean rightSide = Global.player.pos.x > Global.map.size.x/2;
            int pos = FileManager.loadMap(changeMap);
            boolean spawnFound = false;
            for (Observer i : addQueue){
                if (i instanceof Prop && ((Prop)i).name.equals("Spawn") && ((Prop)i).seed == pos){
                    Global.player.pos.set(((Prop)i).pos);
                    spawnFound = true;
                    break;
                }
            }
            if (!spawnFound){
                if (rightSide){
                    Global.player.pos.set(24,Global.map.fallSafe(new P(24,0),0).getCrossPointY(24)-1);
                } else {
                    Line rightEdge = Global.map.fallSafe(new P(Global.map.size.x-24,0),0);
                    Global.player.pos.set(Global.map.size.x-24,rightEdge.getCrossPointY(rightEdge.ptd.x-24)-1);
                }
            }
            addQueue.add(Global.player);
            addQueue.add(Global.player.weapon);
            Global.player.onMapChange();
            Global.display.input = Global.player.input;
            Global.mapEdgeCenter(Global.cameraTo = Global.player.pos.getAdd(0,0));
            Global.mapEdgeCenter(Global.camera = Global.cameraTo.getAdd(0,0));
            if (changeMapEvent != null){
                changeMapEvent.perform();
                changeMapEvent = null;
            }
        } else {
            FileManager.loadMap(changeMap);
        }
        changeMap = null;
    }
    public static void updateControl(){
        for (Intelligence i : controllee) i.update();
    }
    public static void addUpdatee(Updatable p){
        updatee.add(p);
    }
    public static void update(){
        while (addQueue.size() > 0){
            Observer p = addQueue.remove(0);
            if (p instanceof Prop){
                props.add((Prop) p);
                ((Prop)p).onField();
            }
            if (p instanceof Effect) effects.add((Effect) p);
            if (p instanceof Intelligence) controllee.add((Intelligence) p);
            if (p instanceof Unit){
                Unit unit = (Unit) p;
                unitList.put(unit.controlId, unit);
            }
            if (p instanceof Drawable) addDrawee((Drawable) p);
        }
        while (removeQueue.size() > 0){
            Observer p = removeQueue.remove(0);
            drawee.remove(p);
            props.remove(p);
            unitList.remove(p);
            controllee.remove(p);
        }
        Global.camera.add(Global.cameraTo.getSubtract(Global.camera.getAdd(0,40)).mult(.2f,.2f));
        for (Updatable i : updatee) i.update();
        if (!editor) {
            for (Prop i : props) i.update();
            for (int i = 0; i < effects.size(); i++) {
                Effect t = effects.get(i);
                if (t.over()){
                    drawee.remove(t);
                    effects.remove(t);
                }
            }
            if (changeMap != null) changeMap(false);
            else if (time.update()){
                Global.map.event.update();
                Global.update();
                FileManager.saveCharacter();
            }
        }
    }
    public static void draw(Graphics2D g2){
        Global.setRel();
        for (Drawable i : drawee){
            i.draw(g2);
        }
        Global.map.weather.draw(g2);
    }
    public static void add(Observer p){
        addQueue.add(p);
    }
    public static void cancelAdd(Observer p){
        addQueue.remove(p);
    }
    public static void remove(Observer p){
        removeQueue.add(p);
    }
    public static void resetDepth(Drawable p){
        if (drawee.remove(p)) addDrawee(p);
    }
    private static void addDrawee(Drawable p){
        int size = drawee.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                if (drawee.get(i).getDepth() > p.getDepth()){
                    drawee.add(i, p);
                    return;
                }
            }
        }
        drawee.add(p);
    }
    public static void regrowth(){
        int size = addQueue.size();
        for (int i=size-1;i>=0;i--){
            Observer t = addQueue.get(i);
            if (t instanceof Prop && !(t instanceof Player)){
                if (((Prop)t).dynamic) {
                    addQueue.remove(t);
                    drawee.remove(t); //drawee already added through thread gap
                }
            }
        }
    }
    public static String toMapString(){
        String result = "";
        for (Prop i : props){
            if (!i.toString().equals("") && !(i instanceof Player)) result += i.toString()+";";
        }
        if (result.length() == 0) return "";
        result = result.substring(0,result.length()-1);
        return result;
    }
    public static void sound(Unit p, String msg){
        for (Map.Entry i : unitList.entrySet()){
            Unit unit = (Unit) i.getValue();
            if (i != p && unit.faction != p.faction) {
                if (Math.abs(unit.pos.x - p.pos.x) < 600 && Math.abs(unit.pos.y - p.pos.y) < 200) {
                    if (i instanceof Man) {
                        unit.hear(p, msg);
                    }
                }
            }
        }
    }
    static ArrayList<Prop> collideInteractive(P p){
        ArrayList<Prop> result = new ArrayList<>();
        for (Prop i : props){
            if (i.onInteract != null) {
                if (i instanceof Man){
                    if (((Man) i).getHand().getSubtract(p).getDist() <= 64) result.add(i);
                } else {
                    if (i.pos.getSubtract(p).getDist() <= i.getSize()) result.add(i);
                }
            }
        }
        return result;
    }
    public static ArrayList<Prop> collideProp(P p, int r){
        ArrayList<Prop> result = new ArrayList<>();
        for (Prop i : props){
            if (i.hittable(Faction.Destructive) && p.getSubtract(i.pos).getDist() <= r+i.getSize()) result.add(i);
        }
        return result;
    }
    public static ArrayList<Unit> collideUnit(P p, int r, Faction faction){
        ArrayList<Unit> result = new ArrayList<>();
        for (Map.Entry i : unitList.entrySet()){
            Unit unit = (Unit) i.getValue();
            if (unit.hittable(faction) && p.getSubtract(unit.pos).getDist() <= r) result.add(unit);
        }
        return result;
    }
    public static ArrayList<Unit> perceive(Unit host, P vision, int r){
        ArrayList<Unit> seen = new ArrayList<>();
        int max = r;
        for (Map.Entry i : unitList.entrySet()){
            Unit unit = (Unit) i.getValue();
            if (host != i && unit.state != Actions.Sneak) {
                float x = Math.abs(unit.pos.x-vision.x), y = Math.abs(unit.pos.y - vision.y);
                if (x < r && y < r) {
                    if (x+y < max){
                        max = (int) (x+y);
                        seen.add(0,unit);
                    } else seen.add(unit);
                }
            }
        }
        return seen;
    }
    public static P getEntrancePos(int p){
        int index = 0;
        for (Prop i : props){
            if (i.name.equals("PlayerSpawn")){
                if (index == p) return i.pos;
                else index++;
            }
        }
        return new P();
    }

    public static void removeUpdatee(Updatable p) {
        updatee.remove(p);
    }

    public static void unrender() {
        for (Observer i : drawee){
            if (i instanceof Terrain) remove(i);
        }
    }
}
