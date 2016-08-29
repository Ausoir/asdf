package com.ga.game;

import com.ga.map.Global;
import com.ga.map.ManFactory;
import com.ga.tool.Performer;
import com.ga.unit.*;
import com.ga.unit.item.ItemFactory;
import com.ga.unit.item.Projectile;

/**
 * Created by Ga on 8/7/2016.
 */
public class Command {
    public static Player host;
    public static void speechOrder(String p){
        p = p.toLowerCase();
        if (p.startsWith("cmd")) {
            order(p.substring(4));
        } else if (p.contains("rally")){
            host.summon();
        } else if (p.contains("fireball")){
            host.setState(Actions.CAST);
            host.delay.set(30);
            host.delay.performer = new Performer(){
                public void perform(){
                    new Projectile("item/Fireball",host.pos.getAdd(host.dir*60,-20),Global.player.mP.getAdd(host.pos),5).faction = 1;
                    host.delay.performer = null;
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
            String[] cmds = p.split(",");
            for (String i : cmds) {
                String[] cmd = i.split("@");
                if (cmd[0].equals("say")) host.say(cmd[1]);
                else if (cmd[0].equals("delevel")) host.stats.delevel(cmd[1]);
                else if (cmd[0].equals("level")) host.stats.level(cmd[1]);
                else if (cmd[0].equals("stat")) host.get(cmd[1].split(":")[0]).addTotal(Integer.parseInt(cmd[1].split(":")[1]));
                else if (cmd[0].equals("obj")) ManFactory.produce(Global.wordify(cmd[1]), host.pos.getAdd(150, -50), -1);
                else if (cmd[0].equals("dummy")) ManFactory.produce(Global.wordify(cmd[1]), host.pos.getAdd(150, -50), -1).brain = null;
                else if (cmd[0].equals("item")) host.addItem(ItemFactory.produce(Global.wordify(cmd[1]) ,-1));
                else if (p.equals("revive")) {
                    host.state = Actions.DOWN;
                    host.delay.set(50);
                    host.get("health").heal();
                    host.height = host.clip.getHeight();
                    host.pos.y -= host.height;
                }
            }
        } catch (Exception e){
            Global.p("Command order failed for \""+p+"\"");
            e.printStackTrace();
        }
    }
}
