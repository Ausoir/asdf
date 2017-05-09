package com.ga.unit;

import com.ga.game.Settings;
import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.Updater;
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
 * Created by Ga on 5/7/2016. asd
 */
public class Unit extends Prop {
    private int[] rangeZone = new int[]{-5};
    private HashMap<Integer, Actions[]> moveset = new HashMap<>();
    protected HashMap<Actions, int[]> cooldown = new HashMap<>();
    public int controlId;
    public Body clip;
    public Actions state = Actions.Idle;
    public boolean jump = false;
    public Intelligence brain;
    public EventTimer delay = new EventTimer(5, false, null);
    public P target = new P(); //For appendage rotation
    public Stats stats = new Stats(this);
    public float damageMod = 1;
    public String speechType;
    protected Speech speech = new Speech(this);
    public int stateLevel = 1;
    public int attackSpeed = 0;
    private Actions lastState = Actions.Idle;
    private int jumpAcross = 0;

    public Unit(int seed) {
        super(null,seed,101);
        controlId = Global.getControlId();
        dynamic = true;
        gravity = true;
        tags.add(Tag.Swinging);
        tags.add(Tag.Athletics);
        tags.add(Tag.Jumping);
        tags.add(Tag.Combustible);
        tags.add(Tag.Freezable);
    }

    @Override
    public String[] load(String p){
        String[] sections = super.load(p);
        stats.load(sections[4]);
        faction = Faction.valueOf(sections[5]);
        loadActions(sections[6]);
        clip.setOrganHealth();
        return sections;
    }

    protected void loadActions(String p){
        String[] rangeMoves = p.split("/");
        String[] ranges = rangeMoves[0].split(",");
        String[] moveZones = rangeMoves[1].split("&");
        rangeZone = new int[ranges.length];
        for (int i=0;i<ranges.length;i++) rangeZone[i] = Integer.parseInt(ranges[i]);
        for (int i=0;i<moveZones.length;i++) {
            String[] moves = moveZones[i].split(",");
            Actions[] movesets = new Actions[moves.length];
            for (int ii=0;ii<moves.length;ii++){
                movesets[ii] = Actions.getByName(moves[ii]);
            }
            this.moveset.put(i, movesets);
        }
    }

    public int performActions(int dist){
        int range = -1;
        for (int i=0;i<rangeZone.length;i++){
            if (dist <= rangeZone[i]){
                range = i;
                break;
            }
        }
        if (range != -1) {
            Actions[] t = moveset.get(range);
            if (t == null) {
                Global.p("Can't find moveset for range " + dist + "@Zone " + range);
            } else {
                Actions move = t[Global.r.nextInt(t.length)];
//                Global.p("Actions for range " + dist + "@Zone " + range + " = "+move.getName());
                return setState(move);
            }
        }
        return -1;
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
    protected int[] getCooldown(Actions state) {
        int[] r = cooldown.get(state);
        if (r == null) r = new int[]{Global.getTime()[0]};
        return r;
    }
    protected boolean tagCapable(Tags p){
        return true;
    }
    public int setState(Actions state) {
        if (delay.over() && jump && this.state != state && tagCapable(state.getTags())) {
            face(target.x > 0 ? 1 : -1, true);
            int req = state.getCost();
            if (req != -1) req += getStatPenalty();
            else req = 0;
            if (Global.getTime()[0] >= getCooldown(state)[0]) {
                if (get("Stamina").require(req)) {
                    if (state != Actions.Idle) delay.performer = null;
                    stateLevel = getStateLevel(state);
                    if (stateLevel >= 1){
                        cooldown.put(state, new int[]{Global.getTime()[0]+state.getCooldown()});
                        stateSet(state);
                        return 0; //fine
                    } else return 5; //not enough power
                } else {
                    return 2; //no stamina
                }
            } else return 3; //cooldown
        }
        return 1; //delay
    }
    public float getSpeed(){
        return get("Speed").get()*clip.getLegStatus()*.01f;
    }
    public void attackSound(){
        Sound.play("swing");
    }
    public int getStateLevel(Actions state){
        return get("Level").get();
    }
    public void stateSet(Actions state){
        if (state != Actions.Idle) this.state.onInterrupt(this);
        delay.set(state.getDel());
        this.state = state;
        clip.setState(state.getClipName());
        if (brain != null) brain.setState(state);
        this.state.onSet(this);
        int asmod = get("Attack").get();
        if (delay.max != 0) delay.set(Math.max(3, delay.time - attackSpeed - asmod));
        clip.setSpeed(attackSpeed+asmod);
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
        return (int) ((get("Level").get())*damageMod);
    }

    protected int getDamageReduction(int organ){
        return 0;
    }

    public int getStatPenalty() {
        return 0;
    }

    public void buff(Buffs buff, int level){
        stats.addEffect(buff, level);
    }

    public void hit(int dir, float knockback) {
        delay.performer = null;
        jump = false;
        buff(Buffs.Pain, 1);
        if (stats.triggerEffect(Buffs.Mark)) knockback += .5f;
        if (state == Actions.Rest) knockback += .5f;
        vel.x = knockback * 8 * dir;
        vel.y = -knockback - 5;
        if (state == Actions.Down) die();
        else if (knockback > 0){
            if (knockback > .4f && rand.nextBoolean()){
                stateSet(Actions.Down);
                if (knockback > rand.nextFloat()*2) die();
            } else stateSet(Actions.Hit);
            if (rand.nextBoolean()) buff(Buffs.Bleed,1);
            if (rand.nextBoolean()) buff(Buffs.Pain,1);
        }
    }

    @Override
    public boolean hittable(Faction faction){
        return super.hittable(faction) && state != Actions.Dead && state != Actions.Duck;
    }

    public boolean checkHit(Line hitBox, int damage){
        boolean r = false;
        if (stats.triggerEffect(Buffs.Invincible) || stats.triggerEffect(Buffs.Evade)) return false;
        for (int i = 0; i < clip.organs.length; i++) {
            int dam = Math.max(1, damage - getDamageReduction(i));
            if (clip.organs[i].checkHit(hitBox)) {
                if (!r){
                    Stat stamina = get("Stamina");
                    int tstamina = stamina.get();
                    stamina.add(-damage);
                    damage -= tstamina;
                    if (damage > 0) {
                        hit(hitBox.pt.x < pos.x ? 1 : -1, (float) (damage - getDamageReduction(3))/stamina.getMax());
                    }
                }
                P t = new P(pos.x, hitBox.ptd.y);
                Particle.explode("Blood", t, t.getSubtract(hitBox.pt), 3, 3, 12, true, 5);
                clip.organs[i].hit(hitBox, dam);
                r = true;
            }
        }
        return r;
    }

    public Stat get(String p){
        return stats.getStat(p);
    }

    public void die() {
        Updater.remove(brain);
        stateSet(Actions.Dead);
        Global.player.witnessDeath(this);
    }

    @Override
    public void collide(){
        jump = false;
        super.collide();
        if (!jump) {
            jumpAcross++;
            if (jumpAcross > 5) lastLand = new Line();
        } else jumpAcross = 0;
    }

    @Override
    public void onField(){}

    @Override
    public void onCollide(Line l){
        super.onCollide(l);
        if (!jump) {
            jump = true;
            if (state != Actions.Sneak) setDepth(l.depth-1);
            int mag = (int) (vel.y - Settings.gravityMax);
            if (vel.y > Settings.gravity*3) Sound.play("land");
            if (hittable(Faction.Destructive)) {
                if (mag >= 0) {
                    stateSet(Actions.Down);
                    int statpenalty = getStatPenalty();
                    delay.time += statpenalty * 20;
                } else if (state == Actions.Jump && vel.y > 0) {
                    stateSet(Actions.Hit);
                    delay.set(4);
                }
            }
        }
    }

    protected void stateUpdate(){
        state.update(this);
    }

    protected void stateDone(){
        state.onFinish(this);
    }

    protected void moveTriggered(int p){
        if (lastState != state) {
            state.trigger(this);
            lastState = state;
        }
    }

    public boolean attack(){
        for (Unit i : Updater.collideUnit(pos, (int) getReach(), faction)){
            P hand = getHand();
            if (i.checkHit(new Line(hand, hand.getAdd(vel)),getDamage())){
                onAttack(i);
                return true;
            }
        }
        return false;
    }

    public void onAttack(Unit p){
    }

    @Override
    public void update() {
        speech.update();
        int m = clip.update();
        if (state != Actions.Dead) {
            if (m != -1) moveTriggered(m);
            stats.update();
            if (delay.update() && delay.performer == null) stateDone();
            stateUpdate();
            stats.rest(1);
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
        String r = super.toString();
        r += "@"+stats.toString()+"@"+faction.name()+"@";
        for (int i:rangeZone) r += i+",";
        r = r.substring(0,r.length()-1)+"/";
        Iterator it = moveset.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Actions[] moves = (Actions[]) pair.getValue();
            for (Actions i : moves) r += i.getName() + ",";
            r = r.substring(0,r.length()-1)+(it.hasNext()?"&":"");
        }
        return r;
    }

    public String getDescription(){
        String r = "*Faction - "+faction.name()+"\n";
        r += "*Stats - \n"+stats.toString().split("/")[0].replace("&"," ")+"\n";
        r += "*Effects - "+stats.getEffectsList();
        return r;
    }

    public void revive() {
        Particle.holyExplosion(pos);
        buff(Buffs.Invincible,1);
        stateSet(Actions.Down);
        delay.set(50);
        clip.setOrganHealth();
        for (Organ i : clip.organs){
            i.amputated = false;
        }
    }
}