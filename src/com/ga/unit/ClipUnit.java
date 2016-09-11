package com.ga.unit;

import com.ga.map.Global;
import com.ga.tool.Clips;
import com.ga.tool.P;
import com.ga.tool.Timer;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ga on 8/1/2016.
 */
public class ClipUnit {
    protected HashMap<Actions, int[]> animation = new HashMap<Actions, int[]>() {{
        put(Actions.Idle, new int[]{0, -1});
    }};
    public Unit host;
    protected int[] nextAnimation = animation.get(Actions.Idle);
    public Organ[] organs;
    protected Timer frame;
    protected int[][] nextClip;
    int[][][] localAnimation;
    int[] localFrames;
    protected int clip = 0;
    protected Actions state = Actions.Idle;
    protected Organ HEAD;
    protected P BODY;
    protected int FOOT;

    ClipUnit(Unit host, String clip) {
        this.host = host;
        reset(Clips.valueOf(clip).get());
        randomize();
    }

    protected void setProperties(int[][] p){
        Color c = new Color(p[0][0],p[0][1],p[0][2],p[0][3]);
        for (int i=0;i<organs.length;i++){
            organs[i].setStyle(c, p[1][i]);
            if (p[2][i] == 1) organs[i].color = organs[i].color.darker();
        }
    }

    private void reset(Clip p) {
        nextClip = Clips.clone(p.model);
        organs = new Organ[nextClip.length];
        localAnimation = Clips.clone(p.clip);
        localFrames = Clips.clone(p.frames);
        frame = new Timer(0);
        for (int i = 0; i < organs.length; i++) {
            initializeOrgan(i);
        }
        setProperties(p.property);
        HEAD = organs[Math.abs(p.property[3][0])-1];
        BODY = getOrgan(p.property[3][1]);
        FOOT = Math.abs(p.property[3][2])-1;
        BODY.set(0, -localAnimation[0][FOOT][1]);
        for (Map.Entry<String, int[]> i : p.moveset.entrySet()){
            animation.put(Actions.valueOf(i.getKey()), i.getValue());
        }
    }

    private P getOrgan(int p){
        if (p < 0) return organs[-p-1].ptd;
        else return organs[p-1].pt;
    }

    private void initializeOrgan(int p) {
        if (organs[p] == null) {
            if (nextClip[p][0]-1 == p){
                organs[p] = new Organ(new P(), 0, 0);
            } else {
                if (organs[Math.abs(nextClip[p][0])-1] == null){
                    initializeOrgan(Math.abs(nextClip[p][0])-1);
                }
                organs[p] = new Organ(getOrgan(nextClip[p][0]), 0, 0);
                organs[p].stroke = new BasicStroke(13);
                organs[p].color = Color.black;
            }
        }
    }

    protected void resetLocalAnimation(int p){
    }

    protected void randomize() {
        for (int i = 0; i < localAnimation.length; i++) {
            randomizeSection(i, (int) (Global.clamp((float) host.rand.nextGaussian(),-1,1) * 2), (int) (Global.clamp((float) host.rand.nextGaussian(),-1,1) * 2));
        }
        int dead = animation.get(Actions.Dead)[0];
        localAnimation[dead] = localAnimation[dead + Global.r.nextInt(3)];
        int i = Math.abs(-localAnimation[0][FOOT][1]/3);
        if (i != 0) randomizeSection(dead, Global.r.nextInt(i*2) - i, Global.r.nextInt(i*2) - i);
    }

    protected void randomizeSection(int p, int x, int y) {
        for (int t = 0; t < localAnimation[p].length; t++)
            localAnimation[p][t] = new int[]{localAnimation[p][t][0] + x, localAnimation[p][t][1] + y};
    }

    public void setState(Actions state) {
        switch (state) {
            case Revive:
                BODY.set(0, -localAnimation[0][FOOT][1]);
                break;
            case Dead:
                BODY.set(0, 0);
                break;
        }
        this.state = state;
        clip = 0;
        if (animation.containsKey(state)) nextAnimation = animation.get(state);
        else{
            Global.p("Can't find clip state "+state.getName());
            nextAnimation = animation.get(Actions.Idle);
        }
        setClip();
    }

    void setClip() {
        if (clip >= nextAnimation.length) clip = 0;
        if (nextAnimation[clip] == -1) {
            clip--;
            host.setState(Actions.Idle);
            state = Actions.Idle;
        }
        nextClip = localAnimation[nextAnimation[clip]];
        frame.set(localFrames[nextAnimation[clip]]);
    }

    public void update() {
        if (!frame.update()) {
            for (int i = 0; i < nextClip.length; i++) {
                float x = (((nextClip[i][0] * host.dir) + organs[i].pt.x) - organs[i].ptd.x) / (frame.max);
                float y = ((nextClip[i][1] + organs[i].pt.y) - organs[i].ptd.y) / (frame.max);
                organs[i].ptd.add(x,y);
                if ((x*x)+(y*y)>40) {
                    host.moveTriggered(i);
                }
            }
        } else {
            clip++;
            setClip();
        }
    }

    @Override
    public String toString() {
        return nextAnimation[clip] + "," + frame.time;
    }
}
