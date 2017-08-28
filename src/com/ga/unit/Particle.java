package com.ga.unit;

import com.ga.game.Settings;
import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.map.Picture;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Timer;

/**
 * Created by Ga on 8/29/2016. zxc
 */
public class Particle extends Thing implements Effect {
    private Timer timer = new Timer(4, false);
    private P vel;
    private boolean gravity;
    private Particle(Map map, String name, int depth, P pt, P vel, float rotation, float speed, boolean gravity, int lifetime) {
        super(map, name, Global.r.nextInt(9999), depth);
        pos = pt;
        save = false;
        this.vel = new P(vel);
        this.vel.add(Global.cos(rotation)*speed,Global.sin(rotation)*speed);
        this.gravity = gravity;
        this.rotation = rotation;
        this.timer.set(lifetime);
        start();
    }
    public void setImage(String p){
        image.set(Picture.loadCache(p));
    }
    public static void dripHoly(Map map, P pt, int depth){
        explode(map, "Glare",depth,pt,new P(),1,4,0,true,4);
    }
    public static void sparkle(Map map, String name, int depth, P pt, float radius, int times){
        for (int i=0;i<times;i++) {
            float x = (float) (Math.random()*radius-radius/2);
            float y = (float) (Math.random()*radius-radius/2);
            new Particle(map, name, depth, pt.getAdd(x,y), new P(), 0, 3, false, 1);
        }
    }
    public static void groundSlam(Map map, P pt, int depth){
        explode(map, "Dust",depth,pt,new P(0,-16),1,12,8,true,4);
    }
    static void explosion(Map map, P pt, int depth){
        explode(map, "Smoke",depth,pt,new P(0,-16),7,15,15,false,7);
        explode(map, "Fire",depth,pt,new P(0,-24),16,15,10,true,4);
    }
    public static void holyExplosion(Map map, P pt, int depth){
        explode(map, "Glare",depth,pt,new P(0,-1),24,24,20,true,7);
    }
    public static void shock(Map map, P pt, int depth){
        explode(map, "Glare",depth,pt,new P(0,0),3,48,0,false,3);
    }
    public static void cleanse(Map map, P pt, int depth){
        explode(map, "Glare",depth,pt,new P(),1,16,0,false,2);
    }
    public static void frozen(Map map, P pt, int depth){
        explode(map, "Ice",depth,pt,new P(),3,3,0,false,12);
    }
    public static void smoke(Map map, P pt, int depth){
        explode(map, "Smoke",depth,pt,new P(),7,16,0,false,4);
    }
    public static void burn(Map map, P pt, int depth){
        Particle.explode(map, "Flame",depth,pt,new P(0, -5), 2, 7, 3, false, 4);
    }
    public static void implode(Map map, String name, int depth, P pt, P vel, int num, int speed, int velSpeed, boolean gravity, int lifetime){
        P t;
        for (int i=0;i<num;i++){
            float rot = Global.r.nextFloat()*6.28f;
            float spd = Global.r.nextFloat()*speed;
            t = new P(pt);
            t.add(Global.cos(rot)*spd*lifetime,Global.sin(rot)*spd*lifetime);
            new Particle(map, name,depth,t,new P(),rot-3.14f,spd, gravity, lifetime);
        }
    }
    public static void explode(Map map, String name, int depth, P pt, P vel, int num, int speed, int velSpeed, boolean gravity, int lifetime){
        P v = vel.getAtan(velSpeed);
        for (int i=0;i<num;i++){
            new Particle(map, name,depth,new P(pt),v, Global.r.nextFloat()*6.28f,Global.r.nextFloat()*speed, gravity, lifetime);
        }
    }
    private void collide(){
        P vp = pos.getAdd(vel);
        Line pp = new Line(pos,vp);
        for (Line i : map.land){
            if (i.lineCross(pp)){
                if (i.clockwise()) {
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
