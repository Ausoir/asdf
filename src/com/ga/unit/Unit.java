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
    public int id;
    public Body clip;
    public Shadow shadow;
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
    private Moveset prevState = Actions.Idle;

    public Unit(String p) {
        super(p.split("@")[0],Integer.parseInt(p.split("@")[1]),new P(p.split("@")[2]),10);
        String[] t = p.split("@");
        id = Integer.parseInt(t[3]);
        stats.load(t[4]);
        faction = Faction.valueOf(t[5]);
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
        shadow = new Shadow(this);
        gravity = true;
    }

    public void performMoveset(String p){
        Moveset[] t = moveset.get(p);
        if (t == null){
//            Global.p("Can't find moveset "+p);
        } else setState(t[Global.r.nextInt(t.length)]);
    }

    public void reset(){
        state = Actions.Idle;
        Updater.addDrawee(speech);
        Updater.addDrawee(hp);
        Updater.addQueue.add(shadow);
    }

    @Override
    public void remove() {
        super.remove();
        if (shadow != null) shadow.remove();
    }

    public void say(String p) {
        speech.say(p);
    }

    public String getConversation(){
        return speech.getText();
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
        return image.size.x;
    }
    public boolean isExhausted(){
        return false;
    }
    public void setState(Moveset state) {
        setState(state, target.x, target.y);
    }
    public void setState(Moveset state, float x, float y) {
        if (delay.over() && jump && this.state != state) {
            prevState = state;
            Global.server.sendFromLocal("IN@" + id + "@" + state.getCodeName() + "@" + (int)x + "@" + (int)y);
        }
    }
    public void setStateEcho(String p){
        String t = p.substring(1);
        if (p.startsWith("A")) setStateEcho(Actions.getByName(t));
        else if (p.startsWith("K")) setStateEcho(Skills.getByName(t));
        else if (p.startsWith("M")) setStateEcho(Spells.getByName(t));
    }
    public int setStateEcho(Moveset state) {
        if (state != Actions.Idle) delay.performer = null;
        int[] cdinput = getCooldown(state);
        int prevCd = cdinput[0];
        if (Global.cooldown(cdinput, state.getCooldown())) {
            cooldown.put(state, cdinput);
            if (!stateSet(state)){
                cooldown.put(state, new int[]{prevCd});
                return 2;
            }
        } else return 3;
        return 0;
    }
    public void setAnimation(Moveset p){
        clip.setState(p);
    }
    protected boolean stateSet(Moveset state){
        String statreq = state.getStatReq();
        float req = state.getCost();
        req += getStatPenalty();
        if (statreq == null || get(statreq).require((int) req)) {
            stats.unrest();
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
            actionSet(state, 1);
            return true;
        }
        return false;
    }
    protected void actionSet(Moveset state, int level){
        if (state == Actions.Duck){
            vel.x = 0;
            if (Global.map.fallSafe(pos.getAdd(vel))) pos.y += Settings.gravity;
        } else if (state == Skills.Jump){
            vel.y -= 40 - (level * 2);
        }
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

    protected int getStatPenalty() {
        return 0;
    }

    public boolean hit(Matter hitter, int damage, DamageType type) {
        delay.performer = null;
        delay.set(0);
        int x = pos.x-hitter.pos.x > 0 ? 1 : -1;
        face(x < 0 ? -1 : 1);
        float ratio = (float) damage * 8 / get("Health").getMax();
        float knockback = Math.min(ratio, 50);
        jump = false;
        if (type == DamageType.Push) {
            knockback *= 10;
        } else {
            get("Health").add(-damage);
            if (state != Actions.Dead) {
//            stats.addEffect(Buffs.Pain, 1);
                if (brain != null) brain.hit(hitter);
            }
        }
        vel.x = knockback*x;
        vel.y = -knockback * 1.3f - 5;
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
                            if (clip.organs[i].hit(p, dam, damageType)) {
                                clip.getStatus();
                            } else {
                                hit(hitter, dam, damageType);
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
        shadow.remove();
    }

    @Override
    public void collide(){
        jump = false;
        super.collide();
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
        if (state == WalkL){
            vel.x = -get("Speed").get()*clip.getLegStatus()*.1f;
        } else if (state == WalkR){
            vel.x = get("Speed").get()*clip.getLegStatus()*.1f;
        }
        else if (state == Rest) stats.rest(.5f);
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
        if (pos.y >= Global.map.getHeight()){
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
        String r = getClass().getSimpleName()+"]"+super.toString()+"@"+id;
        r += "@"+stats.toString()+"@"+faction.name();
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