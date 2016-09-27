package com.ga.unit;

import com.ga.map.Global;
import com.ga.unit.skill.*;

import java.util.ArrayList;

/**
 * Created by Ga on 8/30/2016.
 */
public class SkillTree {
    private ArrayList<Moveset> skills = new ArrayList<>();
    private ArrayList<Stat> skillCdLevels = new ArrayList<>();
    private ArrayList<Stat> skillLevels = new ArrayList<>();
    public SkillTree(){}
    public void load(String p){
        if (p.equals(";")) return;
        String[] data = p.substring(1).split(";");
        for (String i : data) {
            String[] skill = i.split("@");
            skills.add(getByName(skill[0]));
            skillLevels.add(new Stat(Integer.parseInt(skill[1]),99));
            skillCdLevels.add(new Stat(Integer.parseInt(skill[2]),99));
        }
    }
    public ArrayList<Moveset> getSkills(){
        return skills;
    }
    void addSkill(Moveset p){
        if (!skills.contains(p)) {
            skills.add(p);
            skillLevels.add(new Stat(1,99));
            skillCdLevels.add(new Stat(1,99));
        }
    }
    public boolean advanceAble(Moveset p, boolean cd){
        int level = Global.player.host.get("Level").get();
        int index = skills.indexOf(p);
        if (cd){
            if (p instanceof Skills) {
                switch ((Skills) p) {
                    case Riposte:
                    case Lunge:
                    case Throw:
                        return false;
                }
            } else {
                if (p == Spells.Fireworks) return false;
            }
            if (index != -1 && level <= skillCdLevels.get(index).get()) return false;
        } else {
            if (p == Spells.Fireworks) return false;
            if (index != -1 && level <= skillLevels.get(index).get()) return false;
        }
        return true;
    }
    boolean advanceSkill(Moveset p, boolean cd){
        if (!advanceAble(p, cd)) return false;
        for (int i=0;i<skills.size();i++){
            if (skills.get(i) == p){
                if (!cd) skillLevels.get(i).add(1);
                else skillCdLevels.get(i).add(1);
                return true;
            }
        }
        return false;
    }
    public static Moveset getByName(String p){
        Moveset move = Spells.getByName(p);
        if (move == null) move = Actions.getByName(p);
        if (move == null) move = Skills.getByName(p);
        if (move == null) System.out.println("Can't find spell "+p);
        return move;
    }
    @Override
    public String toString(){
        String r = ";";
        for (int i=0;i<skills.size();i++){
            r += skills.get(i).toString()+"@";
            r += skillLevels.get(i).get()+"@";
            r += skillCdLevels.get(i).get()+";";
        }
        return r;
    }
    public boolean hasSkill(Moveset p){
        return skills.indexOf(p) != -1;
    }
    public int getSkillCdLevel(Moveset p) {
        int i = skills.indexOf(p);
        if (i == -1) return 0;
        return skillCdLevels.get(i).get();
    }
    public int getSkillLevel(Moveset p) {
        int i = skills.indexOf(p);
        if (i == -1) return 0;
        return skillLevels.get(i).get();
    }
}
