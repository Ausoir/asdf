package com.ga.unit;

import com.ga.game.Observer;
import com.ga.map.Drawable;
import com.ga.map.Picture;
import com.ga.map.Updater;
import com.ga.tool.P;

import java.awt.*;

/**
 * Created by Ga on 5/6/2016.
 */
public class Thing implements Drawable, Observer {
    public Picture image;
    public P pos;
    public String name;
    protected int depth; //5:Object, 8:Corpse, 10:Unit
    public int dir = 1;
    public float rotation = 0;
    public boolean save = true;
    public Thing(String name){
        this.name = name;
        if (name != null) {
            setImage(name);
        } else {
            image = new Picture();
            image.visible = false;
            image.setSize(0,0);
        }
        pos = new P();
    }
    public void setImage(String p){
        image = new Picture(p);
    }
    public void rotateTo(P p){
        rotation = ((float) Math.atan2(p.y,p.x));
    }
    public Thing(String name, int depth){
        this(name);
        this.depth = depth;
        Updater.add(this);
    }
    public boolean isFacing(P p){
        return (pos.x > p.x && dir == -1) || (pos.x < p.x && dir == 1);
    }
    public void draw(Graphics2D g){
        image.draw(g, pos.x, pos.y, rotation, dir);
    }

    @Override
    public int getDepth() {
        return depth;
    }
    public void setDepth(int depth) {
        if (this.depth != depth) {
            this.depth = depth;
            Updater.resetDepth(this);
        }
    }

    public void remove(){
        Updater.remove(this);
    }
    public String toString(){
        if (!save) return "";
        return name + "@" + pos;
    }
}