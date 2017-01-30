package com.ga.map;

import com.ga.game.Sound;
import com.ga.tool.Performer;
import com.ga.unit.Particle;
import com.ga.unit.Player;
import com.ga.unit.Updater;
import com.ga.unit.skill.Actions;

/**
 * Created by Ga on 10/31/2016.
 */
public enum MapEvent implements Updatable{
    None(new Performer(){
        public void perform(){
        }
    }),
    Tutorial(new Performer(){
        public void perform(){
            Player p = Global.player.host;
            if (cookie == null) {
                if (p.pos.x > 3900) {
                    if (p.weapon.name.equals("Guidebook")) {
                        setStage(2);
                    } else {
                        if (p.vel.x > 0) {
                            p.setState(Actions.Idle);
                            p.say("I need the book!");
                        }
                    }
                }
            } else if (cookie.equals("2")){
                if (!p.armor.isBare(1)) setStage(3);
            } else if (cookie.equals("3")){
                if (p.pos.x > 5500 && p.pos.y < 400) setStage(4);
            }
        }
        private void setStage(int p){
            cookie = p+"";
            Particle.holyExplosion(Global.player.host.pos);
            Sound.play("positive");
        }
    });
    private Performer event;
    public static String cookie;
    MapEvent (Performer p){
        event = p;
    }
    public void update(){
        event.perform();
    }
    @Override
    public String toString(){
        return name()+(cookie!=null?":"+cookie:"");
    }
    public static MapEvent getByName(String p){
        String[] t = p.split(":");
        MapEvent r = valueOf(t[0]);
        if (t.length>1) cookie = t[1];
        Updater.add(r);
        return r;
    }
}