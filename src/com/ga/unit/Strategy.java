package com.ga.unit;

import com.ga.tool.P;
import com.ga.tool.SyncRand;
import com.ga.unit.man.Man;
import com.ga.unit.skill.Actions;

import java.util.ArrayList;

public enum Strategy {
    Brute, //non-humanoid
    Spear{
        public void micromanage(Unit host, float dist){
            super.micromanage(host, dist);
            if (host.state == Actions.Hold){
                host.delay.time = -1;
                if (dist > 300) move(host, dist);
            }
        }
    }, Careful, Ranged, Magic;
    public float getDist(Unit host){
        int x = (int) ((host.brain.target.pos.x + host.brain.target.vel.x) - (host.pos.x + host.vel.x));
        int y = (int) (host.brain.target.pos.y - host.pos.y);
        host.target = new P(x,y);
        if (host instanceof Man) ((Man)host).weapon.rotateTo(host.target.getMult(host.dir,host.dir));
        return (float) Math.abs(Math.sqrt(x * x + y * y) - host.getReach());
    }
    void move(Unit host, float dist){
        if (dist > 100){
            if (host.setState(Actions.Sprint) != 0) host.setState(Actions.Right);
        } else host.setState(Actions.Right);
    }
    public void micromanage(Unit host, float dist){
        if (host.state == Actions.Rest) host.delay.time = (12 - host.stateLevel) * host.get("Stamina").getMax();
        else if (host.state.getTags().contains(Tag.Channeling)) host.delay.set(100);
    }
    public void update(Unit host){
        float dist = getDist(host);
        Actions action = Actions.Idle;
        ArrayList<Actions> moves = new ArrayList<>();
        if (host.get("Stamina").get() == 0) action = Actions.Rest;
        else {
            for (Actions act : host.moveset) {
                boolean added = false;
                float distanceDiscretion = act.getRange(host) - dist;
                if (distanceDiscretion >= 0) {
                    for (int i = 0; i < moves.size(); i++) {
                        if (distanceDiscretion < moves.get(i).getRange(host) - dist) {
                            moves.add(i, act);
                            added = true;
                            break;
                        }
                    }
                    if (!added) moves.add(act);
                }
            }
            boolean move = false;
            if (moves.size() > 0 && SyncRand.r.nextInt(3) != 0) action = moves.get(0);
            else if (moves.size() > 1 && SyncRand.r.nextInt(3) != 0) action = moves.get(1);
            else if (moves.size() > 2) action = moves.get(SyncRand.r.nextInt(moves.size()));
            else move = true;
            if (dist > action.getRange(host)) move = true;
            if (move) move(host, dist);
        }
        int fb = host.setState(action);
        if (fb == 2) host.setState(Actions.Rest);
        micromanage(host, dist);
        host.say(host.get("Stamina").toString() + " " + action.name() + " " + fb);
    }
}
