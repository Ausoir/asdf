package com.ga.unit;

import com.ga.game.Command;
import com.ga.game.Display;
import com.ga.game.FileManager;
import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.tool.Performer;
import com.ga.tool.Timer;
import com.ga.unit.item.Inventory;
import com.ga.unit.item.Item;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Moveset;
import com.ga.unit.skill.Spell;
import com.ga.unit.skill.Stat;

import static com.ga.unit.BodyHumanoid.FIST;

/**
 * Created by Ga on 7/30/2016.
 */
public class Player extends Man {
    public int gold;
    public Inventory inventory = new Inventory(this);
    public SkillTree skillTree = new SkillTree();
    public Timer save = new Timer(50, true);
    public Player(String p) {
        super(p);
        faction = Faction.Player;
        Command.host = this;
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
    private void grabItem(){
        Item t = Updater.collide(pos.getAdd(clip.organs[FIST].ptd));
        if (t != null){
            setWeapon(t);
        }
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
        if (inventory.addItem(weapon)){
            say("Sheathed "+weapon.name);
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
        if (!delay.over() || !jump) return;
        if (inventory.pockets.length > p && inventory.pockets[p] != -1) {
            Item item = inventory.getItem(inventory.pockets[p]);
            inventory.remove(inventory.pockets[p]);
            setWeapon(item);
        }
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
    }
    public int setState(Moveset state){
        face(Global.player.mP.x > pos.x-Global.camera.x ? 1 : -1);
        target = Global.player.mP.getSubtract(pos.getSubtract(Global.camera));
        if (state instanceof Actions) {
            switch ((Actions) state) {
                case Pickup:
                    if (weapon.name.equals("Fist")) grabItem();
                    break;
                default:
                    weapon.rotate(target.getMult(dir,dir));
            }
        }
        if (weapon.getType() == Item.Tag.Claw) weapon.rotation = (weapon.getOrgan().getAtan()+(dir<0?3.14f:0));
        int r = super.setState(state);
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
            say("Weapon broke!");
        }
        return r;
    }

    public void onCollide(Line r){
        super.onCollide(r);
        if (state != Actions.Dead) {
            if (r.opt == 2) {
                vel.set(0, 0);
                Global.map.teleport(pos, r);
            } else if (r.opt >= 100) {
                if (r.opt >= 200) Global.map.loadMap(r.opt - 200, Global.player.teleportReady());
                else Global.map.loadMap(r.opt - 100, true);
            }
        }
    }
    public void update(){
        Global.player.update();
        super.update();
        Global.camera = pos.getAdd(0,-40); //to be able to walk to edge
        Global.mapEdgeCenter(Global.camera);
        if (save.update() && state != Actions.Dead) FileManager.saveCharacter();
    }
    public String toCharacterString(){
        String r = super.toString()+"\n";
        r += gold+";"+Global.getTime()[0]+"\n";
        r += Global.player.toString()+"\n";
        r += stats.toString()+"\n";
        r += inventory.toString()+"\n";
        r += skillTree.toString();
        return r;
    }
    public void load(String p){
        String[] line = p.split("%");
        gold = Integer.parseInt(line[1].split(";")[1]);
        stats.load(line[3]);
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
        inventory.load(line[4]);
        skillTree.load(line[5]);
        Global.player.load(line[2]);
    }
}
