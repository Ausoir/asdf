package com.ga.tool;

import com.ga.map.Global;

import java.util.Random;

/**
 * Created by Ga on 12/13/2016. asdasdasdawdadqw
 */
public class Test {
    public static void main(String[] args){
        String[] stat = new String[]{"Str","Agi","Int"};
        int[] statV = new int[]{1,1,1};
        int target = 2;
        statV[target] += 1;
        int level = 6;
            int i, current = 1, prev;
            for (i=1;i<level;i+=prev){
                prev = current;
                current = i;
            }
            if (i != level) { //not fibo
                statV[new Random().nextInt(statV.length)] --;
            }
            Global.p("Final stats: "+statV[0]+","+statV[1]+","+statV[2]);
    }
}
