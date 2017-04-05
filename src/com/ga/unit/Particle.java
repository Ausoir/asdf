package com.ga.unit;

import com.ga.game.Settings;
import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Timer;

/**
 * Created by Ga on 8/29/2016.
 */
public class Particle extends Thing implements Effect {
    private Timer timer = new Timer(4, false);
    private P vel;
    private boolean gravity;
    public Particle(String name, P pt, P vel, float rotation, float speed, boolean gravity, int lifetime) {
        super(name, 800);
        pos = pt;
        save = false;
        this.vel = new P(vel);
        this.vel.add(Global.cos(rotation)*speed,Global.sin(rotation)*speed);
        this.gravity = gravity;
        this.rotation = rotation;
        this.timer.set(lifetime);
    }
    public static void dripBlood(P pt){
        explode("Blood",pt,new P(),3,1,0,true,3);
    }
    public static void dripFire(P pt){
        explode("Fire",pt,new P(0,-1),6,1,Settings.gravity,true,3);
    }
    public static void dripHoly(P pt){
        explode("Glare",pt,new P(),1,4,0,true,4);
    }
    public static void groundSlam(P pt){
        explode("Dust",pt,new P(0,-16),1,12,8,true,4);
    }
    public static void explosion(P pt){
        explode("Smoke",pt,new P(0,-16),50,15,15,false,7);
        explode("Fire",pt,new P(0,-24),100,15,10,true,4);
    }
    public static void holyExplosion(P pt){
        explode("Glare",pt,new P(0,-1),100,24,20,true,7);
    }
    public static void cleanse(P pt){
        explode("Glare",pt,new P(),1,16,0,false,2);
    }
    public static void frozen(P pt){
        explode("Ice",pt,new P(),3,3,0,false,12);
    }
    public static void smoke(P pt){
        explode("Smoke",pt,new P(),8,16,0,false,4);
    }
    public static void burn(P pt){
        Particle.explode("Flame", pt, new P(0, -5), 2, 7, 3, false, 4);
    }
    public static void implode(String name, P pt, P vel, int num, int speed, int velSpeed, boolean gravity, int lifetime){
        P t;
        for (int i=0;i<num;i++){
            float rot = Global.r.nextFloat()*6.28f;
            float spd = Global.r.nextFloat()*speed;
            t = new P(pt);
            t.add(Global.cos(rot)*spd*lifetime,Global.sin(rot)*spd*lifetime);
            new Particle(name,t,new P(),rot-3.14f,spd, gravity, lifetime);
        }
    }
    public static void explode(String name, P pt, P vel, int num, int speed, int velSpeed, boolean gravity, int lifetime){
        P v = vel.getAtan(velSpeed);
        for (int i=0;i<num;i++){
            new Particle(name,new P(pt),v, Global.r.nextFloat()*6.28f,Global.r.nextFloat()*speed, gravity, lifetime);
        }
    }
    public void collide(){
        P vp = pos.getAdd(vel);
        Line pp = new Line(pos,vp);
        for (Line i : Global.map.land){
            if (i.lineCross(pp)){
                if (i.isLand()) {
                    float slope = i.getSlope();
                    if (!Double.isInfinite(slope)) vel.y = slope * vel.x;
                    vel.x /= 2;
                }
            }
        }
    }
    @Override
    public boolean over() {
        if (gravity) vel.y += Settings.gravity;
        collide();
        pos.add(vel);
        return timer.update();
    }
}
