package com.ga.unit;

import com.ga.game.Settings;
import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.Updater;
import com.ga.tool.*;
import com.ga.unit.item.Item;
import com.ga.unit.player.MainPlayer;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Buffs;
import com.ga.unit.skill.Stat;
import com.ga.unit.skill.Stats;

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
    protected Speech speech = new Speech(this);
    public int stateLevel = 1;
    public int attackSpeed = 0;
    private Actions lastState = Actions.Idle;
    private int jumpAcross = 0;
    public int stateDuration = 0;
    Item[] loot;

    public Unit(String name, int seed) {
        super(null,seed);
        this.name = name;
        controlId = Global.getControlId();
        gravity = true;
        tags.add(Tag.Marksman);
        tags.add(Tag.Athletics);
        tags.add(Tag.Combustible);
        tags.add(Tag.Freezable);
    }

    @Override
    public String[] load(String p){
        String[] sections = super.load(p);
        clip.load(sections[4]);
        stats.load(sections[5]);
        faction = Faction.valueOf(sections[6]);
        brain = Intelligence.get(sections[7], this);
        loadActions(sections[8]);
        clip.setOrganHealth();
        return sections;
    }

    protected void loadActions(String p){
        String[] rangeMoves = p.split("/");
        String[] ranges = rangeMoves[0].split(",");
        String[] moveZones = rangeMoves[1].split(",");
        rangeZone = new int[ranges.length];
        for (int i=0;i<ranges.length;i++) rangeZone[i] = Integer.parseInt(ranges[i]);
        for (int i=0;i<moveZones.length;i++) {
            String[] moves = moveZones[i].split("&");
            Actions[] movesets = new Actions[moves.length];
            for (int ii=0;ii<moves.length;ii++){
                movesets[ii] = Actions.getByName(moves[ii]);
            }
            this.moveset.put(i, movesets);
        }
    }

    Actions performActions(int dist){
        int range = moveset.size()-1;
        for (int i=0;i<rangeZone.length;i++){
            if (dist <= rangeZone[i]){
                range = i;
                break;
            }
        }
        Actions[] t = moveset.get(range);
        if (t != null) {
            Actions move = t[Global.r.nextInt(t.length)];
            return move;
        } else {
            Global.p("Can't find moveset for range " + dist + "@Zone " + range);
        }
        return null;
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
        return speech.getText();
    }
    void hear(Prop pos, String p){
        if (!speech.isVisible()) brain.hear(pos, p);
    }
    public P getHand(){
        return pos.getAdd(clip.organs[clip.HAND].ptd);
    }
    public float getReach(){
        return getSize()+36;
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
        Sound.play(pos, "swing_normal");
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
        return (int) (rand.nextInt(get("Level").get()+3)*damageMod);
    }

    protected int getDamageReduction(int organ){
        return 0;
    }

    public void buff(Buffs buff, int level){
        stats.addEffect(buff, level);
    }

    public void hit(int dir, float knockback) {
        delay.performer = null;
        jump = false;
        if (state == Actions.Rest) knockback += .5f;
        vel.x = knockback * 8 * dir;
        vel.y += -knockback * 2;
        if (knockback > 0){
            if (state == Actions.Down) die();
            else {
                buff(Buffs.Bleed, 1);
                if (stats.triggerEffect(Buffs.Mark) || knockback > .5f+Global.r.nextFloat()*.5f) {
                    stateSet(Actions.Down);
                    delay.time *= 1+stats.getEffectLevel(Buffs.Bleed)*.2f;
                } else stateSet(Actions.Hit);
            }
        }
        brain.onHit();
    }

    @Override
    public boolean hittable(Faction faction){
        return super.hittable(faction) && state != Actions.Dead;
    }

    public void checkHit(P hitPoint, int damage){
        float total = (stats.getEffectLevel(Buffs.Bleed)*.5f + 1) * damage;
        hit(hitPoint.x < pos.x ? 1 : hitPoint.x > pos.x ? -1 : 0, total / get("Stamina").getMax());
    }

    public boolean checkHit(Line hitBox, int damage, boolean blunt){
        boolean r = false;
        for (int i = 0; i < clip.organs.length; i++) {
            if (clip.organs[i].checkHit(hitBox)) {
                if (!r) checkHit(hitBox.pt, damage);
                P t = new P(pos.x, hitBox.ptd.y);
                Particle.explode("Blood", getDepth(),t, t.getSubtract(hitBox.pt), 3, 3, 12, true, 5);
                int dam;
                if (!blunt) dam = Math.max(0, damage - getDamageReduction(i));
                else dam = damage;
                if (clip.organs[i].hit(hitBox, dam)) Sound.play(pos, "hit_bloody");
                r = true;
            }
        }
        return r;
    }

    public Stat get(String p){
        return stats.getStat(p);
    }

    public void die() {
        stateSet(Actions.Dead);
        Global.player.witnessDeath(this);
        if (loot != null){
            for (Item i : loot){
                if (i != null){
                    buff(Buffs.Shock,1);
                    break;
                }
            }
        }
        onInteract = new Interactive(){
            public void interact(Man p){
                if (p instanceof MainPlayer && loot != null){
                    Performer[] actions = new Performer[loot.length];
                    boolean lootable = false;
                    for (int i=0;i<loot.length;i++){
                        if (loot[i] != null) {
                            final int index = i;
                            actions[i] = new Performer() {
                                public void perform() {
                                    Updater.addQueue.add(loot[index]);
                                    loot[index] = null;
                                }
                            };
                            lootable = true;
                        }
                    }
                    if (lootable) Global.display.menu.showContainer("Corpse of "+name, loot, actions);
                }
            }
        };
    }

    public void remove(){
        super.remove();
        speech.remove();
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
    public void onCollide(Line l){
        super.onCollide(l);
        if (!jump) {
            jump = true;
            if (l.clockwise()) setDepth(l.depth-1);
            int mag = (int) (vel.y - Settings.gravityMax);
            if (vel.y > Settings.gravity*4) Sound.play(pos, "land");
            if (hittable(Faction.Destructive)) {
                if (mag >= 0) {
                    buff(Buffs.Bleed, Global.r.nextInt(3));
                    stateSet(Actions.Down);
                } else if (state == Actions.Jump && vel.y > 0) {
                    stateSet(Actions.Hit);
                    delay.set(4);
                }
            }
        }
    }

    protected void stateUpdate(){
        state.update(this);
        stateDuration++;
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

    public Unit attack(){
        P hand = getHand();
        for (Unit i : Updater.collideUnit(hand, (int) getReach(), faction)){
            i.checkHit(hand, getDamage());
            onAttack(i);
            return i;
        }
        return null;
    }

    @Override
    public void update() {
        speech.update();
        if (clip.update()) moveTriggered(0);
        stats.update();
        if (delay.update() && delay.performer == null) stateDone();
        stateUpdate();
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
        r += "@"+clip.toString()+"@"+stats.toString()+"@"+faction.name()+"@"+brain.toString()+"@";
        for (int i : rangeZone) r += i+",";
        r = r.substring(0,r.length()-1)+"/";
        Iterator it = moveset.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Actions[] moves = (Actions[]) pair.getValue();
            for (Actions i : moves){
                r += i.getName() + "&";
            }
            r = r.substring(0,r.length()-1)+(it.hasNext()?",":"");
        }
        return r;
    }

    public String getDescription(){
        String r = name + " [Genetic Code:"+seed+"]";
        r += "*Faction - "+faction.name()+"\n";
        r += "*Stats - \n"+stats.toString().split("/")[0].replace("&"," ")+"\n";
        r += "*Effects - "+stats.getEffectsList();
        return r;
    }

    public void revive() {
        onInteract = null;
        stats.clearBuffs();
        Particle.holyExplosion(pos,getDepth());
        state = Actions.Idle;
        stateSet(Actions.Down);
        delay.set(50);
        for (Organ i : clip.organs){
            i.stitch();
        }
        clip.setOrganHealth();
    }
}