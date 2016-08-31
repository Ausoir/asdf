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
    Adrenaline(25,"legs",-2,0,"Adrenaline doubles movement speed for 5 seconds.",new Skill(50,"speed",-1)),
    Rejuvenate(25,"legs",-2,0,"Rejuvenate recovers strength to full at the cost of stamina.",new Skill(100,"arms",-1)),
    Cleave(25,"arms",-1,0,"Cleave grants the next attack a cleaving property.",new Skill(1000,"health",0)),
    Empower(25,"arms",-1,0,"Empower grants the next attack 10 bonus damage.",new Skill(80,"damage",10)),
    Frenzy(25,"health",-.2f,0,"Frenzy gives you infinite stamina, but at the cost of health and restlessness for 10 seconds.",new Skill(1000,"health",0)),
    Evade(25,"legs",-2,0,"Evade makes you invulnerable for 6 seconds.",new Skill(65,"health",0)),
    Fireball(20,"legs",-2,0,"Shoot a fireball.",new Skill(0,"health",0){
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
    protected String stat = "health";
    protected float cost, velx = 0;
    protected Actions action = Actions.CAST;
    protected Man host;
    protected EventTimer timer;
    protected String description;
    public boolean active = false;
    private int[] lastused = new int[]{-9999};

    Skills(int del, String statReq, float cost, float velx, String desc, Performer performer){
        timer = new EventTimer(del,false,performer);
        this.stat = statReq;
        this.cost = cost;
        this.velx = velx;
        this.description = desc;
    }
    public void init(Man host){
        if (this.host != null) end();
        this.host = host;
        host.stats.addEffect(this);
        active = true;
        timer.reset();
        if (timer.performer instanceof Skill) ((Skill) timer.performer).start(host);
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
