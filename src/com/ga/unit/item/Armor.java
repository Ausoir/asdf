package com.ga.unit.item;

import com.ga.map.Picture;
import com.ga.tool.Clips;
import com.ga.tool.Performer;
import com.ga.unit.Man;

/**
 * Created by Ga on 8/9/2016.
 */
public class Armor extends Item {
    public int piece = 0;
    private float armor = 0;
    private float armorMod = 1;
    public int encumberance = 0;
    public Armor(String name, int seed, int weight, int uses, float armor, int encumberance, int piece) {
        super(name, seed, weight, uses, 100, .04f);
        setType(Tag.Armor);
        this.armor = armor;
        this.name = name;
        this.encumberance = encumberance;
        this.piece = piece;
        if (piece != 5) image = new Picture(name, Math.abs(Integer.parseInt(Clips.clip.get("Man").model[piece][1]))-1);
        delay = 80;
    }
    public float getArmor(){
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
    protected void onHit(Man host){
        super.use(host);
    }
    @Override
    public boolean use(final Man host){
        host.setState(action);
        final Armor me = this;
        host.delay.performer = new Performer(){
            public void perform(){
                if (host.armor.isBare(piece)) {
                    host.armor.set(me, piece, true);
                    host.setWeapon(null);
                    me.remove();
                }
            }
        };
        return false;
    }
    @Override
    public String getDisplayName(){
        String r = super.getDisplayName();
        if (piece == 0 || piece == 4) r+=" Arm";
        else if (piece == 1 || piece == 2) r+=" Leggings";
        else if (piece == 3) r+=" Armor";
        return r;
    }
    public String getDescription(){
        String r = super.getDescription();
        r = r.replace(" uses "," durability ");
        if (emod != null){
            r += " This armor has "+probability*100+"% chance of activating its enchantment on being hit.";
        }
        return r + " This armor decreases damage by "+getArmor()+" points.";
    }
    @Override
    public String toItemString(){
        return super.toItemString()+","+piece;
    }
}
