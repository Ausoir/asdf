package com.ga.unit;

import com.ga.map.Global;
import com.ga.tool.EventTimer;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.intelligence.Communication;
import com.ga.unit.intelligence.MerchantIntelligence;
import com.ga.unit.intelligence.UnitIntelligence;
import com.ga.unit.item.Armor;
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
    protected Item weapon;
    protected Armor armor;

    public Man(String p){
        super(p);
        clip = new BodyHumanoid(this);
        speechType = faction.name().toLowerCase();
        String[] t = p.split("@");
        setWeapon(ItemFactory.load(t[6]));
        setArmor((Armor) ItemFactory.load(t[7]));
        if (faction == Faction.Merchant){
            brain = new MerchantIntelligence(this);
        } else if (faction != Faction.Player && faction != Faction.Player2) {
            brain = new UnitIntelligence(this);
            brain.setComm(new Communication());
            if (!wieldingNone()) moveset.put("Melee",new Moveset[]{Kick, Use, Swing, Pierce, Sidekick, Jump});
            moveset.put("Range",new Moveset[]{Spells.Stun, Spells.Fireball, Spells.Fireblast});
            moveset.put("Random",new Moveset[]{Spells.Haste});
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
            weapon.field();
            weapon.remove();
            p = ItemFactory.produce("Fist",0);
        }
        weapon = p;
        weapon.pos = pos;
        weapon.setOrgan(clip.organs[FIST]);
        weapon.dir = dir;
    }

    public void setArmor(Armor p){
        if (p == null) p = (Armor) ItemFactory.produce("Underwear",0);
        armor = p;
        if (!equippingNone()) {
            clip.organs[0].setImage("item/armor/"+p.name + "_skin",2);
            clip.organs[1].setImage("item/armor/"+p.name + "_skin",1);
            clip.organs[2].setImage("item/armor/"+p.name + "_skin",1);
            clip.organs[3].setImage("item/armor/"+p.name + "_skin",0);
            clip.organs[4].setImage("item/armor/"+p.name + "_skin",2);
        } else {
            clip.organs[0].setImage("unit/Man_skin",2);
            clip.organs[1].setImage("unit/Man_skin",1);
            clip.organs[2].setImage("unit/Man_skin",1);
            clip.organs[3].setImage("unit/Man_skin",0);
            clip.organs[4].setImage("unit/Man_skin",2);
            clip.organs[5].setImage(name);
        }
    }

    public boolean wieldingNone(){ return weapon == null || weapon.name.equals("Fist"); }
    public boolean equippingNone(){ return armor == null || armor.name.equals("Underwear"); }

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
        return pos.getAdd(clip.organs[FIST].ptd);
    }
    protected void actionSet(Moveset state, int level){
        super.actionSet(state, level);
        if (state instanceof Skills) {
            int speed = weapon.speed;
            damageType = weapon.damageType;
            switch ((Skills) state) {
                case Use:
                    weapon.use(this);
                    return;
                case Duck:
                    cooldown.put(state,new int[]{Global.getTime()[0]+(getStatPenalty()*4)});
                    break;
                case Sidekick:
                    speed = 0;
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
                    damageMod = 1 + level;
                    speed = 0;
                    break;
                case Backstep:
                    vel.x -= getSpeed() * 5 * dir;
                    break;
                case Underhand:
                    damageMod = 1 + (level * .5f);
                    break;
                case Stab:
                    damageMod = 2 + level;
                    break;
                case Pierce:
                    damageMod = .5f + ((Math.abs(vel.x) * level) / 20);
                    break;
                case Dash:
                    vel.x -= getSpeed() * 2 * dir;
                    delay.time += 2 * level;
                    delay.max += 2 * level;
                    damageMod = level * .5f;
                    break;
                case Whirlwind:
                    delay.time += 35+(8 * level);
                    damageMod = .5f + ((float) level * .5f);
                    delay.performer = new Performer(){
                        public boolean perform(){
                            setState(Skills.Rest);
                            return true;
                        }
                    };
                    break;
                case Throw:
                    damageMod = level;
                    break;
                case Swing:
                    damageMod = 1+(level*.5f);
                    speed = Math.min(10,speed);
                    break;
                default:
                    damageMod = .5f + (level * .5f);
            }
            delay.set(Math.max(3, delay.time - speed));
            clip.setSpeed(speed);
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
        return (int) ((weapon.getDamage()+get("Proficiency").get())*damageMod);
    }

    @Override
    protected int getDamageReduction(){
        return (int) armor.getArmor();
    }

    @Override
    protected int getStatPenalty() {
        return armor.weight+weapon.weight;
    }

    @Override
    public boolean hit(Matter hitter, int damage, DamageType type) {
        target = hitter.pos.getAdd(0,0);
        armor.use(this);
        boolean r = super.hit(hitter, damage, type);
        if (weapon.getOrgan().amputated){
            Item wpn = weapon;
            setWeapon(null);
            wpn.pos = new P(pos);
            wpn.field();
            wpn.gravity = false;
        }
        return r;
    }

    @Override
    public boolean isExhausted(){
        return get("Stamina").get() < weapon.weight+armor.weight;
    }

    public Stat get(String p){
        return stats.getStat(p);
    }

    public void die() {
        say("@x");
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
                cooldown.put(state,new int[]{Global.getTime()[0]+24});
            }
            if (brain != null) brain.attacked();
            if (stats.triggerEffect(Buffs.Empower)){
                Particle.explode("Blood",hitbox.ptd,new P(),12,20,0,true,3);
                Particle.explode("Fire",hitbox.ptd,new P(),30,8,0,false,3);
            }
        }
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
                        if (!weapon.name.equals("Fist")) {
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
                            p.setImage(weapon.name);
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
                            public boolean perform(){
                                p.remove();
                                return true;
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
        } else if (organ == clip.FOOT && state == Sidekick){
            Updater.collide(this, clip.organs[organ].getAdd(this.pos), false);
        } else if (organ == clip.FOOT-1 && state == Kick){
            Updater.collide(this, clip.organs[organ].getAdd(this.pos), false);
        }
    }

    @Override
    protected void stateUpdate(){
        super.stateUpdate();
        if (state instanceof Skills) {
            switch ((Skills) state){
                case Swing:
                    if (delay.time <= 25 && delay.time > 12) {
                        int t = 25 - delay.time;
                        float rot = -((weapon.dir * 10) + t * t * .03f * -weapon.dir);
                        weapon.rotation = rot;
                        attack();
                    }
                    break;
                case Jump:
                    if (jump){
                        weapon.rotation = 1f*dir;
                        attack();
                        break;
                    }
                case Whirlwind:
                    weapon.rotation = delay.time * -weapon.dir;
                    attack();
                    break;
                case Dash:
                    if (!jump) attack();
                    break;
            }
        }
    }

    public void drawDebug(Graphics2D g) {
        g.setColor(Color.white);
        P fist = clip.organs[FIST].ptd.getAdd(pos);
        P foot = clip.organs[clip.FOOT].ptd.getAdd(pos);
        P t = Global.getRelPos((int) (clip.organs[0].pt.x + pos.x), (int) (clip.organs[0].pt.y + pos.y));
//        g.drawArc((int)(t.x-(size.x/2)), (int) (t.y-(size.y/2)), (int)size.x, (int)size.y, 0, 360);
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
        weapon.draw(g);
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
}