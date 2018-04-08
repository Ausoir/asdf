package com.ga.tool;

import com.ga.game.FileManager;
import com.ga.unit.Clip;

import java.util.HashMap;

/**
 * Created by Ga on 7/26/2016. asd
 */
public class Clips {
    public static HashMap<String, Clip> clip = new HashMap<>();
    public static void init(){
        for (String[] i : FileManager.fileCache.get("2d")) {
            clip.put(i[0], FileManager.readClip(i[1]));
        }
    }
    public static int[][][] clone(int[][][] p){
        int[][][] result = new int[p.length][][];
        for (int i=0;i<p.length;i++){
            result[i] = clone(p[i]);
        }
        return result;
    }
    private static int[][] clone(int[][] p){
        int[][] result = new int[p.length][];
        for (int ii=0;ii<p.length;ii++){
            result[ii] = new int[p[ii].length];
            System.arraycopy(p[ii], 0, result[ii], 0, p[ii].length);
        }
        return result;
    }
    public static int[] clone(int[] p){
        int[] result = new int[p.length];
        System.arraycopy(p, 0, result, 0, p.length);
        return result;
    }
}