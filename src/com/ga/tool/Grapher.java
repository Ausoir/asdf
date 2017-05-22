package com.ga.tool;

import com.ga.map.Global;
import com.ga.map.Updater;
import com.ga.unit.Man;
import com.ga.unit.Organ;
import com.ga.unit.Unit;

import java.awt.*;
import java.util.Map;

/**
 * Created by ga on 5/14/17. qewqew
 */
public class Grapher {
    public static void draw(Graphics2D g){
        g.setColor(Color.RED);
        g.setStroke(new BasicStroke(1));
        for (Map.Entry<Integer, Unit> i : Updater.unitList.entrySet()){
            Unit v = i.getValue();
            for (Organ o : v.clip.organs){
                g.drawLine((int)(v.pos.x+o.pt.x+ Global.rel.x), (int)(v.pos.y+o.pt.y+Global.rel.y),
                        (int)(v.pos.x+o.ptd.x+Global.rel.x), (int)(v.pos.y+o.ptd.y+Global.rel.y));
            }
            if (v instanceof Man){
                Line test = ((Man)v).weapon.getLine();
                test = test.getAdd(Global.rel);
                g.drawLine(test.pt.x(),test.pt.y(),test.ptd.x(), test.ptd.y());
            }
        }
    }
}
