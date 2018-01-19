package com.ga.map;

import com.ga.game.Observer;
import com.ga.tool.P;
import com.ga.unit.*;
import com.ga.unit.man.Man;
import com.ga.unit.skill.Actions;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Ga on 5/6/2016. asd
 */
public class Updater {
    public static boolean editor = false;
    private Map map;
    private Drawable drawee;
    private ArrayList<Particle> effects = new ArrayList<>();
    private ArrayList<Updatable> updatee = new ArrayList<>();
    ArrayList<Prop> props = new ArrayList<>();
    ArrayList<AI> controllee = new ArrayList<>();
    public ArrayList<Unit> unitsLoaded = new ArrayList<>();
    public ArrayList<Observer> addQueue = new ArrayList<>();
    private ArrayList<Observer> removeQueue = new ArrayList<>();

    public Updater(Map map){
        this.map = map;
        drawee = new Drawable() {};
    }
    void clear(){
        drawee = new Drawable() {};
        effects.clear();
        addQueue.clear();
        removeQueue.clear();
        unitsLoaded.clear();
        controllee.clear();
        props.clear();
        Global.display.removeSpeech();
    }
    public void updateControl(){
        for (AI i : controllee) i.update();
    }
    public void addUpdatee(Updatable p){
        updatee.add(p);
    }
    public void processQueue(){
        while (addQueue.size() > 0){
            Observer p = addQueue.remove(0);
            if (p instanceof Drawable) drawee.add((Drawable) p, (Drawable) p);
            if (p instanceof Unit){
                Unit unit = (Unit)p;
                if (((Unit)p).map == map){
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
            if (p instanceof Particle) effects.add((Particle) p);
            if (p instanceof AI) controllee.add((AI) p);
        }
        while (removeQueue.size() > 0){
            Observer p = removeQueue.remove(0);
//            drawee.remove((Drawable) p);
            props.remove(p);
            unitsLoaded.remove(p);
            controllee.remove(p);
        }
    }
    public void update(){
        processQueue();
        Global.camera.add(Global.cameraTo.getSubtract(Global.camera.getAdd(0,40)).mult(.2f,.2f));
        for (Updatable i : updatee) i.update();
        if (!editor){
            for (Prop i : props) i.update();
            for (Unit i : unitsLoaded) i.update();
        }
        for (int i = 0; i < effects.size(); i++) {
            Particle t = effects.get(i);
            if (t.over()){
                t.remove();
                effects.remove(t);
            }
        }
    }
    public void draw(Graphics2D g2){
        Global.setRel();
        Drawable t = drawee;
        while (t != null){
           t.draw(g2);
            t = t.next;
        }
//        Grapher.draw(g2);
//        printDraw();
    }
    private ArrayList<Drawable> printDraw(){
        ArrayList<Drawable> stack = new ArrayList<>();
        Drawable t = drawee;
        while (t.next != null){
            stack.add(t);
            t = t.next;
        }
        return stack;
    }
    public void remove(Observer p){
        if (!addQueue.remove(p)) removeQueue.add(p);
    }
    public String toString(){
        String result = "";
        for (Prop i : props){
            if (i.save) result += i.toString()+";";
        }
        if (result.length() > 0) result = result.substring(0,result.length()-1);
        return result;
    }
    public String toActorString(){
        String result = "";
        for (Unit i : map.updater.unitsLoaded) {
            if (i.save) result += i.toString() + "\n";
        }
        return result;
    }
    public static String toAllActorString(){
        String result = "";
        ArrayList<Map> maps = (ArrayList<Map>) Global.maps.clone();
        for (Map map : maps) {
            result += map.updater.toActorString();
        }
        return result;
    }
    public static Unit getUnloadedActor(int p){
        for (Map map : Global.maps) {
            for (Observer i : map.updater.addQueue) {
                if (i instanceof Unit && ((Unit) i).controlId == p) return (Unit) i;
            }
            for (Unit i : map.updater.unitsLoaded){
                if (i.controlId == p) return i;
            }
        }
        return null;
    }
    public Unit getLoadedActor(int p){
        for (Unit i : unitsLoaded){
            if (i.controlId == p) return i;
        }
        return null;
    }
    public ArrayList<Prop> collideInteractive(P p){
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
    public ArrayList<Prop> collideAll(P p, int r){
        ArrayList<Prop> result = new ArrayList<>();
        for (Prop i : props){
            if (p.getSubtract(i.pos).getDist() <= r+i.getSize()) result.add(i);
        }
        for (Prop i : unitsLoaded){
            if (p.getSubtract(i.pos).getDist() <= r+i.getSize()) result.add(i);
        }
        return result;
    }
    public ArrayList<Prop> collideProp(P p, int r){
        ArrayList<Prop> result = new ArrayList<>();
        for (Prop i : props){
            if (i.hittable(Faction.Destructive) && p.getSubtract(i.pos).getDist() <= r+i.getSize()) result.add(i);
        }
        return result;
    }
    public ArrayList<Unit> collideUnit(P p, int r, Faction faction){
        ArrayList<Unit> result = new ArrayList<>();
        for (Unit i : unitsLoaded){
            if (i.hittable(faction) && p.getSubtract(i.pos).getDist() <= r) result.add(i);
        }
        return result;
    }
    public ArrayList<Unit> perceive(Unit host, P vision, int r){
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
    public int sleep(int p){
        int origTime = Global.time;
        Unit encounter = getMapEncounter(Global.player.faction, p);
        map.weather.fadeIn();
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
    private Unit getMapEncounter(Faction faction, int time){
        ArrayList<Unit> units = new ArrayList<>();
        ArrayList<Float> dangers = new ArrayList<>();
        for (Unit i : unitsLoaded) {
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
    public P getEntrancePos(int p, P failsafe) {
        if (props.size() == 0) {
            for (Observer i : addQueue) {
                if (i instanceof Prop) {
                    Prop prop = (Prop) i;
                    if (prop.factoryId == Stock.Spawn.ordinal() && prop.seed == p) {
                        return prop.pos;
                    }
                }
            }
        } else {
            for (Prop i : props) {
                if (i.factoryId == Stock.Spawn.ordinal() && i.seed == p) {
                    return i.pos;
                }
            }
        }
        return failsafe;
    }

    void adjust(P adjustment) {
        for (Observer i : addQueue){
            if (i instanceof Prop){
                ((Prop)i).pos = ((Prop)i).pos.getAdd(adjustment);
            }
        }
    }
}
