package com.ga.unit.item;

import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.tool.EventTimer;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Prop;
import com.ga.unit.Unit;

/**
 * Created by Ga on 8/12/2016. asd
 */
public class Projectile extends Prop {
    public float rotSpeed = .03f;
    public int damage = 0;
    public boolean blunt = true;
    public EventTimer delay = new EventTimer(-1, false, null);
    private Projectile(Map map, String name, P pt, float rot, int speed) {
        super(map, name, name, 0);
        pos = pt;
        save = false;
        vel.set(Global.cos(rot)*speed, Global.sin(rot)*speed);
        rotation = rot;
        setDepth(100);
        start();
    }
    protected Projectile(Map map, String name, P pt, P dest, int speed) {
        this(map, name, pt, (float) Math.atan2(dest.y-pt.y, dest.x-pt.x), speed);
    }
    @Override
    public int getDamage(){
        return damage;
    }
    @Override
    public void onCollide(Line l){
        stuck();
    }
    @Override
    public void onAttack(Unit p){
        stuck();
    }
    @Override
    public void onField(){}
    public void stuck(){
        vel.x = 0;
        gravity = true;
        tangible = false;
    }
    @Override
    public void update(){
        rotation+=rotSpeed*vel.x;
        if (tangible) {
            for (Unit i : map.updater.unitsLoaded){
                if (i.hittable(faction) && i.hit(new Line(pos,pos.getAdd(vel)),blunt,damage)) onAttack(i);
            }
        }
        super.update();
        delay.update();
    }
}