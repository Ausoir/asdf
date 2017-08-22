package com.ga.unit.item;

import com.ga.game.Sound;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.Man;

/**
 * Created by Ga on 8/9/2016. asd
 */
public class Armor extends Item {
    private int piece = 0;
    private int armorMod = 0;
    int pocket = 0;
    public Armor(String name, int seed, int piece, int encumbrance, int pockets, Material[] materials) {
        super(name, seed, materials, ItemType.Armor);
        this.encumbrance = encumbrance;
        value = 24;
        this.pocket = pockets;
        this.piece = piece;
        delay = 80;
        if (piece != 0) image.set(name, piece);
    }
    void shade(int color){}
    public void onCreate(){
        imbueMeta(true, true, .03f);
        if (emod != null && !emod.tags.contains(com.ga.unit.Tag.Buff)){
            emod = null;
            ademod = AdenchantMod.None;
        }
    }
    public void randomSize(P sizeVariance) {
    }
    float getArmor(){
        return Math.max(0,material.hardness + armorMod);
    }
    protected void setModifier(PrefixMod mod){
        super.setModifier(mod);
        if (mod == PrefixMod.Awful) armorMod = -2;
        else if (mod == PrefixMod.Old) armorMod = -1;
        else if (mod == PrefixMod.Fine) armorMod = 1;
        else if (mod == PrefixMod.Mastercraft) armorMod = 2;
    }
    protected void setModifier(SpecialtyMod mod){
        if (mod == SpecialtyMod.Tough) armorMod += admod.magnitude;
        else super.setModifier(mod);
    }
    public void remove(){
        if (owner != null) owner.armor.remove(this);
        super.remove();
    }
    public int onHit(int damage){ //damage reduction
        int r = (int) Math.max(0, damage - getArmor());
        if (usable(r)){
            if (owner != null) trigger(owner);
        }
        return r;
    }
    @Override
    public boolean use(final Man host){
        if (host.getVacancy() >= 0) {
            host.setState(type.action);
            final Armor me = this;
            host.delay.performer = new Performer() {
                public void perform() {
                    if (host.armor.isBare(piece)) {
                        Sound.play(host.pos, "wear");
                        me.remove();
                        host.armor.set(me, piece);
                        owner = host;
                    }
                }
            };
            return true;
        } else host.say("Too encumbering!");
        return false;
    }
    @Override
    public String getName(){
        String r = material.name() + " " + super.getName();
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
        return r + " This armor decreases damage by "+getArmor()+" points." +
                " It has "+pocket+" pockets.";
    }
}
