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
import com.ga.unit.skill.Stat;

import java.awt.*;
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
                    p.faction = 1;
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
    private Timer save = new Timer(150, true);
    public Player(P pt, int seed) {
        super("Player",pt,seed);
        Command.host = this;
        get("health").setTotal(30);
        stats.setStat("memory",new Stat(1));
        stats.setStat("points",new Stat(0));
        stats.setStat("adapt",new Stat(0));
        get("points").setTotal(25);
        get("adapt").setTotal(25);
        clip.organs[1].setStyle(new Color(0,140,20),9);
        clip.organs[2].setStyle(new Color(20,20,180),13);
        clip.organs[3].setStyle(new Color(50,50,200),13);
        clip.organs[4].setStyle(new Color(0,220,65),15);
        clip.organs[5].setStyle(Color.green,9);
        speechType = "p";
        setWeapon((Weapon) ItemFactory.produce("Fist",0));
        setArmor((Armor) ItemFactory.produce("Underwear",0));
    }
    @Override
    public void say(String p){
        Display.menu.setVisible(false);
        super.say(p);
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
        t.pos = new P(pos.getAdd(0,height));
        t.setDepth(8);
        t.rotation = (float)Global.r.nextInt(628)/100;
        Updater.addQueue.add(t);
        get("speed").add(t.weight);
    }
    public boolean addItem(Item p){
        if (inventory.addItem(p)) {
            get("speed").add(-p.weight);
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
                public void perform(){
                    if (!item.use(host)){
                        say(item.getFullName()+" is not working!");
                    } else if (item.uses.get() == 0) {
                        say("Used up "+item.name);
                        inventory.remove(item);
                    }
                    delay.performer = null;
                }
            };
            target = Global.player.mP;
            clip.setState(item.action);
        }
    }
    public void summon(){
        companions.summon();
    }
    public void setState(Moveset state){
        face(Global.player.mP.x > 0 ? 1 : -1);
        target = Global.player.mP;
        if (state instanceof Actions) {
            switch ((Actions) state) {
                case SUMMON:
                    summon();
                    break;
                case PICKUP:
                    grabItem();
                    break;
            }
        }
        super.setState(state);
    }

    public void onCollide(Line r){
        super.onCollide(r);
        if (state != Actions.DEAD) {
            if (r.opt == 2) {
                vel.set(0, 0);
                Global.map.teleport(pos, r);
                pos.add(0, -height);
            } else if (r.opt >= 100) {
                if (r.opt >= 200) Global.map.loadMap(r.opt - 200, Global.player.teleportReady());
                else Global.map.loadMap(r.opt - 100, true);
            }
        }
    }
    public void update(){
        companions.update();
        Global.player.update();
        if (weapon != null && state != Actions.DEAD){
            weapon.rotate(Global.player.mP.getMult(dir,dir));
            if (get("arms").get() < weapon.weight && delay.over()) weapon.rotation = 6f*weapon.dir;
        }
        super.update();
        Global.camera = new P(pos); //to be able to walk to edge
        Global.mapEdge();
        if (save.update() && pos.y < Global.map.map.size.y) FileManager.saveCharacter();
    }
    public String toCharacterString(){
        String r = super.toString()+"@"+Global.map.maps[1]+"@"+gold+"\n";
        r += stats.toString()+"\n";
        r += inventory.toString()+"\n";
        r += skillTree.toString();
        return r;
    }
    public void load(String p){
        String[] line = p.split("%");
        gold = Integer.parseInt(line[0].split("@")[4]);
        stats.load(line[1]);
        hp = new PlayerHealthBar(get("health"),get("arms"),get("legs"),pos);
        get("arms").onChange = (new Performer(){
            @Override
            public void perform(){
                if (get("arms").isFull() && state == Actions.IDLE) clip.setState(Actions.IDLE);
                hp.show(15);
            }
        });
        get("legs").onChange = (new Performer(){
            @Override
            public void perform(){
                if (get("legs").isFull() && state == Actions.IDLE) clip.setState(Actions.IDLE);
                hp.show(15);
            }
        });
        inventory.load(line[2]);
        skillTree.load(line[3]);
    }
}
