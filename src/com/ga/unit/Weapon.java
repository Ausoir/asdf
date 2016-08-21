package com.ga.unit;

import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.tool.P;

import java.awt.*;

/**
 * Created by Ga on 8/1/2016.
 */
public class Weapon extends Item {
    public int damage = 0;
    public float length;
    public Organ organ = new Organ(new P(),0,0);
    public Weapon(String name) {
        super(name);
        if (name != null) length = size.x/2;
        else length = 0;
        rotation = 35;
    }
    public Line getLine(){
        return new Line(pos, getPoint());
    }
    public P getPoint(){
        P result = new P();
        if (length > 2) result.set((int)length*Global.cos(rotation)*dir,(int) (length*Global.sin(rotation))*dir);
        result.add(organ.ptd.getAdd(pos));
        return result;
    }
    public void draw(Graphics2D g){
        super.draw(g, organ.ptd);
    }
}
