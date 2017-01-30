package com.ga.unit.item;

import com.ga.game.*;
import com.ga.game.Dialog;
import com.ga.map.Global;
import com.ga.tool.Interactive;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.*;
import com.ga.unit.skill.*;

import java.awt.*;
import java.util.Random;

/**
 * Created by Ga on 8/5/2016.
 */
public class Item extends Matter {
    public enum Tag{
        Sword(Skills.Swing, DamageType.Cut, "swing_knife", "hit_cut"), Spear(Skills.Pierce, DamageType.Cut, "swing_silent", "hit_bloody"), Mace(Skills.Swing, DamageType.Bash, "swing_silent", "hit_bash"),
        Dagger(Skills.Stab, DamageType.Cut, "swing_normal", "hit_bloody"), Armor(Skills.Rest, DamageType.Push, "swing_normal"), Scroll(Skills.Guard, DamageType.Push, "scroll"),
        Rapier(Skills.Underhand, DamageType.Cut, "swing_silent", "hit_cut"), Shoot(Skills.Shoot, DamageType.Cut, "swing_normal"), Snipe(Skills.Snipe, DamageType.Cut, "swing_normal"), Throwing(Skills.Throw, DamageType.Cut, "swing_normal"),
        Darting(Skills.Dart, DamageType.Cut, "swing_normal"), Consumable(Actions.Consume, DamageType.Push, "swing_normal"), Claw(Skills.Swing, DamageType.Cut, "swing_normal"),
        Enchant(Actions.Buff, DamageType.Push, "swing_normal");
        private Moveset action;
        private DamageType type;
        private String sound, soundHit = "hit_punch";
        Tag(Moveset p, DamageType type, String sound){
            action = p;
            this.type = type;
            this.sound = sound;
        }
        Tag(Moveset p, DamageType type, String sound, String soundHit){
            this(p, type, sound);
            this.soundHit = soundHit;
        }
        public Moveset getAction() {
            return action;
        }
        public DamageType getDamageType(){
            return type;
        }
        public String getSound(){
            return sound;
        }
        public String getHitSound(){
            return soundHit;
        }
    }
    enum SpecialtyMod{
        None, Light, Heavy, Feather, Sharp, Dull, Hasty, Lazy, Durable, Tough, Runic, Instant, Comfortable, Cursed, Quick, Slow, Recharging;
        static SpecialtyMod get(Random seed){
            if (seed.nextGaussian() > 1) return values()[seed.nextInt(values().length)];
            return None;
        }
    }
    enum PrefixMod{
        None(125), Broken(20), Old(20), Fine(15), Mastercraft(4), Legendary(1);
        static int pool;
        int weight;
        PrefixMod(int weight){
            expandPool(weight);
            this.weight = weight;
        }
        void expandPool(int weight){
            pool += weight;
        }
        static PrefixMod get(Random seed){
            int r = seed.nextInt(pool);
            int t = 0;
            for (PrefixMod i : values()){
                t+=i.weight;
                if (t >= r) return i;
            }
            return None;
        }
    }
    enum AdverbMod{
        None(1), Very(2), Super(3), Ultra(4);
        int magnitude;
        AdverbMod(int mag){
            this.magnitude = mag;
        }
        static AdverbMod load(String p){
            for (AdverbMod i : values()){
                if ((""+i.magnitude).equals(p)) return i;
            }
            return None;
        }
        public String toString(){
            return ""+magnitude;
        }
    }
    enum AdenchantMod{
        None(2,.15f), Minor(1,.1f), Major(3,.25f), Greater(4,.35f), Ultimate(5,.4f);
        int level;
        float magnitude;
        AdenchantMod(int level, float mag){
            this.level = level;
            this.magnitude = mag;
        }
        static AdenchantMod get(Random seed){
            float t = (float) seed.nextGaussian();
            if (t > 2.5) return Ultimate;
            else if (t > 2) return Greater;
            else if (t > 1.5) return Major;
            else if (t < -1) return Minor;
            else return None;
        }
    }
    AdverbMod admod = AdverbMod.None;
    AdenchantMod ademod;
    SpecialtyMod smod;
    PrefixMod pmod;
    Spells emod = null;
    public int weight;
    protected int cooldown = 100;
    public int delay = 0;
    protected float probability;
    public Moveset action = Actions.Consume;
    public int[] lastused = new int[]{0};
    public Stat uses = new Stat(1);
    private Tag type = Tag.Consumable;
    private Organ organ = null;
    public int speed = 0;
    public int damage = 1;
    public Item(String name, int seed, int weight, int uses, int cooldown, float enchantRatio) {
        super(name, seed, new P(), 8);
        this.name = name;
        this.weight = weight;
        this.uses.setTotal(uses);
        this.cooldown = cooldown;
        pmod = PrefixMod.get(rand);
        smod = SpecialtyMod.get(rand);
        if (rand.nextFloat() < enchantRatio) {
            ademod = AdenchantMod.get(rand);
            emod = Spells.values()[rand.nextInt(Spells.values().length)];
            probability = ademod.magnitude;
        }
        setModifier(pmod);
        setModifier(smod);
        gravity = true;
        final Item me = this;
        onInteract = new Interactive(image.sizeRadius+5){
            public void interact(Unit p){
                if (p instanceof Man) {
                    Man t = ((Man)p);
                    t.setWeapon(me);
                }
            }
        };
        Updater.remove(this);
    }
    public void setType(Tag p){
        this.type = p;
        this.action = p.getAction();
        this.damageType = p.getDamageType();
    }
    public Tag getType(){
        return type;
    }
    public void setOrgan(Organ p){
        this.organ = p;
    }
    public Organ getOrgan() {
        return organ;
    }
    protected void setModifier(PrefixMod mod){
        if (mod == PrefixMod.Broken){
            speed -= 10;
            uses.add((int) (uses.getMax()*-.7f));
        }else if (mod == PrefixMod.Old){
            speed -= 5;
            uses.add((int) Math.min(-1,uses.getMax()*-.3f));
        }else if (mod == PrefixMod.Fine){
            uses.addTotal(Math.max(1,uses.getMax()));
            probability += .1f;
        }else if (mod == PrefixMod.Mastercraft){
            speed += 4;
            probability += .2f;
        }else if (mod == PrefixMod.Legendary){
            speed += 7;
            probability += .3f;
        }
    }
    protected void setModifier(SpecialtyMod mod){
        if (mod == SpecialtyMod.None) admod = AdverbMod.None;
        else if (mod == SpecialtyMod.Sharp) smod = SpecialtyMod.None;
        else if (mod == SpecialtyMod.Dull) smod = SpecialtyMod.None;
        else if (mod == SpecialtyMod.Tough) smod = SpecialtyMod.None;
        else if (mod == SpecialtyMod.Heavy) this.weight += 1*admod.magnitude;
        else if (mod == SpecialtyMod.Light) this.weight = Math.max(0, this.weight + (-1 * admod.magnitude));
        else if (mod == SpecialtyMod.Feather) this.weight /= 2*admod.magnitude;
        else if (mod == SpecialtyMod.Quick) speed = admod.magnitude;
        else if (mod == SpecialtyMod.Slow) speed = -admod.magnitude;
        else if (mod == SpecialtyMod.Hasty) cooldown *= 1-(.2f*admod.magnitude);
        else if (mod == SpecialtyMod.Lazy) cooldown *= 1+(.2f*admod.magnitude);
        else if (mod == SpecialtyMod.Durable) uses.addTotal(uses.getMax());
        else if (mod == SpecialtyMod.Runic) probability += .2f;
        else if (mod == SpecialtyMod.Instant) delay = 1;
        else if (mod == SpecialtyMod.Comfortable) smod = SpecialtyMod.None;
        else if (mod == SpecialtyMod.Recharging){
            cooldown = ((cooldown+5) * 2)+40;
            uses.setTotal(-1);
        }
    }
    public int getRarity(){
        return pmod.ordinal()+(smod != SpecialtyMod.None?10:0)+(emod != null?100:0);
    }
    public Color getRarityColor(){
        int rarity = getRarity();
        Color result = Color.black;
        switch (rarity){
            case 1:
                result = Color.LIGHT_GRAY;
                break;
            case 2:
                result = Color.WHITE;
                break;
            case 3:
                result = Color.GREEN;
                break;
            case 4:
                return Color.CYAN;
            case 5:
                return Color.MAGENTA;
        }
        if (rarity >= 100){
            result = Color.BLUE;
        }
        return result;
    }
    public int getValue(){
        int r;
        int adverb = admod.ordinal()+1;
        boolean special = smod != SpecialtyMod.None;
        switch (pmod.ordinal()){
            case 0:
                r = 100;
                break;
            case 1:
                r = 20;
                break;
            case 2:
                r = 50;
                break;
            case 3:
                r = 1000;
                break;
            case 4:
                r = 5000;
                break;
            default:
                r = 50000;
                break;
        }
        if (smod == SpecialtyMod.Cursed){
            r = 1;
        } else if (special) r += (r*adverb);
        if (emod != null){
            r += 500;
            r *= 2;
        }
        return r;
    }
    public String getRarityDescription(){
        String r = Dialog.get("item1");
        int rare = getRarity();
        if (rare >= 10) r = Dialog.get("item2");
        if (rare >= 100) r = Dialog.get("item3");
        if (rare % 5 == 4) r = Dialog.get("item4");
        else if (rare % 5 == 5) r = Dialog.get("item5");
        return r;
    }
    public String getDisplayName(){
        return name;
    }
    public String getFullName(){
        String r = "";
        if (admod != AdverbMod.None) r+=admod.name()+" ";
        if (smod != SpecialtyMod.None) r+=smod.name()+" ";
        if (pmod != PrefixMod.None) r+=pmod.name()+" ";
        r += getDisplayName();
        if (emod != null){
            r+=" of ";
            if (ademod != AdenchantMod.None){
                r += ademod.name()+" ";
            }
            r+=emod.getName();
        }
        r = r.replace("_"," ");
        return r;
    }
    public void unequip(Man host){
        if (smod == SpecialtyMod.Cursed){
            Curse.get(host, 5);
        }
    }
    protected boolean canUse(){
        if (uses.get() != 0 && Global.checkCooldown(lastused, cooldown)<= 0) {
            return true;
        }
        return false;
    }
    protected void playSound(){
        Sound.play(type.getSound());
    }
    public boolean use(Man host){
        if (canUse()) {
            playSound();
            Global.cooldown(lastused, cooldown);
            if (uses.get() != -1) uses.add(-1);
            host.setState(action);
            if (emod != null && rand.nextFloat() < probability){
                emod.getEvent().perform(host, ademod.level*3);
                host.state = emod;
            }
            return true;
        }
        return false;
    }
    public void drop(Man host) {
        if (host.weapon == this) host.setWeapon(null);
        pos = new P(host.pos);
        field();
        rotation = (float)Global.r.nextInt(628)/100;
    }
    public void remove(){
        if (organ != null) {
            ((Man) organ.host).setWeapon(null);
            setOrgan(null);
        }
        super.remove();
    }
    public void field(){
        gravity = true;
        Updater.add(this);
        setOrgan(null);
    }
    public String getSound(){
        return type.getSound();
    }
    public String getHitSound(){
        return type.getHitSound();
    }
    public String getDescription() {
        String r = "";
        int uses = this.uses.get();
        r += com.ga.game.Dialog.get("itemDsc",new String[]{getFullName(),weight+"",(uses==-1?"unlimited":uses+"")});
        if (emod != null){
            String ade = "";
            if (ademod != AdenchantMod.None) ade = "Level "+ademod.level+" ";
            r += com.ga.game.Dialog.get("itemEncDsc",new String[]{getRarityDescription(), ade+emod.name(), cooldown+""});
            int cd = Global.checkCooldown(lastused, cooldown);
            if (cd <= 0) r += Dialog.get("itemEncRdy");
            else r += Dialog.get("itemEncNRdy", new String[]{cd+""});
        }
        return r;
    }
    @Override
    public P getPoint(){
        P result = pos.getAdd(organ.ptd);
        result.add(image.size.x*.5f*Global.cos(rotation)*dir,(int) (image.size.x*.5f*Global.sin(rotation))*dir);
        return result;
    }
    public int getDamage() {
        return damage;
    }
    public Line getLine(){
        return new Line(pos.getAdd(organ.pt), getPoint());
    }
    @Override
    public void draw(Graphics2D g){
        P t = pos.getAdd(organ==null?new P():organ.ptd);
        image.draw(g, t.x, t.y, rotation, dir);
    }
    @Override
    public String toString(){
        if (organ == null) return "]]"+toItemString()+"/"+pos;
        else return "";
    }
    public String toItemString(){
        return name + "," + seed + "," + uses + "," + admod;
    }
    public void update(){
        if (organ == null) super.update();
    }
}