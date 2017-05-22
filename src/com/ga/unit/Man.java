package com.ga.unit;

import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.Updater;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.item.ArmorSet;
import com.ga.unit.item.Item;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Buffs;
import com.ga.unit.skill.Stat;

import java.awt.*;

import static com.ga.unit.BodyHumanoid.FIST;
import static com.ga.unit.BodyHumanoid.OFIST;

/**
 * Created by Ga on 5/7/2016. asd
 */
public class Man extends Unit {
    protected Item fist = ((Item) Stock.getByName("Fist",0)).cancelAdd();
    public Item weapon = fist;
    public ArmorSet armor;
    public Line rope;

    public Man(int seed) {
        super(seed);
        clip = new BodyHumanoid(this);
    }

    public void setHead(String p){
        clip.organs[5].setImage(p);
    }

    public void setWeapon(Item p){
        if (p == null) p = fist;
        weapon.onUnequip(this);
        weapon = p;
        weapon.onEquip(this);
        weapon.pos = new P(pos);
        weapon.dir = dir;
    }

    public void setDepth(int depth){
        super.setDepth(depth);
        weapon.setDepth(depth);
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
        return weapon.getSize();
    }

    void stepSound(){
        if (Global.camera.getSubtract(pos).getSum() < 1000) {
            String ground = Global.map.getLand(lastLand.next);
            if (Sound.has("step_" + ground)) Sound.play("step_" + ground);
            else Sound.play("step_dirt");
        }
    }

    public void attackSound(){
        Sound.play("swing_normal");
    }

    @Override
    public P getHand(){
        if (!clip.organs[FIST].amputated) return pos.getAdd(clip.organs[FIST].ptd);
        else if (!clip.organs[OFIST].amputated) return pos.getAdd(clip.organs[OFIST].ptd);
        else return super.getHand();
    }

    public void stateSet(Actions state){
        attackSpeed = -armor.getEncumberance()-weapon.weight;
        super.stateSet(state);
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
        return (int) Math.max(0,(weapon.getDamage()*damageMod)-armor.armor[FIST].encumberance);
    }

    @Override
    protected int getDamageReduction(int organ){
        return (int) armor.getArmor(organ);
    }

    @Override
    public void hit(int dir, float knockback) {
        super.hit(dir, knockback);
        armor.onHit();
    }

    public Stat get(String p){
        return stats.getStat(p);
    }

    public void die() {
        say("@x");
        weapon.drop(this);
        super.die();
    }

    public void onAttack(Unit p){
        if (weapon.usable(1)) weapon.trigger(p);
        if (brain != null) brain.onAttack();
    }

    public Unit attack() {
        boolean cleave = false;
        if (stats.triggerEffect(Buffs.Cleave)) cleave = true;
        Line hitbox = weapon.getLine();
        Unit target = null;
        for (Unit i : Updater.collideUnit(pos, (int) hitbox.getLength()+20, faction)){
            if (i.checkHit(hitbox,getDamage())){
                target = i;
                if (!cleave) break;
            }
        }
        if (target != null) {
            onAttack(target);
            return target;
        }
        return null;
    }

    public boolean checkHit(Line hitBox, int damage) {
        boolean r;
        if (r = super.checkHit(hitBox, damage)) {
            if (clip.organs[FIST].amputated && clip.organs[OFIST].amputated){
                tags.remove(Tag.Aiming);
            }
        }
        return r;
    }

    @Override
    protected void moveTriggered(int organ){
        weapon.pos.set(pos.x + clip.organs[FIST].ptd.x, pos.y + clip.organs[FIST].ptd.y);
        super.moveTriggered(organ);
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
    public void update(){
        super.update();
        weapon.pos.set(pos.x + clip.organs[FIST].ptd.x, pos.y + clip.organs[FIST].ptd.y);
    }

    @Override
    public String[] load(String p){
        String[] t = p.split("#");
        if (t[1].equals("")) setWeapon(fist);
        else setWeapon((Item) Stock.get(t[1]));
        armor = new ArmorSet(this, t[2]);
        String[] r = super.load(t[0]);
        final Man me = this;
        clip.organs[FIST].onAmputate = new Performer(){
            public void perform(){
                if (clip.organs[FIST].amputated){
                    weapon.drop(me);
                    setWeapon(fist);
                }
            }
        };
        return r;
    }

    @Override
    public String toString(){
        String r = super.toString();
        if (!r.equals("")) r += "#"+weapon.forceToString()+"#"+armor.toString();
        return r;
    }
}