package com.ga.unit.man;

import com.ga.game.Sound;
import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.*;
import com.ga.unit.item.ArmorSet;
import com.ga.unit.item.Crafts;
import com.ga.unit.item.Food;
import com.ga.unit.item.Item;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Buffs;
import com.ga.unit.skill.Stat;

import java.awt.*;
import java.util.ArrayList;

import static com.ga.unit.man.BodyHumanoid.FIST;
import static com.ga.unit.man.BodyHumanoid.HEAD;

/**
 * Created by Ga on 5/7/2016. asd
 */
public class Man extends Unit {
    private String face;
    private Item fist;
    public Item weapon = fist;
    public ArmorSet armor;
    public int selectingItem = -1;
    public String memorized = "";
    public int gold;
    public Inventory inventory = new Inventory(this);
    public Crafts crafting = new Crafts(this);
    public Food.Material[] favorites;

    public Man(Map map, String name, int seed) {
        super(map, name, seed);
        clip = new BodyHumanoid(this);
        int favNum = rand.nextInt(4);
        favorites = new Food.Material[1+favNum];
        for (int i=0;i<favNum+1;i++){
            favorites[i] = Food.Material.values()[Food.Material.values().length-1];
        }
    }
    @Override
    protected void init(){
        tags.add(Tag.Strength);
        tags.add(Tag.Dexterity);
        tags.add(Tag.Athletics);
    }
    public void onField(){
    }
    boolean hasSkill(Actions move){
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
    public void setWeapon(Item p){
        if (p == null) p = fist;
        if (p != fist) {
            if (sheathe()) {
                Sound.play(map, pos, "wield");
            } else {
                dropItem(weapon);
            }
        }
        weapon.onUnequip(this);
        weapon = p;
        weapon.onEquip(this);
        weapon.pos = new P(pos);
        weapon.dir = dir;
    }

    public boolean wieldingNone(){ return weapon == null || weapon == fist; }

    private boolean sheathe() {
        if (wieldingNone()) return true;
        if (inventory.addItem(weapon)){
            weapon.remove();
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

    public int getWeight(){
        return inventory.weight+armor.getWeight()+weapon.weight;
    }

    @Override
    public void stateSet(Actions state){
        attackSpeed -= weapon.weight;
        super.stateSet(state);
        final Man me = this;
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
            case Wield:
                if (selectingItem == -1) {
                    sheathe();
                } else if (selectingItem < inventory.getItemSize()) {
                    Item item = inventory.getItem(selectingItem);
                    setWeapon(item);
                    inventory.remove(item);
                    item.start();
                    selectingItem = -1;
                } else if (selectingItem >= 10 && selectingItem <= 16) {
                    Sound.play(map, pos, "unwear");
                    int piece = selectingItem - 10;
                    if (sheathe() && !armor.isBare(piece)) {
                        setWeapon(armor.armor[piece]);
                        armor.armor[piece].start();
                        armor.set(null, piece);
                    }
                }
                break;
        }
    }

    @Override
    public void remove() {
        super.remove();
        weapon.remove();
        armor.remove();
    }

    public float getReach(){
        return weapon.getSize();
    }

    void stepSound(){
        String ground = map.getLand(lastLand.tid);
        if (Sound.has("step_" + ground)) Sound.play(map, pos, "step_" + ground);
        else Sound.play(map, pos, "step_dirt");
    }

    public void attackSound(){
        Sound.play(map, pos, "swing_normal");
    }

    @Override
    public boolean face(int dir, boolean force) {
        if (super.face(dir, force)) {
            if (weapon.dir != dir) {
                weapon.dir = dir;
                weapon.rotation *= -1;
            }
            armor.armor[0].dir = dir;
            clip.setClip();
            return true;
        }
        return false;
    }

    @Override
    public int getDamage(){
        return (int) Math.max(0,weapon.getDamage()*damageMod);
    }

    @Override
    protected boolean damageBreakthrough(int organ, int damage){
        return armor.getOrganArmor(organ).checkBreak(damage);
    }

    public int getVacancy(){
        Stat dex = get("Dexterity");
        return dex.get() - armor.getEncumbrance() - weapon.encumbrance;
    }

    public Stat get(String p){
        return stats.getStat(p);
    }

    public void die() {
        say("@x");
        if (rand.nextBoolean()) weapon.drop(this);
        weapon.remove();
        super.die();
    }

    public void onAttack(Unit p){
        if (weapon.usable(2)) weapon.trigger(p);
        brain.onAttack();
    }

    public Unit attack() {
        boolean cleave = false;
        if (stats.triggerEffect(Buffs.Cleave)) cleave = true;
        Line hitbox = weapon.getLine();
        Unit target = null;
        for (Unit i : map.updater.collideUnit(pos, (int) hitbox.getLength()+20, faction)){
            if (i.hit(hitbox,getDamage())){
                target = i;
                if (!cleave) break;
            }
        }
        if (target != null) {
            onAttack(target);
            return target;
        }
        return null;
    }

    @Override
    protected void moveTriggered(int organ){
        weapon.pos.set(pos.x + clip.organs[FIST].ptd.x, pos.y + clip.organs[FIST].ptd.y);
        super.moveTriggered(organ);
    }

    public void setVisible(boolean p) {
        super.setVisible(p);
        weapon.image.visible = p;
    }

    @Override
    public void setDepth(float p){
        super.setDepth(p);
        if (weapon != null) weapon.setDepth(p-.000001f);
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);
        armor.armor[0].pos.set(pos.getAdd(clip.organs[5].pt));
        armor.armor[0].rotation = clip.organs[5].getAtan()+1.57f;
        armor.armor[0].draw(g);
    }

    public String getDescription(){
        String r = super.getDescription()+"\n";
        if (!wieldingNone()) r += "*Wielding - "+weapon.getFullName()+"\n";
        r += "*Wearing - "+armor.getDescription()+"\n";
        r += "*Favorite Foods - ";
        for (Food.Material i : favorites) r += i.name()+", ";
        r = r.substring(0,r.length()-2)+"\n";
        return r;
    }

    @Override
    public void update(){
        super.update();
        weapon.pos.set(pos.x + clip.organs[FIST].ptd.x, pos.y + clip.organs[FIST].ptd.y);
    }

    private void setFace(String p){
        face = p;
        clip.organs[5].setImage(p);
    }

    public ArrayList<Prop> getAvailableItems(){
        ArrayList<Prop> items = map.updater.collideProp(pos,100);
        items.removeIf(o -> !(o instanceof Item));
        items.addAll(inventory.getItems());
        return items;
    }
    public ArrayList<Prop> getAvailableProps() {
        ArrayList<Prop> items = map.updater.collideProp(pos,120);
        items.removeIf(o -> (o instanceof Item));
//        items.removeIf(o -> !o.image.visible);
        return items;
    }

    @Override
    protected Tags getTags(){
        Tags r = new Tags();
        r.addAll(super.getTags());
        r.addAll(armor.tags);
        r.addAll(weapon.tags);
        return r;
    }

    @Override
    public Man start(){
        super.start();
        if (!wieldingNone()){
            weapon.map = map;
            weapon.start();
        }
        return this;
    }

    @Override
    public String[] load(String p){
        String[] t = p.split("#");
        String[] lines = super.load(t[0]);

        gold = Integer.parseInt(lines[13]);
        memorized = lines[14];
        crafting.load(lines[15]);

        String[] items = t[1].split(";");
        setFace(items[0]);
        weapon = fist = ((Item) Stock.Fist.create(map, -1,0));
        if (!items[1].equals("")) setWeapon((Item) Stock.get(map, items[1]));
        armor = new ArmorSet(this, items[2],items[3],items[4]);
        if (items.length > 5){
            for (int i=5;i<items.length;i++){
                Item item = (Item) Stock.get(map, items[i]);
                inventory.addItem(item);
            }
        }
        final Man me = this;
        clip.organs[FIST].onAmputate = new Performer(){
            public void perform(){
                weapon.drop(me);
                setWeapon(null);
            }
        };
        clip.organs[HEAD].onAmputate = new Performer(){
            public void perform(){
                armor.set(null, 0);
            }
        };
        return t;
    }

    @Override
    public String toString(){
        StringBuilder r = new StringBuilder();
        r.append(super.toString()).append("@").append(gold).append("@").append(memorized);
        r.append("@").append(crafting.toString()).append("@;");
        r.append("#").append(face).append(";").append(weapon.forceToString()).append(";").append(armor.toString()).append(";").append(inventory.toString());
        return r.toString();
    }
}