package com.ga.unit;

import com.ga.game.Settings;
import com.ga.map.Global;
import com.ga.tool.EventTimer;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.intelligence.Intelligence;
import com.ga.unit.intelligence.Speech;
import com.ga.unit.skill.*;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.ga.unit.skill.Actions.WalkL;
import static com.ga.unit.skill.Actions.WalkR;
import static com.ga.unit.skill.Skills.Rest;

/**
 * Created by Ga on 5/7/2016.
 */
public abstract class Unit extends Matter {
    public int[] rangeZone = new int[]{-5};
    public HashMap<Integer, Moveset[]> moveset = new HashMap<>();
    public int id;
    public Body clip;
    public Moveset state = Actions.Idle;
    public boolean jump = false;
    private float speed = 0;
    public Intelligence brain;
    public EventTimer delay = new EventTimer(5, false, null);
    public P target = new P(); //For appendage rotation
    protected HashMap<Moveset, int[]> cooldown = new HashMap<>();
    public Stats stats = new Stats(this);
    protected float damageMod = 1;
    public String speechType;
    protected Speech speech = new Speech(this);

    public Unit(String p) {
        super(p.split("@")[0],Integer.parseInt(p.split("@")[1]),new P(p.split("@")[2]),10);
        String[] t = p.split("@");
        id = Integer.parseInt(t[3]);
        stats.load(t[4]);
        faction = Faction.valueOf(t[5]);
        String[] rangeMoves = t[6].split("/");
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
        gravity = true;
    }

    private Moveset getMoveset(String p){
        Moveset r = Actions.getByName(p);
        if (r == null) r = Skills.getByName(p);
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
                return setState(move);
            }
        }
        return -1;
    }

    public void reset(){
        state = Actions.Idle;
        Updater.add(speech);
    }

    public void say(String p) {
        speech.say(p);
        Spells t = Spells.getByName(p);
        if (t != null) setState(t);
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
        return clip.size.x;
    }
    public boolean isExhausted(){
        return false;
    }
    public int setState(Moveset state) {
        if ((delay.over() && jump && this.state != state) || state == Actions.Hit) {
            face(target.x > 0 ? 1 : -1);
            if (state != Actions.Idle) delay.performer = null;
            int[] cdinput = getCooldown(state);
            int prevCd = cdinput[0];
            if (Global.cooldown(cdinput, state.getCooldown())) {
                cooldown.put(state, cdinput);
                if (!stateSet(state)) {
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
    protected float getSpeed(){
        return get("Speed").get()*clip.getLegStatus()*.1f;
    }
    protected boolean stateSet(Moveset state){
        float req = state.getCost();
        if (req != -1) req += getStatPenalty();
        if (get("Stamina").require((int) req)) {
            stats.unrest();
            delay.set(state.getDel());
            if (state instanceof Actions || state instanceof Skills) {
                if (state instanceof Skills) vel.x *= ((Skills)state).getVelX();
                else {
                    if (state == WalkL){
                        face(-1);
                        speed = -getSpeed();
                    } else if (state == WalkR){
                        face(1);
                        speed = getSpeed();
                    }
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
        if (state == Actions.Down){
            vel.x = 0;
            if (Global.map.fallSafe(pos.getAdd(vel))) pos.y += Settings.gravity;
        } else if (state == Skills.Jump || state == Skills.Airstrike){
            vel.y -= 40 + (level * 2);
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

    protected int getDamageReduction(int organ){
        return 0;
    }

    public int getStatPenalty() {
        return 0;
    }

    public boolean hit(Matter hitter, int damage, DamageType type) {
        delay.performer = null;
        delay.set(0);
        float ratio = (float) damage * 8 / get("Health").getMax();
        float knockback = Math.min(ratio, 30);
        jump = false;
        if (type != DamageType.Push) {
            get("Health").add(-damage);
            if (state != Actions.Dead) {
                stats.addEffect(Buffs.Slow, 2);
                if (brain != null) brain.hit(hitter);
            }
        }
        if (hitter != null) {
            int x = pos.x - hitter.pos.x > 0 ? 1 : -1;
            vel.x = knockback * x;
        }
        vel.y = -knockback * 1.3f - 5;
        return true;
    }

    @Override
    public boolean hittable(){
        if (state != Actions.Down && state != Actions.Hit && state != Actions.Dead && state != Skills.Duck) {
            if (!stats.hasEffect(Buffs.Evade)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkHit(Matter hitter, Line p){
        if (faction.canAttack(hitter.faction)) {
            if (hittable()) {
                for (int i = 0; i < clip.organs.length; i++) {
                    int dam = hitter.getDamage() - getDamageReduction(i);
                    if (clip.organs[i].checkHit(p)) {
                        P t = new P(pos.x, p.ptd.y);
                        Particle.explode("Blood", t, t.getSubtract(hitter.pos), 3, 3, 12, true, 5);
                        if (clip.organs[i].hit(p, dam, hitter.damageType)) { // amputated
                            clip.updateStatus();
                        } else {
                            hit(hitter, dam, hitter.damageType);
                        }
                        return true;
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
        setDepth(8);
        delay.set(-1);
    }

    @Override
    public void collide(){
        jump = false;
        super.collide();
    }

    @Override
    public void onCollide(Line l){
        super.onCollide(l);
        if (!jump) {
            jump = true;
            int mag = (int) (vel.y - Settings.gravityMax);
            if (mag >= 0){
                setState(Actions.Hit);
                delay.time += getStatPenalty()*30;
                get("Health").add(-(getStatPenalty()+1+mag));
            }
        }
    }

    protected void stateUpdate(){
        if (delay.over() && state == WalkL || state == WalkR){
            vel.x = speed;
        } else if (state == Rest){
            if (stats.rest(5)) setState(Actions.Idle);
        }
    }

    protected void stateDone(){
        setState(Actions.Idle);
        clip.setSpeed(8);
    }

    protected void moveTriggered(int p){
        if (state instanceof Skills) {
            Updater.collide(this, clip.organs[p].getAdd(pos), false);
        }
    }

    @Override
    public void update() {
        if (jump) vel.x *= .9f;
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
        clip.update();
        for (Organ i : clip.organs){
            i.draw(g);
        }
    }

    @Override
    public String toString(){
        if (state == Actions.Dead) return "";
        String r = getClass().getSimpleName()+"]"+super.toString()+"@"+id;
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
        Particle.holyExplosion(pos);
        stats.addEffect(Buffs.Evade,1);
        state = Actions.Revive;
        clip.setState(Actions.Revive);
        delay.set(50);
        get("Health").heal();
        reset();
    }
}