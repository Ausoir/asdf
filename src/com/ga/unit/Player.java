package com.ga.unit;

import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.tool.Performer;
import com.ga.unit.intelligence.PlayerInput;
import com.ga.unit.item.Crafts;
import com.ga.unit.item.Inventory;
import com.ga.unit.item.Item;
import com.ga.unit.skill.Moveset;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Ga on 7/30/2016.
 */
public class Player extends Man {
    public String memorized = "";
    public int gold;
    public Inventory inventory;
    public SkillTree skillTree = new SkillTree();
    public Crafts crafting;
    public int selectingItem = -1;
    public PlayerInput input;
    public String cmap;
    public Player(String p) {
        super(p.substring(1));
        speechType = "p";
        inventory = new Inventory(this);
        crafting = new Crafts(this);
        armor.onChange = new Performer(){
            public void perform(){
                inventory.setMaxSize(armor.getPockets());
            }
        };
        inventory.setMaxSize(armor.getPockets());
    }
    public void learnSkill(Moveset p){
        say("Learned spell "+p.getName()+"!");
        skillTree.addSkill(p);
    }
    public void advanceSkill(Moveset p, boolean cd){
        if (get("Memory").require(1))
           skillTree.advanceSkill(p, cd);
    }
    public void setWeapon(Item p){
        if (p == null){
            super.setWeapon(p);
        } else if (sheathe()){
            Sound.play("wield");
            super.setWeapon(p);
        } else{
            dropItem(weapon);
            super.setWeapon(p);
        }
    }
    public boolean sheathe() {
        if (wieldingNone()) return true;
        if (inventory.sheathe(weapon)){
            weapon.remove();
            return true;
        }
        return false;
    }
    public void dropItem(Item p) {
        Item t = inventory.remove(p);
        if (t != null) {
            t.drop(this);
        } else if (p == weapon) {
            weapon.drop(this);
            setWeapon(null);
        } else return;
        say("Dropping " + p.getFullName());
    }
    @Override
    protected int[] getCooldown(Moveset state){
        int level = skillTree.getSkillCdLevel(state);
        int[] r = cooldown.get(state);
        if (r == null) r = new int[]{0};
        int reduction = 0;
        float stateCd = state.getCooldown();
        if (stateCd > 0) {
            float cd = stateCd * (float) (1f / (Math.pow(2, level)));
            reduction = (int) (stateCd - cd) - level;
        }
        return new int[]{r[0]-reduction};
    }
    @Override
    public int setState(Moveset state){
        int r = super.setState(state);
        if (r != 0){
            if (r == 2) {
                int req = (int) state.getCost();
                req += getStatPenalty();
                if (req > get("Stamina").getMax()) say("This skill is beyond my abilities!");
                else say("I'm too exhausted!");
            } else if (r == 3){
                say(state.getName() + " is available in " + Global.checkCooldown(getCooldown(state), state.getCooldown()) + " seconds!");
            } else if (r == 4){
                say("Cannot cast!");
            }
        }
        return r;
    }
    @Override
    public int getStateLevel(Moveset state){
        return skillTree.getSkillLevel(state);
    }
    @Override
    public int getStatPenalty() {
        return Math.max(0, armor.getWeight()+weapon.weight-get("Strength").get());
    }
    public float getSpeed() {
        float t = Math.max(0,super.getSpeed()-Math.max(0,inventory.weight+armor.getWeight()+weapon.weight-get("Strength").get()));
        return t;
    }
    public ArrayList<Prop> getAvailableItems(){
        ArrayList<Prop> items = Updater.collideProp(pos,100);
        Iterator it = items.iterator();
        while (it.hasNext()){
            if (!(it.next() instanceof Item)) it.remove();
        }
        if (!wieldingNone()) items.add(weapon);
        items.addAll(inventory.getItems());
        return items;
    }
    public String toCharacterString(){
        String r = super.toString()+"\n";
        r += cmap+";"+gold+";"+memorized+";"+Global.getTime()[0]+";"+Global.gameHour+"\n";
        r += input.toString()+"\n";
        r += inventory.toString()+"\n";
        r += skillTree.toString()+"\n";
        r += crafting.toString();
        return r;
    }
    public void load(String p){
        String[] line = p.split("%");
        String[] other = line[1].split(";");
        cmap = other[0];
        gold = Integer.parseInt(other[1]);
        memorized = other[2];
        inventory.load(line[3]);
        skillTree.load(line[4]);
        crafting.load(line[5]);
    }
}