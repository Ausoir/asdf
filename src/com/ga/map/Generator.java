package com.ga.map;

import com.ga.game.FileManager;
import com.ga.game.Observer;
import com.ga.game.Settings;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.T;
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
            if (i[1].startsWith(FileManager.makeDirectory("Data/map/building")) && !i[1].startsWith(FileManager.makeDirectory("Data/map/building/inside"))) {
                String meta = FileManager.read(FileManager.makeDirectory(i[1])).split("%")[0];
                buildings.put(i[0], (int) new T(meta).get(2).get(0).getFloat());
            }
        }
    }
    void generateBuilding(String building, int buildingSize){
        ArrayList<Line> validLands = new ArrayList<>();
        for (Line l : map.landRaw) {
            if (l.opt > 0 && l.tid >= 0 && l.clockwise() && Math.abs(l.getSlope()) < .1f && l.ptd.x()-l.pt.x() >= buildingSize) {
                validLands.add(l);
            }
        }
        if (validLands.size() > 0) {
            generateBuilding(building, validLands.get(Creation.r.nextInt(validLands.size())).getMidpoint(), 10);
        }
    }
    int generateBuildings(T p, Line land, int bid) {
        int estate = land.ptd.x() - land.pt.x();
        ArrayList<String[]> possibleBuildings = new ArrayList<>(); //Get all buildings that fit the estate
        for (T i : p) {
            int size = buildings.get(i.get());
            if (size <= estate) possibleBuildings.add(new String[]{i.get(), size + ""});
        }
        ArrayList<String[]> resultBuildings = new ArrayList<>(); //Build buildings according to estate
        if (possibleBuildings.size() > 0) {
            while (possibleBuildings.size() > 0) {
                String[] building = possibleBuildings.remove(Creation.r.nextInt(possibleBuildings.size()));
                if (estate < Integer.parseInt(building[1])) break;
                estate -= Integer.parseInt(building[1]);
                resultBuildings.add(building);
            }
            int gap = estate / resultBuildings.size();
            int cPosX = land.pt.x();
            for (String[] i : resultBuildings) {
                int halfSize = Integer.parseInt(i[1])/2;
                cPosX += halfSize + Creation.r.nextInt(gap);
                bid = generateBuilding(i[0], new P(cPosX, land.getCrossPointY(cPosX)), bid) + 1;
                cPosX += halfSize;
            }
        }
        return bid;
    }
    public int generateBuilding(String building, P pos, int bid){
        Global.p("Generating building ["+building+"]");
        Map blueprint = new Map("building/" + building);
        blueprint.start();

        float depth = map.checkLand(new Line(pos.x, pos.y - 4, pos.x, pos.y + 600), Line::landable).depth;
        float startDepth = depth - .1f;
        float depthScale = .01f;
        P offset = new P(-blueprint.size.x / 2, -blueprint.size.y + 24);
        blueprint.adjust(offset.getAdd(pos));
        blueprint.setTexture();
        int next = map.getNextTerrain();
        for (Line i : blueprint.landRaw) {
            i.tid += next;
            i.depth = startDepth + i.depth * depthScale + (depthScale*.1f);
            map.landRaw.add(i);
            if (!i.vertical() && i.opt > 0) {
                map.land.add(i);
            }
        }
        for (java.util.Map.Entry pair : blueprint.formTextures.entrySet()) {
            String newTexture = (String) pair.getValue();
            float newDepth = Float.parseFloat(newTexture.substring(0, newTexture.indexOf(",")));
            newTexture = (startDepth + newDepth * depthScale) + newTexture.substring(newTexture.indexOf(","));
            map.formTextures.put(((int) pair.getKey()) + next, newTexture);
        }
        for (java.util.Map.Entry pair : blueprint.terrains.entrySet()) {
            Terrain terrain = (Terrain) pair.getValue();
            terrain.setDepth(startDepth + terrain.depth * depthScale);
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
                Line closest = blueprint.checkLand(new Line(prop.pos.getAdd(0, -4), new P(prop.pos.x, prop.pos.y + 600)), Line::landable);
                if (closest != null) {
                    prop.lastLand = closest;
                    prop.setDepth(startDepth + closest.depth * depthScale);
                }
                if (prop.factoryId == Stock.Spawn.ordinal()) {
                    prop.subId = 2;
                    prop.seed = bid+prop.seed-1;
                    highestBid = Math.max(highestBid, prop.seed);
                } else if (prop.factoryId == Stock.Travel.ordinal()) {
                    if (prop.subId == 1) { //overmap
                        Global.p("Unimplemented travel ID 1 found."); //NOT IMPLEMENTED
                    } else if (prop.subId == 2) { //outside to inside
                        String newmap = map.name.changeMapName("B"+map.name.getMapname()+"_"+bid);
                        generateBuildingInsides(prop, 0, bid,
                                newmap+"_0", //Something like B_1_14
                                FileManager.makeDirectory("building/inside/") + building+"_0");
                    }
                } else {
                    if (prop.seed < -2 && Creation.r.nextBoolean()) prop.remove();
                    else if (prop.seed == -2 || prop.seed == -3) prop.seed = -1;
                }
            }
        }
        blueprint.updater.addQueue.clear();
        return highestBid;
    }
    private void generateBuildingInsides(Prop travel, int index, int returnSeedOffset, String newMapName, String mapToLoad){
        travel.name = newMapName;
        Map house = new Map(mapToLoad);
        house.start();
        house.updater.processQueue();
        for (Prop ob : house.updater.props) {
            if (ob.factoryId == Stock.Travel.ordinal()) {
                String newMapNameWithoutIndex = newMapName.substring(0,newMapName.lastIndexOf("_"));
                if (ob.subId == 2) { //inside to inside
                    String newLoadMapWithoutIndex = mapToLoad.substring(0,mapToLoad.lastIndexOf("_"));
                    generateBuildingInsides(ob, ++index, returnSeedOffset,
                            newMapNameWithoutIndex+"_"+index,
                            newLoadMapWithoutIndex+"_"+index); //Something like B_1_14_1
                } else if (ob.subId == 3) { //inside to outside
                    ob.name = map.name.toString();
                    ob.seed = returnSeedOffset + ob.seed - 1;
                } else if (ob.subId == 4) { //inside to previous inside
                    ob.name = newMapNameWithoutIndex+"_"+(index-1);
                }
            } else if (ob.factoryId != Stock.Spawn.ordinal()){
                if (travel.seed < -2 && Creation.r.nextBoolean()) ob.remove();
                else if (ob.seed == -2 || ob.seed == -3) ob.seed = -1;
            }
        }
        FileManager.saveMap(newMapName, house);
    }
    void makeEdgeSpawns(){
        Line leftSpawn = map.checkLand(new Line(map.size.x-24,0,map.size.x-24,map.size.y),Line::landable);
        Stock.Spawn.create(map, -1, 1).start().pos.set(map.size.x-24,leftSpawn.getCrossPointY(leftSpawn.ptd.x-24)-Math.abs(leftSpawn.getSlope())-Settings.gravity*2-8);
        Line rightSpawn = map.checkLand(new Line(24,0,24,map.size.y),Line::landable);
        Stock.Spawn.create(map, -1, 2).start().pos.set(24,rightSpawn.getCrossPointY(24)-Math.abs(rightSpawn.getSlope())-Settings.gravity*2-8);
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
        assert high != null;
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
        assert blocker != null;
        blocker.finish(depth+",/0/0/0/255,none");
        blocker.reverse();
        Iterator it = map.landRaw.iterator();
        Line i;
        while (it.hasNext()) {
            i = (Line) it.next();
            if (i.tid == p) i.opt = 0;
        }
    }
}
