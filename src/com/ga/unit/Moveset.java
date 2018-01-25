package com.ga.unit;

import com.ga.tool.ToolString;
import com.ga.unit.skill.Actions;

import java.util.ArrayList;

public class Moveset extends ArrayList<Actions>{
    public void load(Unit host, String p){
        for (String i : p.split(",")) host.learnSkill(Actions.getByName(i));
    }
    public String toString(){
        return ToolString.tosArrayList(this);
    }
}
