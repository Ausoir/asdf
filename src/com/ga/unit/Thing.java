package com.ga.unit;

import com.ga.game.Settings;
import com.ga.game.Texture;
import com.ga.map.Drawable;
import com.ga.map.Global;
import com.ga.tool.P;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Ga on 5/6/2016.
 */
public class Thing implements Drawable {
    public BufferedImage image;
    public boolean visible = true;
    public P pos;
    public P size;
    public String name;
    public int depth;
    public int dir = 1;
    protected float rotation = 0;
    public Thing(String name){
        this.name = name;
        if (name != null) {
            String dir = "";
            if (this instanceof Man) dir = "unit/";
            else if (this instanceof Item) dir = "item/";
            image = Texture.load(dir + name + ".png");
            size = new P(image.getWidth(), image.getHeight());
        }
    }
    public void rotate(P p){
        rotate((float) Math.atan2(p.y,p.x));
    }
    public void rotate(float p){
        rotation = p;
    }
    public Thing(String name, P pt, int depth){
        this(name);
        this.depth = depth;
        pos = pt;
        Updater.addQueue.add(this);
    }
    public void draw(Graphics2D g, P offset){
        if (image != null && visible) {
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

    public void remove(){
        Updater.remove(this);
    }
    public String toString(){
        return name + "@" + pos;
    }
}