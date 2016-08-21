package com.ga.map;

import com.ga.game.FileManager;
import com.ga.game.Settings;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Thing;
import com.ga.unit.Updater;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Ga on 5/7/2016.
 */
public class Map {
    public static Thing panorama;
    public static Thing map;
    public static Thing fmap;
    public ArrayList<Line> land = new ArrayList<>();
    public P mapSize;
    private String[] maps;
    public Map(String[] maps, P pt) {
        this.maps = maps;
        final int mag = 3;
        panorama = new Thing(maps[0], pt, 1){
            @Override
            public void draw(Graphics2D g){
                P t = Global.getRelPos(pos.x,pos.y).getMult(.1f,.1f);
                t.x = Global.clamp(t.x,-size.x+Settings.screenX+50,0);
                t.y = Global.clamp(t.y,-size.y+Settings.screenY,0);
                g.drawImage(image,(int)(t.x),(int)(t.y),(int)size.x,(int)size.y,null);
            }
        };
        panorama.size.mult(mag,mag);
        panorama.pos = panorama.pos.getAdd(500,0);
        map = new Thing(maps[1], pt, 2){
            public void draw(Graphics2D g){
                P t = Global.getRelPos(pos.x,pos.y);
                t.x = Global.clamp(t.x,(-size.x)+Settings.screenX+50,0);
                t.y = Global.clamp(t.y,(-size.y)+Settings.screenY,0);
                g.drawImage(image,(int)t.x,(int)t.y,(int)size.x,(int)size.y,null);
            }
        };
        fmap = new Thing(maps[2], pt, 30){
            public void draw(Graphics2D g){
                P t = Global.getRelPos(pos.x,pos.y);
                t.x = Global.clamp(t.x,(-size.x)+Settings.screenX+50,0);
                t.y = Global.clamp(t.y,(-size.y)+Settings.screenY,0);
                g.drawImage(image,(int)t.x,(int)t.y,(int)size.x,(int)size.y,null);
            }
        };
        fmap.size.mult(mag,mag);
        map.size.mult(mag,mag);
    }
    public String toString(){
        String result = "";
        for (String i : maps){
            result += i+",";
        }
        result = result.substring(0,result.length()-1)+"\n";
        if (land.size() == 0) return result;
        for (Line i : land){
            result += i+";";
        }
        result = result.substring(0,result.length()-1);
        return result;
    }
    public void teleport(P p, Line l){
        for (Line i : land){
            if (i != l && i.pt.is(l.ptd)){
                p.set(i.ptd);
            }
        }
    }
    public void loadMap(int opt, boolean rdy){
        if (rdy){
            int mapnum = opt / 10, posnum = opt % 10;
            Updater.changeMap = maps[mapnum+3];
            Updater.changeMapPos = posnum;
        }
    }
    public boolean collide(P pos, P v, int offset){
        v.y += Settings.gravity;
        if (v.y > Settings.gravityMax) v.y = Settings.gravityMax;
        P op = pos.getAdd(0,offset);
        P vp = op.getAdd(v);
        Line pp = new Line(op,vp);
        boolean result = false;
        for (Line i : land){
            if (pp.lineCross(i)){
                float slope = i.getSlope();
                float t = (slope * v.x * 10000 - 5) / 10000f;
                if (t < v.y) {
                    result = true;
                    if (Math.abs(v.x) > 0 && t < 0) {
                        float xforce = (float) Math.cos(Math.atan(t));
                        if (xforce < .07){
                            v.set(0,0);
                            return true;
                        }
                        else v.x *= xforce;
                    }
                    v.x *= .9f;
                    v.y = (slope * v.x * 10000 - 5) / 10000f;
                } else if (i.opt == 1) v.y = 0;
                if (i.opt == 2) {
                    v.set(0, 0);
                    teleport(pos, i);
                    pos.add(0, -offset);
                } else if (i.opt >= 100){
                    if (i.opt >= 200) loadMap(i.opt-200, Global.player.teleportReady());
                    else loadMap(i.opt-100, true);
                }
            }
        }
        return result;
    }
}