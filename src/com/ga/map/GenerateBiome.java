package com.ga.map;

/**
 * Created by ga on 5/28/17. asd
 */
public class GenerateBiome {
    private static final int FOREST = 2;
    private static final int DEEPWOOD = 3;
    private static final int DESERT = -1;
    static void generate(Map map, MapName dir){
        boolean editor = Global.editor;
        Global.editor = true;
        int mapVar = dir.getIndex();
        Creation.prepareRandom(mapVar);
        map.temperature = getTemperature(mapVar);
        int humidity = getHumidity(mapVar);
        if (humidity >= FOREST) map.panoramaName = "Forest";
        else if (map.isSnowing()) map.panoramaName = "Taiga";
        else if (humidity <= DESERT) map.panoramaName = "Desert";
        else map.panoramaName = "Grassland";
        int cityType = 1;
        if (getTown(mapVar-1)) cityType++;
        if (getTown(mapVar+1)) cityType++;
        if (getTown(mapVar)) GenerateTown.generate(map, cityType);
        else if (humidity >= DEEPWOOD) GenerateForest.generateDeepwoods(map);
        else if (humidity >= FOREST) GenerateForest.generate(map);
        else if (humidity <= DESERT && map.temperature <= 0) GeneratePlains.generateDesert(map);
        else if (humidity <= DESERT) GeneratePlains.generateFields(map);
        else GeneratePlains.generateHills(map); //humidity == 0
        Global.editor = editor;
    }
    public static boolean getTown(int index){
        if (index <= 3) return true; //starting location
        index *= 1243;
        int mapVar = Creation.seed + index;
        float var = (Global.cos((mapVar) * .2f) + Global.sin((mapVar) * .2f) + Global.sin((mapVar) * .3f) * 1.1f) * Global.cos((mapVar) * .5f);
        return Math.abs(var) < .3f;
    }
    public static int getHumidity(int index){
        float mapVar = ((Global.sin(Creation.seed)*77141) + (index*1243))/10000f;
        float var = Global.sin(mapVar)+Global.cos(mapVar)+Global.sin(mapVar*3)+Global.sin(mapVar*9)+Global.sin(mapVar*8);
        return Math.round(var);
    }
    public static int getTemperature(int index){ //usually 0 ~ 5
        float mapVar = ((Global.sin(Creation.seed)*44379) + (index * 653))/4000f;
        float var = Global.sin(mapVar*5)+Global.cos(mapVar*2)+Global.sin(mapVar*2)+Global.sin(mapVar*4);
        return Math.abs((int) var);
    }
}