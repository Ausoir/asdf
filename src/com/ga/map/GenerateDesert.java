package com.ga.map;

import com.ga.tool.P;

class GenerateDesert {
    static void generate(Map map){
        Generator generator = new Generator(map, 0);
        map.size = new P(Creation.r.nextInt(4000)+4000, 4000);
        TerrainTrail ground = new TerrainTrail(map, new P(-40, map.size.y()+40));
        ground.move(0, -640);
        ground.move(map.size.x()+80, 0);
        ground.move(0, 640);
        ground.climbable();
        ground.finish("3&dirt&sand&Rock=.1");
        generator.makeEdgeSpawns();
        int feature = Creation.r.nextInt(4);
        for (int i=0;i<feature;i++) {
            generator.generateBuildings(new String[]{"Tree1"}, map.landRaw.get(1), 1+i);
        }
    }
}