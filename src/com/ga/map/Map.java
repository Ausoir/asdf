package com.ga.map;

import com.ga.game.FileManager;
import com.ga.game.Settings;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Matter;
import com.ga.unit.Thing;
import com.ga.unit.Updater;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Ga on 5/7/2016.
 */
public class Map {
    public static Thing panorama;
    public static Thing map;
    public static Thing fmap;
    public ArrayList<Line> land = new ArrayList<>();
    public ArrayList<Terrain> landform = new ArrayList<>();
    private HashMap<Terrain, ArrayList<Line>> formLines = new HashMap<>();
    public HashMap<Integer, String> formTextures = new HashMap<>();
    public String[] maps;
    private MapEvent event;
    public static final int magnify = 2;
    public Map(final String[] maps, P pt) {
        this.maps = maps;
        event = new MapEvent(maps[0]);
        final boolean noPanorama = maps[1].equals("None");
        panorama = new Thing(maps[1], pt, 1) {
            private boolean none = noPanorama;
            @Override
            public void draw(Graphics2D g) {
                if (none) {
                    g.setColor(Color.black);
                    g.fillRect(0,0, Settings.screenX, Settings.screenY);
                } else {
                    P t = new P(Global.getRelX()+pos.x, Global.getRelY()+pos.y).mult(.1f);
                    t.x = Global.clamp(t.x, -image.size.x + Settings.screenX, 0);
                    t.y = Global.clamp(t.y, -image.size.y + Settings.screenY, 0);
                    g.drawImage(image.getImage(), (int) (t.x), (int) (t.y), (int) image.size.x, (int) image.size.y, null);
                }
            }
        };
        if (!noPanorama) {
            panorama.image.size.mult(magnify, magnify);
            panorama.pos = panorama.pos.getAdd(500, 0);
        } else panorama.image.visible = false;
        map = new Thing(null, pt, 2){
            public void draw(Graphics2D g){
                g.drawImage(image.getImage(),(int)(Global.getRelX()+pos.x),(int)(Global.getRelY()+pos.y),(int)image.size.x,(int)image.size.y,null);
            }
        };
        if (maps[2].startsWith("None")){
            map.setImage("None");
            map.image.visible = false;
            if (maps[2].length() > 4) {
                String[] size = maps[2].substring(5).split("x");
                map.image.size.set(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
            } else {
                map.image.size.set(Settings.screenX, Settings.screenY);
            }
        } else {
            map.setImage(maps[2]);
            map.image.size.mult(magnify,magnify);
        }
        fmap = new Thing(maps[3], pt, 30) {
            public void draw(Graphics2D g) {
                for (Terrain i : landform){
                    i.draw(g);
                }
                g.drawImage(image.getImage(),(int)(Global.getRelX()+pos.x),(int)(Global.getRelY()+pos.y),(int)image.size.x,(int)image.size.y,null);
            }
        };
        if (maps[3].equals("None")) fmap.image.visible = false;
        else fmap.image.size.mult(magnify, magnify);
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
        Iterator it = formTextures.entrySet().iterator();
        while (it.hasNext()){
            java.util.Map.Entry pair = (java.util.Map.Entry) it.next();
            result += pair.getKey() + ":" + pair.getValue();
            if (it.hasNext()) result += ";";
        }
        result += "\n";
        if (land.size() == 0) return result;
        for (Line i : land){
            if (!i.equals("")){
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
        for (Terrain i : landform){
            ArrayList<Line> t = formLines.get(i);
            for (Line ii : t){
                int origX = (int) (ii.pt.x-i.pos.x);
                int origY = (int) (ii.pt.y-i.pos.y);
                int x2 = (int) (ii.ptd.x-i.pos.x);
                i.shade(origX, x2, origY, ii.getSlope());
            }
        }
    }

    public void setTexture(){
        HashMap<Integer, Terrain> hashmap = new HashMap<>();
        for (Line i : land){
            if (i.next >= 0){
                Terrain terrain = hashmap.get(i.next);
                if (terrain == null){
                    String formTexture = formTextures.get(i.next);
                    if (formTexture == null) terrain = new Terrain("Dirt", null);
                    else {
                        String[] formName = formTextures.get(i.next).split("&");
                        terrain = new Terrain(formName[0], formName.length > 1 ? formName[1] : null);
                    }
                    hashmap.put(i.next, terrain);
                }
                if (formLines.get(terrain) == null) formLines.put(terrain, new ArrayList<Line>());
                formLines.get(terrain).add(i);
                terrain.addPoint(i.pt.x, i.pt.y);
            }
        }
        Iterator it = hashmap.entrySet().iterator();
        while (it.hasNext()) {
            java.util.Map.Entry pair = (java.util.Map.Entry) it.next();
            Terrain terrain = (Terrain) pair.getValue();
            terrain.render();
            landform.add(terrain);
        }
    }

    public void generateProps(){
        Line l;
        if (land.size() > 0) {
            for (int i = 0; i < 50; i++) {
                int tx, ty, timeout = 0;
                do {
                    l = land.get((int) (Global.r.nextFloat() * land.size()));
                    tx = (int) (Global.r.nextFloat() * (l.ptd.x - l.pt.x));
                    ty = (int) (l.pt.y + (int) (tx * l.getSlope()));
                    if (timeout++ > 8){
                        Global.p("Error generating prop");
                        return;
                    }
                } while (!l.clockwise());
                String texture = formTextures.get(l.next);
                if (texture != null) {
                    String[] formProps = formTextures.get(l.next).split("&");
                    if (formProps.length > 2) {
                        String prop = null;
                        String[] props = formProps[2].split(",");
                        if (props.length > 1) {
                            float rand = (float) Math.random();
                            for (String ii : props) {
                                if (rand < Float.parseFloat(ii.split("=")[1])) {
                                    prop = ii.split("=")[0];
                                    break;
                                }
                            }
                        } else {
                            prop = formProps[2];
                        }
                        if (prop != null) {
                            int xSize = Texture.load(prop).getWidth() / 2;
                            if (l.pt.x + tx + xSize < l.ptd.x && l.pt.x + tx - xSize > l.pt.x) {
                                Matter t = new Matter(prop, 0, new P(l.pt.x + tx, ty), 11) {
                                    public String toString() {
                                        return "";
                                    }
                                };
                                t.image.size.x *= Math.random() + .5f;
                                t.image.size.y *= Math.random() + .5f;
                                if (Math.random() < .5f) t.dir = -1;
                                t.pos.y -= t.image.size.y / 3;
                                t.rotation = (float) Math.atan(l.getSlope());
                            }
                        }
                    }
                }
            }
        }
    }

    public P randomSurface(){
        Line i;
        int tx, ty;
        do {
            i = land.get((int) (Global.r.nextFloat() * land.size()));
            tx = (int) (Global.r.nextFloat() * (i.ptd.x-i.pt.x));
            ty = (int) (i.pt.y + (int) (tx * i.getSlope()));
        } while (!i.clockwise());
        return new P(i.pt.x+tx,ty);
    }
}