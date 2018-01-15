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
    public Actions[] moveset;
    HashMap<Actions, int[]> cooldown = new HashMap<>();
    public int controlId;
    public Body clip;
    public Actions state = Actions.Idle;
    public boolean jump = false;
    public AI brain;
    public EventTimer delay = new EventTimer(5, false, null);
    public P target = new P();
    public Stats stats = new Stats(this);
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
        String[] movesets = sections[11].split(",");
        moveset = new Actions[movesets.length];
        for (int i=0;i<movesets.length;i++) moveset[i] = Actions.getByName(movesets[i]);
        clip.setOrganHealth();
        return sections;
    }

    @Override
    public Unit start(){
        speech = new Speech(this);
        super.start();
        return this;
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

    String getConversation(){
        return speech.getTextarea().getText();
    }
    void hear(Prop pos, String p){
        if (!speech.getTextarea().isVisible()) brain.hear(pos, p);
    }
    public P getHand(){
        if (clip.getHand() == -1) return pos.getAdd(0,-clip.getHeight());
        return pos.getAdd(clip.organs[clip.getHand()].ptd);
    }
    public float getReach(){
        Stat reach = get("Reach");
        if (reach == null) return getSize();
        return reach.get();
    }
    protected int[] getCooldown(Actions state) {
        int[] r = cooldown.get(state);
        if (r == null) r = new int[]{Global.time};
        return r;
    }
    protected boolean tagCapable(Tags p){
        return true;
    }
    public int setState(Actions state) {
        if (delay.over() && jump && this.state != state && tagCapable(state.getTags())) {
            lastState = null;
            face(target.x > 0 ? 1 : -1, true);
            if (Global.time >= getCooldown(state)[0]) {
                if (get("Stamina").require(state.getCost())) {
                    if (state != Actions.Idle) delay.performer = null;
                    stateLevel = getStateLevel(state);
                    if (stateLevel >= 1){
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
    public int getStateLevel(Actions state){
        return get("Level").get();
    }
    public void stateSet(Actions state){
        if (this.state != Actions.Dead) {
            if (state != Actions.Idle) this.state.onInterrupt(this);
            delay.set(state.getDel());
            this.state = state;
            clip.setState(state.getClipName());
            brain.setState(state);
            damageMod = 1;
            stateDuration = 0;
            this.state.onSet(this);
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
        return (int) (rand.nextInt(get("Level").get()+2)*damageMod);
    }

    protected boolean processDamage(int organ, int damage){
        return true;
    }

    public void buff(Buffs buff, int level){
        stats.addEffect(buff, level);
    }

    @Override
    public boolean hittable(Faction faction){
        return super.hittable(faction) && state != Actions.Dead;
    }

    public boolean hit(Line hitBox, int damage, boolean blunt){
        if (state == Actions.Down){
            die();
            return true;
        } else {
            boolean ko = stats.triggerEffect(Buffs.Mark) || get("Toughness").get() - (damage + stats.getEffectLevel(Buffs.Bleed)) <= 0;
            //Organ amputate
            for (int i = 0; i < clip.organs.length; i++) {
                if (clip.organs[i].checkHit(hitBox)) {
                    P t = new P(pos.x, hitBox.ptd.y);
                    Particle.explode(map, "Blood", getDepth(), t, t.getSubtract(hitBox.pt), 3, 3, 12, true, 5);
                    if (!blunt && ko && processDamage(i, damage)) clip.organs[i].amputate(hitBox, damage);
                    else clip.organs[i].hit(hitBox);
                    return true;
                }
            }
            //Knockdown
            P mid = hitBox.getMidpoint();
            float radius = hitBox.getLength() / 2;
            if (mid.getSubtract(pos.getAdd(0, -clip.getHeight())).getDist() < radius + getSize()) {
                float knockback = damage / get("Toughness").get();
                delay.performer = null;
                jump = false;
                if (state == Actions.Rest) knockback += .5f;
                vel.x = (knockback * 8 + 5) * Float.compare(pos.x, hitBox.pt.x);
                vel.y += -5 - knockback * 2;
                if (damage > 0) {
                    buff(Buffs.Bleed, 1);
                    if (ko) {
                        stateSet(Actions.Down);
                        delay.time *= 1 + stats.getEffectLevel(Buffs.Bleed) * .2f;
                    } else stateSet(Actions.Hit);
                }
                brain.onHit();
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
            if (jumpAcross == 3) lastLand = new Line();
        } else jumpAcross = 0;
    }

    @Override
    public void onCollide(Line l){
        super.onCollide(l);
        if (!jump) {
            jump = true;
            if (l.clockwise() && !l.vertical()) setDepth(l.depth-OFFSET_UNIT);
            int mag = (int) (vel.y - Settings.gravityMax);
            if (vel.y > Settings.gravity*4) Sound.play(map, pos, "land");
            if (mag >= 0 && hittable(Faction.Destructive)) {
                buff(Buffs.Bleed, Global.r.nextInt(3));
                stateSet(Actions.Down);
            } else if (state == Actions.Jump && vel.y > 0) {
                stateSet(Actions.Hit);
                delay.set(4);
            }
        }
    }

    protected void stateUpdate(){
        state.update(this);
        stateDuration++;
    }

    protected void stateFinish(){
        state.onFinish(this);
    }

    protected void moveTriggered(int p){
        if (lastState != state) {
            moveTriggered = true;
            state.trigger(this);
            lastState = state;
        }
    }

    public Unit attack(){
        P hand = getHand();
        for (Unit i : map.updater.collideUnit(hand, (int) getReach(), faction)){
            i.hit(new Line(hand, hand.getAdd(dir*5,0)), getDamage(), true);
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

    @Override
    public String toString(){
        StringBuilder r = new StringBuilder(super.toString());
        r.append("@").append(controlId).append("@").append(map.name).append("@").append(clip.toString()).append("@").append(stats.toString()).append("@").append(faction.name()).append("@").append(brain.toString()).append("@");
        for (Actions i : moveset) r.append(i.getName()).append(",");
        return r.deleteCharAt(r.length()-1).toString();
    }

    public String getDescription(){
        String r = name + " [Genetic Code:"+seed+"]\n";
        r += "*Faction - "+faction.name()+"\n";
        r += "*Stats - \n"+stats.toString().split("/")[0].replace("&"," ")+"\n";
        r += "*Effects - "+stats.getEffectsList();
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