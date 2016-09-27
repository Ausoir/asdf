package com.ga.unit;

import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.tool.P;

import java.awt.*;

/**
 * Created by Ga on 8/23/2016.
 */
class Shadow extends Thing {
    private static final P size = new P(50,20);
    private P hostPos;
    Shadow(P pt) {
        super(null, null, new P(), 9);
        setHostPos(pt);
    }
    public void setHostPos(P pt){
        hostPos = pt;
        pos.set(hostPos);
    }
    public void collide(){
        Line pp = new Line(hostPos,hostPos.getAdd(0,300));
        float lowest = pos.y+300, current;
        for (Line i : Global.map.land){
            if (i.lineCross(pp)){
                current = i.pt.y+(i.getSlope() * (hostPos.x-i.pt.x));
                if (current < lowest){
                    lowest = current;
                }
            }
        }
        pos.y = lowest;
    }
    public void draw(Graphics2D g){
        collide();
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
