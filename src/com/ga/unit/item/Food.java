package com.ga.unit.item;

import com.ga.map.Global;
import com.ga.map.ShadedPicture;
import com.ga.map.Shader;
import com.ga.unit.Man;
import com.ga.unit.player.Health;
import com.ga.unit.player.Player;
import com.ga.unit.skill.Buffs;
import com.ga.unit.skill.Stat;

import java.awt.*;

/**
 * Created by Ga on 2/17/2017. asd
 */
public class Food extends Item {
    private String nutrition;
    private int perish;
    private int happy = 0;

    public Food(String name, int seed, int weight, int uses, String nutrition, int perish, int happy) {
        super(name, seed, new Item.Material[]{Item.Material.Other}, ItemType.Consumable);
        this.weight = weight;
        this.durability = uses;
        this.uses = uses;
        this.nutrition = nutrition;
        this.perish = Global.time + perish;
        this.happy = happy;
        shuffleNutrition();
        int toxicity = getPoisonChance();
        if (toxicity > 0) {
            image = new ShadedPicture(rand, name, Shader.Color);
            ((ShadedPicture) image).shade(new Color(100 - toxicity, 255 - toxicity, 100 - toxicity, 180).getRGB());
        }
    }
    private void shuffleNutrition(){
        char[] data = nutrition.toCharArray();
        for (int i = 0; i < data.length; i++) {
            if (rand.nextInt(3+(data.length/4)) == 0){
                data[i] = Health.mutate(data[i],rand.nextFloat());
            } else {
                int r = (int) (rand.nextFloat() * data.length);
                char t = data[i];
                data[i] = data[r];
                data[r] = t;
            }
        }
        nutrition = new String(data);
    }
    @Override
    public void use(Man p){
        if (usable(1)) {
            super.use(p);
            int perishTime = perish - Global.time;
            if (p instanceof Player){
                Stat motivation = p.get("Motivation");
                if (perishTime <= 0 && rand.nextInt(10) < getPoisonChance()){
                    int severity = perishTime / 100;
                    p.buff(Buffs.Poison, severity);
                    p.say("Gawrrrr! That didn't taste good...");
                    for (int i=0;i<severity;i++) nutrition = Health.mutate(nutrition);
                    motivation.add(-severity, false);
                } else {
                    motivation.add(happy, false);
                }
                ((Player) p).health.record(nutrition);
            }
        }
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
        r += "\nNutrition Facts:\n"+ Health.getDescription(nutrition);
        return r;
    }
    public String[] load(String p){
        String[] r = super.load(p);
        perish = Integer.parseInt(p.split("@")[6]);
        return r;
    }
    @Override
    public String toString(){
        String r = super.toString();
        if (!r.equals("")) r += "@"+perish;
        return r;
    }
}
