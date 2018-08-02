package com.ga.map;

/**
 * Created by ga on 5/28/17. asd
 */
public class GenerateBiome {
    private static final int FOREST = 2;
    private static final int DEEPWOOD = 3;
    public static void generate(Map map, MapName dir){
        int mapVar = dir.getIndex();
        map.temperature = getTemperature(mapVar);
        int humidity = getHumidity(mapVar);
        if (humidity >= FOREST) map.panoramaName = "Forest";
        else if (map.isSnowing()) map.panoramaName = "Taiga";
        else map.panoramaName = "Grassland";
        if (getTown(mapVar)) GenerateTown.generate(map);
        else if (humidity >= DEEPWOOD) GenerateForest.generateDeepwoods(map);
        else if (humidity == FOREST) GenerateForest.generate(map);
        else if (humidity == 1) GeneratePlains.generateFields(map);
        else if (humidity < -1) GeneratePlains.generateDesert(map);
        else GeneratePlains.generateGrasslands(map);
    }
    public static boolean getTown(int index){
        index *= 1243;
        int mapVar = Creation.seed + index;
        float var = (Global.cos((mapVar) * .15f) + Global.sin((mapVar) * .1f) + Global.sin((mapVar) * .12f)*.7f) * Global.cos((mapVar) * .5f);
        return Math.abs(var) < .4f;
    }
    public static int getHumidity(int index){
        float mapVar = ((Global.sin(Creation.seed)*77141) + (index*1243))/10000f;
        float var = Global.sin(mapVar)+Global.cos(mapVar)+Global.sin(mapVar*5)+Global.sin(mapVar*10)+Global.sin(mapVar*9);
        return Math.round(var);
    }
    public static int getTemperature(int index){ //usually 0 ~ 5
        float mapVar = ((Global.sin(Creation.seed)*44379) + (index * 653))/40000f;
        float var = Global.sin(mapVar)+Global.cos(mapVar)+Global.sin(mapVar*4)+Global.sin(mapVar*12);
        return Math.round(var*2);
    }
}