package com.ga.unit.skill;

import com.ga.tool.Performer;
import com.ga.tool.Timer;
import com.ga.unit.Man;

/**
 * Created by Ga on 8/30/2016.
 */
public class Skill extends Performer {
    protected Timer timer = new Timer(-1);
    protected String stat;
    protected float time, buff;
    protected int added;
    protected Man host;
    Skill(int time, String stat, int buff){
        this.time = time;
        this.stat = stat;
        this.buff = buff;
    }
    void start(Man host){
        this.host = host;
        if (buff >= 0) added = (int) buff;
        else added = (int) host.get(stat).getByPercentage(-buff);
        host.get(stat).add(added,true);
        if (time >= 0) timer = new Timer((int) time);
        else timer = new Timer((int) (host.get(stat).get()*-time));
    }
    boolean update(){
        if (timer.update()){
            host.get(stat).add(-added, true);
            return true;
        }
        return false;
    }
}
