package com.ga.unit.item;

import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.unit.Organ;
import com.ga.unit.item.Item;

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
    public float length;
    public Organ organ = new Organ(new P(),0,0);
    public Weapon(String name, int seed, int weight, int uses, int num, int sides) {
        super(name, seed, weight, uses, 100);
        setDamage(num, sides);
        if (name != null) length = size.x/2;
        else length = 0;
        rotation = 35;
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
            damage.num = 1;
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
        return new Line(pos, getPoint());
    }
    public P getPoint(){
        P result = new P();
        if (length > 2) result.set((int)length*Global.cos(rotation)*dir,(int) (length*Global.sin(rotation))*dir);
        result.add(organ.ptd.getAdd(pos));
        return result;
    }
    public String getDescription(){
        String r = super.getDescription();
        r = r.replace(" uses "," durability ");
        return r + " This weapon deals "+damage.num+"D"+damage.sides+" damage.";
    }
    public void draw(Graphics2D g){
        super.draw(g, organ==null?P.getZero():organ.ptd);
    }
}
