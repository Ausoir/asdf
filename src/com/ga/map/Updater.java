package com.ga.map;

import com.ga.game.FileManager;
import com.ga.game.Observer;
import com.ga.tool.EventTimer;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.*;
import com.ga.unit.skill.Actions;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Ga on 5/6/2016. asd
 */
public class Updater {
    public static boolean editor = false;
    private static ArrayList<Drawable> drawee = new ArrayList<>();
    private static ArrayList<Effect> effects = new ArrayList<>();
    private static ArrayList<Updatable> updatee = new ArrayList<>();
    static ArrayList<Prop> props = new ArrayList<>();
    private static ArrayList<AI> controllee = new ArrayList<>();
    public static ArrayList<Unit> unitsLoaded = new ArrayList<>();
    public static ArrayList<Unit> units = new ArrayList<>();
    public static ArrayList<Observer> addQueue = new ArrayList<>();
    private static ArrayList<Observer> removeQueue = new ArrayList<>();
    static String changeMap = null;
    static Performer changeMapEvent = null;
    static void clear(){
        drawee.clear();
        effects.clear();
        addQueue.clear();
        removeQueue.clear();
        unitsLoaded.clear();
        units.clear();
        controllee.clear();
        props.clear();
        Global.map.clear();
        Global.display.removeSpeech();
    }
    static void changeMap(){
        String newMap = changeMap;
        changeMap = null;
        final ArrayList<Integer> chasers = new ArrayList<>();
        for (AI i : controllee){
            if (i.target == Global.player || i.host.faction == Global.player.faction){
                chasers.add(i.host.controlId);
            }
        }
        clear();
        FileManager.loadActors();
        FileManager.loadPlayer(1);
        int pos = FileManager.loadMap(newMap);
        update();
        Global.player.pos.set(getEntrancePos(pos));
        for (int i : chasers){
            Unit unit = getUnloadedActor(i);
            unit.pos.set(Global.player.pos);
            unit.cmap = Global.player.cmap;
            units.remove(unit);
            unit.start();
        }
        if (changeMapEvent != null){
            changeMapEvent.perform();
            changeMapEvent = null;
        }
    }
    public static void updateControl(){
        for (AI i : controllee) i.update();
    }
    public static void updateOutside(){
        Faction.update();
    }
    public static String moveToward(String from, String to){
        for (Prop i : props) {
            if (i.factoryId == Stock.Travel.ordinal()) {
                return i.pos;
            }
        }
    }
    public static void addUpdatee(Updatable p){
        updatee.add(p);
    }
    public static void update(){
        while (addQueue.size() > 0){
            Observer p = addQueue.remove(0);
            if (p instanceof Unit){
                Unit unit = (Unit)p;
                units.add((Unit) p);
                if (((Unit)p).cmap.equals(Global.map.name)){
                    unitsLoaded.add(unit);
                    unit.onField();
                    unit.brain.start();
                } else {
                    continue; //Don't add to drawable
                }
            } else if (p instanceof Prop){
                Prop obj = (Prop)p;
                props.add(obj);
                (obj).onField();
            }
            if (p instanceof Effect) effects.add((Effect) p);
            if (p instanceof AI) controllee.add((AI) p);
            if (p instanceof Drawable) addDrawee((Drawable) p);
        }
        while (removeQueue.size() > 0){
            Observer p = removeQueue.remove(0);
            drawee.remove(p);
            props.remove(p);
            unitsLoaded.remove(p);
            units.remove(p);
            controllee.remove(p);
        }
        Global.camera.add(Global.cameraTo.getSubtract(Global.camera.getAdd(0,40)).mult(.2f,.2f));
        for (Updatable i : updatee) i.update();
        if (!editor){
            for (Prop i : props) i.update();
            for (Unit i : unitsLoaded) i.update();
        }
        for (int i = 0; i < effects.size(); i++) {
            Effect t = effects.get(i);
            if (t.over()){
                drawee.remove(t);
                effects.remove(t);
            }
        }
        if (changeMap != null) changeMap();
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
    public static String toPropString(){
        String result = "";
        for (Prop i : props){
            if (i.save) result += i.toString()+";";
        }
        if (result.length() > 0) result = result.substring(0,result.length()-1);
        return result;
    }
    public static String toActorString(){
        String result = "";
        for (Unit i : units){
            if (i.save) result += i.toString()+"\n";
        }
        return result;
    }
    public static Unit getUnloadedActor(int p){
        for (Observer i : addQueue){
            if (i instanceof Unit && ((Unit)i).controlId == p) return (Unit) i;
        }
        for (Unit i : units){
            if (i.controlId == p) return i;
        }
        return null;
    }
    public static Unit getLoadedActor(int p){
        for (Unit i : unitsLoaded){
            if (i.controlId == p) return i;
        }
        return null;
    }
    public static ArrayList<Prop> collideInteractive(P p){
        ArrayList<Prop> result = new ArrayList<>();
        for (Prop i : props){
            if (i.onInteract != null) {
                if (i.pos.getSubtract(p).getDist() <= 24) result.add(i);
            }
        }
        for (Unit i : unitsLoaded){
            if (i.onInteract != null) {
                if (i instanceof Man){
                    if (i.getHand().getSubtract(p).getDist() <= 24) result.add(i);
                } else {
                    if (i.pos.getSubtract(p).getDist() <= 24) result.add(i);
                }
            }
        }
        return result;
    }
    public static ArrayList<Prop> collideAll(P p, int r){
        ArrayList<Prop> result = new ArrayList<>();
        for (Prop i : props){
            if (p.getSubtract(i.pos).getDist() <= r+i.getSize()) result.add(i);
        }
        for (Prop i : unitsLoaded){
            if (p.getSubtract(i.pos).getDist() <= r+i.getSize()) result.add(i);
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
        for (Unit i : unitsLoaded){
            if (i.hittable(faction) && p.getSubtract(i.pos).getDist() <= r) result.add(i);
        }
        return result;
    }
    public static ArrayList<Unit> perceive(Unit host, P vision, int r){
        ArrayList<Unit> seen = new ArrayList<>();
        int max = r;
        for (Unit i : unitsLoaded){
            if (host != i) {
                float x = Math.abs(i.pos.x-vision.x), y = Math.abs(i.pos.y - vision.y);
                if (x < r && y < r/2) {
                    if (x+y < max){
                        max = (int) (x+y);
                        seen.add(0,i);
                    } else seen.add(i);
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
        for (Unit i : Updater.unitsLoaded) {
            if (i.hittable(faction)){
                boolean sorted = false;
                float danger = (float) Math.pow(800f/(i.pos.getSubtract(Global.player.pos).getDist()),2);
                for (int ii=0;ii<dangers.size();ii++){
                    if (danger > dangers.get(ii)){
                        sorted = true;
                        dangers.add(ii, danger);
                        units.add(ii, i);
                        break;
                    }
                }
                if (!sorted){
                    dangers.add(danger);
                    units.add(i);
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
    private static P getEntrancePos(int p){
        for (Prop i : props) {
            if (i.factoryId == Stock.Spawn.ordinal() && i.seed == p) {
                return i.pos;
            }
        }
        return new P();
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
