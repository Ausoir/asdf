package com.ga.unit;

import com.ga.tool.Line;
import com.ga.tool.P;

import java.awt.*;

/**
 * Created by Ga on 7/29/2016.
 */
public class Organ extends Line {
    public Color color = Color.black;
    public Stroke stroke = new BasicStroke(10);
    public int damage = 1;
    public Organ(P pt, int x, int y){
        this.pt = pt;
        this.ptd = new P(pt.x+x,pt.y+y);
    }
    public void setStyle(Color color, int size){
        this.color = color;
        this.stroke = new BasicStroke(size);
    }
}
