package com.ga.unit;

import com.ga.game.Command;
import com.ga.game.Display;
import com.ga.map.Global;
import com.ga.map.ManFactory;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.tool.Timer;
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
                    Man p = ManFactory.produce(companions.get(i), pos.getAdd(0,-50));
                    p.dir = -dir;
                    p.brain.skillset.clear();
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
    public Player(P pt) {
        super("Player",pt);
        Command.host = this;
        get("health").set(30);
        stats.stats.put("points",new Stat(0));
        stats.stats.put("adapt",new Stat(0));
        get("points").max = 25;
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
        hp = new PlayerHealthBar(get("health"),get("arms"),get("legs"),pos);
        get("arms").onChange = (new Performer(){
            @Override
            public void perform(){
                if (get("arms").get() >= weapon.weight && state == Actions.IDLE) clip.setState(Actions.IDLE);
                hp.show(15);
            }
        });
        get("legs").onChange = (new Performer(){
            @Override
            public void perform(){
                if (get("legs").get() >= 2 && state == Actions.IDLE) clip.setState(Actions.IDLE);
                hp.show(15);
            }
        });
    }
    @Override
    public void say(String p){
        Display.menu.setVisible(false);
        super.say(p);
    }
    public void useItem(int p){
        if (!delay.over() || !jump) return;
        if (inventory.pockets.length > p && inventory.pockets[p] != null) {
            final Item item = inventory.pockets[p];
            delay.set(item.delay);
            final Man host = this;
            delay.performer = new Performer(){
                public void perform(){
                    if (!item.use(host)){
                        say(item.name+" is not working!");
                    } else if (item.uses == 0) {
                        say("Used up "+item.name);
                        inventory.remove(item);
                    }
                    delay.performer = null;
                }
            };
            clip.setState(item.action);
        }
    }
    public void summon(){
        companions.summon();
    }
    public void setState(Actions state){
        face(Global.player.mP.x > 0 ? 1 : -1);
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
            case ATTACK:
                clip.setPunch(Global.player.mR, dir);
                break;
            case KICK:
                clip.setKick(Global.player.mR, dir);
                break;
        }
        super.setState(state);
    }
    public void update(){
        companions.update();
        Global.player.update();
        if (weapon != null && state != Actions.DEAD){
            weapon.rotate(Global.player.mP.getMult(dir,dir));
            if (get("arms").get() < weapon.weight && delay.over()) weapon.rotate(6f*weapon.dir);
        }
        super.update();
        Global.mapEdge();
    }
    public String toString(){
        return "";
    }
}
