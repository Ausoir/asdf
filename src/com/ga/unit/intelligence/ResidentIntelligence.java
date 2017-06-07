package com.ga.unit.intelligence;

import com.ga.map.Global;
import com.ga.map.Updater;
import com.ga.tool.Timer;
import com.ga.unit.Man;
import com.ga.unit.Prop;
import com.ga.unit.Unit;

import java.util.ArrayList;

/**
 * Created by Ga on 3/6/2017. asd
 */
public class ResidentIntelligence extends Intelligence {
    Timer advertise;
    String ad = "@talk";
    public ResidentIntelligence(Unit man) {
        super(man);
        advertise = new Timer(100+man.rand.nextInt(200), false);
    }
    @Override
    public void hear(Prop u, String p){
        super.hear(u, p);
        if (u == Global.player){
            if ((u.dir == 1 && u.pos.x < host.pos.x) || u.dir == -1 && u.pos.x > host.pos.x) {
                if (u.pos.getSubtract(host.pos).getDist() < 160) host.say("@talk");
            }
        }
    }
    @Override
    public boolean perceived(Prop p){
        if (p instanceof Man) {
            if (target != p) {
                target = (Man) p;
                setTaskEcho(Behavior.Idle);
                if (advertise.over() && host.getConversation().equals("")){
                    if (host.rand.nextFloat() < .5f) host.say("@c");
                    else host.say(ad);
                    advertise.reset();
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public void update(){
        super.update();
        advertise.update();
    }
}