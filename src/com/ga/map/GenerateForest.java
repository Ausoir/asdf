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
        ground.finish("4&dirt&grass&Tree=1");
        generator.makeEdgeSpawns();
    }
}
