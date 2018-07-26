package com.ga.unit.item;

import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.map.ShadedPicture;
import com.ga.map.Shader;
import com.ga.unit.man.Man;
import com.ga.unit.skill.Buffs;
import com.ga.unit.skill.Stat;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Ga on 2/17/2017. asd
 */
public class Food extends Item {
    public enum Ingredient {
        White(1,3,-10,3),Red(1,3,-10,2),Fish(1,2,-2,1),Dairy(1,2,1,0),Vegetable(1,1,-2,8),Grain(1,2,0,10),Fruit(1,1,4,7),
        Cut(2,0,0,-1,.8f),Boiled(2,1,-1,1,.5f),Fried(2,0,5,6,.4f),Roasted(2,1,3,2,.6f),Smoked(2,0,0,1,.6f),Dried(2,-1,-1,24,.7f),Spiced(2,0,5,-8,.7f),Sealed(2,0,0,30,.3f);
        int type, nutrition, taste, perish;
        float loss = .5f;
        Ingredient(int type, int nutrition, int taste, int perish){
            this.type = type;
            this.nutrition = nutrition;
            this.taste = taste;
            this.perish = perish;
        }
        Ingredient(int type, int nutrition, int taste, int perish, float loss){
            this(type, nutrition, taste, perish);
            this.loss = loss;
        }
        public static ArrayList<Ingredient> getByType(int p){
            ArrayList<Ingredient> r = new ArrayList<>();
            for (Ingredient i : values()){
                if (i.type == p) r.add(i);
            }
            return r;
        }
    }
    public class Type{
        ArrayList<Ingredient> ingredients;
        int getValue(){
            return (getNutrition()*100) + (getMotivation()*2) + (getPerish()*20);
        }
        int getNutrition(){
            int result = 0;
            for (Ingredient i : Ingredient.values()){
                if (ingredients.contains(i)) result += i.nutrition;
            }
            return result;
        }
        int getMotivation(){
            int result = 0;
            for (Ingredient i : ingredients){
                result = Math.max(result, i.taste);
            }
            return result;
        }
        int getPerish(){
            int result = 0;
            for (Ingredient i : ingredients) result += i.perish;
            int min = ingredients.get(0).perish;
            for (Ingredient i : ingredients) min = Math.min(result, i.perish);
            return (result+min) / (ingredients.size()+1);
        }
        void load(String p){
            ingredients = new ArrayList<>();
            String[] mats = p.split(",");
            for (String mat : mats) {
                ingredients.add(Ingredient.valueOf(mat));
            }
        }
        String getDescription(){
            String result = "";
            for (Ingredient i : ingredients) result += i+", ";
            return result.substring(0,result.length()-2);
        }
        public String toString(){
            String r = "";
            for (Ingredient i : ingredients) r += i+",";
            return r.substring(0,r.length()-1);
        }
    }
    private int perish;
    private Type type = new Type();
    public Food(Map map, String name, int seed, int uses, String materials) {
        super(map, name, seed, new Item.Material[]{Item.Material.Organic}, ItemType.Consumable, uses);
        type.load(materials);
        this.perish = Global.hour + type.getPerish();
        onTimeChange();
    }
    public void cook(Ingredient ingredient){
        type.ingredients.add(ingredient);
        perish = (((perish - Global.hour)+ingredient.perish)/2)+Global.hour;
    }
    public void combine(Food food, Ingredient method){
        for (Ingredient i : food.type.ingredients){
            if (i.type == 1){
                cook(i);
            }
        }
        durability += food.getUses();
        durability *= method.loss;
        isUsable(true, -food.getUses());
        food.remove();
    }
    public int getValue(){
        return (type.getValue()*(10-getPoisonChance())*getUses())/10;
    }
    @Override
    public boolean use(Man p){
        if (isUsable(true, 1) && super.use(p)) {
            int perishTime = perish - Global.hour;
            Stat motivation = p.get("Motivation");
            if (perishTime <= 0 && rand.nextInt(10) < getPoisonChance()){
                p.buff(Buffs.Poison, 1);
                p.say("Gawrrrr! That didn't taste good...");
                motivation.add(getMotivation(), false);
            } else {
                motivation.add(getMotivation(), false);
            }
            p.get("Nutrition").add(type.getValue()/2, false);
            return true;
        }
        return false;
    }
    private int getPoisonChance(){ // 0 to 10
        return (int) Global.clamp(Global.hour-perish, 0, 10);
    }
    private int getMotivation(){
        return Math.max(0, type.getMotivation() - getPoisonChance());
    }
    @Override
    public String getDescription(){
        int perishTime = perish - Global.hour;
        String r = super.getDescription();
        r = r.replace(" uses "," servings ");
        r += " This food provides "+type.getNutrition()+" Nutrition points and "+getMotivation()+" Motivation points.";
        if (perishTime <= 0) r += " This food has "+getPoisonChance()+"0% chance of causing food poisoning.";
        else r += " This food will go bad in "+perishTime+" hours.";
        r += "\nDetail:\n" + type.getDescription();
        return r;
    }
    public void onTimeChange(){
        int toxicity = getPoisonChance();
        if (toxicity > 0) {
            image = new ShadedPicture(rand, name, Shader.Color);
            ((ShadedPicture) image).shade(new Color(100 - toxicity, 255 - toxicity, 100 - toxicity, 180).getRGB());
        }
    }
    public String[] load(String p){
        String[] r = super.load(p);
        perish = Integer.parseInt(r[8]);
        type.load(r[9]);
        name = r[10];
        return r;
    }
    @Override
    public String toString(){
        String r = super.toString();
        if (!r.equals("")) r += "@"+perish+"@"+type.toString()+"@"+name;
        return r;
    }
}
