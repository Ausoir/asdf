package com.ga.unit;

import com.ga.game.Settings;
import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.item.Armor;
import com.ga.unit.item.Projectile;
import com.ga.unit.item.Weapon;
import com.ga.unit.skill.*;

import java.awt.*;

import static com.ga.unit.BodyHumanoid.*;
import static com.ga.unit.skill.Actions.Idle;
import static com.ga.unit.skill.Skills.*;

/**
 * Created by Ga on 5/7/2016.
 */
public class Man extends Unit {
    protected Weapon weapon;
    protected Armor armor;
    protected Shadow shadow;

    public Man(String name, P pt, int seed) {
        super(name, pt, seed);
        shadow = new Shadow(pos);
        this.name = name;
        clip = new BodyHumanoid(this);
        speechType = name.toLowerCase();
        moveset.put("Melee",new Moveset[]{Kick, Weapon, Swing, Pierce});
        moveset.put("Range",new Moveset[]{Spells.Fireworks});
        moveset.put("Random",new Moveset[]{Spells.Adrenaline});
    }

    public Man(P pt){
        super(null,pt,0);
        shadow = new Shadow(pos);
        clip = new BodyHumanoid(this);
    }

    public void reset(){
        super.reset();
        Updater.addQueue.add(shadow);
    }

    public void setWeapon(Weapon p){
        weapon = p;
        weapon.pos = pos;
        weapon.setOrgan(clip.organs[FIST]);
        weapon.dir = dir;
        if (!p.name.equals("Fist")){
            clip.localAnimation[0] = clip.localAnimation[2];
        } else {
            clip.resetLocalAnimation(0);
        }
    }

    public void setArmor(Armor p){
        armor = p;
        if (!p.name.equals("Underwear")) {
            clip.organs[0].setImage("item/armor/" + p.name + "_skin",2);
            clip.organs[1].setImage("item/armor/" + p.name + "_skin",1);
            clip.organs[2].setImage("item/armor/" + p.name + "_skin",1);
            clip.organs[3].setImage("item/armor/" + p.name + "_skin",0);
            clip.organs[4].setImage("item/armor/" + p.name + "_skin",2);
        } else {
            clip.organs[0].image = null;
            clip.organs[1].image = null;
            clip.organs[2].image = null;
            clip.organs[3].image = null;
            clip.organs[4].image = null;
            clip.organs[5].setImage("unit/" + name);
        }
    }

    public Weapon getWeapon(){
        return weapon;
    }

    @Override
    public void remove() {
        super.remove();
        if (shadow != null) shadow.remove();
        if (speech != null) speech.remove();
    }

    public void rotateWeapon(P p){
        weapon.rotate(p);
    }

    public float getReach(){
        return weapon.getPoint().getSubtract(pos).getDist();
    }

    @Override
    public P getHand(){
        return pos.getAdd(clip.organs[FIST].ptd);
    }
    protected boolean stateSet(Moveset state){
        if (state == Weapon){
            setState(weapon.action);
            return true;
        }
        String statreq = state.getStatReq();
        float req = state.getCost();
        if (req < 0){
            if (statreq.equals("Stamina")) req = weapon.weight*-req;
            else if (statreq.equals("Energy")) req = (armor.weight+weapon.weight)*-req;
        }
        if (statreq == null || get(statreq).require((int) req)) {
            stats.unrest();
            super.stateSet(state);
            actionSet(state, 1);
            return true;
        }
        return false;
    }
    protected void actionSet(Moveset state, int level){
        if (state instanceof Actions) {
            switch ((Actions) state) {
                case Idle:
                    if (!weapon.name.equals("Fist")) {
                        if (!get("Stamina").isFull() || !get("Energy").isFull())
                            clip.setState(Actions.Exhausted);
                    }
                    break;
                case Duck:
                    vel.x = 0;
                    if (Global.map.fallSafe(pos.getAdd(vel))) pos.y += Settings.gravity;
                    break;
            }
        } else if (state instanceof Skills) {
            switch ((Skills) state) {
                case Jump:
                    vel.y -= 40 - (level * 2);
                    jump = false;
                    break;
                case Riposte:
                    damageMod = 1 + (level * .5f);
                    break;
                case Stab:
                    damageMod = 2 + level;
                    break;
                case Pierce:
                    damageMod = .5f + ((Math.abs(vel.x) * level) / 20);
                    break;
                case Lunge:
                    damageMod = level * .3f;
                    break;
                case Dash:
                    vel.x -= 10 * dir;
                    delay.time += 2 * level;
                    delay.max += 2 * level;
                    damageMod = level * .5f;
                    break;
                case Whirlwind:
                    delay.time += 35+(8 * level);
                    damageMod = .5f + ((float) level * .5f);
                    break;
                case Throw:
                    damageMod = level;
                    break;
                default:
                    damageMod = .5f + (level * .5f);
            }
        }
    }

    public boolean face(int dir) {
        if (super.face(dir)) {
            weapon.dir = dir;
            clip.setClip();
            return true;
        }
        return false;
    }

    @Override
    public int getDamage(){
        return (int) ((weapon.getDamage()+get("Proficiency").get())*damageMod);
    }

    @Override
    protected int getDamageReduction(){
        return (int) armor.getArmor();
    }

    @Override
    public boolean hit(Matter hitter, int damage) {
        armor.use(this);
        return super.hit(hitter, damage);
    }

    @Override
    public boolean isExhausted(){
        return get("Stamina").get() < weapon.weight && get("Energy").get() < armor.weight;
    }

    public Stat get(String p){
        return stats.getStat(p);
    }

    public void die() {
        say("@x");
        super.die();
        shadow.remove();
    }

    private void attack() {
        Matter hit;
        boolean cleave = false;
        if (state == Swing) cleave = true;
        else if (stats.triggerEffect(Buffs.Cleave)) cleave = true;
        Line hitbox = weapon.getLine();
        hit = Updater.collide(this, hitbox, cleave);
        if (hit != null){
            if (state == Riposte){
                cooldown.put(state,new int[]{Global.getTime()[0]+24});
            }
            if (brain != null) brain.attacked();
            if (stats.triggerEffect(Buffs.Empower)){
                Particle.explode("Blood",hitbox.ptd,P.getZero(),12,20,0,true,3);
                Particle.explode("Fire",hitbox.ptd,P.getZero(),30,8,0,false,3);
            }
        }
    }

    @Override
    protected void moveTriggered(int organ){
        if (organ == FIST) {
            if (state instanceof Skills) {
                switch ((Skills) state) {
                    case Stab:
                    case Pierce:
                    case Riposte:
                    case Lunge:
                        attack();
                        break;
                    case Dash:
                        vel.set(get("Speed").get() * dir * (damageMod / 2), -5);
                        break;
                    case Throw:
                        if (!weapon.name.equals("Fist")) {
                            Projectile p = new Projectile("item/weapon/" + weapon.name, pos.getAdd(clip.organs[OFIST].ptd), Global.player.mP.getAdd(pos), 50) {
                                @Override
                                public void stuck() {
                                    if (tangible) Particle.explode("Fire", pos, vel, 20, 10, 15, true, 3);
                                    super.stuck();
                                }
                            };
                            p.rotSpeed = .03f;
                            p.faction = faction;
                            p.damage = getDamage();
                            p.gravity = true;
                            cooldown.put(state, new int[]{Global.getTime()[0] + 30});
                        }
                        break;
                    case Leap:
                        if (jump) {
                            vel.y -= 80;
                            vel.x = get("Speed").get() * dir;
                            jump = false;
                        }
                        break;
                }
            }
        } else if (organ == FOOT && state == Kick){
            Updater.collide(this, clip.organs[FOOT].getAdd(this.pos), false);
        }
    }

    @Override
    protected void stateUpdate(){
        super.stateUpdate();
            if (state == Rest || state == Idle) stats.rest(armor.comfortability); //in addition to previous
            else if (state == Dash) attack();
            else if (state == Whirlwind) {
                weapon.rotation = delay.time * -weapon.dir;
                attack();
            } else if (state == Swing) {
                weapon.rotation = weapon.dir + delay.time * .2f * -weapon.dir;
                attack();
            }
    }

    public void drawDebug(Graphics2D g) {
        g.setColor(Color.white);
        P fist = clip.organs[FIST].ptd.getAdd(pos);
        P foot = clip.organs[FOOT].ptd.getAdd(pos);
        P t = Global.getRelPos((int) (clip.organs[0].pt.x + pos.x), (int) (clip.organs[0].pt.y + pos.y));
        g.drawArc((int)(t.x-(size.x/2)), (int) (t.y-(size.y/2)), (int)size.x, (int)size.y, 0, 360);
        t = Global.getRelPos((int) fist.x, (int) fist.y);
        g.drawArc((int) t.x, (int) t.y, 3, 3, 0, 360);
        t = Global.getRelPos((int) foot.x, (int) foot.y);
        g.drawArc((int) t.x, (int) t.y, 3, 3, 0, 360);
        g.setColor(Color.red);
        t = Global.getRelPos((int) weapon.getPoint().x, (int) weapon.getPoint().y);
        g.drawArc((int) t.x, (int) t.y, 3, 3, 0, 360);
        g.setColor(Color.blue);
        t = Global.getRelPos((int) (pos.x), (int) (pos.y));
        g.drawArc((int) t.x, (int) t.y, 5, 5, 0, 360);
    }

    @Override
    public void draw(Graphics2D g) {
        if (weapon != null) weapon.draw(g);
        super.draw(g);
//        drawDebug(g);
    }

    public Armor getArmor() {
        return armor;
    }

    public void revive() {
        super.revive();
        clip.setState(state);
    }

    @Override
    public String toString(){
        String r = super.toString();
        if (!r.equals("")) return "Man:"+r;
        return r;
    }
}