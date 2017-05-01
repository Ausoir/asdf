package com.ga.unit.player;

import com.ga.map.Global;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Stat;

import java.util.ArrayList;

/**
 * Created by Ga on 8/30/2016. qweqwe
 */
public class SkillTree {
    public ArrayList<Actions> skills = new ArrayList<>();
    public ArrayList<Stat> skillLevels = new ArrayList<>();
    SkillTree(){}
    public void load(String p){
        if (p.equals(";")) return;
        String[] data = p.substring(1).split(";");
        for (String i : data) {
            String[] skill = i.split("@");
            Actions move = Actions.getByName(skill[0]);
            if (move != null) {
                skills.add(move);
                skillLevels.add(new Stat(Integer.parseInt(skill[1]), 99));
            }
        }
    }
    void addSkill(Actions p){
        if (!skills.contains(p)) {
            skills.add(p);
            skillLevels.add(new Stat(1,99));
        }
    }
    public boolean advanceAble(Actions p){
        int level = Global.player.get("Level").get();
        int index = skills.indexOf(p);
        return !(index != -1 && level <= skillLevels.get(index).get());
    }
    public boolean advanceSkill(Actions p){
        if (!advanceAble(p)) return false;
        for (int i=0;i<skills.size();i++){
            if (skills.get(i) == p){
                skillLevels.get(i).add(1);
                return true;
            }
        }
        return false;
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
    public boolean hasSkill(Actions p){
        return skills.indexOf(p) != -1;
    }
    public int getSkillLevel(Actions p) {
        int i = skills.indexOf(p);
        if (i == -1) return 0;
        return skillLevels.get(i).get();
    }
}
