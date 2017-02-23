package com.ga.game;

import com.ga.map.EditorInput;
import com.ga.map.Global;
import com.ga.unit.Matter;
import com.ga.unit.Player;
import com.ga.unit.Updater;
import com.ga.unit.factory.Stock;
import com.ga.unit.item.Item;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Buffs;
import com.ga.unit.skill.Skills;
import com.ga.unit.skill.Spells;

/**
 * Created by Ga on 8/7/2016.
 */
public class Command {
    public static Player host;
    public static void order(String p){
        try {
            String[] cmds = p.split(";");
            for (String i : cmds) {
                String[] cmd = i.split("@");
                if (cmd[0].equals("say")) host.say(cmd[1]);
                else if (cmd[0].equals("menuDialog")) Display.menuDialog.show(cmd[1],cmd[2]);
                else if (cmd[0].equals("heal")) host.get("Health").add(Integer.parseInt(cmd[1]));
                else if (cmd[0].equals("seed")) host.weapon.seed = Integer.parseInt(cmd[1]);
                else if (cmd[0].equals("time")) Global.setTime(Global.getTime()[0]+Integer.parseInt(cmd[1]));
                else if (cmd[0].equals("slow")) host.stats.addEffect(Buffs.Slow, 3);
                else if (cmd[0].equals("status")) host.say("Stamina:"+host.get("Stamina")+" Penalty:"+host.getStatPenalty()+" Health:"+host.get("Health"));
                else if (cmd[0].equals("delevel")) host.stats.delevel(cmd[1]);
                else if (cmd[0].equals("level")) host.stats.level(cmd[1]);
                else if (cmd[0].equals("stat")) host.get(cmd[1].split(":")[0]).addTotal(Integer.parseInt(cmd[1].split(":")[1]));
                else if (cmd[0].equals("advSkill")) for (int num=0;num<Integer.parseInt(cmd[1].split(":")[1]);num++) host.skillTree.advanceSkill(Skills.valueOf(cmd[1].split(":")[0]),false);
                else if (cmd[0].equals("advSkillCd")) for (int num=0;num<Integer.parseInt(cmd[1].split(":")[1]);num++) host.skillTree.advanceSkill(Skills.valueOf(cmd[1].split(":")[0]),true);
                else if (cmd[0].equals("advSpell")) for (int num=0;num<Integer.parseInt(cmd[1].split(":")[1]);num++) host.skillTree.advanceSkill(Spells.valueOf(cmd[1].split(":")[0]),false);
                else if (cmd[0].equals("advSpellCd")) for (int num=0;num<Integer.parseInt(cmd[1].split(":")[1]);num++) host.skillTree.advanceSkill(Spells.valueOf(cmd[1].split(":")[0]),true);
                else if (cmd[0].equals("man")) Stock.getByName(cmd[1], -1).pos = host.pos.getAdd(host.target);
                else if (cmd[0].equals("mob")) for (int num=0;num<Integer.parseInt(cmd[1].split(":")[1]);num++) Stock.getByName(cmd[1], -1).pos = host.pos.getAdd(host.target);
                else if (cmd[0].equals("item")) host.setWeapon((Item) Stock.getByName(cmd[1], -1));
                else if (cmd[0].equals("itemN")) host.setWeapon((Item) Stock.get(new String[]{cmd[1],cmd[2]}));
                else if (cmd[0].equals("tp")) host.pos.add(Integer.parseInt(cmd[1].split(",")[0]),Integer.parseInt(cmd[1].split(",")[1]));
                else if (cmd[0].equals("to")) host.pos.add(host.target);
//                else if (cmd[0].equals("inside")) host.say(Matter.isInside(host.pos.getAdd(host.target).x, host.pos.getAdd(host.target).y)+"");
                else if (cmd[0].equals("tpto")) host.pos.set(Integer.parseInt(cmd[1].split(",")[0]),Integer.parseInt(cmd[1].split(",")[1]));
                else if (cmd[0].equals("actSkill")) host.setState(Skills.valueOf(cmd[1]));
                else if (cmd[0].equals("actAction")) host.setState(Actions.valueOf(cmd[1]));
                else if (cmd[0].equals("learnSkill")) host.learnSkill(Skills.valueOf(cmd[1]));
                else if (cmd[0].equals("learnSpell")) host.learnSkill(Spells.valueOf(cmd[1]));
                else if (cmd[0].equals("atkspd")) host.weapon.speed = Integer.parseInt(cmd[1]);
                else if (cmd[0].equals("sound")) Sound.play(cmd[1]);
                else if (p.equals("revive")) host.revive();
                else if (cmd[0].equals("randomObj")) new Matter(cmd[1],0,8).pos = Global.map.randomSurface();
                else if (p.startsWith("cast")){
                    host.setState(Spells.valueOf(p.substring(5)));
                } else if (p.equals("editor")){
                    Updater.editor = true;
                    Display.input = new EditorInput();
                } else if (p.equals("online")) {
                    Global.server.online();
                } else if (p.startsWith("cellcall")) {
                    Global.server.online();
                    Global.server.requestConnection(p.substring(9));
                }
            }
        } catch (Exception e){
            Global.p("Command order failed for \""+p+"\"");
            e.printStackTrace();
        }
    }
}
