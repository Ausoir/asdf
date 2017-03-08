package com.ga.map;

import com.ga.tool.P;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Ga on 5/6/2016.
 */
public class Picture extends Texture {
    public int sizeRadius = 10;
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
    public void setSize(float x, float y){
        size = new P(x,y);
        sizeRadius = (int) size.getMult(.5f,.5f).getDist();
    }
    public void draw(Graphics2D g, float x, float y, float rot, int dir, float sizeX, float sizeY){
        if (visible) {
            float xMid = size.x*dir/2;
            float yMid = size.y/2;
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