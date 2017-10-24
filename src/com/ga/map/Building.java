package com.ga.map;

import com.ga.game.Observer;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Prop;

import java.awt.*;
import java.util.ArrayList;

public class Building extends Prop{
    private Map drawboard;
    private P offset;
    public Building(Map map, String name, int seed) {
        super(map, null, seed);
        drawboard = new Map("building/"+name);
        this.name = drawboard.callname;
        image.visible = false;
    }
    public void onField(){
        offset = new P(-drawboard.size.x/2, -drawboard.size.y);
        drawboard.adjust(offset.getAdd(pos));
        drawboard.setTexture();
        for (java.util.Map.Entry pair : drawboard.terrains.entrySet()) {
            Terrain terrain = (Terrain) pair.getValue();
            terrain.setDepth(terrain.getDepth()-1);
            terrain.map = this.map;
            terrain.render();
            terrain.start();
        }
        for (Line i : drawboard.land){
            if (!i.vertical()) {
                Line l = new Line(i.pt.x, i.pt.y, i.ptd.x, i.ptd.y, -2, 0) {
                    public String toString() {
                        return "";
                    }
                };
                l.depth = i.depth;
                map.land.add(l);
            }
        }
        drawboard.updater.adjust(offset.getAdd(pos));
        for (Observer i : drawboard.updater.addQueue){
            if (i instanceof Prop){
                Prop prop = (Prop) i;
                prop.map = map;
                prop.save = false;
                prop.start();
            }
        }
        drawboard = null;
    }
}