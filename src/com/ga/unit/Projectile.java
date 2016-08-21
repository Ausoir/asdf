package com.ga.unit;

import com.ga.tool.P;

/**
 * Created by Ga on 8/12/2016.
 */
public class Projectile extends Unit {
    public Projectile(String name, P pt, P dest, int speed) {
        super(name, pt, 20);
        vel.set(dest.getAtan(pt, 10));
    }
    public void hit(Man hitter, int dmg){
        super.hit(hitter,dmg);
        remove();
    }
    public void update(){
        super.update();
        rotation+=.02f*vel.x;
        if (Updater.collide(this, pos, 100, 100, false)) remove();
    }
}
