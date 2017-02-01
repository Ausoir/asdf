package com.ga.tool;

import com.ga.map.Global;

import java.awt.*;

/**
 * Created by Ga on 7/26/2016.
 */
public class Line {
    public static int MAXSLOPE = 2;
    public P pt;
    public P ptd;
    public int next = -1;
    public int opt = 0; //0:normal,1:ceiling,2:tp,3:rope
    public Line(){
        pt = new P();
        ptd = new P();
    }
    public Line(float x, float y, float x1, float y1){
        pt = new P(x,y);
        ptd = new P(x1,y1);
    }
    public Line(float x, float y, float x1, float y1, int next, int opt){
        this(x,y,x1,y1);
        this.next = next;
        this.opt = opt;
    }
    public Line(P p1, P p2){
        pt = p1;
        ptd = p2;
    }
    public boolean isLand(){
        return opt != -1 && opt != 1;
    }
    public Line getAdd(P p){
        return new Line(pt.getAdd(p), ptd.getAdd(p));
    }
    public boolean clockwise(){
        return pt.x <= ptd.x;
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
        float x = Global.rel.x, y = Global.rel.y;
        g.drawLine((int) (offset.x+pt.x+x), (int) (offset.y+pt.y+y), (int) (offset.x+ptd.x+x), (int) (offset.y+ptd.y+y));
    }
    public String toString(){
        return pt + "," + ptd + "," + next + "," + opt;
    }
}
