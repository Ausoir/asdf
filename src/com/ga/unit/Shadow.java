package com.ga.unit;

import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.tool.P;

import java.awt.*;

/**
 * Created by Ga on 8/23/2016.
 */
public class Shadow extends Thing {
    protected P shadowSize = new P(50,6);
    public P offset = new P(0,0);
    private Unit host;
    Shadow(Unit p) {
        super(null, new P(), 9);
        host = p;
        pos.set(host.pos);
    }
    public float collide(){
        Line pp = new Line(host.pos,host.pos.getAdd(0,300));
        float lowest = pos.y+300, current;
        float result = 0;
        for (Line i : Global.map.land){
            if (i.lineCross(pp)){
                current = i.pt.y+(i.getSlope() * (host.pos.x-i.pt.x));
                if (current < lowest){
                    lowest = current;
                    result = i.getSlope();
                }
            }
        }
        pos.y = lowest;
        return result;
    }
    public void draw(Graphics2D g){
        float r = (float) Math.atan(collide());
        float mag = Global.clamp((300-(pos.y-host.pos.y))/300,0.1f,1);
        P size = this.shadowSize.getMult(mag,mag);
        g.setColor(new Color(0,0,0,150));
        P p = Global.getRelPos(host.pos.x-(size.x/2),pos.y-(size.y/3));
        int x = (int) (p.x+(offset.x*host.dir));
        int y = (int) (p.y+offset.y);
        int rotX = (int) (x + size.x/2);
        int rotY = (int) (y + size.y/2);
        g.rotate(r, rotX, rotY);
        g.fillOval(x, y, (int) (size.x), (int) (size.y));
        g.rotate(-r, rotX, rotY);
    }
    @Override
    public String toString(){
        return "";
    }
}
