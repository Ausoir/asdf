package com.ga.unit;

import com.ga.map.Global;
import com.ga.tool.Timer;
import com.ga.unit.skill.Skill;
import com.ga.unit.skill.Stat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Ga on 8/1/2016.
 */
public class Stats {
    protected ArrayList<Skill> effects = new ArrayList<>();
    public HashMap<String, Stat> stats = new HashMap<String, Stat>(){{
        put("health", new Stat((int) (Global.rng(100)+Global.r.nextInt(20)+5)));
        put("speed", new Stat((int) (Global.rng(5)+Global.r.nextInt(3)+5)));
        put("arms", new Stat(3));
        put("legs", new Stat(3));
        put("damage", new Stat(1));
    }};
    private Timer timer = new Timer(100, true);
    public Stats(){
    }
    public void addEffect(Skill p){
        effects.add(p);
    }
    public boolean hasEffect(String p){
        for (Skill i : effects){
            if (i.getClass().getSimpleName().equals(p)){
                if (i.active) return true;
            }
        }
        return false;
    }
    public void endEffect(String p){
        for (int i = 0; i < effects.size(); i++) {
            if (effects.get(i).getClass().getSimpleName().equals(p)){
                if (effects.get(i).active) effects.remove(i);
            }
        }
    }
    public void delevel(String stat){
        if (stats.get("adapt").get() > 0 && stats.get(stat).get() > 0) {
            stats.get("points").addTotal(1);
            stats.get(stat).addTotal(-1);
            stats.get("adapt").addTotal(-1);
        }
    }
    public void level(String stat){
        if (stats.get("points").get() > 0) {
            stats.get("points").addTotal(-1);
            stats.get(stat).addTotal(1);
        }
    }
    public void update(){
        Iterator it = effects.iterator();
        while (it.hasNext()){
            Skill i = (Skill) it.next();
            if (i.update()) it.remove();
        }
        if (timer.update()){
            Stat arm = stats.get("arms");
            Stat leg = stats.get("legs");
            arm.add(Math.round(arm.getByPercentage(.34f)));
            leg.add(Math.round(leg.getByPercentage(.34f)));
        }
    }
    public void load(String p){
        String[] data = p.split(";");
        for (int i=0;i<data.length;i++){
            String[] pair = data[i].split(":");
            stats.put(pair[0], new Stat(Integer.parseInt(pair[1].split(",")[0]),Integer.parseInt(pair[1].split(",")[1])));
        }
    }
    public String toString(){
        String result = "";
        Iterator it = stats.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            result += pair.getKey() + ":" + pair.getValue()+";";
        }
        result = result.substring(0,result.length()-1);
        return result;
    }
}