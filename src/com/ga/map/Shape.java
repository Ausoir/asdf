package com.ga.map;

import com.ga.game.Settings;
import com.ga.tool.Line;
import com.ga.tool.P;

import java.awt.*;
import java.util.ArrayList;

public class Shape extends Drawable {
    static final float ZOOM = 1f;
    static final BasicStroke stroke = new BasicStroke(1.5f);
    Polygon polygon;
    ArrayList<Line> lines = new ArrayList<>();
    protected P pos;
    P finalSize;
    Map map;
    private int length = 0; //polygon size is always multiples of 4
    int offset = 0;
    Shape(Map map, float depth){
        this.map = map;
        setDepth(depth);
        polygon = new Polygon();
    }
    public void start(){
        map.updater.addQueue.add(this);
    }
    void addLine(Line p){
        lines.add(p);
        p.depth = depth;
    }
    public void addPoint(float x, float y){
        polygon.addPoint((int)(x/ZOOM), (int)(y/ZOOM));
        length++;
    }
    public void render(){
        int lowX = polygon.xpoints[0], lowY = polygon.ypoints[0], highX = polygon.xpoints[0], highY = polygon.ypoints[0];
        for (int i=0;i<length;i++){
            polygon.ypoints[i] += offset;
            if (polygon.xpoints[i] < lowX) lowX = polygon.xpoints[i];
            if (polygon.ypoints[i] < lowY) lowY = polygon.ypoints[i];
            if (polygon.xpoints[i] > highX) highX = polygon.xpoints[i];
            if (polygon.ypoints[i] > highY) highY = polygon.ypoints[i];
        }
        for (int i=0;i<polygon.xpoints.length;i++) {
            polygon.xpoints[i] -= lowX;
            polygon.ypoints[i] -= lowY;
        }
        setSize(highX-lowX, highY-lowY);
        pos = new P(lowX, lowY);
        pos.mult(ZOOM);
    }
    void setSize(float x, float y){
        finalSize = new P(x*ZOOM, y*ZOOM);
    }
    boolean checkDraw(Graphics2D g){
        return (pos.x > Global.camera.x- Settings.centerX-finalSize.x && pos.y > Global.camera.y-Settings.centerY-finalSize.y
                && pos.x < Global.camera.x+Settings.centerX && pos.y < Global.camera.y+Settings.centerY);
    }
    public void setDepth(float depth) {
        super.setDepth(depth);
        for (Line i : lines) i.depth = depth;
    }
    public void remove(){
        map.updater.remove(this);
    }
}