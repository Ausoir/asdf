package com.ga.unit.skill;

/**
 * Created by Ga on 7/28/2016.
 */
public enum Skills implements Moveset {
    Duck(18,-1,0,3,"Duck down to dodge attacks."),
    Guard(23,-1,0,0,"Deflect an oncoming blow."),
    Lunge(0,-1,0,0,"Lunge at the target, dealing little damage but halting its advance."),
    Pierce(25,1,2.5f,3,"A piercing attack that attacks while keeping momentum. Velocity is added to the damage."),
    Swing(25,1,0,3,"Swing your weapon from top to bottom, attacking multiple enemies at once."),
    Kick(3,-1,1,1,"A quick close range attack to push away enemies to use when you run out of stamina."),
    Sidekick(18,-1,1,1,"A slow and damaging close range attack for when you run out of stamina."),
    Whirlwind(0,5,0,40,"Swing your weapon back and forth. Costly, but quick and effective even to multiple enemies. Hits multiple times. [Level*3 seconds duration]"),
    Stab(40,2,0,15,"Stab with your weapon, dealing enormous damage. [Damage:(2+Level)*100%]"),
    Use(0,-1,1,0,"Use the current weapon's basic moveset."),
    Dash(35,1,0,20,"Dash through enemies with a piercing attack. [10*Level Distance] [Damage:.5*Level]"),
    Rest(0,-1,0,1,"Resting restores stamina."),
    Underhand(32,0,0,0,"A careful lunge from the waist. Successful stab refunds stamina."),
    Throw(12,-1,0,1,"Throw your weapon to deal damage from afar."),
    Dart(12,0,0,1,"Dart your weapon to deal damage from afar."),
    Shoot(27,-1,0,0,"Fire your weapon."),
    Snipe(27,-1,.5f,0,"Fire your weapon."),
    Slash(60,2,0,30,"Slash forward twice, dealing massive damage. [Damage: (1+Level)*100%]"),
    Jump(47,1,4f,0,"Jump into the air."),
    Airstrike(27,1,4f,0,"Jump into the air while attacking."),
    Leap(40,2,3,80,"Perform a leap over great distances."),
    Backstep(20,0,-1f,14,"Take a step backwards.");
    private String description;
    private int del, cooldown = 0;
    private float cost, velx;
    Skills(int del, float cost, float velx, int cooldown, String desc){
        this.del = del;
        this.cost = cost;
        this.velx = velx;
        this.cooldown = cooldown;
        this.description = desc;
    }
    public String getDescription(){
        String t = ""+((int)cost);
        return description + " [Cost:"+t+" "+"]";
    }
    public String getName(){
        return name();
    }
    public String getCodeName(){
        return "K"+name();
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