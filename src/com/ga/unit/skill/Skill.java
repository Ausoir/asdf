package com.ga.unit.skill;

import com.ga.tool.Timer;
import com.ga.unit.Man;

/**
 * Created by Ga on 8/1/2016.
 */
public class Skill {
    public String stat = "health";
    public int cost = 0;
    protected Man host;
    protected Timer timer = new Timer(50, false);
    public boolean active = false;
    private int[] lastused = new int[]{-9999};

    public Skill(){}
    public void init(Man host){
        if (this.host != null) end();
        setHost(host);
        start();
    }
    public void setHost(Man host){
        this.host = host;
        host.stats.addEffect(this);
    }
    protected void start(){
        active = true;
        timer.reset();
    }
    protected void end(){
        active = false;
        this.host = null;
    }
    public boolean update(){
        if (active && timer.update()){
            if (host != null) end();
            return true;
        }
        return false;
    }
}
