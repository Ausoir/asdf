package com.ga.unit;

import com.ga.intelligence.Intelligence;
import com.ga.intelligence.Speech;
import com.ga.map.Global;
import com.ga.tool.Clips;
import com.ga.tool.EventTimer;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.skill.Adrenaline;
import com.ga.unit.skill.Skill;
import com.ga.unit.skill.Stat;

import java.awt.*;

/**
 * Created by Ga on 5/7/2016.
 */
public class Man extends Unit {
    public int height;
    public String speechType = "";
    public ClipMan clip;
    public Actions state = Actions.IDLE;
    public boolean jump = false;
    public Intelligence brain;
    public EventTimer delay = new EventTimer(new Performer(), 0, false);
    protected Weapon weapon;
    private Armor armor = new Armor(null);
    public Skill skill = new Adrenaline();
    private Speech speech = new Speech(this);
    public Stats stats = new Stats();
    protected HealthBar hp;
    public Man(String name, P pt) {
        super(name, pt, 10);
        clip = new ClipMan(this);
        height = clip.getHeight();
        brain = new Intelligence(this);
        hp = new HealthBar(get("health"), pos);
        setWeapon(ItemFactory.produceWeapon("Fist"));
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
        clip.organs[1].setStyle(p.color1, 9);
        clip.organs[2].setStyle(p.color2, 13);
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

    public void setState(Actions state) {
        if (!delay.over() || !jump) return;
        if (this.state != state) {
            String statreq = "health";
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
            if (get(statreq).require(req)){
                if (brain != null) brain.setState(state);
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
                    if (get("arms").get() < weapon.weight || get("legs").get() < 2)
                        clip.setState(Actions.EXHAUSTED);
                }
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
        damage = (int) Math.max(0,damage - armor.armor);
        if (damage > 0) {
            delay.set(0);
            damage = Global.r.nextInt(damage);
            float ratio;
            int x = hitter.pos.x - pos.x > 0 ? 1 : -1;
            face(x < 0 ? -1 : 1);
            ratio = (float) damage * 50 / get("health").max;
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
    }

    @Override
    public P getCollisionPoint(){
        return clip.organs[0].pt.getAdd(pos);
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
        height = 15;
        clip.setState(Actions.DEAD);
        Updater.setDepth(this, 9);
        delay.set(-1);
    }

    public void attack() {
        boolean hit;
        int r = 625, dam = weapon.damage + get("damage").get();
        if (state == Actions.WHIRLWIND) {
            r = (int) Math.max(r,(weapon.length*weapon.length));
            dam *= 2;
            hit = Updater.collide(this, clip.organs[ClipMan.FIST].ptd.getAdd(pos), dam, r, true);
        } else if (state == Actions.SWING) {
            hit = Updater.collide(this, weapon.getLine(), dam, true);
        } else {
            hit = Updater.collide(this, weapon.getPoint(), dam, r, false);
            if (hit) stats.endEffect("Cleave");
        }
        if (hit && brain != null) brain.attacked();
    }

    public void update() {
        stats.update();
        delay.update();
        clip.update();
        if (state == Actions.WALKL) vel.x = -get("speed").get();
        else if (state == Actions.WALKR) vel.x = get("speed").get();
        if (!weapon.name.equals("Fist")) {
            if (state == Actions.WHIRLWIND) {
                if (delay.time < 20) {
                    weapon.rotate(delay.time * -weapon.dir);
                    attack();
                }
            } else if (state == Actions.SWING) {
                weapon.rotate(weapon.dir + delay.time * .2f * -weapon.dir);
                if (delay.time < 12) attack();
            }
        }
        if (Global.map.collide(pos, vel, height)) jump = true;
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
}