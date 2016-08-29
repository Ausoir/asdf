package com.ga.unit.item;

import com.ga.tool.EventTimer;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.Man;
import com.ga.unit.Unit;
import com.ga.unit.Updater;

/**
 * Created by Ga on 8/12/2016.
 */
public class Projectile extends Unit {
    protected float rotSpeed = .03f;
    public int damage = 0;
    public EventTimer delay = new EventTimer(-1, false, null);
    public boolean tangible = true;
    public Projectile(String name, P pt, P dest, int speed) {
        super(name, 0, pt, 20);
        vel.set(dest.getAtan(pt, speed));
        rotation = (float) Math.atan2(dest.y-pt.y,dest.x-pt.x);
    }
    public void hit(Man hitter, int dmg){
        super.hit(hitter,dmg);
        remove();
    }
    public void onAttack(Unit p){
        remove();
    }
    public void update(){
        super.update();
        rotation+=rotSpeed*vel.x;
        if (tangible) {
            Unit u = Updater.collide(this, new Line(pos, pos.getAdd(vel)), damage, false);
            if (u != null) onAttack(u);
        }
        delay.update();
    }
    public String toString(){
        return "";
    }
}
