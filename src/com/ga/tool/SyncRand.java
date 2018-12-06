package com.ga.tool;

import java.util.Random;

public class SyncRand {
    private static Random r = new Random();
    public static Random get(){
        return r;
    }
    public static int getInt(int p){
        return r.nextInt(p);
    }
    public static float getFloat(float p){
        return r.nextFloat() * p;
    }
    public static boolean getBool(){
        return r.nextBoolean();
    }
}
