package com.ga.unit;

import com.ga.game.Observer;
import com.ga.map.Drawable;
import com.ga.map.Map;
import com.ga.map.Picture;
import com.ga.tool.P;

import java.awt.*;
import java.util.Random;

/**
 * Created by Ga on 5/6/2016. thing
 */
public class Thing implements Drawable, Observer {
    public Map map;
    public Random rand;
    public int seed = 0;
    public Picture image;
    public P pos;
    public String name;
    private int depth; //5:Object, 8:Corpse, 10:Unit
    public int dir = Math.random()<.5f?1:-1;
    public float rotation = 0;
    public boolean save = true;
    public Thing(Map map, String name, int seed, int depth){
        this.map = map;
        this.name = name;
        this.seed = seed;
        pos = new P();
        rand = new Random(seed);
        image = new Picture(rand);
        this.depth = depth;
        if (name != null) setImage(name);
    }
    public void start(){
        map.updater.addQueue.add(this);
    }
    public void setImage(String p){
        image.set(p);
    }
    public void rotateTo(P p){
        rotation = ((float) Math.atan2(p.y,p.x));
    }
    boolean isFacing(P p){
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
        this.depth = depth;
        map.updater.resetDepth(this);
    }

    public void remove(){
        map.updater.remove(this);
    }
    public String toString(){
        return name + "@" + pos;
    }
}