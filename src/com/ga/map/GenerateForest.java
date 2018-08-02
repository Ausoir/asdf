package com.ga.map;

import com.ga.tool.P;

class GenerateForest {
    static void generate(Map map){
        Generator generator = new Generator(map, 0);
        map.size = new P(Creation.r.nextInt(4000)+4000, 2800);
        TerrainTrail ground = new TerrainTrail(map, new P(-40, map.size.y()+40));
        ground.move(0, -640);
        ground.move(map.size.x()+80, 0);
        ground.move(0, 640);
        ground.climbable();
        if (map.isSnowing()) ground.finish("4&dirt&snow&Snowtree=1");
        else ground.finish("4&dirt&grass&Tree=1");
        generator.makeEdgeSpawns();
    }
    static void generateDeepwoods(Map map){
        Generator generator = new Generator(map, 0);
        map.size = new P(Creation.r.nextInt(4000)+4000, 4000);
        TerrainTrail ground = new TerrainTrail(map, new P(-40, map.size.y()+40));
        ground.move(0, -640);
        ground.move(map.size.x()+80, 0);
        ground.move(0, 640);
        ground.climbable();
        if (map.isSnowing()) ground.finish("3&dirt&snow&Rock=.1");
        else ground.finish("3&dirt&sand&Rock=.1");
        generator.makeEdgeSpawns();
        generator.generateBuildings(new String[]{"Tree1", "Rock1"}, map.landRaw.get(1), 10);
    }
}
