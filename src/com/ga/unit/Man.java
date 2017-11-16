package com.ga.unit;

import com.ga.game.Sound;
import com.ga.map.Map;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.item.ArmorSet;
import com.ga.unit.item.Item;
import com.ga.unit.skill.Buffs;
import com.ga.unit.skill.Stat;

import java.awt.*;

import static com.ga.unit.BodyHumanoid.*;

/**
 * Created by Ga on 5/7/2016. asd
 */
public class Man extends Unit {
    private String face;
    protected Item fist;
    public Item weapon = fist;
    public ArmorSet armor;

    public Man(Map map, String name, int seed) {
        super(map, name, seed);
        clip = new BodyHumanoid(this);
    }

    public void setWeapon(Item p){
        if (p == null) p = fist;
        weapon.onUnequip(this);
        weapon = p;
        weapon.onEquip(this);
        weapon.pos = new P(pos);
        weapon.dir = dir;
    }

    public boolean wieldingNone(){ return weapon == null || weapon == fist; }

    @Override
    public void remove() {
        super.remove();
        weapon.remove();
        armor.remove();
    }

    public float getReach(){
        return weapon.getSize();
    }

    void stepSound(){
        String ground = map.getLand(lastLand.tid);
        if (Sound.has("step_" + ground)) Sound.play(map, pos, "step_" + ground);
        else Sound.play(map, pos, "step_dirt");
    }

    public void attackSound(){
        Sound.play(map, pos, "swing_normal");
    }

    @Override
    public P getHand(){
        if (!clip.organs[FIST].amputated) return pos.getAdd(clip.organs[FIST].ptd);
        else if (!clip.organs[OFIST].amputated) return pos.getAdd(clip.organs[OFIST].ptd);
        else return super.getHand();
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
        return (int) Math.max(0,weapon.getDamage()*damageMod);
    }

    @Override
    protected int processDamage(int organ, int damage){
        return armor.getOrganArmor(organ).onHit(damage);
    }

    public int getVacancy(){
        return 99;
    }

    public Stat get(String p){
        return stats.getStat(p);
    }

    public void die() {
        say("@x");
        if (rand.nextBoolean()) weapon.drop(this);
        weapon.remove();
        super.die();
    }

    public void onAttack(Unit p){
        if (weapon.usable(2)) weapon.trigger(p);
        brain.onAttack();
    }

    public Unit attack() {
        boolean cleave = false;
        if (stats.triggerEffect(Buffs.Cleave)) cleave = true;
        Line hitbox = weapon.getLine();
        Unit target = null;
        for (Unit i : map.updater.collideUnit(pos, (int) hitbox.getLength()+20, faction)){
            if (i.checkHit(hitbox,getDamage(),weapon.type.isBlunt())){
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
    public void setDepth(float p){
        super.setDepth(p);
        if (weapon != null) weapon.setDepth(p-.0001f);
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

    private void setFace(String p){
        face = p;
        clip.organs[5].setImage(p);
    }

    public void disconnect(){
        super.remove();
        weapon.disconnect();
    }

    public void start(){
        super.start();
        if (!wieldingNone()){
            weapon.map = map;
            weapon.start();
        }
    }

    @Override
    public String[] load(String p){
        String[] t = p.split("#");
        setFace(t[1]);
        String[] r = super.load(t[0]);
        weapon = fist = ((Item)Stock.Fist.create(map, -1,0));
        if (!t[2].equals("")) setWeapon((Item) Stock.get(map, t[2]));
        armor = new ArmorSet(this, t[3]);
        final Man me = this;
        clip.organs[FIST].onAmputate = new Performer(){
            public void perform(){
                weapon.drop(me);
                setWeapon(null);
            }
        };
        clip.organs[HEAD].onAmputate = new Performer(){
            public void perform(){
                armor.set(null, 0);
            }
        };
        return r;
    }

    @Override
    public String toString(){
        String r = super.toString() + "#"+face+"#"+weapon.forceToString()+"#"+armor.toString();
        return r;
    }
}