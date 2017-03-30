package com.ga.unit.intelligence;

import com.ga.map.Global;
import com.ga.tool.Line;
import com.ga.unit.Man;
import com.ga.unit.Unit;
import com.ga.unit.skill.Actions;

/**
 * Created by Ga on 7/30/2016.
 */
class Travel implements Task {
    private Unit host;
    private int travel; //top left:1, top right:2, bottom left:3, bottom right:4
    private boolean man;
    protected Travel(Unit host, int travel){
        this.host = host;
        this.travel = travel;
    }
    protected Travel(Man host, int travel){
        this((Unit) host, travel);
        man = true;
    }

    @Override
    public void blocked(boolean fall) {
    }

    public void update(){
        switch (travel){
            case 1:
            case 3:
                host.face(-1, true);
                break;
            case 2:
            case 4:
                host.face(1, true);
                break;
        }
        if (man && (travel == 1 || travel == 2)) {
            for (final Line i : Global.map.land) {
                if (i.opt == 1) {
                    if (i.lineCross(new Line(host.pos.x, host.pos.y, host.pos.x + (24*host.dir), host.pos.y))) {
                        host.pos.x = i.pt.x - (24*host.dir);
                        host.setState(Actions.Climb);
                        host.delay.set(999);
                        ((Man)host).rope = i;
                        return;
                    }
                }
            }
        }
        host.setState(Actions.Walk);
    }

    @Override
    public int getAlertness() {
        return 0;
    }
}
