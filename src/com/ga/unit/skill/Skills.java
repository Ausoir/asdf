package com.ga.unit.skill;

import com.ga.unit.Unit;

/**
 * Created by Ga on 7/28/2016.
 */
public enum Skills implements Moveset {
    Sprint(15,1,0,36,"Run faster using stamina.",new Spell(){
        public void perform(Unit host, int lvl){
            host.vel.x = host.getSpeed() * 2 * host.dir;
        }
    }),
    Duck(18,-1,0,6,"Duck down to dodge attacks.",null),
    Guard(23,-1,0,0,"Deflect an oncoming blow.",null),
    Lunge(0,-1,0,0,"Lunge at the target, dealing little damage but halting its advance.",null),
    Pierce(25,1,2.5f,0,"A piercing attack that attacks while keeping momentum. Velocity is added to the damage.",null),
    Swing(25,1,0,0,"Swing your weapon from top to bottom.",null),
    Kick(3,-1,1,1,"A quick close range attack to push away enemies at no stamina cost.",null),
    Sidekick(18,-1,1,1,"A slow and damaging close range attack for when you run out of stamina.",null),
    Whirlwind(0,5,0,60,"Swing your weapon back and forth. Costly, but quick and effective even to multiple enemies. Hits multiple times. [Level*3 seconds duration]",null),
    Stab(40,2,0,24,"Stab with your weapon, dealing enormous damage. [Damage:(2+Level)*100%]",null),
    Use(0,-1,1,0,"Use the current weapon's basic moveset.",null),
    Dash(35,1,0,24,"Dash through enemies with a piercing attack. [10*Level Distance] [Damage:.5*Level]",null),
    Rest(0,-1,0,0,"Resting restores stamina.",null),
    Underhand(32,0,0,0,"A careful lunge from the waist. Successful stab refunds stamina.",null),
    Throw(12,-1,0,1,"Throw your weapon to deal damage from afar.",null),
    Dart(12,0,0,1,"Dart your weapon to deal damage from afar.",null),
    Shoot(27,-1,0,0,"Fire your weapon.",null),
    Snipe(27,-1,.5f,0,"Fire your weapon.",null),
    Slash(60,2,0,40,"Slash forward twice, dealing massive damage. [Damage: (1+Level)*100%]",null),
    Jump(47,1,4f,0,"Jump into the air.",null),
    Airstrike(27,1,4f,4,"Jump into the air while attacking.",null),
    Leap(40,2,3,60,"Perform a leap over great distances.",null),
    Backstep(20,0,-1f,7,"Take a step backwards.",null);
    private String description;
    private int del, cooldown = 0;
    private float cost, velx;
    private Spell update;
    Skills(int del, float cost, float velx, int cooldown, String desc, Spell update){
        this.del = del;
        this.cost = cost;
        this.velx = velx;
        this.cooldown = cooldown;
        this.description = desc;
        this.update = update;
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

    @Override
    public void update(Unit p, int level){
        if (update != null) update.perform(p, level);
    }

    @Override
    public void done(Unit p){
        p.setState(Actions.Idle);
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