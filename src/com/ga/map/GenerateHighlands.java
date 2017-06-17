package com.ga.map;

import com.ga.tool.Line;
import com.ga.tool.P;

import java.util.ArrayList;

/**
 * Created by Ga on 2/23/2017. asd
 */
class GenerateHighlands extends Generator {
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
    private void create(P mapSize, P start, P end){
        TerrainTrail trail = new TerrainTrail(start);
        int preX = Creation.r.nextInt((int) (mapSize.x/8))+300;
        int postX = Creation.r.nextInt((int) (mapSize.x/8))+300;
        trail.move(preX,0);
        Line first = new Line(trail.last, end.getAdd(-postX,0));
        ArrayList<Line> total = new ArrayList<>();
        total.add(first);
        divideLand(total, 0, 8);
        for (Line i : total) trail.add(i);
        trail.move(postX,0);
        trail.add(new P(trail.last.x, mapSize.y+64));
        trail.add(new P(start.x, mapSize.y+64));
        trail.finish(this, "dirt&grass"+(Creation.r.nextBoolean()?"&Rock=.1,Bush=.2,Weed=.4,Tree=1":""));
        if (start.y > 0 && end.y > 0) trail.minY(800);
    }
    Generator create(char p, P mapSize){
        switch (p){
            case '-':
                create(mapSize, new P(-64, Creation.r.nextInt((int) (mapSize.y/3))+mapSize.y/3),
                        new P(mapSize.x+64, Creation.r.nextInt((int) (mapSize.y/3))+mapSize.y/3));
                break;
            case '1':
                create(mapSize, new P(-64, Creation.r.nextInt((int) (mapSize.y/2))+mapSize.y/4),
                        new P(mapSize.x-64, mapSize.y+64));
                break;
            case '3':
                create(mapSize, new P(64, mapSize.y+64),
                        new P(mapSize.x+64, Creation.r.nextInt((int) (mapSize.y/2))+mapSize.y/4));
                break;
            case '7':
                create(mapSize, new P(-64, Creation.r.nextInt((int) (mapSize.y/5))+mapSize.y*3/4),
                        new P(mapSize.x+64, -64));
                break;
            case '9':
                create(mapSize, new P(-64, -64),
                        new P(mapSize.x+64, Creation.r.nextInt((int) (mapSize.y/5))+mapSize.y*3/4));
                break;
        }
        return this;
    }
}