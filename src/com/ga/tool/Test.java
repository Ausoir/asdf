package com.ga.tool;

import com.ga.map.Global;

import java.util.Random;

/**
 * Created by Ga on 12/13/2016. asdasdasdawdadqw
 */
public class Test {
    public static void main(String[] args){
        Global.init();
        int seed = 890235;//(int) (Math.random() * 999999);
        int max = 100;
        float[] r = new float[max], r2 = new float[max], r3 = new float[max];
        for (int i = 0; i < max; i++) {
            int mapVar = seed + (i * 1243);
            r[i] = (Global.cos((mapVar) * .15f) + Global.sin((mapVar) * .1f) + Global.sin((mapVar) * .12f)*.7f) * Global.cos((mapVar) * .5f);
            float var = ((Global.sin(seed)*99999) + (i * 1243))/10000f;
            r2[i] = Global.sin(var)+Global.cos(var)+Global.sin(var*5)+Global.sin(var*10);
            var = ((Global.sin(seed)*25678) + (i * 653))/40000f;
            r3[i] = Global.sin(var)+Global.cos(var)+Global.sin(var*4)+Global.sin(var*12);
        }
        for (int i = 0; i < max; i++) {
            String town = (r[i]+"").substring(0,Math.min(5,(r[i]+"").length()));
            String temp = (r2[i]+"").substring(0,Math.min(5,(r2[i]+"").length()));
            String biom = ((r3[i]*3)+"").substring(0,Math.min(5,(r3[i]+"").length()));
            Global.p(i + " " + (Math.abs(r[i])<.4f?"TOWN":"    ") + " " + town + "   " + temp + " " + (r2[i]>1?" WET":"    ") + "   " + biom + (r3[i]>1?" WARM":"    "));
        }
    }
}
