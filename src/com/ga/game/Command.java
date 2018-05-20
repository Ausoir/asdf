package com.ga.game;

import com.ga.map.*;
import com.ga.tool.Line;
import com.ga.unit.Prop;
import com.ga.unit.Stock;
import com.ga.unit.Traits;
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
                else if (cmd[0].equals("menuDialog")) Menu.menu.showDialog(cmd[1],cmd[2]);
                else if (cmd[0].equals("die")) host.die();
                else if (cmd[0].equals("crft")) host.crafting.craft(cmd[1]);
                else if (cmd[0].equals("buff")) host.buff(Buffs.valueOf(cmd[1]), Integer.parseInt(cmd[2]));
                else if (cmd[0].equals("seed")) host.weapon.seed = Integer.parseInt(cmd[1]);
                else if (cmd[0].equals("time")) Global.timePass(Integer.parseInt(cmd[1]));
                else if (cmd[0].equals("stat")) host.get(cmd[1]).setMax(Integer.parseInt(cmd[2]));
                else if (cmd[0].equals("unit")){
                    int loop = 1;
                    if (cmd.length>2) loop = Integer.parseInt(cmd[2]);
                    for (int ii=0;ii<loop;ii++) {
                        Prop unit = Stock.getByName(host.map, cmd[1], -1);
                        unit.start();
                        unit.pos.set(host.pos.getAdd(host.target));
                    }
                }
                else if (cmd[0].equals("mob")) Mob.spawn(host.map, cmd[1],host.lastLand);
                else if (cmd[0].equals("item")){
                    Item item = (Item) Stock.getByName(host.map, cmd[1], -1);
                    item.start();
                    host.setWeapon(item);
                }
                else if (cmd[0].equals("build")) new Generator(host.map, 99).generateBuilding(cmd[1], host.pos.getAdd(host.target), 9999);
                else if (cmd[0].equals("tp")) host.pos.add(Integer.parseInt(cmd[1].split(",")[0]),Integer.parseInt(cmd[1].split(",")[1]));
                else if (cmd[0].equals("to")){
                    host.lastLand = new Line();
                    host.pos.add(host.target);
                }
                else if (cmd[0].equals("tpto")) host.pos.set(Integer.parseInt(cmd[1].split(",")[0]),Integer.parseInt(cmd[1].split(",")[1]));
                else if (cmd[0].equals("act")) host.stateSet(Actions.valueOf(cmd[1]));
                else if (cmd[0].equals("learn")) host.learnSkill(Actions.valueOf(cmd[1]));
                else if (cmd[0].equals("trait")) host.learnTrait(Traits.Trait.valueOf(cmd[1]));
                else if (cmd[0].equals("forget")) host.moveset.remove(Actions.valueOf(cmd[1]));
                else if (cmd[0].equals("lose")) host.traits.forget(host, Traits.Trait.valueOf(cmd[1]));
                else if (cmd[0].equals("recipe")) host.crafting.knowledge.add(cmd[1]);
                else if (cmd[0].equals("sound")) Sound.play(host.map, Global.camera, cmd[1]);
                else if (cmd[0].equals("depth")) host.say("Me: "+host.getDepth()+" Line: "+host.lastLand.depth+", Terrain: "+host.map.terrains.get(host.lastLand.tid).getDepth());
                else if (cmd[0].equals("revive")) host.revive();
                else if (cmd[0].equals("hit")) host.hit(host.getPosLine(), 0);
                else if (cmd[0].equals("render")){
                    for (Object terrain : host.map.terrains.entrySet()) ((Terrain)terrain).render();
                }
                else if (cmd[0].equals("tatter")){
                    int dmg = Integer.parseInt(cmd[1]);
                    if (dmg < 0) Global.player.armor.mend();
                    for (Item item : Global.player.armor.armor){
                        item.isUsable(true, dmg);
                    }
                    Global.player.armor.onHit();
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
