package com.ga.map;

import com.ga.tool.P;
import com.ga.unit.Prop;
import com.ga.unit.Stock;

import java.util.ArrayList;

/**
 * Created by ga on 5/28/17. asd
 */
class GenerateDungeon extends GenerateHouse {
    Generator create(String[] info){
        int returnDoorSeed = Integer.parseInt(info[1]);
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
        P margin = new P(800,400);
        int maxDiffX = (int) (high.x-low.x)+margin.x()*2;
        int maxDiffY = (int) (high.y-low.y)+margin.y()*2;
        Global.map = new Map(info[0], new String[]{"-1","None","None",maxDiffX+"x"+maxDiffY,Global.player.cmap});
        P adjustment = low.mult(-1).add(margin.x,margin.y);
        TerrainTrail trail = new TerrainTrail(new P(0,-advance).add(adjustment));
        for (P i : forward) trail.add(i.add(adjustment));
        for (int i=backward.size()-1;i>=0;i--) trail.add(backward.get(i).add(adjustment));
        trail.finish(this, "rock&none");
        Prop door = Stock.Door.create(returnDoorSeed);
        P doorSize = door.image.getSize();
        int exitSegment = Creation.r.nextInt(segments-6)+3;
        P exitPos = forward.get(exitSegment).getSubtract(backward.get(exitSegment)).mult(.5f,.5f)
                .add(backward.get(exitSegment));
        P doorPos = exitPos.getAdd(0, -doorSize.y/2-2);
        TerrainTrail platform = new TerrainTrail(exitPos.getAdd(-doorSize.x/2,0));
        platform.move(doorSize.x,0);
        platform.move(0,24);
        platform.move(-doorSize.x,0);
        platform.finish(this, "wood&basic");
        platform.climbable();
        door.pos.set(doorPos);
        Stock.Exit.create(returnDoorSeed).pos.set(doorPos.x,doorPos.y);
        Stock.Spawn.create(returnDoorSeed).pos.set(doorPos.x,doorPos.y);
        generateBlocker(trail.terrain);
        return this;
    }
}
