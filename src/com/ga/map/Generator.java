package com.ga.map;

import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Prop;
import com.ga.unit.Stock;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by ga on 5/28/17. asd
 */
class Generator {
    static void init(int next){
        TerrainTrail.id = next;
    }
    private static void generateFoundation(int id, P mapSize, P point, float lastY, String[] texture){
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
    static void generateTown(P mapSize){
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
            if (l.next >= 0 && l.clockwise() && Math.abs(l.getSlope()) < .1f) {
                if (Math.abs(l.getSlope()) < .01f) Global.map.placeProp(l, "SteamHouse=.1,TowerHouse=1", -1);
                else Global.map.placeProp(l, "SteamHouse=1", -1);
                for (int ii=0;ii<Creation.r.nextInt(9);ii++) Global.map.placeProp(l,"Stack=.2,Barrel=.5,Crate=1", -1);
                for (int ii=0;ii<Creation.r.nextInt(9);ii++){
                    Global.map.placeProp(l, "Resident=.4", -1);
                }
            }
        }
    }
    static void generateCity(P mapSize){
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
        baseline.finish("0&steel&road");
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
            trail.finish("1&stair&none");
            trail.climbable();
        }
    }
    private static void generateSkyscraper(Line land){
        if (land.getSlope() == 0) {
            Line l = land;
            Prop last = Global.map.placeProp(l, "TowerHouse=1", -1);
            for (int i = 0; i < Creation.r.nextInt(7) + 3; i++) {
                Prop t = Stock.TowerHouse.create(-1,-1);
                t.pos.set(last.pos.x, last.pos.y-last.image.getSize().y+4);
                last = t;
            }
            Global.map.placeProp(l, "TowerTip=1", -1);
        }
    }
    static void generateCave(String[] info){
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

        Global.map = new Map(info[0], new P(40,40));
        TerrainTrail trail = new TerrainTrail(new P(0,-advance));
        for (P i : forward) trail.add(i);
        for (int i=backward.size()-1;i>=0;i--) trail.add(backward.get(i));
        trail.finish("0&rock&none");

//        Prop door = Stock.Door.create(returnDoorSeed);
        Prop door = Stock.Travel.create(-1,returnDoorSeed); //temporary door replacement
        P doorSize = door.image.getSize();
        int exitSegment = Creation.r.nextInt(segments-6)+3;
        P exitPos = forward.get(exitSegment).getSubtract(backward.get(exitSegment)).mult(.5f,.5f)
                .add(backward.get(exitSegment));
        P doorPos = exitPos.getAdd(0, -doorSize.y/2-2);
        TerrainTrail platform = new TerrainTrail(exitPos.getAdd(-doorSize.x/2,0));
        platform.move(doorSize.x,0);
        platform.move(0,24);
        platform.move(-doorSize.x,0);
        platform.finish("1&wood&basic");
        platform.climbable();
        door.pos.set(doorPos);
//        Stock.Exit.create(returnDoorSeed).pos.set(doorPos.x,doorPos.y);
        Stock.Spawn.create(-1,returnDoorSeed).pos.set(doorPos.x,doorPos.y);
        normalize(800,400);
        generateBlocker(2,trail.terrain);
    }
    static void generateLand(int depth, P mapSize){
        P start = new P(-64, Creation.r.nextInt(mapSize.y()-128)+64);
        P end = new P(mapSize.x+64, Creation.r.nextInt(mapSize.y()-128)+64);
        TerrainTrail trail = new TerrainTrail(start);
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
    }
    static void divideLand(ArrayList<Line> total, int current, int max){
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
    static void generateHouse(String[] info){
        int startDepth = 0;
        int returnDoorSeed = Integer.parseInt(info[1]);
        P mapSize = new P(info[2]).mult(2f);
        Global.map = new Map(info[0], mapSize);
        float bottom = mapSize.y - 64;
        P topleft = new P(64, 64), topright = new P(mapSize.x-64, 64),
                bottomleft = new P(64, bottom), bottomright = new P(mapSize.x-64, bottom);

        String[] textures = new String[]{"wood","steel","brick"};
        String texture = textures[Global.r.nextInt(textures.length)];

        TerrainTrail trail = new TerrainTrail(bottomleft);
        trail.add(topleft);
        trail.add(topright);
        trail.add(bottomright);
        trail.finish(startDepth+"&"+texture+"&none");
        int floorNum = Integer.parseInt(info[4]);
        ArrayList<Integer> dividers = new ArrayList<>();
        if (floorNum > 1) {
            float floorHeight = mapSize.y/(floorNum+1);
            int floors = Math.max(0,floorNum-1);
            float lastY = bottomleft.y;
            boolean[] sides = new boolean[floors];
            for (int i=0;i<floors;i++) {
                TerrainTrail stair = new TerrainTrail(new P(48, lastY + 4));
                stair.move(0, -floorHeight);
                stair.move(80, 0);
                stair.move(320, floorHeight);
                stair.finish((startDepth+1)+"&"+texture+"&basic");
                stair.climbable();
                lastY -= floorHeight;
                sides[i] = Global.r.nextBoolean();
                if (sides[i]) stair.switchSides(mapSize.x);
            }
            lastY = bottomleft.y;
            for (int i=0;i<floors;i++) {
                lastY -= floorHeight;
                TerrainTrail divider = new TerrainTrail(new P(48,lastY));
                divider.move(436, 0);
                divider.move(mapSize.x-420-112, 0);
                divider.lastOpt(2);
                divider.move(0, 40);
                divider.move(-mapSize.x+112+420, 0);
                divider.move(0, -20);
                divider.move(-436, 0);
                divider.finish("_0,0,0,255&none");
                divider.climbable();
                if (sides[i]) divider.switchSides(mapSize.x);
                dividers.add(divider.terrain);
            }
        }
//        Prop door = Stock.Door.create(returnDoorSeed);
        Prop door = Stock.Travel.create(-1,returnDoorSeed); //temporary door replacement
        P result = new P(mapSize.x / 2, bottom-door.image.getSize().y/2-2);
        door.pos.set(result);
//        Stock.Exit.create(returnDoorSeed).pos.set(result.x,result.y);
        Stock.Spawn.create(-1,returnDoorSeed).pos.set(result.x,result.y);
        int merchantType = Integer.parseInt(info[3]);
        if (merchantType != 0){ //1:food,2:shop,3:guild,4:training
            Stock.getByName("Merchant",merchantType-1).pos.set(Global.r.nextInt((int) mapSize.x-130)+65,result.y);
            int spacing = Global.r.nextInt(240)+120;
            for (int i=0;i<mapSize.x/spacing;i++) {
                int cx = spacing*i;
                for (Line l : Global.map.land) {
                    if (l.next >= 0 && l.clockwise() && l.getSlope() == 0 && cx-l.pt.x>spacing/2-10 && l.ptd.x-cx>spacing/2-10 && Global.r.nextInt(3)!=0) {
                        float y = l.getCrossPointY(cx);
                        if (merchantType == 1 || merchantType == 2) {
                            Stock.getByName("Shorttable", -1).pos.set(cx, y - 1);
                            Stock.getByName("Shortchair", -1).pos.set(cx - 50, y - 1);
                            Stock.getByName("Shortchair", -1).pos.set(cx + 50, y - 1);
                        } else if (merchantType == 3 || merchantType == 4){
                            Stock.getByName("Training_Dummy", -1).pos.set(cx, y - 1);
                        }
                    }
                }
            }
        }
        generateBlocker(startDepth+2,trail.terrain);
    }
    static void normalize(int marginX, int marginY){
        P high = new P(), low = new P();
        for (Line i : Global.map.land){
            low.x = Math.min(Math.min(low.x,i.pt.x),i.ptd.x);
            low.y = Math.min(Math.min(low.y,i.pt.y),i.ptd.y);
            high.x = Math.max(Math.max(high.x,i.pt.x),i.ptd.x);
            high.y = Math.max(Math.max(high.y,i.pt.y),i.ptd.y);
        }
        P margin = new P(marginX, marginY);
        int maxDiffX = (int) (high.x-low.x)+margin.x();
        int maxDiffY = (int) (high.y-low.y)+margin.y();
        Global.map.size = new P(maxDiffX, maxDiffY);
        P adjustment = new P(0,low.y*-1+margin.y);
        for (Line i : Global.map.land) {
            i.pt = i.pt.getAdd(adjustment);
            i.ptd = i.ptd.getAdd(adjustment);
        }
        Updater.normalize(adjustment);
    }
    static void generateFloor(int depth, P mapSize, int randomness, int varianceY, String[] texture){
        float horizon = mapSize.y-600;
        P orig = new P(0, horizon);
        TerrainTrail trail = new TerrainTrail(orig);
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
    static void generateBlocker(int depth, int p){
        float highX = 0;
        Line high = null;
        for (final Line i : Global.map.landRaw) {
            if (i.next == p) {
                if (i.pt.x > highX) {
                    highX = i.pt.x;
                    high = i;
                }
            }
        }
        TerrainTrail blocker = null;
        for (final Line i : Global.map.landRaw) {
            if (i.next == high.next){
                if (blocker == null) blocker = new TerrainTrail(new P(i.pt));
                else blocker.add(i.pt.x, i.pt.y);
                if (i == high){
                    blocker.add(Global.map.size.x+128, -128);
                    blocker.add(-128, -128);
                    blocker.add(-128, Global.map.size.y+128);
                    blocker.add(Global.map.size.x+128, Global.map.size.y+128);
                    blocker.add(Global.map.size.x+128, -128);
                    blocker.add(i.pt.x, i.pt.y);
                }
            }
        }
        blocker.finish(depth+"&_0,0,0,255&none");
        blocker.reverse();
        Iterator it = Global.map.landRaw.iterator();
        Line i;
        while (it.hasNext()) {
            i = (Line) it.next();
            if (i.next == p) it.remove();
        }
        Global.map.landRaw.add(new Line(-64,Global.map.size.y+64, -64,-64, p,0));
        Global.map.landRaw.add(new Line(-64,-64, Global.map.size.x+64,-64, p,0));
        Global.map.landRaw.add(new Line(Global.map.size.x+64,-64, Global.map.size.x+64,Global.map.size.y+64, p,0));
        Global.map.landRaw.add(new Line(Global.map.size.x+64,Global.map.size.y+64, -64,Global.map.size.y+64, p,0));
    }
}
