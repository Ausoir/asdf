package com.ga.unit.item;

import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.map.ShadedPicture;
import com.ga.map.Shader;
import com.ga.unit.man.Man;
import com.ga.unit.skill.Buffs;
import com.ga.unit.skill.Stat;

import java.awt.*;

/**
 * Created by Ga on 2/17/2017. asd
 */
public class Food extends Item {
    public enum Material{
        White(500,3),Red(800,3),Fish(600,3),Vegetable(100,10),Grain(200,8),Fruit(300,5),Boiled(200,-3),Fried(400,2),Roasted(300,0),Smoked(300,10),Dried(1000,10);
        int value, perish;
        Material(int value, int perish){
            this.value = value;
            this.perish = perish;
        }
    }
    public class Type{
        Material[] materials;
        int getValue(){
            int result = 0;
            for (Material i : materials) result += i.value;
            return result;
        }
        void load(String p){
            String[] mats = p.split(",");
            materials = new Material[mats.length];
            for (int i=0;i<mats.length;i++){
                materials[i] = Material.valueOf(mats[i]);
            }
        }
        String getDescription(){
            String result = "";
            for (Material i : materials) result += i+", ";
            return result.substring(0,result.length()-2);
        }
    }
    private int perish;
    private Type type = new Type();
    public Food(Map map, String name, int seed, int size, String materials) {
        super(map, name, seed, new Item.Material[]{Item.Material.Flesh}, ItemType.Consumable, size);
        type.load(materials);
        this.weight = 1;
        this.perish = Global.time + perish;
        int toxicity = getPoisonChance();
        if (toxicity > 0) {
            image = new ShadedPicture(rand, name, Shader.Color);
            ((ShadedPicture) image).shade(new Color(100 - toxicity, 255 - toxicity, 100 - toxicity, 180).getRGB());
        }
    }
    public int getValue(){
        return type.getValue();
    }
    @Override
    public boolean use(Man p){
        if (isUsable(true, 1) && super.use(p)) {
            int perishTime = perish - Global.time;
            Stat motivation = p.get("Motivation");
            if (perishTime <= 0 && rand.nextInt(10) < getPoisonChance()){
                int severity = perishTime / 100;
                p.buff(Buffs.Poison, severity);
                p.say("Gawrrrr! That didn't taste good...");
                motivation.add(-severity, false);
            } else {
                int likes = 0;
                for (Material i : type.materials){
                    for (Material ii : p.favorites){
                        if (i == ii) likes++;
                    }
                }
                motivation.add(Math.max(0, type.getValue()/100-perish) + likes*4, false);
            }
            Stat hunger = p.get("Nutrition");
            if (hunger != null) hunger.add(1, false);
            return true;
        }
        return false;
    }
    private int getPoisonChance(){ // 0 to 10
        return (Global.time-perish)/100;
    }
    @Override
    public String getDescription(){
        int perishTime = perish - Global.time;
        String r = super.getDescription();
        r = r.replace(" uses "," servings ");
        if (perishTime <= 0) r += " This food has "+getPoisonChance()+"0% chance of causing food poisoning.";
        else r += " This food will go bad in "+perishTime+" seconds.";
        r += "\nDetail:\n" + type.getDescription();
        return r;
    }
    public String[] load(String p){
        String[] r = super.load(p);
        perish = Integer.parseInt(p.split("@")[8]);
        return r;
    }
    @Override
    public String toString(){
        String r = super.toString();
        if (!r.equals("")) r += "@"+perish;
        return r;
    }
}
