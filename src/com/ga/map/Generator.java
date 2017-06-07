package com.ga.map;

import com.ga.tool.Line;
import com.ga.tool.P;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ga on 5/28/17. asd
 */
public class Generator {
    ArrayList<Integer> depths = new ArrayList<>();
    HashMap<Integer, ArrayList<Integer>> depthBinding = new HashMap<>();
    public static void generate(String[] info){
        Generator result;
        if (info[0].startsWith("I_")) {
            result = new GenerateHouse().create(info);
        } else if (info[0].startsWith("D_")) {
            result = new GenerateDungeon().create(info);
        } else {
            P mapSize = new P(6000+Creation.r.nextInt(6000), 3200+Creation.r.nextInt(1600));
            String id = info[0].split("_")[1];
            Global.map = new Map(info[0], new String[]{id, "None", Creation.panoramaAt(Integer.parseInt(id)),
                    ((int) mapSize.x) + "x" + ((int) mapSize.y)});
            if (Creation.r.nextInt(5) == 0) {
                result = new GenerateTown().create(mapSize);
            } else {
                if (Creation.r.nextBoolean()) result = new GenerateHighlands().createPlains(mapSize);
                else {
                    result = new GenerateHighlands().create('-', mapSize);
                    if (Creation.r.nextBoolean()) ((GenerateHighlands) result).create('-', mapSize);
                }
                Global.map.placeProp(Global.map.randomLandableSurface(), "Dungeon=1");
            }
        }
        result.finish();
    }
    private void finish(){
        for (java.util.Map.Entry i : Global.map.formTextures.entrySet()){
            for (int ii=0;ii<depths.size();ii++){
                int key = (int) i.getKey();
                if (depths.get(ii) == key) {
                    Global.map.formTextures.put(key, ii+"&"+Global.map.formTextures.get(key));
                    if (depthBinding.get(key) != null) {
                        for (int iii : depthBinding.get(key))
                            Global.map.formTextures.put(iii, ii + "&" + Global.map.formTextures.get(iii));
                    }
                    break;
                }
            }
        }
    }
    void generateFloor(int depth, P mapSize, int randomness, int varianceY, String[] texture){
        float horizon = mapSize.y-600;
        P orig = new P(0, horizon), pt1 = orig, pt2 = new P();
        while (pt2.x < mapSize.x) {
            int randX = (Creation.r.nextInt(240) + 20) * (Creation.r.nextInt(randomness)+1);
            int randY = (Creation.r.nextInt(varianceY) - (varianceY/2)) * Creation.r.nextInt(randomness);
            pt2 = new P(pt1.x + randX, orig.y + randY);
            if (pt2.x+(200*randomness*.5f) > mapSize.x) pt2.x = mapSize.x;
            Global.map.land.add(new Line(pt1, pt2, 0, 0));
            pt1 = pt2;
        }
        pt1.y = horizon;
        Line t;
        Global.map.land.add(t = new Line(pt1, new P(pt2.x, mapSize.y), 0, 0));
        Global.map.land.add(t = new Line(t.ptd, new P(0, mapSize.y), 0, 0));
        Global.map.land.add(new Line(t.ptd, orig, 0, 0));
        Global.map.formTextures.put(0, depth+"&"+texture[Creation.r.nextInt(texture.length)]);
    }
    private void createFloater(P spot, int rad){
        TerrainTrail trail = new TerrainTrail(new P(spot.x+rad, spot.y));
        int num = Creation.r.nextInt(7)+4;
        float gap = 3.14f/num;
        for (int i=1;i<num;i++){
            float currentArc = gap*i+Creation.r.nextFloat()*gap/6-gap/3;
            int radVariance = Creation.r.nextInt(rad/2) + rad * 3 / 4;
            trail.add(spot.x + Global.cos(currentArc) * radVariance, spot.y + Global.sin(currentArc) * radVariance * .3f);
        }
        trail.add(new P(spot.x-rad, spot.y));
        trail.finishNoDepth("dirt&grass");
        trail.climbable();

        Line closest = Global.map.checkLand(new Line(spot.getAdd(0,1), new P(spot.x, spot.y+9999)));
        if (closest != null) {
            float closestY = closest.getCrossPointY(spot.x);
            depths.add(depths.indexOf(closest.next), trail.terrain);
            if (Math.abs(closestY-spot.y) < 800){
                if (closestY-spot.y < 0)
                    Global.map.land.add(new Line(spot.x, closestY-64, spot.x, spot.y, -1, 1));
                else
                    Global.map.land.add(new Line(spot.x, spot.y-64, spot.x, closestY, -1, 1));
            }
        } else depths.add(0, trail.terrain);
    }
    private Generator createFloaters(P mapSize, int num){
        int lastY = (int) (mapSize.y - Creation.r.nextInt((int) (mapSize.y/(num+1))));
        for (int i=0;i<num;i++) {
            P spot = new P(Creation.r.nextInt((int) mapSize.x), lastY);
            createFloater(spot, Creation.r.nextInt(1280) + 240);
            lastY -= Creation.r.nextInt((int) (mapSize.y/(num+1)));
        }
        return this;
    }
}
