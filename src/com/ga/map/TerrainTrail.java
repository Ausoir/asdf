package com.ga.map;

import com.ga.tool.Line;
import com.ga.tool.P;

import java.util.ArrayList;

/**
 * Created by ga on 5/28/17. asd
 */
class TerrainTrail {
    static int id = 0;
    private ArrayList<Line> lines = new ArrayList<>();
    int terrain;
    P last;
    TerrainTrail(P p){
        this.terrain = id++;
        reset(p);
    }
    private void reset(P p){
        last = p;
    }
    Line add(float x, float y){
        return add(new P(x,y));
    }
    Line move(float x, float y){
        return add(last.x+x,last.y+y);
    }
    Line add(P p){
        Line r = new Line(last, p, terrain, 0);
        lines.add(r);
        last = p;
        return r;
    }
    void add(Line p){
        p.next = terrain;
        p.opt = 0;
        lines.add(p);
        last = p.ptd;
    }
    void lastOpt(int p){
        lines.get(lines.size()-1).opt = p;
    }
    void climbable(){
        for (Line i : lines) {
            if (i.opt != 2 && !i.clockwise()) i.opt = -1;
        }
    }
    void switchSides(float x2){
        for (Line i : lines) {
            if (i.opt == 1){
                i.pt = new P(x2-i.pt.x,i.pt.y);
                i.ptd = new P(x2-i.ptd.x,i.ptd.y);
            } else {
                P t = new P(i.pt);
                i.pt = new P(x2 - i.ptd.x, i.ptd.y);
                i.ptd = new P(x2 - t.x, t.y);
            }
        }
    }
    private void connect(){
        if (lines.size() > 1) add(lines.get(0).pt);
    }
    private void setTexture(String p){
        Global.map.formTextures.put(terrain, p);
    }
    void finish(String p){
        if (lines.size() > 0) {
            connect();
            Global.map.land.addAll(lines);
            if (!p.equals("")) setTexture(p);
        }
    }
    void reverse() {
        for (Line i : lines){
            if (i.opt == 0) {
                P t = i.pt;
                i.pt = i.ptd;
                i.ptd = t;
            }
        }
    }
}