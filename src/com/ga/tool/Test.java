package com.ga.tool;

import com.ga.map.Creation;
import com.ga.map.GenerateBiome;
import com.ga.map.Global;

/**
 * Created by Ga on 12/13/2016. asdasdasdawdadqw
 */
public class Test {
    public static void main(String[] args){
        Global.init();
        int max = 100;
        Creation.seed = 138752;
//        Creation.seed = (int) (Math.random()*999999);
        for (int i = 0; i < max; i++) {
            boolean town = GenerateBiome.getTown(i);
            int humid = GenerateBiome.getHumidity(i);
            int temp = GenerateBiome.getTemperature(i);
            Global.p(i+" "+(town?"TOWN   humid:":"       humid:")+getInt(humid)+"   temp:"+getInt(temp));
        }
    }
    private static String getInt(int p){
        return (p<0?p+"":" "+p);
    }
}
