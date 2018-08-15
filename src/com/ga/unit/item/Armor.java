package com.ga.unit.item;

import com.ga.game.Sound;
import com.ga.map.Map;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.Tag;
import com.ga.unit.man.BodyHumanoid;
import com.ga.unit.man.Man;
import com.ga.unit.skill.Actions;

/**
 * Created by Ga on 8/9/2016. asd
 */
public class Armor extends Item {
    int encumbrance;
    private int piece;
    private int armorMod = 0;
    private int warmth;
    int pocket;
    public Armor(Map map, String name, int seed, Material[] materials, int piece, int encumbrance, int pockets, int warmth) {
        super(map, name, seed, materials, ItemType.Armor);
        this.encumbrance = encumbrance;
        value = value*2 + 500;
        this.pocket = pockets;
        this.piece = piece;
        this.warmth = warmth;
        if (piece == 1) image.set(name, 0);
        else if (piece == 2) image.set(name, 2);
        else if (piece == 4) weight = 0;
    }
    void shade(int color){}
    public void onCreate(){
        imbueMeta(true, true, .1f);
    }
    @Override
    boolean isCursed() {
        return emod != null && emod.tags.contains(Tag.Curse) || super.isCursed();
    }
    public void randomSize(P sizeVariance) {}
    private float getArmor(){
        return Math.max(0,material.hardness + armorMod);
    }
    int getWarmth(){
        return Math.max(0, warmth+material.getWarmth());
    }
    protected void setModifier(SpecialtyMod mod){
        if (mod == SpecialtyMod.Hardened) armorMod += admod.magnitude;
        else super.setModifier(mod);
    }
    public void remove(){
        if (owner != null) owner.armor.remove(this);
        super.remove();
    }
    public boolean checkBreak(boolean blunt, int damage){ //damage reduction
        int r = (int) Math.max(0, blunt?damage:damage - getArmor());
        if (isUsable(true, r)){
            if (owner != null) trigger(owner);
            return false;
        }
        return true;
    }
    @Override
    public boolean use(final Man host){
        if (host.getVacancy() >= encumbrance) {
            host.setState(type.action);
            final Armor me = this;
            host.delay.performer = new Performer() {
                public void perform() {
                    if (host.clip instanceof BodyHumanoid) {
                        if (host.armor.isBare(piece)) {
                            Sound.play(host.map, host.pos, "wear");
                            me.remove();
                            host.setWeapon(null);
                            host.armor.set(me, piece);
                            owner = host;
                        } else {
                            host.setState(Actions.Wield);
                        }
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
        if (piece == 1) r+=" Armor";
        else if (piece == 2) r+=" Pants";
        return r;
    }
    public String getDescription(){
        String r = super.getDescription();
        r = r.replace(" uses "," durability ");
        if (encumbrance > 0) r += "Encumbrance: "+encumbrance+"\n";
        if (emod != null) r += "This armor has "+(int)(probability*100)+"% chance of activating its enchantment on being hit. ";
        if (getArmor() > 0) r += "This armor decreases damage by "+getArmor()+" points. ";
        if (getWarmth() > 0) r += "This armor provides "+getWarmth()+" warmth. ";
        if (pocket > 0) r += "This armor has "+pocket+" pockets. ";
        return r;
    }
}