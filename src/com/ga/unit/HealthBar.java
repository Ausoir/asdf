package com.ga.unit;

import com.ga.map.Drawable;
import com.ga.map.Global;
import com.ga.map.Updatable;
import com.ga.tool.P;
import com.ga.tool.Timer;
import com.ga.unit.skill.Stat;

import java.awt.*;

/**
 * Created by Ga on 8/5/2016.
 */
public class HealthBar implements Drawable, Updatable{
    Stroke bstroke, fstroke;
    Stat stat;
    Timer display = new Timer(0, false);
    P pos;
    private boolean visible = false;
    public HealthBar(Stat stat, P pos){
        this.stat = stat;
        bstroke = new BasicStroke(20);
        fstroke = new BasicStroke(14);
        Updater.add(this);
        this.pos = pos;
    }
    public void show(int p){
        visible = true;
        display.set(p);
    }
    public void draw(Graphics2D g){
        if (visible) {
            g.setStroke(bstroke);
            g.setColor(Color.black);
            P t = Global.getRelPos(pos.x, pos.y);
            P a = t.getAdd(-48, 70), b = t.getAdd(48, 70);
            g.drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y);
            if (stat.getPercentage() >= .1f) {
                g.setStroke(fstroke);
                g.setColor(Color.green);
                P c = t.getAdd(-46, 70), d = t.getAdd(-46 + (stat.getPercentage() * 92), 70);
                g.drawLine((int) c.x, (int) c.y, (int) d.x, (int) d.y);
            }
        }
    }
    public void update(){
        if (display.update()){
            visible = false;
        }
    }

    @Override
    public int getDepth() {
        return 35;
    }
}
