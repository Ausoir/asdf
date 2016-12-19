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

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ga on 5/6/2016.
 */
public class Updater {
    public static int tick = 0;
    public static boolean pause = false;
    public static boolean editor = false;
    public static boolean showTerrain = false;
    public static int id = 1;
    public static ArrayList<Drawable> drawee = new ArrayList<>();
    public static ArrayList<Effect> effects = new ArrayList<>();
    public static ArrayList<Updatable> updatee = new ArrayList<>();
    public static ArrayList<Matter> matter = new ArrayList<>();
    public static ArrayList<Unit> colidee = new ArrayList<>();
    public static HashMap<Integer, Unit> unitList = new HashMap<>();
    public static ArrayList<Observer> addQueue = new ArrayList<>();
    public static ArrayList<Observer> removeQueue = new ArrayList<>();
    public static Color landColor = Color.pink;
    public static String changeMap = null;
    public static int changeMapPos = 0;
    public static Performer changeMapEvent = null;
    private static Timer time = new Timer(10,true);
    public static void clear(){
        drawee.clear();
        updatee.clear();
        colidee.clear();
        effects.clear();
        addQueue.clear();
        unitList.clear();
        matter.clear();
    }
    public static int getId(){
        return id++;
    }
    public static void changeMap(boolean editor){
        clear();
        if (!editor) {
            addQueue.add(Global.player.host);
            addQueue.add(Global.player);
            Global.player.host.pos.set(FileManager.readMap(changeMap, changeMapPos));
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
    public static void draw(Graphics2D g2){
        if (addQueue.size() > 0){
            for (int i=0;i<addQueue.size();i++){
                Observer p = addQueue.get(i);
                if (p instanceof Drawable) addDrawee((Drawable) p);
                if (p instanceof Matter) matter.add((Matter) p);
                else if (p instanceof Updatable) updatee.add((Updatable) p);
                if (p instanceof Effect) effects.add((Effect) p);
                if (p instanceof Unit){
                    Unit unit = (Unit) p;
                    colidee.add(unit);
                    unitList.put(unit.id, unit);
                }
                addQueue.remove(i);
            }
        }
        if (removeQueue.size() > 0){
            for (int i=0;i<removeQueue.size();i++) {
                Observer p = removeQueue.get(i);
                remove(p);
            }
        }
        for (Drawable i : drawee) i.draw(g2);
        if (editor || showTerrain){
            g2.setColor(landColor);
            for (Line i : Global.map.land) i.draw(g2,P.getZero());
        } else Global.camera = Global.player.host.pos.getAdd(0,-40);
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
            tick++;
        }
        if (changeMap != null) changeMap(false);
    }
    private static void remove(Observer p){
        addQueue.remove(p);
        drawee.remove(p);
        updatee.remove(p);
        matter.remove(p);
        colidee.remove(p);
        effects.remove(p);
        unitList.remove(p);
    }
    public static void addDrawee(Drawable p){
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
            if (!i.toString().equals("")) result += i.toString()+";";
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
        return P.getZero();
    }
}
