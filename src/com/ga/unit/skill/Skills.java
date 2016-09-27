package com.ga.unit.skill;

import com.ga.tool.Word;

/**
 * Created by Ga on 7/28/2016.
 */
public enum Skills implements Moveset {
    Jump(2,"Stamina",-1,3f,5,"Jump over obstacles."), Lunge(0,null,0,0,0,"Lunge at the target, dealing little damage but halting its advance."),
    Pierce(25,"Stamina",-1,2.5f,3,"A piercing attack that attacks while keeping momentum. Velocity is added to the damage."),
    Swing(25,"Stamina",-1,0,3,"Swing your weapon from top to bottom, attacking multiple enemies at once."), Kick(30,"Energy",0,1,3,"A quick, close range attack."),
    Whirlwind(0,"Stamina",-3,0,40,"Swing your weapon back and forth. Costly, but quick and effective even to multiple enemies. Hits multiple times. [Level*3 seconds duration]"),
    Stab(40,"Stamina",-2,0,30,"Stab with your weapon, dealing enormous damage. [Damage:4*Level]"), Weapon(0,null,0,0,0,"Use the current weapon's basic moveset."),
    Dash(35,"Stamina",-1,0,20,"Dash through enemies with a piercing attack. [10*Level Distance] [Damage:.5*Level]"), Rest(0,null,0,0,1,"Resting regenerates stamina and energy by 50% every 3 seconds."),
    Riposte(0,null,0,0,0,"A careful stab from the waist with no stamina cost. Successful stab causes cooldown."),
    Throw(0,null,0,0,0,"Throw your weapon to deal ranged damage. Don't worry about retrieval."),
    Leap(40,"Stamina",-2,3,80,"Perform a leap over great distances.");
    private String description;
    private String statReq;
    private int del, cooldown = 0;
    private float cost, velx;
    Skills(int del, String statReq, float cost, float velx, int cooldown, String desc){
        this.del = del;
        this.statReq = statReq;
        this.cost = cost;
        this.velx = velx;
        this.cooldown = cooldown;
        this.description = desc;
    }
    public String getDescription(){
        String t = ""+((int)cost);
        if (cost < 0){
            if (statReq.equals("Stamina")) t = -cost+"*Weapon Weight";
            else if (statReq.equals("Energy")) t = -cost+"*Equipment weight";
        }
        return description + " [Cost:"+t+" "+ Word.wordify(statReq)+"]";
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

    public float getVelX() {
        return velx;
    }

    @Override
    public int getDel() {
        return del;
    }

    @Override
    public int getCooldown() {
        return cooldown;
    }

    public static Moveset getByName(String p) {
        for (Skills s : values()) {
            if (s.getName().equals(p)){
                return s;
            }
        }
        return null;
    }
}