package com.ga.map;

import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Updater;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Created by Ga on 1/10/2017.
 */
public class Terrain extends ShadedPicture implements Drawable{
    int depth; //7,10,13
    private Polygon polygon;
    private TexturePaint imageTexture;
    private ArrayList<Line> lines = new ArrayList<>();
    protected P pos;
    public Terrain(String texture, Shader shader, int depth){
        super(texture, shader);
        setDepth(10+(depth*3));
        polygon = new Polygon();
        imageTexture = new TexturePaint(image, new Rectangle2D.Double(0,0,size.x,size.y));
    }
    public void addLine(Line p){
        lines.add(p);
        p.depth = depth;
    }
    public void addPoint(float x, float y){
        addPoint((int)x, (int)y);
    }
    private void addPoint(int x, int y){
        polygon.addPoint(x, y);
    }
    @Override
    public void shade(){
        if (shader != null) {
            for (Line i : lines) {
                int origX = (int) (i.pt.x - pos.x);
                int origY = (int) (i.pt.y - pos.y);
                int x2 = (int) (i.ptd.x - pos.x);
                int y2 = (int) (i.ptd.y - pos.x);
                shader.render(image, origX, x2, origY, y2, i.getSlope());
            }
        }
    }
    public void render(){
        int lowX = polygon.xpoints[0], lowY = polygon.ypoints[0], highX = polygon.xpoints[0], highY = polygon.ypoints[0];
        for (int i=0;i<polygon.xpoints.length;i++){
            if (polygon.xpoints[i] < lowX) lowX = polygon.xpoints[i];
            if (polygon.ypoints[i] < lowY) lowY = polygon.ypoints[i];
            if (polygon.xpoints[i] > highX) highX = polygon.xpoints[i];
            if (polygon.ypoints[i] > highY) highY = polygon.ypoints[i];
        }
        for (int i=0;i<polygon.xpoints.length;i++) {
            polygon.xpoints[i] -= lowX;
            polygon.ypoints[i] -= lowY;
        }
        size.x = highX-lowX;
        size.y = highY-lowY;
        pos = new P(lowX, lowY);
        image = new BufferedImage(highX-lowX, highY-lowY, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setPaint(imageTexture);
        g.fill(polygon);
        shade();
    }
    public void draw(Graphics2D g){
        if (visible) {
            P t = new P(Global.rel.x+pos.x, Global.rel.y+pos.y);
            g.drawImage(image,(int)t.x,(int)t.y,(int)size.x,(int)size.y,null);
        }
    }

    @Override
    public int getDepth() {
        return Global.map.isIndoors()?10:depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
        Updater.resetDepth(this);
        for (Line i : lines) i.depth = depth;
    }
}