package com.ga.unit;

import com.ga.map.Global;
import com.ga.map.Picture;
import com.ga.map.ShadedPicture;
import com.ga.map.Shader;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.item.Projectile;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Ga on 7/29/2016.
 */
public class Organ extends Line {
    int strokeSize = 10;
    protected ShadedPicture image;
    boolean amputated = false;
    public Unit host;
    private Organ parent;
    private ArrayList<Organ> children = new ArrayList<>();
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
        image = new ShadedPicture(p, null);
    }
    public void setImage(String p, int index){
        image = new ShadedPicture(p, index, Shader.XYLinearGradient);
        image.setSize(strokeSize, 0);
        image.shade();
    }
    public Picture getImage(){
        return image;
    }
    public void setSize(int size){
        this.strokeSize = size;
        if (type != 0 && image != null) image.getSize().x = size;
    }
    public void draw(Graphics2D g){
        image.draw(g, host.pos.x+pt.x, host.pos.y+pt.y, getAtan()-1.57f, host.dir, strokeSize, getLength()+3);
    }
    protected void hit(Line p, int damage, DamageType type){
        health -= damage;
        if (health <= 0 && (type == DamageType.Cut || type == DamageType.Burn)) {
            if (type == DamageType.Cut){
                if (image != null) {
                    new Projectile(null, host.pos, p.getAtan(10).add(p.ptd), 20) {
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
                P m = p.getMidPoint();
                if (Math.pow(image.sizeRadius, 2) - ((Math.pow(m.x - t.x, 2) + Math.pow(m.y - t.y, 2))) > 0) {
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
