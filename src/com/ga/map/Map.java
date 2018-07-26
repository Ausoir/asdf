package com.ga.map;

import com.ga.game.FileManager;
import com.ga.game.Settings;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
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
public class Map {
    int margin = 0;
    public Weather weather = new Weather(this);
    public ArrayList<Line> land = new ArrayList<>();
    public ArrayList<Line> landRaw = new ArrayList<>();
    public HashMap<Integer, String> formTextures = new HashMap<>();
    public HashMap<Integer, Terrain> terrains = new HashMap<>();
    private static HashMap<String, String> defaultProps = new HashMap<String, String>() {{
        put("grass", "Rock=.1,Bush=.2,Weed=1");
        put("dirt", "Rock=.2");
        put("rock", "Rock=.2");
        put("road", "Rock=.1");
        put("snow", "Rock=.1,Bush=.1");
        put("sand", "XBush=.1,Shell=.2,Starfish=.3");
    }};
    public P size;
    public MapName name;
    String callname = "Somewhere";
    private String panoramaName = "";
    private Thing[] panorama = new Thing[3];
    public Updater updater;
    int temperature = 0;

    Map(String name) {
        this.name = new MapName(name);
        updater = new Updater(this);
    }

    public void start() {
        if (name.isField() && !FileManager.isFile(FileManager.makeDirectory("Data/map/" + name + ".map"))) {
            GenerateBiome.generate(this, name);
            generateProps();
            FileManager.saveMap(name.value, this);
            clear();
        }
        load(name.value);
        weather.start();
        onTimeChange();
    }

    public void load(String dir) {
        String data = FileManager.read(FileManager.makeDirectory("Data/map/" + dir + ".map"));
        String[] line = data.split("%");
        String[] names = line[0].split(";");
        callname = names[0];
        this.size = new P(names[2]);
        panoramaName = names[1];
        if (names[1].equals("")) {
            panorama[0] = new Thing(this, "", 0, 0) {
                @Override
                public void draw(Graphics2D g) {
                    g.setColor(Color.black);
                    g.fillRect(0, 0, Settings.screenX, Settings.screenY);
                }
            }.start();
        } else {
            for (int i=0;i<3;i++){
                float depth = (i+.1f) * .1f;
                panorama[i] = new Thing(this, panoramaName+ "_" + i, 0, depth) {
                    @Override
                    public void setImage(String p){
                        super.setImage(p);
                        P imgSize = image.getSize();
                        imgSize.x = Math.max(Settings.screenX, Math.max((map.size.x-Settings.screenX)*depth+Settings.screenX,imgSize.x));
                        imgSize.y = Math.max(Settings.screenY, Math.max((map.size.y-Settings.screenY)*depth+Settings.screenY,imgSize.y));
                    }
                    @Override
                    public void draw(Graphics2D g) {
                        P size = image.getSize();
                        P t = Global.rel.getMult(depth, depth).add(pos);
                        g.drawImage(image.getImage(), (int) (t.x), (int) (t.y), (int) size.x, (int) size.y, null);
                    }
                }.start();
            }
            Global.display.onResize.add(new Performer(){
                public void perform(){
                    for (Thing i : panorama) i.setImage(i.name);
                }
            });
        }
        this.temperature = names.length<=3?0:Integer.parseInt(names[3]);
        if (line.length > 1) {
            String[] sections = line[1].split(";");
            if (!line[1].equals("")) {
                for (String section1 : sections) {
                    String values[] = section1.split(":");
                    formTextures.put(Integer.parseInt(values[0]), values[1]);
                }
            }
            if (line.length > 2) {
                if (!line[2].equals("")) {
                    sections = line[2].split(";");
                    for (String section1 : sections) {
                        Line surface = new Line(section1);
                        landRaw.add(surface);
                        //set depth here so next block prop spawns auto adjust to correct land depth
                        surface.depth = Float.parseFloat(formTextures.get(surface.tid).split("&")[0]) * .1f + 3;
                        if (surface.opt > 0) land.add(surface);
                    }
                }
                if (line.length > 3 && !line[3].equals(";")) {
                    sections = line[3].split(";");
                    if (sections.length > 0) {
                        for (String section1 : sections) {
                            Stock.get(this, section1).start();
                        }
                    }
                }
            }
        }
    }

    int getNextTerrain() {
        int high = -1;
        int nextPolygon = 0;
        for (Line i : landRaw) {
            if (i.tid > high) {
                high = i.tid;
                nextPolygon = i.tid + 1;
            }
        }
        return nextPolygon;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(callname + ";" + panoramaName + ";" + size.x() + "," + size.y() + ";" + temperature + "\n");
        Iterator it = formTextures.entrySet().iterator();
        while (it.hasNext()) {
            java.util.Map.Entry pair = (java.util.Map.Entry) it.next();
            result.append(pair.getKey()).append(":").append(pair.getValue());
            if (it.hasNext()) result.append(";");
        }
        result.append("\n");
        ArrayList<Line> landRawClone = (ArrayList<Line>) landRaw.clone();
        for (Line i : landRawClone) {
            if (!i.toString().equals("")) result.append(i).append(";");
        }
        if (landRawClone.size() > 0) result = new StringBuilder(result.substring(0, result.length() - 1));
        result.append("\n").append(updater.toString());
        return result.toString();
    }

    public void clear() {
        updater.clear();
        land.clear();
        landRaw.clear();
        terrains.clear();
        formTextures.clear();
    }

    public void setTexture() {
        for (final Line i : landRaw) {
            if (i.tid >= 0) {
                String formTexture = formTextures.get(i.tid);
                String[] formName = formTexture.split("&");
                float depth = Float.parseFloat(formName[0]);
                Terrain terrain = terrains.get(i.tid);
                if (terrain == null) {
                    terrain = new Terrain(this, formName[1], Shader.getByName(formName[2]), depth);
                    terrains.put(i.tid, terrain);
                    terrain.start();
                }
                terrain.addLine(i);
                terrain.addPoint(i.pt.x, i.pt.y);
            } else {
                String depth = formTextures.get(i.tid);
                if (depth != null) i.depth = Integer.parseInt(formTextures.get(i.tid));
                else i.depth = 0;
            }
        }
    }

    public void render() {
        for (Object o : terrains.entrySet()) {
            java.util.Map.Entry pair = (java.util.Map.Entry) o;
            Terrain terrain = (Terrain) pair.getValue();
            terrain.render();
        }
    }

    void generateProps() {
        for (Line l : landRaw) { //generation happens before loading into land from landRaw
            if (l.clockwise() && l.opt > 0) {
                if (Math.abs(l.getSlope()) < Line.MAXSLOPE
                        && (l.pt.y < size.y || l.ptd.y < size.y) && (l.pt.x < size.x || l.ptd.x < size.x)
                        && (l.pt.y > 0 || l.ptd.y > 0) && (l.pt.x > 0 || l.ptd.x > 0)) {
                    if (Math.abs(l.getSlope()) < .5f) {
                        String texture = formTextures.get(l.tid);
                        if (texture != null) {
                            String[] formProps = formTextures.get(l.tid).split("&");
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

    Prop placeProp(Line l, String p, int seed) {
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
            float landSize = Math.abs(l.ptd.x - l.pt.x);
            Prop t = Stock.createByName(this, prop, seed);
            t.manualDepth = false;
            P size = t.image.getSize();
            if (landSize >= size.x) {
                float halfSizeX = size.x / 2;
                int tx = (int) (Creation.r.nextFloat() * (l.ptd.x - l.pt.x));
                tx = (int) Global.clamp(tx, halfSizeX, (l.ptd.x - l.pt.x) - halfSizeX);
                t.pos = new P(l.pt.x + tx, (int) l.getCrossPointY(l.pt.x + tx));
                t.rotation = l.getAtan();
                t.start();
                return t;
            }
        }
        return null;
    }

    public String getLand(int p) {
        String terrain = formTextures.get(p);
        if (terrain != null) {
            String t[] = formTextures.get(p).split("&");
            if (t.length > 1) return t[1];
            else return t[0];
        }
        return "none";
    }

    public Line checkLand(Line p, float depth) {
        ArrayList<Line> list = new ArrayList<>();
        for (Line i : landRaw) {
            if (i.clockwise() && i.depth >= depth && i.lineCross(p)) {
                list.add(i);
            }
        }
        if (list.size() == 0) return null;
        else {
            Line highest = null;
            for (Line i : list) {
                if (highest == null || i.depth > highest.depth) {
                    highest = i;
                }
            }
            return highest;
        }
    }

    public Line checkLand(Line p) {
        ArrayList<Line> list = new ArrayList<>();
        for (Line i : landRaw) {
            if (i.clockwise() && i.lineCross(p)) {
                list.add(i);
            }
        }
        if (list.size() == 0) return null;
        else {
            Line closest = null;
            float closestY = p.ptd.y;
            for (Line i : list) {
                float y = i.getCrossPointY(p.pt.x);
                if (y <= closestY) {
                    closest = i;
                    closestY = y;
                }
            }
            return closest;
        }
    }

    public P getFailsafePos() {
        return new P(size.x / 2, 36);
    }

    public void onTimeChange() {
        if (panorama != null && !panoramaName.equals("")) {
            for (int i=0;i<panorama.length;i++){
                panorama[i].name = panoramaName + ("_"+i) + (weather.isNight() ? "_Night" : "");
                panorama[i].setImage(panorama[i].name);
            }
            weather.refreshTimeColor();
        }
        updater.onTimeChange();
    }

    public int mapEdge(P p) {
        int result = 0;
        if (p.x < 0) result = 4;
        else if (p.x > size.x) result = 6;
        if (p.y < 0) result = 8;
        else if (p.y > size.y) result = 2;
        return result;
    }

    public P mapEdgeCenter(P p) {
        if (size.x > Settings.screenX && size.y > Settings.screenY)
            return new P(Global.clamp(p.x, Settings.centerX + margin, size.x - Settings.centerX - margin),
                    Global.clamp(p.y, Settings.centerY + 40 + margin, size.y - Settings.centerY - margin - margin));
        return p;
    }

    void clean() {
        Iterator it = formTextures.entrySet().iterator();
        while (it.hasNext()) {
            boolean check = false;
            java.util.Map.Entry pair = (java.util.Map.Entry) it.next();
            int key = (int) pair.getKey();
            for (Line i : landRaw) {
                if (i.tid == key) {
                    check = true;
                    break;
                }
            }
            if (!check) {
                it.remove();
            }
        }
    }

    void normalize(int marginX, int marginY) {
        P high = new P(), low = size.getAdd(0, 0);
        for (Line i : landRaw) {
            low.x = Math.min(Math.min(low.x, i.pt.x), i.ptd.x);
            low.y = Math.min(Math.min(low.y, i.pt.y), i.ptd.y);
            high.x = Math.max(Math.max(high.x, i.pt.x), i.ptd.x);
            high.y = Math.max(Math.max(high.y, i.pt.y), i.ptd.y);
        }
        P margin = new P(marginX, marginY);
        int maxDiffX = (int) (high.x - low.x) + margin.x();
        int maxDiffY = (int) (high.y - low.y) + margin.y();
        size = new P(maxDiffX, maxDiffY);
        P adjustment = new P(-low.x + marginX / 2, -low.y + marginY / 2);
        adjust(adjustment);
        if (updater != null) updater.adjust(adjustment);
    }

    void adjust(P p) {
        for (Line i : landRaw) {
            i.pt = i.pt.getAdd(p);
            i.ptd = i.ptd.getAdd(p);
        }
    }
}