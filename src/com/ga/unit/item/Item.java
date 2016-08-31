package com.ga.unit.item;

import com.ga.map.Global;
import com.ga.unit.Actions;
import com.ga.unit.Man;
import com.ga.unit.Thing;
import com.ga.unit.skill.Stat;

import java.util.Random;

/**
 * Created by Ga on 8/5/2016.
 */
public class Item extends Thing {
    enum SpecialtyMod{
        None, Light, Heavy, Feather, Sharp, Dull, Quick, Slow, Durable, Tough;
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
    enum EnchantMod {
        None(100), Vampirism(1), Whirlwind(1), Dashing(1), Adrenaline(1), Cleaving(1);
        static int pool;
        int weight;
        EnchantMod(int weight){
            expandPool(weight);
            this.weight = weight;
        }
        void expandPool(int weight){
            pool += weight;
        }
        static EnchantMod get(Random seed){
            int r = seed.nextInt(pool);
            int t = 0;
            for (EnchantMod i : values()){
                t+=i.weight;
                if (t >= r) return i;
            }
            return None;
        }
    }
    AdverbMod admod;
    SpecialtyMod smod;
    PrefixMod pmod;
    EnchantMod emod;
    public int weight;
    protected int cooldown = 100;
    public int delay = 10;
    public Actions action = Actions.CONSUME;
    private int[] lastused = new int[]{0};
    public Stat uses = new Stat(1);
    public Item(String name, int seed, int weight, int uses, int cooldown) {
        super(name, seed);
        this.weight = weight;
        this.uses.setTotal(uses);
        this.cooldown = cooldown;
        admod = AdverbMod.get(rand);
        pmod = PrefixMod.get(rand);
        smod = SpecialtyMod.get(rand);
        emod = EnchantMod.get(rand);
        setModifier(PrefixMod.get(rand));
        setModifier(SpecialtyMod.get(rand));
    }
    protected void setModifier(PrefixMod mod){
        if (mod == PrefixMod.Broken) uses.addTotal((int) (uses.getMax()*-.7f));
        else if (mod == PrefixMod.Old) uses.addTotal((int) Math.min(-1,uses.getMax()*-.3f));
        else if (mod == PrefixMod.Fine) uses.addTotal(Math.max(1,uses.getMax()));
        else if (mod == PrefixMod.Mastercraft) uses.setTotal(-1);
        else if (mod == PrefixMod.Legendary) uses.setTotal(-1);
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
    }
    public int getRarity(){
        return pmod.ordinal()+(smod != SpecialtyMod.None?10:0)+(emod != EnchantMod.None?100:0);
    }
    public int getValue(){
        int r = 9999999;
        boolean enchanted = emod != EnchantMod.None;
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
            case 5:
                r = 50000;
                break;
        }
        if (enchanted){
            r += 500;
            r *= 2;
        }
        if (special) r += (r*adverb);
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
        if (emod != EnchantMod.None) r+=" of "+emod.name();
        r = r.replace("_"," ");
        return r;
    }
    public void unequip(){}
    public boolean use(Man host){
        if (uses.get() != 0 && Global.cooldown(lastused, cooldown)) {
            uses.add(-1);
            return true;
        }
        return false;
    }
    public String getDescription() {
        String r = "a "+getFullName()+".";
        int uses = this.uses.get();
        r+= " Weighs "+weight+" and has "+(uses==-1?"infinite":uses)+" uses left.";
        if (emod != EnchantMod.None){
            r += " This item is magically enchanted with "+emod.name()+". The item's cooldown is "+cooldown/10+" seconds.";
            int cd = Global.checkCooldown(lastused, cooldown);
            if (cd <= 0) r += " Currently the item is ready to use.";
            else r += " Currently the item needs "+cd/10+" more seconds to recharge.";
        }
        return r;
    }
    public String toString(){
        return "";
    }
}