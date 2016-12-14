package com.ga.unit;

import com.ga.map.Global;
import com.ga.tool.Clips;
import com.ga.tool.P;
import com.ga.tool.Timer;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Moveset;
import com.ga.unit.skill.Skills;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ga on 8/1/2016.
 */
public class Body {
    protected HashMap<Moveset, int[]> animation = new HashMap<Moveset, int[]>() {{
        put(Actions.Idle, new int[]{0, -1});
    }};
    private Clip clip;
    public Unit host;
    protected int[] nextAnimation = animation.get(Actions.Idle);
    public Organ[] organs;
    protected Timer frame;
    protected int[][] nextClip;
    int[][][] localAnimation;
    int[] localFrames;
    protected int current = 0;
    protected Moveset state = Actions.Idle;
    protected int BODY, FOOT;
    protected int legNum = 0, legCurrent = 0;
    protected float appendageSize = 1;

    Body(Unit host, String current) {
        this.host = host;
        this.clip = Clips.clip.get(current);
        reset(current, clip);
        appendageSize = (float) Math.abs(host.rand.nextGaussian()*.3f)+.7f;
        randomize();
        organs[BODY].pt.y = -localAnimation[0][FOOT][1];
    }

    protected void setProperties(int[][] p){
        for (int i=0;i<organs.length;i++){
            organs[i].setSize(p[1][i]);
            if (p[2][i] == 1) organs[i].vital = true;
            else if (p[2][i] == 2){
                organs[i].leg = true;
                FOOT = i;
                legNum++;
                legCurrent++;
            }
            organs[i].health = p[3][i];
        }
        host.shadow.shadowSize.x = p[0][0];
    }

    private void reset(String name, Clip p) {
        organs = new Organ[p.model.length];
        localAnimation = Clips.clone(p.clip);
        localFrames = Clips.clone(p.frames);
        frame = new Timer(0);
        setState(Actions.Idle);
        for (int i = 0; i < organs.length; i++) {
            initializeOrgan(name, p.model,i);
        }
        setProperties(p.property);
        for (Map.Entry<String, int[]> i : p.moveset.entrySet()){
            Moveset move = Actions.getByName(i.getKey());
            if (move == null) move = Skills.getByName(i.getKey());
            animation.put(move, i.getValue());
        }
    }

    private void initializeOrgan(String name, String[][] model, int p) {
        if (organs[p] == null) {
            int clip = Integer.parseInt(model[p][0]);
            if (clip-1 == p){
                organs[p] = new Organ( null, true);
                BODY = p;
            } else {
                if (organs[Math.abs(clip)-1] == null){
                    initializeOrgan(name, model, Math.abs(clip)-1);
                }
                organs[p] = new Organ(organs[Math.abs(clip)-1], clip<0);
            }
            organs[p].ptd = new P(nextClip[p][0],nextClip[p][1]);
            organs[p].host = host;
            if (model[p].length > 1){
                organs[p].type = Integer.parseInt(model[p][1]);
                if (model[p].length > 2) organs[p].setImage(model[p][2]);
                else if (organs[p].type != 0) organs[p].setImage(name+"_skin", organs[p].type-1);
            }
        }
    }

    protected void randomize() {
        for (int i = 0; i < localAnimation.length; i++) {
            randomizeSection(i);
        }
        int dead = animation.get(Actions.Dead)[0];
        localAnimation[dead] = localAnimation[dead + Global.r.nextInt(3)];
        int i = (int) Math.abs(-organs[BODY].ptd.y/5f);
        if (i != 0) randomizeSection(dead);
    }

    protected void randomizeSection(int p) {
        for (int t = 0; t < localAnimation[p].length; t++) {
            int x = (int) (localAnimation[p][t][0]*appendageSize);
            int y = (int) (localAnimation[p][t][1]*appendageSize);
            localAnimation[p][t] = new int[]{x, y};
        }
    }

    public void setState(Moveset state) {
        if (state == Actions.Dead) organs[BODY].pt.y = 0;
        else if (state == Actions.Revive) organs[BODY].pt.y = -localAnimation[0][FOOT][1];
        this.state = state;
        current = 0;
        if (animation.containsKey(state)) nextAnimation = animation.get(state);
        else{
            Global.p("Can't find current state "+state.getName());
            nextAnimation = animation.get(Actions.Idle);
        }
        setClip();
    }

    public void setSpeed(int p){
        frame.set(Math.max(1, frame.time-p));
    }

    void setClip() {
        if (current >= nextAnimation.length) current = 0;
        if (nextAnimation[current] == -1) {
            current--;
            host.setState(Actions.Idle);
            state = Actions.Idle;
        }
        nextClip = localAnimation[nextAnimation[current]];
        frame.set(localFrames[nextAnimation[current]]);
    }

    protected void rotateAppendage(int organ, int frame){
        int length = Math.abs(clip.clip[frame][organ][0])+Math.abs(clip.clip[frame][organ][1]);
        P atanDir = host.target.getAdd(localAnimation[frame][organ][0]*host.dir,localAnimation[frame][organ][1]).getAtan(1);
        localAnimation[frame][organ][0] = (int) (length*atanDir.x)*host.dir;
        localAnimation[frame][organ][1] = (int) (length*atanDir.y);
    }

    public void update() {
        if (!frame.update()) {
            for (int i = 0; i < nextClip.length; i++) {
                if (!organs[i].amputated) {
                    float x = (((nextClip[i][0] * host.dir) + organs[i].pt.x) - organs[i].ptd.x) / (frame.max);
                    float y = ((nextClip[i][1] + organs[i].pt.y) - organs[i].ptd.y) / (frame.max);
                    organs[i].ptd.add(x, y);
                    if ((x * x) + (y * y) > 50) {
                        host.moveTriggered(i);
                    }
                }
            }
        } else {
            current++;
            setClip();
        }
    }

    protected void getStatus(){
        int legs = legNum;
        for (Organ o : organs) {
            if (o.amputated) {
                if (o.leg){
                    legs--;
                }
                if (o.vital) {
                    setState(Actions.Down);
                    host.die();
                    return;
                }
            }
        }
        if (legs != legNum){
            if (legs == 0) organs[BODY].pt.y = 0;
            legCurrent = legs;
        }
    }

    protected float getLegStatus(){
        return legCurrent/legNum;
    }

    @Override
    public String toString() {
        return nextAnimation[current] + "," + frame.time;
    }
}
