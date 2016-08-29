package com.ga.unit;

import com.ga.game.Settings;
import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.tool.P;

/**
 * Created by Ga on 8/12/2016.
 */
public class Unit extends Thing {
    public boolean gravity = true;
    public int faction = 2;
    public P vel = new P(0, 0);
    public Unit(String name, int seed, P pt, int depth) {
        super(name, seed, pt, depth);
    }
    public void hit(Unit hitter, int dmg){
    }
    public boolean checkHit(Unit hitter, Line p, int force){
        if (faction != hitter.faction) {
            float s = (size.x*size.x)+(size.y+size.y);
            if ((Math.pow(p.pt.x - pos.x, 2) + Math.pow(p.pt.y - pos.y, 2))-s < 0) {
                hit(hitter, force);
                return true;
            }
        }
        return false;
    }
    public void collide(){
        P vp = pos.getAdd(vel);
        Line pp = new Line(pos,vp);
        for (Line i : Global.map.land){
            if (i.lineCross(pp)){
                onCollide(i);
            }
        }
    }
    public void onCollide(Line l){
        float slope = l.getSlope();
        vel.y = (slope * vel.x * 10000 - 5) / 10000f;
    }
    public void update(){
        collide();
        pos.add(vel);
        if (gravity) {
            vel.y += Settings.gravity;
            if (vel.y > Settings.gravityMax) vel.y = Settings.gravityMax;
        }
    }
}
