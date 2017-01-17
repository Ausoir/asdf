package com.ga.unit.skill;

/**
 * Created by Ga on 7/28/2016.
 */
public enum Actions implements Moveset {
    Idle, LookUp, LookLeft, LookDown, LookRight, WalkL, WalkR, Hit(45,null), Dead(99999,null), Wield(18, null), Recall,
    Conjure, Wait(50,null), Cast, Consume, Exhausted, Interact(8,null), Down(50,null), Buff, Revive;
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
}