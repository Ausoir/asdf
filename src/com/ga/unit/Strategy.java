package com.ga.unit;

import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.tool.SyncRand;
import com.ga.unit.man.Man;
import com.ga.unit.skill.Actions;

import java.util.ArrayList;

public enum Strategy {
    Brute, //non-humanoid
    Melee{ //humanoid
        public int micromanage(Unit host, Actions action, float dist){
            if (action == Actions.Use){
                Man man = (Man) host;
                if (man.wieldingNone()){
                    action = Actions.Punch;
                } else if (!man.weapon.isUsable(false, 1)){
                    action = Actions.Throw;
                }
            }
            return super.micromanage(host, action, dist);
        }
    },
    Ranged{
        public int micromanage(Unit host, Actions action, float dist){
            if (action == Actions.Use && dist < 32){
                action = Actions.Punch;
            }
            return Melee.micromanage(host, action, dist);
        }
    };
    public float getDist(Unit host){
        int avgX = 0, avgY = 0;
        for (Organ i : host.brain.target.clip.organs){
            avgX += i.pt.x + i.ptd.x;
            avgY += i.pt.y + i.ptd.y;
        }
        avgX /= host.brain.target.clip.organs.length*2;
        avgY /= host.brain.target.clip.organs.length*2;
        P hostPos = host.getHand();
        P targetPos = host.brain.target.pos;
        int x = (int) ((targetPos.x + host.brain.target.vel.x) - (hostPos.x + host.vel.x));
        int y = (int) (targetPos.y - hostPos.y);
        host.target = new P(x+avgX,y+avgY);
        if (host instanceof Man) ((Man)host).weapon.rotateTo(host.target.getMult(host.dir,host.dir));
        return (float) Math.abs(Math.sqrt(x * x + y * y) - host.getReach());
    }
    static Actions getMove(Unit host, P target){
        int x = (int) ((target.x) - (host.pos.x + host.vel.x));
        int y = (int) (target.y - host.pos.y);
        if (Math.abs(y*2) > Math.abs(x)) {
            if (y > 0) return Actions.Drop;
            else return Actions.Jump;
        }
        if (Math.abs(x) > 100 && host.moveset.contains(Actions.Sprint)) return Actions.Sprint;
        return host.dir == 1 ? Actions.Right : Actions.Left;
    }
    public int micromanage(Unit host, Actions action, float dist){
        int fb = host.setState(action);
        action.specialOrder(host, dist);
        if (host.state.getTags().contains(Tag.Channeling)) host.delay.set(160);
        return fb;
    }
    public void update(Unit host){
        float dist = getDist(host);
        host.face(host.brain.target.pos.x>host.pos.x?1:-1, false);
        Actions action;
        ArrayList<Actions> moves = new ArrayList<>();
        for (Actions act : host.moveset) {
            if (dist < act.getRange(host) && host.getCooldown(act).isReady()) {
                if (host.get("Strength").get() >= act.getCost()) moves.add(act);
                else moves.add(Actions.Rest);
            }
        }
        if (moves.size() > 0 && SyncRand.r.nextInt(3) != 0) action = moves.get(0);
        else if (moves.size() > 1 && SyncRand.r.nextInt(3) != 0) action = moves.get(1);
        else if (moves.size() > 2) action = moves.get(SyncRand.r.nextInt(moves.size()));
        else action = getMove(host, host.brain.target.pos);
        int fb = micromanage(host, action, dist);
//        host.say(host.get("Strength").toString() + " " + action.name() + " " + fb);
    }
}