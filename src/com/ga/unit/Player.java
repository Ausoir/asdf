package com.ga.unit;

import com.ga.map.Global;
import com.ga.tool.Performer;
import com.ga.unit.intelligence.PlayerInput;
import com.ga.unit.item.Inventory;
import com.ga.unit.item.Item;
import com.ga.unit.skill.*;

import java.util.ArrayList;

import static com.ga.unit.BodyHumanoid.FIST;

/**
 * Created by Ga on 7/30/2016.
 */
public class Player extends Man {
    protected String memorized = "";
    public int gold;
    public Inventory inventory = new Inventory();
    public SkillTree skillTree = new SkillTree();
    public int selectingItem = -1;
    public PlayerInput input;
    public Player(String p) {
        super(p);
        speechType = "p";
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
        if (p == null || sheathe()) super.setWeapon(p);
        else{
            dropItem(weapon);
            super.setWeapon(p);
        }
    }
    public boolean sheathe() {
        if (wieldingNone()) return true;
        if (inventory.sheathe(weapon)){
            weapon.remove();
            setWeapon(null);
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
        if (level == 0) return super.getCooldown(state);
        float cd = state.getCooldown() * (float)(1f/(Math.pow(2,level)));
        int reduction = (int) ((state.getCooldown()-cd))-level;
        return new int[]{super.getCooldown(state)[0]-reduction};
    }
    @Override
    protected void startSkill(Moveset move, Spell p){
        p.start(this, skillTree.getSkillLevel(move));
    }
    @Override
    protected void actionSet(Moveset state, int level){
        super.actionSet(state, skillTree.getSkillLevel(state));
        if (state instanceof Actions) {
            switch ((Actions) state) {
                case Wield:
                    delay.performer = new Performer() {
                        public boolean perform() {
                            if (selectingItem == -1) {
                                sheathe();
                                return false;
                            } else if (selectingItem < inventory.getItemSize()) {
                                Item item = inventory.getItem(selectingItem);
                                setWeapon(item);
                                inventory.remove(item);
                                selectingItem = -1;
                            } else if (selectingItem >= 10 && selectingItem <= 16){
                                if (sheathe()) {
                                    setWeapon(armor.armor[selectingItem-10]);
                                    armor.set(null,selectingItem-10);
                                }
                            }
                            return true;
                        }
                    };
                    break;
            }
        }
    }
    public int setState(Moveset state){
        if (state instanceof Actions) {
            switch ((Actions) state) {
                case Idle:
                    if (!get("Stamina").isFull()) state = Actions.Exhausted;
                    break;
                case Interact:
                    ArrayList<Matter> obj = Updater.collide(pos.getAdd(clip.organs[FIST].ptd), 12);
                    for (Matter i : obj) {
                        if (i.interactive) {
                            i.interact(this);
                            break;
                        }
                    }
                    break;
            }
        } else if (state instanceof Skills) {
            switch ((Skills) state) {
                case Snipe:
                case Shoot:
                    weapon.rotate(target.getMult(dir,dir));
                    weapon.rotation += .1*dir;
            }
            if (this.state instanceof Skills) {
                Moveset a = this.state;
                Moveset b = state;
                if (a == Skills.Use) a = weapon.action;
                if (b == Skills.Use) b = weapon.action;
                if (a != b) {
                    delay.set(0);
                }
            }
        }
        if (weapon.getType() == Item.Tag.Claw) weapon.rotation = (weapon.getOrgan().getAtan()+(dir<0?3.14f:0));
        int r = super.setState(state);
        if (r == 2){
            int req = (int) state.getCost();
            req += getStatPenalty();
            if (req > get("Stamina").getMax()) say("This skill is beyond my abilities!");
            else say("Need " + (req - get("Stamina").get()) + " more stamina!");
        } else if (r == 3){
            int[] cdinput = getCooldown(state);
            say(state.getName() + " is available in "+Global.checkCooldown(cdinput, state.getCooldown())+" seconds!");
        } else if (r == 4){
            say("Item broke!");
        }
        return r;
    }
    @Override
    public int getStatPenalty() {
        return (armor.getWeight()+weapon.weight)/get("Strength").get();
    }
    protected float getSpeed() {
        float t = Math.max(0,super.getSpeed()-(inventory.weight/get("Strength").get())-getStatPenalty());
        clip.setMoveSpeed((int) Global.clamp(t-1,-20,4));
        return t;
    }
    protected void stateDone(){
        if (input.moving == 0) super.stateDone();
        else if (input.moving == -1) setState(Actions.WalkL);
        else if (input.moving == 1) setState(Actions.WalkR);
    }
    @Override
    public void stateUpdate(){
        super.stateUpdate();
        if (input.moving == 0 && (state == Actions.WalkL || state == Actions.WalkR)){
            setState(Actions.Idle);
        }
    }
    public String toCharacterString(){
        String r = super.toString()+"\n";
        r += gold+";"+memorized+";"+Global.getTime()[0]+"\n";
        r += input.toString()+"\n";
        r += inventory.toString()+"\n";
        r += skillTree.toString();
        return r;
    }
    public void load(String p){
        String[] line = p.split("%");
        gold = Integer.parseInt(line[1].split(";")[0]);
        memorized = line[1].split(";")[1];
        inventory.load(line[3]);
        skillTree.load(line[4]);
    }
}