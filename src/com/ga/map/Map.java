package com.ga.map;

import com.ga.game.FileManager;
import com.ga.game.Settings;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Matter;
import com.ga.unit.Thing;
import com.ga.unit.Updater;
import com.ga.unit.item.Item;
import com.ga.unit.item.ItemFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Ga on 5/7/2016.
 */
public class Map {
    public Thing panorama;
    public Thing map;
    public Thing fmap;
    public ArrayList<Line> land = new ArrayList<>();
    public ArrayList<Terrain> landform = new ArrayList<>();
    private HashMap<Terrain, ArrayList<Line>> formLines = new HashMap<>();
    public HashMap<Integer, String> formTextures = new HashMap<>();
    private static HashMap<String, String> defaultProps = new HashMap<String, String>(){{
        put("grass","Rock=.1,Bush=.2,Weed=1");
        put("dirt","Rock=.1,Weed=.3,Bush=.1");
        put("road","Rock=.2");
        put("snow","Rock=.1,Weed=.3,Bush=.1");
    }};
    private static HashMap<String, String> defaultPropsCC = new HashMap<String, String>(){{
        put("dirt","Root=.1");
    }};
    private boolean inside = false;
    private Terrain blocker = null;
    public static String[] maps;
    public static MapEvent event;
    public static P size;
    public static final int magnify = 2;
    public Map(final String[] maps, P pt) {
        this.maps = maps;
        inside = maps[0].equals("1");
        event = MapEvent.getByName(maps[1]);
        final boolean noPanorama = maps[2].equals("None");
        panorama = new Thing(maps[2], pt, 1) {
            private boolean none = noPanorama;
            @Override
            public void draw(Graphics2D g) {
                if (none) {
                    g.setColor(Color.black);
                    g.fillRect(0,0, Settings.screenX, Settings.screenY);
                } else {
                    P t = new P(Global.rel.x+pos.x, Global.rel.y+pos.y).mult(.1f);
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
        if (!inside) {
            map = new Thing(null, pt, 2) {
                public void draw(Graphics2D g) {
                    g.drawImage(image.getImage(), (int) (Global.rel.x + pos.x), (int) (Global.rel.y + pos.y), (int) image.size.x, (int) image.size.y, null);
                }
            };
        } else {
            map = new Thing(null, pt, 2) {
                public void draw(Graphics2D g) {
                    g.drawImage(image.getImage(), (int) (Global.rel.x + pos.x), (int) (Global.rel.y + pos.y), (int) image.size.x, (int) image.size.y, null);
                    for (Terrain i : landform) i.draw(g);
                }
            };
        }
        int manualSize = maps[3].indexOf(":");
        String mapName = maps[3];
        if (manualSize != -1){
            String[] size = mapName.substring(manualSize+1).split("x");
            mapName = mapName.substring(0,manualSize);
            map.setImage(mapName);
            this.size = new P(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
        }
        if (mapName.startsWith("None")){
            map.image.visible = false;
            if (this.size == null) this.size = new P(Settings.screenX, Settings.screenY);
        } else {
            map.setImage(mapName);
            map.image.size.mult(magnify,magnify);
            if (this.size == null) this.size = new P(map.image.size);
        }
        if (!inside) {
            fmap = new Thing(maps[4], pt, 30) {
                public void draw(Graphics2D g) {
                    for (Terrain i : landform) i.draw(g);
                    g.drawImage(image.getImage(), (int) (Global.rel.x + pos.x), (int) (Global.rel.y + pos.y), (int) image.size.x, (int) image.size.y, null);
                }
            };
        } else {
            fmap = new Thing(maps[4], pt, 30) {
                public void draw(Graphics2D g) {
                    blocker.draw(g);
                    g.drawImage(image.getImage(), (int) (Global.rel.x + pos.x), (int) (Global.rel.y + pos.y), (int) image.size.x, (int) image.size.y, null);
                }
            };
        }
        if (maps[4].equals("None")) fmap.image.visible = false;
        else fmap.image.size.mult(magnify, magnify);
    }
    public String toString(){
        String result = maps[0]+","+event.toString()+",";
        for (int i=2;i<maps.length;i++){
            result += maps[i]+",";
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
        Updater.changeMap = maps[mapnum + 5];
        Updater.changeMapPos = posnum;
//        Global.server.sendFromLocal("DC@"+maps[mapnum+5]+"@"+posnum);
    }

    public boolean fallSafe(P pos){
        Line line = new Line(pos.getAdd(0,100), pos.getAdd(0,9999));
        for (Line i : land){
            if (i.lineCross(line)) return true;
        }
        return false;
    }

    public void clear(){
        land.clear();
        landform.clear();
        formLines.clear();
        formTextures.clear();
    }

    public void setShader() {
        for (Terrain i : landform){
            ArrayList<Line> t = formLines.get(i);
            if (t != null) {
                for (Line ii : t) {
                    int origX = (int) (ii.pt.x - i.pos.x);
                    int origY = (int) (ii.pt.y - i.pos.y);
                    int x2 = (int) (ii.ptd.x - i.pos.x);
                    i.shade(origX, x2, origY, ii.getSlope());
                }
            }
        }
    }

    public void setTexture(){
        HashMap<Integer, Terrain> hashmap = new HashMap<>();
        float highX = 0;
        Line high = null;
        if (inside) {
            blocker = new Terrain("black", null);
            for (final Line i : land) {
                if (i.next >= 0) {
                    if (i.pt.x > highX) {
                        highX = i.pt.x;
                        high = i;
                    }
                }
            }
        }
        for (final Line i : land){
            if (i.opt == 1){
                Terrain t = new Terrain("rope", null);
                t.addPoint(i.pt.x-8, i.pt.y);
                t.addPoint(i.pt.x+8, i.pt.y);
                t.addPoint(i.ptd.x+8, i.ptd.y);
                t.addPoint(i.ptd.x-8, i.ptd.y);
                t.render();
                landform.add(t);
                new Thing("Ropetop",i.pt,31).save = false;
            }
            if (i.next >= 0){
                Terrain terrain = hashmap.get(i.next);
                if (terrain == null){
                    String formTexture = formTextures.get(i.next);
                    if (formTexture == null) continue;
                    else {
                        String[] formName = formTexture.split("&");
                        terrain = new Terrain(formName[0], formName.length > 1 ? formName[1] : null);
                    }
                    hashmap.put(i.next, terrain);
                }
                if (formLines.get(terrain) == null) formLines.put(terrain, new ArrayList<Line>());
                formLines.get(terrain).add(i);
                terrain.addPoint(i.pt.x, i.pt.y);
                if (inside) blocker.addPoint(i.pt.x, i.pt.y);
                if (i == high){
                    blocker.addPoint(i.pt.x, i.pt.y);
                    blocker.addPoint(size.x, 0);
                    blocker.addPoint(0, 0);
                    blocker.addPoint(0, size.y);
                    blocker.addPoint(size.x, size.y);
                    blocker.addPoint(size.x, 0);
                    blocker.addPoint(i.pt.x, i.pt.y);
                }
            }
        }
        if (inside) blocker.render();
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
        for (int i = 0; i < land.size(); i++) {
            l = land.get(i);
            if (l.isLand() && Math.abs(l.getSlope()) < Line.MAXSLOPE-1) {
                String texture = formTextures.get(l.next);
                if (texture != null) {
                    String[] formProps = formTextures.get(l.next).split("&");
                    boolean cw = l.clockwise();
                    int size = (int) (Math.abs(l.ptd.x-l.pt.x) * .01f);
                    for (int ii = 0; ii < size; ii++) {
                        String prop = null;
                        if (cw) {
                            if (formProps.length > 2) {
                                prop = formProps[2];
                            } else if (formProps.length > 1) {
                                prop = defaultProps.get(formProps[1]);
                            }
                        } else {
                            prop = defaultPropsCC.get(formProps[0]);
                        }
                        if (prop != null) placeProp(l, prop);
                    }
                }
            }
        }
    }

    private void placeProp(Line l, String p){
        String prop = null;
        String[] props = p.split(",");
        float rand = (float) Math.random();
        if (props.length > 1) {
            for (String ii : props) {
                if (rand < Float.parseFloat(ii.split("=")[1])) {
                    prop = ii.split("=")[0];
                    break;
                }
            }
        } else {
            if (rand < Float.parseFloat(p.split("=")[1])) prop = p.split("=")[0];
        }
        if (prop != null) {
            int xSize = Texture.load(prop).getWidth() / 2;
            if (Math.abs(l.ptd.x-l.pt.x) > xSize*2) {
                int tx = (int) (Global.r.nextFloat() * (l.ptd.x - l.pt.x));
                if (l.clockwise()) tx = (int) Global.clamp(tx, xSize, (l.ptd.x - l.pt.x) - xSize);
                else tx = (int) Global.clamp(tx, l.ptd.x - l.pt.x + xSize, -xSize);
                int ty = (int) (l.pt.y + (int) (tx * l.getSlope()));
                Item t = ItemFactory.produce(prop, -2, 0);
                t.pos = new P(l.pt.x + tx, ty);
                t.field();
                t.image.size.x *= Math.random() + .5f;
                t.image.size.y *= Math.random() + .5f;
                if (Math.random() < .5f) t.dir = -1;
                t.rotation = (float) Math.atan(l.getSlope());
                if (!l.clockwise()){
                    t.pos.y += t.image.size.y / 4;
                    t.rotation += 3.14f;
                } else t.pos.y -= t.image.size.y / 4;
            }
        }
    }

    public String getLand(int p){
        String terrain = formTextures.get(p);
        if (terrain != null) {
            String t[] = formTextures.get(p).split("&");
            if (t.length > 1) return t[1];
            else return t[0];
        }
        return "none";
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

    public static boolean isInside(int x, int y){
        Line pp = new Line(x,y,999999,y);
        int num = 0;
        for (Line i : Global.map.land){
            if (i.isLand() && i.lineCross(pp)){
                num++;
            }
        }
        return num % 2 == 1;
    }
}