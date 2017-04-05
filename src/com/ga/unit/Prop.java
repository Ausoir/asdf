package com.ga.unit;

import com.ga.game.Settings;
import com.ga.map.Global;
import com.ga.tool.Interactive;
import com.ga.tool.Line;
import com.ga.tool.P;

import java.util.Random;

/**
 * Created by Ga on 3/13/2017. asd
 */
public class Prop extends Thing {
    protected int factoryId = -1;
    public int seed = 0;
    public Random rand;
    public Interactive onInteract = null;
    public boolean gravity = false;
    boolean mobile = true;
    public Faction faction = Faction.Destructive;
    public P vel = new P(0, 0);
    public DamageType damageType = DamageType.Bash;
    protected boolean tangible = true;
    protected boolean dynamic = false; //regrowth sensitive
    public Line lastLand = new Line();
    public Tags tags = new Tags();
    public Prop(){
        super(null);
    }
    public Prop(String name, int seed, int depth) {
        super(name, depth);
        this.seed = seed;
        rand = new Random(this.seed);
        onLoad();
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
    public void onLoad(){}
    public void onField(){
        adjustToLand();
    }
    public void adjustToLand(){
        Line closest = null;
        float closestY = pos.y + 9999;
        Line line = new Line(pos, new P(pos.x, closestY));
        for (Line i : Global.map.land) {
            if (line.lineCross(i)) {
                if (i.isLandable()) {
                    float y = i.getCrossPointY(pos.x);
                    if (y < closestY) {
                        closest = i;
                        closestY = y;
                    }
                }
            }
        }
        if (closest != null) {
            rotation = (float) Math.atan(closest.getSlope());
            setDepth(closest.depth - 2);
        }
    }
    public int getSize(){
        return image.sizeRadius;
    }
    public P getPoint(){
        float x = image.getSize().x*.5f*dir;
        return pos.getAdd(x*Global.cos(rotation),x*Global.sin(rotation));
    }
    public boolean hittable(Faction faction){
        return tangible && faction.canAttack(this.faction);
    }
    public void collide(){
        P vp;
        Line pp;
        boolean collided = true;
        int timeout = 8;
        float maxX = vel.x;
        while (collided) {
            collided = false;
            vp = pos.getAdd(vel);
            pp = new Line(pos,vp);
            for (Line i : Global.map.land) {
                if (i.lineCross(pp)) {
                    if (i.isLand() && i.depth >= lastLand.depth) {
                        onCollide(i);
                        float slope = i.getSlope();
                        boolean cc = !i.clockwise();
                        float lineY = i.getCrossPointY(pos.x);
                        if (cc && lineY <= pos.y){
                            vel.set(0, 0);
                            return;
                        } else if (!cc && lineY >= pos.y) {
                            float slopeSpeed = vel.x + (Global.sin((float) Math.atan(slope)) * Math.abs(vel.x));
                            if (vel.x > 0) vel.x = Math.min(slopeSpeed, maxX);
                            else if (vel.x < 0) vel.x = Math.max(slopeSpeed, maxX);
                            if (!Double.isInfinite(slope)){
                                vel.y += (i.pt.y + (slope * (pos.x + vel.x - i.pt.x))) - (pos.y + vel.y) - 1;
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
    public void explode(int r){
        for (Unit i : Updater.collideUnit(pos, getSize()+r, faction)){
            i.checkHit(new Line(pos, pos),getDamage(),damageType);
        }
    }
    public void onCollide(Line p){
    }
    public void update(){
        if (mobile) {
            int edge = Global.mapEdge(pos.getAdd(vel));
            if (edge % 2 == 0) vel.x = 0;
            else if (edge % 3 == 0) {
                if (edge == 9) remove();
                else vel.y = 0;
            }
            collide();
            pos.add(vel);
            if (gravity) {
                vel.y += Settings.gravity;
                if (vel.y > Settings.gravityMax) vel.y = Settings.gravityMax;
            }
        }
    }

    @Override
    public String toString(){
        if (!save) return "";
        return (factoryId==-1?"!"+name:""+factoryId) + "@" + seed + "@" + pos;
    }
}

