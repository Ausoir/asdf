package com.ga.unit;

import com.ga.map.Global;
import com.ga.unit.skill.Skills;

import java.util.ArrayList;

/**
 * Created by Ga on 8/30/2016.
 */
public class SkillTree {
    public ArrayList<Moveset> skills = new ArrayList<>();
    public SkillTree(){}
    public Moveset getSkill(int p){
        return skills.get(p);
    }
    public void load(String p){
        String[] data = p.substring(1).split(";");
        try {
            for (String i : data) {
                skills.add(Skills.valueOf(i));
            }
        } catch (Exception e){
            Global.p("Can't find moveset");
        }
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
