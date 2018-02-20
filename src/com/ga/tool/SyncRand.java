package com.ga.tool;

import java.util.Random;

public class SyncRand {
    public static Random r = new Random();
    public static void synchronize(int seed){
        r = new Random(seed);
    }
}
