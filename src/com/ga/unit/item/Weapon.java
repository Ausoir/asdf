package com.ga.unit.item;

import com.ga.game.Sound;
import com.ga.map.Map;
import com.ga.unit.Tag;
import com.ga.unit.Unit;

/**
 * Created by Ga on 8/1/2016. asd
 */
public class Weapon extends Item {
    private int quality = 0;
    public Weapon(Map map, String name, int seed, Material[] materials, ItemType type, int durability, int speedMod, int damageMod) {
        super(map, name, seed, materials, type, durability);
        this.weight += damageMod;
        speed = weight - speedMod;
        value = value*2 + 1000;
        cooldown.setDuration(0);
        rotation = 35;
        disposable = false;
    }
    public void onCreate(){
        imbueMeta(true, true, .1f);
    }
    protected void setModifier(SpecialtyMod mod){
        if (mod == SpecialtyMod.Sharp) quality += admod.magnitude;
        else if (mod == SpecialtyMod.Dull) quality -= admod.magnitude;
        else super.setModifier(mod);
    }
    public int getQuality(){
        return quality;
    }
    public void enhance(){
        if (isEnhanceable()){
            quality++;
        }
    }
    private boolean isEnhanceable(){
        return quality < material.hardness;
    }

    @Override
    public int getSpeed(){
        return uses==0?speed-10:speed;
    }
    @Override
    public int getValue(){
        return super.getValue() + (quality*1500);
    }
    @Override
    public boolean trigger(Unit target) {
        if (!type.isBlunt()){
            isUsable(true, 1);
            Sound.play(map, pos, "hit_cut");
        } else Sound.play(map, pos, "hit_bash");
        return super.trigger(target);
    }
    @Override
    public void playAttackSound(){
        Sound.play(map, pos, type.sound);
    }
    @Override
    protected void playSound(){
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
        int damage = super.getDamage()+quality;
        return Math.max(0,damage);
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
        return r + " This weapon deals "+getDamage()+" damage.";
    }
    public String[] load(String p){
        String[] r = super.load(p);
        quality = Integer.parseInt(r[8]);
        if (quality > 0) disposable = false;
        return r;
    }
    public String toString(){
        return super.toString()+"@"+quality;
    }
}
