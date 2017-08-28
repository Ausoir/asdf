package com.ga.unit.player;

import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.Man;
import com.ga.unit.Prop;
import com.ga.unit.Tag;
import com.ga.unit.Tags;
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
    public Crafts crafting;
    public int selectingItem = -1;
    public Health health;
    public Actions learning;
    public Item offhand;
    private boolean leftHandMode = false;
    private ArrayList<Trait> traits = new ArrayList<>();
    public Player(Map map, int seed) {
        super(map, null, seed);
        name = "Player";
        speechType = "p";
        inventory = new Inventory(this);
        crafting = new Crafts(this);
        health = new Health(this);
        tags.add(Tag.Athletics);
        tags.add(Tag.Might);
        tags.add(Tag.Agility);
        tags.add(Tag.Channeling);
    }
    public void onField(){
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
            weapon.remove();
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
    public boolean tagCapable(Tags p){
        if (p.size() > 0) {
            if (!tags.has(p) && !armor.tags.has(p) && !weapon.tags.has(p)) return false;
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
        int penalty = Math.max(0, armor.armor[state.isLeg()?1:2].weight - get("Strength").get());
        Stat end = get("Endurance");
        if (end.get() > end.getMax()) penalty += (end.get()-end.getMax())/10;
        attackSpeed -= penalty;
        if (state.getCost() >= 0){
            end.add(state.getCost(), true);
            get("Stamina").add(-penalty, false);
            if (getVacancy() < state.getDifficulty()){
                if (state.getDifficulty() > 0) say("Too encumbering! This action requires "+state.getDifficulty()+" extra encumbrance.");
                else say("Too encumbering!");
                state = Actions.Hit;
            }
        }
        super.stateSet(state);
        if (state.getCooldown() > 0){
            int[] t = cooldown.get(state);
            if (t != null) cooldown.put(state, new int[]{t[0] + penalty});
        }
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
                break;
            case Interact:
                delay.performer = new Performer() {
                    public void perform() {
                        P reach = pos.getAdd(clip.organs[FIST].ptd);
                        ArrayList<Prop> obj = map.updater.collideInteractive(reach);
                        if (obj.size() != 0) {
                            for (Prop i : obj) {
                                if (i.onInteract != null) {
                                    i.onInteract.interact(me);
                                    break;
                                }
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
                            ArrayList<Prop> obj = map.updater.collideInteractive(reach);
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
                            item.start();
                            selectingItem = -1;
                        } else if (selectingItem >= 10 && selectingItem <= 16) {
                            Sound.play(pos, "unwear");
                            int piece = selectingItem - 10;
                            if (sheathe() && !armor.isBare(piece)) {
                                setWeapon(armor.armor[piece]);
                                armor.armor[piece].start();
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
            if (i == Tag.Agility) power += get(i.toString()).get() * 100;
            else power += get(i.toString()).get();
            num++;
        }
        if (num == 0) return 1;
        return (power / num) / 100;
    }
    @Override
    public int getStateLevel(Actions state){
        int difficulty = state.getDifficulty();
        if (difficulty == 0 && state.getTrainers().size() == 0) return 1;
        int power = getStatePower(state);
        return Math.max(0,power+1-difficulty);
    }
    public float getSpeed() {
        float speed = get("Speed").get()*clip.getLegStatus();
        float penalty = Math.max(0,inventory.weight+armor.getWeight()+weapon.weight+offhand.weight-get("Strength").get()*2);
        return Math.max(0,speed-penalty)*.01f;
    }
    public boolean hasSkill(Actions move){
        for (Actions i : moveset){
            if (i == move) return true;
        }
        return false;
    }
    public void learnSkill(Actions move){
        Actions[] newset = new Actions[moveset.length+1];
        System.arraycopy(moveset, 0, newset, 0, moveset.length);
        newset[newset.length-1] = move;
        moveset = newset;
    }
    public boolean hasTrait(Trait p){
        return traits.contains(p);
    }
    public ArrayList<Prop> getAvailableItems(){
        ArrayList<Prop> items = map.updater.collideProp(pos,100);
        Iterator it = items.iterator();
        while (it.hasNext()){
            if (!(it.next() instanceof Item)) it.remove();
        }
        items.addAll(inventory.getItems());
        return items;
    }
    public int getVacancy(){
        Stat dex = get("Dexterity");
        if (dex == null) return 99;
        return dex.get() - armor.getEncumbrance() - weapon.encumbrance;
    }
    public void die(){
        super.die();
        save = true;
    }
    public void update(){
        super.update();
        offhand.pos.set(pos.x + clip.organs[OFIST].ptd.x, pos.y + clip.organs[OFIST].ptd.y);
    }
    public String toCharacterString(){
        StringBuilder r = new StringBuilder();
        r.append(gold).append(";").append(memorized).append(";").append(learning).append("\n");
        r.append(health.toString()).append("\n;");
        for (Trait i : traits) r.append(i.name()).append(";");
        r.append("\n");
        r.append(inventory.toString()).append("\n");
        r.append(crafting.toString()).append("\n");
        return r.toString();
    }
    public void loadTraits(String p){
        String[] traits = p.split(";");
        for (String i : traits){
            if (!i.equals("")){
                Trait trait = Trait.getByName(i);
                this.traits.add(trait);
                trait.onSet(this);
            }
        }
    }
    public void loadPlayer(String[] line){
        offhand = fist;
        String[] other = line[0].split(";");
        gold = Integer.parseInt(other[0]);
        memorized = other[1];
        learning = Actions.getByName(other[2]);
        inventory.load(line[3]);
        crafting.load(line[4]);
        health.load(line[1]);
        loadTraits(line[2]);
        armor.onChange = new Performer(){
            public void perform(){
                inventory.setMaxSize(armor.getPockets());
            }
        };
        inventory.setMaxSize(armor.getPockets());
        health.record("");
        final Player me = this;
        clip.organs[OFIST].onAmputate = new Performer(){
            public void perform(){
                offhand.drop(me);
                leftHandMode = true;
                setWeapon(fist);
                leftHandMode = false;
            }
        };
    }
}