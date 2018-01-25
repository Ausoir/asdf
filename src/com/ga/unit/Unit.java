package com.ga.unit;

import com.ga.game.Settings;
import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.tool.EventTimer;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Buffs;
import com.ga.unit.skill.Stat;
import com.ga.unit.skill.Stats;

import java.awt.*;
import java.util.HashMap;

/**
 * Created by Ga on 5/7/2016. asd
 */
public class Unit extends Prop {
    private static final float OFFSET_UNIT = .0001f;
    public Moveset moveset = new Moveset();
    HashMap<Actions, int[]> cooldown = new HashMap<>();
    public int controlId;
    public Body clip;
    public Actions state = Actions.Idle;
    public boolean jump = false;
    public AI brain;
    public EventTimer delay = new EventTimer(5, false, null);
    public P target = new P();
    public Stats stats = new Stats(this);
    public Traits traits = new Traits();
    public float damageMod = 1;
    protected String speechType;
    protected Speech speech;
    public int stateLevel = 1;
    public int attackSpeed = 0;
    private Actions lastState = Actions.Idle;
    private int jumpAcross = 0;
    public int stateDuration = 0;
    private boolean moveTriggered = false;

    public Unit(Map map, String name, int seed) {
        super(map, null, seed, 8);
        this.name = name;
        gravity = true;
        init();
    }
    protected void init(){}
    @Override
    public Unit start(){
        speech = new Speech(this);
        super.start();
        return this;
    }
    @Override
    public String[] load(String p){
        String[] sections = super.load(p);
        controlId = Integer.parseInt(sections[5]);
        map = Global.getMap(sections[6]);
        clip.load(sections[7]);
        faction = Faction.valueOf(sections[9]);
        Intelligence.get(sections[10], this);
        stats.load(sections[8]);
        moveset.load(this,sections[11]);
        clip.setOrganHealth();
        traits.load(this,sections[12]);
        return sections;
    }
    @Override
    public String toString(){
        return super.toString() + "@" + controlId + "@" + map.name + "@" + clip.toString() + "@" + stats.toString() + "@" + faction.name() +
                "@" + brain.toString() + "@" + moveset.toString() + "@" + traits.toString();
    }
    public boolean learnSkill(Actions move){
        if (moveset.size()-1 < get("Intelligence").get()){
            moveset.add(move);
            return true;
        }
        return false;
    }
    public boolean learnTrait(Traits.Trait p) {
        if (traits.isLearnable(p) && traits.traits.size() < get("Wisdom").get()) {
            traits.traits.add(p);
            return true;
        }
        return false;
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
        return speech.recent;
    }
    void hear(Prop pos, String p){
        if (!speech.getTextarea().isVisible()) brain.hear(pos, p);
    }
    public P getHand(){
        int hand = clip.getHand();
        if (hand == -1) return pos.getAdd(0,-clip.getHeight());
        return pos.getAdd(clip.organs[hand].ptd);
    }
    public float getReach(){
        return getSize();
    }
    protected int[] getCooldown(Actions state) {
        int[] r = cooldown.get(state);
        if (r == null) r = new int[]{Global.time};
        return r;
    }
    public int setState(Actions state) {
        if (delay.over() && jump && this.state != state) {
            lastState = null;
            face(target.x > 0 ? 1 : -1, true);
            if (Global.time >= getCooldown(state)[0]) {
                if (get("Stamina").require(state.getCost())) {
                    if (state != Actions.Idle) delay.performer = null;
                    stateLevel = getStateLevel(state);
                    if (stateLevel > 0){
                        cooldown.put(state, new int[]{Global.time+state.getCooldown()});
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
        Sound.play(map, pos, "swing_normal");
    }
    protected Tags getTags(){
        return tags;
    }
    protected int getStatePower(Actions state){
        int r = 0;
        Tags tags = state.getTags();
        for (Tag i : getTags()){
            if (tags.contains(i)) r++;
        }
        return r / Math.max(1,tags.size());
    }
    public int getStateLevel(Actions state){
        return Math.max(0, getStatePower(state)+1-state.getDifficulty());
    }
    public void stateSet(Actions state){
        if (this.state != Actions.Dead) {
            if (state != Actions.Idle) stateInterrupt();
            delay.set(state.getDel());
            this.state = state;
            clip.setState(state.getClipName());
            damageMod = 1;
            stateDuration = 0;
            stateOnSet();
            int asmod = get("Attack").get();
            int clipspeed = clip.setSpeed(attackSpeed + asmod);
            if (delay.max != 0) delay.set(Math.max(clipspeed, delay.time - attackSpeed - asmod));
            moveTriggered = false;
        }
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
        return (int) (rand.nextInt(get("Level").get()+1)*damageMod);
    }

    protected boolean damageBreakthrough(int organ, int damage){
        return true;
    }

    public void buff(Buffs buff, int level){
        stats.addEffect(buff, level);
    }

    @Override
    public boolean hittable(Faction faction){
        return super.hittable(faction) && state != Actions.Dead;
    }

    public boolean hit(Line hitBox, int damage){
        //Organ amputate
        for (int i = clip.organs.length-1; i >= 0; i--) {
            if (clip.organs[i].checkHit(hitBox)) {
                if (damage > 0) {
                    P t = new P(pos.x, hitBox.ptd.y);
                    Particle.explode(map, "Blood", getDepth(), t, t.getSubtract(hitBox.pt), 3, 3, 12, true, 5);
                    if (state == Actions.Down) {
                        if (clip.organs[i].vital && stats.getEffectLevel(Buffs.Bleed) < 3) buff(Buffs.Bleed, 1);
                        else clip.organs[i].amputate(hitBox, damage);
                    } else if (damageBreakthrough(i, damage)) {
                        if (traits.has(Traits.Trait.Tough)) {
                            if (stats.getEffectLevel(Buffs.Bleed) < 3) buff(Buffs.Bleed, 1);
                            else die();
                        } else clip.organs[i].amputate(hitBox, damage);
                    } else if (damage < get("Stamina").get() + 2) {
                        clip.organs[i].hit(hitBox);
                    } else {
                        P mid = hitBox.getMidpoint();
                        float radius = hitBox.getLength() / 2;
                        if (mid.getSubtract(pos.getAdd(0, -clip.getHeight())).getDist() < radius + getSize()) {
                            delay.performer = null;
                            jump = false;
                            if (stats.getEffectLevel(Buffs.Stun) < 1) {
                                vel.x = (damage + 5) * Float.compare(pos.x, hitBox.pt.x);
                                vel.y += -5 - damage;
                            }
                            buff(Buffs.Bleed, 1);
                            int blood = stats.getEffectLevel(Buffs.Bleed);
                            if (blood > 2) stateSet(Actions.Down);
                            else stateSet(Actions.Hit);
                            brain.onHit();
                            return true;
                        }
                    }
                } else if (rand.nextBoolean()){
                    clip.organs[i].hit(hitBox);
                }
                return true;
            }
        }
        return false;
    }

    public Stat get(String p){
        return stats.getStat(p);
    }

    public void die() {
        save = false;
        onInteract = null;
        if (state != Actions.Dead) {
            stateSet(Actions.Dead);
            Global.player.witnessDeath(this);
        }
    }

    public void disconnect(){ //do not use remove() - calls overriden method
        super.remove();
        speech.remove();
    }

    public void remove(){ //do not use disconnect()
        super.remove();
        speech.remove();
    }

    @Override
    public void collide(){
        jump = false;
        super.collide();
        if (!jump && vel.y > 0) {
            jumpAcross++;
            if (jumpAcross == 4) lastLand = Line.blank;
        } else jumpAcross = 0;
    }

    @Override
    public void onCollide(Line l){
        super.onCollide(l);
        if (!jump) {
            jump = true;
            if (l.clockwise() && !l.vertical()) setDepth(l.depth-OFFSET_UNIT);
            if (vel.y > Settings.gravity*4) Sound.play(map, pos, "land");
            if (vel.y >= Settings.gravityMax && hittable(Faction.Destructive)) {
                stateSet(Actions.Down);
                delay.time = 200;
                if (!traits.has(Traits.Trait.Lander)) buff(Buffs.Bleed, Global.r.nextInt(3));
                else delay.time = (int) Math.max(1,delay.time + Math.log(1 + get("Stamina").get())/Math.log(.5f)*75);
            } else if (state == Actions.Jump && vel.y > 0) {
                stateSet(Actions.Hit);
                delay.set(4);
            }
        }
    }
    protected void stateOnSet(){
        state.onSet(this);
    }
    protected void stateUpdate(){
        state.update(this);
        stateDuration++;
    }
    protected void stateInterrupt(){
        state.onInterrupt(this);
    }
    protected void stateFinish(){
        state.onFinish(this);
    }
    protected void stateTrigger(){
        state.trigger(this);
    }
    protected void moveTriggered(int p){
        if (lastState != state) {
            moveTriggered = true;
            stateTrigger();
            lastState = state;
        }
    }

    public Unit attack(){
        P hand = getHand();
        for (Unit i : map.updater.collideUnit(hand, (int) getReach(), faction)){
            i.hit(new Line(hand, hand.getAdd(dir*5,0)), getDamage());
            onAttack(i);
            if (!stats.triggerEffect(Buffs.Cleave)) return i;
        }
        return null;
    }

    @Override
    public void update() {
        speech.update();
        if (clip.update()) moveTriggered(0);
        if (state != Actions.Dead) {
            stats.update();
            if (delay.update() && delay.performer == null){
                if (!moveTriggered) moveTriggered(0);
                stateFinish();
            }
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

    public String getDescription(){
        String r = name + " [Genetic Code:"+seed+"]\n";
        r += "*Faction - "+faction.name()+"\n";
        r += "*Stats - \n"+stats.toString().split("/")[0].replace("&"," ")+"\n";
        r += "*Effects - "+stats.getEffectsList()+"\n";
        r += "*Traits - "+traits.toString()+"\n";
        return r;
    }

    public void revive() {
        stats.clearBuffs(false);
        Particle.holyExplosion(map, pos, getDepth());
        state = Actions.Idle;
        delay.set(50);
        for (Organ i : clip.organs){
            i.stitch();
        }
        clip.setOrganHealth();
        clip.updateStatus();
        stateSet(Actions.Down);
        init();
    }
}