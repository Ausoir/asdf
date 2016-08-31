package com.ga.unit;

import com.ga.game.Settings;
import com.ga.game.Texture;
import com.ga.map.Drawable;
import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.unit.item.Item;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Created by Ga on 5/6/2016.
 */
public class Thing implements Drawable {
    public int seed = 0;
    public Random rand;
    public BufferedImage image;
    private boolean visible = true;
    public P pos;
    public P size;
    public String name;
    int depth;
    public int dir = 1;
    public float rotation = 0;
    public Thing(String name, int seed){
        this.name = name;
        if (seed == -1) this.seed = Global.getSeed();
        else this.seed = seed;
        rand = new Random(this.seed);
        if (name != null) {
            String dir = "";
            if (this instanceof Man) dir = "unit/";
            else if (this instanceof Item) dir = "item/";
            setImage(Texture.load(dir + name + ".png"));
        }
    }
    protected void setImage(BufferedImage image){
        this.image = image;
        size = new P(this.image.getWidth(), this.image.getHeight());
    }
    public void rotate(P p){
        rotation = ((float) Math.atan2(p.y,p.x));
    }
    protected Thing(String name, int seed, P pt, int depth){
        this(name, seed);
        this.depth = depth;
        pos = pt;
        Updater.addQueue.add(this);
    }
    public boolean isFacing(P p){
        return (pos.x > p.x && dir == -1) || (pos.x < p.x && dir == 1);
    }
    public void draw(Graphics2D g, P offset){
        if (visible) {
            P t = Global.getRelPos(pos.x - (size.x / 2), pos.y - (size.y / 2));
            t.add(offset.x + (dir < 0 ? size.x : 0), offset.y);
            P r = t.getAdd(dir < 0 ? -size.x / 2 : size.x / 2, size.y / 2);
            float rot = rotation;
            g.rotate(rot, r.x, r.y);
            if (t.x > -150 && t.y > -100 && t.x < Settings.screenX + 150 && t.y < Settings.screenY + 100)
                g.drawImage(image, (int) t.x, (int) t.y, (int) size.x * dir, (int) size.y, null);
            g.rotate(-rot, r.x, r.y);
        }
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
        return name + "@" + seed + "@" + pos;
    }
}