package com.ga.map;

import com.ga.game.Observer;
import com.ga.tool.L;
import com.ga.tool.P;
import com.ga.tool.T;
import com.ga.unit.*;

import java.awt.*;

/**
 * Created by Ga on 5/6/2016. asd
 */
public class Updater {
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
    void addUpdatee(Updatable p){
        updatee.add(p);
    }
    public void processQueue(){
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
    }
    public void update(){
        processQueue();
        Global.updateCamera();
        for (Updatable i : updatee) i.update();
        if (!Global.editor){
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
        StringBuilder result = new StringBuilder();
        for (Prop i : props){
            if (i.save) result.append(T.wrap(i.toString())).append(",");
        }
        for (Unit i : unitsLoaded){
            if (i.save && !i.traveler) result.append(T.wrap(i.toString())).append(",");
        }
        if (result.length() > 0) result = new StringBuilder(result.substring(0, result.length() - 1));
        return result.toString();
    }
    public static String toActorString(){
        StringBuilder result = new StringBuilder();
        for (Map map : Global.maps) {
            for (Unit i : map.updater.unitsLoaded) {
                if (i.save && i.traveler) result.append(i.toString()).append("\n");
            }
        }
        return result.toString();
    }
    public static Unit getActor(int p){
        for (Map map : Global.maps) {
            for (Observer i : map.updater.addQueue) {
                if (i instanceof Unit && ((Unit) i).controlId == p) return (Unit) i;
            }
            for (Unit i : map.updater.unitsLoaded) {
                if (i.controlId == p) return i;
            }
        }
        return null;
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
                    if (x + y < max) {
                        max = (int) (x + y);
                        seen.add(0, i);
                    } else seen.add(i);
                }
            }
        }
        return seen;
    }
    private void onTimeChange(){
        for (Prop i : props) i.onTimeChange();
    }
    public boolean sleep(int p, boolean checkHostile){
        if (p == 0) return true;
        for (Unit i : unitsLoaded) {
            if (i.map == Global.player.map && i.hittable(Global.player.faction) && (checkHostile || i.brain.target == Global.player)) {
                Global.player.say("There are enemies on the map!");
                return false;
            }
        }
        if (Global.player.status.isDangerous()){
            Global.player.say("I am inflicted with dangerous status effect!");
            return false;
        }
        if (Global.player.stats.fatigue.get() < p){
            Global.player.say("I am too tired! I need "+(p-Global.player.stats.fatigue.get())+" more Fatigue points.");
            return false;
        }
        if (Global.player.stats.nutrition.get() < p){
            Global.player.say("I am too hungry! I need "+(p-Global.player.stats.nutrition.get())+" more Nutrition points.");
            return false;
        }
        if (Global.player.armor.getWarmth() < map.temperature){
            if (!Global.player.stats.agility.require(p)) {
                Global.player.say("It's too cold!");
                return false;
            } else Global.player.say("Vitality decreased by "+p+" from cold temperature.");
        }
        Global.player.stats.fatigue.require(p);
        Global.player.stats.nutrition.require(p);
        forceSleep(p);
        return true;
    }
    public void forceSleep(int p){
        map.weather.fadeIn();
        //Hour pass
        Global.player.stats.strength.heal();
        if (Global.player.traits.getTraitLevel(Traits.Trait.Learner) > 0) Global.player.stats.motivation.add(p, false);
        Global.timePass(p*100);
        Global.hour+=p;
        for (Map i : Global.maps) i.onTimeChange();
        onTimeChange();
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
