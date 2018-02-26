 package com.ga.map;

import com.ga.game.Settings;
import com.ga.tool.Line;
import com.ga.tool.P;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Created by Ga on 1/10/2017. xhfsdxghf
 */
public class Terrain extends Drawable {
    private static final float ZOOM = 1.5f;
    private ShadedPicture image;
    private Polygon polygon;
    private TexturePaint imageTexture;
    private Color color;
    private ArrayList<Line> lines = new ArrayList<>();
    protected P pos;
    private int length = 0; //polygon size is always multiples of 4
    private int offset = 0;
    private P finalSize;
    Map map;
    public Terrain(Map map, String texture, Shader shader, float depth){
        image = new ShadedPicture(null, texture.startsWith("_")?null:texture, shader){
            @Override
            public void shade(int p){
                if (shader != null) {
//                    shader.renderLines(lines, pos, ZOOM, image); //slow
                    for (Line i : lines) {
                        int origX = (int) ((i.pt.x - pos.x)/ZOOM);
                        int origY = (int) ((i.pt.y - pos.y)/ZOOM);
                        int x2 = (int) ((i.ptd.x - pos.x)/ZOOM);
                        int y2 = (int) ((i.ptd.y - pos.y)/ZOOM);
                        shader.render(image, origX, x2, origY, y2, i.getSlope());
                    }
                }
            }
        };
        this.map = map;
        setDepth(3+(depth*.1f));
        polygon = new Polygon();
        if (image.image == null){
            String[] data = texture.substring(1).split(",");
            this.color = new Color(Integer.parseInt(data[0]),Integer.parseInt(data[1]),Integer.parseInt(data[2]),Integer.parseInt(data[3]));
        } else {
            imageTexture = new TexturePaint(image.image, new Rectangle2D.Double(0, 0, image.getSize().x, image.getSize().y));
        }
        if (shader == Shader.Grass) offset = -2;
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
        P size = image.getSize();
        image.image = new BufferedImage((int)size.x, (int)size.y, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.image.createGraphics();
        if (imageTexture != null) g.setPaint(imageTexture);
        else g.setColor(color);
        g.fill(polygon);
        image.shade(0);
    }
    private void setSize(float x, float y){
        image.setSize(x, y);
        finalSize = new P(x*ZOOM, y*ZOOM);
    }
    public void draw(Graphics2D g){
        if (image.visible && pos.x > Global.camera.x-Settings.centerX-finalSize.x && pos.y > Global.camera.y-Settings.centerY-finalSize.y
                && pos.x < Global.camera.x+Settings.centerX && pos.y < Global.camera.y+Settings.centerY) {
            g.drawImage(image.image,(int)(pos.x+Global.rel.x),(int) (pos.y+Global.rel.y),finalSize.x(),finalSize.y(),null);
//            for (Line i : lines) g.drawLine((int)(t.x+i.pt.x),(int)(t.y+i.pt.y),(int)(t.x+i.ptd.x),(int)(t.y+i.ptd.y));
        }
    }

    public void setDepth(float depth) {
        super.setDepth(depth);
        for (Line i : lines) i.depth = depth;
    }
}