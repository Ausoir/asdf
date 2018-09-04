package com.ga.unit.item;

import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.map.ShadedPicture;
import com.ga.map.Shader;
import com.ga.tool.T;
import com.ga.unit.man.Man;
import com.ga.unit.skill.Buffs;

import java.awt.*;

/**
 * Created by Ga on 2/17/2017. asd
 */
public class Food extends Item {
    private int nutrition, taste, perish;
    public Food(Map map, String name, int seed, int uses) {
        super(map, name, seed, Material.Organic, ItemType.Consumable, uses);
        onTimeChange();
    }
    public void set(int nutrition, int taste, int perish){
        this.nutrition = nutrition;
        this.taste = taste;
        this.perish = Global.hour + perish;
    }
    public int getValue(){
        return (int) Math.max(0, ((uses*(taste+nutrition)+.4f*(perish-Global.hour))*100));
    }
    @Override
    public boolean use(Man p){
        if (isUsable(true, 1) && super.use(p)) {
            int perishTime = perish - Global.hour;
            if (perishTime <= 0 && seed % 10 < getPoisonChance()){
                p.buff(Buffs.Poison, 1);
                p.say("Gawrrrr! That didn't taste good...");
            } else {
                p.stats.get("Motivation").add(taste, false);
            }
            p.stats.get("Nutrition").add(nutrition, false);
            return true;
        }
        return false;
    }
    private int getPoisonChance(){ // 0 to 10
        return (int) Global.clamp(Global.hour-perish, 0, 10);
    }
    @Override
    public String getDescription(){
        int perishTime = perish - Global.hour;
        String r = super.getDescription();
        r = r.replace(" uses "," servings ");
        r += " This food provides "+nutrition+" Nutrition points and "+taste+" Motivation points.";
        if (perishTime < 0) r += " This food has "+getPoisonChance()+"0% chance of causing food poisoning.";
        else r += " This food will go bad in "+perishTime+" hours.";
        return r;
    }
    public void onTimeChange(){
        int toxicity = getPoisonChance();
        if (toxicity > 0) {
            image = new ShadedPicture(rand, name, Shader.Color);
            ((ShadedPicture) image).shade(new Color(100 - toxicity, 255 - toxicity, 100 - toxicity, 180).getRGB());
        }
    }
    public void load(T p){
        super.load(p);
        nutrition = p.get(9).getInt();
        taste = p.get(10).getInt();
        perish = p.get(11).getInt();
    }
    @Override
    public String toString(){
        String r = super.toString();
        if (!r.equals("")) r += ","+nutrition+","+taste+","+perish;
        return r;
    }
}
