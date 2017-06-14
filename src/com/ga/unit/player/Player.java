package com.ga.unit.player;

import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.Updater;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.*;
import com.ga.unit.PlayerInput;
import com.ga.unit.item.Crafts;
import com.ga.unit.item.Item;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import static com.ga.unit.BodyHumanoid.FIST;
import static com.ga.unit.BodyHumanoid.OFIST;

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
    public Actions learning;
    public Item offhand = fist;
    private boolean leftHandMode = false;
    private ArrayList<Trait> traits = new ArrayList<>();
    public Player(String name, int seed) {
        super(name, seed);
        loadActions("-5,100,1000/Use&Walk&Sprint");
        speechType = "p";
        inventory = new Inventory(this);
        crafting = new Crafts(this);
        health = new Health(this);
        Collections.addAll(tags, Tags.trainers);
    }
    public void learnSkill(Actions p){
        say("Learned spell "+p.getName()+"!");
        skillTree.addSkill(p);
    }
    public void advanceSkill(Actions p){
        if (get("Memory").require(1))
           skillTree.advanceSkill(p);
    }
    public void setWeapon(Item p){
        if (!leftHandMode){
            if (p != null) {
                if (sheathe()) {
                    Sound.play(pos, "wield");
                } else {
                    dropItem(weapon);
                }
            }
            super.setWeapon(p);
        } else {
            if (p == null) p = fist;
            offhand.onUnequip(this);
            offhand = p;
            offhand.onEquip(this);
            offhand.setDepth(getDepth()+1);
            offhand.dir = dir;
        }
    }
    private boolean sheathe() {
        if (wieldingNone()) return true;
        if (inventory.addItem(weapon)){
            weapon.pos = Global.offplace;
            setWeapon(null);
            return true;
        }
        return false;
    }
    public boolean offhandNone(){
        return offhand == fist;
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
        if (p.size() > 0) {
            if (!tags.has(p)) {
                for (Tag i : p) {
                    if (!tags.contains(i) && !armor.tags.contains(i)) return true;
                }
            }
        }
        return true;
    }
    @Override
    public int setState(Actions state){
        int r = super.setState(state);
        if (r != 0){
            if (r == 2) {
                if (state.getCost() > get("Stamina").getMax()) say("Not enough Stamina!");
            } else if (r == 4) {
                say("Cannot cast!");
            } else if (r == 5) say("This action is beyond my abilities!");
        }
        return r;
    }
    public void stateSet(Actions state){
        if (state.getDel() > 0 && getVacancy() < state.getEncumberance()+weapon.encumberance){
            attackSpeed -= 50;
        }
        super.stateSet(state);
        final Player me = this;
        switch (state) {
            case Idle:
                if (!get("Stamina").isFull()) clip.setState(Actions.Exhausted.getClipName());
                break;
            case Sprint:
            case Walk:
                float speed = getSpeed();
                if (speed <= 0) clip.setState(Actions.Exhausted.getClipName());
                else clip.setMoveSpeed((int) Global.clamp(speed-1,-20,4));
                face(input.moving, true);
                break;
            case Interact:
                delay.performer = new Performer() {
                    public void perform() {
                        P reach = pos.getAdd(clip.organs[FIST].ptd);
                        ArrayList<Prop> obj = Updater.collideInteractive(reach);
                        for (Prop i : obj) {
                            if (i.onInteract != null) {
                                i.onInteract.interact(me);
                                break;
                            }
                        }
                    }
                };
                break;
            case Discard:
                setWeapon(null);
                stateSet(Actions.Idle);
                break;
            case Grab:
                if (offhand == fist) {
                    delay.performer = new Performer() {
                        public void perform() {
                            P reach = pos.getAdd(clip.organs[OFIST].ptd);
                            ArrayList<Prop> obj = Updater.collideInteractive(reach);
                            for (Prop i : obj) {
                                if (i != weapon && i != offhand && i.onInteract != null) {
                                    leftHandMode = true;
                                    i.onInteract.interact(me);
                                    leftHandMode = false;
                                    break;
                                }
                            }
                        }
                    };
                } else {
                    leftHandMode = true;
                    setWeapon(null);
                    leftHandMode = false;
                    stateSet(Actions.Idle);
                }
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
                        } else if (selectingItem >= 10 && selectingItem <= 16) {
                            Sound.play(pos, "unwear");
                            int piece = selectingItem - 10;
                            if (sheathe() && !armor.isBare(piece)) {
                                Updater.add(armor.armor[piece]);
                                setWeapon(armor.armor[piece]);
                                armor.set(null, piece);
                            }
                        }
                    }
                };
                break;
        }
    }
    public int getStatePower(Actions state){
        int power = 0;
        int num = 0;
        for (Tag i : state.getTrainers()){
            power += get(i.toString()).get();
            num++;
        }
        if (num == 0) return 1;
        return (power / num) / 100;
    }
    @Override
    public int getStateLevel(Actions state){
        int difficulty = state.getDifficulty();
        if (difficulty == 0) return 1;
        int power = getStatePower(state);
        if (state == Actions.Rest) power -= armor.armor[0].weight;
//        return Math.max(0,skillTree.getSkillLevel(state)-(difficulty-power));
        return 1;
    }
    public float getSpeed() {
        float speed = get("Speed").get()*clip.getLegStatus();
        float penalty = Math.max(0,inventory.weight+armor.getWeight()+weapon.weight+offhand.weight-get("Strength").get()*2);
        return Math.max(0,speed-penalty)*.01f;
    }
    public boolean hasTrait(Trait p){
        return traits.contains(p);
    }
    public void witnessDeath(Unit p){
        if (faction.canAttack(p.faction) && p.pos.getSubtract(pos).inOval(900,500) && learning != null){
            boolean fast = hasTrait(Trait.Fast_Learner) && get("Motivation").require(3);
            if (fast || get("Motivation").require(1)) {
                Stat progress = get("Progress");
                progress.add(Math.max(1,p.get("Level").get()*3-get("Level").get())*(fast?2:1),true);
                if (progress.isFull()) {
                    learnSkill(learning);
                    learning = null;
                    progress.add(-progress.getMax(), true);
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
        items.addAll(inventory.getItems());
        return items;
    }
    public void update(){
        super.update();
        offhand.pos.set(pos.x + clip.organs[OFIST].ptd.x, pos.y + clip.organs[OFIST].ptd.y);
    }
    public String toCharacterString(){
        String r = super.toString()+"\n";
        r += Global.time+";"+cmap+";"+gold+";"+memorized+";"+learning+"\n";
        r += input.toString()+"\n";
        r += inventory.toString()+"\n";
        r += skillTree.toString()+"\n";
        r += crafting.toString()+"\n";
        r += health.toString()+"\n";
        r += ";";
        for (Trait i : traits) r += i.name()+";";
        return r;
    }
    public String[] load(String p){
        String[] line = p.split("%");
        String[] r = super.load(line[0]);
        clip.organs[5].setImage("Player");
        String[] other = line[1].split(";");
        cmap = other[1];
        cworld = cmap.split("_")[0];
        gold = Integer.parseInt(other[2]);
        memorized = other[3];
        learning = Actions.getByName(other[4]);
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
        return r;
    }
}