package com.ga.map;

import com.ga.game.FileManager;
import com.ga.game.Observer;
import com.ga.game.Settings;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Prop;
import com.ga.unit.Stock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by ga on 5/28/17. asd
 */
public class Generator {
    private static HashMap<String, Integer> buildings = new HashMap<>();
    private Map map;
    public Generator(Map map, int next){
        this.map = map;
        if (next != -1) TerrainTrail.id = next;
    }
    public static void init(){
        for (String[] i : FileManager.fileCache.get("map")){
            if (i[1].startsWith("Data/map/building")) {
                buildings.put(i[0], Integer.valueOf(FileManager.read(i[1].substring(5)).split("%")[0].split(";",-1)[2].split(",")[0]));
            }
        }
    }
    private void generateFoundations(int max, P mapSize, float maxY, String texture){
        float lastY = maxY;
        for (int i=0;i<max;i++) {
            int yRise = Creation.r.nextInt(400)+240;
            P point = new P(Creation.r.nextInt((int) (map.size.x*(3f/5)))+(map.size.x*(1f/5)),lastY-yRise);
            int width = (int) Global.clamp((float) Math.abs(mapSize.x * (Creation.r.nextGaussian() * .4f)), 120, mapSize.x * (7f / 8));
            P pt1 = new P(Math.max(0, point.x - width), point.y);
            P pt2 = new P(Math.min(mapSize.x, point.x + width), point.y);
            int finalWidth = (int) (pt2.x - pt1.x);
            if (finalWidth <= 360) {
                pt1.x = Math.max(pt1.x-360, 0);
                pt2.x = Math.min(pt2.x+360,mapSize.x-360);
            }
            int midWidth = (int) (yRise * .2f);
            int mid = (int) (pt1.x + finalWidth/2 + Creation.r.nextInt(midWidth*2) - midWidth);
            P pt11 = new P(mid-midWidth, point.y);
            P pt12 = new P(mid+midWidth, point.y);
            P lowPt1, lowPt2;
            if (Creation.r.nextBoolean()) {
                lowPt1 = pt1;
                lowPt2 = pt11;
            } else {
                lowPt1 = pt12;
                lowPt2 = pt2;
            }
            lowPt1.y += yRise/2;
            lowPt2.y += yRise/2;
            int id = max-i;
            int ladderX = Creation.r.nextInt(lowPt2.x()-lowPt1.x()-80)+lowPt1.x()+40;
            float ladderDepth = id+.01f;
            Line ground = map.checkLand(new Line(new P(ladderX, lowPt1.y), new P(ladderX, lowPt1.y+99999)), id);
            if (ground != null) {
                for (int y = lowPt1.y()+40; y < (int) ground.getCrossPointY(ladderX); y += 40) {
                    TerrainTrail ladder = new TerrainTrail(map, new P(ladderX - 40, y-20));
                    ladder.move(80, 0);
                    ladder.move(0, 20);
                    ladder.move(-80, 0);
                    ladder.finish(ladderDepth + "&" + texture);
                    ladder.climbable();
                    ladderDepth += .01f;
                }
            }
            TerrainTrail foundation = new TerrainTrail(map, pt1);
            foundation.add(pt11);
            foundation.add(pt12);
            foundation.add(pt2);
            foundation.add(new P(pt2.x, maxY));
            foundation.add(new P(pt1.x, maxY));
            foundation.finish(id+"&"+texture);
            lastY = point.y()-yRise;
        }
    }
    private int generateBuildings(String[] p, Line land, int bid) {
        int estate = land.ptd.x() - land.pt.x();
        ArrayList<String[]> possibleBuildings = new ArrayList<>();
        ArrayList<String[]> resultBuildings = new ArrayList<>();
        for (String i : p) {
            int size = buildings.get(i);
            if (size <= estate) possibleBuildings.add(new String[]{i, size + ""});
        }
        if (possibleBuildings.size() > 0) {
            while (true) {
                String[] building = possibleBuildings.get(Creation.r.nextInt(possibleBuildings.size()));
                if (estate < Integer.parseInt(building[1])) break;
                estate -= Integer.parseInt(building[1]);
                resultBuildings.add(building);
            }
            int gap = estate / resultBuildings.size();
            int cPosX = land.pt.x();
            for (String[] i : resultBuildings) {
                cPosX += Creation.r.nextInt(gap) + Integer.parseInt(i[1])/2;
                bid = generateBuilding(i[0], new P(cPosX, land.getCrossPointY(cPosX)), bid) + 1;
                cPosX += Integer.parseInt(i[1])/2;
            }
        }
        return bid;
    }
    public int generateBuilding(String building, P pos, int bid){
        Map blueprint = new Map("building/" + building);
        blueprint.start();

        float depth = map.checkLand(new Line(pos.x, pos.y - 4, pos.x, pos.y + 600)).depth;
        P offset = new P(-blueprint.size.x / 2, -blueprint.size.y + 24);
        blueprint.adjust(offset.getAdd(pos));
        blueprint.setTexture();
        int next = map.getNextTerrain();
        for (Line i : blueprint.landRaw) {
            i.tid += next;
            i.depth = depth - .1f + i.depth * .01f + .001f;
            map.landRaw.add(i);
            if (!i.vertical() && i.opt > 0) {
                map.land.add(i);
            }
        }
        for (java.util.Map.Entry pair : blueprint.formTextures.entrySet()) {
            String newTexture = (String) pair.getValue();
            float newDepth = Float.parseFloat(newTexture.substring(0, newTexture.indexOf("&")));
            newTexture = (depth - .1f + newDepth * .01f) + newTexture.substring(newTexture.indexOf("&"));
            map.formTextures.put(((int) pair.getKey()) + next, newTexture);
        }
        for (java.util.Map.Entry pair : blueprint.terrains.entrySet()) {
            Terrain terrain = (Terrain) pair.getValue();
            terrain.setDepth(depth - .1f + terrain.depth * .01f);
            terrain.map = this.map;
            terrain.render();
            terrain.start();
        }
        blueprint.updater.adjust(offset.getAdd(pos));
        int highestBid = bid;
        for (Observer i : blueprint.updater.addQueue) {
            if (i instanceof Prop) {
                Prop prop = (Prop) i;
                prop.manualDepth = true;
                prop.map = map;
                prop.start();
                Line closest = blueprint.checkLand(new Line(prop.pos.getAdd(0, -4), new P(prop.pos.x, prop.pos.y + 600)));
                if (closest != null) {
                    prop.lastLand = closest;
                    prop.setDepth(depth - .1f + closest.depth * .01f);
                }
                if (prop.factoryId == Stock.Spawn.ordinal()) {
                    prop.subId = 2;
                    prop.seed = bid+prop.seed-1;
                    highestBid = Math.max(highestBid, prop.seed);
                } else if (prop.factoryId == Stock.Travel.ordinal()) {
                    if (prop.subId > 0) {
                        prop.name = prop.name + (prop.subId==1?map.name:"") + "-" + bid;
                        Map house = new Map("building/In_" + building);
                        house.start();
                        house.updater.processQueue();
                        for (Prop ob : house.updater.props) {
                            if (ob.factoryId == Stock.Travel.ordinal() && ob.subId == 1) {
                                ob.name = map.name;
                                ob.seed = bid + ob.seed - 1;
                            }
                        }
                        FileManager.saveMap("B" + map.name + "-" + bid, house);
                    }
                }
            }
        }
        blueprint.updater.addQueue.clear();
        return highestBid;
    }
    private float lowestY(){
        float r = 0;
        for (Line i : map.landRaw) {
            if (i.clockwise() && i.pt.y > r && !i.vertical()) r = i.pt.y;
        }
        return r;
    }
    void generateTown(){
        int total = Creation.r.nextInt(10000)+6000;
        float ratio = Creation.r.nextFloat()*.3f+.55f;
        map.size = new P(total*ratio, total*(1-ratio));
        String[] landTexture = new String[]{"dirt&grass","dirt&road_unpaved","dirt&road_normal","dirt&road_highway"};
        String[] foundationTexture = new String[]{"whitebrick&basic", "concrete&basic", "concrete&road_unpaved", "rockwood&basic"};
        int max = Creation.r.nextInt(4)+2;

        TerrainTrail trail = new TerrainTrail(map, new P(-40, map.size.y+40));
        trail.move(0, -640);
        trail.move(map.size.x+80, 0);
        trail.move(0, 640);
        trail.finish((max+1)+"&"+landTexture[Creation.r.nextInt(landTexture.length)]);

        makeEdgeSpawns(map.size);
        generateFoundations(max, map.size, lowestY()+24, foundationTexture[Creation.r.nextInt(foundationTexture.length)]);
        map.normalize(0,1600);
        map.size.y -= 800;
        int bid=1;
        ArrayList<Line> beforeBuilding = (ArrayList<Line>) map.landRaw.clone();
        for (Line l : beforeBuilding) {
            if (l.opt > 0 && l.tid >= 0 && l.clockwise() && Math.abs(l.getSlope()) < .1f) {
                P mid = l.getMidpoint();
                if (map.checkLand(new Line(mid.x, 0, mid.x, mid.y - 1), l.depth) == null) {
                    if (Creation.r.nextInt(3) == 0) {
                        for (int ii = 0; ii < Creation.r.nextInt(9); ii++){
                            if (l.getWidth() > 200) map.placeProp(l, "Barrel=.5,Crate=1", -1); // if > ladder width 160
                        }
                        for (int ii = 0; ii < Creation.r.nextInt(9); ii++) {
//                          map.placeProp(l, "Resident=.4", -1);
                        }
                    }
                    if (l.getSlope() < .01f) {
                        bid = generateBuildings(new String[]{"Restaurant","Inn","Shop","Insurance","Armory","Hospital","Clothier","Safehouse","School"}, l, bid) + 1;
                    }
                }
            }
        }
    }
    void generateCave(String[] info){
        int returnDoorSeed = Integer.parseInt(info[1]);
        int origDirection = Creation.r.nextInt(360);
        int momentum = Creation.r.nextInt(48)-24;
        int advance = 240;
        int width = 360;
        ArrayList<P> forward = new ArrayList<>();
        ArrayList<P> backward = new ArrayList<>();
        P fw = new P(0, 0), bw, low = new P(), high = new P();
        int direction = origDirection;
        int segments = Creation.r.nextInt(10)+20;
        for (int i=0;i<segments;i++){
            width += Creation.r.nextGaussian()*3;
            momentum += Creation.r.nextGaussian()*3;
            direction += Creation.r.nextGaussian()*16 + momentum;
            direction = (int) Global.clamp(direction, origDirection-150, origDirection+150);
            float sin = Global.sin(direction)*advance;
            float cos = Global.cos(direction)*advance;
            float sinW = Global.sin(direction+90)*width;
            float cosW = Global.cos(direction+90)*width;
            fw.add(cos, sin);
            if (i == segments-1){
                fw.add(cos+cosW/2, sin+sinW/2);
                bw = fw.getAdd(0,0);
            } else bw = fw.getAdd(cos+cosW, sin+sinW);
            low.x = Math.min(low.x,fw.x);
            low.y = Math.min(low.y,fw.y);
            high.x = Math.max(high.x,fw.x);
            high.y = Math.max(high.y,fw.y);
            forward.add(fw.getAdd(0,0));
            backward.add(bw.getAdd(0,0));
        }

        map = new Map(info[0]);
        TerrainTrail trail = new TerrainTrail(map, new P(0,-advance));
        for (P i : forward) trail.add(i);
        for (int i=backward.size()-1;i>=0;i--) trail.add(backward.get(i));
        trail.finish("0&rock&none");

//        Prop door = Stock.Door.create(map, returnDoorSeed);
        Prop door = Stock.Travel.create(map, -1,returnDoorSeed); //temporary door replacement
        P doorSize = door.image.getSize();
        int exitSegment = Creation.r.nextInt(segments-6)+3;
        P exitPos = forward.get(exitSegment).getSubtract(backward.get(exitSegment)).mult(.5f,.5f)
                .add(backward.get(exitSegment));
        P doorPos = exitPos.getAdd(0, -doorSize.y/2-2);
        TerrainTrail platform = new TerrainTrail(map, exitPos.getAdd(-doorSize.x/2,0));
        platform.move(doorSize.x,0);
        platform.move(0,24);
        platform.move(-doorSize.x,0);
        platform.finish("1&wood&basic");
        platform.climbable();
        door.pos.set(doorPos);
//        Stock.Exit.create(map, returnDoorSeed).pos.set(doorPos.x,doorPos.y);
        Stock.Spawn.create(map, -1,returnDoorSeed).start().pos.set(doorPos.x,doorPos.y);
        map.normalize(800,800);
        generateBlocker(2,trail.terrain);
    }
    private void makeEdgeSpawns(P mapSize){
        Line leftSpawn = map.checkLand(new Line(mapSize.x-24,0,mapSize.x-24,mapSize.y));
        Stock.Spawn.create(map, -1, 1).start().pos.set(mapSize.x-24,leftSpawn.getCrossPointY(leftSpawn.ptd.x-24)-Math.abs(leftSpawn.getSlope())-Settings.gravity*2-8);
        Line rightSpawn = map.checkLand(new Line(24,0,24,mapSize.y));
        Stock.Spawn.create(map, -1, 2).start().pos.set(24,rightSpawn.getCrossPointY(24)-Math.abs(rightSpawn.getSlope())-Settings.gravity*2-8);
    }
    void generateForest(){
        map.size = new P(Creation.r.nextInt(4000)+4000, 4000);
        TerrainTrail ground = new TerrainTrail(map, new P(-40, map.size.y()+40));
        ground.move(0, -640);
        ground.move(map.size.x()+80, 0);
        ground.move(0, 640);
        ground.climbable();
        ground.finish("4&dirt&grass&Tree=1");
        makeEdgeSpawns(map.size);
    }
    void generateLand(){
        map.size = new P(Creation.r.nextInt(4000)+2000, Creation.r.nextInt(4000)+2000);
        int depth = 4;
        P start = new P(-64, Creation.r.nextInt(map.size.y()-128)+64);
        P end = new P(map.size.x+64, Creation.r.nextInt(map.size.y()-128)+64);
        TerrainTrail trail = new TerrainTrail(map, start);
        int preX = Creation.r.nextInt((int) (map.size.x/8))+300;
        int postX = Creation.r.nextInt((int) (map.size.x/8))+300;
        trail.move(preX,0);
        Line first = new Line(trail.last, end.getAdd(-postX,0));
        ArrayList<Line> total = new ArrayList<>();
        total.add(first);
        divideLand(total, 0, 8);
        float highestY = 0;
        for (Line i : total){
            trail.add(i);
            if (i.pt.y > highestY) highestY = i.pt.y;
            if (i.ptd.y > highestY) highestY = i.ptd.y;
        }
        trail.move(postX,0);
        trail.add(new P(trail.last.x, highestY+400));
        trail.add(new P(start.x, highestY+400));
        trail.setOpt(1);
        trail.finish(depth+"&dirt&grass"+(Creation.r.nextBoolean()?"&Rock=.1,Bush=.2,Weed=.4,Tree=1":""));
        makeEdgeSpawns(map.size);
        map.normalize(0,800);
        map.size.y -= 400;
    }
    void divideLand(ArrayList<Line> total, int current, int max){
        if (current >= max) return;
        Line last = total.remove(total.size()-1);
        P mid = new P(Creation.r.nextFloat()*(last.ptd.x-last.pt.x)+last.pt.x,
                Creation.r.nextFloat()*(last.ptd.y-last.pt.y)+last.pt.y);
        Line one = new Line(last.pt, mid);
        Line two = new Line(mid, last.ptd);
        float shortest = last.getLength()/(8*(current+1));
        float atan = last.getAtan()+1.57f;
        float rand = Global.clamp((float) Creation.r.nextGaussian()/2,-1,1);
        mid.add(Global.cos(atan)*shortest*rand, Global.sin(atan)*shortest*rand);
        total.add(one);
        divideLand(total, ++current, max);
        total.add(two);
        divideLand(total, ++current, max);
    }
    void generateBlocker(int depth, int p){
        float highX = 0;
        Line high = null;
        for (final Line i : map.landRaw) {
            if (i.tid == p) {
                if (i.pt.x > highX) {
                    highX = i.pt.x;
                    high = i;
                }
            }
        }
        TerrainTrail blocker = null;
        for (final Line i : map.landRaw) {
            if (i.tid == high.tid){
                if (blocker == null) blocker = new TerrainTrail(map, new P(i.pt));
                else blocker.add(i.pt.x, i.pt.y);
                if (i == high){
                    blocker.add(map.size.x+128, -128);
                    blocker.add(-128, -128);
                    blocker.add(-128, map.size.y+128);
                    blocker.add(map.size.x+128, map.size.y+128);
                    blocker.add(map.size.x+128, -128);
                    blocker.add(i.pt.x, i.pt.y);
                }
            }
        }
        blocker.finish(depth+"&_0,0,0,255&none");
        blocker.reverse();
        Iterator it = map.landRaw.iterator();
        Line i;
        while (it.hasNext()) {
            i = (Line) it.next();
            if (i.tid == p) it.remove();
        }
        map.landRaw.add(new Line(-64,map.size.y+64, -64,-64, p, 0, depth));
        map.landRaw.add(new Line(-64,-64, map.size.x+64,-64, p, 0, depth));
        map.landRaw.add(new Line(map.size.x+64,-64, map.size.x+64,map.size.y+64, p, 0, depth));
        map.landRaw.add(new Line(map.size.x+64,map.size.y+64, -64,map.size.y+64, p, 0, depth));
    }
}
