package com.ga.map;

import com.ga.tool.Performer;
import com.ga.unit.Stock;

/**
 * Created by Ga on 10/31/2016. asd
 */
public enum MapEvent{
    None(null, new Performer(){
        public void perform(){
        }
    }),
    Rural("Punk=.05,Slime=.1", new Performer(){
        public void perform(){
        }
    }),
    Urban("Resident=.6", new Performer(){
        public void perform(){
        }
    }),
    Tutorial(null, new Performer(){
    });
    private Performer event;
    private String mobs;
    public static String cookie;
    MapEvent (String mobs, Performer p){
        this.mobs = mobs;
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
    private void regrowth(){
        Updater.regrowth();
        Global.map.generateProps();
        if (mobs != null) { //encounter
            String[] moblist = mobs.split(",");
            for (int times = 0; times < 48; times++) {
                float random = Creation.r.nextFloat();
                for (String i : moblist) {
                    String[] mobp = i.split("=");
                    if (random < Float.parseFloat(mobp[1])) {
                        Global.map.placeProp(Global.map.randomLandableSurface(), i);
                        break;
                    }
                }
            }
        }
    }
    public void onChange(){
        regrowth();
    }
}