package com.ga.unit.skill;

import com.ga.tool.L;
import com.ga.tool.T;
import com.ga.unit.Unit;

/**
 * Created by Ga on 8/1/2016. asd
 */
public class Status {
    private Unit host;
    private L<Buffs.Buff> effects = new L<>();
    public Status(Unit host){
        this.host = host;
    }
    public String toString(){
        return effects.toString();
    }
    public void load(T p){
        for (T i : p) {
            Buffs buffs = Buffs.values()[i.get(0).getInt()];
            Buffs.Buff buff = buffs.make(host, i.get(1).getInt());
            effects.add(buff);
            buff.time = i.get(2).getInt();
            buff.var = i.get(3).getInt();
        }
    }
    public void addEffect(Buffs p, int level){
        Buffs.Buff origBuff = getEffect(p);
        if (origBuff == null)
        {
            Buffs.Buff buff = p.make(host, level);
            effects.add(buff);
            buff.onSet();
        } else {
            origBuff.onStack(level);
        }
    }
    private Buffs.Buff getEffect(Buffs p){
        for (Buffs.Buff i : effects){
            if (i.buff == p) return i;
        }
        return null;
    }
    public int getEffectLevel(Buffs p){
        for (Buffs.Buff i : effects){
            if (i.buff == p) return i.level;
        }
        return 0;
    }
    public boolean triggerEffect(Buffs p){
        Buffs.Buff buff = getEffect(p);
        return buff != null && buff.onTrigger();
    }
    public void update(){
        for (int i=0;i<effects.size();i++) {
            Buffs.Buff effect = effects.get(i);
            if (effect.update()){
                effects.remove(i--);
                effect.onFinish(); //if die() happens effect is already removed
            }
        }
    }
    public void initialize(){
        for (Buffs.Buff buff : effects) buff.onLoad();
    }
    public String getEffectsList(){
        if (effects.size() == 0) return "None";
        StringBuilder r = new StringBuilder();
        for (int i=0;i<effects.size();i++){
            r.append(effects.get(i).getDetails());
            if (i != effects.size()-1) r.append("\n");
        }
        return r.toString();
    }
    public L<Buffs.Buff> getEffects(){
        return effects;
    }
    public boolean isDangerous(){
        for (Buffs.Buff i : effects){
            if (i.isDangerous()) return true;
        }
        return false;
    }
    public void clearBuffs() {
        for (int i=0;i<effects.size();i++) {
            Buffs.Buff effect = effects.remove(0);
            effect.onFinish();
        }
    }
}