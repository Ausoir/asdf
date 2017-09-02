package com.ga.map;

import com.ga.game.Observer;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Prop;

import java.awt.*;
import java.util.ArrayList;

public class Building extends Prop{
    private ArrayList<Terrain> terrains = new ArrayList<>();
    private Map drawboard;
    private P offset;
    public Building(Map map, String name, int seed) {
        super(map, null, seed);
        drawboard = new Map("building/"+name);
        this.name = drawboard.callname;
        drawboard.setTexture();
        for (java.util.Map.Entry pair : drawboard.terrains.entrySet()) {
            Terrain terrain = (Terrain) pair.getValue();
            boolean added = false;
            for (int i = 0; i < terrains.size(); i++) {
                if (terrain.getDepth() < terrains.get(i).getDepth()) {
                    terrains.add(i, terrain);
                    added = true;
                    break;
                }
            }
            if (!added) terrains.add(terrain);
        }
    }
    public void onCreate(){
        for (Observer i : drawboard.updater.addQueue){
            if (i instanceof Prop){
                Prop prop = (Prop) i;
                prop.map = map;
                prop.start();
            }
        }
    }
    public void onField(){
        super.onField();
        offset = new P(-drawboard.size.x/2, -drawboard.size.y);
        for (Line i : drawboard.land){
            if (!i.vertical()) {
                Line l = new Line(i.pt.x + pos.x + offset.x, i.pt.y + pos.y + offset.y, i.ptd.x + pos.x + offset.x, i.ptd.y + pos.y + offset.y, -2, 0) {
                    public String toString() {
                        return "";
                    }
                };
                l.depth = (i.depth - 10) / 3 + getDepth();
                map.land.add(l);
            }
        }
        drawboard = null;
    }
    public void draw(Graphics2D g){
        for (Terrain i : terrains) i.draw(g, offset.getAdd(pos));
    }
}