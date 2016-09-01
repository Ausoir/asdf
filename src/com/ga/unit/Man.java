package com.ga.unit;

import com.ga.game.Settings;
import com.ga.map.Global;
import com.ga.tool.EventTimer;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.intelligence.Intelligence;
import com.ga.unit.intelligence.Speech;
import com.ga.unit.item.Armor;
import com.ga.unit.item.Weapon;
import com.ga.unit.skill.Buff;
import com.ga.unit.skill.Skills;
import com.ga.unit.skill.Stat;

import java.awt.*;

/**
 * Created by Ga on 5/7/2016.
 */
public class Man extends Unit {
    public int height;
    public String speechType;
    public ClipMan clip;
    public Actions state = Actions.Idle;
    public boolean jump = false;
    public Intelligence brain;
    public EventTimer delay = new EventTimer(5, false, null);
    public Weapon weapon;
    public Armor armor;
    public P target = new P();
    private Speech speech = new Speech(this);
    public Stats stats = new Stats();
    protected HealthBar hp;

    public Man(String name, P pt, int seed) {
        super(name, seed, pt, 10);
        clip = new ClipMan(this);
        height = clip.getHeight();
        hp = new HealthBar(get("health"), pos);
        new Shadow(pos);
        speechType = name.toLowerCase();
    }

    public void setWeapon(Weapon p){
        weapon = p;
        weapon.pos = pos;
        weapon.setOrgan(clip.organs[ClipMan.FIST]);
        weapon.dir = dir;
        if (!p.name.equals("Fist")){
            clip.localAnimation[0] = clip.localAnimation[2];
        } else {
            clip.resetLocalAnimation(0);
        }
    }

    public void setArmor(Armor p){
        armor = p;
        clip.organs[1].setStyle(p.color1.darker(), 9);
        clip.organs[2].setStyle(p.color2.darker(), 13);
        clip.organs[3].setStyle(p.color2, 13);
        clip.organs[4].setStyle(p.color1, 15);
        clip.organs[5].setStyle(p.color1, 9);
    }

    public Weapon getWeapon(){
        return weapon;
    }

    public void remove() {
        super.remove();
        Updater.remove(this);
    }

    public void say(String p) {
        speech.say(p);
    }

    public boolean isSpeaking(){
        return speech.isSpeaking();
    }

    public void hear(Unit pos, String p){
        if (brain != null) brain.hear(pos, p);
    }

    public void setState(Moveset state) {
        if (!delay.over() || !jump) return;
        if (this.state != state) {
            stats.unrest();
            String statreq = state.getStatReq();
            float req = state.getCost();
            Actions action = null;
            if (state instanceof Actions) action = (Actions) state;
            if (action != null) {
                switch (action) {
                    case Weapon:
                        setState(weapon.getMoveset());
                        return;
                    case WalkL:
                        face(-1);
                        break;
                    case WalkR:
                        face(1);
                        break;
                }
            }
            if (req < 0){
                if (statreq.equals("arms")) req = weapon.weight*-req;
                else if (statreq.equals("legs")) req = armor.weight*-req;
            }
            if (statreq == null || get(statreq).require((int) req)){
                delay.set(state.getDel());
                vel.x *= state.getVelX();
                if (action != null) {
                    this.state = action;
                    clip.setState(action);
                    stateSet(action);
                    if (brain != null) brain.setState(action);
                } else {
                    Skills t = (Skills) state;
                    clip.setState(t.getAction());
                    delay.performer = t.getEvent(this);
                }
            }
        }
    }

    public void stateSet(Actions state){
        switch (state){
            case Idle:
                if (!weapon.name.equals("Fist")){
                    if (!get("arms").isFull() || !get("legs").isFull())
                        clip.setState(Actions.Exhausted);
                }
                break;
            case Duck:
                vel.x = 0;
                if (Global.map.fallSafe(pos.getAdd(vel))) pos.y+= Settings.gravity;
                break;
            case Jump:
                vel.y -= 50;
                jump = false;
                break;
        }
    }

    public void face(int dir) {
        if (!delay.over() || !jump || this.dir == dir) return;
        this.dir = dir;
        weapon.dir = dir;
        clip.setClip();
    }

    public void face(P p) {
        face(p.x - pos.x > 0 ? 1 : -1);
    }

    @Override
    public void hit(Unit hitter, int damage) {
        if (state == Actions.Down || state == Actions.Hit) return;
        if (stats.hasEffect(Buff.Evade)) return;
        damage = (int) Math.max(0,damage - armor.getArmor());
        delay.set(0);
        int x = hitter.pos.x - pos.x > 0 ? 1 : -1;
        face(x < 0 ? -1 : 1);
        float ratio = (float) damage * 10 / get("health").getMax();
        float knockback = Math.min(ratio, 50);
        if (ratio > 100) setState(Actions.Down);
        else setState(Actions.Hit);
        vel.x = knockback*x;
        vel.y = -knockback * 1.5f - 10;
        jump = false;
        hurt(damage);
        if (state != Actions.Dead) {
            say("@hit");
            if (brain != null && hitter instanceof Man){
                brain.hit((Man) hitter);
            }
        }
    }

    @Override
    public boolean checkHit(Unit hitter, Line p, int force){
        if (faction != hitter.faction) {
            if (p.lineCross(new Line(clip.organs[0].ptd,clip.organs[2].ptd).getAdd(pos))){
                hit(hitter, force);
                return true;
            }
        }
        return false;
    }

    public boolean isExhausted(){
        return get("arms").get() < weapon.weight && get("legs").get() < armor.weight;
    }

    public Stat get(String p){
        return stats.getStat(p);
    }

    public void hurt(int damage) {
        hp.show();
        get("health").add(-damage);
        if (get("health").get() <= 0) die();
    }

    public void die() {
        say("@x");
        brain = null;
        delay.set(5);
        jump = true;
        state = Actions.Dead;
        height = 0;
        clip.setState(Actions.Dead);
        setDepth(9);
        delay.set(-1);
    }

    public void attack() {
        Unit hit;
        int dam = weapon.getDamage() + get("damage").get();
        boolean cleave = false;
        if (stats.endEffect(Buff.Cleave)) cleave = true;
        if (state == Actions.Stab) dam *= 4;
        else if (state == Actions.Pierce) dam += Math.abs(vel.x);
        else if (state == Actions.Swing) cleave = true;
        hit = Updater.collide(this, weapon.getLine(), dam, cleave);
        if (hit != null){
            P pt = weapon.getPoint();
            Particle.explode(pt,pt.getAdd(clip.organs[ClipMan.FIST].ptd.getMinus()),10,20,true);
            if (brain != null) brain.attacked();
        }
    }

    @Override
    public void collide(){
        P op = pos.getAdd(0,height);
        P vp = op.getAdd(vel);
        Line pp = new Line(op,vp);
        for (Line i : Global.map.land){
            if (i.lineCross(pp)){
                float slope = i.getSlope();
                float t = (slope * vel.x * 10000 - 5) / 10000f;
                if (t < vel.y) {
                    if (Math.abs(vel.x) > 0 && t < 0) {
                        float xforce = (float) Math.cos(Math.atan(t));
                        if (xforce < .07){
                            vel.set(0,0);
                        } else vel.x *= xforce;
                    }
                    vel.x *= .9f;
                    onCollide(i);
                } else if (i.opt == 1){
                    vel.y = 0;
                    onCollide(i);
                }
            }
        }
    }

    @Override
    public void onCollide(Line l){
        super.onCollide(l);
        jump = true;
    }

    private void stateUpdate(){
        if (state == Actions.WalkL) vel.x = -get("speed").get()/10;
        else if (state == Actions.WalkR) vel.x = get("speed").get()/10;
        else if (state == Actions.Rest) stats.rest();
        if (!weapon.name.equals("Fist")) {
            switch (state){
                case Whirlwind:
                    weapon.rotation = delay.time * -weapon.dir;
                    attack();
                    break;
                case Swing:
                    weapon.rotation = weapon.dir + delay.time * .2f * -weapon.dir;
                    if (delay.time < 12) attack();
                    break;
                case Stab:
                    if (delay.time < 10) attack();
                    break;
                case Dash:
                    if (delay.time == 20){
                        vel.set(get("speed").get()*dir,-10);
                    } else if (delay.time < 20){
                        attack();
                    }
                    break;
            }
        }
    }

    @Override
    public void update() {
        stats.update();
        delay.update();
        clip.update();
        stateUpdate();
        super.update();
        if (brain != null) brain.update();
    }

    public void drawDebug(Graphics2D g) {
        g.setColor(Color.white);
        P fist = clip.organs[ClipMan.FIST].ptd.getAdd(pos);
        P foot = clip.organs[ClipMan.FOOT].ptd.getAdd(pos);
        P t = Global.getRelPos((int) (clip.organs[0].pt.x + pos.x), (int) (clip.organs[0].pt.y + pos.y));
        g.drawArc((int) t.x, (int) t.y, 20, 20, 0, 360);
        t = Global.getRelPos((int) fist.x, (int) fist.y);
        g.drawArc((int) t.x, (int) t.y, 3, 3, 0, 360);
        t = Global.getRelPos((int) foot.x, (int) foot.y);
        g.drawArc((int) t.x, (int) t.y, 3, 3, 0, 360);
        g.setColor(Color.red);
        t = Global.getRelPos((int) weapon.getPoint().x, (int) weapon.getPoint().y);
        g.drawArc((int) t.x, (int) t.y, 3, 3, 0, 360);
        g.setColor(Color.blue);
        t = Global.getRelPos((int) (pos.x), (int) (height + pos.y));
        g.drawArc((int) t.x, (int) t.y, 5, 5, 0, 360);
    }

    @Override
    public void draw(Graphics2D g) {
        weapon.draw(g);
        for (Organ i : clip.organs) {
            g.setStroke(i.stroke);
            g.setColor(i.color);
            i.draw(g, pos, 1);
        }
        super.draw(g, clip.organs[0].ptd);
//        drawDebug(g);
    }

    @Override
    public String toString(){
        if (state == Actions.Dead) return "";
        pos.y -= height;
        String r = super.toString();
        pos.y += height;
        return r;
    }
}