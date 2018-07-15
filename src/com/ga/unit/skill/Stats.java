package com.ga.unit.skill;

import com.ga.map.Global;
import com.ga.tool.Timer;
import com.ga.unit.Unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ga on 8/1/2016. asd
 */
public class Stats {
    private ArrayList<Buff> effects = new ArrayList<>();
    private HashMap<String, Stat> stats = new HashMap<>();
    public Timer restTimer = new Timer(480, true);
    private Unit host;
    public Stats(Unit host){
        this.host = host;
    }
    public Stat getStat(String p) {
        Stat stat = stats.get(p);
        if (stat == null) Global.p("Can't find stat "+p);
        return stat;
    }
    public void addStat(String p, int value){
        stats.put(p, new Stat(value));
    }
    public void addEffect(Buffs p, int level){
        Buff origBuff = getEffect(p);
        if (origBuff == null)
        {
            Buff buff = p.make(host, level);
            buff.onSet();
            effects.add(buff);
        } else {
            origBuff.onStack(level);
        }
    }
    private Buff getEffect(Buffs p){
        for (Buff i : effects){
            if (i.buff == p) return i;
        }
        return null;
    }
    public int getEffectLevel(Buffs p){
        for (Buff i : effects){
            if (i.buff == p) return i.level;
        }
        return 0;
    }
    public boolean triggerEffect(Buffs p){
        Buff buff = getEffect(p);
        return buff != null && buff.onTrigger();
    }
    public boolean rest(int mag){
        if (restTimer.update(mag)){
            stats.get("Strength").heal();
            host.stateSet(Actions.Idle);
            return true;
        }
        return false;
    }
    public void update(){
        for (int i=0;i<effects.size();i++) {
            Buff effect = effects.get(i);
            if (effect.update()){
                effect.onFinish();
                effects.remove(i--);
            }
        }
    }
    public void load(String p){
        String[] statOrBuff = p.split("/");
        String[] data = statOrBuff[0].split("&");
        for (String i : data){
            String[] pair = i.split(":");
            Stat newstat = new Stat(pair[1]);
            stats.put(pair[0], newstat);
        }
        if (statOrBuff.length > 1) {
            data = statOrBuff[1].split(",");
            for (String i : data) {
                String[] t = i.split("&");
                Buffs buffs = Buffs.values()[Integer.parseInt(t[0])];
                Buff buff = buffs.make(host, Integer.parseInt(t[1]));
                effects.add(buff);
                effects.get(effects.size()-1).time = Integer.parseInt(t[2]);
                effects.get(effects.size()-1).var = Integer.parseInt(t[3]);
            }
        }
    }
    public void initialize(){
        for (Buff buff : effects) buff.onLoad();
    }
    public String getEffectsList(){
        if (effects.size() == 0) return "None";
        String r = "";
        for (int i=0;i<effects.size();i++){
            r += effects.get(i).getDescription();
            if (i != effects.size()-1) r += "\n";
        }
        return r;
    }
    public String toString(){
        String result = "";
        for (Map.Entry pair : stats.entrySet()){
            result += pair.getKey() + ":" + pair.getValue()+"&";
        }
        result = result.substring(0,result.length()-1)+"/";
        for (Buff i : effects){
            result += i.toString()+",";
        }
        return result.substring(0,result.length()-1);
    }
    public void endBuff(Buffs p){
        for (int i=0;i<effects.size();i++) {
            Buff effect = effects.get(i);
            if (effect.buff == p){
                effects.remove(i--);
                effect.onFinish();
            }
        }
    }
    public void clearBuffs() {
        for (int i=0;i<effects.size();i++) {
            Buff effect = effects.remove(0);
            effect.onFinish();
        }
    }
}