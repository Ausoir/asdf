package com.ga.unit;

import com.ga.unit.skill.Skills;

import java.util.ArrayList;

/**
 * Created by Ga on 8/30/2016.
 */
public class SkillTree {
    public ArrayList<Moveset> skills = new ArrayList<>();
    public SkillTree(){}
    public void load(String p){
        String[] data = p.substring(1).split(";");
        for (String i : data) {
            skills.add(getByName(i));
        }
    }
    public static Moveset getByName(String p){
        Moveset move = Skills.getByName(p);
        if (move == null) move = Actions.getByName(p);
        if (move == null) System.out.println("Can't find skill "+p);
        return move;
    }
    @Override
    public String toString(){
        String r = ";";
        for (Moveset i : skills){
            r += i.toString()+";";
        }
        return r;
    }

    public ArrayList<Moveset> getSkills() {
        return skills;
    }
}
