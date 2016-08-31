package com.ga.unit;

/**
 * Created by Ga on 7/28/2016.
 */
public enum Actions implements Moveset {
    Idle(0,null,0,0,null), WalkL, WalkR, Hit(15,null,0,1,null), Dead,
    Jump(2,"legs",-1,1.5f,"Jump over obstacles."), Pierce(25,"arms",-1,1.5f,"A piercing attack that attacks while keeping momentum. Velocity is added to the damage."),
    Swing(25,"arms",-1,0,"Swing your weapon from top to bottom, attacking multiple enemies at once."), Kick(30,"legs",0,1,"A quick, close range attack."),
    Whirlwind(50,"arms",-3,0,"Swing your weapon back and forth. Costly, but quick and effective even to multiple enemies."),
    Stab(40,"arms",-2,0,"Stab your weapon with two hands, dealing 4 times the normal damage."), Weapon(0,null,0,0,"Use the current weapon's basic moveset."),
    Dash(35,"legs",-3,0,"Dash through enemies with a piercing attack."), Rest(0,null,0,0,"Resting regenerates stamina by 1/3 every 3 seconds."),
    Summon, Drop, Enter, Cast, Duck, Consume, Exhausted, Throw, Shoot, Pickup, Down(50,null,0,1.5f,null);
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

    public static Moveset getByName(String p) {
        for (Actions s : values()) {
            if (s.getName().equals(p)){
                return s;
            }
        }
        return null;
    }
}