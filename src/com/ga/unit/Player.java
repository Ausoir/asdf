package com.ga.unit;

import com.ga.game.Command;
import com.ga.game.Display;
import com.ga.game.FileManager;
import com.ga.map.Global;
import com.ga.map.ManFactory;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.tool.Timer;
import com.ga.unit.item.*;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Moveset;
import com.ga.unit.skill.Spell;
import com.ga.unit.skill.Stat;

import java.util.ArrayList;

/**
 * Created by Ga on 7/30/2016.
 */
public class Player extends Man {
    class Companions{
        ArrayList<String> companions = new ArrayList<>();
        ArrayList<Timer> cooldowns = new ArrayList<>();
        public Companions(){
            add("Player",100);
            add("Man",200);
            add("Knight",300);
        }
        public void add(String p, int cd){
            companions.add(p);
            cooldowns.add(new Timer(cd, false));
        }
        public void summon(){
            for (int i=0;i<companions.size();i++){
                if (cooldowns.get(i).over()){
                    cooldowns.get(i).reset();
                    Man p = ManFactory.produce(companions.get(i), pos.getAdd(0,-50), -1);
                    p.dir = -dir;
                    p.faction = Faction.Player;
                    break;
                }
            }
        }
        public void update(){
            for (Timer i : cooldowns) i.update();
        }
    }
    public int gold;
    public Inventory inventory = new Inventory(this);
    private Companions companions = new Companions();
    public SkillTree skillTree = new SkillTree();
    public Timer save = new Timer(50, true);
    public Player(P pt, int seed) {
        super("Player",pt,seed);
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
        setWeapon((Weapon) ItemFactory.produce("Fist",0));
        setArmor((Armor) ItemFactory.produce("Underwear",0));
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
        Item t = Updater.collide(pos.getAdd(clip.organs[1].ptd));
        if (t != null){
            if (addItem(t)) {
                say("Picking up " + t.getFullName());
                t.remove();
            } else say("Can't pick up "+t.getFullName()+"... Inventory is full!");
        }
    }
    public void dropItem(int p) {
        Item t = inventory.remove(p);
        say("Dropping "+t.getFullName());
        t.drop(this);
        Updater.addQueue.add(t);
        get("Speed").add(t.weight);
    }
    public boolean addItem(Item p){
        if (inventory.addItem(p)) {
            p.pickup(this);
            get("Speed").add(-p.weight);
            return true;
        }
        return false;
    }
    public void describeItem(int p) {
        Item item = inventory.getItem(p);
        String r = item.getDescription();
        say(r);
    }
    public void useItem(int p){
        if (!delay.over() || !jump) return;
        if (inventory.pockets.length > p && inventory.pockets[p] != -1) {
            final Item item = inventory.getItem(inventory.pockets[p]);
            delay.set(item.delay);
            final Man host = this;
            delay.performer = new Performer(){
                public boolean perform(){
                    if (!item.use(host)){
                        say(item.getFullName()+" is not working!");
                    } else if (item.uses.get() == 0) {
                        say("Used up "+item.name);
                        inventory.remove(item);
                    }
                    delay.performer = null;
                    return true;
                }
            };
            target = Global.player.mP;
            clip.setState(item.action);
        }
    }
    public void summon(){
        companions.summon();
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
                    grabItem();
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
        companions.update();
        Global.player.update();
        super.update();
        Global.camera = pos.getAdd(0,-40); //to be able to walk to edge
        Global.mapEdgeCenter(Global.camera);
        if (save.update() && state != Actions.Dead) FileManager.saveCharacter();
    }
    public String toCharacterString(){
        String r = super.toString()+"@"+gold+"@"+Global.getTime()[0]+"\n";
        r += Global.player.toString()+"\n";
        r += stats.toString()+"\n";
        r += inventory.toString()+"\n";
        r += skillTree.toString();
        return r;
    }
    public void load(String p){
        String[] line = p.split("%");
        gold = Integer.parseInt(line[0].split("@")[3]);
        stats.load(line[2]);
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
        get("Health").onChange = (new Performer(){
            @Override
            public boolean perform(){
                hp.show(15);
                return true;
            }
        });
        inventory.load(line[3]);
        skillTree.load(line[4]);
        Global.player.load(line[1]);
    }
}
