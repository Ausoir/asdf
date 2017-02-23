package com.ga.unit.item;

import com.ga.tool.EventTimer;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Matter;
import com.ga.unit.Updater;

/**
 * Created by Ga on 8/12/2016.
 */
public class Projectile extends Matter {
    public float rotSpeed = .03f;
    public int damage = 0;
    public EventTimer delay = new EventTimer(-1, false, null);
    public Projectile(String name, P pt, P dest, int speed) {
        super(name, 0, 10);
        pos = pt;
        save = false;
        vel.set(dest.getAtan(pt, speed));
        rotation = (float) Math.atan2(dest.y-pt.y,dest.x-pt.x);
    }
    public int getDamage(){
        return damage;
    }
    public void onCollide(Line l){
        stuck();
    }
    protected void onAttack(Matter p){
        stuck();
    }
    public void stuck(){
        vel.set(0,0);
        gravity = true;
        tangible = false;
    }
    public void update(){
        rotation+=rotSpeed*vel.x;
        if (tangible) {
            Matter u = Updater.collide(this, new Line(pos, pos.getAdd(vel)), false);
            if (u != null) onAttack(u);
        }
        super.update();
        delay.update();
    }
}
