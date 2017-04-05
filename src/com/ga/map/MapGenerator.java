package com.ga.map;

import com.ga.game.FileManager;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Prop;
import com.ga.unit.Stock;

/**
 * Created by Ga on 2/23/2017. asd
 */
public class MapGenerator {
    private static void generateFloor(int depth, P mapsize, int randomness, int varianceY, String[] texture){
        float horizon = mapsize.y-600;
        P orig = new P(0, horizon), pt1 = orig, pt2 = new P();
        while (pt2.x < mapsize.x) {
            int randX = (Global.r.nextInt(240) + 20) * (Global.r.nextInt(randomness)+1);
            int randY = (Global.r.nextInt(varianceY) - (varianceY/2)) * Global.r.nextInt(randomness);
            pt2 = new P(pt1.x + randX, orig.y + randY);
            if (pt2.x+(200*randomness*.5f) > mapsize.x) pt2.x = mapsize.x;
            Global.map.land.add(new Line(pt1, pt2, 0, 0));
            pt1 = pt2;
        }
        pt1.y = horizon;
        Line t;
        Global.map.land.add(t = new Line(pt1, new P(pt2.x, mapsize.y), 0, 0));
        Global.map.land.add(t = new Line(t.ptd, new P(0, mapsize.y), 0, 0));
        Global.map.land.add(new Line(t.ptd, orig, 0, 0));
        Global.map.formTextures.put(0, depth+"&"+texture[Global.r.nextInt(texture.length)]);
    }
    private static void generateMound(int id, P mapsize, String[] texture, int height){
        float horizon = mapsize.y-600;
        P initPoint = new P(Global.r.nextFloat()*mapsize.x*.3f, mapsize.y);
        P oldPoint = initPoint;
        for (int i=0;i<Global.r.nextInt(7)+3;i++) {
            P newPoint = new P(oldPoint.x + (Global.r.nextFloat()*mapsize.x*.24f), horizon+(Global.r.nextInt(height)-height));
            Global.map.land.add(new Line(oldPoint,newPoint,id,0));
            oldPoint = newPoint;
        }
        oldPoint.y = mapsize.y;
        Global.map.land.add(new Line(oldPoint,initPoint,id,-1));
        Global.map.formTextures.put(id, (3+id)+"&"+texture[Global.r.nextInt(texture.length)]);
    }
    private static void generateFoundation(int id, P mapsize, P point, float lastY, String[] texture){
        int width = (int) Global.clamp((float) Math.abs(mapsize.x*(Global.r.nextGaussian()*.4f)),100,mapsize.x*(7f/8));
        P pt1 = new P(Math.max(600, point.x-width),point.y);
        P pt2 = new P(Math.min(mapsize.x-600,point.x+width),point.y);
        if (pt1.x == 600) pt1.x += Global.r.nextInt(300);
        if (pt2.x == mapsize.x-600) pt2.x -= Global.r.nextInt(300);
        int finalWidth = (int) (pt2.x-pt1.x);
        if (finalWidth <= 0) return;
        P pt3 = new P(pt2.x,mapsize.y-500);
        P pt4 = new P(pt1.x,mapsize.y-500);
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
        P mapsize = new P(houseInfos[0]).mult(1.5f);
        Global.map = new Map(new String[]{"1","None","None",((int)mapsize.x)+"x"+((int)mapsize.y),returnAddr});
        float bottom = mapsize.y - 64;
        P topleft = new P(64, 64), topright = new P(mapsize.x-64, 64),
                bottomleft = new P(64, bottom), bottomright = new P(mapsize.x-64, bottom);
        Global.map.land.add(new Line(bottomleft, bottomright, 0, 0));
        Global.map.land.add(new Line(bottomright, topright, 0, 0));
        Global.map.land.add(new Line(topright, topleft, 0, 0));
        Global.map.land.add(new Line(topleft, bottomleft, 0, 0));
        String[] texture = new String[]{"wood&none","steel&none","brick&none"};
        Global.map.formTextures.put(0, "1&"+texture[Global.r.nextInt(texture.length)]);
        P result = new P(mapsize.x / 2, bottom - 50);
        Stock.getByName("Door",returnDoorSeed).pos.set(result);
        Stock.getByName("Exit",returnDoorSeed).pos.set(result);
        if (houseInfos[1].equals("1")) Stock.getByName("Merchant",-1).pos.set(Global.r.nextInt((int) mapsize.x-30)+15,result.y-20);
    }
    private static void generatePlains(String prevmap, String nextmap){
        P mapsize = new P(7200, 1600);
        Global.map = new Map(new String[]{"0","Rural",Global.map.maps[2],((int)mapsize.x)+"x"+((int)mapsize.y),prevmap,nextmap});
        String[] texture = new String[]{"dirt&grass","dirt&grass&Rock=.1,Bush=.2,Weed=.4,Tree=1"};
        generateFloor(9, mapsize, Global.r.nextInt(9) + 1, 40, texture);
        Global.map.formTextures.put(-1, "9");
        for (int i=0;i<Global.r.nextInt(4);i++) generateMound(i+1, mapsize, texture, Global.r.nextInt(240)+80);
        for (int i=0;i<Global.map.land.size();i++){
            Line l = Global.map.land.get(i);
            if (l.next >= 0 && l.isLandable() && Math.abs(l.getSlope()) < .1f && Math.abs(l.ptd.x-l.pt.x)>500 && Global.r.nextInt(3) == 0) {
                ObjectGenerator.generateSingleHouse(l);
            }
        }
    }
    private static void generateCity(String prevmap, String nextmap){
        P mapsize = new P(10000, 6000);
        Global.map = new Map(new String[]{"0","Urban",Global.map.maps[2],((int)mapsize.x)+"x"+((int)mapsize.y),prevmap,nextmap});
        Line[] lands = new Line[14];
        int fragment = (int) (mapsize.x/14);
        for (int i=0;i<lands.length;i++) Global.map.land.add(lands[i] = new Line(i*fragment,mapsize.y-600,(i+1)*fragment,mapsize.y-600,0,0));
        Global.map.land.add(new Line(mapsize.x,mapsize.y-600,mapsize.x,mapsize.y,0,0));
        Global.map.land.add(new Line(mapsize.x,mapsize.y,0,mapsize.y,0,0));
        Global.map.land.add(new Line(0,mapsize.y,0,mapsize.y-600,0,0));
        Global.map.formTextures.put(0, "9&steel&road");
        Global.map.formTextures.put(-1, "9");
        for (int i=3;i<lands.length-3;i++){
            ObjectGenerator.generateSkyscraper(lands[i]);
        }
    }
    private static void generateTown(String prevmap, String nextmap){
        P mapsize = new P(6000, 4000);
        Global.map = new Map(new String[]{"0","None",Global.map.maps[2],((int)mapsize.x)+"x"+((int)mapsize.y),prevmap,nextmap});
        String[] foundationTexture = new String[]{"brick&basic","marble&basic","concrete&basic","rock&basic","wood&basic"};
        float lastY = mapsize.y-700-Global.r.nextInt(160);
        int max = Global.r.nextInt(6)+4;
        for (int i=0;i<max;i++){
            P pos = new P(Global.r.nextInt(4000)+1000,lastY-(Global.r.nextInt(360)+80));
            generateFoundation(max-i, mapsize, pos, lastY, foundationTexture);
            lastY = pos.y;
        }
        Global.map.formTextures.put(-1, ""+max+1);
        generateFloor(max+1, mapsize, Global.r.nextInt(4) + 7, 20, new String[]{"dirt&road","dirt&grass","rock&road","rock&grass"});
        for (int i=0;i<Global.map.land.size();i++){
            Line l = Global.map.land.get(i);
            if (l.next >= 0 && l.isLandable() && Math.abs(l.getSlope()) < .1f && Math.abs(l.ptd.x-l.pt.x)>500) {
                ObjectGenerator.generateSingleHouse(l);
            }
        }
    }
    public static void generateMap(String map, String prevmap, String nextmap){
        switch (map) {
            case "Plains":
                generatePlains(prevmap, nextmap);
                break;
            case "City":
                generateCity(prevmap, nextmap);
                break;
            case "Town":
                generateTown(prevmap, nextmap);
                break;
        }
    }
}

class ObjectGenerator{
    private static void generateHouse(String name, Line land){
        Prop base = Stock.getByName(name, -1);
        FileManager.delete("map/House_"+Global.thouseId+".map");
        float lineX = land.getMidPoint().x;
        if (Math.abs(land.getSlope()) > .1f) lineX += base.image.size.x/2*(land.getSlope()>0?1:-1);
        float lineY = land.getCrossPointY(lineX);
        P tpos = new P(land.getMidPoint().x, lineY-base.image.size.y/2+8);
        base.pos.load(tpos.toString());
        P s = base.image.getSize().getMult(.5f, .5f);
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