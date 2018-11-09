package com.ga.map;

import com.ga.tool.Line;
import com.ga.tool.P;

import java.util.ArrayList;

class GeneratePlains {
    static void generateHills(Map map){
        generate(map, map.isSnowing() ? "dirt,snow,[Weed=.1,Frozen Bush=.2]" : "dirt,grass,[Bush=.1,Weed=1]", 2);
    }
    static void generateFields(Map map){
        String theme;
        if (map.isSnowing()) theme = "dirt,snow,[Rock=.1,Frozen Bush=.2,Weed=.4,Frozen Tree=1]";
        else theme = "dirt,grass,[Rock=.1,Bush=.2,Weed=.4,Tree=1]";
        generate(map, theme, 3);
    }
    static void generateDesert(Map map){
        generate(map, "sand,sand,[Rock=.1,Dead Bush=.2]", 1);
    }
    private static void generate(Map map, String theme, int type){
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
        divideLand(total, 0, 7, type);
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
        String props = depth+","+theme;
        trail.finish(props);
        generator.makeEdgeSpawns();
        map.normalize(0,800);
        map.size.y -= 400;
        if (Creation.r.nextBoolean()) generator.generateBuilding("Cave", 400);
    }
    private static void divideLand(ArrayList<Line> total, int current, int max, int type){
        if (current >= max) return;
        Line last = total.remove(total.size()-1);
        P mid;
        if (type == 2){
            mid = new P((Creation.r.nextFloat()*.8f+.1f)*(last.ptd.x-last.pt.x)+last.pt.x,
                    (Creation.r.nextFloat()*.8f+.1f)*(last.ptd.y-last.pt.y)+last.pt.y);
        } else if (type == 3){
            float ratio = (Global.clamp((float) Creation.r.nextGaussian()/3,-.9f,.9f)+1)/2f;
            mid = new P(ratio*(last.ptd.x-last.pt.x)+last.pt.x,
                    ratio*(last.ptd.y-last.pt.y)+last.pt.y);
        } else {
            mid = new P(.5f*(last.ptd.x-last.pt.x)+last.pt.x,
                    .5f*(last.ptd.y-last.pt.y)+last.pt.y);
        }
        Line one = new Line(last.pt, mid);
        Line two = new Line(mid, last.ptd);
        float shortest = last.getLength()/(8*(current+1));
        float atan = last.getAtan()+1.57f;
        float rand = Global.clamp((float) Creation.r.nextGaussian()/2,-1,1);
        mid.add(Global.cos(atan)*shortest*rand, Global.sin(atan)*shortest*rand);
        total.add(one);
        divideLand(total, ++current, max, type);
        total.add(two);
        divideLand(total, ++current, max, type);
    }
}
