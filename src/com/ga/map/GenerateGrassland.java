package com.ga.map;

import com.ga.tool.Line;
import com.ga.tool.P;

import java.util.ArrayList;

class GenerateGrassland {
    static void generate(Map map){
        map.callname = "";
        Generator generator = new Generator(map, 0);
        map.size = new P(Creation.r.nextInt(4000)+2000, Creation.r.nextInt(4000)+2000);
        int depth = 4;
        P start = new P(-64, Creation.r.nextInt(map.size.y()-128)+64);
        P end = new P(map.size.x+64, Creation.r.nextInt(map.size.y()-128)+64);
        TerrainTrail trail = new TerrainTrail(map, start);
        int preX = Creation.r.nextInt((int) (map.size.x/8))+300;
        int postX = Creation.r.nextInt((int) (map.size.x/8))+300;
        trail.move(preX,0);
        Line first = new Line(trail.last, end.getAdd(-postX,0));
        ArrayList<Line> total = new ArrayList<>();
        total.add(first);
        divideLand(total, 0, 8);
        float highestY = 0;
        for (Line i : total){
            trail.add(i);
            if (i.pt.y > highestY) highestY = i.pt.y;
            if (i.ptd.y > highestY) highestY = i.ptd.y;
        }
        trail.move(postX,0);
        trail.add(new P(trail.last.x, highestY+400));
        trail.add(new P(start.x, highestY+400));
        trail.setOpt(1);
        trail.finish(depth+"&dirt&grass"+(Creation.r.nextBoolean()?"&Rock=.1,Bush=.2,Weed=.4,Tree=1":""));
        generator.makeEdgeSpawns();
        map.normalize(0,800);
        map.size.y -= 400;
        if (Creation.r.nextInt(3) == 0) {
            Line l = map.landRaw.get(Creation.r.nextInt(map.landRaw.size()));
            if (l.opt > 0 && l.tid >= 0 && l.clockwise() && Math.abs(l.getSlope()) < .1f) {
                generator.generateBuildings(new String[]{"To_Forest"}, l, 10);
            }
        }
    }
    private static void divideLand(ArrayList<Line> total, int current, int max){
        if (current >= max) return;
        Line last = total.remove(total.size()-1);
        P mid = new P(Creation.r.nextFloat()*(last.ptd.x-last.pt.x)+last.pt.x,
                Creation.r.nextFloat()*(last.ptd.y-last.pt.y)+last.pt.y);
        Line one = new Line(last.pt, mid);
        Line two = new Line(mid, last.ptd);
        float shortest = last.getLength()/(8*(current+1));
        float atan = last.getAtan()+1.57f;
        float rand = Global.clamp((float) Creation.r.nextGaussian()/2,-1,1);
        mid.add(Global.cos(atan)*shortest*rand, Global.sin(atan)*shortest*rand);
        total.add(one);
        divideLand(total, ++current, max);
        total.add(two);
        divideLand(total, ++current, max);
    }
}
