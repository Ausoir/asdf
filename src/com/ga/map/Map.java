package com.ga.map;

import com.ga.game.FileManager;
import com.ga.game.Settings;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Thing;
import com.ga.unit.Updater;

import java.awt.*;
import java.awt.image.BufferedImage;
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
    private MapEvent event;
    public static final int magnify = 3;
    public Map(final String[] maps, P pt) {
        this.maps = maps;
        event = new MapEvent(maps[0]);
        final boolean noPanorama = maps[1].equals("None");
        panorama = new Thing(noPanorama?null:maps[1], pt, 1) {
            private boolean none = noPanorama;
            @Override
            public void draw(Graphics2D g) {
                if (none) {
                    g.setColor(Color.black);
                    g.fillRect(0,0, Settings.screenX, Settings.screenY);
                } else {
                    P t = Global.getRelPos(pos.x, pos.y).getMult(.1f, .1f);
                    t.x = Global.clamp(t.x, -image.size.x + Settings.screenX, 0);
                    t.y = Global.clamp(t.y, -image.size.y + Settings.screenY, 0);
                    g.drawImage(image.getImage(), (int) (t.x), (int) (t.y), (int) image.size.x, (int) image.size.y, null);
                }
            }
        };
        if (!noPanorama) {
            panorama.image.size.mult(magnify, magnify);
            panorama.pos = panorama.pos.getAdd(500, 0);
        }
        map = new Thing(maps[2], pt, 2){
            public void draw(Graphics2D g){
                P t = Global.getRelPos(pos.x,pos.y);
                t.x = Global.clamp(t.x,(-image.size.x)+Settings.screenX,0);
                t.y = Global.clamp(t.y,(-image.size.y)+Settings.screenY,0);
                g.drawImage(image.getImage(),(int)t.x,(int)t.y,(int)image.size.x,(int)image.size.y,null);
            }
        };
        map.image.size.mult(magnify,magnify);
        if (!maps[3].equals("None")) {
            fmap = new Thing(maps[3], pt, 30) {
                public void draw(Graphics2D g) {
                    P t = Global.getRelPos(pos.x, pos.y);
                    t.x = Global.clamp(t.x, (-image.size.x) + Settings.screenX, 0);
                    t.y = Global.clamp(t.y, (-image.size.y) + Settings.screenY, 0);
                    g.drawImage(image.getImage(), (int) t.x, (int) t.y, (int) image.size.x, (int) image.size.y, null);
                }
            };
            fmap.image.size.mult(magnify, magnify);
        }
    }
    public int getHeight(){
        return (int) map.image.size.y;
    }
    public int getWidth(){
        return (int) map.image.size.x;
    }
    public String toString(){
        String result = "";
        for (String i : maps){
            result += i+",";
        }
        result = result.substring(0,result.length()-1)+"\n";
        if (land.size() == 0) return result;
        for (Line i : land){
            if (!i.equals("")){
                if (i.pt.x > i.ptd.x){
                    float t = i.pt.x;
                    i.pt.x = i.ptd.x;
                    i.ptd.x = t;
                    t = i.pt.y;
                    i.pt.y = i.ptd.y;
                    i.ptd.y = t;
                }
                result += i+";";
            }
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
    public void loadMap(int opt){
        int mapnum = opt / 10, posnum = opt % 10;
        Global.player.host.stats.end();
        FileManager.saveCharacter();
        Updater.changeMap = maps[mapnum + 4];
        Updater.changeMapPos = posnum;
//        Global.server.sendFromLocal("DC@"+maps[mapnum+4]+"@"+posnum);
    }
    public boolean fallSafe(P pos){
        Line line = new Line(pos.getAdd(0,100), pos.getAdd(0,9999));
        for (Line i : land){
            if (i.lineCross(line)) return true;
        }
        return false;
    }

    public void setShader() {
        BufferedImage image = map.image.getImage();
        for (Line i : land){
            int origX = (int) (i.pt.x/magnify);
            int origY = (int) (i.pt.y/magnify);
            int x = origX;
            int x2 = (int) (i.ptd.x/magnify);
            if (origX > x2){
                x = x2;
                x2 = origX;
            }
            for (int ix = x; ix < x2; ix++){
                int ty = origY+(int)((ix-origX)*i.getSlope());
                for (int iy = 0; iy < 20; iy++) {
                    int rgb = image.getRGB(ix, ty+iy);
                    int alpha = (rgb & 0xff000000) >>> 24;
                    int shadeMag = -(21-iy);
                    int red = Math.max(((rgb & 0xff0000) >>> 16)+shadeMag, 0);
                    int green = Math.max(((rgb & 0xff00) >>> 8)+shadeMag, 0);
                    int blue = Math.max(((rgb & 0xff))+shadeMag, 0);
                    image.setRGB(ix, ty + iy, new Color(red, green, blue, alpha).getRGB());
                }
            }
        }
    }

    public P randomSurface(){
        Line i = land.get((int) (Global.r.nextFloat()*land.size()));
        int origX = (int) (i.pt.x);
        int origY = (int) (i.pt.y);
        int x = origX;
        int x2 = (int) (i.ptd.x);
        if (origX > x2){
         x = x2;
         x2 = origX;
        }
        int tx = (int) (Global.r.nextFloat()*(x2-x));
        int ty = origY+(int)(tx*i.getSlope());
        return new P(origX+tx,ty);
    }
}