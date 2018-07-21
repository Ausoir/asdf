package com.ga.unit;

import com.ga.game.Observer;
import com.ga.map.Global;
import com.ga.tool.Interactive;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Timer;
import com.ga.unit.man.Man;
import com.ga.unit.skill.Actions;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by ga on 6/12/17. asd
 */
public abstract class AI implements Observer {
    int factoryId = -1;
    int type = 0;
    public Unit host;
    int taskProgress = 0;
    Stack<Behavior> tasks = new Stack<>();
    public Unit target;
    AI(Unit host){
        this.host = host;
    }
    public void start(){
        host.map.updater.addQueue.add(this);
    }
    public void update(){
        if (host.state != Actions.Dead) {
            perceive(160);
            if (tasks.peek().update(host)) {
                tasks.pop();
                taskProgress = 0;
                if (tasks.empty()) bored();
            }
        }
    }
    void bored(){
        setTask(Behavior.Wander);
    }
    public void setTask(Behavior p){
        tasks.push(p);
    }
    public void onAttack(){}
    public void onHit(){
        if (!perceive(400)){
            if (tasks.peek().getAlertness() < 1){
                setTask(Behavior.Search);
            }
        }
    }
    boolean blocked(int dir){
        boolean blocked = false;
        boolean fall = true;
        P front = host.pos.getAdd((host.getSpeed()*3+48)*dir,0);
        if (Math.abs(host.map.mapEdge(front)-5) == 1) blocked = true;
        else {
            Line pp = new Line(host.pos, front);
            Line pp2 = new Line(front.getAdd(0, -40), front.getAdd(0, 40));
            for (Line i : host.map.land) {
                if (i.lineCross(pp) && (Math.abs(i.getSlope()) > Line.MAXSLOPE || !i.clockwise())) {
                    blocked = true;
                }
                if (i.lineCross(pp2)) {
                    fall = false;
                }
            }
        }
        if (blocked){
            tasks.peek().blocked(host, false);
            return true;
        } else if (fall){
            tasks.peek().blocked(host, true);
            return true;
        }
        return false;
    }
    void hear(Prop u, String p){}
    boolean perceive(int d) {
        ArrayList<Unit> seen = host.map.updater.perceive(host, host.pos.getAdd(host.dir < 0 ? -d : d, 0), d * 2);
        for (Unit t : seen) {
            if (t != host && target != t && t.state != Actions.Dead) {
                return perceived(t);
            }
        }
        blocked(host.dir);
        return false;
    }
    boolean perceived(Prop p){
        return false;
    }
    void load(String p) {
        String[] tasks = p.split("&");
        for (int i=tasks.length-1;i>=0;i--) {
            String[] v = tasks[i].split(":");
            setTask(Behavior.values()[Integer.parseInt(v[0])]);
            this.tasks.peek().load(host, v[1]);
        }
    }
    public String toString() {
        String r = "";
        for (int i=tasks.size()-1;i>=0;i--) {
            r += tasks.get(i).toString(host)+"&";
        }
        if (r.length()>0) r = r.substring(0,r.length()-1);
        return factoryId+":"+type+";"+r;
    }
}

class NeutralAI extends AI{
    Timer advertise;
    String ad = "@talk";
    NeutralAI(Unit host) {
        super(host);
    }
    void load(String p){
        super.load(p);
        advertise = new Timer(100+host.rand.nextInt(200), false);
        host.onInteract = new Interactive() {
            public void interact(Man p) {
                if (p == Global.player) hear(p, "hi");
            }
        };
    }
    public boolean perceived(Prop p){
        if (p.faction.canAttack(host.faction) && p instanceof Unit) {
            host.face(host.pos.x < p.pos.x ? -1 : 1, true);
            host.setState(Actions.Sprint);
        } else if (p instanceof Man) {
            if (target != p) {
                target = (Man) p;
                host.setState(Actions.Idle);
                if (advertise.over() && host.getConversation().equals("")){
                    if (host.rand.nextFloat() < .5f) host.say("@c");
                    else host.say(ad);
                    advertise.reset();
                    return true;
                }
            }
        }
        return false;
    }
    public void hear(Prop u, String p) {
        super.hear(u, p);
        if (u == Global.player) {
            if (p.equals("hi")) {
                if ((u.dir == 1 && u.pos.x < host.pos.x) || u.dir == -1 && u.pos.x > host.pos.x) {
                    if (u.pos.getSubtract(host.pos).getDist() < 160){
                        host.say("@use");
                        host.face(u.pos.x<host.pos.x?-1:1,true);
                        shop();
                    }
                }
            }
        }
    }
    void shop(){}
    public void update(){
        super.update();
        advertise.update();
    }
}