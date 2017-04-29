package com.ga.unit.item;

import com.ga.map.Global;
import com.ga.unit.Man;
import com.ga.unit.Tag;

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
    private int damageMod = 0;
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
        imbueMeta(true, true, .04f, true);
        if (emod != null && !emod.tags.contains(com.ga.unit.Tag.Debuff)){
            emod = null;
            ademod = AdenchantMod.None;
        }
    }
    protected void setModifier(PrefixMod mod){
        super.setModifier(mod);
        if (mod == PrefixMod.Mastercraft){
            damageMod = 1;
        } else if (mod == PrefixMod.Legendary){
            damageMod = 2;
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
    public int getDamage() {
        return damageSide.getDamage()+super.getDamage()+damageMod;
    }
    @Override
    public void use(Man host){
        if (uses == 0) return;
        super.use(host);
    }
    private void setDamage(int num, int sides){
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
        return r + " This weapon deals "+damageSide.num+"d"+damageSide.sides+" + "+damageMod+" damage.";
    }
}
