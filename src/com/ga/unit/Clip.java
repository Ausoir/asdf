package com.ga.unit;

import java.util.HashMap;

/**
 * Created by Ga on 7/30/2016.
 */
public class Clip {
    public int[][] property;
    public int[][] model;
    public int[][][] clip;
    public int[] frames;
    public HashMap<String, int[]> moveset;
    public Clip(int[][] property, int[][] model, int[][][] clip, int[] frames, HashMap<String, int[]> moveset){
        this.property = property;
        this.model = model;
        this.clip = clip;
        this.frames = frames;
        this.moveset = moveset;
    }
    public void print(){
        System.out.println("=========");
        for (int[][] lines : clip){
            for (int[] sections : lines){
                for (int value : sections){
                    System.out.print(value+",");
                }
                System.out.println(";");
            }
        }
    }
}
