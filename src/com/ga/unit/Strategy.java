package com.ga.unit;

import com.ga.tool.P;
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
            if (action == Actions.Use){
                Man man = (Man) host;
                if (dist < 32 || man.wieldingNone()){
                    action = Actions.Punch;
                } else if (!man.weapon.isUsable(false, 1) && man.inventory.getItemSize() == 0){
                    action = Actions.Throw;
                }
            }
            return super.micromanage(host, action, dist);
        }
    };
    public float getDist(Unit host){
        P hostPos = host.getHand();
        P targetPos = host.brain.target.pos;
        int x = (int) ((targetPos.x + host.brain.target.vel.x) - (hostPos.x + host.vel.x));
        int y = (int) (targetPos.y - hostPos.y);
        host.target = new P(x,y-host.brain.target.clip.getHeight());
        if (host instanceof Man) ((Man)host).weapon.rotateTo(host.target.getMult(host.dir,host.dir));
        return (float) Math.abs(Math.sqrt(x * x + y * y) - host.getReach());
    }
    static Actions getMove(Unit host, P target){
        int x = (int) (target.x - (host.pos.x + host.vel.x));
        int y = (int) (target.y - host.pos.y);
        if (Math.abs(x) <= host.getSpeed()*400) {
            if (y > 0) return Actions.Drop;
            else return Actions.Jump;
        }
        if (Math.abs(x) > 100 && host.moveset.contains(Actions.Sprint)) return Actions.Sprint;
        return host.dir == 1 ? Actions.Right : Actions.Left;
    }
    public int micromanage(Unit host, Actions action, float dist){
        host.face(host.brain.target.pos.x>host.pos.x?1:-1, false);
        int fb = host.setState(action);
        if (fb == 2) host.setState(Actions.Rest);
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
            float diff = act.getRange(host) - dist;
            if (diff > 0 && host.getCooldown(act).isReady()) {
                boolean added = false;
                for (int i=0;i<moves.size();i++){
                    if (moves.get(i).getRange(host) - dist > diff){
                        moves.add(i, act);
                        added = true;
                        break;
                    }
                }
                if (!added) moves.add(act);
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