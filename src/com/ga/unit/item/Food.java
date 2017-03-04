package com.ga.unit.item;

import com.ga.unit.Man;
import com.ga.unit.skill.Buffs;

/**
 * Created by Ga on 2/17/2017.
 */
public class Food extends Item {
    public int perish = 1;
    private int hunger = 3;
    private int punish;

    public Food(String name, int seed, int weight, int uses, int hunger, int perish, int punish) {
        super(name, seed, weight, uses, 0, 0);
        this.perish = perish;
        this.punish = punish;
        this.hunger = hunger;
    }
    public void onTimeChange(int p){
        perish-= p;
    }
    @Override
    public boolean use(Man p){
        boolean r = super.use(p);
        if (r) {
            if (perish <= 0 && rand.nextInt(100) < getPoisonChance()){
                p.stats.addEffect(Buffs.Poison, punish);
                p.say("Gawrrrr! That didn't taste good...");
                p.get("Hunger").add(-4);
            } else {
                p.get("Hunger").add(hunger);
            }
            if (uses.get() == 0) remove();
        }
        return r;
    }
    private int getPoisonChance(){
        return Math.min(10,1-perish)*10;
    }
    @Override
    public String getDescription(){
        String r = super.getDescription();
        r = r.replace(" uses "," servings ");
        if (perish <= 0) r += " This food has "+getPoisonChance()+"% chance of causing "+getPunishDescription()+".";
        else r += " This food will go bad in "+perish+" hours.";
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
