package com.ga.unit;

import com.ga.game.Settings;
import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.tool.*;
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
    public L<Actions> moveset = new L<>();
    public HashMap<Actions, Cooldown> cooldown = new HashMap<>();
    public int controlId;
    public Body clip;
    public Actions state = Actions.Idle;
    public boolean grounded = false;
    public AI brain;
    public EventTimer delay = new EventTimer(5, false, null);
    public P target = new P();
    public Stats stats = new Stats(this);
    public Traits traits = new Traits();
    public float damageMod = 1;
    protected String speechType;
    protected Speech speech = new Speech(this);
    public int stateLevel = 1;
    private Actions lastState = Actions.Idle;
    private int jumpAcross = 0;
    public int stateDuration = 0;
    private boolean moveTriggered = false;

    public Unit(Map map, String name, int seed) {
        super(map, null, seed, 8);
        this.name = name;
        gravity = true;
        tags.add(Tag.Athletics);
    }
    @Override
    public Unit start(){
        super.start();
        speech.start();
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
        Actions.load(this,sections[11]);
        clip.setOrganHealth();
        traits.load(this,sections[12]);
        return sections;
    }
    @Override
    public String toString(){
        return super.toString() + "@" + controlId + "@" + map.name.toString() + "@" + clip.toString() + "@" + stats.toString() + "@" + faction.name() +
                "@" + brain.toString() + "@" + moveset.toString() + "@" + traits.toString();
    }
    @Override
    public void onField(){
        stats.initialize();
    }
    private int getMemorySize(){
        return moveset.size()-1+traits.getMemorySize();
    }
    public boolean canLearn(){
        return getMemorySize() < get("Intelligence").getMax();
    }
    public boolean learnSkill(Actions move){
        if (!moveset.contains(move) && canLearn()){
            moveset.add(move);
            return true;
        }
        return false;
    }
    public boolean learnTrait(Traits.Trait p) {
        return canLearn() && traits.learn(this, p);
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
        return speech.text;
    }
    void hear(Prop pos, String p){
        if (!speech.visible) brain.hear(pos, p);
    }
    public P getHand(){
        int hand = clip.getHand();
        if (hand == -1) return pos.getAdd(0,-clip.getHeight());
        return pos.getAdd(clip.organs[hand].ptd);
    }
    public P getHead(){
        int head = clip.getHead();
        if (head == -1) return pos.getAdd(0,-clip.getHeight());
        return pos.getAdd(clip.organs[head].ptd);
    }
    public float getReach(){
        return getSize();
    }
    public Cooldown getCooldown(Actions state) {
        Cooldown r = cooldown.get(state);
        if (r == null){
            r = new Cooldown(state.getCooldown());
            cooldown.put(state, r);
        }
        return r;
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
    public int setState(Actions state) {
        return state.set(this);
    }
    public int getStateLevel(Actions state){
        return Math.max(0, getStatePower(state)+1-state.getDifficulty());
    }
    public void stateSet(Actions state){
        face(target.x > 0 ? 1 : -1, false);
        clip.setState(state.getClipName());
        if (state != Actions.Idle){
            stats.restTimer.reset();
            getCooldown(state).reset();
            delay.performer = null;
            stateInterrupt();
            delay.set(state.getDel());
            if (delay.max != 0 && state.isDynamicAnimation()){
                int attackSpeed = getAttackSpeed();
                int clipspeed = clip.setSpeed(attackSpeed);
                delay.set(Math.max(clipspeed, delay.time - attackSpeed));
            }
        }
        this.state = state;
        damageMod = 1;
        stateDuration = 0;
        moveTriggered = false;
        stateOnSet(state);
    }
    protected int getAttackSpeed(){
        return get("Attack").get();
    }
    public boolean face(int dir, boolean force) {
        if (force || (delay.over() && grounded && this.dir != dir)) {
            this.dir = dir;
            return true;
        }
        return false;
    }
    public boolean requireMana(int p) {
        return true;
    }

    @Override
    public int getDamage(){
        return (int) ((get("Level").get()+1)*damageMod);
    }

    protected boolean damageBreakthrough(int organ, boolean blunt, int damage){
        return true;
    }

    public void buff(Buffs buff, int level){
        stats.addEffect(buff, level);
    }

    @Override
    public boolean hittable(Faction faction){
        return super.hittable(faction) && state != Actions.Dead;
    }

    public Line getPosLine(){
        int organ;
        int timeout = 0;
        do {
            timeout++;
            organ = SyncRand.r.nextInt(clip.organs.length);
        } while (timeout < clip.organs.length+3 && clip.organs[organ].amputated);
        return clip.organs[organ].getIntersector().getAdd(pos);
    }
    public boolean hit(Line hitBox, int damage){
        return hit(hitBox, true, damage);
    }
    public boolean hit(Line hitBox, boolean blunt, int damage){
        //Organ amputate
        for (int i = clip.organs.length-1; i >= 0; i--) {
            if (clip.organs[i].checkHit(hitBox)) {
                if (damage > 0) {
                    P t = new P(pos.x, hitBox.ptd.y);
                    Particle.explode(map, "Blood", getDepth(), t, t.getSubtract(hitBox.pt), 3, 3, 12, true, 5);
                    P mid = hitBox.getMidpoint();
                    float radius = hitBox.getLength() / 2;
                    if (mid.getSubtract(pos.getAdd(0, -clip.getHeight())).getDist() < radius + getSize()) {
                        delay.performer = null;
                        grounded = false;
                        if (!traits.has(Traits.Trait.Tough)) {
                            if (stats.getEffectLevel(Buffs.Stun) < 1) {
                                vel.x = damage * Float.compare(pos.x, hitBox.pt.x);
                                vel.y += -damage;
                            }
                            stateSet(Actions.Hit);
                        }
                        brain.onHit();
                        clip.organs[i].hit(hitBox);
                    }
                    if (stats.triggerEffect(Buffs.Mark)) clip.organs[i].amputate(hitBox, damage);
                    else if (damageBreakthrough(i, blunt, damage)) {
                        if (get("Agility").require(1+(damage/4))) {
                            Particle.explode(map, "Blood", getDepth()+.01f, getRandomPoint(), new P(), 9, 5, 0, true, 24);
                        } else {
                            buff(Buffs.Bleed, 1);
                            clip.organs[i].amputate(hitBox, damage);
                        }
                    }
                } else {
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
        if (state != Actions.Dead) stateSet(Actions.Dead);
    }

    public void remove(){
        speech.remove();
        super.remove();
    }

    @Override
    public void collide(){
        grounded = false;
        super.collide();
        if (!grounded && vel.y > 0) {
            jumpAcross++;
            if (jumpAcross == 4){
                lastLand = Line.blank;
            }
        } else jumpAcross = 0;
    }

    @Override
    public void onCollide(Line l){
        super.onCollide(l);
        if (!grounded) {
            grounded = true;
            if (l.clockwise() && !l.vertical()) setDepth(l.depth-OFFSET_UNIT);
            if (vel.y > 0){
                if (vel.y >= Settings.gravityMax) {
                    if (hittable(Faction.Destructive)) {
                        stateSet(Actions.Down);
                        delay.time = (int) (800f / Global.clamp(get("Agility").getMax(), 1, 800));
                    }
                } else if (state == Actions.Jump && stateDuration > 1) {
                    Sound.play(map, pos, "land");
                    stateSet(Actions.Hit);
                    delay.set(4);
                }
            }
        }
    }
    protected void stateOnSet(Actions state){
        state.onSet(this);
    }
    protected void stateUpdate(){
        state.update(this);
        stateDuration++;
    }
    protected void stateInterrupt(){
        state.onInterrupt(this);
    }
    protected boolean stateFinish(){
        return state.onFinish(this);
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
        image.visible = p;
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
        String r = name + " [Genetic Code: "+seed+"] [Faction: "+faction.name()+"]\n";
        r += "  *** Stats ***\n"+stats.toString().split("/")[0].replace("&"," ")+"\n";
        r += "  *** Effects ***\n"+stats.getEffectsList()+"\n";
        r += "  *** Traits ***\n"+traits.toString()+"\n";
        r += "  *** Capabilities ***\n"+getTags().toString()+"\n";
        return r;
    }

    public void revive() {
        stats.clearBuffs();
        Particle.holyExplosion(map, pos, getDepth());
        state = Actions.Idle;
        delay.set(50);
        for (Organ i : clip.organs){
            i.stitch();
        }
        clip.setOrganHealth();
        clip.updateStatus();
        stateSet(Actions.Down);
    }
}