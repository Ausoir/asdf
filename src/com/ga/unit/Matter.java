package com.ga.unit;

import com.ga.game.Settings;
import com.ga.map.Global;
import com.ga.map.Updatable;
import com.ga.tool.Interactive;
import com.ga.tool.Line;
import com.ga.tool.P;

import java.util.Random;

/**
 * Created by Ga on 8/12/2016.
 */
public class Matter extends Thing implements Updatable {
    public int factoryId = -1;
    public int seed = 0;
    public Random rand;
    public boolean gravity = false;
    public Faction faction = Faction.Destructive;
    public P vel = new P(0, 0);
    public DamageType damageType = DamageType.Bash;
    protected boolean tangible = true;
    public Interactive onInteract = null;
    protected float sizeMod;
    public Matter(){
        super(null);
    }
    public Matter(String name, int seed, int depth) {
        super(name, depth);
        if (seed == -1) this.seed = Global.getSeed();
        else this.seed = seed;
        rand = new Random(this.seed);
    }
    public void randomSize(){
        P s = image.getSize();
        float rx = rand.nextFloat();
        float ry = rand.nextFloat();
        image.setSize(s.x * (rx * .6f + .7f), s.y * (ry * .6f + .7f));
        sizeMod = (rx + ry)/2;
    }
    public int getDamage(){
        return 0;
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
                    if (i.isLand()) {
                        onCollide(i);
                        float slope = i.getSlope();
                        boolean cc = !i.clockwise();
                        float lineY = i.pt.y + (slope * (pos.x - i.pt.x));
                        if (cc && lineY <= pos.y){
                            vel.set(0, 0);
                            return;
                        } else if (!cc && lineY >= pos.y) {
                            float slopeSpeed = vel.x + (Global.sin((float) Math.atan(slope)) * Math.abs(vel.x));
                            vel.x = vel.x > 0 ? Math.min(slopeSpeed, maxX)
                                    : Math.max(slopeSpeed, maxX);
                            if (!Double.isInfinite(slope)) vel.y += (i.pt.y + (slope * (pos.x + vel.x - i.pt.x))) - (pos.y + vel.y) - 1;
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
        for (Unit i : Updater.collideUnit(pos, getCollisionSize()+r, faction)){
            i.checkHit(new Line(pos, pos),getDamage(),damageType);
        }
    }
    public int getCollisionSize(){
        return image.sizeRadius;
    }
    public void onCollide(Line l){
    }
    public P getPoint(){
        float x = image.getSize().x*.5f*dir;
        return pos.getAdd(x*Global.cos(rotation),x*Global.sin(rotation));
    }
    @Override
    public void update(){
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
    @Override
    public String toString(){
        if (!save) return "";
        return (factoryId==-1?"!"+name:""+factoryId) + "@" + seed + "@" + pos;
    }
}
