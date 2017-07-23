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
class GenerateHouse extends Generator {
    Generator create(String[] info){
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
        trail.finish(this, texture+"&none");
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
                stair.finish(this, texture + "&basic");
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
                divider.finishNoDepth("black&none");
                divider.climbable();
                if (sides[i]) divider.switchSides(mapSize.x);
                dividers.add(divider.terrain);
            }
        }

        Prop door = Stock.Door.create(returnDoorSeed);
        P result = new P(mapSize.x / 2, bottom-door.image.getSize().y/2-2);
        door.pos.set(result);
//        Stock.Exit.create(returnDoorSeed).pos.set(result.x,result.y);
        Stock.Spawn.create(returnDoorSeed).pos.set(result.x,result.y);
        int merchantType = Integer.parseInt(info[3]);
        if (merchantType != 0){ //1:food,2:shop,3:guild,4:training
            Stock.Merchant.create(merchantType-1).pos.set(Global.r.nextInt((int) mapSize.x-130)+65,result.y);
            int spacing = Global.r.nextInt(240)+120;
            for (int i=0;i<mapSize.x/spacing;i++) {
                int cx = spacing*i;
                for (Line l : Global.map.land) {
                    if (l.next >= 0 && l.clockwise() && l.getSlope() == 0 && cx-l.pt.x>spacing/2-10 && l.ptd.x-cx>spacing/2-10 && Global.r.nextInt(3)!=0) {
                        float y = l.getCrossPointY(cx);
                        if (merchantType == 1 || merchantType == 2) {
                            Stock.getInstant("Shorttable", -1).pos.set(cx, y - 1);
                            Stock.getInstant("Shortchair", -1).pos.set(cx - 50, y - 1);
                            Stock.getInstant("Shortchair", -1).pos.set(cx + 50, y - 1);
                        } else if (merchantType == 3 || merchantType == 4){
                            Stock.getInstant("Training_Dummy", -1).pos.set(cx, y - 1);
                        }
                    }
                }
            }
        }
        TerrainTrail blocker = generateBlocker(trail.terrain);
        depthBinding.put(blocker.terrain, dividers);
        return this;
    }
    TerrainTrail generateBlocker(int p){
        float highX = 0;
        Line high = null;
        for (final Line i : Global.map.land) {
            if (i.next == p) {
                if (i.pt.x > highX) {
                    highX = i.pt.x;
                    high = i;
                }
            }
        }
        TerrainTrail blocker = null;
        for (final Line i : Global.map.land) {
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
        blocker.finish(this, "black&none");
        blocker.reverse();
        Iterator it = Global.map.land.iterator();
        Line i;
        while (it.hasNext()) {
            i = (Line) it.next();
            if (i.next == p) it.remove();
        }
        Global.map.land.add(new Line(-64,Global.map.size.y+64, -64,-64, p,0));
        Global.map.land.add(new Line(-64,-64, Global.map.size.x+64,-64, p,0));
        Global.map.land.add(new Line(Global.map.size.x+64,-64, Global.map.size.x+64,Global.map.size.y+64, p,0));
        Global.map.land.add(new Line(Global.map.size.x+64,Global.map.size.y+64, -64,Global.map.size.y+64, p,0));
        return blocker;
    }
}
