package com.ga.map;

import com.ga.game.FileManager;
import com.ga.game.Observer;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.*;
import com.ga.unit.player.Player;
import com.ga.unit.skill.Actions;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ga on 5/6/2016. asd
 */
public class Updater {
    public static boolean editor = false;
    private static ArrayList<Drawable> drawee = new ArrayList<>();
    private static ArrayList<Effect> effects = new ArrayList<>();
    private static ArrayList<Updatable> updatee = new ArrayList<>();
    private static ArrayList<Prop> props = new ArrayList<>();
    private static ArrayList<AI> controllee = new ArrayList<>();
    public static HashMap<Integer, Unit> unitList = new HashMap<>();
    public static ArrayList<Observer> addQueue = new ArrayList<>();
    private static ArrayList<Observer> removeQueue = new ArrayList<>();
    public static String changeMap = null;
    public static Performer changeMapEvent = null;
    static void clearProps(){
        for (Prop i : props) i.remove();
    }
    static void clear(){
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
    static void changeMap(boolean editor){
        String newMap = changeMap;
        changeMap = null;
        clear();
        if (!editor) {
            int pos = FileManager.loadMap(newMap);
            update();
            Global.player.pos.set(getEntrancePos(pos));
            Global.player.reconnect();
            if (changeMapEvent != null){
                changeMapEvent.perform();
                changeMapEvent = null;
            }
        } else {
            FileManager.loadMap(newMap);
        }
    }
    public static void updateControl(){
        for (AI i : controllee) i.update();
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
            if (p instanceof AI) controllee.add((AI) p);
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
        if (!editor) for (Prop i : props) i.update();
        for (int i = 0; i < effects.size(); i++) {
            Effect t = effects.get(i);
            if (t.over()){
                drawee.remove(t);
                effects.remove(t);
            }
        }
        if (changeMap != null) changeMap(false);
        else if (!editor) Global.update();
    }
    public static void draw(Graphics2D g2){
        Global.setRel();
        for (Drawable i : drawee){
            i.draw(g2);
        }
//        Grapher.draw(g2);
        Global.map.weather.draw(g2);
    }
    public static void cancelRemove(Observer p){
        removeQueue.remove(p);
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
                if (drawee.get(i).getDepth() >= p.getDepth()){
                    drawee.add(i, p);
                    return;
                }
            }
        }
        drawee.add(p);
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
    public static ArrayList<Prop> collideInteractive(P p){
        ArrayList<Prop> result = new ArrayList<>();
        for (Prop i : props){
            if (i.onInteract != null) {
                if (i instanceof Man){
                    if (((Man) i).getHand().getSubtract(p).getDist() <= 64) result.add(i);
                } else {
                    if (i.pos.getSubtract(p).getDist() <= 24) result.add(i);
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
            if (host != i) {
                float x = Math.abs(unit.pos.x-vision.x), y = Math.abs(unit.pos.y - vision.y);
                if (x < r && y < r/2) {
                    if (x+y < max){
                        max = (int) (x+y);
                        seen.add(0,unit);
                    } else seen.add(unit);
                }
            }
        }
        return seen;
    }
    public static int sleep(int p){
        int origTime = Global.time;
        Unit encounter = getMapEncounter(Global.player.faction, p);
        Global.map.weather.fadeIn();
        if (encounter != null){
            int timePassed = Global.time - origTime;
            if (timePassed > 1) {
                encounter.pos = Global.player.pos.getAdd(0, 0);
                Actions.Blink.onSet(encounter);
            }
            encounter.brain.target = Global.player;
            Global.player.say("Interrupted after "+timePassed+" seconds!");
            return timePassed;
        } else {
            if (p % 100 == 0) Global.player.say((p/100)+" hour(s) passed.");
            Global.player.say(p+" seconds passed.");
            Global.timePass(p);
            return p;
        }
    }
    private static Unit getMapEncounter(Faction faction, int time){
        ArrayList<Unit> units = new ArrayList<>();
        ArrayList<Float> dangers = new ArrayList<>();
        for (Map.Entry i : unitList.entrySet()) {
            Unit unit = (Unit) i.getValue();
            if (unit.hittable(faction)){
                boolean sorted = false;
                float danger = (float) Math.pow(800f/(unit.pos.getSubtract(Global.player.pos).getDist()),2);
                for (int ii=0;ii<dangers.size();ii++){
                    if (danger > dangers.get(ii)){
                        sorted = true;
                        dangers.add(ii, danger);
                        units.add(ii, unit);
                        break;
                    }
                }
                if (!sorted){
                    dangers.add(danger);
                    units.add(unit);
                }
            }
        }
        for (int ii=0;ii<time;ii+=10) {
            for (int i=0;i<dangers.size();i++) {
                float rand = Global.r.nextFloat();
                if (rand < dangers.get(i)) {
                    Global.timePass(ii);
                    return units.get(i);
                }
            }
        }
        return null;
    }
    public static P getEntrancePos(int p){
        for (Prop i : props) {
            if (i.factoryId == Stock.Spawn.ordinal() && i.seed == p) {
                return i.pos;
            }
        }
        return new P();
    }

    public static void removeUpdatee(Updatable p) {
        updatee.remove(p);
    }

    static void unrender() {
        for (Observer i : drawee){
            if (i instanceof Terrain) remove(i);
        }
    }

    static void normalize(P adjustment) {
        for (Observer i : addQueue){
            if (i instanceof Prop){
                ((Prop)i).pos = ((Prop)i).pos.getAdd(adjustment);
            }
        }
    }
}
