package com.ga.unit;

import com.ga.map.Global;
import com.ga.tool.EventTimer;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.intelligence.Communication;
import com.ga.unit.intelligence.MerchantIntelligence;
import com.ga.unit.intelligence.UnitIntelligence;
import com.ga.unit.item.ArmorSet;
import com.ga.unit.item.Item;
import com.ga.unit.item.ItemFactory;
import com.ga.unit.item.Projectile;
import com.ga.unit.skill.*;

import java.awt.*;

import static com.ga.unit.BodyHumanoid.FIST;
import static com.ga.unit.BodyHumanoid.OFIST;
import static com.ga.unit.skill.Skills.*;

/**
 * Created by Ga on 5/7/2016.
 */
public class Man extends Unit {
    private Item fist = ItemFactory.produce("Fist",0,0);
    protected Item weapon;
    public ArmorSet armor;
    public Man(String p){
        super(p);
        clip = new BodyHumanoid(this);
        final Man me = this;
        clip.organs[FIST].onAmputate = new Performer(){
            public void perform(){
                if (me.weapon.getOrgan().amputated){
                    me.weapon.drop(me);
                    me.setWeapon(fist);
                }
            }
        };
        speechType = faction.name().toLowerCase();
        String[] t = p.split("@");
        if (t[7].startsWith("Fist,")) setWeapon(fist);
        else setWeapon(ItemFactory.load(t[7]));
        armor = new ArmorSet(this, t[8]);
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
        if (!r.equals("")){
            r += "@"+weapon.toItemString()+"@"+armor.toItemString();
        }
        return r;
    }

    public void setWeapon(Item p){
        if (p == null){
            if (weapon != null) {
                weapon.field();
                weapon.remove();
            }
            p = fist;
        }
        weapon = p;
        weapon.pos = pos;
        weapon.setOrgan(clip.organs[FIST]);
        weapon.dir = dir;
    }

    public boolean wieldingNone(){ return weapon == null || weapon == fist; }

    public Item getWeapon(){
        return weapon;
    }

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
    protected void stateSet(Moveset state){
        super.stateSet(state);
        if (state instanceof Skills) {
            int speed = weapon.speed-armor.getEncumberance();
            damageType = weapon.damageType;
            switch ((Skills) state) {
                case Use:
                    if (!clip.organs[FIST].amputated) weapon.use(this);
                    return;
                case Duck:
                case Guard:
                case Sprint:
                case Jump:
                case Airstrike:
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
                    delay.time += 35+(8 * stateLevel);
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
        boolean r = super.hit(hitter, damage, type);
        return r;
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

    private void attack() {
        Matter hit;
        boolean cleave = false;
        if (stats.triggerEffect(Buffs.Cleave)) cleave = true;
        Line hitbox = weapon.getLine();
        hit = Updater.collide(this, hitbox, cleave);
        if (hit != null){
            if (state == Underhand){
                get("Stamina").add(4);
            }
            if (brain != null) brain.attacked();
            if (stats.triggerEffect(Buffs.Empower)){
                Particle.explode("Blood",hitbox.ptd,new P(),12,20,0,true,3);
                Particle.explode("Fire",hitbox.ptd,new P(),30,8,0,false,3);
            }
        }
    }

    private void swingAttack(float rot1, float rot2){
        if (rot1 > rot2){
            float t = rot1;
            rot1 = rot2;
            rot2 = t;
        }
        for (float i=rot1;i<rot2;i+=.2f){
            weapon.rotation = i;
            attack();
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
        if (organ == FIST) {
            weapon.rotate(target.getMult(dir, dir));
            weapon.rotation += .1 * dir;
            if (state instanceof Skills) {
                switch ((Skills) state) {
                    case Slash:
                        weapon.rotation = delay.time * -.3f * -weapon.dir;
                        vel.set(dir * Global.cos(weapon.rotation)*15, -1);
                    case Stab:
                    case Pierce:
                    case Lunge:
                    case Whirlwind:
                        attack();
                        break;
                    case Underhand:
                        vel.set(5*dir,-1);
                        attack();
                        break;
                    case Dash:
                        vel.set(getSpeed() * 10 * dir * (damageMod / 2), -5);
                        break;
                    case Throw:
                        if (weapon != fist) {
                            Projectile p = new Projectile(null, pos.getAdd(clip.organs[OFIST].ptd), pos.getAdd(target), 50) {
                                Item wpn = weapon;
                                @Override
                                public void stuck() {
                                    if (tangible) Particle.groundSlam(pos);
                                    wpn.field();
                                    wpn.pos = new P(pos);
                                    wpn.rotation = rotation;
                                    remove();
                                }
                            };
                            p.image = weapon.image;
                            p.rotSpeed = .03f;
                            p.faction = faction;
                            p.damage = getDamage()+weapon.weight;
                            weapon.remove();
                            setWeapon(null);
                        }
                        break;
                    case Leap:
                        if (jump) {
                            vel.y -= 80;
                            vel.x = getSpeed() * 10 * dir;
                            jump = false;
                        }
                        break;
                    case Shoot:
                    case Snipe:
                        P tip = weapon.getPoint();
                        final Projectile p = new Projectile("Bullet",tip,pos.getAdd(target), (int) (150+(rand.nextFloat()*50))){
                            @Override
                            public void onCollide(Line l){
                                remove();
                            }
                        };
                        p.delay = new EventTimer(4,false,new Performer(){
                            public void perform(){
                                p.remove();
                            }
                        });
                        if (state == Shoot) p.delay.set(2);
                        p.dir = dir;
                        p.rotSpeed = 0;
                        p.rotate(target.getSubtract(tip).add(pos));
                        p.faction = faction;
                        p.damage = (weapon.getDamage()*10)+4;
                        p.gravity = false;
                        Particle.explode("Spark", tip, p.vel, 5, 10, 15, false,3);
                        float start = rand.nextFloat();
                        p.pos.add(p.vel.getMult(start, start));
                        getWeapon().rotation += dir * -.3f;
                        break;
                }
            }
        } else if ((organ == clip.FOOT && state == Sidekick) || (organ == clip.FOOT-1 && state == Kick)){
            for (Matter i : Updater.collide(clip.organs[organ].ptd.getAdd(this.pos), 50)){
                if (i instanceof Unit) {
                    if (faction.canAttack(i.faction)) {
                        if (i.hittable()) ((Unit)i).hit(this, (int) ((armor.armor[clip.FOOT].getDamage()+1)*damageMod), damageType);
                    }
                }
            }
        }
    }

    @Override
    public void onCollide(Line l){
        boolean wasJump = jump;
        super.onCollide(l);
        if (!wasJump){
            if (state == Skills.Jump){
                setState(Actions.Hit);
                delay.set(4);
            }
        }
    }

    @Override
    protected void stateUpdate(){
        super.stateUpdate();
        if (state instanceof Skills) {
            switch ((Skills) state){
                case Guard:
                    weapon.rotation = target.getAtan()+(dir<0?-3.14f:0);
                    break;
                case Swing:
                    if (delay.time <= 25 && delay.time > 12) {
                        int t = 25 - delay.time;
                        float rot = -((weapon.dir * 10) + t * t * .03f * -weapon.dir);
                        if (Math.abs(rot) < 8) swingAttack(weapon.rotation, rot);
                        weapon.rotation = rot;
                    }
                    break;
                case Airstrike:
                    if (jump){
                        weapon.rotation = 1f*dir;
                        attack();
                        break;
                    }
                case Whirlwind:
                    weapon.rotation = delay.time * -weapon.dir;
                    break;
                case Dash:
                    if (!jump) attack();
                    break;
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