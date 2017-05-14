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
    private int punish;
    private int happy = 0;

    public Food(String name, int seed, int weight, int uses, String nutrition, int perish, int punish, int happy) {
        super(name, seed, weight, uses);
        this.nutrition = nutrition;
        this.perish = Global.getHour() + perish;
        this.punish = punish;
        this.happy = happy;
        shuffleNutrition();
        int toxicity = getPoisonChance();
        if (toxicity > 0) {
            image = new ShadedPicture(name, Shader.Color);
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
            int perishTime = perish - Global.getHour();
            if (p instanceof Player){
                Stat motivation = p.get("Motivation");
                if (perishTime <= 0 && rand.nextInt(100) < getPoisonChance()){
                    if (punish > 3) p.buff(Buffs.Poison, punish-3);
                    p.say("Gawrrrr! That didn't taste good...");
                    for (int i=0;i<punish;i++) nutrition = Health.mutate(nutrition);
                    motivation.add(-punish, false);
                } else {
                    motivation.add(happy, false);
                }
                ((Player) p).health.record(nutrition);
            }
        }
    }
    private int getPoisonChance(){
        int perishTime = perish - Global.getHour();
        return Math.min(10,1-perishTime)*10;
    }
    @Override
    public String getDescription(){
        int perishTime = perish - Global.getHour();
        String r = super.getDescription();
        r = r.replace(" uses "," servings ");
        if (perishTime <= 0) r += " This food has "+getPoisonChance()+"% chance of causing "+getPunishDescription()+".";
        else r += " This food will go bad in "+perishTime+" hours.";
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

    private String getPunishDescription() {
        if (punish > 8) return "certain death";
        else if (punish > 2) return "cancer";
        else if (punish > 0) return "food poisoning";
        else return "bad breath";
    }
}
