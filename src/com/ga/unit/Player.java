package com.ga.unit;

import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.intelligence.PlayerInput;
import com.ga.unit.item.Inventory;
import com.ga.unit.item.Item;
import com.ga.unit.skill.*;

import java.util.ArrayList;
import java.util.HashMap;

import static com.ga.unit.BodyHumanoid.FIST;

/**
 * Created by Ga on 7/30/2016.
 */
public class Player extends Man {
    private HashMap<Moveset, int[]> cooldown = new HashMap<>();
    protected String memorized = "";
    public int gold;
    public Inventory inventory;
    public SkillTree skillTree = new SkillTree();
    public int selectingItem = -1;
    public Line rope;
    public PlayerInput input;
    public Player(String p) {
        super(p);
        speechType = "p";
        inventory = new Inventory(this);
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
        Sound.play("wield");
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
    protected int[] getCooldown(Moveset state){
        int level = skillTree.getSkillCdLevel(state);
        int reduction;
        if (level == 0) reduction = 0;
        else {
            float cd = state.getCooldown() * (float) (1f / (Math.pow(2, level)));
            reduction = (int) ((state.getCooldown() - cd)) - level;
        }
        int[] r = cooldown.get(state);
        if (r == null) r = new int[]{0};
        return new int[]{r[0]-reduction};
    }
    @Override
    public int setState(Moveset state){
        int[] cdinput = getCooldown(state);
        int prevCd = cdinput[0];
        int r;
        if (Global.cooldown(cdinput, state.getCooldown())) {
            cooldown.put(state, cdinput);
            r = super.setState(state);
            if (r != 0){
                cooldown.put(state, new int[]{prevCd});
                if (r == 2) {
                    int req = (int) state.getCost();
                    req += getStatPenalty();
                    if (req > get("Stamina").getMax()) say("This skill is beyond my abilities!");
                    else say("I'm too exhausted!");
                }
            }
            return r;
        } else {
            say(state.getName() + " is available in " + Global.checkCooldown(cdinput, state.getCooldown()) + " seconds!");
            return 3;
        }
    }
    @Override
    public void stateSet(Moveset state){
        if (state == Skills.Sprint){
            int t = input.moving;
            if (t == 0) t = dir;
            face(t);
        }
        super.stateSet(state);
        stateLevel = skillTree.getSkillLevel(state);
        if (weapon.getType() == Item.Tag.Claw) weapon.rotation = (weapon.getOrgan().getAtan()+(dir<0?3.14f:0));
        if (state instanceof Actions) {
            switch ((Actions) state) {
                case Idle:
                    if (!get("Stamina").isFull()) clip.setState(Actions.Exhausted);
                    break;
                case Use:
                    if (weapon.uses.get() == 0) say("This item is no longer usable...");
                    break;
                case Interact:
                    P reach = pos.getAdd(clip.organs[FIST].ptd);
                    ArrayList<Matter> obj = Updater.collideInteractive(reach);
                    for (Matter i : obj) {
                        if (i.onInteract != null) {
                            i.onInteract.interact(this);
                            break;
                        }
                    }
                    for (Line i : Global.map.land) {
                        if (i.opt == 1) {
                            if (i.lineCross(new Line(reach.x, reach.y, reach.x + (24 * dir), reach.y))) {
                                delay.set(0);
                                pos.x = i.pt.x-(24*dir);
                                setState(Actions.Climb);
                                rope = i;
                            }
                        }
                    }
                    break;
                case Walk:
                    if (input.moving != dir) clip.setState(Actions.WalkL);
                    break;
                case Wield:
                    delay.performer = new Performer() {
                        public void perform() {
                            if (selectingItem == -1) {
                                sheathe();
                            } else if (selectingItem < inventory.getItemSize()) {
                                Item item = inventory.getItem(selectingItem);
                                setWeapon(item);
                                inventory.remove(item);
                                selectingItem = -1;
                            } else if (selectingItem >= 10 && selectingItem <= 16){
                                Sound.play("unwear");
                                int piece = selectingItem-10;
                                if (sheathe() && !armor.isBare(piece)) {
                                    setWeapon(armor.armor[piece]);
                                    armor.set(null, piece, true);
                                }
                            }
                        }
                    };
                    break;
            }
        } else if (state instanceof Skills){
            switch ((Skills) state){
                case Duck:
                    cooldown.put(state,new int[]{Global.getTime()[0]+((armor.armor[1].encumberance+armor.armor[2].encumberance)*4)});
                    break;
                case Guard:
                    cooldown.put(state,new int[]{Global.getTime()[0]+((armor.armor[FIST].encumberance+armor.armor[0].encumberance)*4)});
                    break;
                case Snipe:
                case Shoot:
                    weapon.rotate(target.getMult(dir,dir));
                    weapon.rotation += .1*dir;
            }
            if (this.state instanceof Skills) {
                Moveset a = this.state;
                Moveset b = state;
                if (a != b) {
                    delay.set(0);
                }
            }
        }
    }
    @Override
    public int getStatPenalty() {
        return Math.max(0, armor.getWeight()+weapon.weight-get("Strength").get());
    }
    public float getSpeed() {
        float s = super.getSpeed();
        float t = Global.clamp(s+get("Strength").get()-inventory.weight-getStatPenalty(),0,s);
        clip.setMoveSpeed((int) Global.clamp(t-1,-20,4));
        return t;
    }
    protected void stateDone(){
        super.stateDone();
        if (input.moving != 0){
            delay.performer = new Performer(){
                public void perform(){
                    setState(Actions.Walk);
                }
            };
        }
    }
    @Override
    public void stateUpdate(){
        super.stateUpdate();
        if (input.moving == 0 && state == Actions.Walk){
            setState(Actions.Idle);
        } else if (state == Actions.Walk) vel.x = getSpeed()*input.moving;
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