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
public class Unit extends Matter {
    public int[] rangeZone = new int[]{-5};
    public HashMap<Integer, Moveset[]> moveset = new HashMap<>();
    public int id;
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
        super(null,Integer.parseInt(p.split("@")[1]),new P(p.split("@")[2]),10);
        name = p.split("@")[0];
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
//                Global.p("Moveset for range " + dist + "@Zone " + range + " = "+move.getName());
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
    public P getHand(){
        return pos.getAdd(0,0);
    }
    public float getReach(){
        return image.sizeRadius+4;
    }
    public boolean isExhausted(){
        return false;
    }
    public int setState(Moveset state) {
        if (delay.over() && jump && this.state != state) {
            face(target.x > 0 ? 1 : -1);
            float req = state.getCost();
            if (req != -1) req += getStatPenalty();
            if (get("Stamina").require((int) req)) {
                if (state != Actions.Idle) delay.performer = null;
                if (state instanceof Spells && !castable()) return 4;
                stateSet(state);
                return 0; //fine
            } else return 2; //no stamina
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
    public void stateSet(Moveset state){
        stats.unrest();
        delay.set(state.getDel());
        this.state = state;
        stateLevel = get("Level").get();
        if (state instanceof Actions || state instanceof Skills) {
            if (state instanceof Skills){
                vel.x *= ((Skills)state).getVelX();
                if (state == Skills.Jump || state == Skills.Airstrike){
                    Sound.play("jump");
                    vel.y -= 40 + (stateLevel * 2);
                }
            } else {
                vel.x = 0;
            }
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
        float ratio = (float) damage * 8 / get("Health").getMax();
        float knockback = Math.min(ratio, 30);
        jump = false;
        if (ratio > 50) stateSet(Actions.Down);
        else stateSet(Actions.Hit);
        Sound.play("kick");
        if (type != DamageType.Push) {
            stats.addEffect(Buffs.Slow, 2);
            if (brain != null) brain.hit(hitter);
            get("Health").add(-damage);
        }
        if (hitter != null) vel.x = knockback * (pos.x - hitter.pos.x > 0 ? 1 : -1);
        else vel.x = knockback * dir;
        vel.y = -knockback * 1.2f - 5;
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
                        hit(hitter, dam, hitter.damageType);
                        clip.organs[i].hit(p, dam, hitter.damageType);
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
        Updater.remove(brain);
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
            if (vel.y > Settings.gravity*3) Sound.play("land");
            if (mag >= 0){
                stateSet(Actions.Down);
                delay.time += getStatPenalty()*30;
                get("Health").add(-(getStatPenalty()+1+mag));
            }
        }
    }

    protected void stateUpdate(){
        state.update(this, stateLevel);
    }

    protected void stateDone(){
        state.done(this);
        clip.setSpeed(8);
    }

    protected void moveTriggered(int p){
        if (state instanceof Skills) {
            Updater.collide(this, clip.organs[p].getAdd(pos), false);
        }
    }

    public void attack(){
        Updater.collide(this, (int)getReach());
    }

    @Override
    public void update() {
        vel.x *= .96f;
        if (Math.abs(vel.x) <= 1) vel.x = 0;
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
        setDepth(10);
        Particle.holyExplosion(pos);
        stats.addEffect(Buffs.Evade,1);
        state = Actions.Revive;
        clip.setState(Actions.Revive);
        delay.set(50);
        get("Health").heal();
        reset();
    }
}