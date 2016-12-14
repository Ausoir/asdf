package com.ga.unit;

import com.ga.map.Global;
import com.ga.tool.Timer;
import com.ga.unit.skill.Buffs;
import com.ga.unit.skill.Spell;
import com.ga.unit.skill.Stat;

import java.util.*;

/**
 * Created by Ga on 8/1/2016.
 */
public class Stats {
    private ArrayList<Buffs> effects = new ArrayList<>();
    private ArrayList<Timer> effectTimes = new ArrayList<>();
    private ArrayList<int[]> effectLevels = new ArrayList<>();
    private ArrayList<Spell> effectEffects = new ArrayList<>();
    private HashMap<String, Stat> stats = new HashMap<String, Stat>(){{
        put("Health", new Stat(Math.abs(Global.nextGaussian(50))+15));
        put("Speed", new Stat((int) (Global.rng(50)+Global.r.nextInt(30)+50)));
        put("Stamina", new Stat(3));
        put("Energy", new Stat(3));
        put("Proficiency", new Stat(1));
    }};
    private Timer timer = new Timer(125, true);
    private Unit host;
    public Stats(Unit host){
        this.host = host;
    }
    public Stat getStat(String p) {
        Stat stat = stats.get(p);
        if (stat == null) Global.p("Can't find stat "+p);
        return stat;
    }
    public void setStat(String name, Stat p) {
        stats.put(name,p);
    }
    public void addEffect(Buffs p, int level){
        if (!hasEffect(p)) {
            if (p.stat != null) {
                if (p.buffRel != null) stats.get(p.stat).add(Math.round(p.buff * stats.get(p.buffRel).getMax()), true);
                else stats.get(p.stat).add((int) (p.buff*level), true);
            }
            if (p.effect != null) {
                p.effect.start(host, level);
            }
            effectTimes.add(new Timer(p.time * level));
            effectLevels.add(new int[]{level});
            effectEffects.add(p.effect);
            effects.add(p);
        } else {
            int i = effects.indexOf(p);
            effectTimes.get(i).set(p.time * level);
        }
    }
    public boolean hasEffect(Buffs p){
        return effects.contains(p);
    }
    public void end(){
        for (int i = 0; i < effects.size(); i++) {
            endEffect(effects.get(i));
        }
    }
    public boolean triggerEffect(Buffs p){
        if (hasEffect(p)){
            int[] t = effectLevels.get(effects.indexOf(p));
            t[0]--;
            if (t[0] <= 0) endEffect(p);
            return true;
        }
        return false;
    }
    public void endEffect(Buffs p){
        if (p.stat != null) {
            if (p.buffRel != null) stats.get(p.stat).add(-Math.round(p.buff * stats.get(p.buffRel).getMax()));
            else stats.get(p.stat).add(-(int) p.buff);
        }
        int i = effects.indexOf(p);
        effects.remove(i);
        effectTimes.remove(i);
        effectLevels.remove(i);
        effectEffects.remove(i);
    }
    public void delevel(String stat){
        if (stats.get("Reset Points").get() > 0 && stats.get(stat).get() > 0) {
            stats.get("Level Points").addTotal(1);
            stats.get(stat).addMax(-1);
            stats.get("Reset Points").addTotal(-1);
        }
    }
    public void level(String stat){
        if (stats.get("Level Points").get() > 0) {
            stats.get("Level Points").addTotal(-1);
            stats.get(stat).addMax(1);
            stats.get(stat).add(1);
        }
    }
    public void unrest(){
        timer.reset();
    }
    public void rest(float mag){
        if (timer.update()){
            Stat arm = stats.get("Stamina");
            Stat leg = stats.get("Energy");
            if (mag > 0) {
                arm.add(Math.round(arm.getByPercentage(mag)));
                leg.add(Math.round(leg.getByPercentage(mag)));
            }
        }
    }
    public void update(){
        for (int i = 0; i < effects.size(); i++) {
            if (effectEffects.get(i).perform() || effectTimes.get(i).update()){
                endEffect(effects.get(i));
            } else{

            }
        }
    }
    public void load(String p){
        String[] data = p.split("&");
        for (int i=0;i<data.length;i++){
            String[] pair = data[i].split(":");
            Stat newstat = new Stat(pair[1]);
            newstat.add(0);
            Stat oldstat = stats.get(pair[0]);
            if (oldstat != null) newstat.onChange = oldstat.onChange;
            stats.put(pair[0], newstat);
        }
    }
    public String toString(){
        String result = "";
        Iterator it = stats.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            result += pair.getKey() + ":" + pair.getValue()+"&";
        }
        result = result.substring(0,result.length()-1);
        return result;
    }
}