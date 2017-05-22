package com.ga.map;

import com.ga.game.Settings;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Prop;
import com.ga.unit.Stock;
import com.ga.unit.Thing;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Ga on 5/7/2016. asd
 */
public class Map extends Thing {
    Weather weather = new Weather();
    public ArrayList<Line> land = new ArrayList<>();
    public HashMap<Integer, String> formTextures = new HashMap<>();
    private static HashMap<String, String> defaultProps = new HashMap<String, String>(){{
        put("grass","Rock=.1,Bush=.2,Stone=.1,Weed=1");
        put("dirt","Rock=.2");
        put("rock","Rock=.2");
        put("road","Rock=.1");
        put("snow","Rock=.1,Bush=.1");
        put("sand","XBush=.1,Shell=.2,Starfish=.3");
    }};
    private static HashMap<String, String> defaultPropsCC = new HashMap<String, String>(){{
        put("dirt","Root=.15");
        put("grass","Vine=.8");
    }};
    private Terrain blocker = null;
    public String[] maps;
    public MapEvent event;
    public P size;
    public int id;
    private static final int magnify = 2;
    private boolean panorama = true;
    public Map(String name, final String[] maps) {
        super(null,0,1);
        this.name = name;
        this.maps = maps;
        id = Integer.parseInt(name.split("_")[1]);
        if (maps[2].equals("None")){
            image.visible = false;
            panorama = false;
        } else{
            setImage(maps[2]);
            image.getSize().mult(magnify);
        }
        event = MapEvent.getByName(maps[1]);
        String[] size = maps[3].split("x");
        this.size = new P(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
        onTimeChange();
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
            if (!i.toString().equals("")){
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

    public void loadMap(String p){
        Updater.changeMap = p;
    }

    void clear(){
        land.clear();
        formTextures.clear();
    }

    public void setTexture(){
        float highX = 0;
        Line high = null;
        boolean inside = isIndoors();
        if (inside) {
            blocker = new Terrain("black", Shader.Basic, 10){
                public int getDepth() {
                    return 150;
                }
            };
            for (final Line i : land) {
                if (i.next == 0) {
                    if (i.pt.x > highX) {
                        highX = i.pt.x;
                        high = i;
                    }
                }
            }
        }
        int blockerTerrain = inside ? 0 : -1;
        HashMap<Integer, Terrain> terrains = new HashMap<>();
        for (final Line i : land){
            if (i.opt == 1){
                Line land = checkLand(new Line(i.ptd.getAdd(0,-1), i.ptd.getAdd(0,1)), 0);
                if (land != null) {
                    Terrain t = new Terrain("rope", null, land.depth);
                    t.addPoint(i.pt.x - 8, i.pt.y);
                    t.addPoint(i.pt.x + 8, i.pt.y);
                    t.addPoint(i.ptd.x + 8, i.ptd.y);
                    t.addPoint(i.ptd.x - 8, i.ptd.y);
                    t.render();
                    Thing top = new Thing("Ropetop", 0, land.depth);
                    top.save = false;
                    top.pos = i.pt;
                }
            } else if (i.next >= 0){
                Terrain terrain = terrains.get(i.next);
                if (terrain == null){
                    String formTexture = formTextures.get(i.next);
                    if (formTexture == null) continue;
                    else {
                        String[] formName = formTexture.split("&");
                        int terrainDepth = Terrain.toTerrainDepth(Integer.parseInt(formName[0]));
                        if (inside) terrain = new Terrain(formName[1], Shader.getByName(formName[2]), terrainDepth){
                            public int getDepth(){
                                return 20-super.getDepth();
                            }
                        };
                        else terrain = new Terrain(formName[1], Shader.getByName(formName[2]), terrainDepth);
                    }
                    terrains.put(i.next, terrain);
                }
                terrain.addLine(i);
                terrain.addPoint(i.pt.x, i.pt.y);
                if (blockerTerrain == i.next) {
                    blocker.addPoint(i.pt.x, i.pt.y);
                    if (i == high){
                        blocker.addPoint(i.pt.x, i.pt.y);
                        blocker.addPoint(size.x+128, -128);
                        blocker.addPoint(-128, -128);
                        blocker.addPoint(-128, size.y+128);
                        blocker.addPoint(size.x+128, size.y+128);
                        blocker.addPoint(size.x+128, -128);
                        blocker.addPoint(i.pt.x, i.pt.y);
                    }
                }
            } else {
                String depth = formTextures.get(i.next);
                if (depth != null) i.depth = Terrain.toTerrainDepth(Integer.parseInt(formTextures.get(i.next)));
                else i.depth = Terrain.toTerrainDepth(0);
            }
        }
        for (Object o : terrains.entrySet()) {
            java.util.Map.Entry pair = (java.util.Map.Entry) o;
            Terrain terrain = (Terrain) pair.getValue();
            terrain.render();
        }
        if (inside) blocker.render();
    }

    void generateProps(){
        for (Line l : land) {
            if (l.isLand() && Math.abs(l.getSlope()) < Line.MAXSLOPE
                    && (l.pt.y < size.y || l.ptd.y < size.y) && (l.pt.x < size.x || l.ptd.x < size.x)
                    && (l.pt.y > 0 || l.ptd.y > 0) && (l.pt.x > 0 || l.ptd.x > 0)) {
                if (Math.abs(l.getSlope()) < .5f) {
                    String texture = formTextures.get(l.next);
                    if (texture != null) {
                        String[] formProps = formTextures.get(l.next).split("&");
                        boolean cw = l.clockwise();
                        int size = (int) (Math.abs(l.ptd.x - l.pt.x) * .02f);
                        for (int ii = 0; ii < size; ii++) {
                            String prop = null;
                            if (cw) {
                                if (formProps.length > 3) {
                                    prop = formProps[3];
                                } else if (formProps.length > 2) {
                                    prop = defaultProps.get(formProps[2]);
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
    }

    void placeProp(Line l, String p){
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
            float landSize = Math.abs(l.ptd.x-l.pt.x);
            if (landSize > 72) {
                int tx = (int) (Global.r.nextFloat() * (l.ptd.x - l.pt.x));
                Prop t = Stock.getByName(prop, -1);
                P size = t.image.getSize();
                if (landSize < size.x){
                    t.image.setSize(landSize, size.y);
                }
                int xSize = (int) Math.abs(size.x/2);
                if (l.clockwise()) tx = (int) Global.clamp(tx, xSize, (l.ptd.x - l.pt.x) - xSize);
                else tx = (int) Global.clamp(tx, l.ptd.x - l.pt.x + xSize, -xSize);
                int ty = (int) l.getCrossPointY(tx+l.pt.x);
                t.pos = new P(l.pt.x + tx, ty);
                if (!l.clockwise()){
                    t.pos.y += (size.y / 2)-4;
                    t.rotation = 3.14f;
                } else {
                    t.rotation = l.getSlope();
                }
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

    P randomLandableSurface(){
        Line i;
        int tx, ty;
        do {
            i = land.get((int) (Global.r.nextFloat() * land.size()));
            tx = (int) (Global.r.nextFloat() * (i.ptd.x-i.pt.x));
            ty = (int) (i.pt.y + (int) (tx * i.getSlope()));
        } while (!i.clockwise() || i.getSlope() > Line.MAXSLOPE);
        return new P(i.pt.x+tx,ty);
    }

    public Line checkLand(Line p, int depth){
        ArrayList<Line> list = new ArrayList<>();
        for (Line i : land){
            if (i.isLandable() && i.depth >= depth && i.lineCross(p)){
                list.add(i);
            }
        }
        if (list.size() == 0) return null;
        else {
            Line highest = null;
            for (Line i : list){
                if (highest == null || i.depth > highest.depth){
                    highest = i;
                }
            }
            return highest;
        }
    }

    public Line checkLand(Line p){
        ArrayList<Line> list = new ArrayList<>();
        for (Line i : land){
            if (i.isLandable() && i.depth >= depth && i.lineCross(p)){
                list.add(i);
            }
        }
        if (list.size() == 0) return null;
        else {
            Line closest = null;
            float closestY = p.ptd.y;
            for (Line i : list){
                float y = i.getCrossPointY(pos.x);
                if (y <= closestY){
                    closest = i;
                    closestY = y;
                }
            }
            return closest;
        }
    }

    public boolean isIndoors(){
        return id < 0;
    }

    public void onTimeChange() {
        if (panorama){
            setImage(maps[2]+(weather.isNight()?"_Night":""));
            image.setSize(image.getSize().getMult(magnify, magnify));
        }
        weather.setColor();
    }

    @Override
    public void draw(Graphics2D g) {
        if (image.visible) {
            P t = new P(Global.rel.x + pos.x, Global.rel.y + pos.y).mult(.1f);
            P size = image.getSize();
            t.x = Global.clamp(t.x, -size.x + Settings.screenX, 0);
            t.y = Global.clamp(t.y, -size.y + Settings.screenY, 0);
            g.drawImage(image.getImage(), (int) (t.x), (int) (t.y), (int) size.x, (int) size.y, null);
        } else {
            g.setColor(Color.black);
            g.fillRect(0,0, Settings.screenX, Settings.screenY);
        }
    }
}