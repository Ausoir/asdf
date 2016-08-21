package com.ga.tool;

import com.ga.game.Settings;
import com.ga.map.Global;

import java.awt.*;

/**
 * Created by Ga on 7/26/2016.
 */
public class Line {
    public P pt;
    public P ptd;
    public int opt = 0; //0:normal,1:ceiling,2:tp
    public Line(){
        pt = new P();
        ptd = new P();
    }
    public Line(int x, int y, int x1, int y1){
        pt = new P(x,y);
        ptd = new P(x1,y1);
    }
    public Line(int x, int y, int x1, int y1, int opt){
        this(x,y,x1,y1);
        this.opt = opt;
    }
    public Line(P p1, P p2){
        pt = p1;
        ptd = p2;
    }
    public Line getAdd(P p){
        return new Line(pt.getAdd(p), ptd.getAdd(p));
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
    public void draw(Graphics2D g){
        draw(g, new P(), 1);
    }
    public void draw(Graphics2D g, P offset, int dir){
        P t = Global.getRelPos(offset.x+(pt.x*dir),offset.y+pt.y);
        P td = Global.getRelPos(offset.x+(ptd.x*dir),offset.y+ptd.y);
        if ((t.x > -100 && t.y > -100 && t.x < Settings.screenX + 100 && t.y < Settings.screenY + 100)
            || (td.x > -100 && td.y > -100 && td.x < Settings.screenX + 100 && td.y < Settings.screenY + 100))
            g.drawLine((int)t.x, (int)t.y, (int)td.x, (int)td.y);
    }
    public String toString(){
        return pt + "," + ptd + "," + opt;
    }
}
