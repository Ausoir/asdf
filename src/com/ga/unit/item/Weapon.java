package com.ga.unit.item;

import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Man;
import com.ga.unit.Organ;

import java.awt.*;

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
    private Die damage = new Die(0,1);
    private float damageMod = 1;
    private Organ organ = null;
    public Weapon(String name, int seed, int weight, int uses, int num, int sides, Tag type) {
        super("weapon/",name, seed, weight, uses, 100, 0.03f);
        this.type = type;
        cooldown = 0;
        setDamage(num, sides);
        rotation = 35;
        setType(type);
    }
    public void setOrgan(Organ p){
        this.organ = p;
    }
    public Organ getOrgan() {
        return organ;
    }
    protected void setModifier(PrefixMod mod){
        super.setModifier(mod);
        if (mod == PrefixMod.Broken) damageMod = .5f;
        else if (mod == PrefixMod.Old) damageMod = .8f;
        else if (mod == PrefixMod.Fine) damageMod = 1.2f;
        else if (mod == PrefixMod.Mastercraft) damageMod = 1.5f;
        else if (mod == PrefixMod.Legendary) damageMod = 2;
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
        return (int) (damage.getDamage()*damageMod);
    }
    public void setDamage(int num, int sides){
        damage = new Die(num, sides);
    }
    public Line getLine(){
        return new Line(pos.getAdd(organ.pt), getPoint());
    }
    public P getPoint(){
        P result = pos.getAdd(organ.ptd);
        result.add(size.x*.5f*Global.cos(rotation)*dir,(int) (size.x*.5f*Global.sin(rotation))*dir);
        return result;
    }
    public void drop(Man host){
        super.drop(host);
        organ = null;
    }
    public String getDescription(){
        String r = super.getDescription();
        r = r.replace(" uses "," durability ");
        if (emod != null){
            r += " This weapon has "+getProbability()*100+"% chance of activating its enchantment every hit.";
        }
        return r + " This weapon deals "+damage.num+"d"+damage.sides+" * "+damageMod+" damage.";
    }
    public void draw(Graphics2D g){
        super.draw(g, organ==null?P.getZero():organ.ptd);
    }
}
