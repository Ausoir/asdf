package com.ga.unit.item;

import java.awt.*;

/**
 * Created by Ga on 8/9/2016.
 */
public class Armor extends Item {
    private float armor = 0;
    private float armorMod = 1;
    public Color color1 = Color.green;
    public Color color2 = Color.blue;
    public float comfortability = 0;
    public Armor(String name, int seed, int weight, int uses, float armor, float comfort) {
        super("armor/", name, seed, weight, uses, 100, .04f);
        type = Tag.Armor;
        this.armor = armor;
        this.name = name;
        comfortability = comfort;
    }
    public float getArmor(){
        return armor * armorMod;
    }
    protected void setModifier(PrefixMod mod){
        super.setModifier(mod);
        if (mod == PrefixMod.Broken) armorMod = .5f;
        else if (mod == PrefixMod.Old) armorMod = .8f;
        else if (mod == PrefixMod.Fine) armorMod = 1.2f;
        else if (mod == PrefixMod.Mastercraft) armorMod = 1.5f;
        else if (mod == PrefixMod.Legendary) armorMod = 2;
    }
    protected void setModifier(SpecialtyMod mod){
        if (mod == SpecialtyMod.Comfortable) comfortability += admod.magnitude*.1f;
        else if (mod == SpecialtyMod.Tough) armorMod += .5f;
        else super.setModifier(mod);
    }
    public String getDescription(){
        String r = super.getDescription();
        r = r.replace(" uses "," durability ");
        if (emod != null){
            r += " This armor has "+getProbability()*100+"% chance of activating its enchantment on being hit.";
        }
        return r + " This armor decreases damage by "+getArmor()+" points.";
    }
}
