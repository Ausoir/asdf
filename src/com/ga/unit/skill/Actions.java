package com.ga.unit.skill;

/**
 * Created by Ga on 7/28/2016.
 */
public enum Actions implements Moveset {
    Idle, LookUp, LookLeft, LookDown, LookRight, WalkL, WalkR, Hit(15,null), Dead(99999,null), Pull1, Pull2, Pull3,
    Conjure, Wait(50,null), Cast, Consume, Exhausted, Interact(8,null), Down(50,null), Buff, Revive, Sheathe(18,null);
    private String description;
    private int del;
    Actions(){
        description = "";
    }
    Actions(int del, String desc){
        this.del = del;
        this.description = desc;
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

    @Override
    public String getStatReq() {
        return null;
    }

    public float getCost() {
        return 0;
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
}