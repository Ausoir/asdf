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
    private static final float ZOOM = 1.5f;
    private int depth;
    private Polygon polygon;
    private TexturePaint imageTexture;
    private Color color;
    private ArrayList<Line> lines = new ArrayList<>();
    protected P pos;
    private int length = 0; //polygon size is always multiples of 4
    private P finalSize;
    private Map map;
    public Terrain(Map map, String texture, Shader shader, int depth){
        super(null, texture.startsWith("_")?null:texture, shader);
        this.map = map;
        setDepth(depth);
        polygon = new Polygon();
        map.updater.addQueue.add(this);
        if (image == null){
            String[] data = texture.substring(1).split(",");
            this.color = new Color(Integer.parseInt(data[0]),Integer.parseInt(data[1]),Integer.parseInt(data[2]),Integer.parseInt(data[3]));
        } else {
            imageTexture = new TexturePaint(image, new Rectangle2D.Double(0, 0, getSize().x, getSize().y));
        }
    }
    static int toTerrainDepth(int depth){
        return 10+(depth*3);
    }
    void addLine(Line p){
        lines.add(p);
        p.depth = depth;
    }
    public void addPoint(float x, float y){
        polygon.addPoint((int)(x/ZOOM), (int)(y/ZOOM));
        length++;
    }
    @Override
    public void shade(int param){
        if (shader != null) {
            for (Line i : lines) {
                int origX = (int) ((i.pt.x - pos.x)/ZOOM);
                int origY = (int) ((i.pt.y - pos.y)/ZOOM);
                int x2 = (int) ((i.ptd.x - pos.x)/ZOOM);
                int y2 = (int) ((i.ptd.y - pos.y)/ZOOM);
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
        setSize(highX-lowX, highY-lowY);
        pos = new P(lowX, lowY);
        pos.mult(ZOOM);
        P size = getSize();
        image = new BufferedImage((int)size.x, (int)size.y, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        if (imageTexture != null) g.setPaint(imageTexture);
        else g.setColor(color);
        g.fill(polygon);
        shade(0);
    }
    public void setSize(float x, float y){
        super.setSize(x, y);
        finalSize = new P(x*ZOOM, y*ZOOM);
    }
    public void draw(Graphics2D g){
        if (visible) {
            g.drawImage(image,(int)(pos.x+Global.rel.x), (int) (pos.y+Global.rel.y),finalSize.x(),finalSize.y(),null);
//            for (Line i : lines) g.drawLine((int)(t.x+i.pt.x),(int)(t.y+i.pt.y),(int)(t.x+i.ptd.x),(int)(t.y+i.ptd.y));
        }
    }
    public void draw(Graphics2D g, P offset){
        if (visible) {
            g.drawImage(image,(int)(pos.x+Global.rel.x+offset.x), (int) (pos.y+Global.rel.y+offset.y),finalSize.x(),finalSize.y(),null);
//            for (Line i : lines) g.drawLine((int)(t.x+i.pt.x),(int)(t.y+i.pt.y),(int)(t.x+i.ptd.x),(int)(t.y+i.ptd.y));
        }
    }

    @Override
    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
        map.updater.resetDepth(this);
        for (Line i : lines) i.depth = depth;
    }
}