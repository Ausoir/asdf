package com.ga.unit;

import com.ga.game.Texture;
import com.ga.map.Drawable;
import com.ga.tool.P;

import java.awt.*;

/**
 * Created by Ga on 5/6/2016.
 */
public class Thing implements Drawable {
    public Texture image;
    public P pos;
    protected int sizeRadius;
    public String name;
    int depth;
    public int dir = 1;
    public float rotation = 0;
    public Thing(String name){
        this.name = name;
        if (name != null) {
            setImage(name);
        } else {
            image = new Texture();
            image.visible = false;
        }
        pos = new P();
    }
    public void setImage(String p){
        image = new Texture(p);
    }
    public void rotate(P p){
        rotation = ((float) Math.atan2(p.y,p.x));
    }
    public Thing(String name, P pt, int depth){
        this(name);
        this.depth = depth;
        pos = pt;
        Updater.addQueue.add(this);
    }
    public boolean isFacing(P p){
        return (pos.x > p.x && dir == -1) || (pos.x < p.x && dir == 1);
    }
    public void draw(Graphics2D g, P offset){
        image.draw(g, pos.x+offset.x, pos.y+offset.y, rotation, dir);
    }
    public void draw(Graphics2D g){
        draw(g, P.getZero());
    }

    @Override
    public int getDepth() {
        return depth;
    }
    public void setDepth(int depth) {
        this.depth = depth;
        if (Updater.drawee.remove(this)){
            Updater.addDrawee(this);
        }
    }

    public void remove(){
        Updater.remove(this);
    }
    public String toString(){
        return name + "@" + pos;
    }
}