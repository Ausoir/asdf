package com.ga.unit.item;

import com.ga.map.Global;
import com.ga.tool.EventTimer;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Prop;
import com.ga.unit.Unit;
import com.ga.map.Updater;

/**
 * Created by Ga on 8/12/2016. asd
 */
public class Projectile extends Prop {
    public float rotSpeed = .03f;
    public int damage = 0;
    public EventTimer delay = new EventTimer(-1, false, null);
    protected Projectile(String name, P pt, P dest, int speed) {
        super(name, 0, 60);
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
    protected void onAttack(Unit p){
        stuck();
    }
    public void onField(){}
    public void stuck(){
        vel.set(0,0);
        gravity = true;
        tangible = false;
    }
    public int getCollisionSize(){
        return (int) vel.getDist()*2;
    }
    public void update(){
        rotation+=rotSpeed*vel.x;
        if (tangible) {
            for (Unit i : Updater.collideUnit(pos, getCollisionSize(), faction)){
                if (i.checkHit(new Line(pos,pos.getAdd(vel)),damage)) onAttack(i);
            }
        }
        if (Global.mapEdge(pos.getAdd(vel)) != 0) stuck();
        super.update();
        delay.update();
    }
}