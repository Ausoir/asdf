package com.ga.unit;

import com.ga.game.Display;
import com.ga.game.FileManager;
import com.ga.game.Observer;
import com.ga.map.Drawable;
import com.ga.map.Global;
import com.ga.map.Updatable;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.tool.Timer;
import com.ga.unit.intelligence.Intelligence;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ga on 5/6/2016.
 */
public class Updater {
    public static boolean pause = false;
    public static boolean editor = false;
    public static int id = 1;
    private static ArrayList<Drawable> drawee = new ArrayList<>();
    private static ArrayList<Effect> effects = new ArrayList<>();
    private static ArrayList<Updatable> updatee = new ArrayList<>();
    private static ArrayList<Matter> matter = new ArrayList<>();
    private static ArrayList<Unit> colidee = new ArrayList<>();
    private static ArrayList<Intelligence> controllee = new ArrayList<>();
    public static HashMap<Integer, Unit> unitList = new HashMap<>();
    private static ArrayList<Observer> addQueue = new ArrayList<>();
    private static ArrayList<Observer> removeQueue = new ArrayList<>();
    public static String changeMap = null;
    public static int changeMapPos = 0;
    public static Performer changeMapEvent = null;
    private static Timer time = new Timer(40,true);
    private static void clear(){
        drawee.clear();
        updatee.clear();
        colidee.clear();
        effects.clear();
        addQueue.clear();
        removeQueue.clear();
        unitList.clear();
        controllee.clear();
        matter.clear();
        Global.map.clear();
        Display.addMenus();
    }
    public static int getId(){
        return id++;
    }
    public static void changeMap(boolean editor){
        clear();
        if (!editor) {
            Global.gameHour++;
            addQueue.add(Global.player.host);
            Global.player.host.cmap = changeMap;
            Global.player.host.pos.set(FileManager.readMap(changeMap, changeMapPos));
            Global.mapEdgeCenter(Global.cameraTo = Global.player.host.pos.getAdd(0,0));
            Global.player.host.reset();
            Display.input = Global.player;
            if (changeMapEvent != null){
                changeMapEvent.perform();
                changeMapEvent = null;
            }
        } else {
            FileManager.readMap(changeMap, -2);
        }
        changeMap = null;
    }
    public static void updateControl(){
        for (Intelligence i : controllee) i.update();
    }
    public static void update(){
        while (addQueue.size() > 0){
            Observer p = addQueue.remove(0);
            if (p instanceof Matter) matter.add((Matter) p);
            else if (p instanceof Updatable) updatee.add((Updatable) p);
            if (p instanceof Effect) effects.add((Effect) p);
            if (p instanceof Intelligence) controllee.add((Intelligence) p);
            if (p instanceof Unit){
                Unit unit = (Unit) p;
                colidee.add(unit);
                unitList.put(unit.id, unit);
            }
            if (p instanceof Drawable) addDrawee((Drawable) p);
        }
        while (removeQueue.size() > 0){
            Observer p = removeQueue.remove(0);
            updatee.remove(p);
            matter.remove(p);
            colidee.remove(p);
            effects.remove(p);
            unitList.remove(p);
            controllee.remove(p);
            drawee.remove(p);
        }
        Global.camera.add(Global.cameraTo.getSubtract(Global.camera.getAdd(0,40)).getMult(.2f,.2f));
        for (Updatable i : updatee) i.update();
        if (!editor && !pause) {
            for (Matter i : matter) i.update();
            for (int i = 0; i < effects.size(); i++) {
                if (effects.get(i).over()) remove((Thing) effects.get(i));
            }
            if (time.update()){
                Global.update();
                FileManager.saveCharacter();
            }
            if (changeMap != null) changeMap(false);
        }
    }
    public static void draw(Graphics2D g2){
        Global.setRel();
        for (Drawable i : drawee){
            i.draw(g2);
        }
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
        drawee.remove(p);
        addDrawee(p);
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
    public static String toMapString(){
        String result = "";
        for (Matter i : matter){
            if (!i.toString().equals("") && !(i instanceof Player)) result += i.toString()+";";
        }
        if (result.length() == 0) return "";
        result = result.substring(0,result.length()-1);
        return result;
    }
    public static void sound(Matter p, String msg){
        for (Matter i : colidee){
            if (i != p && i.faction != p.faction) {
                if (Math.abs(i.pos.x - p.pos.x) < 600 && Math.abs(i.pos.y - p.pos.y) < 200) {
                    if (i instanceof Man) {
                        ((Man) i).hear(p, msg);
                    }
                }
            }
        }
    }
    public static ArrayList<Matter> collideInteractive(P p){
        ArrayList<Matter> result = new ArrayList<>();
        for (Matter i : matter){
            if (i.onInteract != null) {
                if (i.pos.getMinus().getAdd(p).getDist() <= i.onInteract.range) result.add(i);
            }
        }
        return result;
    }
    public static ArrayList<Matter> collide(P p, int r){
        ArrayList<Matter> result = new ArrayList<>();
        for (Matter i : matter){
            if (i.pos.getMinus().getAdd(p).getDist() <= r) result.add(i);
        }
        return result;
    }
    public static Unit collide(Matter p, int r){
        Unit result = null;
        for (int i = 0; i < colidee.size(); i++) {
            Unit t = colidee.get(i);
            if (p.faction.canAttack(t.faction)) {
                if (t.checkHit(p,r)) {
                    result = t;
                }
            }
        }
        return result;
    }
    public static Unit collide(Matter host, Line p, boolean cleave){
        Unit result = null;
        for (int i = 0; i < colidee.size(); i++) {
            Unit t = colidee.get(i);
            if (host.faction.canAttack(t.faction)) {
                if (t.checkHit(host,p)){
                    host.attackHitSound();
                    if (!cleave) return t;
                    else result = t;
                }
            }
        }
        return result;
    }
    public static ArrayList<Matter> perceive(Unit host, P vision, int r){
        ArrayList<Matter> seen = new ArrayList<>();
        int max = r;
        for (Matter i : matter){
            if (host != i) {
                float x = Math.abs(i.pos.x-vision.x), y = Math.abs(i.pos.y - vision.y);
                if (x < r && y < r/3) {
                    if (x+y < max){
                        max = (int) (x+y);
                        seen.add(0,i);
                    } else seen.add(i);
                }
            }
        }
        return seen;
    }
    public static P getEntrancePos(int p){
        int index = 0;
        for (Matter i : matter){
            if (i.name.equals("PlayerSpawn")){
                if (index == p) return i.pos;
                else index++;
            }
        }
        return new P();
    }
}
