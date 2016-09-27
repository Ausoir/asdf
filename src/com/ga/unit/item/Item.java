package com.ga.unit.item;

import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.unit.Man;
import com.ga.unit.Matter;
import com.ga.unit.Updater;
import com.ga.unit.skill.*;

import java.util.Random;

/**
 * Created by Ga on 8/5/2016.
 */
public class Item extends Matter {
    public enum Tag{
        Sword(Skills.Swing), Spear(Skills.Pierce), Mace(Skills.Swing), Dagger(Skills.Riposte), Armor(Skills.Rest), Scroll(Actions.Cast),
        Throwing(Skills.Throw), Shooting(Actions.Shoot), Consumable(Actions.Consume), Claw(Skills.Riposte), Enchant(Actions.Buff);
        Moveset action;
        Tag(Moveset p){
            action = p;
        }
        public Moveset getAction() {
            return action;
        }
    }
    enum SpecialtyMod{
        None, Light, Heavy, Feather, Sharp, Dull, Quick, Slow, Durable, Tough, Runic, Instant, Comfortable, Cursed;
        static SpecialtyMod get(Random seed){
            if (seed.nextGaussian() > 1) return values()[seed.nextInt(values().length)];
            return None;
        }
    }
    enum PrefixMod{
        None(100), Broken(20), Old(20), Fine(10), Mastercraft(5), Legendary(1);
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
        static AdverbMod get(Random seed){
            int t = seed.nextInt(100);
            if (t < 3) return Ultra;
            else if (t < 8) return Super;
            else if (t < 18) return Very;
            else return None;
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
    AdverbMod admod;
    AdenchantMod ademod;
    SpecialtyMod smod;
    PrefixMod pmod;
    Spells emod = null;
    public int weight;
    protected int cooldown = 100;
    public int delay = 10;
    protected float probability;
    public Moveset action = Actions.Consume;
    private int[] lastused = new int[]{0};
    public Stat uses = new Stat(1);
    protected Tag type = Tag.Consumable;
    public Item(String dir, String name, int seed, int weight, int uses, int cooldown, float enchantRatio) {
        super("item/"+dir,name, seed, P.getZero(), -1);
        this.name = name;
        this.weight = weight;
        this.uses.setTotal(uses);
        this.cooldown = cooldown;
        pmod = PrefixMod.get(rand);
        smod = SpecialtyMod.get(rand);
        if (smod != SpecialtyMod.None) admod = AdverbMod.get(rand);
        probability = enchantRatio;
        if (rand.nextFloat() < enchantRatio) {
            ademod = AdenchantMod.get(rand);
            emod = Spells.values()[rand.nextInt(Spells.values().length)];
            probability = ademod.magnitude;
        }
        setModifier(pmod);
        setModifier(smod);
        Updater.addQueue.remove(this);
        init();
    }
    protected void init(){

    }
    public void setType(Tag p){
        this.type = p;
        this.action = p.getAction();
    }
    public Tag getType(){
        return type;
    }
    protected void setModifier(PrefixMod mod){
        if (mod == PrefixMod.Broken) uses.addTotal((int) (uses.getMax()*-.7f));
        else if (mod == PrefixMod.Old) uses.addTotal((int) Math.min(-1,uses.getMax()*-.3f));
        else if (mod == PrefixMod.Fine){
            uses.addTotal(Math.max(1,uses.getMax()));
            probability += .1f;
        }
        else if (mod == PrefixMod.Mastercraft){
            uses.setTotal(-1);
            probability += .2f;
        }
        else if (mod == PrefixMod.Legendary){
            uses.setTotal(-1);
            probability += .3f;
        }
        if (uses.getMax() < 0 && (mod != PrefixMod.Mastercraft && mod != PrefixMod.Legendary)) uses.setTotal(0);
    }
    protected void setModifier(SpecialtyMod mod){
        if (mod == SpecialtyMod.None) admod = AdverbMod.None;
        else if (mod == SpecialtyMod.Sharp) smod = SpecialtyMod.None;
        else if (mod == SpecialtyMod.Dull) smod = SpecialtyMod.None;
        else if (mod == SpecialtyMod.Tough) smod = SpecialtyMod.None;
        else if (mod == SpecialtyMod.Heavy) this.weight += 1*admod.magnitude;
        else if (mod == SpecialtyMod.Light) this.weight = Math.max(0, this.weight + (-1 * admod.magnitude));
        else if (mod == SpecialtyMod.Feather) this.weight /= 2*admod.magnitude;
        else if (mod == SpecialtyMod.Quick) cooldown *= 1-(.2f*admod.magnitude);
        else if (mod == SpecialtyMod.Slow) cooldown *= 1+(.2f*admod.magnitude);
        else if (mod == SpecialtyMod.Durable) uses.addTotal(uses.getMax());
        else if (mod == SpecialtyMod.Runic) probability += .2f;
        else if (mod == SpecialtyMod.Instant) delay = 1;
        else if (mod == SpecialtyMod.Comfortable) smod = SpecialtyMod.None;
    }
    public int getRarity(){
        return pmod.ordinal()+(smod != SpecialtyMod.None?10:0)+(emod != null?100:0);
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
        String r = "common";
        int rare = getRarity();
        if (rare >= 10) r = "fine";
        if (rare >= 100) r = "rare";
        if (rare % 5 == 4) r = "ludicrous";
        else if (rare % 5 == 5) r = "legendary";
        return r;
    }
    public String getFullName(){
        String r = "";
        if (admod != AdverbMod.None) r+=admod.name()+" ";
        if (smod != SpecialtyMod.None) r+=smod.name()+" ";
        if (pmod != PrefixMod.None) r+=pmod.name()+" ";
        r += name;
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
    protected float getProbability(){
        return probability+ademod.magnitude;
    }
    public boolean use(Man host){
        if (uses.get() != 0 && Global.cooldown(lastused, cooldown)) {
            if (uses.get() != -1) uses.add(-1);
            if (emod != null && rand.nextFloat() < getProbability()){
                emod.getEvent().start(host, ademod.level);
                emod.getEvent().perform();
            }
            return true;
        }
        return false;
    }
    public void pickup(Man host){
        setDepth(-1);
        gravity = false;
    }
    public void drop(Man host) {
        pos = new P(host.pos);
        setDepth(8);
        rotation = (float)Global.r.nextInt(628)/100;
        gravity = true;
    }
    public String getDescription() {
        String r = "a "+getFullName()+".";
        int uses = this.uses.get();
        r+= " Weighs "+weight+" and has "+(uses==-1?"infinite":uses)+" uses left.";
        if (emod != null){
            String ade = "";
            if (ademod != AdenchantMod.None) ade = "Level "+ademod.level+" ";
            r += " This "+getRarityDescription()+" item is magically enchanted with "+ade+emod.name();
            r += ". The item's cooldown is "+cooldown+" seconds.";
            int cd = Global.checkCooldown(lastused, cooldown);
            if (cd <= 0) r += " Currently the item is ready to use.";
            else r += " Currently the item needs "+cd+" more seconds to recharge.";
        }
        return r;
    }
    public String toString(){
        return "";
    }
}