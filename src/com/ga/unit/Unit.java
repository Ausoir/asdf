package com.ga.unit;

import com.ga.game.Settings;
import com.ga.map.Global;
import com.ga.tool.EventTimer;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.intelligence.Intelligence;
import com.ga.unit.intelligence.Speech;
import com.ga.unit.skill.*;

import java.awt.*;
import java.util.HashMap;

import static com.ga.unit.skill.Actions.WalkL;
import static com.ga.unit.skill.Actions.WalkR;
import static com.ga.unit.skill.Skills.Rest;

/**
 * Created by Ga on 5/7/2016.
 */
public abstract class Unit extends Matter {
    protected HashMap<String, Moveset[]> moveset = new HashMap<String, Moveset[]>(){{
    }};
    public Body clip;
    public Moveset state = Actions.Idle;
    public boolean jump = false;
    public Intelligence brain;
    public EventTimer delay = new EventTimer(5, false, null);
    public P target = new P(); //For appendage rotation
    protected HashMap<Moveset, int[]> cooldown = new HashMap<>();
    public Stats stats = new Stats(this);
    protected HealthBar hp;
    protected float damageMod = 1;
    public String speechType;
    protected Speech speech = new Speech(this);

    public Unit(String name, P pt, int seed) {
        super("unit/", name, seed, pt, 10);
        hp = new HealthBar(get("Health"), pos);
        get("Health").onChange = (new Performer(){
            @Override
            public boolean perform(){
                int health = get("Health").get();
                if (health <= 0) die();
                hp.show(15);
                return true;
            }
        });
    }

    public void performMoveset(String p){
        Moveset[] t = moveset.get(p);
        if (t == null){
//            Global.p("Can't find moveset "+p);
        } else setState(t[Global.r.nextInt(t.length)]);
    }

    public void reset(){
        Updater.addDrawee(hp);
    }

    public void say(String p) {
        speech.say(p);
    }

    public boolean isSpeaking(){
        return speech.isSpeaking();
    }

    public void hear(Matter pos, String p){
        if (brain != null) brain.hear(pos, p);
    }

    protected int[] getCooldown(Moveset state){
        int[] r = cooldown.get(state);
        if (r == null) return new int[]{0};
        return new int[]{r[0]};
    }
    protected void startSkill(Moveset move, Spell p){
        p.start(this, 1);
    }
    public P getHand(){
        return pos;
    }
    public void rotateWeapon(P p){
    }
    public float getReach(){
        return size.x;
    }
    public boolean isExhausted(){
        return false;
    }
    public int setState(Moveset state) {
        if (!delay.over() || !jump) return 1;
        if (this.state != state) {
            delay.performer = null;
            int[] cdinput = getCooldown(state);
            int prevCd = cdinput[0];
            if (Global.cooldown(cdinput, state.getCooldown())) {
                cooldown.put(state, cdinput);
                if (!stateSet(state)){
                    cooldown.put(state, new int[]{prevCd});
                    return 2;
                }
            } else return 3;
        }
        return 0;
    }
    public void setAnimation(Moveset p){
        clip.setState(p);
    }
    protected boolean stateSet(Moveset state){
        delay.set(state.getDel());
        if (state instanceof Actions || state instanceof Skills) {
            if (state instanceof Skills) vel.x *= ((Skills)state).getVelX();
            else {
                if (state == WalkL) face(-1);
                else if (state == WalkR) face(1);
                vel.x = 0;
            }
            this.state = state;
            setAnimation(state);
            if (brain != null) brain.setState(state);
        } else {
            damageMod = 1;
            vel.x = 0;
            this.state = Actions.Idle;
            Spells t = (Spells) state;
            setAnimation(t.getAction());
            Spell k = t.getEvent();
            delay.performer = k;
            startSkill(t, k);
        }
        return true;
    }
    public boolean face(int dir) {
        if (!delay.over() || !jump || this.dir == dir) return false;
        this.dir = dir;
        return true;
    }

    public void face(P p) {
        face(p.x - pos.x > 0 ? 1 : -1);
    }

    @Override
    public int getDamage(){
        return (int) ((get("Proficiency").get())*damageMod);
    }

    protected int getDamageReduction(){
        return 0;
    }

    public boolean hit(Matter hitter, int damage) {
        delay.performer = null;
        delay.set(0);
        int x = pos.x-hitter.pos.x > 0 ? 1 : -1;
        face(x < 0 ? -1 : 1);
        float ratio = (float) damage * 8 / get("Health").getMax();
        float knockback = Math.min(ratio, 50);
        vel.x = knockback*-x;
        vel.y = -knockback * 1.3f - 5;
        jump = false;
        get("Health").add(-damage);
        if (state != Actions.Dead) {
            if (brain != null) brain.hit(hitter);
        }
        return true;
    }

    @Override
    public boolean checkHit(Matter hitter, Line p){
        if (faction != hitter.faction) {
            if (state != Actions.Down && state != Actions.Hit && state != Actions.Dead) {
                if (!stats.hasEffect(Buffs.Evade)) {
                    int dam = hitter.getDamage() - getDamageReduction();
                    for (int i = 0; i < clip.organs.length; i++) {
                        if (clip.organs[i].checkHit(p)) {
                            P t = new P(pos.x, p.ptd.y);
                            Particle.explode("Blood", t, t.getSubtract(hitter.pos), 3, 3, 12, true, 5);
                            if (clip.organs[i].hit(p, dam)) {
                                clip.setHeight();
                                if (clip.organs[i].vital){
                                    setState(Actions.Down);
                                    die();
                                }
                            } else {
                                hit(hitter, dam);
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public Stat get(String p){
        return stats.getStat(p);
    }

    public void die() {
        brain = null;
        jump = true;
        state = Actions.Dead;
        clip.setState(Actions.Dead);
        setDepth(9);
        delay.set(-1);
    }

    @Override
    public void collide(){
        P op = pos;
        P vp = op.getAdd(vel);
        Line pp = new Line(op,vp);
        boolean collided = false;
        for (Line i : Global.map.land){
            if (i.lineCross(pp)){
                if (i.opt != -1) {
                    float slope = i.getSlope();
                    float t = (slope * vel.x * 10000 - 5) / 10000f;
                    if (t < vel.y) {
                        if (Math.abs(vel.x) > 0 && t < 0) {
                            float xforce = (float) Math.cos(Math.atan(t));
                            if (xforce < .07) {
                                vel.set(0, 0);
                            } else vel.x *= xforce;
                        }
                        vel.x *= .9f;
                        onCollide(i);
                        collided = true;
                    } else if (i.opt == 1) {
                        vel.y = 0;
                        onCollide(i);
                    }
                }
            }
        }
        if (!collided) jump = false;
    }

    @Override
    public void onCollide(Line l){
        if (!jump) {
            jump = true;
            if (vel.y > Settings.gravityMax/2) setState(Actions.Hit);
        }
        super.onCollide(l);
    }

    protected void stateUpdate(){
        if (state == WalkL) vel.x = -get("Speed").get()/10;
        else if (state == WalkR) vel.x = get("Speed").get()/10;
        else if (state == Rest) stats.rest(.34f);
    }

    protected void moveTriggered(int p){
        Updater.collide(this, clip.organs[p].getAdd(pos), false);
    }

    @Override
    public void update() {
        clip.update();
        stats.update();
        delay.update();
        stateUpdate();
        super.update();
        if (brain != null) brain.update();
        if (pos.y >= Global.map.map.size.y){
            delay.time = -1;
            setState(Actions.Dead);
        }
    }

    @Override
    public void draw(Graphics2D g) {
        for (Organ i : clip.organs){
            i.draw(g);
        }
    }

    @Override
    public String toString(){
        if (state == Actions.Dead) return "";
        String r = super.toString();
        return r;
    }

    public void revive() {
        Particle.holyExplosion(pos);
        stats.addEffect(Buffs.Evade,1);
        state = Actions.Revive;
        delay.set(50);
        get("Health").heal();
    }
}