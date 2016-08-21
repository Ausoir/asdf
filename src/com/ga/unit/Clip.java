package com.ga.unit;

/**
 * Created by Ga on 7/30/2016.
 */
public class Clip {
    public int[][] model;
    public int[][][] clip;
    public int[] frames;
    public Clip(int[][] model, int[][][] clip, int[] frames){
        this.model = model;
        this.clip = clip;
        this.frames = frames;
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
