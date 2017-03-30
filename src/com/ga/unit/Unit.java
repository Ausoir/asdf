package com.ga.unit;

import com.ga.game.Settings;
import com.ga.game.Sound;
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
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Ga on 5/7/2016.
 */
public class Unit extends Prop {
    public int[] rangeZone = new int[]{-5};
    public HashMap<Integer, Moveset[]> moveset = new HashMap<>();
    protected HashMap<Moveset, int[]> cooldown = new HashMap<>();
    public int controlId;
    public Body clip;
    public Moveset state = Actions.Idle;
    public boolean jump = false;
    public Intelligence brain;
    public EventTimer delay = new EventTimer(5, false, null);
    public P target = new P(); //For appendage rotation
    public Stats stats = new Stats(this);
    public float damageMod = 1;
    public String speechType;
    protected Speech speech = new Speech(this);
    public int stateLevel = 1;

    public Unit(String p) {
        super(null,Integer.parseInt(p.split("@")[1]),101);
        dynamic = true;
        String[] t = p.split("@");
        name = t[0];
        pos.load(t[2]);
        controlId = Integer.parseInt(t[3]);
        stats.load(t[4]);
        final Stat health = get("Health");
        health.onChange = new Performer(){
            public void perform(){
                if (health.isFull()) stats.bloodTimer.set(-1);
                else stats.bloodTimer.set((int) (health.getPercentage()*health.getPercentage()*100)+1, true);
                if (health.get() <= 0) die();
            }
        };
        faction = Faction.valueOf(t[5]);
        loadMoveset(t[6]);
        gravity = true;
    }

    public void loadMoveset(String p){
        String[] rangeMoves = p.split("/");
        String[] ranges = rangeMoves[0].split(",");
        String[] moveZones = rangeMoves[1].split("&");
        rangeZone = new int[ranges.length];
        for (int i=0;i<ranges.length;i++) rangeZone[i] = Integer.parseInt(ranges[i]);
        for (int i=0;i<moveZones.length;i++) {
            String[] moves = moveZones[i].split(",");
            Moveset[] movesets = new Moveset[moves.length];
            for (int ii=0;ii<moves.length;ii++){
                movesets[ii] = getMoveset(moves[ii]);
            }
            this.moveset.put(i, movesets);
        }
    }

    private Moveset getMoveset(String p){
        Moveset r = Actions.getByName(p);
        if (r == null) r = Spells.getByName(p);
        return r;
    }

    public int performMoveset(int dist){
        int range = -1;
        for (int i=0;i<rangeZone.length;i++){
            if (dist <= rangeZone[i]){
                range = i;
                break;
            }
        }
        if (range != -1) {
            Moveset[] t = moveset.get(range);
            if (t == null) {
                Global.p("Can't find moveset for range " + dist + "@Zone " + range);
            } else {
                Moveset move = t[Global.r.nextInt(t.length)];
//                Global.p("Moveset for range " + dist + "@Zone " + range + " = "+move.getName());
                return setState(move);
            }
        }
        return -1;
    }

    public void reset(){
        Updater.add(speech);
        Global.display.add(speech);
    }

    public P getRandomPoint(){
        return pos.getAdd(clip.randomPoint());
    }

    @Override
    public int getSize(){
        return clip.size;
    }

    public void say(String p) {
        speech.say(p);
        Spells t = Spells.getByName(p);
        if (t != null) setState(t);
    }

    public String getConversation(){
        return speech.getText();
    }

    public void hear(Prop pos, String p){
        if (brain != null) brain.hear(pos, p);
    }
    public P getHand(){
        return pos.getAdd(0,0);
    }
    public float getReach(){
        return 24;
    }
    protected int[] getCooldown(Moveset state) {
        int[] r = cooldown.get(state);
        if (r == null) r = new int[]{0};
        return r;
    }
    public int setState(Moveset state) {
        if (delay.over() && jump && this.state != state) {
            face(target.x > 0 ? 1 : -1, true);
            float req = state.getCost();
            if (req != -1) req += getStatPenalty();
            int[] cdinput = getCooldown(state);
            int prevCd = cdinput[0];
            if (Global.cooldown(cdinput, state.getCooldown())) {
                cooldown.put(state, cdinput);
                if (get("Stamina").require((int) req)) {
                    if (state != Actions.Idle) delay.performer = null;
                    if (state instanceof Spells && !castable()) return 4;
                    stateSet(state);
                    return 0; //fine
                } else {
                    cooldown.put(state, new int[]{prevCd});
                    return 2; //no stamina
                }
            } else return 3; //cooldown
        }
        return 1; //delay
    }
    public float getSpeed(){
        return get("Speed").get()*clip.getLegStatus()*.1f;
    }
    protected boolean castable(){
        return true;
    }
    public void attackSound(){
        Sound.play("swing");
    }
    public int getStateLevel(Moveset state){
        return get("Level").get();
    }
    public void stateSet(Moveset state){
        if (state != Actions.Idle) this.state.onInterrupt(this);
        stats.unrest();
        delay.set(state.getDel());
        this.state = state;
        stateLevel = getStateLevel(state);
        if (state instanceof Actions) {
            vel.x *= ((Actions)state).getVelX();
            clip.setState(state);
            if (brain != null) brain.setState(state);
        } else if (state instanceof Spells){
            vel.x = 0;
            this.state = Actions.Cast;
            clip.setState(((Spells)state).getAction());
            final Unit me = this;
            final Moveset s = state;
            delay.performer = new Performer(){
                public void perform(){
                    Sound.play("spell");
                    me.state = s;
                }
            };
        }
        this.state.onSet(this);
    }
    public boolean face(int dir, boolean force) {
        if (force || (delay.over() && jump && this.dir != dir)) {
            this.dir = dir;
            return true;
        }
        return false;
    }

    @Override
    public int getDamage(){
        return (int) ((get("Proficiency").get())*damageMod);
    }

    protected int getDamageReduction(int organ){
        return 0;
    }

    public int getStatPenalty() {
        return 0;
    }

    public boolean hit(Line hitbox, int damage, DamageType type) {
        delay.performer = null;
        float knockback = (float) damage * 8 / get("Health").getMax();
        if (knockback > 1) knockback *= 2;
        jump = false;
        if (knockback > 20) stateSet(Actions.Down);
        else stateSet(Actions.Hit);
        Sound.play("kick");
        int dir = hitbox.pt.x < pos.x ? 1 : -1;
        if (type != DamageType.Push) {
            stats.addEffect(Buffs.Slow, 2);
            if (brain != null) brain.hit();
            face(dir, true);
            get("Health").add(-damage);
        }
        vel.x = knockback * dir;
        vel.y = -knockback-5;
        return true;
    }

    @Override
    public boolean hittable(Faction faction){
        if (super.hittable(faction) && state != Actions.Down && state != Actions.Hit && state != Actions.Dead && state != Actions.Duck) {
            if (!stats.hasEffect(Buffs.Evade)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkHit(Line hitBox, int damage, DamageType type){
        float r = hitBox.getLength();
        P midpoint = hitBox.getMidPoint();
        if (Math.pow(getSize() + r, 2) - ((Math.pow(midpoint.x - pos.x, 2) + Math.pow(midpoint.y - pos.y, 2))) > 0) {
            hit(hitBox, damage, damageType);
            for (int i = 0; i < clip.organs.length; i++) {
                int dam = damage - getDamageReduction(i);
                if (clip.organs[i].checkHit(hitBox)) {
                    P t = new P(pos.x, hitBox.ptd.y);
                    Particle.explode("Blood", t, t.getSubtract(hitBox.pt), 3, 3, 12, true, 5);
                    clip.organs[i].hit(hitBox, dam, type);
                }
            }
            return true;
        }
        return false;
    }

    public Stat get(String p){
        return stats.getStat(p);
    }

    public void die() {
        Updater.remove(brain);
        state = Actions.Dead;
        clip.setState(Actions.Dead);
        delay.set(-1);
    }

    @Override
    public void collide(){
        jump = false;
        super.collide();
    }

    @Override
    public void onField(){}

    @Override
    public void onCollide(Line l){
        super.onCollide(l);
        if (!jump) {
            jump = true;
            if (l.next >= 0 && state != Actions.Sneak) setDepth(l.depth-1);
            int mag = (int) (vel.y - Settings.gravityMax);
            if (vel.y > Settings.gravity*3) Sound.play("land");
            if (hittable(Faction.Destructive)) {
                if (mag >= 0) {
                    stateSet(Actions.Down);
                    delay.time += getStatPenalty() * 20;
                    get("Health").add(-(getStatPenalty() + 1 + mag));
                } else if (state == Actions.Jump && vel.y > 0) {
                    stateSet(Actions.Hit);
                    delay.set(4);
                }
            }
        }
    }

    protected void stateUpdate(){
        state.update(this, stateLevel);
    }

    protected void stateDone(){
        state.onFinish(this);
        clip.setSpeed(8);
    }

    protected void moveTriggered(int p){
        if (state instanceof Actions) {
            for (Unit i : Updater.collideUnit(pos, (int) getReach(), faction)){
                if (i.checkHit(clip.organs[p].getAdd(pos),getDamage(),damageType)) break;
            }
        }
    }

    public boolean attack(){
        for (Unit i : Updater.collideUnit(pos, (int) getReach(), faction)){
            if (i.checkHit(new Line(pos, pos.getAdd(vel)),getDamage(),damageType)) return true;
        }
        return false;
    }

    @Override
    public void update() {
        speech.update();
        vel.x *= .97f;
        int m = clip.update();
        if (state != Actions.Dead) {
            if (m != -1) moveTriggered(m);
            stats.update();
            delay.update();
            stateUpdate();
        }
        super.update();
    }

    public void setVisible(boolean p){
        for (Organ i : clip.organs) {
            i.image.visible = p;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        for (Organ i : clip.organs) {
            i.draw(g);
        }
    }

    @Override
    public String toString(){
        if (state == Actions.Dead || !save) return "";
        String r = super.toString()+"@"+ controlId;
        r += "@"+stats.toString()+"@"+faction.name()+"@";
        for (int i:rangeZone) r += i+",";
        r = r.substring(0,r.length()-1)+"/";
        Iterator it = moveset.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Moveset[] moves = (Moveset[]) pair.getValue();
            for (Moveset i : moves) r += i.getName() + ",";
            r = r.substring(0,r.length()-1)+(it.hasNext()?"&":"");
        }
        return r;
    }

    public void revive() {
        setDepth(10);
        Particle.holyExplosion(pos);
        stats.addEffect(Buffs.Evade,1);
        stateSet(Actions.Down);
        delay.set(50);
        get("Health").heal();
        reset();
    }
}