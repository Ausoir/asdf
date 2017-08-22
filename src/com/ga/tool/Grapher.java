package com.ga.tool;

import com.ga.map.Global;
import com.ga.map.Updater;
import com.ga.unit.Man;
import com.ga.unit.Organ;
import com.ga.unit.Unit;

import java.awt.*;

/**
 * Created by ga on 5/14/17. qewqew
 */
public class Grapher {
    public static void draw(Graphics2D g){
        g.setColor(Color.RED);
        g.setStroke(new BasicStroke(1));
        for (Unit unit : Updater.unitsLoaded){
            for (Organ o : unit.clip.organs){
                g.drawLine((int)(unit.pos.x+o.pt.x+ Global.rel.x), (int)(unit.pos.y+o.pt.y+Global.rel.y),
                        (int)(unit.pos.x+o.ptd.x+Global.rel.x), (int)(unit.pos.y+o.ptd.y+Global.rel.y));
            }
            if (unit instanceof Man){
                Line test = ((Man)unit).weapon.getLine();
                test = test.getAdd(Global.rel);
                g.drawLine(test.pt.x(),test.pt.y(),test.ptd.x(), test.ptd.y());
            }
        }
    }
}
