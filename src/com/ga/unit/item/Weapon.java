package com.ga.unit.item;

import com.ga.map.Global;
import com.ga.unit.Man;

/**
 * Created by Ga on 8/1/2016.
 */
public class Weapon extends Item {
    class Die{
        int num;
        int sides;
        Die(int num, int sides){
            this.num = num;
            this.sides = sides;
        }
        int getDamage(){
            return (Global.r.nextInt(sides)+1) * num;
        }
    }
    private Die damageSide = new Die(0,1);
    private float damageMod = 1;
    public Weapon(String name, int seed, int weight, int uses, int num, int sides, Tag type) {
        super(name, seed, weight, uses);
        value = 36;
        breakable = false;
        setType(type);
        cooldown = 0;
        setDamage(num, sides);
        rotation = 35;
        delay = 0;
    }
    public void onCreate(){
        imbueMeta(true, true, .03f, true);
    }
    protected void setModifier(PrefixMod mod){
        super.setModifier(mod);
        if (mod == PrefixMod.Broken) damageMod = .5f;
        else if (mod == PrefixMod.Old) damageMod = .8f;
        else if (mod == PrefixMod.Fine) damageMod = 1.2f;
        else if (mod == PrefixMod.Mastercraft){
            damageMod = 1.5f;
            uses.setTotal(-1);
        }
        else if (mod == PrefixMod.Legendary){
            damageMod = 2;
            uses.setTotal(-1);
        }
    }
    protected void setModifier(SpecialtyMod mod){
        if (mod == SpecialtyMod.Feather){
            damageMod = .33f;
            this.weight /= 2*admod.magnitude;
        }
        else if (mod == SpecialtyMod.Sharp) damageMod += .2f*admod.magnitude;
        else if (mod == SpecialtyMod.Dull) damageMod += -.2f*admod.magnitude;
        else super.setModifier(mod);
    }
    public int getDamage() {
        int r = 0;
        if (canUse()) r = (int) (damageSide.getDamage()*damageMod)+super.getDamage();
        return r;
    }
    public void setDamage(int num, int sides){
        damageSide = new Die(num, sides);
    }
    @Override
    protected void playSound(){
    }
    public String getDescription(){
        String r = super.getDescription();
        r = r.replace(" uses "," durability ");
        if (emod != null){
            r += " This weapon has "+probability*100+"% chance of activating its enchantment every hit.";
        }
        return r + " This weapon deals "+damageSide.num+"d"+damageSide.sides+" * "+damageMod+" damage.";
    }
}
