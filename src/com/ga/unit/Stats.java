package com.ga.unit;

import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.tool.Timer;
import com.ga.unit.skill.Buffs;
import com.ga.unit.skill.Spell;
import com.ga.unit.skill.Stat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Ga on 8/1/2016.
 */
public class Stats {
    private ArrayList<Buffs> effects = new ArrayList<>();
    private ArrayList<Timer> effectTimes = new ArrayList<>();
    private ArrayList<int[]> effectLevels = new ArrayList<>();
    private ArrayList<Spell> effectEffects = new ArrayList<>();
    private HashMap<String, Stat> stats = new HashMap<>();
    private Timer timer = new Timer(100, true);
    Timer bloodTimer = new Timer(-1, true);
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
            if (p.start != null) p.start.perform(host, level);
            effectTimes.add(new Timer((int) (p.time + (level*p.time*.2f)), false));
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
            stats.get(stat).addTotal(-1);
            stats.get("Reset Points").addTotal(-1);
        }
    }
    public void level(String stat){
        if (stats.get("Level Points").get() > 0) {
            stats.get("Level Points").addTotal(-1);
            stats.get(stat).addTotal(1);
        }
    }
    public void unrest(){
        timer.reset();
    }
    public boolean rest(int mag){
        if (timer.update(mag)){
            stats.get("Stamina").heal();
            return true;
        }
        return false;
    }
    public void update(){
        for (int i = 0; i < effects.size(); i++) {
            effectEffects.get(i).perform(host, effectLevels.get(i)[0]);
            if (effectTimes.get(i).update()){
                endEffect(effects.get(i));
            }
        }
        if (bloodTimer.update()) Particle.explode("Blood",host.getRandomPoint(),new P(),Global.r.nextInt(4),5,0,true,24);
    }
    public void load(String p){
        String[] statOrBuff = p.split("/");
        String[] data = statOrBuff[0].split("&");
        for (int i=0;i<data.length;i++){
            String[] pair = data[i].split(":");
            Stat newstat = new Stat(pair[1]);
            newstat.add(0);
            stats.put(pair[0], newstat);
        }
        if (statOrBuff.length > 1) {
            data = statOrBuff[1].split(";");
            for (int i = 0; i < data.length; i++) {
                String[] t = data[i].split("&");
                Buffs buff = Buffs.getByName(t[0]);
                addEffect(buff,Integer.parseInt(t[2]));
                effectTimes.get(effectTimes.size()-1).load(t[1]);
                if (buff.stat != null) {
                    if (buff.buffRel != null) stats.get(buff.stat).add(-Math.round(buff.buff * stats.get(buff.buffRel).getMax()));
                    else stats.get(buff.stat).add(-(int) buff.buff);
                }
            }
        }
    }
    public int getEffectsSize(){
        return effects.size();
    }
    public String getEffectsList(){
        String r = "";
        for (int i=0;i<effects.size();i++){
            r += effects.get(i).name() + " (Level " + effectLevels.get(i)[0] + ") - " + effectTimes.get(i).time + "s";
            if (i != effects.size()-1) r += "\n";
        }
        return r;
    }
    public String toString(){
        String result = "";
        Iterator it = stats.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            result += pair.getKey() + ":" + pair.getValue()+"&";
        }
        result = result.substring(0,result.length()-1)+"/";
        for (int i=0;i<effects.size();i++){
            result += effects.get(i).name()+"&";
            result += effectTimes.get(i)+"&";
            result += effectLevels.get(i)[0]+";";
        }
        return result.substring(0,result.length()-1);
    }
}