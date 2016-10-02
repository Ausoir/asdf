package com.ga.unit;

import com.ga.game.Display;
import com.ga.game.FileManager;
import com.ga.game.Observer;
import com.ga.map.Drawable;
import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Timer;
import com.ga.unit.item.Item;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Ga on 5/6/2016.
 */
public class Updater {
    public static boolean editor = false;
    public static boolean showTerrain = false;
    public static ArrayList<Drawable> drawee = new ArrayList<>();
    public static ArrayList<Effect> effects = new ArrayList<>();
    public static ArrayList<Observer> other = new ArrayList<>();
    public static ArrayList<Matter> updatee = new ArrayList<>();
    public static ArrayList<Thing> addQueue = new ArrayList<>();
    public static Color landColor = Color.pink;
    public static String changeMap = null;
    public static int changeMapPos = 0;
    private static Timer time = new Timer(10,true);
    public static void clear(){
        drawee.clear();
        other.clear();
        updatee.clear();
        effects.clear();
        addQueue.clear();
    }
    public static void changeMap(boolean editor){
        clear();
        if (!editor) {
            addQueue.add(Global.player.host);
            Global.player.host.pos.set(FileManager.readMap(changeMap, changeMapPos));
            Global.player.host.reset();
            Display.input = Global.player;
        } else {
            FileManager.readMap(changeMap, -2);
        }
        changeMap = null;
    }
    public static void draw(Graphics2D g2){
        if (addQueue.size() > 0){
            for (int i=0;i<addQueue.size();i++){
                Thing p = addQueue.get(i);
                addDrawee(p);
                if (p instanceof Matter) updatee.add((Matter) p);
                if (p instanceof Effect) effects.add((Effect) p);
                addQueue.remove(i);
            }
        }
        for (Drawable i : drawee) i.draw(g2);
        if (editor || showTerrain){
            g2.setColor(landColor);
            for (Line i : Global.map.land) i.draw(g2,P.getZero());
        }
        for (Observer i : other) i.update();
        if (!editor) {
            for (int i = 0; i < updatee.size(); i++) {
                updatee.get(i).update();
            }
            for (int i = 0; i < effects.size(); i++) {
                if (effects.get(i).over()) remove((Thing) effects.get(i));
            }
        }
        if (changeMap != null) changeMap(false);
        if (time.update()) Global.update();
    }
    public static void remove(Thing p){
        addQueue.remove(p);
        drawee.remove(p);
        updatee.remove(p);
        effects.remove(p);
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
        if (updatee.size() == 0) return "";
        String result = "";
        for (Matter i : updatee){
            if (!i.toString().equals("")) result += i.toString()+";";
        }
        result = result.substring(0,result.length()-1);
        return result;
    }
    public static void sound(Matter p, String msg){
        for (Matter i : updatee){
            if (i != p && i.faction != p.faction) {
                if (Math.abs(i.pos.x - p.pos.x) < 600 && Math.abs(i.pos.y - p.pos.y) < 200) {
                    if (i instanceof Man) {
                        ((Man) i).hear(p, msg);
                    }
                }
            }
        }
    }
    public static Item collide(P p){
        for (Drawable i : drawee){
            if (i instanceof Item){
                if (((Item) i).pos.getMinus().getAdd(p).getDist()<=4) return (Item) i;
            }
        }
        return null;
    }
    public static Matter collide(Matter p, int r){
        Matter result = null;
        for (int i = 0; i < updatee.size(); i++) {
            Matter t = updatee.get(i);
            if (p.faction != t.faction) {
                if (t.checkHit(p,r)) {
                    result = t;
                }
            }
        }
        return result;
    }
    public static Matter collide(Matter host, Line p, boolean cleave){
        Matter result = null;
        for (int i = 0; i < updatee.size(); i++) {
            Matter t = updatee.get(i);
            if (host.faction != t.faction) {
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
        for (Matter i : updatee){
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
}
