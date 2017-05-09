package com.ga.map;

import com.ga.tool.Line;
import com.ga.tool.P;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Created by Ga on 1/10/2017. xhfsdxghf
 */
public class Terrain extends ShadedPicture implements Drawable{
    private int depth;
    private Polygon polygon;
    private TexturePaint imageTexture;
    private ArrayList<Line> lines = new ArrayList<>();
    protected P pos;
    private int length = 0; //polygon size is always multiples of 4
    public Terrain(String texture, Shader shader, int depth){
        super(texture, shader);
        setDepth(depth);
        polygon = new Polygon();
        imageTexture = new TexturePaint(image, new Rectangle2D.Double(0,0,size.x,size.y));
        Updater.add(this);
    }
    static int toTerrainDepth(int depth){
        return 10+(depth*3);
    }
    void addLine(Line p){
        lines.add(p);
        p.depth = depth;
    }
    public void addPoint(float x, float y){
        polygon.addPoint((int)x, (int)y);
        length++;
    }
    @Override
    public void shade(int param){
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
        for (int i=0;i<length;i++){
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

        if (lines.size() > 0){
            image = null; //todo: turn on cache
//            image = Texture.load(Global.player.cmap + "_" + lines.get(0).next);
            if (image == null) {
                image = new BufferedImage(highX - lowX, highY - lowY, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = image.createGraphics();
                g.setPaint(imageTexture);
                g.fill(polygon);
                shade(0);
//                Texture.newImage("map/", Global.player.cmap + "_" + lines.get(0).next, image);
            }
        } else { //rope
            image = new BufferedImage(highX - lowX, highY - lowY, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.setPaint(imageTexture);
            g.fill(polygon);
        }
    }
    public void draw(Graphics2D g){
        if (visible) {
            P t = new P(pos.x+Global.rel.x, pos.y+Global.rel.y);
            g.drawImage(image,(int)t.x,(int)t.y,(int)size.x,(int)size.y,null);
//            for (Line i : lines) g.drawLine((int)(t.x+i.pt.x),(int)(t.y+i.pt.y),(int)(t.x+i.ptd.x),(int)(t.y+i.ptd.y));
        }
    }

    @Override
    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
        Updater.resetDepth(this);
        for (Line i : lines) i.depth = depth;
    }
}