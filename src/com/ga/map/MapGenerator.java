package com.ga.map;

import com.ga.tool.Line;
import com.ga.tool.P;

/**
 * Created by Ga on 2/23/2017. asd
 */
public class MapGenerator {
    public static P generate(String map, int repeat, String to, int toPos, int returnDist, String returnAddr, int returnPos){
        P r = null;
        if (map.equals("Plains")) r = generatePlains(repeat, to, toPos, returnDist, returnAddr, returnPos);
        return r;
    }
    private static P generatePlains(int toDist, String to, int toPos, int returnDist, String returnAddr, int returnPos){
        String[] cmap = Global.player.host.cmap.split("@");
        boolean rightSide = cmap[1].equals("R");
        int randomness = Global.r.nextInt(9)+1;
        P mapsize = new P(3000, 1600);
        String destination = "Instance:Plains&"+(toDist-1)+"&"+to+"@"+cmap[1]+"@0@"+toPos;
        String prevmap = "Instance:Plains&"+(returnDist-1)+"&"+returnAddr+"@"+(rightSide?"L":"R")+"@0@"+returnPos;
        Global.map = new Map(new String[]{"0","None","Bg_Shore","None:"+((int)mapsize.x)+"x"+((int)mapsize.y),"None",prevmap,destination});
        P orig = new P(0,mapsize.y/2), pt1 = orig, pt2 = new P();
        while (pt2.x < mapsize.x) {
            int randX = (Global.r.nextInt(200)+50)*randomness;
            int randY = (Global.r.nextInt(40)-20)*randomness;
            pt2 = new P(pt1.x+randX,orig.y+randY);
            if (pt2.x > mapsize.x) pt2.x = mapsize.x;
            Global.map.land.add(new Line(pt1,pt2,0,0));
            pt1 = pt2;
        }
        pt1.y = mapsize.y/2;
        Line t;
        Global.map.land.add(t = new Line(pt1, new P(pt2.x, mapsize.y), 0, 0));
        Global.map.land.add(t = new Line(t.ptd, new P(0, mapsize.y), 0, 0));
        Global.map.land.add(new Line(t.ptd, orig, 0, 0));
        Global.map.formTextures.put(0,"dirt&grass");
        return rightSide ? new P(24,(mapsize.y/2)-50) : new P(mapsize.x-24,(mapsize.y/2)-50);
    }
}