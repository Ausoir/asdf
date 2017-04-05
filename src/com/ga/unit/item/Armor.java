package com.ga.unit.item;

import com.ga.game.Sound;
import com.ga.map.Picture;
import com.ga.tool.Performer;
import com.ga.unit.Man;

/**
 * Created by Ga on 8/9/2016. asd
 */
public class Armor extends Item {
    private int piece = 0;
    private float armor = 0;
    private float armorMod = 1;
    public int encumberance = 0;
    public int pocket = 0;
    public Armor(String name, int seed, int weight, int uses, float armor, int encumberance, int piece) {
        super(name, seed, weight, uses);
        value = 24;
        setType(Tag.Armor);
        this.armor = armor;
        this.name = name;
        this.encumberance = encumberance;
        this.piece = piece;
        if (piece != 0) image = new Picture(name, piece);
        delay = 80;
    }
    public void onCreate(){
        imbueMeta(true, true, .03f, true);
    }
    float getArmor(){
        return armor * armorMod;
    }
    protected void setModifier(PrefixMod mod){
        super.setModifier(mod);
        if (mod == PrefixMod.Broken) armorMod = .5f;
        else if (mod == PrefixMod.Old) armorMod = .8f;
        else if (mod == PrefixMod.Fine) armorMod = 1.2f;
        else if (mod == PrefixMod.Mastercraft){
            uses.setTotal(-1);
            armorMod = 1.5f;
        }
        else if (mod == PrefixMod.Legendary){
            uses.setTotal(-1);
            armorMod = 2;
        }
    }
    protected void setModifier(SpecialtyMod mod){
        if (mod == SpecialtyMod.Comfortable) encumberance = Math.max(0,encumberance-admod.magnitude);
        else if (mod == SpecialtyMod.Tough) armorMod += .5f;
        else super.setModifier(mod);
    }
    void onHit(Man host){
        super.use(host);
    }
    @Override
    public boolean use(final Man host){
        host.setState(action);
        final Armor me = this;
        host.delay.performer = new Performer(){
            public void perform(){
                if (host.armor.isBare(piece)) {
                    Sound.play("wear");
                    host.armor.set(me, piece);
                    host.setWeapon(null);
                    me.remove();
                }
            }
        };
        return false;
    }
    @Override
    public String getName(){
        String r = super.getName();
        if (piece == 1) r+=" Leggings";
        else if (piece == 2) r+=" Armor";
        return r;
    }
    public String getDescription(){
        String r = super.getDescription();
        r = r.replace(" uses "," durability ");
        if (emod != null){
            r += " This armor has "+probability*100+"% chance of activating its enchantment on being hit.";
        }
        return r + " This armor decreases damage by "+getArmor()+" points. It has "+pocket+" pockets.";
    }
}
