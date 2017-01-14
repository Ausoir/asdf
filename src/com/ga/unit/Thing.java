package com.ga.unit;

import com.ga.game.Observer;
import com.ga.game.Picture;
import com.ga.map.Drawable;
import com.ga.tool.P;

import java.awt.*;

/**
 * Created by Ga on 5/6/2016.
 */
public class Thing implements Drawable, Observer {
    public Picture image;
    public P pos;
    protected int sizeRadius;
    public String name;
    int depth; //5:Object, 8:Corpse, 9:Shadow, 10:Unit
    public int dir = 1;
    public float rotation = 0;
    public Thing(String name){
        this.name = name;
        if (name != null) {
            setImage(name);
        } else {
            image = new Picture();
            image.visible = false;
        }
        pos = new P();
    }
    public void setImage(String p){
        image = new Picture(p);
    }
    public void rotate(P p){
        rotation = ((float) Math.atan2(p.y,p.x));
    }
    public Thing(String name, P pt, int depth){
        this(name);
        this.depth = depth;
        pos = pt;
        Updater.add(this);
    }
    public boolean isFacing(P p){
        return (pos.x > p.x && dir == -1) || (pos.x < p.x && dir == 1);
    }
    public void draw(Graphics2D g, P offset){
        image.draw(g, pos.x+offset.x, pos.y+offset.y, rotation, dir);
    }
    public void draw(Graphics2D g){
        image.draw(g, pos.x, pos.y, rotation, dir);
    }

    @Override
    public int getDepth() {
        return depth;
    }
    public void setDepth(int depth) {
        this.depth = depth;
        Updater.resetDepth(this);
    }

    public void remove(){
        Updater.remove(this);
    }
    public String toString(){
        return name + "@" + pos;
    }
}