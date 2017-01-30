package com.ga.unit.skill;

import com.ga.unit.Player;
import com.ga.unit.Unit;

/**
 * Created by Ga on 7/28/2016.
 */
public enum Actions implements Moveset {
    Idle{
        public void update(Unit p, int level){
            if (p.stats.rest(1)) p.stateSet(Actions.Idle);
        }
    }, Walk{
        public void update(Unit p, int level){
            p.vel.x = p.getSpeed()*p.dir;
        }
    }, Climb{
        public void update(Unit p, int level){
            Player player = (Player)p;
            if (player.clip.organs[0].ptd.y + p.pos.y > player.rope.pt.y) p.vel.y = -p.getSpeed();
            else{
                p.jump = true;
                p.vel.x = p.getSpeed()*p.dir;
                p.stateSet(Skills.Jump);
            }
        }
    }, Use, LookUp, LookLeft, LookDown, LookRight, WalkL, Hit(15), Dead(-1), Wield(18), Recall,
    Conjure, Wait(50), Cast, Consume(8), Exhausted, Interact(8), Down(80), Buff, Revive;
    private String description;
    private int del;
    Actions(){
        description = "";
    }
    Actions(int del){
        this();
        this.del = del;
    }
    public String getDescription(){
        return description;
    }
    public String getName(){
        return name();
    }
    public String getCodeName(){
        return "A"+name();
    }
    public boolean isLooker(){
        return this == LookUp || this == LookLeft || this == LookDown || this == LookRight;
    }

    public float getCost() {
        return -1;
    }

    @Override
    public int getDel() {
        return del;
    }

    @Override
    public int getCooldown() {
        return 0;
    }

    public static Moveset getByName(String p) {
        for (Actions s : values()) {
            if (s.getName().equals(p)){
                return s;
            }
        }
        return null;
    }

    @Override
    public void update(Unit p, int level){

    }

    @Override
    public void done(Unit p){
        p.setState(Idle);
    }
}