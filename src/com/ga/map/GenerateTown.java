package com.ga.map;

import com.ga.game.FileManager;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.T;
import com.ga.unit.Stock;

import java.util.ArrayList;

class GenerateTown {
    static void generate(Map map, int cityType){
        Generator generator = new Generator(map, 0);
        map.type = 1;
        map.size = new P(4000+Creation.r.nextInt(3000), 1400);
        String[] landTexture;
        if (map.isSnowing()) landTexture = new String[]{"dirt,snow"};
        else landTexture = new String[]{"dirt,grass","dirt,road_unpaved","dirt,road_normal","dirt,road_highway"};
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
        T buildings = FileManager.readLiteral("Data/raw/Town.txt", cityType);
        for (Line l : beforeBuilding) {
            if (l.opt > 0 && l.tid >= 0 && l.clockwise() && Math.abs(l.getSlope()) < .1f) {
                if (cityType > 1) {
                    for (int ii = 0; ii < Creation.r.nextInt(7); ii++) map.placeProp(l, Stock.createByName(map, "Scrap", -1));
                    for (int ii = 0; ii < Creation.r.nextInt(6); ii++) map.placeProp(l, Stock.createByName(map, "Cone", -1));
                    for (int ii = 0; ii < Creation.r.nextInt(5); ii++) map.placeProp(l, Stock.createByName(map, "Pole", -1));
                    for (int ii = 0; ii < Creation.r.nextInt(4); ii++) map.placeProp(l, Stock.createByName(map, "Streetlight", 0));
                }
                for (int ii = 0; ii < Creation.r.nextInt(9); ii++) map.placeProp(l, Stock.createByName(map, map.isSnowing()?"Frozen Bush":"Bush", -1));
                for (int ii = 0; ii < Creation.r.nextInt(9); ii++) map.placeProp(l, Stock.createByName(map, map.isSnowing()?"Frozen Tree":"Tree", -1));
                if (!map.isSnowing()) {
                    for (int ii = 0; ii < Creation.r.nextInt(7); ii++)
                        map.placeProp(l, Stock.createByName(map, "Resident", -1));
                }
                if (Math.abs(l.getSlope()) < .01f) {
                    bid = generator.generateBuildings(buildings, l, bid) + 1;
                }
            }
        }
    }
}
