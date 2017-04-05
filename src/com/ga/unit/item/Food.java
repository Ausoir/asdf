package com.ga.unit.item;

import com.ga.map.Global;
import com.ga.unit.Man;
import com.ga.unit.skill.Buffs;

/**
 * Created by Ga on 2/17/2017.
 */
public class Food extends Item {
    public int perish;
    private int hunger = 3;
    private int punish;

    public Food(String name, int seed, int weight, int uses, int hunger, int perish, int punish) {
        super(name, seed, weight, uses);
        this.perish = Global.gameHour + perish;
        this.punish = punish;
        this.hunger = hunger;
    }
    @Override
    public boolean use(Man p){
        boolean r = super.use(p);
        if (r && use(1)) {
            int perishTime = perish - Global.gameHour;
            if (perishTime <= 0 && rand.nextInt(100) < getPoisonChance()){
                p.stats.addEffect(Buffs.Poison, punish);
                p.say("Gawrrrr! That didn't taste good...");
                p.get("Hunger").add(-4);
            } else {
                p.get("Hunger").add(hunger);
            }
        }
        return r;
    }
    private int getPoisonChance(){
        int perishTime = perish - Global.gameHour;
        return Math.min(10,1-perishTime)*10;
    }
    @Override
    public String getDescription(){
        int perishTime = perish - Global.gameHour;
        String r = super.getDescription();
        r = r.replace(" uses "," servings ");
        if (perishTime <= 0) r += " This food has "+getPoisonChance()+"% chance of causing "+getPunishDescription()+".";
        else r += " This food will go bad in "+perishTime+" hours.";
        return r;
    }
    @Override
    public String toString(){
        return super.toString()+"@"+perish;
    }

    public String getPunishDescription() {
        if (punish > 8) return "certain death";
        else if (punish > 2) return "cancer";
        else if (punish > 0) return "food poisoning";
        else return "bad breath";
    }
}
