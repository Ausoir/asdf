package com.ga.map;

import com.ga.tool.P;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Ga on 5/6/2016.
 */
public class Picture extends Texture {
    public int sizeRadius = 10;
    private P halfSize;
    private int type = 1; //1:center,2:bottom
    public Picture(){
        super();
    }
    private Picture(BufferedImage image){
        super(image);
        setSize(size.x,size.y);
    }
    public Picture(String name){
        this(load(name));
    }
    public Picture(String name, int index){
        this(getTexture(load(name),32,64,index,0));
    }
    public BufferedImage getImage(){
        return image;
    }
    public P getHalfSize(){
        return halfSize;
    }
    public void setSize(float x, float y){
        size = new P(x,y);
        sizeRadius = (int) size.getMult(.5f,.5f).getDist();
        setCenterType(type);
    }
    public int getCenterType(){
        return type;
    }
    public void setCenterType(int p){
        type = p;
        if (type == 1) halfSize = new P(size.x/2, size.y/2);
        else if (type == 2) halfSize = new P(size.x/2, size.y-4);
        else if (type == 3) halfSize = new P(4, size.y/2);
    }
    public void draw(Graphics2D g, float x, float y, float rot, int dir, float sizeX, float sizeY){
        if (visible) {
            float xMid = halfSize.x*dir;
            float yMid = halfSize.y;
            float rx = x + Global.rel.x;
            float ry = y + Global.rel.y;
            x = rx - xMid;
            y = ry - yMid;
            g.rotate(rot, rx, ry);
            drawImage(g, (int) x, (int) y, (int) sizeX*dir, (int) sizeY);
            g.rotate(-rot, rx, ry);
        }
    }
    protected void drawImage(Graphics2D g, int x, int y, int sizeX, int sizeY){
        g.drawImage(image, x, y, sizeX, sizeY, null);
    }
    public void draw(Graphics2D g, float x, float y, float rot, int dir){
        draw(g,x,y,rot,dir,size.x,size.y);
    }
}