package com.ga.unit;

import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.unit.skill.Stat;

import java.awt.*;

/**
 * Created by Ga on 8/5/2016.
 */
public class PlayerHealthBar extends HealthBar {
    Stroke substroke, bsubstroke;
    Stat stamina, energy;
    public PlayerHealthBar(Stat stat, Stat stamina, Stat energy, P pos){
        super(stat,pos);
        this.stamina = stamina;
        this.energy = energy;
        substroke = new BasicStroke(2);
        bsubstroke = new BasicStroke(10);
    }
    public void draw(Graphics2D g){
        super.draw(g);
        if (!display.over()){
            g.setStroke(bsubstroke);
            g.setColor(Color.black);
            P t = Global.getRelPos(pos.x, pos.y);
            P a = t.getAdd(-48, 82), b = t.getAdd(48, 82);
            g.drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y);

            g.setStroke(substroke);
            g.setColor(Color.white);
            t = Global.getRelPos(pos.x, pos.y);
            P c = t.getAdd(-46, 80), d = t.getAdd(-46 + (stamina.getPercentage() * 92), 80);
            g.drawLine((int) c.x, (int) c.y, (int) d.x, (int) d.y);

            g.setStroke(substroke);
            g.setColor(Color.cyan);
            t = Global.getRelPos(pos.x, pos.y);
            c = t.getAdd(-46, 84);
            d = t.getAdd(-46 + (energy.getPercentage() * 92), 84);
            g.drawLine((int) c.x, (int) c.y, (int) d.x, (int) d.y);
        }
    }

    @Override
    public int getDepth() {
        return 35;
    }
}
