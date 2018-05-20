package com.ga.unit.man;

import com.ga.game.Sound;
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
import java.util.Collections;

import static com.ga.unit.man.BodyHumanoid.FIST;
import static com.ga.unit.man.BodyHumanoid.HEAD;

/**
 * Created by Ga on 5/7/2016. asd
 */
public class Man extends Unit {
    private static Item fist = ((Item) Stock.Fist.create(null, -1,0));
    public Item weapon = fist;
    private String face;
    public ArmorSet armor;
    public Inventory inventory = new Inventory(this);
    public Crafts crafting = new Crafts(this);

    public Man(Map map, String name, int seed) {
        super(map, name, seed);
        clip = new BodyHumanoid(this);
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
        crafting.load(lines[13]);
        String[] items = t[1].split(";",-1);
        setFace(items[0]);
        if (!items[1].equals("")) setWeapon((Item) Stock.get(map, items[1]));
        armor = new ArmorSet(this, items[2],items[3],items[4],items[5]);
        if (items.length > 6){
            for (int i=6;i<items.length;i++){
                Item item = (Item) Stock.get(map, items[i]);
                inventory.addItem(item);
            }
        }
        final Man me = this;
        clip.organs[FIST].onAmputate = new Performer(){
            public void perform(){
                weapon.drop(me);
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
        r.append(super.toString()).append("@").append(crafting.toString()).append("@;");
        r.append("#").append(face).append(";").append(weapon.forceToString()).append(";").append(armor.toString());
        if (inventory.getItemSize() != 0) r.append(";").append(inventory.toString());
        return r.toString();
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

    public boolean sheathe() {
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

    protected void stateOnSet(Actions state){
        state.onSet(this);
    }
    protected void stateUpdate(){
        state.update(this); //calls Man version
        stateDuration++;
    }
    protected void stateInterrupt(){
        state.onInterrupt(this);
    }
    protected boolean stateFinish(){
        return state.onFinish(this);
    }
    protected void stateTrigger(){
        state.trigger(this);
    }
    @Override
    protected void moveTriggered(int organ){
        weapon.pos.set(pos.x + clip.organs[FIST].ptd.x, pos.y + clip.organs[FIST].ptd.y);
        super.moveTriggered(organ);
    }

    @Override
    public void remove() {
        super.remove();
        weapon.remove();
        armor.remove();
    }

    public float getReach(){
        return weapon.getSize()+2;
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
    protected boolean damageBreakthrough(int organ, boolean blunt, int damage){
        return armor.getOrganArmor(organ).checkBreak(blunt, damage);
    }

    @Override
    protected int getStatePower(Actions state){
        int r = 0;
        Tags tags = state.getTags();
        Tags mytags = getTags();
        for (Tag i : mytags){
            if (tags.contains(i)) r++;
        }
        if (state.getTrainers().contains(Tag.Strength)) r += get("Strength").get();
        return Math.max(0,r) / Math.max(1,tags.size());
    }

    public int getVacancy(){
        Stat dex = get("Agility");
        return dex.get() - armor.getEncumbrance() - weapon.encumbrance;
    }

    public Stat get(String p){
        return stats.getStat(p);
    }

    public int getGold(){
        ArrayList<Item> items = new ArrayList<>();
        items.addAll(inventory.getItems());
        items.add(weapon);
        Collections.addAll(items, armor.armor);
        int money = 0;
        for (Item i : items){
            if (i.tags.contains(Tag.Currency)){
                money += i.getUses();
            }
        }
        return money;
    }
    public void obtainGold(int p){
        ArrayList<Item> items = new ArrayList<>();
        items.addAll(inventory.getItems());
        items.add(weapon);
        Collections.addAll(items, armor.armor);
        for (Item i : items){
            if (i.tags.contains(Tag.Currency)){
                int money = Math.min(i.getDurability()-i.getUses(), p);
                p -= money;
                i.isUsable(true,-money);
            }
        }
        if (p > 0){
            Item newbag = (Item) Stock.Goldbag.create(map, -1, 0).start();
            setWeapon(newbag);
            newbag.isUsable(true,-p);
        }
    }
    public boolean requireGold(int p){
        ArrayList<Item> items = new ArrayList<>();
        items.addAll(inventory.getItems());
        items.add(weapon);
        Collections.addAll(items, armor.armor);
        for (Item i : items){
            if (i.tags.contains(Tag.Currency)){
                int money = Math.min(i.getUses(), p);
                if (i.isUsable(true,money)) p -= money;
                if (p == 0) return true;
            }
        }
        return false;
    }
    public boolean requireMana(int p){
        if (stats.triggerEffect(Buffs.Attunement)) return true;
        int innateLevel = traits.getTraitLevel(Traits.Trait.Attunement);
        if (innateLevel > 0){
            int mana = Math.min(get("Intelligence").get()*innateLevel, p);
            p -= mana;
            get("Intelligence").add(-mana/innateLevel, false);
            if (p == 0) return true;
        }
        ArrayList<Item> items = new ArrayList<>();
        items.addAll(inventory.getItems());
        items.add(weapon);
        Collections.addAll(items, armor.armor);
        for (Item i : items){
            if (i.tags.contains(Tag.Mana)){
                int mana = Math.min(i.getUses(), p);
                if (i.isUsable(true,mana)) p -= mana;
                if (p == 0) return true;
            }
        }
        return false;
    }

    public boolean hit(Line hitBox, boolean blunt, int damage) {
        boolean r = super.hit(hitBox, blunt, damage);
        armor.onHit();
        return r;
    }

    public void die() {
        say("@x");
        if (rand.nextBoolean()) weapon.drop(this);
        weapon.remove();
        super.die();
    }

    public void onAttack(Unit p){
        if (weapon.isUsable(true,2)) weapon.trigger(p);
        brain.onAttack();
    }

    public Unit attack() {
        boolean cleave = false;
        if (stats.triggerEffect(Buffs.Cleave)) cleave = true;
        Line hitbox = weapon.getLine();
        Unit target = null;
        for (Unit i : map.updater.collideUnit(pos, (int) hitbox.getLength()+20, faction)){
            if (i.hit(hitbox,weapon.type.isBlunt(),getDamage())){
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

    public int getWarmth(){
        return armor.getWarmth();
    }

    public void setVisible(boolean p) {
        super.setVisible(p);
        armor.setVisible(p);
        weapon.image.visible = p;
    }

    @Override
    public void setDepth(float p){
        super.setDepth(p);
        if (weapon != null) weapon.setDepth(p-.000001f);
    }

    @Override
    public void draw(Graphics2D g) {
        for (int i=0;i<clip.organs.length;i++) {
            clip.organs[i].draw(g);
            armor.display[i].draw(g);
        }
    }

    public String getDescription(){
        String r = super.getDescription();
        if (!wieldingNone()) r += "  *** Wielding ***\n"+weapon.getFullName()+"\n";
        r += "  *** Wearing ***\n"+armor.getDescription()+"\n";
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
        if (!wieldingNone()) items.add(weapon);
        return items;
    }
    public ArrayList<Prop> getAvailableProps() {
        ArrayList<Prop> items = map.updater.collideProp(pos,120);
        items.removeIf(o -> (o instanceof Item));
        items.removeIf(o -> !o.image.visible);
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
}