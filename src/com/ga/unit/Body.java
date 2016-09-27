package com.ga.unit;

import com.ga.map.Global;
import com.ga.tool.Clips;
import com.ga.tool.Line;
import com.ga.tool.P;
import com.ga.tool.Timer;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Moveset;
import com.ga.unit.skill.Skills;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ga on 8/1/2016.
 */
public class Body {
    protected HashMap<Moveset, int[]> animation = new HashMap<Moveset, int[]>() {{
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
    protected Moveset state = Actions.Idle;
    protected Organ foot;
    protected int BODY;

    Body(Unit host, String clip) {
        this.host = host;
        reset(Clips.valueOf(clip).get());
        randomize();
    }

    protected void setProperties(int[][] p){
        Color c = new Color(p[0][0],p[0][1],p[0][2],p[0][3]);
        for (int i=0;i<organs.length;i++){
            organs[i].setStyle(c, p[1][i]);
            if (p[3][i] == 1) organs[i].vital = true;
            organs[i].health = p[4][i];
        }
        foot = organs[p[2][0]];
    }

    private void reset(Clip p) {
        organs = new Organ[p.model.length];
        localAnimation = Clips.clone(p.clip);
        localFrames = Clips.clone(p.frames);
        frame = new Timer(0);
        setState(Actions.Idle);
        for (int i = 0; i < organs.length; i++) {
            initializeOrgan(p.model,i);
        }
        setProperties(p.property);
        for (Map.Entry<String, int[]> i : p.moveset.entrySet()){
            Moveset move = Actions.getByName(i.getKey());
            if (move == null) move = Skills.getByName(i.getKey());
            animation.put(move, i.getValue());
        }
        setHeight();
    }

    protected void setHeight(){
        float t = -foot.getHeight();
        for (Organ i : organs) i.pt.y = t;
    }

    private void initializeOrgan(String[][] model, int p) {
        if (organs[p] == null) {
            int clip = Integer.parseInt(model[p][0]);
            if (clip-1 == p){
                organs[p] = new Organ( null, true);
                BODY = p;
            } else {
                if (organs[Math.abs(clip)-1] == null){
                    initializeOrgan(model, Math.abs(clip)-1);
                }
                organs[p] = new Organ(organs[Math.abs(clip)-1], clip<0);
                organs[p].stroke = new BasicStroke(13);
                organs[p].color = Color.black;
            }
            organs[p].ptd = new P(nextClip[p][0],nextClip[p][1]);
            organs[p].host = host;
            if (model[p].length > 1){
                organs[p].type = Integer.parseInt(model[p][1]);
                if (model[p].length > 2) organs[p].setImage(model[p][2]);
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
        int i = (int) Math.abs(-foot.getHeight()/5f);
        if (i != 0) randomizeSection(dead, Global.r.nextInt(i*2) - i, Global.r.nextInt(i*2) - i);
    }

    protected void randomizeSection(int p, int x, int y) {
        for (int t = 0; t < localAnimation[p].length; t++)
            localAnimation[p][t] = new int[]{localAnimation[p][t][0] + x, localAnimation[p][t][1] + y};
    }

    public void setState(Moveset state) {
        if (state == Actions.Dead) organs[BODY].pt.y = 0;
        if (state == Actions.Revive) setHeight();
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

    protected void rotateAppendage(int organ, int frame){
        int length = Math.abs(Clips.Man.get().clip[frame][organ][0])+Math.abs(Clips.Man.get().clip[frame][organ][1]);
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
                    if ((x * x) + (y * y) > 40) {
                        host.moveTriggered(i);
                    }
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
