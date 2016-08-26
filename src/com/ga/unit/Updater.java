package com.ga.unit;

import com.ga.game.Display;
import com.ga.game.FileManager;
import com.ga.game.Observer;
import com.ga.intelligence.PlayerInput;
import com.ga.map.Drawable;
import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.tool.P;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Ga on 5/6/2016.
 */
public class Updater {
    public static boolean editor = false;
    public static boolean showTerrain = false;
    public static ArrayList<Drawable> drawee = new ArrayList<>();
    public static ArrayList<Observer> other = new ArrayList<>();
    public static ArrayList<Unit> updatee = new ArrayList<>();
    public static ArrayList<Thing> addQueue = new ArrayList<>();
    public static Color landColor = Color.pink;
    public static String changeMap = null;
    public static int changeMapPos = 0;
    public static void clear(){
        drawee.clear();
        other.clear();
        updatee.clear();
        addQueue.clear();
    }
    public static void changeMap(){
        clear();
        FileManager.readMap(changeMap, editor?-2:changeMapPos);
        Display.input = Global.player;
        changeMap = null;
    }
    public static void draw(Graphics2D g2){
        if (addQueue.size() > 0){
            for (int i=0;i<addQueue.size();i++){
                Thing p = addQueue.get(i);
                addDrawee(p);
                if (p instanceof Unit) updatee.add((Unit) p);
                addQueue.remove(i);
            }
        }
        for (Drawable i : drawee) i.draw(g2);
        if (editor || showTerrain){
            g2.setColor(landColor);
            for (Line i : Global.map.land) i.draw(g2);
        }
        for (Observer i : other) i.update();
        if (!editor)
            for (int i = 0; i < updatee.size(); i++) {
                updatee.get(i).update();
            }
        if (changeMap != null) changeMap();
    }
    public static void setDepth(Thing p, int depth){
        p.depth = depth;
        drawee.remove(p);
        addDrawee(p);
    }
    public static void remove(Thing p){
        drawee.remove(p);
        updatee.remove(p);
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
        for (Unit i : updatee){
            if (!i.toString().equals("")) result += i.toString()+";";
        }
        result = result.substring(0,result.length()-1);
        return result;
    }
    public static Unit collide(Man host, Line p, int force, boolean cleave){
        Unit result = null;
        for (Unit i : updatee){
            if (host.faction != i.faction) {
                if (i instanceof Man) {
                    Man ii = (Man) i;
                    Line body = ii.clip.organs[4].getAdd(ii.pos);
                    if (p.lineCross(body)) {
                        ii.hit(host, force);
                        result = i;
                        if (!cleave) return result;
                    }
                }
            }
        }
        return result;
    }
    public static Unit collide(Unit host, P p, int force, int range, boolean cleave){
        Unit result = null;
        for (Unit i : updatee){
            if (host.faction != i.faction) {
                P head = i.getCollisionPoint();
                if (range - (Math.pow(p.x - head.x, 2) + Math.pow(p.y - head.y, 2)) > 0) {
                    i.hit(host, force);
                    result = i;
                    if (!cleave) return result;
                }
            }
        }
        return result;
    }
    public static Unit collide(Man host, P p, int force){
        return collide(host,p,force,625,false);
    }
    public static ArrayList<Unit> perceive(Man host, P vision, int r){
        ArrayList<Unit> seen = new ArrayList<>();
        int max = r;
        for (Unit i : updatee){
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
