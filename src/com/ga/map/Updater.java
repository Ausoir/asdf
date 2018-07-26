package com.ga.map;

import com.ga.game.Observer;
import com.ga.tool.L;
import com.ga.tool.P;
import com.ga.unit.*;
import com.ga.unit.man.Man;

import java.awt.*;

/**
 * Created by Ga on 5/6/2016. asd
 */
public class Updater {
    public boolean editor = false;
    private Map map;
    private Drawable drawee;
    private L<Particle> effects = new L<>();
    private L<Updatable> updatee = new L<>();
    public L<Prop> props = new L<>();
    L<AI> controllee = new L<>();
    public L<Unit> unitsLoaded = new L<>();
    public L<Observer> addQueue = new L<>();
    private L<Observer> removeQueue = new L<>();

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
        if (!editor){
            for (Prop i : props) i.update();
            for (Unit i : unitsLoaded) i.update(); //new class?
        }
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
    private L<Drawable> printDraw(){
        L<Drawable> stack = new L<>();
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
    public L<Prop> collideInteractive(P p){
        L<Prop> result = new L<>();
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
    L<Prop> collideAll(P p, int r){
        L<Prop> result = new L<>();
        for (Prop i : props){
            if (p.getSubtract(i.pos).getDist() <= r+i.getSize()) result.add(i);
        }
        for (Prop i : unitsLoaded){
            if (p.getSubtract(i.pos).getDist() <= r+i.getSize()) result.add(i);
        }
        return result;
    }
    public L<Prop> collideProp(P p, int r){
        L<Prop> result = new L<>();
        for (Prop i : props){
            if (i.hittable(Faction.Destructive) && p.getSubtract(i.pos).getDist() <= r+i.getSize()) result.add(i);
        }
        return result;
    }
    public L<Unit> collideUnit(P p, int r, Faction faction){
        L<Unit> result = new L<>();
        for (Unit i : unitsLoaded){
            if (i.hittable(faction) && p.getSubtract(i.pos).getDist() <= r) result.add(i);
        }
        return result;
    }
    public L<Unit> perceive(Unit host, P vision, int r){
        L<Unit> seen = new L<>();
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
    void onTimeChange(){
        for (Prop i : props) i.onTimeChange();
    }
    public boolean sleep(int p, boolean checkEnemy){
        for (Unit i : unitsLoaded) {
            if (i.map == Global.player.map && i.faction.canAttack(Global.player.faction) && (checkEnemy || i.brain.target == Global.player)) {
                Global.player.say("There are enemies on the map!");
                return false;
            }
        }
        if (!Global.player.get("Fatigue").require(p)){
            Global.player.say("I am too tired! I need "+p+" Fatigue points.");
            return false;
        }
        if (!Global.player.get("Nutrition").require(p)){
            Global.player.get("Fatigue").add(p,false);
            Global.player.say("I am too hungry! I need "+p+" Nutrition points.");
            return false;
        }
        map.weather.fadeIn();
        //Hour pass
        Global.player.stats.clearBuffs();
        Global.player.get("Intelligence").add(Math.max(0, Global.player.traits.getTraitLevel(Traits.Trait.Attunement)-1), false);
        Global.player.get("Strength").heal();
        Global.timePass(p*100);
        Global.hour+=p;
        for (Map i : Global.maps) i.onTimeChange();
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
