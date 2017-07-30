package com.ga.map;

import com.ga.tool.Line;
import com.ga.tool.P;

/**
 * Created by ga on 5/28/17. asd
 */
class Generator {
    public Generator(int next){
        TerrainTrail.id = next;
    }
    void normalize(int marginX, int marginY){
        P high = new P(), low = new P();
        for (Line i : Global.map.land){
            low.x = Math.min(Math.min(low.x,i.pt.x),i.ptd.x);
            low.y = Math.min(Math.min(low.y,i.pt.y),i.ptd.y);
            high.x = Math.max(Math.max(high.x,i.pt.x),i.ptd.x);
            high.y = Math.max(Math.max(high.y,i.pt.y),i.ptd.y);
        }
        P margin = new P(marginX, marginY);
        int maxDiffX = (int) (high.x-low.x)+margin.x();
        int maxDiffY = (int) (high.y-low.y)+margin.y();
        Global.map.size = new P(maxDiffX, maxDiffY);
        P adjustment = new P(0,low.y*-1+margin.y);
        for (Line i : Global.map.land) {
            i.pt = i.pt.getAdd(adjustment);
            i.ptd = i.ptd.getAdd(adjustment);
        }
        Updater.normalize(adjustment);
    }
    void generateFloor(int depth, P mapSize, int randomness, int varianceY, String[] texture){
        float horizon = mapSize.y-600;
        P orig = new P(0, horizon);
        TerrainTrail trail = new TerrainTrail(orig);
        while (trail.last.x < mapSize.x) {
            int randX = (Creation.r.nextInt(240) + 20) * (Creation.r.nextInt(randomness)+1);
            int randY = (Creation.r.nextInt(varianceY) - (varianceY/2)) * Creation.r.nextInt(randomness);
            trail.add(trail.last.x+randX, horizon+randY);
            if (trail.last.x+(200*randomness*.5f) > mapSize.x) trail.last.x = mapSize.x;
        }
        trail.last.y = horizon;
        trail.add(trail.last.x, mapSize.y);
        trail.add(0, mapSize.y);
        trail.finish(depth+"&"+texture[Creation.r.nextInt(texture.length)]);
    }
}
