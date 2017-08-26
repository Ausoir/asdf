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
    boolean leg = false, hand = false;
    public Performer onAmputate = new Performer();

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
        image.draw(g, host.pos.x+pt.x, host.pos.y+pt.y, getAtan()-1.57f, host.dir, strokeSize, getLength()+3);
    }
    void setMaxLength(int p){
        maxLength = p;
    }
    protected boolean hit(Line p, int damage){
        boolean pierce = damage > 0 && Global.r.nextInt(damage) > health;
        if (pierce) {
            if (image != null) {
                final Projectile flyingOrgan = new Projectile(host.map, null, new P(host.pos), p.ptd.getAdd(Global.r.nextInt(24)-12,-Global.r.nextInt(12)), 32+damage*8) {
                    private final int length = (int) getLength();
                    public void update() {
                        if (tangible) {
                            Particle.explode(host.map, "Blood", host.getDepth(), pos, new P(0, -15), 4, 5, 5, true, 5);
                        }
                        super.update();
                    }
                    public void draw(Graphics2D g){
                        if (type == 0) super.draw(g);
                        else image.draw(g, pos.x, pos.y, rotation, dir, strokeSize, length+3);
                    }
                };
                flyingOrgan.image = image.copy();
                flyingOrgan.rotation = getAtan()-1.57f;
                flyingOrgan.dir = host.dir;
                flyingOrgan.setDepth(host.getDepth());
                flyingOrgan.gravity = true;
                flyingOrgan.delay.set(999);
                flyingOrgan.delay.performer = new Performer(){
                    @Override
                    public void perform(){
                        flyingOrgan.remove();
                    }
                };
                image.visible = false;
            }
            Particle.explode(host.map, "Blood", host.getDepth(), p.ptd, new P(), 5, 40, 0, true, 2);
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

    void stitch() {
        amputated = false;
        image.visible = true;
    }
}
