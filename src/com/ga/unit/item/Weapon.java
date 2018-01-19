package com.ga.unit.item;

import com.ga.map.Map;
import com.ga.unit.Tag;

/**
 * Created by Ga on 8/1/2016. asd
 */
public class Weapon extends Item {
    private int quality = 0;
    public Weapon(Map map, String name, int seed, ItemType type, int encumbrance, Material[] materials, int durability) {
        super(map, name, seed, materials, type, durability);
        this.encumbrance = encumbrance;
        value = value*2 + 1000;
        cooldown = 0;
        rotation = 35;
        delay = 0;
    }
    public void onCreate(){
        imbueMeta(true, true, .5f);
        if (emod != null && !emod.tags.contains(Tag.Curse) && rand.nextFloat() > .1f){
            emod = null;
            ademod = AdenchantMod.None;
        }
    }
    protected void setModifier(SpecialtyMod mod){
        if (mod == SpecialtyMod.Feather) this.weight = Math.max(0,this.weight-(2*admod.magnitude));
        else if (mod == SpecialtyMod.Sharp) quality += (admod.magnitude)/2;
        else if (mod == SpecialtyMod.Dull) quality += (-1*admod.magnitude)/2;
        else super.setModifier(mod);
    }
    boolean isEnhanceable(){
        return quality < material.hardness;
    }
    @Override
    boolean isCursed() {
        return emod != null && emod.tags.contains(Tag.Enchant) || super.isCursed();
    }
    @Override
    public String getName(){
        return material.name() + " " + super.getName();
    }
    @Override
    public int getDamage() {
        return Math.max(0,super.getDamage()+quality);
    }
    @Override
    public String getFullName() {
        if (quality == 0) return super.getFullName();
        return super.getFullName() + " +"+quality;
    }
    @Override
    public String getDescription(){
        String r = super.getDescription();
        r = r.replace(" uses "," durability ");
        if (emod != null){
            r += " This weapon has "+(int)(probability*100)+"% chance of activating its enchantment every hit.";
        }
        return r + " This weapon deals upto "+getDamage()+" damage.";
    }
    public String[] load(String p){
        String[] r = super.load(p);
        quality = Integer.parseInt(r[8]);
        return r;
    }
    public String toString(){
        return super.toString()+"@"+ quality;
    }
}
