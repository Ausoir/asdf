package com.ga.map;

import com.ga.tool.P;
import com.ga.unit.Prop;
import com.ga.unit.Stock;

import java.util.ArrayList;

class GenerateCave {
    void generateCave(Map map){
        Generator generator = new Generator(map, 0);
        int returnDoorSeed = 0;
        int origDirection = Creation.r.nextInt(360);
        int momentum = Creation.r.nextInt(48)-24;
        int advance = 240;
        int width = 360;
        ArrayList<P> forward = new ArrayList<>();
        ArrayList<P> backward = new ArrayList<>();
        P fw = new P(0, 0), bw, low = new P(), high = new P();
        int direction = origDirection;
        int segments = Creation.r.nextInt(10)+20;
        for (int i=0;i<segments;i++){
            width += Creation.r.nextGaussian()*3;
            momentum += Creation.r.nextGaussian()*3;
            direction += Creation.r.nextGaussian()*16 + momentum;
            direction = (int) Global.clamp(direction, origDirection-150, origDirection+150);
            float sin = Global.sin(direction)*advance;
            float cos = Global.cos(direction)*advance;
            float sinW = Global.sin(direction+90)*width;
            float cosW = Global.cos(direction+90)*width;
            fw.add(cos, sin);
            if (i == segments-1){
                fw.add(cos+cosW/2, sin+sinW/2);
                bw = fw.getAdd(0,0);
            } else bw = fw.getAdd(cos+cosW, sin+sinW);
            low.x = Math.min(low.x,fw.x);
            low.y = Math.min(low.y,fw.y);
            high.x = Math.max(high.x,fw.x);
            high.y = Math.max(high.y,fw.y);
            forward.add(fw.getAdd(0,0));
            backward.add(bw.getAdd(0,0));
        }

        TerrainTrail trail = new TerrainTrail(map, new P(0,-advance));
        for (P i : forward) trail.add(i);
        for (int i=backward.size()-1;i>=0;i--) trail.add(backward.get(i));
        trail.finish("0&rock&none");

//        Prop door = Stock.Door.create(map, returnDoorSeed);
        Prop door = Stock.Travel.create(map, -1,returnDoorSeed); //temporary door replacement
        P doorSize = door.image.getSize();
        int exitSegment = Creation.r.nextInt(segments-6)+3;
        P exitPos = forward.get(exitSegment).getSubtract(backward.get(exitSegment)).mult(.5f,.5f).add(backward.get(exitSegment));
        P doorPos = exitPos.getAdd(0, -doorSize.y/2-2);
        TerrainTrail platform = new TerrainTrail(map, exitPos.getAdd(-doorSize.x/2,0));
        platform.move(doorSize.x,0);
        platform.move(0,24);
        platform.move(-doorSize.x,0);
        platform.finish("1&wood&basic");
        platform.climbable();
        door.pos.set(doorPos);
//        Stock.Exit.create(map, returnDoorSeed).pos.set(doorPos.x,doorPos.y);
        Stock.Spawn.create(map, -1,returnDoorSeed).start().pos.set(doorPos.x,doorPos.y);
        map.normalize(800,800);
        generator.generateBlocker(2,trail.terrain);
    }
}
