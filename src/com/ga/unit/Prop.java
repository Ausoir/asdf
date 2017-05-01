package com.ga.unit;

import com.ga.game.Settings;
import com.ga.map.Global;
import com.ga.map.Updater;
import com.ga.tool.Interactive;
import com.ga.tool.Line;
import com.ga.tool.P;

import java.util.Random;

/**
 * Created by Ga on 3/13/2017. asd
 */
public class Prop extends Thing {
    public int factoryId = -1;
    public int seed = 0;
    public Random rand;
    public Interactive onInteract = null;
    public boolean gravity = false;
    protected boolean mobile = true;
    public Faction faction = Faction.Destructive;
    public P vel = new P(0, 0);
    protected boolean tangible = true;
    public boolean dynamic = false; //regrowth sensitive
    public Line lastLand = new Line();
    public Tags tags = new Tags();
    public Prop(){
        super(null);
    }
    public Prop(String name, int seed, int depth) {
        super(name, depth);
        this.seed = seed;
        rand = new Random(this.seed);
    }
    public int getDamage(){
        return 0;
    }
    public void randomSize(P sizeVariance){
        P s = image.getSize();
        float rx = rand.nextFloat() * (sizeVariance.x*2) + (1-sizeVariance.x);
        float ry = rand.nextFloat() * (sizeVariance.y*2) + (1-sizeVariance.y);
        image.setSize(s.x * rx, s.y * ry);
    }
    public void onCreate(){}
    public void onField(){
        adjustToLand();
    }
    private void adjustToLand(){
        Line closest = null;
        float closestY = pos.y + 9999;
        Line line = new Line(pos, new P(pos.x, closestY));
        for (Line i : Global.map.land) {
            if (line.lineCross(i)) {
                if (i.next != -1 && i.isLandable()) {
                    float y = i.getCrossPointY(pos.x);
                    if (y < closestY) {
                        closest = i;
                        closestY = y;
                    }
                }
            }
        }
        if (closest != null) {
            setDepth(closest.depth - 2);
        }
    }
    public int getSize(){
        return image.sizeRadius;
    }
    public boolean hittable(Faction faction){
        return tangible && faction.canAttack(this.faction);
    }
    public void collide(){
        P vp;
        Line pp;
        boolean collided = true;
        int timeout = 8;
        while (collided) {
            collided = false;
            vp = pos.getAdd(vel);
            pp = new Line(pos,vp);
            for (Line i : Global.map.land) {
                if (i.isLand() && i.depth >= lastLand.depth) {
                    if (i.lineCross(pp)) {
                        onCollide(i);
                        float slope = i.getSlope();
                        boolean cc = !i.clockwise();
                        float lineY = i.getCrossPointY(pos.x);
                        if (cc && lineY <= pos.y){
                            vel.set(0,0);
                            return;
                        } else if (!cc && lineY >= pos.y) {
                            float atan = (float) Math.atan(slope);
                            if (!Double.isInfinite(slope)){
                                float test = vel.y;
                                vel.y += (i.pt.y + (slope * (pos.x + vel.x - i.pt.x))) - (pos.y + vel.y) - 1;
                                vel.x += Global.sin(atan)*(test-vel.y)*Settings.friction;
                            } else {
                                vel.x *= -1;
                                collide();
                                return;
                            }
                            lastLand = i;
                            collided = true;
                        }
                    }
                }
            }
            timeout--;
            if (timeout == 0){
                System.out.println("Collision timeout pos@"+pos+"; vel@"+vel);
                return;
            }
        }
    }
    protected void explode(int r){
        for (Unit i : Updater.collideUnit(pos, getSize()+r, faction)){
            i.checkHit(new Line(pos, pos),getDamage());
        }
    }
    public void onCollide(Line l){
    }
    public void update(){
        if (mobile) {
            collide();
            int edge = Global.mapEdge(pos.getAdd(vel));
            if (edge > 0) {
                if (Math.abs(edge-5) == 1) vel.x = 0;
                else {
                    if (edge == 9) remove();
                    else vel.y = 0;
                }
            }
            pos.add(vel);
            if (gravity) {
                vel.y += Settings.gravity;
                if (vel.y > Settings.gravityMax) vel.y = Settings.gravityMax;
            }
            vel.x *= .94f;
        }
    }
    public String[] load(String p) {
        String[] ps = p.split("@");
        pos.load(ps[2]);
        rotation = Float.parseFloat(ps[3]);
        return ps;
    }

    @Override
    public String toString(){
        if (!save) return "";
        return (factoryId==-1?name:""+factoryId) + "@" + seed + "@" + pos
                + "@" + String.format(java.util.Locale.US,"%.2f", rotation);
    }
}

