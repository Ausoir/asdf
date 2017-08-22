package com.ga.map;

import com.ga.game.FileManager;
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
    public Weather weather = new Weather();
    public ArrayList<Line> land = new ArrayList<>();
    public ArrayList<Line> landRaw = new ArrayList<>();
    HashMap<Integer, String> formTextures = new HashMap<>();
    public HashMap<Integer, Terrain> terrains = new HashMap<>();
    private static HashMap<String, String> defaultProps = new HashMap<String, String>(){{
        put("grass","Rock=.1,Bush=.2,Weed=1");
        put("dirt","Rock=.2");
        put("rock","Rock=.2");
        put("road","Rock=.1");
        put("snow","Rock=.1,Bush=.1");
        put("sand","XBush=.1,Shell=.2,Starfish=.3");
    }};
    public P size;
    private String callname = "Somewhere";
    private String panorama = "None";
    private static final int magnify = 2;
    public Map(String name, P size) {
        super(null,0,1);
        this.name = name;
        this.size = size;
        image.visible = false;
        onTimeChange();
    }

    public void load(String dir){
        String data = FileManager.read("Data/map/" + dir + ".map");
        String[] line = data.split("%");
        String[] names = line[0].split(";");
        callname = names[0];
        setPanorama(names[1]);
        if (!panorama.equals("None")) {
            image.visible = true;
            setImage(panorama);
            image.getSize().mult(magnify);
        }
        this.size = new P(names[2]);
        if (line.length > 1) {
            String[] sections = line[1].split(";");
            for (String section1 : sections) {
                String values[] = section1.split(":");
                Global.map.formTextures.put(Integer.parseInt(values[0]), values[1]);
            }
            if (line.length > 2) {
                sections = line[2].split(";");
                for (String section1 : sections) {
                    Line surface = new Line(section1);
                    Global.map.landRaw.add(surface);
                    if (surface.opt != 1) Global.map.land.add(surface);
                }
                if (line.length > 3 && !line[3].equals(";")) {
                    sections = line[3].split(";");
                    if (sections.length > 0) {
                        for (String section1 : sections) {
                            Stock.get(section1).start();
                        }
                    }
                }
            }
        }
    }

    public String toString(){
        StringBuilder result = new StringBuilder(callname + ";" + panorama + ";" + size.x() + "," + size.y() + "\n");
        Iterator it = formTextures.entrySet().iterator();
        while (it.hasNext()){
            java.util.Map.Entry pair = (java.util.Map.Entry) it.next();
            result.append(pair.getKey()).append(":").append(pair.getValue());
            if (it.hasNext()) result.append(";");
        }
        result.append("\n");
        for (Line i : landRaw){
            if (!i.toString().equals("")) result.append(i).append(";");
        }
        if (landRaw.size() > 0) result = new StringBuilder(result.substring(0, result.length() - 1));
        return result.toString();
    }

    public void loadMap(String p){
        Updater.changeMap = p;
    }

    void clear(){
        land.clear();
        landRaw.clear();
        terrains.clear();
        formTextures.clear();
    }

    void render(int target){
        String texture = formTextures.get(target);
        String[] formName = texture.split("&");
        int terrainDepth = Terrain.toTerrainDepth(Integer.parseInt(formName[0]));
        Terrain terrain = new Terrain(formName[1], Shader.getByName(formName[2]), terrainDepth);
        terrains.put(target, terrain);
        for (final Line i : landRaw){
            if (i.next == target){
                terrain.addLine(i);
                terrain.addPoint(i.pt.x, i.pt.y);
            }
        }
        terrain.render();
    }

    public void setTexture(){
        for (final Line i : landRaw){
            if (i.next >= 0){
                Terrain terrain = terrains.get(i.next);
                if (terrain == null){
                    String formTexture = formTextures.get(i.next);
                    if (formTexture == null) continue;
                    else {
                        String[] formName = formTexture.split("&");
                        int terrainDepth = Terrain.toTerrainDepth(Integer.parseInt(formName[0]));
                        terrain = new Terrain(formName[1], Shader.getByName(formName[2]), terrainDepth);
                    }
                    terrains.put(i.next, terrain);
                }
                terrain.addLine(i);
                terrain.addPoint(i.pt.x, i.pt.y);
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
    }

    void generateProps(){
        for (Line l : land) {
            if (l.clockwise()) {
                if (Math.abs(l.getSlope()) < Line.MAXSLOPE
                        && (l.pt.y < size.y || l.ptd.y < size.y) && (l.pt.x < size.x || l.ptd.x < size.x)
                        && (l.pt.y > 0 || l.ptd.y > 0) && (l.pt.x > 0 || l.ptd.x > 0)) {
                    if (Math.abs(l.getSlope()) < .5f) {
                        String texture = formTextures.get(l.next);
                        if (texture != null) {
                            String[] formProps = formTextures.get(l.next).split("&");
                            int size = (int) (Math.abs(l.ptd.x - l.pt.x) * .02f);
                            for (int ii = 0; ii < size; ii++) {
                                String prop = null;
                                if (formProps.length > 3) {
                                    prop = formProps[3];
                                } else if (formProps.length > 2) {
                                    prop = defaultProps.get(formProps[2]);
                                }
                                if (prop != null) placeProp(l, prop, -1);
                            }
                        }
                    }
                }
            }
        }
    }

    Prop placeProp(Line l, String p, int seed){
        String prop = null;
        String[] props = p.split(",");
        float rand = Creation.r.nextFloat();
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
            Prop t = Stock.getByName(prop, seed);
            P size = t.image.getSize();
            if (landSize >= size.x) {
                float halfSizeX = size.x/2;
                int tx = (int) (Creation.r.nextFloat() * (l.ptd.x - l.pt.x));
                tx = (int) Global.clamp(tx, halfSizeX, (l.ptd.x - l.pt.x) - halfSizeX);
                t.pos = new P(l.pt.x + tx, (int) l.getCrossPointY(l.pt.x + tx));
                t.rotation = l.getAtan();
                return t;
            } else t.remove();
        }
        return null;
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

    public Line checkLand(Line p, int depth){
        ArrayList<Line> list = new ArrayList<>();
        for (Line i : land){
            if (i.clockwise() && i.depth >= depth && i.lineCross(p)){
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
            if (i.clockwise() && i.lineCross(p)){
                list.add(i);
            }
        }
        if (list.size() == 0) return null;
        else {
            Line closest = null;
            float closestY = p.ptd.y;
            for (Line i : list){
                float y = i.getCrossPointY(p.pt.x);
                if (y <= closestY){
                    closest = i;
                    closestY = y;
                }
            }
            return closest;
        }
    }

    void setPanorama(String p){
        this.panorama = p;
        image.visible = !p.equals("None");
        onTimeChange();
    }

    public void onTimeChange() {
        if (image.visible) {
            setImage(panorama + (weather.isNight() ? "_Night" : ""));
            image.setSize(image.getSize().getMult(magnify, magnify));
            weather.refreshTimeColor();
        }
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