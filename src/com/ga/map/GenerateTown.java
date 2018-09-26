package com.ga.map;

import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Stock;

import java.util.ArrayList;
import java.util.HashMap;

class GenerateTown {
    private static HashMap<Integer, String[]> buildings = new HashMap<>(){{
        put(1,new String[]{"House1","House2","House3","House4","Bighouse1","Bighouse2","Inn","Scrapyard"});
        put(2,new String[]{"Hideout","House1","House2","House3","House4","Restaurant","Inn","Shop","Clothier","School"});
        put(3,new String[]{"Sewer","Restaurant","Shop","Pawnshop","Laboratory","Guild","Armory","Safehouse"});
    }};
    static void generate(Map map, int cityType){
        Generator generator = new Generator(map, 0);
        map.size = new P(6000+Creation.r.nextInt(4000), 1400);
        String[] landTexture = new String[]{"dirt,grass","dirt,road_unpaved","dirt,road_normal","dirt,road_highway"};
        int max = Creation.r.nextInt(4)+2;

        TerrainTrail trail = new TerrainTrail(map, new P(-40, map.size.y+40));
        trail.move(0, -640);
        trail.move(map.size.x+80, 0);
        trail.move(0, 640);
        trail.finish((max+1)+","+landTexture[Creation.r.nextInt(landTexture.length)]);

        generator.makeEdgeSpawns();
        map.normalize(0,2400);
        map.size.y -= 1600;
        int bid = 10;
        ArrayList<Line> beforeBuilding = (ArrayList<Line>) map.landRaw.clone();
        for (Line l : beforeBuilding) {
            if (l.opt > 0 && l.tid >= 0 && l.clockwise() && Math.abs(l.getSlope()) < .1f) {
                if (cityType > 1) {
                    for (int ii = 0; ii < Creation.r.nextInt(9); ii++) map.placeProp(l, Stock.createByName(map, "Scrap", -1));
                    for (int ii = 0; ii < Creation.r.nextInt(9); ii++) map.placeProp(l, Stock.createByName(map, "Streetlight", -1));
                    for (int ii = 0; ii < Creation.r.nextInt(9); ii++) map.placeProp(l, Stock.createByName(map, "Cone", -1));
                    for (int ii = 0; ii < Creation.r.nextInt(9); ii++) map.placeProp(l, Stock.createByName(map, "Pole", -1));
                }
                for (int ii = 0; ii < Creation.r.nextInt(9); ii++) map.placeProp(l, Stock.createByName(map, map.isSnowing()?"Frozen Bush":"Bush", -1));
                for (int ii = 0; ii < Creation.r.nextInt(9); ii++) map.placeProp(l, Stock.createByName(map, map.isSnowing()?"Frozen Tree":"Tree", -1));
                if (!map.isSnowing()) {
                    for (int ii = 0; ii < Creation.r.nextInt(7); ii++)
                        map.placeProp(l, Stock.createByName(map, "Resident", -1));
                }
                if (Math.abs(l.getSlope()) < .01f) {
                    bid = generator.generateBuildings(buildings.get(cityType), l, bid) + 1;
                }
            }
        }
    }
}
