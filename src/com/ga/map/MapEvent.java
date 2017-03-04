package com.ga.map;

import com.ga.game.Dialog;
import com.ga.game.Sound;
import com.ga.tool.Performer;
import com.ga.unit.Particle;
import com.ga.unit.Player;
import com.ga.unit.Updater;
import com.ga.unit.factory.Stock;
import com.ga.unit.item.Item;

/**
 * Created by Ga on 10/31/2016.
 */
public enum MapEvent implements Updatable{
    None("Slime=.2", new Performer(){
        public void perform(){
        }
    }),
    Tutorial(null, new Performer(){
        public void perform(){
            Player p = Global.player.host;
            if (cookie == null) {
                if (p.pos.x > 2700){
                    setStage(1);
                    Item t = (Item) Stock.get(new String[]{"79","1","-1,-1"});
                    t.field();
                    t.pos.set(p.pos.getAdd(0,-300));
                }
            } else if (cookie.equals("1")){
                if (p.pos.x > 4200) {
                    if (p.weapon.name.equals("Guidebook")) {
                        setStage(2);
                    } else {
                        if (p.vel.x > 0) {
                            p.say(Dialog.get("tutorialSay1"));
                        }
                    }
                }
            } else if (cookie.equals("2")){
                if (p.pos.x > 4200 && p.pos.y < 400) setStage(3);
            } else if (cookie.equals("3")){
                if (p.pos.x > 5800 && p.pos.y < 400) setStage(4);
            }
        }
        private void setStage(int p){
            cookie = p+"";
            Particle.holyExplosion(Global.player.host.pos);
            Sound.play("positive");
            if (p > 1) Global.player.host.say(Dialog.get("tutorialSay2"));
        }
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
        Updater.add(r);
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
                        Stock.getByName(mobp[0], -1).pos.set(Global.map.randomSurface());
                        break;
                    }
                }
            }
        }
    }
    public void onChange(String[] oldmap, boolean regrow){
        if (oldmap != null) { //not init
            if (!Global.map.inside && !oldmap[0].equals("1")) Global.nextHour();
            if (!Global.map.inside && regrow) { //regrowth
                regrowth();
            }
        }
    }
}