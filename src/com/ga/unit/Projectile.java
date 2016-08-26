package com.ga.unit;

import com.ga.tool.P;

/**
 * Created by Ga on 8/12/2016.
 */
public class Projectile extends Unit {
    protected float rotSpeed = .02f;
    public int damage = 0;
    protected int range = 0;
    public Projectile(String name, P pt, P dest, int speed) {
        super(name, pt, 20);
        vel.set(dest.getAtan(pt, speed));
        rotation = (float) Math.atan2(dest.y-pt.y,dest.x-pt.x);
        range = (int) size.getDist();
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
        Unit u = Updater.collide(this, pos, damage, range, false);
        if (u != null) onAttack(u);
    }
    public String toString(){
        return "";
    }
}
