package com.ga.unit.item;

import com.ga.map.Global;

import static com.ga.unit.item.Item.Material.*;

/**
 * Created by Ga on 8/1/2016. asd
 */
public class Weapon extends Item {
    private int damageMod = 0;
    public Weapon(String name, int seed, ItemType type, int encumberance) {
        super(name, seed, new Material[]{Wooden, Glass, Stone, Copper, Brass, Bronze, Iron, Silver, Steel, Bone, Titanium, Lead, Crystal}, type);
        this.encumberance = encumberance;
        value = 36;
        cooldown = 0;
        rotation = 35;
        delay = 0;
    }
    public void onCreate(){
        imbueMeta(true, true, .04f);
        if (emod != null && !emod.tags.contains(com.ga.unit.Tag.Debuff)){
            emod = null;
            ademod = AdenchantMod.None;
        }
    }
    protected void setModifier(PrefixMod mod){
        super.setModifier(mod);
        if (mod == PrefixMod.Mastercraft){
            damageMod = 1;
        }
    }
    protected void setModifier(SpecialtyMod mod){
        if (mod == SpecialtyMod.Feather){
            damageMod = -1;
            this.weight = Math.max(0,this.weight-(2*admod.magnitude));
        }
        else if (mod == SpecialtyMod.Sharp) damageMod += (1*admod.magnitude)/2;
        else if (mod == SpecialtyMod.Dull) damageMod += (-1*admod.magnitude)/2;
        else super.setModifier(mod);
    }
    private int getMaxDamage(){
        if (type.isBlunt()) return weight+1;
        else return Math.max(1,material.hardness);
    }
    @Override
    public int getDamage() {
        return Global.r.nextInt(getMaxDamage())+damageMod;
    }
    @Override
    public String getName() {
        return material.name() + " " + super.getName();
    }
    public String getDescription(){
        String r = super.getDescription();
        r = r.replace(" uses "," durability ");
        if (emod != null){
            r += " This weapon has "+(int)(probability*100)+"% chance of activating its enchantment every hit.";
        }
        String damageType = type.isBlunt() ? "Blunt" : "Cutting";
        return r + " This weapon deals upto "+getMaxDamage()+" + "+damageMod+" "+damageType+" damage.";
    }
}
