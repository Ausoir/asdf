package com.ga.tool;

import com.ga.map.Global;

import java.awt.*;

/**
 * Created by Ga on 7/26/2016. asd
 */
public class Line {
    public static float MAXSLOPE = 1.5f;
    public P pt;
    public P ptd;
    public int next = -1;
    public int opt = 0; //0:normal,1:rope,2:impenetrable
    public int depth = 0;
    public Line(String p){
        load(p);
    }
    public void load(String p){
        String[] data = p.split(",");
        float[] intdata = new float[data.length];
        for (int i=0;i<data.length;i++) intdata[i] = Float.parseFloat(data[i]);
        pt = new P(new P(intdata[0], intdata[1]));
        ptd = new P(new P(intdata[2], intdata[3]));
        next = (int) intdata[4];
        opt = (int) intdata[5];
    }
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
    public Line(float x, float y, float x1, float y1, int next, int opt, int depth){
        this(x,y,x1,y1,next,opt);
        this.depth = depth;
    }
    public Line(P p1, P p2){
        pt = p1;
        ptd = p2;
    }
    public Line(P p1, P p2, int next, int opt){
        this(p1, p2);
        this.next = next;
        this.opt = opt;
    }
    public boolean isLand(){
        return opt != -1 && opt != 1;
    }
    public boolean isLandable(){
        return isLand() && clockwise();
    }
    public Line getAdd(P p){
        return new Line(pt.getAdd(p), ptd.getAdd(p));
    }
    public boolean clockwise(){
        return pt.x <= ptd.x;
    }
    public float getAtan(){
        return (float) Math.atan2(ptd.y-pt.y,ptd.x-pt.x);
    }
    public P getAtan(int mag){
        float r = getAtan();
        return new P(Global.cos(r)*mag,Global.sin(r)*mag);
    }
    public float getLength(){
        return ptd.getSubtract(pt).getDist();
    }
    public float getCrossPointX(float y){
        return (y-pt.y)/getSlope()+pt.x;
    }
    public float getCrossPointY(float x){
        return (x-pt.x)*getSlope()+pt.y;
    }
    public boolean lineCross(Line i2){
        boolean a = i2.pt.x-pt.x >= 0, b = i2.pt.x-ptd.x > 0, c = i2.ptd.x-pt.x >= 0, d = i2.ptd.x-ptd.x > 0,
                e = i2.pt.y-pt.y >= 0, f = i2.pt.y-ptd.y > 0, g = i2.ptd.y-pt.y >= 0, h = i2.ptd.y-ptd.y > 0;
        if ((a != b || c != d || a != c) && (e != f || g != h || e != g))
            if (cross(i2.pt) != cross(i2.ptd)) return true;
        return false;
    }
    private boolean cross(P p){
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
