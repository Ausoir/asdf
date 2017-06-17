package com.ga.map;

import com.ga.game.FileManager;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Stock;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ga on 3/30/2017. asd
 */
public class Mob {
    private static final HashMap<String,ArrayList<String>> mobs = FileManager.readDialogue("raw/Mob.txt");
    public static void spawn(String p, Line land){
        float landSize = Math.abs(land.ptd.x-land.pt.x);
        if (landSize > 200) {
            ArrayList<String> types = mobs.get(p);
            String mob = types.get(Global.r.nextInt(types.size()));
            String[] units = mob.split(",");
            for (String unit : units) {
                String[] unitProperty = unit.split(":");
                for (int i = 0; i < Integer.parseInt(unitProperty[1]); i++) {
                    int tx = (int) (Global.r.nextFloat() * (land.ptd.x - land.pt.x));
                    if (land.clockwise()) tx = (int) Global.clamp(tx, 120, (land.ptd.x - land.pt.x) - 120);
                    else tx = (int) Global.clamp(tx, land.ptd.x - land.pt.x + 120, -120);
                    P pos = new P(land.pt.x + tx, (int) land.getCrossPointY(land.pt.x + tx));
                    Stock.getByName(unitProperty[0], -1).pos.set(pos);
                }
            }
        }
    }
}
