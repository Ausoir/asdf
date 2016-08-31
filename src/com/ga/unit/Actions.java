package com.ga.unit;

/**
 * Created by Ga on 7/28/2016.
 */
public enum Actions implements Moveset {
    IDLE(0,null,0,0,null), WALKL, WALKR, HIT(15,null,0,1,null), DEAD,
    JUMP(2,"legs",-1,1.5f,"Jump over obstacles."), PIERCE(25,"arms",-1,1.5f,"A piercing attack that attacks while keeping momentum. Velocity is added to the damage."),
    SWING(25,"arms",-1,0,"Swing your weapon from top to bottom, attacking multiple enemies at once."), KICK(30,"legs",0,1,"A quick, close range attack."),
    WHIRLWIND(50,"arms",-3,0,"Swing your weapon back and forth. Costly, but quick and effective even to multiple enemies."),
    STAB(40,"arms",-2,0,"Stab your weapon with two hands, dealing 4 times the normal damage."), WEAPON(0,null,0,0,"Use the current weapon's basic moveset."),
    DASH(35,"legs",-3,0,"Dash through enemies with a piercing attack."), REST(0,null,0,0,"Resting regenerates stamina by 1/3 every 3 seconds."),
    SUMMON, DROP, ENTER, CAST, DUCK, CONSUME, EXHAUSTED, THROW, SHOOT, PICKUP, DOWN(50,null,0,1.5f,null);
    private String description;
    private String statReq;
    private int del;
    private float cost, velx;
    Actions(){
        description = "";
    }
    Actions(int del, String statReq, int cost, float velx, String desc){
        this.del = del;
        this.statReq = statReq;
        this.cost = cost;
        this.velx = velx;
        this.description = desc;
    }
    public String getDescription(){
        return description;
    }
    public String getName(){
        return name();
    }
    @Override
    public String getStatReq() {
        return statReq;
    }

    public float getCost() {
        return cost;
    }

    @Override
    public float getVelX() {
        return velx;
    }

    @Override
    public int getDel() {
        return del;
    }
}