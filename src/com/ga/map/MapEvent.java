package com.ga.map;

import com.ga.tool.Performer;
import com.ga.unit.Stock;

/**
 * Created by Ga on 10/31/2016. asd
 */
public enum MapEvent{
    None(new Performer(){
        public void perform(){
        }
    }),
    Rural(new Performer(){
        public void perform(){
            Updater.regrowth();
            Global.map.generateProps();
            spawn("Rover=.1,Slime=.1", -1);
        }
    }),
    Urban(new Performer(){
        public void perform(){
            if (Global.map.weather.isNight()){
                Mob.spawn("Gang", Global.map.randomLandableSurface());
            }
        }
    });
    private Performer event;
    public static String cookie;
    MapEvent (Performer p){
        event = p;
    }
    @Override
    public String toString(){
        return name()+(cookie==null?"":"@"+cookie);
    }
    public static MapEvent getByName(String p){
        String[] t = p.split("@");
        MapEvent r = valueOf(t[0]);
        if (t.length > 1) cookie = t[1];
        return r;
    }
    private static void spawn(String p, int seed){
        String[] moblist = p.split(",");
        for (int times = 0; times < 48; times++) {
            float random = Creation.r.nextFloat();
            for (String i : moblist) {
                String[] mobp = i.split("=");
                if (random < Float.parseFloat(mobp[1])) {
                    Global.map.placeProp(Global.map.randomLandableSurface(), i, seed);
                    break;
                }
            }
        }
    }
    public void onChange(){
        event.perform();
    }
}