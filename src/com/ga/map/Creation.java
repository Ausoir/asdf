package com.ga.map;

import com.ga.game.FileManager;

import java.util.Random;

/**
 * Created by Ga on 4/17/2017. asd
 */
public class Creation {
    public static Random r;
    public static int seed;

    public static void create(){
        if (!FileManager.isFile(FileManager.makeDirectory("Data/map/World.map"))) {
            seed = (int) (Math.random()*1000000);
            FileManager.saveWorld();
        } else {
            FileManager.loadWorld();
        }
        r = new Random(seed);
        FileManager.loadPlayer();
    }
    static void prepareRandom(int id){
        r = new Random((id*789)+seed);
    }
    public static int getSeed(int p){
        if (p == -1) return r.nextInt(99999);
        return p;
    }
    private static String toBase8(int p){
        char hex[]={'0','1','2','3','4','5','6','7','8'};
        int num = p, rem;
        String str = "";
        while(num > 0) {
            rem = num % 8;
            str = hex [rem] + str;
            num = num / 8;
        }
        return str;
    }
}