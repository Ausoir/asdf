package com.ga.unit;

import com.ga.game.Sound;
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
    public ShadedPicture image;
    public boolean amputated = false;
    public Unit host;
    private ArrayList<Organ> children = new ArrayList<>();
    public int type;
    int health;
    private int maxLength = 80;
    public boolean vital = false;
    boolean leg = false, hand = false;
    public Performer onAmputate = new Performer();

    public Organ(Organ p, boolean tip){
        if (p != null) {
            p.children.add(this);
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
    public int getSize() {
        return strokeSize;
    }
    public void draw(Graphics2D g){
        float x = host.pos.x+pt.x, y = host.pos.y+pt.y, sizeX = strokeSize, sizeY = getLength()+3;
        if (image.visible && Global.inCamera(x,y, sizeX, sizeY))
            image.draw(g, x, y, getAtan()-1.57f, host.dir, sizeX, sizeY);
    }
    void setMaxLength(int p){
        maxLength = p;
    }
    void hit(Line p){
        ptd.set(pt.getAdd(p.getAtan(maxLength)));
    }
    public void amputate(Line p, int damage){
        Sound.play(host.map, host.pos, "hit_bloody");
        int intensity = 18 + damage*2;
        if (image != null) {
            final Projectile flyingOrgan = new Projectile(host.map, null, new P(host.pos), p.ptd.getAdd(Global.r.nextInt(24)-12,-Global.r.nextInt(12)), intensity) {
                private final int length = (int) getLength();
                public void update() {
                    if (tangible) {
                        Particle.explode(host.map, "Blood", host.getDepth(), pos, new P(0, -15), 4, 5, 5, true, 5);
                    }
                    super.update();
                }
                public void draw(Graphics2D g){
                    if (image.visible && Global.inCamera(pos.x, pos.y, strokeSize, length + 3)) {
                        if (type == 0) super.draw(g);
                        else image.draw(g, pos.x, pos.y, rotation, dir, strokeSize, length + 3);
                    }
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
        for (Organ i : children) i.amputate(p, intensity);
        onAmputate.perform();
        host.clip.updateStatus();
    }
    boolean checkHit(Line p){
        if (!amputated) {
            float atan = getAtan()+1.56f;
            Line left = new Line(pt.x+Global.cos(atan)*strokeSize, pt.y+Global.sin(atan)*strokeSize,
                    ptd.x+Global.cos(atan)*strokeSize, ptd.y+Global.sin(atan)*strokeSize);
            Line right = new Line(pt.x-Global.cos(atan)*strokeSize, pt.y-Global.sin(atan)*strokeSize,
                    ptd.x-Global.cos(atan)*strokeSize, ptd.y-Global.sin(atan)*strokeSize);
            Line hitbox = p.getAdd(host.pos.getMult(-1,-1));
            return lineCross(hitbox) || hitbox.lineCross(left) || hitbox.lineCross(right);
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
