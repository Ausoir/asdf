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
import com.ga.unit.item.Inventory;
import com.ga.unit.item.Item;
import com.ga.unit.item.ItemFactory;
import com.ga.unit.item.Weapon;
import com.ga.unit.skill.*;

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
    public int gold = 0;
    public Inventory inventory = new Inventory(this);
    private Companions companions = new Companions();
    private Skill[] skills = new Skill[3];
    private Timer save = new Timer(150, true);
    public Player(P pt, int seed) {
        super("Player",pt,seed);
        Command.host = this;
        get("health").setTotal(30);
        stats.stats.put("points",new Stat(0));
        stats.stats.put("adapt",new Stat(0));
        get("points").max = 25;
        get("points").add(10);
        get("adapt").max = 25;
        clip.organs[1].setStyle(new Color(0,140,20),9);
        clip.organs[2].setStyle(new Color(20,20,180),13);
        clip.organs[3].setStyle(new Color(50,50,200),13);
        clip.organs[4].setStyle(new Color(0,220,65),15);
        clip.organs[5].setStyle(Color.green,9);
        skills[0] = new Adrenaline();
        skills[1] = new Rejuvenate();
        skills[2] = new Cleave();
        speechType = "p";
        setWeapon((Weapon) ItemFactory.produce("Fist",0));
    }
    @Override
    public void say(String p){
        Display.menu.setVisible(false);
        super.say(p);
    }
    public void addItem(Item p){
        inventory.addItem(p);
        get("speed").add(-p.weight);
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
                        say(item.name+" is not working!");
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
    public void setState(Actions state){
        face(Global.player.mP.x > 0 ? 1 : -1);
        target = Global.player.mP;
        switch (state){
            case SUMMON:
                summon();
                break;
            case SKILL:
                skill = skills[0];
                state = Actions.SKILL;
                break;
            case SKILL1:
                skill = skills[1];
                state = Actions.SKILL;
                break;
            case SKILL2:
                skill = skills[2];
                state = Actions.SKILL;
                break;
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
            if (get("arms").get() < weapon.weight && delay.over()) weapon.rotate(6f*weapon.dir);
        }
        super.update();
        Global.camera = new P(pos); //to be able to walk to edge
        Global.mapEdge();
        if (save.update() && pos.y < Global.map.map.size.y) FileManager.saveCharacter();
    }
    public String toCharacterString(){
        String r = super.toString()+"@"+Global.map.maps[1]+"\n";
        r += stats.toString()+"\n";
        r += inventory.toString();
        return r;
    }
    public void load(String p){
        String[] line = p.split("%");
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
        if (line.length > 2) inventory.load(line[2]);
        for (int i=0;i<inventory.getItemSize();i++){
            get("speed").add(-inventory.getItem(i).weight);
        }
    }
}
