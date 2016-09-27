package com.ga.game;

import com.ga.map.Global;
import com.ga.map.ManFactory;
import com.ga.tool.Performer;
import com.ga.unit.*;
import com.ga.unit.item.ItemFactory;
import com.ga.unit.item.Projectile;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Skills;
import com.ga.unit.skill.Spells;

/**
 * Created by Ga on 8/7/2016.
 */
public class Command {
    public static Player host;
    public static void speechOrder(String p){
        if (p.startsWith("cmd")) {
            order(p.substring(4));
        } else if (p.startsWith("cast")){
            host.setState(Spells.valueOf(p.substring(5)));
        } else if (p.contains("rally")){
            host.summon();
        } else if (p.contains("fireball")){
            host.setState(Actions.Cast);
            host.delay.set(30);
            host.delay.performer = new Performer(){
                public boolean perform(){
                    new Projectile("item/Fireball",host.pos.getAdd(host.dir*60,-20),Global.player.mP.getAdd(host.pos),5).faction = Faction.Player;
                    host.delay.performer = null;
                    return true;
                }
            };
        } else if (p.equals("editor")){
            Updater.editor = true;
            Display.input = new EditorInput();
        } else if (p.equals("save")){
            FileManager.saveMap("Map");
        }
    }
    public static void order(String p){
        try {
            String[] cmds = p.split(";");
            for (String i : cmds) {
                String[] cmd = i.split("@");
                if (cmd[0].equals("say")) host.say(cmd[1]);
                else if (cmd[0].equals("delevel")) host.stats.delevel(cmd[1]);
                else if (cmd[0].equals("level")) host.stats.level(cmd[1]);
                else if (cmd[0].equals("stat")) host.get(cmd[1].split(":")[0]).addTotal(Integer.parseInt(cmd[1].split(":")[1]));
                else if (cmd[0].equals("man")) ManFactory.produce(cmd[1], host.pos.getAdd(host.target), -1);
                else if (cmd[0].equals("mob")) for (int num=0;num<Integer.parseInt(cmd[1].split(":")[1]);num++) ManFactory.produce(cmd[1].split(":")[0], host.pos.getAdd(host.target), -1);
                else if (cmd[0].equals("dog")) new Beast("Dog",cmd[1],host.pos.getAdd(host.target),-1);
                else if (cmd[0].equals("dummy")) ManFactory.produce(cmd[1], host.pos.getAdd(host.target), -1).brain = null;
                else if (cmd[0].equals("item")) host.addItem(ItemFactory.produce(cmd[1] ,-1));
                else if (cmd[0].equals("tp")) host.pos.add(Integer.parseInt(cmd[1].split(",")[0]),Integer.parseInt(cmd[1].split(",")[1]));
                else if (cmd[0].equals("tpto")) host.pos.set(Integer.parseInt(cmd[1].split(",")[0]),Integer.parseInt(cmd[1].split(",")[1]));
                else if (cmd[0].equals("act")) host.setState(Skills.valueOf(cmd[1]));
                else if (cmd[0].equals("learnSkill")) host.learnSkill(Skills.valueOf(cmd[1]));
                else if (cmd[0].equals("learnSpell")) host.learnSkill(Spells.valueOf(cmd[1]));
                else if (p.equals("revive")) host.revive();
            }
        } catch (Exception e){
            Global.p("Command order failed for \""+p+"\"");
            e.printStackTrace();
        }
    }
}
