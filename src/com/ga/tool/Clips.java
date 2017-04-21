package com.ga.tool;

import com.ga.game.FileManager;
import com.ga.unit.Clip;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ga on 7/26/2016. asd
 */
public class Clips {
    public static HashMap<String, Clip> clip = new HashMap<>();
    public static void init(){
        ArrayList<String> clips = FileManager.searchFiles("Data/unit",new String[]{"2d"});
        for (String i : clips) {
            int pathEnd = i.lastIndexOf('\\');
            String fileName = i.substring(pathEnd+1);
            clip.put(fileName.substring(0,fileName.lastIndexOf(".")), FileManager.readClip("unit/"+fileName));
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