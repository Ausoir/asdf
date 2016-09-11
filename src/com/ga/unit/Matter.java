package com.ga.unit;

import com.ga.game.Settings;
import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.tool.P;

/**
 * Created by Ga on 8/12/2016.
 */
public class Matter extends Thing {
    public boolean gravity = true;
    public Faction faction = Faction.Hostile;
    public P vel = new P(0, 0);
    public boolean tangible = true;
    public Matter(String name, int seed, P pt, int depth) {
        super(name, seed, pt, depth);
    }
    public boolean hit(Matter hitter){
        return true;
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
                hit(p);
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
                if (i.opt != -1) onCollide(i);
            }
        }
    }
    public void onCollide(Line l){
        float slope = l.getSlope();
        vel.y = (slope * vel.x * 10000 - 5) / 10000f;
    }
    public void update(){
        if (Global.mapEdge(pos.getAdd(vel))) vel.set(0,0);
        collide();
        pos.add(vel);
        if (gravity) {
            vel.y += Settings.gravity;
            if (vel.y > Settings.gravityMax) vel.y = Settings.gravityMax;
        }
    }
}
