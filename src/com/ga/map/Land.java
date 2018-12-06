package com.ga.map;

import com.ga.tool.Line;

import java.awt.*;

/**
 * Created by Ga on 1/10/2017. xhfsdxghf
 */
public class Land extends Shape {
    private Color color;
    public Land(Map map, String texture, float depth){
        super(map, depth);
        String[] data = texture.substring(1).split("/");
        this.color = new Color(Integer.parseInt(data[0]),Integer.parseInt(data[1]),Integer.parseInt(data[2]),Integer.parseInt(data[3]));
    }
    public void start(){
        map.updater.addQueue.add(this);
    }
    public void draw(Graphics2D g){
        if (checkDraw(g)) {
            g.setColor(color);
            g.setStroke(stroke);
            int x = (int)(pos.x+Global.rel.x), y = (int)(pos.y+Global.rel.y);
            polygon.translate(x, y);
            g.fillPolygon(polygon);
            g.setColor(Color.BLACK);
            g.drawPolygon(polygon);
            polygon.translate(-x, -y);
        }
    }
    public void setDepth(float depth) {
        super.setDepth(depth);
        for (Line i : lines) i.depth = depth;
    }
    public void remove(){
        map.updater.remove(this);
    }
}