package com.ga.unit;

import com.ga.map.Drawable;
import com.ga.map.Map;
import com.ga.map.Picture;
import com.ga.tool.P;

import java.awt.*;
import java.util.Random;

/**
 * Created by Ga on 5/6/2016. thing
 */
public class Thing extends Drawable {
    public Map map;
    public Random rand;
    public int seed;
    public Picture image;
    public P pos;
    public String name, imageName;
    public int dir = 1;
    public float rotation = 0;
    public boolean save = true;
    public Thing(Map map, String name, String imageName, int seed, float depth){
        this.map = map;
        this.name = name;
        this.imageName = imageName;
        this.seed = seed;
        pos = new P();
        rand = new Random(seed);
        image = new Picture(rand);
        setDepth(depth);
        setImage(imageName);
    }
    public Thing start(){
        map.updater.addQueue.add(this);
        return this;
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

    @Override
    public void draw(Graphics2D g){
        image.draw(g, pos.x, pos.y, rotation, dir);
    }

    public void remove(){
        map.updater.remove(this);
    }
    public String toString(){
        return name + "," + pos;
    }
}