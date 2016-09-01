package com.ga.unit;

import com.ga.map.Global;
import com.ga.tool.Timer;
import com.ga.unit.skill.Buff;
import com.ga.unit.skill.Stat;

import java.util.*;

/**
 * Created by Ga on 8/1/2016.
 */
public class Stats {
    private ArrayList<Buff> effects = new ArrayList<>();
    private ArrayList<Timer> effectTimes = new ArrayList<>();
    private HashMap<String, Stat> stats = new HashMap<String, Stat>(){{
        put("health", new Stat((int) (Global.rng(100)+Global.r.nextInt(20)+5)));
        put("speed", new Stat((int) (Global.rng(50)+Global.r.nextInt(30)+50)));
        put("arms", new Stat(3));
        put("legs", new Stat(3));
        put("damage", new Stat(1));
    }};
    private Timer timer = new Timer(100, true);
    public Stats(){
    }
    public Stat getStat(String p) {
        return stats.get(p);
    }
    public void setStat(String name, Stat p) {
        stats.put(name,p);
    }
    public void addEffect(Buff p){
        if (p.stat != null) {
            if (p.buffRel != null) stats.get(p.stat).add((int) p.buff * stats.get(p.buffRel).getMax(), true);
            else stats.get(p.stat).add((int) p.buff, true);
        }
        effectTimes.add(new Timer(p.time));
        effects.add(p);
    }
    public boolean hasEffect(Buff p){
        return effects.contains(p);
    }
    public void end(){
        for (int i = 0; i < effects.size(); i++) {
            endEffect(effects.get(i));
        }
    }
    public boolean endEffect(Buff p){
        if (hasEffect(p)){
            if (p.stat != null) {
                if (p.buffRel != null) stats.get(p.stat).add(-(int) p.buff * stats.get(p.buffRel).getMax());
                else stats.get(p.stat).add(-(int) p.buff);
            }
            int i = effects.indexOf(p);
            effects.remove(i);
            effectTimes.remove(i);
        }
        return false;
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
    public void unrest(){
        timer.reset();
    }
    public void rest(){
        if (timer.update()){
            Stat arm = stats.get("arms");
            Stat leg = stats.get("legs");
            arm.add(Math.round(arm.getByPercentage(.34f)));
            leg.add(Math.round(leg.getByPercentage(.34f)));
        }
    }
    public void update(){
        for (int i = 0; i < effects.size(); i++) {
            if (effectTimes.get(i).update()){
                endEffect(effects.get(i));
            }
        }
    }
    public void load(String p){
        String[] data = p.split(";");
        for (int i=0;i<data.length;i++){
            String[] pair = data[i].split(":");
            Stat newstat = new Stat(Integer.parseInt(pair[1].split(",")[0]),Integer.parseInt(pair[1].split(",")[1]));
            newstat.onChange = stats.get(pair[0]).onChange;
            stats.put(pair[0], newstat);
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