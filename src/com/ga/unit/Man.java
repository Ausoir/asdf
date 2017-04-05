package com.ga.unit;

import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.item.ArmorSet;
import com.ga.unit.item.Item;
import com.ga.unit.skill.*;

import java.awt.*;

import static com.ga.unit.BodyHumanoid.FIST;
import static com.ga.unit.BodyHumanoid.OFIST;
import static com.ga.unit.skill.Actions.Underhand;

/**
 * Created by Ga on 5/7/2016. asd
 */
public class Man extends Unit {
    private Item fist = ((Item) Stock.getByName("Fist",0)).cancelAdd();
    public Item weapon = fist;
    public ArmorSet armor;
    public Line rope;

    public Man(int seed) {
        super(seed);
        clip = new BodyHumanoid(this);
    }
    public void load(String p){
        super.load(p.split("#")[0]);
        final Man me = this;
        clip.organs[FIST].onAmputate = new Performer(){
            public void perform(){
                if (clip.organs[FIST].amputated){
                    weapon.drop(me);
                    setWeapon(fist);
                }
            }
        };
        String[] t = p.split("#");
        if (t[1].equals("")) setWeapon(fist);
        else setWeapon((Item) Stock.get(t[1]));
        armor = new ArmorSet(this, t[2]);
    }

    public void setHead(String p){
        clip.organs[5].setImage(p);
    }

    public void setWeapon(Item p){
        if (p == null) p = fist;
        if (weapon != fist){
            weapon.onUnequip(this);
        }
        weapon = p;
        weapon.pos = pos;
        weapon.setOrgan(clip.organs[FIST]);
        weapon.dir = dir;
    }

    public void setDepth(int depth){
        super.setDepth(depth);
        weapon.setDepth(depth-1);
    }

    public boolean wieldingNone(){ return weapon == null || weapon == fist; }

    @Override
    public void remove() {
        super.remove();
        if (speech != null) speech.remove();
    }

    public void rotateWeapon(P p){
        weapon.rotateTo(p);
    }

    public float getReach(){
        return weapon.getPoint().getSubtract(pos).getDist();
    }

    void stepSound(){
        if (Global.camera.getSubtract(pos).getSum() < 1000) {
            String ground = Global.map.getLand(lastLand.next);
            if (Sound.has("step_" + ground)) Sound.play("step_" + ground);
            else Sound.play("step_dirt");
        }
    }

    public void attackSound(){
        Sound.play(weapon.getSound());
    }

    @Override
    public P getHand(){
        if (!clip.organs[FIST].amputated) return pos.getAdd(clip.organs[FIST].ptd);
        else if (!clip.organs[OFIST].amputated) return pos.getAdd(clip.organs[OFIST].ptd);
        else return super.getHand();
    }

    public void stateSet(Moveset state){
        super.stateSet(state);
        if (state instanceof Actions) {
            int speed = weapon.speed-armor.getEncumberance()-weapon.weight;
            damageType = weapon.damageType;
            switch ((Actions) state) {
                case Use:
                    if (!clip.organs[FIST].amputated) weapon.use(this);
                    break;
                case Duck:
                case Sprint:
                case Jump:
                    speed = 0;
                    break;
                case Airstrike:
                    speed = 0;
                    vel.y -= 20;
                    break;
                case Guard:
                    weapon.rotation = target.getAtan()+(dir<0?-3.14f:0);
                    speed = 0;
                    break;
                case Sidekick:
                    speed = 0-armor.armor[clip.FOOT].encumberance;
                    damageType = DamageType.Bash;
                    break;
                case Kick:
                    speed = 0;
                    damageType = DamageType.Push;
                    break;
                case Lunge:
                    damageMod = -1;
                    break;
                case Triple:
                    damageMod = 1 + stateLevel;
                    weapon.rotation = target.getAtan()+(dir<0?-3.14f:0);
                    speed = 0;
                    break;
                case Backstep:
                    vel.x -= getSpeed() * 5 * dir;
                    break;
                case Underhand:
                    damageMod = 1 + (stateLevel * .5f);
                    break;
                case Stab:
                    damageMod = 2 + stateLevel;
                    break;
                case Thrust:
                    damageMod = .5f + (Math.abs(vel.x) / 20) + (stateLevel*.5f);
                    break;
                case Dash:
                    vel.x -= getSpeed() * dir;
                    delay.time += 2 * stateLevel;
                    delay.max += 2 * stateLevel;
                    damageMod = stateLevel * .5f;
                    break;
                case Whirlwind:
                    delay.time += 8 * stateLevel;
                    damageMod = .5f + ((float) stateLevel * .5f);
                    delay.performer = new Performer(){
                        public void perform(){
                            setState(Actions.Rest);
                        }
                    };
                    break;
                case Throw:
                    damageMod = stateLevel;
                    break;
                case Slash:
                    damageMod = 1+(stateLevel*.5f);
                    speed = Math.min(10,speed);
                    break;
                default:
                    damageMod = .5f + (stateLevel * .5f);
            }
            if (delay.max != 0) delay.set(Math.max(3, delay.time - speed));
            clip.setSpeed(speed);
        } else if (state instanceof Spells){
            weapon.rotation = target.getAtan()+(dir<0?-3.14f:0);
            clip.setSpeed(weapon.speed-armor.getEncumberance());
            damageMod = 1;
        }
    }

    @Override
    public boolean face(int dir, boolean force) {
        if (super.face(dir, force)) {
            if (weapon.dir != dir) {
                weapon.dir = dir;
                weapon.rotation *= -1;
            }
            armor.armor[0].dir = dir;
            clip.setClip();
            return true;
        }
        return false;
    }

    @Override
    public int getDamage(){
        return (int) Math.max(0,((weapon.getDamage()+get("Proficiency").get())*damageMod)-armor.armor[FIST].encumberance+armor.armor[FIST].damage);
    }

    @Override
    protected int getDamageReduction(int organ){
        return (int) armor.getArmor(organ);
    }

    void restorePoise(){
        super.restorePoise();
        poise += armor.getArmor(3)*8;
    }

    @Override
    public boolean hit(Line hitbox, int damage, DamageType type) {
        boolean r = super.hit(hitbox, damage, type);
        if (r){
            target = hitbox.pt.getAdd(0,0);
            armor.onHit();
        }
        return r;
    }

    public Stat get(String p){
        return stats.getStat(p);
    }

    public void die() {
        say("@x");
        weapon.drop(this);
        super.die();
    }

    public boolean attack() {
        boolean cleave = false;
        if (stats.triggerEffect(Buffs.Cleave)) cleave = true;
        Line hitbox = weapon.getLine();
        Unit target = null;
        for (Unit i : Updater.collideUnit(pos, (int) hitbox.getLength(), faction)){
            if (i.checkHit(hitbox,getDamage(),damageType)){
                target = i;
                if (!cleave) break;
            }
        }
        if (target != null){
            if (weapon.use(1)) weapon.trigger(target);
            Sound.play(weapon.getHitSound());
            if (state == Underhand) get("Stamina").add(4);
            if (brain != null) brain.attacked();
            if (stats.triggerEffect(Buffs.Empower)){
                Particle.explode("Blood",hitbox.ptd,new P(),12,20,0,true,3);
                Particle.explode("Fire",hitbox.ptd,new P(),30,8,0,false,3);
            }
            return true;
        }
        return false;
    }

    public boolean checkHit(Line hitBox, int damage, DamageType type) {
        boolean r;
        if (r = super.checkHit(hitBox, damage, type)) {
            if (clip.organs[FIST].amputated && clip.organs[OFIST].amputated){
                tags.remove(Tag.Arms);
            }
        }
        return r;
    }

    @Override
    protected void moveTriggered(int organ){
        if (state instanceof Actions) {
            if (state == Actions.Kick || state == Actions.Sidekick){
                Line hitbox = clip.organs[organ].getAdd(pos);
                int damage = armor.armor[1].getDamage()+1;
                for (Unit i : Updater.collideUnit(pos, (int) hitbox.getLength(), faction))
                    i.checkHit(hitbox,damage,damageType);
            } else state.trigger(this, organ);
        }
    }

    public void setVisible(boolean p) {
        super.setVisible(p);
        weapon.image.visible = p;
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);
        armor.armor[0].pos.set(pos.getAdd(clip.organs[5].pt));
        armor.armor[0].rotation = clip.organs[5].getAtan()+1.57f;
        armor.armor[0].draw(g);
    }

    public String getDescription(){
        String r = super.getDescription()+"\n";
        if (!wieldingNone()) r += "*Wielding - "+weapon.getFullName()+"\n";
        r += "*Wearing - "+armor.getDescription()+"\n";
        return r;
    }

    @Override
    public String toString(){
        String r = super.toString();
        if (!r.equals("")) r += "#"+weapon.forceToString()+"#"+armor.toString();
        return r;
    }
}