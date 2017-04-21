package com.ga.unit.player;

import com.ga.map.Global;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Moveset;
import com.ga.unit.skill.Spells;
import com.ga.unit.skill.Stat;

import java.util.ArrayList;

/**
 * Created by Ga on 8/30/2016. qweqwe
 */
public class SkillTree {
    public ArrayList<Moveset> skills = new ArrayList<>();
    public ArrayList<Stat> skillLevels = new ArrayList<>();
    SkillTree(){}
    public void load(String p){
        if (p.equals(";")) return;
        String[] data = p.substring(1).split(";");
        for (String i : data) {
            String[] skill = i.split("@");
            Moveset move = getByName(skill[0]);
            if (move != null) {
                skills.add(move);
                skillLevels.add(new Stat(Integer.parseInt(skill[1]), 99));
            }
        }
    }
    void addSkill(Moveset p){
        if (!skills.contains(p)) {
            skills.add(p);
            skillLevels.add(new Stat(1,99));
        }
    }
    public boolean advanceAble(Moveset p){
        int level = Global.player.get("Level").get();
        int index = skills.indexOf(p);
        if (p == Spells.Fireworks) return false;
        return !(index != -1 && level <= skillLevels.get(index).get());
    }
    public boolean advanceSkill(Moveset p){
        if (!advanceAble(p)) return false;
        for (int i=0;i<skills.size();i++){
            if (skills.get(i) == p){
                skillLevels.get(i).add(1);
                return true;
            }
        }
        return false;
    }
    public static Moveset getByName(String p){
        Moveset move = Actions.getByName(p);
        if (move == null) move = Spells.getByName(p);
        if (move == null) System.out.println("Can't find spell "+p);
        return move;
    }
    public static Moveset getByCodeName(String p){
        Moveset move = null;
        String t = p.substring(1);
        if (p.startsWith("A")) move = Actions.getByName(t);
        if (p.startsWith("M")) move = Spells.getByName(t);
        if (move == null) System.out.println("Can't find spell "+p+" by code productName "+p);
        return move;
    }
    @Override
    public String toString(){
        String r = ";";
        for (int i=0;i<skills.size();i++){
            r += skills.get(i).toString()+"@";
            r += skillLevels.get(i).get()+";";
        }
        return r;
    }
    public boolean hasSkill(Moveset p){
        return skills.indexOf(p) != -1;
    }
    public int getSkillLevel(Moveset p) {
        int i = skills.indexOf(p);
        if (i == -1) return 0;
        return skillLevels.get(i).get();
    }
}
