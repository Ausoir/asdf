package com.ga.unit.skill;

import com.ga.map.Global;
import com.ga.tool.EventTimer;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.*;
import com.ga.unit.item.Projectile;

/**
 * Created by Ga on 8/1/2016.
 */
public enum Skills implements Moveset {
    Adrenaline(Actions.Cast, 25,"legs",-2,0,10,"Adrenaline doubles movement speed for 5 seconds.",new Skill(50,"speed",-1)),
    Rejuvenate(Actions.Cast, 25,"legs",-2,0,1000,"Rejuvenate recovers strength to full at the cost of stamina.",new Skill(100,"arms",-1)),
    Cleave(Actions.Cast, 25,"arms",-1,0,1000,"Cleave grants the next attack a cleaving property.",new Skill(1000,"health",0)),
    Empower(Actions.Cast, 25,"arms",-1,0,1000,"Empower grants the next attack 10 bonus damage.",new Skill(80,"damage",10)),
    Frenzy(Actions.Cast, 25,"health",-.2f,0,1000,"Frenzy gives you infinite stamina, but at the cost of health and restlessness for 10 seconds.",new Skill(1000,"health",0)),
    Evade(Actions.Cast, 25,"legs",-2,0,1000,"Evade grants you invulnerability for 6 seconds.",new Skill(6500,"health",0)),
    Fireball(Actions.Cast, 40,"legs",-2,0,300,"Shoot a fireball.",new Skill(0,"health",0){
        @Override
        boolean update(){
            final Projectile p = new Projectile("item/Fireball", host.pos.getAdd(host.clip.organs[ClipMan.FIST].ptd), Global.player.mP.getAdd(host.pos), 20+(host.get("arms").get()*2)){
                @Override
                public void stuck(){
                    Particle.explode(pos, P.getZero(), 300, 20, false);
                    Updater.collide(this,10000,15);
                    remove();
                }
                @Override
                public void update(){
                    super.update();
                    Particle.explode(pos, P.getZero(), 30, 10, true);
                }
            };
            p.faction = 1;
            p.damage = 5;
            p.gravity = false;
            p.delay.set(50);
            p.delay.performer = new Performer(){
                @Override
                public void perform(){
                    p.stuck();
                }
            };
            return super.update();
        }
    });
    protected String stat;
    protected float cost, velx;
    protected Actions action;
    protected Man host;
    protected EventTimer timer;
    protected String description;
    public boolean active = false;
    protected int cooldown;
    private int[] lastused = new int[]{-9999};

    Skills(Actions action, int del, String statReq, float cost, float velx, int cooldown, String desc, Performer performer){
        this.action = action;
        timer = new EventTimer(del,false,performer);
        this.stat = statReq;
        this.cost = cost;
        this.velx = velx;
        this.cooldown = cooldown;
        this.description = desc;
    }
    public void init(Man host){
        if (Global.cooldown(lastused,cooldown)) {
            if (this.host != null) end();
            this.host = host;
            host.stats.addEffect(this);
            active = true;
            timer.reset();
            if (timer.performer instanceof Skill) ((Skill) timer.performer).start(host);
        } else {
            host.say(getName()+" is on cooldown!");
        }
    }
    public void end(){
        active = false;
        this.host = null;
    }
    public boolean update(){
        if (active && timer.update()){
            if (host != null) {
                if (!(timer.performer instanceof Skill)) {
                    end();
                    return true;
                } else {
                    if (((Skill) timer.performer).update()) {
                        end();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static Skills getByName(String p){
        for (Skills s : values()) {
            if (s.getName().equals(p)){
                return s;
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return name();
    }

    public String getDescription(){
        return description;
    }

    public Actions getAction(){
        return action;
    }

    @Override
    public String getStatReq() {
        return stat;
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
        return timer.max;
    }
}
