package com.ga.map;

import com.ga.game.FileManager;

import java.util.Random;

/**
 * Created by Ga on 4/17/2017. asd
 */
public class Creation {
    public static Random r = new Random();
    public static int seed;

    public static void create(){
        FileManager.loadGame();
        if (!FileManager.isFile("Data/map/World.map")) {
            seed = (int) (Math.random()*1000000);
            FileManager.saveWorld();
        } else {
            FileManager.readWorld();
        }
    }
    static void prepareRandom(int id){
        r = new Random(id+seed);
    }
    public static int getSeed(int p){
        if (p == -1) return r.nextInt(99999);
        return p;
    }
}