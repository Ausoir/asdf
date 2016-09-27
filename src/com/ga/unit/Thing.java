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
    protected boolean visible = true;
    public P pos;
    public P size;
    protected int sizeRadius;
    public String name;
    int depth;
    public int dir = 1;
    public float rotation = 0;
    public Thing(String dir, String name){
        this.name = name;
        if (dir != null && name != null) {
            setImage(Texture.load(dir + name));
        } else visible = false;
        pos = new P();
    }
    protected void setImage(BufferedImage image){
        this.image = image;
        size = new P(this.image.getWidth(), this.image.getHeight());
        sizeRadius = (int) size.getMult(.5f,.5f).getDist();
    }
    public void rotate(P p){
        rotation = ((float) Math.atan2(p.y,p.x));
    }
    public Thing(String dir, String name, P pt, int depth){
        this(dir, name);
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
            if (t.x > -150 && t.y > -100 && t.x < Settings.screenX + 150 && t.y < Settings.screenY + 100) {
                P r = t.getAdd(dir < 0 ? -size.x / 2 : size.x / 2, size.y / 2);
                float rot = rotation;
                g.rotate(rot, r.x, r.y);
                g.drawImage(image, (int) t.x, (int) t.y, (int) size.x * dir, (int) size.y, null);
                g.rotate(-rot, r.x, r.y);
            }
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
        return name + "@" + pos;
    }
}