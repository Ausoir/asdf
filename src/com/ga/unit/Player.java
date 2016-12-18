package com.ga.unit;

import com.ga.game.Display;
import com.ga.map.Global;
import com.ga.tool.Line;
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
    public int gold;
    public Inventory inventory = new Inventory(this);
    public SkillTree skillTree = new SkillTree();
    private int selectingItem = -1;
    public PlayerInput input;
    public Player(String p) {
        super(p);
        faction = Faction.Player;
        get("Health").setTotal(30);
        stats.setStat("Level",new Stat(1));
        stats.setStat("Memory",new Stat(1));
        stats.setStat("Level Points",new Stat(0));
        stats.setStat("Reset Points",new Stat(0));
        get("Level Points").setTotal(25);
        get("Reset Points").setTotal(25);
        speechType = "p";
    }
    @Override
    public void say(String p){
        Display.menu.setVisible(false);
        super.say(p);
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
        } else if (p == armor){
            armor.drop(this);
            setArmor(null);
        } else return;
        say("Dropping " + p.getFullName());
    }
    public void selectItem(int p){
        selectingItem = p;
        setState(Actions.Sheathe);
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
                case Sheathe:
                    delay.performer = new Performer() {
                        public boolean perform() {
                            if (selectingItem == -1 || selectingItem == inventory.wielded) {
                                sheathe();
                            } else if (!wieldingNone()) {
                                sheathe();
                                return false;
                            } else {
                                if (inventory.pockets.length > selectingItem && inventory.pockets[selectingItem] != -1) {
                                    Item item = inventory.getItem(inventory.pockets[selectingItem]);
                                    inventory.wielded = selectingItem;
                                    inventory.remove(inventory.pockets[selectingItem]);
                                    setWeapon(item);
                                }
                                selectingItem = -1;
                            }
                            return true;
                        }
                    };
                    break;
            }
        }
    }
    public int setStateEcho(Moveset state){
        if (state != Actions.Idle && state != Actions.Wait){
            Display.shop.setVisible(false);
            Display.dialog.setVisible(false);
        }
        if (state instanceof Actions) {
            switch ((Actions) state) {
                case WalkL:
                case WalkR:
                    if (delay.over()) {
                        if (Global.cos(weapon.rotation) < .8f) weapon.rotation *= .9f;
                    }
                    break;
                case Interact:
                    ArrayList<Matter> obj = Updater.collide(pos.getAdd(clip.organs[FIST].ptd), 12);
                    for (Matter i : obj){
                        if (i.interactive){
                            i.interact(this);
                            break;
                        }
                    }
                    break;
                case Pull1:
                    selectItem(0);
                    break;
                case Pull2:
                    selectItem(1);
                    break;
                case Pull3:
                    selectItem(2);
                    break;
            }
        } else if (state instanceof Skills) {
            switch ((Skills) state) {
                case Snipe:
                case Shoot:
                    weapon.rotate(target.getMult(dir,dir));
                    weapon.rotation += .1*dir;
            }
            Moveset a = this.state;
            Moveset b = state;
            if (a == Skills.Use) a = weapon.action;
            if (b == Skills.Use) b = weapon.action;
            if (a != b){
                delay.set(0);
            }
        }
        if (weapon.getType() == Item.Tag.Claw) weapon.rotation = (weapon.getOrgan().getAtan()+(dir<0?3.14f:0));
        int r = super.setStateEcho(state);
        if (r == 1) {
        } else if (r == 2){
            String statreq = state.getStatReq();
            int req = (int) state.getCost();
            if (req < 0){
                if (statreq.equals("Stamina")) req = weapon.weight*-req;
                else if (statreq.equals("Energy")) req = armor.weight*-req;
            }
            say("Need "+req+" more "+ statreq+"!");
        } else if (r == 3){
            int[] cdinput = getCooldown(state);
            say("Spell is available in "+Global.checkCooldown(cdinput, state.getCooldown())+" seconds!");
        } else if (r == 4){
            say("Use broke!");
        }
        return r;
    }

    public void onCollide(Line r){
        super.onCollide(r);
        if (state != Actions.Dead) {
            if (r.opt == 2) {
                if (state == Actions.Wait){
                    vel.set(0, 0);
                    Global.map.teleport(pos, r);
                }
            } else if (r.opt >= 100) {
                boolean host = Global.player == input ? true : false;
                if (r.opt >= 200){
                    if (state == Actions.Wait) Global.map.loadMap(host, r.opt - 200);
                } else Global.map.loadMap(host, r.opt - 100);
            }
        }
    }
    public void update(){
        super.update();
        Global.mapEdgeCenter(Global.camera);
    }
    @Override
    public String toString(){
        return "";
    }
    public String toCharacterString(){
        String r = super.toString()+"\n";
        r += gold+";"+Global.getTime()[0]+"\n";
        r += input.toString()+"\n";
        r += inventory.toString()+"\n";
        r += skillTree.toString();
        return r;
    }
    public void load(String p){
        String[] line = p.split("%");
        gold = Integer.parseInt(line[1].split(";")[1]);
        hp = new PlayerHealthBar(get("Health"),get("Stamina"),get("Energy"),pos);
        get("Stamina").onChange = (new Performer(){
            @Override
            public boolean perform(){
                if (get("Stamina").isFull() && state == Actions.Idle) clip.setState(Actions.Idle);
                hp.show(15);
                return true;
            }
        });
        get("Energy").onChange = (new Performer(){
            @Override
            public boolean perform(){
                if (get("Energy").isFull() && state == Actions.Idle) clip.setState(Actions.Idle);
                hp.show(15);
                return true;
            }
        });
        inventory.load(line[3]);
        skillTree.load(line[4]);
    }
}
