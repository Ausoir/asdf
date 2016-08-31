package com.ga.unit;

import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.tool.P;

import java.awt.*;

/**
 * Created by Ga on 8/23/2016.
 */
class Shadow extends Unit {
    private static final P size = new P(50,20);
    private P hostPos;
    Shadow(P pt) {
        super(null, 0, new P(), 9);
        hostPos = pt;
        gravity = false;
        pos.set(hostPos);
    }
    @Override
    public boolean checkHit(Unit hitter, int range, int force){
        return false;
    }
    @Override
    public boolean checkHit(Unit hitter, Line p, int force){
        return false;
    }
    @Override
    public void collide(){
        Line pp = new Line(hostPos,hostPos.getAdd(0,150));
        for (Line i : Global.map.land){
            if (i.lineCross(pp)){
                onCollide(i);
            }
        }
    }
    @Override
    public void onCollide(Line l){
        pos.y = l.pt.y+(l.getSlope() * (hostPos.x-l.pt.x));
    }
    public void draw(Graphics2D g){
        float mag = Global.clamp((300-(pos.y-hostPos.y))/300,0.1f,1);
        P size = this.size.getMult(mag,mag);
        g.setColor(new Color(0,0,0,150));
        P p = Global.getRelPos(hostPos.x-(size.x/2),pos.y-(size.y/3));
        g.fillOval((int) p.x, (int) p.y, (int) (size.x), (int) (size.y));
    }
    @Override
    public String toString(){
        return "";
    }
}
