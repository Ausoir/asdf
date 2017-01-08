package com.ga.unit;

import com.ga.game.Settings;
import com.ga.map.Global;
import com.ga.map.Updatable;
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
    public boolean interactive = false;
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
    public boolean checkHit(P p, int r){
        if (!tangible) return false;
        if (Math.pow(sizeRadius+r,2) - ((Math.pow(p.x-pos.x,2) + Math.pow(p.y-pos.y,2))) > 0) {
            return true;
        }
        return false;
    }
    public boolean checkHit(Matter p, int r){
        if (p.faction != faction) {
            if (checkHit(p.pos,p.sizeRadius+r)) {
                return true;
            }
        }
        return false;
    }
    public boolean checkHit(Matter hitter, Line p){
        return checkHit(hitter,0);
    }
    public void collide(){
        P vp = pos.getAdd(vel);
        Line pp = new Line(pos,vp);
        for (Line i : Global.map.land){
            if (i.lineCross(pp)){
                if (i.opt != -1) {
                    float slope = i.getSlope();
                    float t = slope * vel.x;
                    if (t < vel.y) {
                        if (vel.x != 0 && t < 0) {
                            float xforce = (float) Math.cos(Math.atan(slope));
                            if (xforce < .1) {
                                vel.set(0, 0);
                                onCollide(i);
                                return;
                            } else {
                                vel.x *= xforce*xforce;
                            }
                        }
                        vel.x *= .9f;
                        vel.y = slope * vel.x;
                        vel.y += (i.pt.y + (slope * (pos.x - i.pt.x)))-pos.y-1;
                        onCollide(i);
                    } else if (i.opt == 1) {
                        vel.y = 0;
                        onCollide(i);
                    }
                }
            }
        }
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
        }
    }
    public void interact(Unit p){
    }
    @Override
    public String toString(){
        return name + "@" + seed + "@" + pos;
    }
}
