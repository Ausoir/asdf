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
    public String[] maps;
    public Map(String[] maps, P pt) {
        this.maps = maps;
        final int mag = 3;
        panorama = new Thing("map/"+maps[0], pt, 1){
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
        map = new Thing("map/"+maps[1], pt, 2){
            public void draw(Graphics2D g){
                P t = Global.getRelPos(pos.x,pos.y);
                t.x = Global.clamp(t.x,(-size.x)+Settings.screenX+50,0);
                t.y = Global.clamp(t.y,(-size.y)+Settings.screenY,0);
                g.drawImage(image,(int)t.x,(int)t.y,(int)size.x,(int)size.y,null);
            }
        };
        fmap = new Thing("map/"+maps[2], pt, 30){
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
            FileManager.saveCharacter();
            int mapnum = opt / 10, posnum = opt % 10;
            Updater.changeMap = maps[mapnum+3];
            Updater.changeMapPos = posnum;
        }
    }
    public boolean fallSafe(P pos){
        Line line = new Line(pos.getAdd(0,100), pos.getAdd(0,9999));
        for (Line i : land){
            if (i.lineCross(line)) return true;
        }
        return false;
    }
}