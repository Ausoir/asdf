package com.ga.map;

import com.ga.game.FileManager;
import com.ga.tool.P;
import com.ga.unit.Stock;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ga on 3/30/2017. asd
 */
public class Mob {
    private static final HashMap<String,ArrayList<String>> mobs = FileManager.readDialogue("raw/Mob.txt");
    public static void spawn(String p, P pos){
        ArrayList<String> types = mobs.get(p);
        String mob = types.get(Global.r.nextInt(types.size()));
        String[] units = mob.split(",");
        for (String unit : units){
            String[] unitProperty = unit.split(":");
            for (int i=0;i<Integer.parseInt(unitProperty[1]);i++){
                Stock.getByName(unitProperty[0],-1).pos.set(pos);
            }
        }
    }
}
