package com.ga.unit;

import com.ga.game.Settings;
import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.tool.Interactive;
import com.ga.tool.Line;
import com.ga.tool.P;

/**
 * Created by Ga on 3/13/2017. asd
 */
public class Prop extends Thing {
    public int factoryId = -1;
    public int subId = -1;
    public Interactive onInteract = null;
    public boolean gravity = false;
    public boolean mobile = true;
    public Faction faction = Faction.Destructive;
    public P vel = new P(0, 0);
    public boolean tangible = true;
    public Line lastLand = new Line();
    public Tags tags = new Tags();
    boolean manualDepth = false;
    public Prop(Map map, String name, int seed) {
        super(map, name, seed, 1);
    }
    public int getDamage(){
        return 0;
    }
    public void randomSize(P sizeVariance){
        P s = image.getSize();
        float rx = rand.nextFloat() * (sizeVariance.x*2) + (1-sizeVariance.x);
        float ry = rand.nextFloat() * (sizeVariance.y*2) + (1-sizeVariance.y);
        image.setSize(s.x * rx, s.y * ry);
    }
    public void onCreate(){}
    public void onField(){
        if (!manualDepth) adjustToLand();
    }
    public void onAttack(Unit p){}
    private void adjustToLand(){
        Line closest = map.checkLand(new Line(pos.getAdd(0,-4), new P(pos.x, pos.y+600)));
        if (closest != null) {
            lastLand = closest;
            setDepth(closest.depth - 2);
        }
    }
    public int getSize(){
        return image.sizeRadius;
    }
    public boolean hittable(Faction faction){
        return tangible && faction.canAttack(this.faction);
    }
    public void collide(){
        P vp;
        Line pp;
        boolean collided = true;
        int timeout = 8;
        while (collided) {
            collided = false;
            vp = pos.getAdd(vel);
            pp = new Line(pos,vp);
            for (int i=0;i<map.land.size();i++) {
                Line land = map.land.get(i);
                if (land.depth >= lastLand.depth) {
                    if (land.lineCross(pp)) {
                        float slope = land.getSlope();
                        boolean cw = land.clockwise();
                        float lineY = land.getCrossPointY(pos.x);
                        boolean touching = cw && lineY >= pos.y;
                        if (touching && lastLand.depth == -1) {
                            Line testt = new Line(pos.x, pos.y - 9999, pos.x, pos.y);
                            int cws = 0, ccs = 0; //check if landsite is overlapped behind a higher depth polygon
                            boolean overlapped = false;
                            for (Line ii : map.landRaw) {
                                if (ii.depth > land.depth && ii.lineCross(testt)) {
                                    overlapped = true;
                                    if (ii.clockwise()) cws++;
                                    else ccs++;
                                }
                            }
                            if (overlapped && cws > ccs) touching = false;
                        }
                        if (touching) {
                            onCollide(land);
                            float atan = (float) Math.atan(slope);
                            if (vel.x > 0 != atan > 0) vel.x = vel.x * Global.cos(atan);
                            if (!Double.isInfinite(slope))
                                vel.y += land.getCrossPointY(pos.x+vel.x) - (pos.y + vel.y) - 1;
                            lastLand = land;
                            collided = true;
                        } else if (!cw && lineY <= pos.y) {
                            vel.set(0, 0);
                            return;
                        }
                    }
                }
            }
            timeout--;
            if (timeout == 0){
                System.out.println("Collision timeout pos@"+pos+"; vel@"+vel);
                return;
            }
        }
    }
    public void onCollide(Line l){
    }
    public void update(){
        if (mobile) {
            collide();
            int edge = map.mapEdge(pos.getAdd(vel));
            if (edge > 0) {
                if (Math.abs(edge-5) == 1) vel.x = 0;
                else if (edge == 2) remove();
            }
            pos.add(vel);
            if (gravity) {
                vel.y += Settings.gravity;
                if (vel.y > Settings.gravityMax) vel.y = Settings.gravityMax;
            }
            vel.x *= .94f;
        }
    }
    public String[] load(String p) {
        String[] ps = p.split("@");
        pos.load(ps[3]);
        rotation = Float.parseFloat(ps[4]);
        return ps;
    }

    @Override
    public String toString(){
        if (!save) return "";
        return (factoryId==-1?name:""+factoryId) + "@" + subId + "@" + seed + "@" + pos
                + "@" + String.format(java.util.Locale.US,"%.2f", rotation);
    }
}

