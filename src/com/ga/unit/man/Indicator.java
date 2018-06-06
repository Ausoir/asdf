package com.ga.unit.man;

import com.ga.map.Drawable;
import com.ga.map.Global;
import com.ga.unit.skill.Stat;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * Created by ga on 4/23/17. asd
 */
public class Indicator extends Drawable {
    private Player host;
    private static Font font = new Font("Helvetica",Font.BOLD,12);
    private Color[] oval = new Color[]{
            new Color(100, 20, 25, 0),
            new Color(19, 100, 43, 0),
            new Color(10, 40, 100, 0),
    };
    private Color sg = new Color(58, 60, 60,0);
    private Color sg2 = new Color(58, 60, 60,0);
    private float[] fade = new float[oval.length];
    private Stat[] measuree = new Stat[oval.length];
    private Color[][] ovalFade = new Color[oval.length][15];
    private int time;
    Indicator(Player host){
        this.host = host;
        host.map.updater.addQueue.add(this);
        for (int i=0;i<measuree.length;i++) measuree[i] = new Stat(1);
        for (int i=0;i<oval.length;i++) {
            for (int ii = 14; ii >= 0; ii--) {
                ovalFade[i][ii] = new Color(oval[i].getRed(), oval[i].getGreen(), oval[i].getBlue(), ii * 16);
            }
        }
        setDepth(15);
    }
    void remove(){
        host.map.updater.remove(this);
    }
    void setMeasuree(int index, Stat p){
        measuree[index] = p;
    }
    public void draw(Graphics2D g) {
        int x = (int) (host.pos.x + Global.rel.x);
        int y = (int) (host.pos.y + Global.rel.y);
        int cd = time-Global.time;
        if (cd >= 0){
            int width = g.getFontMetrics(font).stringWidth(cd+"")/2;
            g.setFont(font);
            g.setColor(sg2);
            g.drawString(cd+"",x-width-1,y+3);
            g.setColor(sg);
            g.drawString(cd+"",x-width,y+4);
        }
        for (int i=0;i<oval.length;i++){
            g.setColor(oval[i]);
            int radius = 18 - (i*4);
            g.fill(makeRing(x, y, radius, (int) (measuree[i].getPercentage()*360), 4));
        }
    }
    private static Shape makeRing(int x, int y, int radius, int angle, int thickness)
    {
        int diameter = radius*2, dithickness = thickness*2;
        Arc2D outer = new Arc2D.Float(x-radius, y-radius, diameter, diameter, 0, angle, Arc2D.PIE);
        Ellipse2D inner = new Ellipse2D.Float(x-radius+thickness, y-radius+thickness, diameter-dithickness, diameter-dithickness);
        Area area = new Area(outer);
        area.subtract(new Area(inner));
        return area;
    }
    void show(int index, float strength){
        fade[index] = strength;
    }
    void showCd(int time){
        this.time = time;
        sg = new Color(40,34,44,200);
        sg2 = new Color(120,120,140,200);
    }
    void update(){
        for (int i=0;i<fade.length;i++){
            if (fade[i] > 0) {
                fade[i] = Math.max(0, fade[i] - .01f);
                float t = Math.min(.99f, fade[i]);
                oval[i] = ovalFade[i][(int) (t * ovalFade[0].length)];
            }
        }
    }
}
