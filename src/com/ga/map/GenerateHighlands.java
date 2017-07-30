package com.ga.map;

import com.ga.tool.Line;
import com.ga.tool.P;

import java.util.ArrayList;

/**
 * Created by Ga on 2/23/2017. asd
 */
class GenerateHighlands extends Generator {
    GenerateHighlands(int next) {
        super(next);
    }
    void create(int depth, P mapSize){
        P start = new P(-64, Creation.r.nextInt(mapSize.y()-128)+64);
        P end = new P(mapSize.x+64, Creation.r.nextInt(mapSize.y()-128)+64);
        TerrainTrail trail = new TerrainTrail(start);
        int preX = Creation.r.nextInt((int) (mapSize.x/8))+300;
        int postX = Creation.r.nextInt((int) (mapSize.x/8))+300;
        trail.move(preX,0);
        Line first = new Line(trail.last, end.getAdd(-postX,0));
        ArrayList<Line> total = new ArrayList<>();
        total.add(first);
        divideLand(total, 0, 7);
        float highestY = 0;
        for (Line i : total){
            trail.add(i);
            if (i.pt.y > highestY) highestY = i.pt.y;
            if (i.ptd.y > highestY) highestY = i.ptd.y;
        }
        trail.move(postX,0);
        trail.add(new P(trail.last.x, Math.max(highestY+64, mapSize.y+64)));
        trail.add(new P(start.x, Math.max(highestY+64, mapSize.y+64)));
        trail.finish(depth+"&dirt&grass"+(Creation.r.nextBoolean()?"&Rock=.1,Bush=.2,Weed=.4,Tree=1":""));
    }
    private void divideLand(ArrayList<Line> total, int current, int max){
        if (current >= max) return;
        Line last = total.remove(total.size()-1);
        P mid = new P(Creation.r.nextFloat()*(last.ptd.x-last.pt.x)+last.pt.x,
                Creation.r.nextFloat()*(last.ptd.y-last.pt.y)+last.pt.y);
        Line one = new Line(last.pt, mid);
        Line two = new Line(mid, last.ptd);
        float shortest = last.getLength()/(8*(current+1));
        float atan = last.getAtan()+1.57f;
        float rand = Global.rng(1);
        mid.add(Global.cos(atan)*shortest*rand, Global.sin(atan)*shortest*rand);
        total.add(one);
        divideLand(total, ++current, max);
        total.add(two);
        divideLand(total, ++current, max);
    }
}