package com.ga.unit;

import com.ga.game.Texture;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.item.FlyingOrgan;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Ga on 7/29/2016.
 */
public class Organ extends Line {
    public int strokeSize = 10;
    protected Texture image;
    protected boolean amputated = false;
    public Matter host;
    protected Organ parent;
    protected ArrayList<Organ> children = new ArrayList<>();
    protected int sizeRadius;
    public int type;
    public int health = 1;
    protected boolean vital = false;
    protected boolean leg = false;

    public Organ(Organ p, boolean tip){
        parent = p;
        if (parent != null) parent.children.add(this);
        if (p != null) {
            if (tip) pt = p.ptd;
            else pt = p.pt;
        } else pt = new P();
        ptd = new P();
    }
    public void setImage(String p){
        image = new Texture(p);
    }
    public void setImage(String p, int index){
        image = new Texture(p, index);
        image.size.x = strokeSize;
        image.size.y = 0;
        image.setShader(Texture.Shader.XYLinearGradient);
    }
    public Texture getImage(){
        return image;
    }
    public void setSize(int size){
        this.strokeSize = size;
    }
    public void draw(Graphics2D g){
        P pos = host.pos.getAdd(pt);
        if (type != 0){
            image.draw(g, pos.x, pos.y, getAtan()-1.57f, host.dir, strokeSize, getLength()+3);
        } else if (image != null && !amputated){
            image.draw(g, pos.x, pos.y, getAtan()+1.57f, host.dir);
        }
    }
    protected boolean hit(Line p, int damage, DamageType type){
        health -= damage;
        if (health <= 0 && (type == DamageType.Cut || type == DamageType.Burn)) {
            if (type == DamageType.Cut){
                host = new FlyingOrgan(this, p.ptd.getAdd(0, -15), 30);
                Particle.explode("Blood", p.ptd, new P(), 5, 40, 0, true, 2);
            } else {
                Particle.explode("Flame", p.ptd, new P(0, -5), 1, 12, 3, false, 3);
            }
            amputated = true;
            for (Organ i : children) i.hit(p, 999, type);
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
}
