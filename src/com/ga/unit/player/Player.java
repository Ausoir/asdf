package com.ga.unit.player;

import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.Updater;
import com.ga.tool.Performer;
import com.ga.unit.*;
import com.ga.unit.intelligence.PlayerInput;
import com.ga.unit.item.Crafts;
import com.ga.unit.item.Item;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Moveset;
import com.ga.unit.skill.Stat;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Ga on 7/30/2016. asd
 */
public class Player extends Man {
    public String memorized = "";
    public int gold;
    public Inventory inventory;
    public SkillTree skillTree = new SkillTree();
    public Crafts crafting;
    public int selectingItem = -1;
    public PlayerInput input;
    public String cmap, cworld;
    public Health health;
    public Moveset learning;
    ArrayList<Trait> traits = new ArrayList<>();
    public Player(int seed) {
        super(seed);
        loadMoveset("-5,100,1000/Use&Walk&Sprint");
        speechType = "p";
        inventory = new Inventory(this);
        crafting = new Crafts(this);
        health = new Health(this);
    }
    public void learnSkill(Moveset p){
        say("Learned spell "+p.getName()+"!");
        skillTree.addSkill(p);
    }
    public void advanceSkill(Moveset p){
        if (get("Memory").require(1))
           skillTree.advanceSkill(p);
    }
    public void setWeapon(Item p){
        if (p == null){
            super.setWeapon(null);
        } else if (sheathe()){
            Sound.play("wield");
            super.setWeapon(p);
        } else {
            dropItem(weapon);
            super.setWeapon(p);
        }
    }
    boolean sheathe() {
        if (wieldingNone()) return true;
        if (inventory.sheathe(weapon)){
            weapon.remove();
            return true;
        }
        return false;
    }
    private void dropItem(Item p) {
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
    protected boolean tagCapable(Tags p){
        if (!tags.has(p)) {
            for (Tag i : p) {
                if (!tags.contains(i) && !armor.tags.contains(i)) return true;
            }
        }
        return true;
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
            } else if (r == 5) say("This skill is beyond my abilities!");
        }
        return r;
    }
    @Override
    public int getStateLevel(Moveset state){
        int difficulty = state.getDifficulty();
        if (difficulty == 0) return 1;
        int power = 0;
        for (Tag i : state.getTags().getTrainers()){
            power += get(i.toString()).get();
        }
        return Math.max(0,skillTree.getSkillLevel(state)-(difficulty-power));
    }
    @Override
    public int getStatPenalty() {
        return Math.max(0, armor.getWeight()+weapon.weight-get("Strength").get());
    }
    public float getSpeed() {
        return Math.max(0,super.getSpeed()-Math.max(0,inventory.weight+armor.getWeight()+weapon.weight-get("Strength").get()));
    }
    public void witnessDeath(Unit p){
        if (faction.canAttack(p.faction) && p.pos.getSubtract(pos).inOval(900,500) && learning != null){
            boolean fast = traits.contains(Trait.Fast_Learner) && get("Motivation").require(3);
            if (fast || get("Motivation").require(1)) {
                Stat progress = get("Progress");
                progress.add(Math.max(1,p.get("Level").get()*3-get("Level").get())*(fast?2:1),true);
                if (progress.isFull()) {
                    progress.set(progress.get()-progress.getMax(), true);
                    int difficulty = learning.getDifficulty();
                    Tags tags = learning.getTags().getTrainers();
                    int amount = 10 / tags.size();
                    int mod = 10 % tags.size();
                    for (Tag i : tags){
                        Stat tagStat = get(i.toString());
                        tagStat.add(amount/Math.max(1,tagStat.get()-difficulty));
                    }
                    progress.add(mod*10);
                }
            }
        }
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
        r += Global.getTime()[0]+";"+cmap+";"+cworld+";"+gold+";"+memorized+";"+learning+"\n";
        r += input.toString()+"\n";
        r += inventory.toString()+"\n";
        r += skillTree.toString()+"\n";
        r += crafting.toString()+"\n";
        r += health.toString()+"\n";
        for (Trait i : traits) r += i.name()+";";
        return r;
    }
    public void load(String p){
        String[] line = p.split("%");
        super.load(line[0]);
        setHead("Player");
        String[] other = line[1].split(";");
        cmap = other[1];
        cworld = other[2];
        gold = Integer.parseInt(other[3]);
        memorized = other[4];
        learning = SkillTree.getByName(other[5]);
        inventory.load(line[3]);
        skillTree.load(line[4]);
        crafting.load(line[5]);
        String[] traits = line[7].split(";");
        for (String i : traits){
            if (!i.equals("")){
                Trait trait = Trait.getByName(i);
                this.traits.add(trait);
                trait.onSet(this);
            }
        }
        health.load(line[6]);
        armor.onChange = new Performer(){
            public void perform(){
                inventory.setMaxSize(armor.getPockets());
            }
        };
        inventory.setMaxSize(armor.getPockets());
        health.record("");
    }
}