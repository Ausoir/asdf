package com.ga.map;

import com.ga.tool.Line;
import com.ga.tool.P;

import java.util.ArrayList;
import java.util.HashMap;

class GenerateTown {
    private static HashMap<Integer, String[]> buildings = new HashMap<>(){{
        put(1,new String[]{"House1","House2","House3","House4","Bighouse1","Bighouse2","Inn","Scrapyard"});
        put(2,new String[]{"Hideout","House1","House2","House3","House4","Restaurant","Inn","Shop","Armory","School"});
        put(3,new String[]{"Sewer","Restaurant","Shop","Pawnshop","Laboratory","Guild","Clothier","Safehouse"});
    }};
    static void generate(Map map, int cityType){
        Generator generator = new Generator(map, 0);
        map.size = new P(6000+Creation.r.nextInt(4000), 1400);
        String[] landTexture = new String[]{"dirt&grass","dirt&road_unpaved","dirt&road_normal","dirt&road_highway"};
        int max = Creation.r.nextInt(4)+2;

        TerrainTrail trail = new TerrainTrail(map, new P(-40, map.size.y+40));
        trail.move(0, -640);
        trail.move(map.size.x+80, 0);
        trail.move(0, 640);
        trail.finish((max+1)+"&"+landTexture[Creation.r.nextInt(landTexture.length)]);

        generator.makeEdgeSpawns();
        map.normalize(0,2400);
        map.size.y -= 1600;
        int bid=10;
        ArrayList<Line> beforeBuilding = (ArrayList<Line>) map.landRaw.clone();
        for (Line l : beforeBuilding) {
            if (l.opt > 0 && l.tid >= 0 && l.clockwise() && Math.abs(l.getSlope()) < .1f) {
                P mid = l.getMidpoint();
                if (Creation.r.nextInt(3) == 0) {
                    for (int ii = 0; ii < Creation.r.nextInt(9); ii++) {
                        map.placeProp(l, "Scrap=.1,Streetlight=.2,Cone=.3,Pole=.4,Bush=.5", -1);
                    }
                }
                if (Math.abs(l.getSlope()) < .01f) {
                    bid = generator.generateBuildings(buildings.get(cityType), l, bid) + 1;
                }
            }
        }
    }
    private static void generateFoundations(Map map, int max, float maxY, String texture){
        float lastY = maxY;
        for (int i=0;i<max;i++) {
            int yRise = Creation.r.nextInt(400)+240;
            P point = new P(Creation.r.nextInt((int) (map.size.x*(3f/5)))+(map.size.x*(1f/5)),lastY-yRise);
            int width = (int) Global.clamp((float) Math.abs(map.size.x * (Creation.r.nextGaussian() * .4f)), 120, map.size.x * (7f / 8));
            P pt1 = new P(Math.max(0, point.x - width), point.y);
            P pt2 = new P(Math.min(map.size.x, point.x + width), point.y);
            int finalWidth = (int) (pt2.x - pt1.x);
            if (finalWidth <= 360) {
                pt1.x = Math.max(pt1.x-360, 0);
                pt2.x = Math.min(pt2.x+360,map.size.x-360);
            }
            int midWidth = (int) (yRise * .2f);
            int mid = (int) (pt1.x + finalWidth/2 + Creation.r.nextInt(midWidth*2) - midWidth);
            P pt11 = new P(mid-midWidth, point.y);
            P pt12 = new P(mid+midWidth, point.y);
            P lowPt1, lowPt2;
            if (Creation.r.nextBoolean()) {
                lowPt1 = pt1;
                lowPt2 = pt11;
            } else {
                lowPt1 = pt12;
                lowPt2 = pt2;
            }
            lowPt1.y += yRise/2;
            lowPt2.y += yRise/2;
            int id = max-i;
            int ladderX = Creation.r.nextInt(lowPt2.x()-lowPt1.x()-80)+lowPt1.x()+40;
            float ladderDepth = id+.01f;
            Line ground = map.checkLand(new Line(new P(ladderX, lowPt1.y), new P(ladderX, lowPt1.y+99999)), e->e.depth>=id);
            if (ground != null) {
                for (int y = lowPt1.y()+40; y < (int) ground.getCrossPointY(ladderX); y += 40) {
                    TerrainTrail ladder = new TerrainTrail(map, new P(ladderX - 40, y-20));
                    ladder.move(80, 0);
                    ladder.move(0, 20);
                    ladder.move(-80, 0);
                    ladder.finish(ladderDepth + "&" + texture);
                    ladder.climbable();
                    ladderDepth += .01f;
                }
            }
            TerrainTrail foundation = new TerrainTrail(map, pt1);
            foundation.add(pt11);
            foundation.add(pt12);
            foundation.add(pt2);
            foundation.add(new P(pt2.x, maxY));
            foundation.add(new P(pt1.x, maxY));
            foundation.finish(id+"&"+texture);
            lastY = point.y()-yRise;
        }
    }
}
