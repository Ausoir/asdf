package com.ga.map;

import com.ga.game.Observer;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.*;
import com.ga.unit.man.Man;

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
    public ArrayList<Prop> props = new ArrayList<>();
    ArrayList<AI> controllee = new ArrayList<>();
    public ArrayList<Unit> unitsLoaded = new ArrayList<>();
    public ArrayList<Observer> addQueue = new ArrayList<>();
    private ArrayList<Observer> removeQueue = new ArrayList<>();

    Updater(Map map){
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
            if (p instanceof Drawable) drawee.add((Drawable) p);
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
            if (p != null) { //thread safe
                ((Drawable) p).removeThis();
                props.remove(p);
                unitsLoaded.remove(p);
                controllee.remove(p);
                effects.remove(p);
            }
        }
    }
    public void update(){
        processQueue();
        Global.updateCamera();
        for (Updatable i : updatee) i.update();
        for (Prop i : props) i.update();
        if (!editor) for (Unit i : unitsLoaded) i.update(); //new class?
        for (Particle t : effects) t.update();
    }
    public void draw(Graphics2D g2){
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
        if (!addQueue.remove(p) && !removeQueue.contains(p)) removeQueue.add(p);
    }
    public String toString(){
        String result = "";
        for (Prop i : props){
            if (i.save) result += i.toString()+";";
        }
        if (result.length() > 0) result = result.substring(0,result.length()-1);
        return result;
    }
    public static String toActorString(){
        String result = "";
        for (Map map : Global.maps) {
            for (Unit i : map.updater.unitsLoaded) {
                if (i.save) result += i.toString() + "\n";
            }
        }
        return result;
    }
    public static Unit getActor(int p){
        for (Map map : Global.maps) {
            for (Observer i : map.updater.addQueue){
                if (i instanceof Unit && ((Unit) i).controlId == p) return (Unit) i;
            }
            for (Unit i : map.updater.unitsLoaded) {
                if (i.controlId == p) return i;
            }
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
    ArrayList<Prop> collideAll(P p, int r){
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
            if (host != i && i.image.visible) {
                float x = Math.abs(i.pos.x-vision.x), y = Math.abs(i.pos.y - vision.y);
                if (x < r && y < r/3) {
//                    int overlap = 0;
//                    P head = i.clip.organs[Math.max(0, i.clip.getHead())].pt.getAdd(i.pos);
//                    for (Line l : host.map.land){
//                        if (l.depth > host.getDepth() && new Line(head, new P(head.x, 0)).lineCross(l)){
//                            overlap++;
//                        }
//                    }
//                    if (overlap % 2 == 0) {
                        if (x + y < max) {
                            max = (int) (x + y);
                            seen.add(0, i);
                        } else seen.add(i);
//                    }
                }
            }
        }
        return seen;
    }
    public boolean sleep(int p, boolean checkEnemy){
        if (checkEnemy) {
            for (Unit i : unitsLoaded) {
                if (i.map == Global.player.map && i.faction.canAttack(Global.player.faction) && i.brain.target == Global.player) {
                    Global.player.say("There are enemies on the map!");
                    return false;
                }
            }
        }
        if (!Global.player.get("Nutrition").require(p)){
            Global.player.say("I am too hungry! I need "+p+" nutrition points.");
            return false;
        }
        map.weather.fadeIn();
        Global.hourPass(p);
        return true;
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
