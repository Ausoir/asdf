package com.ga.map;

import com.ga.game.Dialog;
import com.ga.game.Sound;
import com.ga.tool.Performer;
import com.ga.unit.Particle;
import com.ga.unit.Updater;
import com.ga.unit.Stock;
import com.ga.unit.item.Item;

/**
 * Created by Ga on 10/31/2016.
 */
public enum MapEvent implements Updatable{
    None(null, new Performer(){
        public void perform(){
        }
    }),
    Rural("Punk=.05,Slime=.1", new Performer(){
        public void perform(){
        }
    }),
    Urban("Guard=.1,Punk=.1", new Performer(){
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
    public void update(){
        event.perform();
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
            for (int times = 0; times < 24; times++) {
                float random = Global.r.nextFloat();
                for (String i : moblist) {
                    String[] mobp = i.split("=");
                    if (random < Float.parseFloat(mobp[1])) {
                        Stock.getByName(mobp[0], -1).pos.set(Global.map.randomSurface().getAdd(0,-60));
                        break;
                    }
                }
            }
        }
    }
    public void onChange(int timeChange){
        if (timeChange > 0){
            Global.setHour(Global.gameHour+timeChange);
            regrowth();
        }
    }
}