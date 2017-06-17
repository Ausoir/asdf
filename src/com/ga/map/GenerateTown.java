package com.ga.map;

import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Prop;
import com.ga.unit.Stock;

import java.util.ArrayList;

/**
 * Created by ga on 5/28/17. asd
 */
class GenerateTown extends Generator {
    private void generateFoundation(int id, P mapSize, P point, float lastY, String[] texture){
        int width = (int) Global.clamp((float) Math.abs(mapSize.x*(Creation.r.nextGaussian()*.4f)),100,mapSize.x*(7f/8));
        P pt1 = new P(Math.max(600, point.x-width),point.y);
        P pt2 = new P(Math.min(mapSize.x-600,point.x+width),point.y);
        if (pt1.x == 600) pt1.x += Creation.r.nextInt(300);
        if (pt2.x == mapSize.x-600) pt2.x -= Creation.r.nextInt(300);
        int finalWidth = (int) (pt2.x-pt1.x);
        if (finalWidth <= 0) return;
        P pt3 = new P(pt2.x,mapSize.y-500);
        P pt4 = new P(pt1.x,mapSize.y-500);
        int midWidth = (int) ((lastY-point.y)*(Creation.r.nextFloat()*.3f+.5f))*2;
        if (finalWidth-midWidth*2 > 100 && Creation.r.nextInt(4) != 0){
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
            Global.map.land.add(new Line(pt1, pt11, id, 0));
            Global.map.land.add(new Line(pt11, pt12, id, 0));
            Global.map.land.add(new Line(pt12, pt2, id, 0));
        } else Global.map.land.add(new Line(pt1, pt2, id, 0));
        Global.map.land.add(new Line(pt2, pt3, id, -1));
        Global.map.land.add(new Line(pt3, pt4, id, 0));
        Global.map.land.add(new Line(pt4, pt1, id, -1));
        Global.map.formTextures.put(id, id+"&"+texture[Creation.r.nextInt(texture.length)]);
    }
    Generator create(P mapSize){
        Global.map.event = MapEvent.Urban;
        String[] landTexture = new String[][]{
                new String[]{"dirt&grass","dirt&road"},
                new String[]{"rock&road","rock&grass"}}[Creation.r.nextInt(2)];
        String[] foundationTexture = new String[][]{
                new String[]{landTexture[0], landTexture[1], "brick&basic", "marble&basic"},
                new String[]{landTexture[0], landTexture[1], "concrete&basic", "rock&basic"},
                new String[]{landTexture[0], landTexture[1], "steel&basic"}
        }[Creation.r.nextInt(3)];
        float lastY = mapSize.y-700-Creation.r.nextInt(160);
        int max = Creation.r.nextInt(6)+4;
        for (int i=0;i<max;i++){
            P pos = new P(Creation.r.nextInt(4000)+1000,lastY-(Creation.r.nextInt(360)+80));
            generateFoundation(max-i, mapSize, pos, lastY, foundationTexture);
            lastY = pos.y;
        }
        generateFloor(max+1, mapSize, Creation.r.nextInt(4) + 7, 20, new String[]{landTexture[Creation.r.nextInt(landTexture.length)]});
        for (int i=0;i<Global.map.land.size();i++){
            Line l = Global.map.land.get(i);
            if (l.next >= 0 && l.isLandable() && Math.abs(l.getSlope()) < .1f) {
                if (Math.abs(l.getSlope()) < .01f) Global.map.placeProp(l, "SteamHouse=.1,TowerHouse=1", -1);
                else Global.map.placeProp(l, "SteamHouse=1", -1);
                for (int ii=0;ii<Creation.r.nextInt(9);ii++) Global.map.placeProp(l,"Stack=.2,Barrel=.5,Crate=1", -1);
                for (int ii=0;ii<Creation.r.nextInt(9);ii++){
                    Global.map.placeProp(l,"Man=.4", Stock.getMan("Resident"));
                }
            }
        }
        return this;
    }
    Generator generateCity(P mapSize){
        int dividend = 14;
        TerrainTrail baseline = new TerrainTrail(new P(0,mapSize.y-600));
        int fragment = (int) (mapSize.x/dividend);
        for (int i=0;i<dividend;i++){
            Line land = baseline.move(fragment, 0);
            if (i >= 3 && i < dividend-3) generateSkyscraper(land);
        }
        baseline.add(new Line(mapSize.x,mapSize.y-600,mapSize.x,mapSize.y,0,0));
        baseline.add(new Line(mapSize.x,mapSize.y,0,mapSize.y,0,0));
        baseline.add(new Line(0,mapSize.y,0,mapSize.y-600,0,0));
        baseline.finish(this, "steel&road");
        ArrayList<TerrainTrail> alltrails = new ArrayList<>();
        for (Line l : Global.map.land){
            if (l.next == -1 && Creation.r.nextInt(3)!=0){
                Line next;
                boolean right = Creation.r.nextBoolean();
                if (right) next = new Line(l.ptd.x+fragment-8,l.ptd.y-500,l.ptd.x+fragment-8,l.ptd.y+500);
                else next = new Line(l.pt.x-fragment+8,l.pt.y-500,l.pt.x-fragment+8,l.pt.y+500);
                for (Line l2 : Global.map.land) {
                    if (l2.lineCross(next)){
                        TerrainTrail trail;
                        int margin = 16;
                        if (l2.next != -1) margin += Creation.r.nextInt((int) (l.ptd.x-l.pt.x-32));
                        if (right) {
                            trail = new TerrainTrail(l.ptd.getAdd(-20, 0));
                            trail.add(l2.pt.getAdd(margin-4, 0));
                            trail.add(l2.pt.getAdd(margin+4, 2));
                            trail.add(l.ptd.getAdd(-12, 2));
                        } else {
                            trail = new TerrainTrail(l2.ptd.getAdd(-20, 0));
                            trail.add(l.pt.getAdd(margin-4, 0));
                            trail.add(l.pt.getAdd(margin+4, 2));
                            trail.add(l2.ptd.getAdd(-12, 2));
                        }
                        alltrails.add(trail);
                        break;
                    }
                }
            }
        }
        for (TerrainTrail trail : alltrails){
            trail.finish(this, "stair&none");
            trail.climbable();
        }
        return this;
    }
    private void generateSkyscraper(Line land){
        if (land.getSlope() == 0) {
            Line l = land;
            Prop last = Global.map.placeProp(l, "TowerHouse=1", -1);
            for (int i = 0; i < Creation.r.nextInt(7) + 3; i++) {
                Prop t = Stock.TowerHouse.create(-1);
                t.pos.set(last.pos.x, last.pos.y-last.image.getSize().y+4);
                last = t;
            }
            Global.map.placeProp(l, "TowerTip=1", -1);
        }
    }
}
