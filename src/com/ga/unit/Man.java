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
import com.ga.unit.skill.Adrenaline;
import com.ga.unit.skill.Skill;
import com.ga.unit.skill.Stat;

import java.awt.*;

/**
 * Created by Ga on 5/7/2016.
 */
public class Man extends Unit {
    public int height;
    public String speechType;
    public ClipMan clip;
    public Actions state = Actions.IDLE;
    public boolean jump = false;
    public Intelligence brain;
    public EventTimer delay = new EventTimer(5, false, null);
    public Weapon weapon;
    public Armor armor;
    public P target = new P();
    public Skill skill = new Adrenaline();
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
        weapon.organ = clip.organs[ClipMan.FIST];
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

    public void hear(Unit pos, String p){
        if (brain != null) brain.hear(pos, p);
    }

    public void setState(Actions state) {
        if (!delay.over() || !jump) return;
        if (this.state != state) {
            String statreq = null;
            int req = 0;
            float velx = 1;
            int del = 0;
            switch (state) {
                case HIT:
                    del = 15;
                    break;
                case DOWN:
                    del = 50;
                    velx = 1.5f;
                    break;
                case SKILL:
                    statreq = skill.stat;
                    req = skill.cost;
                    del = 15;
                    velx = 0;
                    break;
                case IDLE:
                    velx = 0;
                    break;
                case JUMP:
                    statreq = "legs";
                    req = 2;
                    velx = 1.5f;
                    jump = false;
                    break;
                case WALKL:
                    face(-1);
                    del = 5;
                    break;
                case WALKR:
                    face(1);
                    del = 5;
                    break;
                case ATTACK:
                    statreq = "arms";
                    req = weapon.weight;
                    velx = 1.5f;
                    del = 25;
                    break;
                case SWING:
                    if (weapon.name.equals("Fist")) return;
                    statreq = "arms";
                    req = weapon.weight;
                    velx = 0;
                    del = 25;
                    break;
                case WHIRLWIND:
                    statreq = "arms";
                    req = weapon.weight*2;
                    velx = 0;
                    del = 50;
                    break;
                case KICK:
                    statreq = "legs";
                    req = armor.weight;
                    velx = 1;
                    del = 30;
                    break;
            }
            if (statreq == null || get(statreq).require(req)){
                if (brain != null) brain.setState(state);
//                face(target.x<0?-1:1);
                delay.set(del);
                vel.x *= velx;
                this.state = state;
                clip.setState(state);
                stateSet(state);
            }
        }
    }

    public void stateSet(Actions state){
        switch (state){
            case IDLE:
                if (!weapon.name.equals("Fist")){
                    if (!get("arms").isFull() || !get("legs").isFull())
                        clip.setState(Actions.EXHAUSTED);
                }
                break;
            case DUCK:
                vel.x = 0;
                if (Global.map.fallSafe(pos.getAdd(vel))) pos.y+= Settings.gravity;
                break;
            case JUMP:
                vel.y -= 50;
                break;
            case SKILL:
                skill.init(this);
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
        if (state == Actions.DOWN || state == Actions.HIT) return;
        if (stats.hasEffect("Evade")) return;
        damage = (int) Math.max(0,damage - armor.getArmor());
        delay.set(0);
        int x = hitter.pos.x - pos.x > 0 ? 1 : -1;
        face(x < 0 ? -1 : 1);
        float ratio = (float) damage * 10 / get("health").max;
        float knockback = Math.min(ratio, 50);
        if (ratio > 100) setState(Actions.DOWN);
        else setState(Actions.HIT);
        vel.x = knockback*x;
        vel.y = -knockback * 1.5f - 10;
        jump = false;
        hurt(damage);
        if (state != Actions.DEAD) {
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
        return stats.stats.get(p);
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
        state = Actions.DEAD;
        height = 0;
        clip.setState(Actions.DEAD);
        setDepth(9);
        delay.set(-1);
    }

    public void attack() {
        Unit hit = null;
        int dam = weapon.getDamage() + get("damage").get();
        if (state == Actions.WHIRLWIND) {
            dam *= 2;
            hit = Updater.collide(this, new Line(clip.organs[ClipMan.FIST].ptd.getAdd(pos),weapon.getPoint()), dam, true);
        } else if (state == Actions.SWING) {
            hit = Updater.collide(this, weapon.getLine(), dam, true);
        } else if (state == Actions.ATTACK){
            dam += Math.abs(vel.x);
            hit = Updater.collide(this, weapon.getLine(), dam, false);
            if (hit != null) stats.endEffect("Cleave");
        }
        if (hit != null && brain != null) brain.attacked();
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
                } else if (i.opt == 1){
                    vel.y = 0;
                }
                onCollide(i);
            }
        }
    }

    @Override
    public void onCollide(Line l){
        super.onCollide(l);
        jump = true;
    }

    @Override
    public void update() {
        stats.update();
        delay.update();
        clip.update();
        if (state == Actions.WALKL) vel.x = -get("speed").get()/10;
        else if (state == Actions.WALKR) vel.x = get("speed").get()/10;
        if (!weapon.name.equals("Fist")) {
            if (state == Actions.WHIRLWIND) {
                if (delay.time < 20) {
                    weapon.rotation = delay.time * -weapon.dir;
                    attack();
                }
            } else if (state == Actions.SWING) {
                weapon.rotation = weapon.dir + delay.time * .2f * -weapon.dir;
                if (delay.time < 12) attack();
            }
        }
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
        pos.y -= height;
        String r = super.toString();
        pos.y += height;
        return r;
    }
}