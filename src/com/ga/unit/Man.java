package com.ga.unit;

import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.factory.Stock;
import com.ga.unit.intelligence.Communication;
import com.ga.unit.intelligence.MerchantIntelligence;
import com.ga.unit.intelligence.UnitIntelligence;
import com.ga.unit.item.ArmorSet;
import com.ga.unit.item.Item;
import com.ga.unit.skill.*;

import java.awt.*;

import static com.ga.unit.BodyHumanoid.FIST;
import static com.ga.unit.BodyHumanoid.OFIST;
import static com.ga.unit.skill.Skills.Underhand;

/**
 * Created by Ga on 5/7/2016.
 */
public class Man extends Unit {
    private Item fist = ((Item) Stock.getByName("Fist",0)).cancelAdd();
    public Item weapon = fist;
    public ArmorSet armor;
    private String ground = "dirt";
    public Line rope;

    public Man(String p){
        super(p.split("#")[0]);
        clip = new BodyHumanoid(this);
        final Man me = this;
        clip.organs[FIST].onAmputate = new Performer(){
            public void perform(){
                if (clip.organs[FIST].amputated){
                    weapon.drop(me);
                    setWeapon(fist);
                }
            }
        };
        speechType = faction.name().toLowerCase();
        String[] t = p.split("#");
        if (t[1].equals("")) setWeapon(fist);
        else setWeapon((Item) Stock.get(t[1].split("@")));
        Updater.cancelAdd(weapon);
        armor = new ArmorSet(this, t[2]);
        if (faction == Faction.Merchant){
            brain = new MerchantIntelligence(this);
        } else if (faction != Faction.Player && faction != Faction.Player2) {
            brain = new UnitIntelligence(this);
            brain.setComm(new Communication());
        }
    }

    @Override
    public String toString(){
        String r = super.toString();
        if (!r.equals("")) r += "#"+weapon.forceToString()+"#"+armor.toString();
        return r;
    }

    public void setWeapon(Item p){
        if (p == null) p = fist;
        if (weapon != fist) weapon.field();
        weapon = p;
        weapon.pos = pos;
        weapon.setOrgan(clip.organs[FIST]);
        weapon.dir = dir;
    }

    public boolean wieldingNone(){ return weapon == null || weapon == fist; }

    @Override
    public void remove() {
        super.remove();
        if (speech != null) speech.remove();
    }

    public void rotateWeapon(P p){
        weapon.rotate(p);
    }

    public float getReach(){
        return weapon.getPoint().getSubtract(pos).getDist();
    }

    void stepSound(){
        if (Sound.has("step_"+ground)) Sound.play("step_"+ground);
        else Sound.play("step_dirt");
    }

    public void attackSound(){
        Sound.play(weapon.getSound());
    }

    @Override
    public void attackHitSound(){
        Sound.play(weapon.getHitSound());
    }

    @Override
    public P getHand(){
        if (!clip.organs[FIST].amputated) return pos.getAdd(clip.organs[FIST].ptd);
        else if (!clip.organs[OFIST].amputated) return pos.getAdd(clip.organs[OFIST].ptd);
        else return super.getHand();
    }
    @Override
    protected boolean castable(){
        return !clip.organs[FIST].amputated || !clip.organs[OFIST].amputated;
    }
    public void stateSet(Moveset state){
        super.stateSet(state);
        if (state == Actions.Use){
            if (!clip.organs[FIST].amputated) weapon.use(this);
            return;
        } else if (state instanceof Skills) {
            int speed = weapon.speed-armor.getEncumberance();
            damageType = weapon.damageType;
            switch ((Skills) state) {
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
                    damageMod = 3;
                    damageType = DamageType.Push;
                    break;
                case Lunge:
                    damageMod = -1;
                    break;
                case Slash:
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
                case Pierce:
                    damageMod = .5f + (Math.abs(vel.x) / 20) + (stateLevel*.5f);
                    break;
                case Dash:
                    vel.x -= getSpeed() * 2 * dir;
                    delay.time += 2 * stateLevel;
                    delay.max += 2 * stateLevel;
                    damageMod = stateLevel * .5f;
                    break;
                case Whirlwind:
                    delay.time += 8 * stateLevel;
                    damageMod = .5f + ((float) stateLevel * .5f);
                    delay.performer = new Performer(){
                        public void perform(){
                            setState(Skills.Rest);
                        }
                    };
                    break;
                case Throw:
                    damageMod = stateLevel;
                    break;
                case Swing:
                    damageMod = 1+(stateLevel*.5f);
                    speed = Math.min(10,speed);
                    break;
                default:
                    damageMod = .5f + (stateLevel * .5f);
            }
            delay.set(Math.max(3, delay.time - speed));
            clip.setSpeed(speed);
        } else if (state instanceof Spells){
            weapon.rotation = target.getAtan()+(dir<0?-3.14f:0);
            clip.setSpeed(weapon.speed-armor.getEncumberance());
            damageMod = 1;
        }
    }

    public boolean face(int dir) {
        if (super.face(dir)) {
            weapon.dir = dir;
            weapon.rotation *= -1;
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

    @Override
    public boolean hit(Matter hitter, int damage, DamageType type) {
        target = hitter.pos.getAdd(0,0);
        armor.onHit();
        return super.hit(hitter, damage, type);
    }

    @Override
    public boolean isExhausted(){
        return get("Stamina").get() < weapon.weight+armor.getWeight();
    }

    public Stat get(String p){
        return stats.getStat(p);
    }

    public void die() {
        say("@x");
        weapon.drop(this);
        super.die();
    }

    public void attack() {
        Matter hit;
        boolean cleave = false;
        if (stats.triggerEffect(Buffs.Cleave)) cleave = true;
        Line hitbox = weapon.getLine();
        hit = Updater.collide(this, hitbox, cleave);
        if (hit != null){
            weapon.use(1);
            if (state == Underhand) get("Stamina").add(4);
            if (brain != null) brain.attacked();
            if (stats.triggerEffect(Buffs.Empower)){
                Particle.explode("Blood",hitbox.ptd,new P(),12,20,0,true,3);
                Particle.explode("Fire",hitbox.ptd,new P(),30,8,0,false,3);
            }
        }
    }

    @Override
    public void attackDenied(){
        weapon.dir *= -1;
    }

    @Override
    public boolean checkHit(Matter hitter, Line p){
        if (state == Skills.Guard) {
            if (faction.canAttack(hitter.faction)) {
                if (hittable()) {
                    if (p.lineCross(weapon.getLine())) {
                        hitter.attackDenied();
                        return false;
                    }
                }
            }
        }
        return super.checkHit(hitter, p);
    }

    @Override
    protected void moveTriggered(int organ){
        weapon.rotate(target.getMult(dir, dir));
        weapon.rotation += .1f * dir;
        if (state instanceof Skills) {
            ((Skills)state).trigger(this, organ);
        }
    }

    @Override
    public void onCollide(Line l){
        boolean wasJump = jump;
        super.onCollide(l);
        if (!wasJump){
            if (l.next != -1) ground = Global.map.getLand(l.next);
            if (state == Skills.Jump){
                stateSet(Actions.Hit);
                delay.set(4);
            }
        }
    }

    public void setVisible(boolean p) {
        super.setVisible(p);
        weapon.image.visible = p;
    }

    @Override
    public void draw(Graphics2D g) {
        weapon.draw(g);
        super.draw(g);
    }

    public void revive() {
        super.revive();
        clip.setState(state);
    }
}