package com.ga.game;

import com.ga.map.Global;
import com.ga.tool.P;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Created by Ga on 1/10/2017.
 */
public class Form extends Texture {
    private Polygon polygon;
    private TexturePaint imageTexture;
    public P pos;
    public Form(String p){
        super(p);
        polygon = new Polygon();
        imageTexture = new TexturePaint(image, new Rectangle2D.Double(0,0,size.x,size.y));
    }
    public void addPoint(float x, float y){
        addPoint((int)x, (int)y);
    }
    public void addPoint(int x, int y){
        polygon.addPoint(x, y);
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
//        g.setTransform(AffineTransform.getTranslateInstance(Global.getRelX(),Global.getRelY()));
        g.setPaint(imageTexture);
        g.fill(polygon);
    }
    public void draw(Graphics2D g){
        if (visible) {
            g.setTransform(AffineTransform.getTranslateInstance(Global.getRelX(),Global.getRelY()));
            g.drawImage(image,(int)pos.x,(int)pos.y,(int)size.x,(int)size.y,null);
        }
    }
}
