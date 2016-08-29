package com.ga.unit.intelligence;

import com.ga.tool.P;
import com.ga.unit.Actions;
import com.ga.unit.Man;

/**
 * Created by Ga on 7/30/2016.
 */
public class Search implements Task {
    private Man host;
    private P target;
    public Search(Man host, P pos){
        this.host = host;
        target = pos;
    }
    public void update(){
        if (Math.abs(host.pos.x-target.x) > 100){
            host.setState(host.pos.x<target.x ? Actions.WALKR : Actions.WALKL);
        } else{
            host.setState(Actions.IDLE);
        }
    }
}
