package com.ga.unit;

import com.ga.game.Settings;
import com.ga.game.Texture;
import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.item.FlyingOrgan;
import com.ga.unit.item.Projectile;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Ga on 7/29/2016.
 */
public class Organ extends Line {
    public Color color = Color.black;
    public Stroke stroke = new BasicStroke(10);
    protected int strokeSize = 10;
    protected String name;
    protected BufferedImage image;
    protected boolean amputated = false;
    public Matter host;
    protected Organ parent;
    protected P size;
    protected int sizeRadius;
    protected int type;
    public int health = 1;
    protected boolean vital = false;

    public Organ(Organ p, boolean tip){
        parent = p;
        if (p != null) {
            if (tip) pt = p.ptd;
            else pt = p.pt;
        } else pt = new P();
        ptd = new P();
    }
    public void setImage(String p){
        name = p;
        image = Texture.load(p);
        size = new P(image.getWidth(), image.getHeight());
        sizeRadius = (int) size.getMult(.5f,.5f).getDist();
    }
    public void setImage(String p, int sub){
        name = p;
        image = Texture.getTexture(Texture.load(p),32,64,sub,0);
        size = new P(image.getWidth(), image.getHeight());
        sizeRadius = (int) size.getMult(.5f,.5f).getDist();
    }
    public void setStyle(Color color, int size){
        this.color = color;
        this.stroke = new BasicStroke(size);
        this.strokeSize = size;
    }
    public void draw(Graphics2D g){
        g.setStroke(stroke);
        g.setColor(color);
        if (image == null) super.draw(g, host.pos);
        else {
            P pos = host.pos.getAdd(pt);
            if (type == 1){
                P t = Global.getRelPos(pos.x - (size.x*host.dir / 2), pos.y - (size.y / 2));
                if (t.x > -150 && t.y > -100 && t.x < Settings.screenX + 150 && t.y < Settings.screenY + 100) {
                    P r = t.getAdd(size.x*host.dir / 2, size.y / 2);
                    float rot = parent.getAtan() + 1.57f;
                    g.rotate(rot, r.x, r.y);
                    g.drawImage(image, (int) t.x, (int) t.y, (int) size.x*host.dir, (int) size.y, null);
                    g.rotate(-rot, r.x, r.y);
                }
            }else if (type == 2){
                int size = strokeSize/2;
                P t = Global.getRelPos(pos.x - (size*host.dir), pos.y);
                if (t.x > -150 && t.y > -100 && t.x < Settings.screenX + 150 && t.y < Settings.screenY + 100) {
                    P r = t.getAdd(size*host.dir, 0);
                    float rot = getAtan() + 1.57f;
                    g.rotate(rot, r.x, r.y);
                    float test = -(getLength() + size);
                    g.drawImage(image, (int) t.x, (int) t.y, strokeSize*host.dir, (int) test, null);
                    g.rotate(-rot, r.x, r.y);
                }
            }
        }
    }
    protected boolean hit(Line p, int damage){
        health -= damage;
        if (health <= 0) {
            host = new FlyingOrgan(this, p.ptd.getAdd(0, -15), 30);
            amputated = true;
            return true;
        } else ptd.add(p.getAtan(16));
        return false;
    }
    protected boolean checkHit(Line p){
        if (!amputated) {
            Line body = getAdd(host.pos);
            if (type == 1 && image != null) {
                P t = body.ptd;
                if (Math.pow(sizeRadius, 2) - ((Math.pow(p.ptd.x - t.x, 2) + Math.pow(p.ptd.y - t.y, 2))) > 0) {
                    return true;
                }
            } else if (p.lineCross(body)) {
                return true;
            }
        }
        return false;
    }
    public String toString(){
        return "";
    }

    public float getHeight() {
        if (parent == null) return 0;
        return parent.getHeight()+(amputated?0:Math.abs(ptd.y-pt.y));
    }
}
