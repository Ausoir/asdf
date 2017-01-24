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
    public int seed = 0;
    public Random rand;
    public boolean gravity = false;
    public Faction faction = Faction.Destructive;
    public P vel = new P(0, 0);
    public DamageType damageType = DamageType.Bash;
    public boolean tangible = true;
    public Interactive onInteract = null;
    public Matter(){
        super(null);
    }
    public Matter(String name, int seed, P pt, int depth) {
        super(name, pt, depth);
        if (seed == -1) this.seed = Global.getSeed();
        else this.seed = seed;
        rand = new Random(this.seed);
    }
    public int getDamage(){
        return 0;
    }
    public boolean hittable(){
        return true;
    }
    public boolean checkHit(P p, int r){
        if (!hittable()) return false;
        if (!tangible) return false;
        if (Math.pow(sizeRadius+r,2) - ((Math.pow(p.x-pos.x,2) + Math.pow(p.y-pos.y,2))) > 0) {
            return true;
        }
        return false;
    }
    public boolean checkHit(Matter p, int r){
        if (!hittable()) return false;
        if (p.faction.canAttack(faction)) {
            if (checkHit(p.pos,p.sizeRadius+r)) {
                return true;
            }
        }
        return false;
    }
    public boolean checkHit(Matter hitter, Line p){
        return checkHit(hitter,0);
    }
    public void attackDenied(){

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
                    if (i.opt != -1) {
                        onCollide(i);
                        float slope = i.getSlope();
                        boolean cc = !i.clockwise();
                        float tangent = (float) Math.atan(slope);
                        if (!cc && Math.abs(slope) > 3){
                            vel.set(0, 0);
                            return;
                        } else if (cc && i.pt.y + (slope * (pos.x - i.pt.x)) < pos.y) {
                            vel.set(0, 0);
                            return;
                        }
                        vel.x = vel.x > 0 ? Global.clamp(vel.x + (Global.sin(tangent) * Math.abs(vel.x)), 1f, maxX)
                                : Global.clamp(vel.x + (Global.sin(tangent) * Math.abs(vel.x)), maxX, -1f);
                        vel.y += (i.pt.y + (slope * (pos.x + vel.x - i.pt.x))) - (pos.y + vel.y) - 1;
                        collided = true;
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
    private boolean isInside(float x, float y){
        Line pp = new Line((int)x,(int)y,999999,(int)y);
        int num = 0;
        for (Line i : Global.map.land){
            if (i.lineCross(pp)){
                num++;
            }
        }
        return num % 2 == 1;
    }
    public void onCollide(Line l){
    }
    public P getPoint(){
        return pos.getAdd(image.size.x*.5f*Global.cos(rotation)*dir,(int) (image.size.x*.5f*Global.sin(rotation))*dir);
    }
    @Override
    public void update(){
        int edge = Global.mapEdge(pos.getAdd(vel));
        if (edge > 1) {
            if (edge % 3 == 0) vel.x = 0;
            if (edge % 2 == 0) vel.y = 0;
        }
        collide();
        pos.add(vel);
        if (gravity) {
            vel.y += Settings.gravity;
            if (vel.y > Settings.gravityMax) vel.y = Settings.gravityMax;
            if (pos.y+vel.y >= Global.map.getHeight()) remove();
        }
    }
    @Override
    public String toString(){
        return name + "@" + seed + "@" + pos;
    }
}
