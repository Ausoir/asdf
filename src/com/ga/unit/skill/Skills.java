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
    Adrenaline(Actions.Duck, 25,"legs",-2,0,10,"Adrenaline doubles movement speed for 5 seconds.",produceBuff(Buff.Adrenaline)),
    Rejuvenate(Actions.Duck, 25,"legs",-2,0,1000,"Rejuvenate recovers strength for a short while at the cost of stamina.",produceBuff(Buff.Rejuvenate)),
    Cleave(Actions.Duck, 25,"arms",-1,0,1000,"Cleave grants the user a cleaving property.",produceBuff(Buff.Cleave)),
    Empower(Actions.Duck, 25,"arms",-1,0,1000,"Empower grants the next attack 10 bonus damage.",produceBuff(Buff.Empower)),
    Frenzy(Actions.Duck, 25,"health",10,0,1000,"Frenzy grants the user infinite stamina at the cost of health and restlessness for 10 seconds.",produceBuff(Buff.Rejuvenate)),
    Evade(Actions.Duck, 25,"legs",-2,0,1000,"Evade grants you invulnerability for 6 seconds.",produceBuff(Buff.Evade)),
    Fireball(Actions.Cast, 40,"legs",-2,0,300,"Shoot a fireball.",new Skill(){
        @Override
        public void perform(){
            final Projectile p = new Projectile("item/Fireball", host.pos.getAdd(host.clip.organs[ClipMan.FIST].ptd), Global.player.mP.getAdd(host.pos), 20+(host.get("arms").get()*2)){
                @Override
                protected void onAttack(Unit p){
                    if (!host.stats.hasEffect(Buff.Cleave)) super.onAttack(p);
                }
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
        }
    });
    private static Skill produceBuff(final Buff p){
        return new Skill(){
            void start(Man host){
                host.stats.addEffect(p);
            }
        };
    }
    protected String stat;
    protected float cost, velx;
    protected Actions action;
    protected Man host;
    protected EventTimer timer;
    protected String description;
    protected int cooldown;

    Skills(Actions action, int del, String statReq, float cost, float velx, int cooldown, String desc, Performer performer){
        this.action = action;
        timer = new EventTimer(del,false,performer);
        this.stat = statReq;
        this.cost = cost;
        this.velx = velx;
        this.cooldown = cooldown;
        this.description = desc;
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

    public Performer getEvent(Man host){
        ((Skill) timer.performer).start(host);
        return timer.performer;
    }
}
