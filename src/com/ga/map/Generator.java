package com.ga.map;

import com.ga.game.FileManager;
import com.ga.game.Observer;
import com.ga.game.Settings;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Prop;
import com.ga.unit.Stock;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by ga on 5/28/17. asd
 */
public class Generator {
    private Map map;
    public Generator(Map map, int next){
        this.map = map;
        if (next != -1) TerrainTrail.id = next;
    }
    private void generateFoundation(int id, P mapSize, P point, float lastY, String[] texture){
        int width = (int) Global.clamp((float) Math.abs(mapSize.x*(Creation.r.nextGaussian()*.4f)),100,mapSize.x*(7f/8));
        P pt1 = new P(Math.max(600+Creation.r.nextInt(300), point.x-width),point.y);
        P pt2 = new P(Math.min(mapSize.x-600-Creation.r.nextInt(300), point.x+width),point.y);
        int finalWidth = (int) (pt2.x-pt1.x);
        if (finalWidth <= 0) return;
        P pt3 = new P(pt2.x,mapSize.y-500);
        P pt4 = new P(pt1.x,mapSize.y-500);
        int midWidth = (int) ((lastY-point.y)*(Creation.r.nextFloat()*.3f+.5f))*2;
        if (finalWidth-midWidth*2 > 150 && Creation.r.nextInt(5) != 0){
            int mid = Creation.r.nextInt(finalWidth-midWidth*2)+midWidth;
            P pt11 = new P(pt1.x+(mid-midWidth),point.y);
            P pt12 = new P(pt1.x+(mid+midWidth),point.y);
            float drop = lastY-point.y;
            if (Creation.r.nextBoolean()){
                pt11.y += drop;
                pt1.y += drop;
            } else {
                pt12.y += drop;
                pt2.y += drop;
            }
            map.landRaw.add(new Line(pt1, pt11, id, 1, id));
            map.landRaw.add(new Line(pt11, pt12, id, 1, id));
            map.landRaw.add(new Line(pt12, pt2, id, 1, id));
        } else map.landRaw.add(new Line(pt1, pt2, id, 1, id));
        map.landRaw.add(new Line(pt2, pt3, id, 0, id));
        map.landRaw.add(new Line(pt3, pt4, id, 0, id));
        map.landRaw.add(new Line(pt4, pt1, id, 0, id));
        map.formTextures.put(id, id+"&"+texture[Creation.r.nextInt(texture.length)]);
    }
    public void generateBuilding(String p, P pos, int bid){
        Map blueprint = new Map("building/"+p);
        blueprint.start();

        float depth = map.checkLand(new Line(pos.x, pos.y-4, pos.x, pos.y+600)).depth;
        P offset = new P(-blueprint.size.x/2, -blueprint.size.y+24);
        blueprint.adjust(offset.getAdd(pos));
        blueprint.setTexture();
        int next = map.getNextTerrain();
        for (Line i : blueprint.landRaw){
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
            map.formTextures.put(((int) pair.getKey())+next, newTexture);
        }
        for (java.util.Map.Entry pair : blueprint.terrains.entrySet()) {
            Terrain terrain = (Terrain) pair.getValue();
            terrain.setDepth(depth - .1f + terrain.depth * .01f);
            terrain.map = this.map;
            terrain.render();
            terrain.start();
        }
        blueprint.updater.adjust(offset.getAdd(pos));
        for (Observer i : blueprint.updater.addQueue){
            if (i instanceof Prop){
                Prop prop = (Prop) i;
                prop.manualDepth = true;
                prop.map = map;
                prop.start();
                Line closest = blueprint.checkLand(new Line(prop.pos.getAdd(0,-4), new P(prop.pos.x, prop.pos.y+600)));
                if (closest != null) {
                    prop.lastLand = closest;
                    prop.setDepth(depth - .1f + closest.depth * .01f);
                }
                if (((Prop) i).factoryId == Stock.Spawn.ordinal()){
                    ((Prop) i).subId = 2;
                    ((Prop) i).seed = bid;
                } else if (((Prop) i).factoryId == Stock.Travel.ordinal()){
                    ((Prop) i).name = "B"+map.name+"-"+bid;
                    ((Prop) i).seed = 1;
                    Map house = new Map("building/In_"+p);
                    house.start();
                    house.updater.processQueue();
                    for (Prop ob : house.updater.props){
                        if (ob.factoryId == Stock.Travel.ordinal()){
                            ob.name = map.name;
                            ob.seed = bid;
                        }
                    }
                    FileManager.saveMap("B"+map.name+"-"+bid, house);
                }
            }
        }
        blueprint.updater.addQueue.clear();
    }
    void generateTown(){
        map.size = new P(Creation.r.nextInt(10000)+5000, Creation.r.nextInt(4500)+1500);
        String[] landTexture = new String[][]{
                new String[]{"dirt&grass","dirt&road_unpaved","dirt&road_normal","dirt&road_highway"},
                new String[]{"dirt&grass","dirt&road_unpaved","dirt&road_normal","dirt&road_highway"}}[Creation.r.nextInt(2)];
        String[] foundationTexture = new String[][]{
                new String[]{landTexture[0], landTexture[1], "brick&basic"},
                new String[]{landTexture[0], landTexture[1], "concrete&basic"},
                new String[]{landTexture[0], landTexture[1], "steel&road_unpaved", "concrete&road_unpaved"}
        }[Creation.r.nextInt(3)];
        float lastY = map.size.y-700-Creation.r.nextInt(160);
        int max = Creation.r.nextInt(4)+1;
        for (int i=0;i<max;i++){
            P pos = new P(Creation.r.nextInt((int) (map.size.x*(4f/5)))+(map.size.x*(1f/5)),lastY-(Creation.r.nextInt(360)+80));
            generateFoundation(max-i, map.size, pos, lastY, foundationTexture);
            lastY = pos.y;
        }
        map.normalize(800,1200);
        map.adjust(new P(0,400));
        generateFloor(max+1, map.size, Creation.r.nextInt(5) + 1, 20, new String[]{landTexture[Creation.r.nextInt(landTexture.length)]});
        int bid=1;
        for (int i=0;i<map.landRaw.size();i++){
            Line l = map.landRaw.get(i);
            if (l.opt > 0 && l.tid >= 0 && l.clockwise() && Math.abs(l.getSlope()) < .1f) {
                P mid = l.getMidpoint();
                if (map.checkLand(new Line(mid.x, 0, mid.x, mid.y-1), l.depth) == null) {
                    if (Creation.r.nextInt(3) == 0) {
                        for (int ii = 0; ii < Creation.r.nextInt(9); ii++) map.placeProp(l, "Barrel=.5,Crate=1", -1);
                        for (int ii = 0; ii < Creation.r.nextInt(9); ii++) {
//                          map.placeProp(l, "Resident=.4", -1);
                        }
                    }
                    if (l.getSlope() == 0 && l.getWidth() > 1600) {
                        generateBuilding("Restaurant", mid, bid);
                        bid++;
                        System.out.println("Generated");
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
        map.normalize(800,400);
        generateBlocker(2,trail.terrain);
    }
    private void makeEdgeSpawns(P mapSize){
        Line leftSpawn = map.checkLand(new Line(mapSize.x-24,0,mapSize.x-24,mapSize.y));
        Stock.Spawn.create(map, -1, 0).start().pos.set(mapSize.x-24,leftSpawn.getCrossPointY(leftSpawn.ptd.x-24)-Math.abs(leftSpawn.getSlope())-Settings.gravity*2-1);
        Line rightSpawn = map.checkLand(new Line(24,0,24,mapSize.y));
        Stock.Spawn.create(map, -1, 1).start().pos.set(24,rightSpawn.getCrossPointY(24)-Math.abs(rightSpawn.getSlope())-Settings.gravity*2-1);
    }
    void generateLand(int depth, P mapSize){
        P start = new P(-64, Creation.r.nextInt(mapSize.y()-128)+64);
        P end = new P(mapSize.x+64, Creation.r.nextInt(mapSize.y()-128)+64);
        TerrainTrail trail = new TerrainTrail(map, start);
        int preX = Creation.r.nextInt((int) (mapSize.x/8))+300;
        int postX = Creation.r.nextInt((int) (mapSize.x/8))+300;
        trail.move(preX,0);
        Line first = new Line(trail.last, end.getAdd(-postX,0));
        ArrayList<Line> total = new ArrayList<>();
        total.add(first);
        divideLand(total, 0, 7);
        float highestY = 0;
        for (Line i : total){
            trail.add(i);
            if (i.pt.y > highestY) highestY = i.pt.y;
            if (i.ptd.y > highestY) highestY = i.ptd.y;
        }
        trail.move(postX,0);
        trail.add(new P(trail.last.x, Math.max(highestY+64, mapSize.y+64)));
        trail.add(new P(start.x, Math.max(highestY+64, mapSize.y+64)));
        trail.finish(depth+"&dirt&grass"+(Creation.r.nextBoolean()?"&Rock=.1,Bush=.2,Weed=.4,Tree=1":""));
        makeEdgeSpawns(mapSize);
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
        float rand = Global.rng(1);
        mid.add(Global.cos(atan)*shortest*rand, Global.sin(atan)*shortest*rand);
        total.add(one);
        divideLand(total, ++current, max);
        total.add(two);
        divideLand(total, ++current, max);
    }
    void generateFloor(int depth, P mapSize, int randomness, int varianceY, String[] texture){
        float horizon = mapSize.y-600;
        P orig = new P(0, horizon);
        TerrainTrail trail = new TerrainTrail(map, orig);
        while (trail.last.x < mapSize.x) {
            int randX = (Creation.r.nextInt(240) + 20) * (Creation.r.nextInt(randomness)+1);
            int randY = (Creation.r.nextInt(varianceY) - (varianceY/2)) * Creation.r.nextInt(randomness);
            trail.add(trail.last.x+randX, horizon+randY);
            if (trail.last.x+(200*randomness*.5f) > mapSize.x) trail.last.x = mapSize.x;
        }
        trail.last.y = horizon;
        trail.add(trail.last.x, mapSize.y);
        trail.add(0, mapSize.y);
        trail.finish(depth+"&"+texture[Creation.r.nextInt(texture.length)]);
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
        map.landRaw.add(new Line(-64,map.size.y+64, -64,-64, p, 1, depth));
        map.landRaw.add(new Line(-64,-64, map.size.x+64,-64, p, 1, depth));
        map.landRaw.add(new Line(map.size.x+64,-64, map.size.x+64,map.size.y+64, p, 1, depth));
        map.landRaw.add(new Line(map.size.x+64,map.size.y+64, -64,map.size.y+64, p, 1, depth));
    }
}
