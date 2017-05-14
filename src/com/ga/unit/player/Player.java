package com.ga.unit.player;

import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.Updater;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.*;
import com.ga.unit.intelligence.PlayerInput;
import com.ga.unit.item.Crafts;
import com.ga.unit.item.Item;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Stat;

import java.util.ArrayList;
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
    private Item offhand = fist;
    private boolean leftHandMode = false;
    ArrayList<Trait> traits = new ArrayList<>();
    public Player(int seed) {
        super(seed);
        loadActions("-5,100,1000/Use&Walk&Sprint");
        speechType = "p";
        inventory = new Inventory(this);
        crafting = new Crafts(this);
        health = new Health(this);
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
                    Sound.play("wield");
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
    public int setState(Actions state){
        int r = super.setState(state);
        if (r != 0){
            if (r == 2) {
                int req = state.getCost();
                req += getStatPenalty();
                if (req > get("Stamina").getMax()) say("This skill is beyond my abilities!");
            } else if (r == 4){
                say("Cannot cast!");
            } else if (r == 5) say("This skill is beyond my abilities!");
        }
        return r;
    }
    public void stateSet(Actions state){
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
                            if (i != weapon && i != offhand && i.onInteract != null) {
                                i.onInteract.interact(me);
                                break;
                            }
                        }
                        for (final Line i : Global.map.land) {
                            if (i.opt == 1) {
                                if (i.lineCross(new Line(reach.x - (12 * dir), reach.y, reach.x + (12 * dir), reach.y))) {
                                    clip.setState(Actions.Guard.getClipName());
                                    delay.set(40);
                                    final int firstDir = dir;
                                    delay.performer = new Performer() {
                                        public void perform() {
                                            pos.x = i.pt.x - (24 * dir);
                                            setState(Actions.Climb);
                                            dir = firstDir;
                                            input.moving = 0;
                                            rope = i;
                                        }
                                    };
                                }
                            }
                        }
                    }
                };
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
                            Sound.play("unwear");
                            int piece = selectingItem - 10;
                            if (sheathe() && !armor.isBare(piece)) {
                                setWeapon(armor.armor[piece]);
                                armor.set(null, piece);
                            }
                        }
                    }
                };
                break;
            case Duck:
                cooldown.put(state, new int[]{Global.time + ((armor.armor[1].encumberance + armor.armor[2].encumberance) * 4)});
                break;
            case Guard:
                cooldown.put(state, new int[]{Global.time + ((armor.armor[FIST].encumberance + armor.armor[0].encumberance) * 4)});
                break;
        }
    }
    @Override
    public int getStateLevel(Actions state){
        int difficulty = state.getDifficulty();
        if (difficulty == 0) return 1;
        int power = 0;
        for (Tag i : state.getTags().getTrainers()){
            power += get(i.toString()).get();
        }
//        return Math.max(0,skillTree.getSkillLevel(state)-(difficulty-power));
        return 1;
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
                        tagStat.add(amount/Math.max(1,tagStat.get()-difficulty), false);
                    }
                    if (getStateLevel(learning) > 0){
                        learnSkill(learning);
                        learning = null;
                    }
                    progress.add(mod*10, false);
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
        setHead("Player");
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