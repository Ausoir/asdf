package com.ga.unit;

import com.ga.map.Global;
import com.ga.map.Picture;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.item.FlyingOrgan;
import com.ga.unit.item.Projectile;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Ga on 7/29/2016.
 */
public class Organ extends Line {
    int strokeSize = 10;
    protected Picture image;
    boolean amputated = false;
    public Unit host;
    private Organ parent;
    private ArrayList<Organ> children = new ArrayList<>();
    private int sizeRadius;
    public int type;
    public int health = 1;
    boolean vital = false;
    boolean leg = false;
    Performer onAmputate = new Performer();

    Organ(Organ p, boolean tip){
        parent = p;
        if (parent != null) parent.children.add(this);
        if (p != null) {
            if (tip) pt = p.ptd;
            else pt = p.pt;
        } else pt = new P();
        ptd = new P();
    }
    public void setImage(String p){
        image = new Picture(p);
    }
    public void setImage(String p, int index){
        image = new Picture(p, index);
        image.size.x = strokeSize;
        image.size.y = 0;
        setShader();
    }
    protected void setShader(){
        image.setShader(Picture.Shader.XYLinearGradient);
    }
    public Picture getImage(){
        return image;
    }
    public void setSize(int size){
        this.strokeSize = size;
        if (type != 0 && image != null) image.size.x = size;
    }
    public void draw(Graphics2D g){
        image.draw(g, host.pos.x+pt.x, host.pos.y+pt.y, getAtan()-1.57f, host.dir, strokeSize, getLength()+3);
    }
    protected void hit(Line p, int damage, DamageType type){
        health -= damage;
        if (health <= 0 && (type == DamageType.Cut || type == DamageType.Burn)) {
            if (type == DamageType.Cut){
                if (image != null) {
                    new Projectile(null, host.pos, p.ptd.getAdd(0, -15), 30) {
                        public void update() {
                            float length = getLength() * .45f;
                            if (tangible) {
                                Particle.explode("Blood", pos.getAdd(pt), new P(0, -15), 4, 5, 5, true, 5);
                                pt.set(Global.cos(rotation) * -length, Global.sin(rotation) * -length);
                                ptd.set(Global.cos(rotation) * length, Global.sin(rotation) * length);
                            }
                            super.update();
                        }
                    }.image = image;
                }
                Particle.explode("Blood", p.ptd, new P(), 5, 40, 0, true, 2);
            } else {
                Particle.explode("Flame", p.ptd, new P(0, -5), 1, 12, 3, false, 3);
            }
            amputated = true;
            for (Organ i : children) i.hit(p, 999, type);
            onAmputate.perform();
            host.clip.updateStatus();
        } else ptd.add(p.getAtan(16));
    }
    boolean checkHit(Line p){
        if (!amputated) {
            Line body = getAdd(host.pos);
            if (type == 0 && image != null) {
                P t = body.pt;
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
}
