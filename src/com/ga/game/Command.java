package com.ga.game;

import com.ga.map.*;
import com.ga.tool.Line;
import com.ga.unit.PlayerInput;
import com.ga.unit.Prop;
import com.ga.unit.Stock;
import com.ga.unit.item.Item;
import com.ga.unit.man.Player;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Buffs;

/**
 * Created by Ga on 8/7/2016. asd
 */
public class Command {
    public static Player host;
    public static void order(String p){
        try {
            String[] cmds = p.split(";");
            for (String i : cmds) {
                String[] cmd = i.split("@");
                if (cmd[0].equals("say")) host.say(cmd[1]);
                else if (cmd[0].equals("menuDialog")) Global.display.menu.showDialog(cmd[1],cmd[2]);
                else if (cmd[0].equals("die")) host.die();
                else if (cmd[0].equals("crft")) host.crafting.craft(cmd[1]);
                else if (cmd[0].equals("buff")) host.buff(Buffs.valueOf(cmd[1]), Integer.parseInt(cmd[2]));
                else if (cmd[0].equals("seed")) host.weapon.seed = Integer.parseInt(cmd[1]);
                else if (cmd[0].equals("time")) Global.time += Integer.parseInt(cmd[1]);
                else if (cmd[0].equals("stat")) host.get(cmd[1]).addTotal(Integer.parseInt(cmd[2]));
                else if (cmd[0].equals("gold")) host.gold = Integer.parseInt(cmd[1]);
                else if (cmd[0].equals("unit")){
                    Prop unit = Stock.getByName(host.map, cmd[1], -1);
                    unit.start();
                    unit.pos.set(host.pos.getAdd(host.target));
                }
                else if (cmd[0].equals("mob")) Mob.spawn(host.map, cmd[1],host.lastLand);
                else if (cmd[0].equals("item")){
                    Item item = (Item) Stock.getByName(host.map, cmd[1], -1);
                    item.start();
                    host.setWeapon(item);
                }
                else if (cmd[0].equals("build")) new Generator(host.map, 99).generateBuilding(cmd[1], host.pos, 1);
                else if (cmd[0].equals("tp")) host.pos.add(Integer.parseInt(cmd[1].split(",")[0]),Integer.parseInt(cmd[1].split(",")[1]));
                else if (cmd[0].equals("to")){
                    host.lastLand = new Line();
                    host.pos.add(host.target);
                }
                else if (cmd[0].equals("tpto")) host.pos.set(Integer.parseInt(cmd[1].split(",")[0]),Integer.parseInt(cmd[1].split(",")[1]));
                else if (cmd[0].equals("act")) host.setState(Actions.valueOf(cmd[1]));
                else if (cmd[0].equals("level")) Global.player.witnessDeath(host);
                else if (cmd[0].equals("learn")) host.learnSkill(Actions.valueOf(cmd[1]));
                else if (cmd[0].equals("recipe")) host.crafting.knowledge.add(cmd[1]);
                else if (cmd[0].equals("sound")) Sound.play(host.map, Global.camera, cmd[1]);
                else if (cmd[0].equals("depth")) host.say("Me: "+host.getDepth()+" Line: "+host.lastLand.depth+", Terrain: "+host.map.terrains.get(host.lastLand.tid).getDepth());
                else if (p.equals("revive")) host.revive();
                else if (p.equals("render")){
                    for (Object terrain : host.map.terrains.entrySet()) ((Terrain)terrain).render();
                }
                else if (p.equals("test")){
                    Player unit = (Player) Stock.Player.create(host.map,0,0);
                    unit.pos.set(host.pos);
                    unit.save = false;
                    new PlayerInput(unit);
                    unit.start();
                }
                else if (p.equals("editor")){
                    Global.display.input = new EditorInput();
                }
            }
        } catch (Exception e){
            Global.p("Command order failed for \""+p+"\"");
            e.printStackTrace();
        }
    }
}
