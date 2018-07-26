package com.ga.map;

/**
 * Created by ga on 5/28/17. asd
 */
public class GenerateBiome {
    public static void generate(Map map, MapName dir){
        int mapVar = dir.getIndex();
        map.temperature = getTemperature(mapVar);
        int humidity = getHumidity(mapVar);
        if (getTown(mapVar)) GenerateTown.generate(map);
        else if (humidity > 2) GenerateForest.generateDeepwoods(map);
        else if (humidity > 1) GenerateForest.generate(map);
        else GenerateGrassland.generate(map);
    }
    private static boolean getTown(int index){
        index *= 1243;
        int mapVar = Creation.seed + index;
        float var = (Global.cos((mapVar) * .15f) + Global.sin((mapVar) * .1f) + Global.sin((mapVar) * .12f)*.7f) * Global.cos((mapVar) * .5f);
        return Math.abs(var) < .4f;
    }
    private static int getHumidity(int index){
        float mapVar = ((Global.sin(Creation.seed)*99999) + (index*1243))/10000f;
        float var = Global.sin(mapVar)+Global.cos(mapVar)+Global.sin(mapVar*5)+Global.sin(mapVar*10);
        return Math.round(var);
    }
    private static int getTemperature(int index){ //usually 0 ~ 5
        float mapVar = ((Global.sin(Creation.seed)*25678) + (index * 653))/40000f;
        float var = Global.sin(mapVar)+Global.cos(mapVar)+Global.sin(mapVar*4)+Global.sin(mapVar*12);
        return Math.round(var*2);
    }
}