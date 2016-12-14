package com.ga.tool;

import com.ga.unit.Clip;
import com.ga.game.FileManager;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ga on 7/26/2016.
 */
public class Clips {
    public static HashMap<String, Clip> clip = new HashMap<>();
    public static void init(){
        File[] files = new File("Data/unit").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".2d");
            }
        });
        for (File file : files) {
            if (file.isFile()) {
                String t = file.getName();
                clip.put(t.substring(0,t.lastIndexOf(".")), FileManager.readClip("unit/"+file.getName()));
            }
        }
    }
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
    public static int[] clone(int[] p){
        int[] result = new int[p.length];
        for (int ii=0;ii<p.length;ii++){
            result[ii] = p[ii];
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