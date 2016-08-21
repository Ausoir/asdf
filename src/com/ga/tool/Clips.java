package com.ga.tool;

import com.ga.unit.Clip;
import com.ga.game.FileManager;

/**
 * Created by Ga on 7/26/2016.
 */
public class Clips {
    public static Clip man = FileManager.readClip("unit/Man.2d");
    public static int[][][] clone(int[][][] p){
        int[][][] result = new int[p.length][][];
        for (int i=0;i<p.length;i++){
            result[i] = clone(p[i]);
        }
        return result;
    }
    public static int[][] clone(int[][] p){
        int[][] result = new int[p.length][];
        for (int ii=0;ii<p.length;ii++){
            result[ii] = new int[p[ii].length];
            for (int iii=0;iii<p[ii].length;iii++) result[ii][iii] = p[ii][iii];
        }
        return result;
    }
    public static void print(int[][][] p){
        for (int[][] i : p){
            for (int[] ii : i){
                for (int iii : ii) System.out.print(iii+",");
                System.out.print(";");
            }
            System.out.println("");
        }
    }
}