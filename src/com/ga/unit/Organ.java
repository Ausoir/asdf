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
 * Created by Ga on 7/29/2016. asd
 */
public class Organ extends Line {
    int strokeSize = 10;
    protected ShadedPicture image;
    boolean amputated = false;
    public Unit host;
    private Organ parent;
    private ArrayList<Organ> children = new ArrayList<>();
    int type;
    public int health = 1;
    private int maxLength = 80;
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
        image = new ShadedPicture(host.rand, p, null);
    }
    public void setImage(String p, int index){
        setImage(p, index, Shader.XYLinearGradient);
    }
    public void setImage(String p, int index, Shader shader){
        image = new ShadedPicture(host.rand, p, index, shader);
        image.setSize(strokeSize, 0);
        image.shade(0);
    }
    public Picture getImage(){
        return image;
    }
    public void setSize(int size){
        this.strokeSize = size;
        if (type != 0 && image != null){
            image.setSize(size, image.getSize().y);
        }
    }
    public void draw(Graphics2D g){
        if (!amputated) image.draw(g, host.pos.x+pt.x, host.pos.y+pt.y, getAtan()-1.57f, host.dir, strokeSize, getLength()+3);
    }
    void setMaxLength(int p){
        maxLength = p;
    }
    protected boolean hit(Line p, int damage){
        boolean pierce = damage > 0 && Global.r.nextInt(damage) > health;
        if (pierce) {
            if (image != null) {
                Projectile flyingOrgan = new Projectile(null, new P(host.pos), p.getAtan(10).add(p.ptd), 20) {
                    public void update() {
                        float length = getLength() * .45f;
                        if (tangible) {
                            Particle.explode("Blood", pos.getAdd(pt), new P(0, -15), 4, 5, 5, true, 5);
                            pt.set(Global.cos(rotation) * -length, Global.sin(rotation) * -length);
                            ptd.set(Global.cos(rotation) * length, Global.sin(rotation) * length);
                        }
                        super.update();
                    }
                };
                flyingOrgan.gravity = true;
                flyingOrgan.image = image;
            }
            Particle.explode("Blood", p.ptd, new P(), 5, 40, 0, true, 2);
            amputated = true;
            for (Organ i : children) i.hit(p, 9999);
            onAmputate.perform();
            host.clip.updateStatus();
            return true;
        } else {
            ptd.set(p.getAtan(maxLength));
        }
        return false;
    }
    boolean checkHit(Line p){
        if (!amputated) {
            Line body = getAdd(host.pos);
            if (p.lineCross(body)) {
                return true;
            }
        }
        return false;
    }
    public String toString(){
        return "";
    }
}
