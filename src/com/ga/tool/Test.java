package com.ga.tool;

import com.ga.game.FileManager;

import java.io.File;

/**
 * Created by Ga on 12/13/2016. asdasdasdawdadqw
 */
public class Test {
    public static void main(String[] args){
        File dir = new File("Data/map/building");
        fixItems(dir);
    }
    private static void fixItems(File dir){
        if (dir.isDirectory()){
            for (File i : dir.listFiles()) fixItems(i);
        } else {
            T data = FileManager.readLiterals(dir.getPath());
            for (T i : data.get(3)){
                if (i.get(0).getInt() == 9){
                    i.get(8).get();
                }
            }
            data.refresh();
            FileManager.saveLiterals(dir.getPath(), data);
        }
    }
}