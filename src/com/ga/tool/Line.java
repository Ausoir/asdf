package com.ga.tool;

import com.ga.map.Global;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Created by Ga on 7/26/2016.
 */
public class Line {
    public P pt;
    public P ptd;
    public int next = -1;
    public int opt = 0; //0:normal,1:ceiling,2:tp
    public Line(){
        pt = new P();
        ptd = new P();
    }
    public Line(int x, int y, int x1, int y1){
        pt = new P(x,y);
        ptd = new P(x1,y1);
    }
    public Line(int x, int y, int x1, int y1, int next, int opt){
        this(x,y,x1,y1);
        this.next = next;
        this.opt = opt;
    }
    public Line(P p1, P p2){
        pt = p1;
        ptd = p2;
    }
    public Line getAdd(P p){
        return new Line(pt.getAdd(p), ptd.getAdd(p));
    }
    public float getAtan(){
        float r = (float) Math.atan2(ptd.y-pt.y,ptd.x-pt.x);
        return r;
    }
    public P getAtan(int mag){
        float r = (float) Math.atan2(ptd.y-pt.y,ptd.x-pt.x);
        return new P(Global.cos(r)*mag,Global.sin(r)*mag);
    }
    public float getLength(){
        return ptd.getSubtract(pt).getDist();
    }
    public boolean lineCross(Line i2){
        boolean a = i2.pt.x-pt.x >= 0, b = i2.pt.x-ptd.x > 0, c = i2.ptd.x-pt.x >= 0, d = i2.ptd.x-ptd.x > 0,
                e = i2.pt.y-pt.y >= 0, f = i2.pt.y-ptd.y > 0, g = i2.ptd.y-pt.y >= 0, h = i2.ptd.y-ptd.y > 0;
        if ((a != b || c != d || a != c || b != d) && (e != f || g != h || e != g || f != h))
            if (cross(i2.pt) != cross(i2.ptd)) return true;
        return false;
    }
    public boolean cross(P p){
        return ((p.x-pt.x)*((ptd.y-pt.y)/(ptd.x-pt.x)))-(p.y-pt.y) >= 0;
    }
    public float getSlope(){
        return (ptd.y-pt.y)/(ptd.x-pt.x);
    }
    public void draw(Graphics2D g, P offset){
        g.setTransform(AffineTransform.getTranslateInstance(Global.getRelX(),Global.getRelY()));
        g.drawLine((int) (offset.x+pt.x), (int) (offset.y+pt.y), (int) (offset.x+ptd.x), (int) (offset.y+ptd.y));
    }
    public String toString(){
        return pt + "," + ptd + "," + next + "," + opt;
    }
}
