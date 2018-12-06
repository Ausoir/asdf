package com.ga.map;

import com.ga.tool.Line;
import com.ga.tool.P;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Created by Ga on 1/10/2017. xhfsdxghf
 */
public class Terrain extends Shape {
    private ShadedPicture image;
    private TexturePaint imageTexture;
    private boolean outline = false;
    public Terrain(Map map, String texture, Shader shader, float depth){
        super(map, depth);
        image = new ShadedPicture(null, Picture.load(texture,null), shader){
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
        imageTexture = new TexturePaint(image.image, new Rectangle2D.Double(0, 0, image.getSize().x, image.getSize().y));
        offset = shader.getOffset();
        if (shader != Shader.None) outline = false;
    }
    public void start(){
        map.updater.addQueue.add(this);
    }
    public void render(){
        super.render();
        P size = image.getSize();
        if (size.x > 0 && size.y > 0) {
            image.image = new BufferedImage((int) size.x, (int) size.y, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.image.createGraphics();
            g.setPaint(imageTexture);
            g.fill(polygon);
            image.shade(0);
        } else {
            Global.p("Terrain size violation at "+pos.toString());
        }
    }
    void setSize(float x, float y){
        super.setSize(x, y);
        image.setSize(x, y);
    }
    public void draw(Graphics2D g){
        if (checkDraw(g)) {
            int x = (int)(pos.x+Global.rel.x), y = (int)(pos.y+Global.rel.y);
            g.drawImage(image.image,x,y,finalSize.x(),finalSize.y(),null);
            if (outline) {
                g.setColor(Color.BLACK);
                g.setStroke(stroke);
                polygon.translate(x, y);
                g.drawPolygon(polygon);
                polygon.translate(-x, -y);
            }
        }
    }
    public void setDepth(float depth) {
        super.setDepth(depth);
        for (Line i : lines) i.depth = depth;
    }
    public void remove(){
        map.updater.remove(this);
    }
}