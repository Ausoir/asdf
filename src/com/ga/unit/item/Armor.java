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
    private int warmth;
    int pocket;
    public Armor(Map map, String name, String imageName, int seed, Material material, int piece, int encumbrance, int pockets, int durabilityMod, int warmth) {
        super(map, name, imageName, seed, material, ItemType.Armor);
        this.encumbrance = encumbrance;
        value = value + 400;
        this.pocket = pockets;
        this.piece = piece;
        this.warmth = warmth;
        if (piece == 1) image.set("/"+imageName+"/32/64/0/0");
        else if (piece == 2) image.set("/"+imageName+"/32/64/2/0");
        else if (piece == 4) weight = 0;
        this.durability += durabilityMod;
        this.uses += durabilityMod;
    }
    public void onCreate(){
        imbueMeta(true, true, .1f);
    }
    @Override
    boolean isCursed() {
        return emod != null && emod.tags.contains(Tag.Curse) || super.isCursed();
    }
    public void randomSize(P sizeVariance) {}
    int getWarmth(){
        return Math.max(0, warmth+material.getWarmth());
    }
    public void remove(){
        if (owner != null) owner.armor.remove(this);
        super.remove();
    }
    public boolean checkBreak(boolean blunt, int damage){ //damage reduction
        if (damage == 0) return false;
        int r = Math.max(1, blunt?damage:damage - material.hardness);
        if (isUsable(false, 1)){
            isUsable(true, Math.min(r, uses));
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
            host.delay.performer = new Performer(()->{
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
            });
            return true;
        } else host.say("Too encumbering!");
        return false;
    }
    @Override
    public String getName(){
        return material.name() + " " + super.getName();
    }
    public String getDescription(){
        String r = super.getDescription();
        r = r.replace(" uses "," durability ");
        if (encumbrance > 0) r += "Encumbrance: "+encumbrance+"\n";
        if (emod != null) r += "This armor has "+(int)(probability*100)+"% chance of activating its enchantment on being hit. ";
        if (material.hardness > 0) r += "This armor decreases cutting damage by "+material.hardness+" points. ";
        if (getWarmth() > 0) r += "This armor provides "+getWarmth()+" warmth. ";
        if (pocket > 0) r += "This armor has "+pocket+" pockets. ";
        return r;
    }
}