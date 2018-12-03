package com.ga.map;

import com.ga.tool.P;
import com.ga.tool.T;

class GenerateForest {
    static void generate(Map map){
        Generator generator = new Generator(map, 0);
        map.type = 3;
        map.size = new P(Creation.r.nextInt(4000)+4000, 2800);
        TerrainTrail ground = new TerrainTrail(map, new P(-40, map.size.y()+40));
        ground.move(0, -640);
        ground.move(map.size.x()+80, 0);
        ground.move(0, 640);
        ground.climbable();
        if (map.isSnowing()) ground.finish("4,grass,snow,[Frozen Tree=1]");
        else ground.finish("4,grass,Road_Unpaved,[Tree=1]");
        generator.makeEdgeSpawns();
        if (Creation.r.nextBoolean()) generator.generateBuilding("Mound", 360);
    }
    static void generateDeepwoods(Map map){
        Generator generator = new Generator(map, 0);
        map.size = new P(Creation.r.nextInt(4000)+4000, 4000);
        TerrainTrail ground = new TerrainTrail(map, new P(-40, map.size.y()+40));
        ground.move(0, -640);
        ground.move(map.size.x()+80, 0);
        ground.move(0, 640);
        ground.climbable();
        if (map.isSnowing()) ground.finish("3,dirt,snow,[Rock=.1,Frozen Tree=.2,Frozen Bush=.4]");
        else ground.finish("3,dirt,sand,[Rock=.1,Tree=.2,Bush=.4]");
        generator.makeEdgeSpawns();
        generator.generateBuildings(new T().add("Tree1").add("Tree2").add("Tree3").add("Tree4").add("Tree1").add("Tree2").add("Tree3").add("Tree4").add("Cabin").add("Rock1"),
                map.landRaw.get(1), 10);
        if (Creation.r.nextBoolean()) generator.generateBuilding("Cave", 400);
    }
}
