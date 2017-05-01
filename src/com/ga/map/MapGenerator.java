package com.ga.map;

import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Prop;
import com.ga.unit.Stock;

import java.util.ArrayList;

/**
 * Created by Ga on 2/23/2017. asd
 */
public class MapGenerator {
    private static void generateFloor(int depth, P mapSize, int randomness, int varianceY, String[] texture){
        float horizon = mapSize.y-600;
        P orig = new P(0, horizon), pt1 = orig, pt2 = new P();
        while (pt2.x < mapSize.x) {
            int randX = (Global.r.nextInt(240) + 20) * (Global.r.nextInt(randomness)+1);
            int randY = (Global.r.nextInt(varianceY) - (varianceY/2)) * Global.r.nextInt(randomness);
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
        Global.map.formTextures.put(0, depth+"&"+texture[Global.r.nextInt(texture.length)]);
    }
    private static void generateMound(int id, P mapSize, String[] texture, int height){
        float horizon = mapSize.y-600;
        P initPoint = new P(Global.r.nextFloat()*mapSize.x*.3f, mapSize.y);
        P oldPoint = initPoint;
        for (int i=0;i<Global.r.nextInt(7)+3;i++) {
            P newPoint = new P(oldPoint.x + (Global.r.nextFloat()*mapSize.x*.24f), horizon+(Global.r.nextInt(height)-height));
            Global.map.land.add(new Line(oldPoint,newPoint,id,0));
            oldPoint = newPoint;
        }
        oldPoint.y = mapSize.y;
        Global.map.land.add(new Line(oldPoint,initPoint,id,-1));
        Global.map.formTextures.put(id, (3+id)+"&"+texture[Global.r.nextInt(texture.length)]);
    }
    private static void generateFoundation(int id, P mapSize, P point, float lastY, String[] texture){
        int width = (int) Global.clamp((float) Math.abs(mapSize.x*(Global.r.nextGaussian()*.4f)),100,mapSize.x*(7f/8));
        P pt1 = new P(Math.max(600, point.x-width),point.y);
        P pt2 = new P(Math.min(mapSize.x-600,point.x+width),point.y);
        if (pt1.x == 600) pt1.x += Global.r.nextInt(300);
        if (pt2.x == mapSize.x-600) pt2.x -= Global.r.nextInt(300);
        int finalWidth = (int) (pt2.x-pt1.x);
        if (finalWidth <= 0) return;
        P pt3 = new P(pt2.x,mapSize.y-500);
        P pt4 = new P(pt1.x,mapSize.y-500);
        int midWidth = (int) ((lastY-point.y)*(Global.r.nextFloat()*.3f+.5f))*2;
        if (finalWidth-midWidth*2 > 100 && Global.r.nextInt(4) != 0){
            int mid = Global.r.nextInt(finalWidth-midWidth*2)+midWidth;
            P pt11 = new P(pt1.x+(mid-midWidth),point.y);
            P pt12 = new P(pt1.x+(mid+midWidth),point.y);
            float drop = lastY-point.y;
            if (Global.r.nextBoolean()){
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
        Global.map.formTextures.put(id, id+"&"+texture[Global.r.nextInt(texture.length)]);
    }
    public static void generateHouse(final String returnAddr, int returnDoorSeed, String houseInfo){
        String[] houseInfos = houseInfo.split("&");
        P mapSize = new P(houseInfos[0]).mult(2f);
        Global.map = new Map(new String[]{"-1","None","None",((int)mapSize.x)+"x"+((int)mapSize.y),returnAddr});
        float bottom = mapSize.y - 64;
        P topleft = new P(64, 64), topright = new P(mapSize.x-64, 64),
                bottomleft = new P(64, bottom), bottomright = new P(mapSize.x-64, bottom);

        String[] textures = new String[]{"wood","steel","brick"};
        String texture = textures[Global.r.nextInt(textures.length)];

        ArrayList<TerrainTrail> alltrails = new ArrayList<>();
        TerrainTrail trail = new TerrainTrail(0,bottomleft);
        alltrails.add(trail);
        trail.add(bottomright);
        int floorNum = Integer.parseInt(houseInfos[2]);
        int terrain = 1;
        if (floorNum <= 1) {
            trail.add(topright);
            trail.add(topleft);

            TerrainTrail floor2 = new TerrainTrail(terrain++, new P());
            alltrails.add(floor2);
            if (Global.r.nextBoolean()) {
                floor2.reset(new P(48,mapSize.y+4));
                floor2.add(48, mapSize.y/2);
                floor2.add(mapSize.x-48, mapSize.y/2);
                floor2.add(mapSize.x-48, mapSize.y+4);
                alltrails.add(new TerrainTrail(new Line(256,mapSize.y/2-64,256,mapSize.y,-1,1)).finish(""));
            }
            floor2.finish("1&" + texture + "&basic");
            floor2.climbable();
        } else {
            float floorHeight = mapSize.y/(Integer.parseInt(houseInfos[2])+1);
            int floors = Math.max(0,Integer.parseInt(houseInfos[2])-1);
            ArrayList<Float> returnFloor = new ArrayList<>();
            for (int i=0;i<floors;i++) {
                if (Global.r.nextBoolean()) {
                    TerrainTrail stair = new TerrainTrail(terrain++,new P(48,trail.last.y+4));
                    stair.move(0,-floorHeight-40);
                    stair.move(80,0);
                    stair.move(320,floorHeight+40);
                    stair.finish("2&"+texture+"&basic");
                    stair.climbable();
                    alltrails.add(stair);

                    trail.move(0, -floorHeight);
                    trail.move(-(mapSize.x -420), 0);
                    trail.move(0, -20);
                    trail.move(-(420-128), 0);
                    trail.lastOpt(-1);
                    trail.move(0, -20);
                    trail.move(420-128, 0);
                    trail.move(mapSize.x -420, 0);
                    trail.lastOpt(2);
                } else {
                    trail.move(0, -floorHeight-40);
                    returnFloor.add(trail.last.y);
                }
            }

            trail.add(topright);
            trail.add(topleft);
            for (int i=returnFloor.size()-1;i>=0;i--) {
                trail.add(64,returnFloor.get(i));

                TerrainTrail stair = new TerrainTrail(terrain++,new P(mapSize.x-48,floorHeight+40+trail.last.y));
                stair.move(-400,4);
                stair.move(320,-floorHeight-40);
                stair.move(80,0);
                stair.finish("2&"+texture+"&basic");
                stair.climbable();
                alltrails.add(stair);

                trail.move(mapSize.x-420, 0);
                trail.lastOpt(2);
                trail.move(420-128, 0);
                trail.move(0, 20);
                trail.move(-(420-128), 0);
                trail.lastOpt(-1);
                trail.move(0, 20);
                trail.move(-(mapSize.x - 420), 0);
            }
        }
        trail.finish("3&"+texture+"&none");

        if (Global.r.nextBoolean()){
            for (TerrainTrail t : alltrails) TerrainTrail.switchSides(t.lines, mapSize.x);
        }
        P result = new P(mapSize.x / 2, bottom-4);
        Prop door = Stock.Door.create(returnDoorSeed);
        door.pos.set(result);
        Stock.Exit.create(returnDoorSeed).pos.set(result.x,result.y-door.image.size.y/2);
        Stock.Spawn.create(returnDoorSeed).pos.set(result.x,result.y-door.image.size.y/2);
        int merchantType = Integer.parseInt(houseInfos[1]);
        if (merchantType != 0){ //1:food,2:shop,3:guild,4:training
            Stock.Merchant.create(merchantType).pos.set(Global.r.nextInt((int) mapSize.x-130)+65,result.y-20);
            int spacing = Global.r.nextInt(240)+120;
            for (int i=0;i<mapSize.x/spacing;i++) {
                int cx = spacing*i;
                for (Line l : Global.map.land) {
                    if (l.next >= 0 && l.isLandable() && l.getSlope() == 0 && cx-l.pt.x>spacing/2-10 && l.ptd.x-cx>spacing/2-10 && Global.r.nextInt(3)!=0) {
                        float y = l.getCrossPointY(cx);
                        if (merchantType == 1 || merchantType == 2) {
                            Stock.getInstant("Shorttable", -1).pos.set(cx, y - 1);
                            Stock.getInstant("Shortchair", -1).pos.set(cx - 50, y - 1);
                            Stock.getInstant("Shortchair", -1).pos.set(cx + 50, y - 1);
                        } else if (merchantType == 3 || merchantType == 4){
                            Stock.getInstant("Dummy", -1).pos.set(cx, y - 1);
                        }
                    }
                }
            }
        }
    }
    private static void generateMerge(int id){
        P mapSize = new P(8000, 5600);
        Global.map = new Map(new String[]{id+"","Rural",Global.map.maps[2],((int)mapSize.x)+"x"+((int)mapSize.y)});
        String[] texture = new String[]{"dirt&grass","dirt&grass&Rock=.1,Bush=.2,Weed=.4,Tree=1"};
        generateFloor(9, mapSize, Global.r.nextInt(9) + 1, 40, texture);
        Global.map.formTextures.put(-1, "9");
        float lastY = mapSize.y-700;
        int max = Global.r.nextInt(5)+2;
        for (int i=0;i<max;i++){
            P pos = new P(Global.r.nextInt(6000)+1000, lastY-(Global.r.nextInt(480)+180));
            float left = pos.x-Global.r.nextInt(720)-80, right = pos.x+Global.r.nextInt(720)+80;
            TerrainTrail trail = new TerrainTrail(i+1,new P(left,pos.y));
            trail.add(right, trail.last.y);
            int width = (int) ((right-left)*(Global.r.nextFloat()*.4f+.1f));
            int bottomright = (int) (right-Global.r.nextInt(width));
            int bottomleft = (int) (left+Global.r.nextInt(width));
            for (int ii = bottomright;ii >= bottomleft; ii-=Global.r.nextInt(240)+40){
                trail.add(ii,pos.y+(Global.r.nextInt(120)+80));
            }
            trail.finish((i+1)+"&dirt&grass");
            lastY = pos.y;
        }
    }
    private static void createFloater(int id, P p, int rad, int depth){
        TerrainTrail trail = new TerrainTrail(id, new P(p.x+rad, p.y));
        float gap = 3.14f/(Global.r.nextInt(5)+3);
        float currentArc = 0;
        while (currentArc + gap < 3.14f) {
            currentArc += Global.r.nextFloat() * gap + gap * .5f;
            currentArc = Math.min(currentArc, 3.14f);
            int radVariance = Global.r.nextInt(rad/3) + rad * 2 / 3;
            trail.add(p.x + Global.cos(currentArc) * radVariance, p.y + Global.sin(currentArc) * radVariance);
        }
        trail.add(new P(p.x-rad, p.y));
        trail.finish(depth+"&dirt&grass");
        trail.climbable();
    }
    private static void createFloaters(P mapSize, int num, int id){
        for (int i=0;i<num;i++) {
            P spot = new P(Global.r.nextInt((int) mapSize.x), Global.r.nextInt((int) mapSize.y-800)+600);
            int floaterDepth = i + 2;
            Line closest = null;
            float closestY = spot.y + 9999;
            Line line = new Line(spot, new P(spot.x, closestY));
            for (Line l : Global.map.land) {
                if (line.lineCross(l)) {
                    if (l.next != -1 && l.isLandable()) {
                        float y = l.getCrossPointY(spot.x);
                        if (y < closestY) {
                            closest = l;
                            closestY = y;
                        }
                    }
                }
            }
            if (closest != null) {
                floaterDepth = Integer.parseInt(Global.map.formTextures.get(closest.next).split("&")[0]) - 1;
                if (closestY-spot.y < 800) Global.map.land.add(new Line(spot.x, spot.y-64, spot.x, closestY, -1, 1));
            }
            createFloater(i + id, spot, Global.r.nextInt(1280) + 240, floaterDepth);
        }
    }
    private static void divideLand(ArrayList<Line> total, int times, P mapSize){
        if (times >= 8) return;
        Line last = total.remove(total.size()-1);
        P mid = new P(Global.r.nextFloat()*(last.ptd.x-last.pt.x)+last.pt.x,
                Global.r.nextFloat()*(last.ptd.y-last.pt.y)+last.pt.y);
        Line one = new Line(last.pt, mid);
        Line two = new Line(mid, last.ptd);
        float shortest = last.getLength();
        shortest /= 2*times;
        float atan = last.getAtan()+1.57f;
        float rand = Global.r.nextFloat()*2-1;
        mid.add(Global.cos(atan)*shortest*rand, Global.sin(atan)*shortest*rand);
        mid.x = Global.clamp(mid.x, 256, mapSize.x-mapSize.x/5);
        mid.y = Global.clamp(mid.y, 256, mapSize.y-mapSize.y/5);
        total.add(one);
        divideLand(total, ++times, mapSize);
        total.add(two);
        divideLand(total, ++times, mapSize);
    }
    private static void generateSlope(P mapSize, int id, P start, P end){
        Global.map = new Map(new String[]{id+"","Rural",Global.map.maps[2],((int)mapSize.x)+"x"+((int)mapSize.y)});
        TerrainTrail trail = new TerrainTrail(0, start);
        Line first = new Line(trail.last, end);
        ArrayList<Line> total = new ArrayList<>();
        total.add(first);
        divideLand(total, 2, mapSize);
        for (Line i : total) trail.add(i);
        trail.add(new P(first.ptd.x, mapSize.y+64));
        trail.add(new P(start.x, mapSize.y+64));
        trail.finish("1&dirt&grass");
    }
    private static void generatePlains(int id){
        P mapSize = new P(7200, 1600);
        Global.map = new Map(new String[]{id+"","Rural",Global.map.maps[2],((int)mapSize.x)+"x"+((int)mapSize.y)});
        String[] texture = new String[]{"dirt&grass","dirt&grass&Rock=.1,Bush=.2,Weed=.4,Tree=1"};
        generateFloor(9, mapSize, Global.r.nextInt(9) + 1, 40, texture);
        Global.map.formTextures.put(-1, "9");
        for (int i=0;i<Global.r.nextInt(4);i++) generateMound(i+1, mapSize, texture, Global.r.nextInt(240)+80);
        for (int i=0;i<Global.map.land.size();i++){
            Line l = Global.map.land.get(i);
            if (l.next >= 0 && l.isLandable() && Math.abs(l.getSlope()) < .1f && Math.abs(l.ptd.x-l.pt.x) > 500 && Global.r.nextInt(3) == 0) {
                ObjectGenerator.generateSingleHouse(l);
            }
        }
    }
    private static void generateCity(int id){
        P mapSize = new P(10000, 6000);
        Global.map = new Map(new String[]{id+"","Urban",Global.map.maps[2],((int)mapSize.x)+"x"+((int)mapSize.y)});
        Line[] lands = new Line[14];
        int fragment = (int) (mapSize.x/14);
        for (int i=0;i<lands.length;i++) Global.map.land.add(lands[i] = new Line(i*fragment,mapSize.y-600,(i+1)*fragment,mapSize.y-600,0,0));
        Global.map.land.add(new Line(mapSize.x,mapSize.y-600,mapSize.x,mapSize.y,0,0));
        Global.map.land.add(new Line(mapSize.x,mapSize.y,0,mapSize.y,0,0));
        Global.map.land.add(new Line(0,mapSize.y,0,mapSize.y-600,0,0));
        Global.map.formTextures.put(0, "9&steel&road");
        Global.map.formTextures.put(-1, "9");
        for (int i=3;i<lands.length-3;i++){
            ObjectGenerator.generateSkyscraper(lands[i]);
        }
        ArrayList<TerrainTrail> alltrails = new ArrayList<>();
        for (Line l : Global.map.land){
            if (l.next == -1 && Global.r.nextInt(3)!=0){
                Line next;
                boolean right = Global.r.nextBoolean();
                if (right) next = new Line(l.ptd.x+fragment-8,l.ptd.y-500,l.ptd.x+fragment-8,l.ptd.y+500);
                else next = new Line(l.pt.x-fragment+8,l.pt.y-500,l.pt.x-fragment+8,l.pt.y+500);
                for (Line l2 : Global.map.land) {
                    if (l2.lineCross(next)){
                        TerrainTrail trail;
                        int margin = 16;
                        if (l2.next != -1) margin += Global.r.nextInt((int) (l.ptd.x-l.pt.x-32));
                        if (right) {
                            trail = new TerrainTrail(1 + alltrails.size(), l.ptd.getAdd(-20, 0));
                            trail.add(l2.pt.getAdd(margin-4, 0));
                            trail.add(l2.pt.getAdd(margin+4, 2));
                            trail.add(l.ptd.getAdd(-12, 2));
                        } else {
                            trail = new TerrainTrail(1 + alltrails.size(), l2.ptd.getAdd(-20, 0));
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
            trail.finish("9&stair&none");
            trail.climbable();
        }
    }
    private static void generateTown(int id){
        P mapSize = new P(6000, 4000);
        Global.map = new Map(new String[]{id+"","None",Global.map.maps[2],((int)mapSize.x)+"x"+((int)mapSize.y)});
        String[] foundationTexture = new String[]{"brick&basic","marble&basic","concrete&basic","rock&basic","wood&basic"};
        float lastY = mapSize.y-700-Global.r.nextInt(160);
        int max = Global.r.nextInt(6)+4;
        for (int i=0;i<max;i++){
            P pos = new P(Global.r.nextInt(4000)+1000,lastY-(Global.r.nextInt(360)+80));
            generateFoundation(max-i, mapSize, pos, lastY, foundationTexture);
            lastY = pos.y;
        }
        Global.map.formTextures.put(-1, ""+max+1);
        generateFloor(max+1, mapSize, Global.r.nextInt(4) + 7, 20, new String[]{"dirt&road","dirt&grass","rock&road","rock&grass"});
        for (int i=0;i<Global.map.land.size();i++){
            Line l = Global.map.land.get(i);
            if (l.next >= 0 && l.isLandable() && Math.abs(l.getSlope()) < .1f && Math.abs(l.ptd.x-l.pt.x)>500) {
                ObjectGenerator.generateSingleHouse(l);
            }
        }
    }
    public static void generateMap(int id, char terrain){
        P mapSize = new P(12000, 3600);
        int randY = (int) (Global.r.nextInt((int) (mapSize.y/2))+mapSize.y/4);
        if (terrain == '*') {
            generateMerge(id);
        } else if (terrain == '4' || terrain == '6' || terrain == '2' || terrain == '8') {
            if (Global.r.nextInt(4) != 0) generateTown(id);
            else generateCity(id);
        } else if (terrain == '-') {
            generatePlains(id);
        } else if (terrain == 'I') {
            generatePlains(id);
        } else if (terrain == '1') {
            generateSlope(mapSize, id, new P(-64, randY),
                    new P(mapSize.x-Global.r.nextInt(800)-200, mapSize.y+64));
            createFloaters(mapSize, Global.r.nextInt(7), 1);
        } else if (terrain == '3') {
            generateSlope(mapSize, id, new P(Global.r.nextInt(800)+200, mapSize.y+64),
                    new P(mapSize.x+64, randY));
            createFloaters(mapSize, Global.r.nextInt(7), 1);
        } else if (terrain == '7') {
            generateSlope(mapSize, id, new P(-64, randY),
                    new P(mapSize.x-Global.r.nextInt(800)-200, -64));
            createFloaters(mapSize, Global.r.nextInt(7), 1);
        } else if (terrain == '9') {
            generateSlope(mapSize, id, new P(Global.r.nextInt(800)+200, -64),
                    new P(mapSize.x+64, randY));
            createFloaters(mapSize, Global.r.nextInt(7), 1);
        } else if (terrain == 'n') {
            generateSlope(mapSize, id, new P(-64, randY),
                    new P(mapSize.x+64, randY));
            generateSlope(mapSize, id, new P(Global.r.nextInt(800)+200, mapSize.y+64),
                    new P(mapSize.x+64, randY));
            createFloaters(mapSize, Global.r.nextInt(7), 1);
        } else {
            System.out.println("Failed to generate map for terrain '"+terrain+"'");
            System.exit(1);
        }
    }
}

class ObjectGenerator{
    private static void generateHouse(String name, Line land){
        Prop base = Stock.getByName(name, -1);
        float lineX = land.getMidPoint().x;
        if (Math.abs(land.getSlope()) > .1f) lineX += base.image.size.x/2*(land.getSlope()>0?1:-1);
        float lineY = land.getCrossPointY(lineX);
        P tpos = new P(land.getMidPoint().x, lineY);
        base.pos.load(tpos.toString());
        P s = base.image.getSize().getMult(.5f, .5f);
        if (!name.equals("TowerTip"))
            Global.map.land.add(new Line(tpos.x-s.x, land.getCrossPointY(land.getMidPoint().x-base.image.size.x/2)-s.y*2+8,
                tpos.x+s.x, land.getCrossPointY(land.getMidPoint().x+base.image.size.x/2)-s.y*2+8,-1,0,land.depth));
    }
    static void generateSingleHouse(Line land){
        if (Global.r.nextBoolean()) generateHouse("SteamHouse",land);
        else generateHouse("TowerHouse",land);
    }
    static void generateSkyscraper(Line land){
        generateHouse("TowerHouse",land);
        for (int i=0;i<Global.r.nextInt(7)+3;i++) {
            generateHouse("TowerHouse", Global.map.land.get(Global.map.land.size()-1));
        }
        generateHouse("TowerTip", Global.map.land.get(Global.map.land.size()-1));
    }
}

class TerrainTrail{
    ArrayList<Line> lines = new ArrayList<>();
    private int terrain;
    P last;
    TerrainTrail(int terrain, P p){
        this.terrain = terrain;
        reset(p);
    }
    TerrainTrail(Line p){
        lines.add(p);
        last = p.ptd;
    }
    void reset(P p){
        last = p;
    }
    void add(float x, float y){
        add(new P(x,y));
    }
    void move(float x, float y){
        add(last.x+x,last.y+y);
    }
    void add(P p){
        lines.add(new Line(last, p, terrain, 0));
        last = p;
    }
    void add(Line p){
        p.next = terrain;
        p.opt = 0;
        lines.add(p);
        last = p.ptd;
    }
    void lastOpt(int p){
        lines.get(lines.size()-1).opt = p;
    }
    void climbable(){
        for (Line i : lines) {
            if (!i.clockwise()) i.opt = -1;
        }
    }
    static void switchSides(ArrayList<Line> lines, float x2){
        for (Line i : lines) {
            if (i.opt == 1){
                i.pt = new P(x2-i.pt.x,i.pt.y);
                i.ptd = new P(x2-i.ptd.x,i.ptd.y);
            } else {
                P t = new P(i.pt);
                i.pt = new P(x2 - i.ptd.x, i.ptd.y);
                i.ptd = new P(x2 - t.x, t.y);
            }
        }
    }
    private void connect(){
        if (lines.size() > 1) add(lines.get(0).pt);
    }
    private void setTexture(String p){
        Global.map.formTextures.put(terrain, p);
    }
    TerrainTrail finish(String p){
        if (lines.size() > 0) {
            connect();
            Global.map.land.addAll(lines);
            if (!p.equals("")) setTexture(p);
        }
        return this;
    }
}