package com.ga.unit;

import com.ga.map.Global;
import com.ga.map.Shader;
import com.ga.tool.Clips;
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
    private Clip clip;
    public Unit host;
    protected int[] nextAnimation = animation.get(Actions.Idle);
    public Organ[] organs;
    protected Timer frame;
    protected int[][] nextClip;
    protected int[][][] localAnimation;
    protected int[] localFrames;
    protected int current = 0;
    protected Moveset state = Actions.Idle;
    protected int BODY, FOOT;
    protected int legNum = 0, legCurrent = 0;
    protected float appendageSize = 1;
    protected int size = 0;

    public Body(Unit host, String current) {
        this.host = host;
        this.clip = Clips.clip.get(current);
        reset(clip);
        appendageSize = (float) Math.abs(host.rand.nextGaussian()*.3f)+.7f;
        randomize();
        organs[BODY].pt.y = -getHeight();
    }

    protected void setProperties(int[][] p){
        int hpPool = 0;
        for (int i=0;i<organs.length;i++){
            organs[i].setSize(p[0][i]);
            if (p[1][i] == 1) organs[i].vital = true;
            else if (p[1][i] == 2){
                organs[i].leg = true;
                FOOT = i;
                legNum++;
                legCurrent++;
            }
            organs[i].health = p[2][i];
            hpPool += p[2][i];
        }
        for (int i=0;i<organs.length;i++){
            organs[i].health = (int)(((float) organs[i].health / hpPool)*host.get("Health").get());
        }
    }

    private void reset(Clip p) {
        organs = new Organ[p.model.length];
        localAnimation = Clips.clone(p.clip);
        localFrames = Clips.clone(p.frames);
        frame = new Timer(0);
        nextClip = localAnimation[nextAnimation[current]];
        for (int i = 0; i < organs.length; i++) {
            initializeOrgan(p.model,i);
        }
        setProperties(p.property);
        for (Map.Entry<String, int[]> i : p.moveset.entrySet()){
            Moveset move = Actions.getByName(i.getKey());
            if (move == null) move = Skills.getByName(i.getKey());
            animation.put(move, i.getValue());
        }
    }

    private void initializeOrgan(String[][] model, int p) {
        if (organs[p] == null) {
            int clip = Integer.parseInt(model[p][0]);
            Organ parent = null;
            boolean tip = true;
            if (clip-1 == p){
                BODY = p;
            } else {
                if (organs[Math.abs(clip)-1] == null){
                    initializeOrgan(model, Math.abs(clip)-1);
                }
                parent = organs[Math.abs(clip)-1];
                tip = clip < 0;
            }
            if (model[p].length > 1){
                int type = Integer.parseInt(model[p][1]);
                if (type < 0) organs[p] = new Organ(parent, tip) {
                                    public void draw(Graphics2D g) {
                                        image.draw(g, host.pos.x + ptd.x, host.pos.y + ptd.y, (float) Math.atan2(pt.y - ptd.y, pt.x - ptd.x) - 1.57f, host.dir, strokeSize, getLength() + 6);
                                    }
                                    protected void setShader(){
                                    }
                                };
                else if (type == 0) organs[p] = new Organ(parent, tip) {
                                        public void draw(Graphics2D g) {
                                            if (!amputated) image.draw(g, host.pos.x+pt.x, host.pos.y+pt.y, getAtan()+1.57f, host.dir);
                                        }
                                    };
                else organs[p] = new Organ(parent, tip);
                organs[p].type = Math.abs(type);
                if (model[p].length > 2){
                    if (type == 0) organs[p].setImage(model[p][2]);
                    else organs[p].setImage(model[p][2],organs[p].type-1);
                }
            } else {
                organs[p] = new Organ(parent, tip);
            }
            if (Math.abs(nextClip[p][0]) > size) size = Math.abs(nextClip[p][0]);
            if (Math.abs(nextClip[p][1]) > size) size = Math.abs(nextClip[p][1]);
            organs[p].ptd = new P(nextClip[p][0],nextClip[p][1]);
            organs[p].host = host;
        }
    }

    public void setShade(Shader p){
        for (int i=0;i<organs.length;i++){
            organs[i].image.setShader(p);
            organs[i].image.shade();
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

    public int getHeight(){
        return localAnimation[0][FOOT][1];
    }

    public P randomPoint(){
        return organs[(int) (Math.random()*organs.length)].ptd;
    }

    public void setState(Moveset state) {
        if (state == Actions.Dead) organs[BODY].pt.y = 0;
        else if (state == Actions.Down) organs[BODY].pt.y = 0;
        else if (state == Actions.Hit) organs[BODY].pt.y /= 2;
        else setHeight();
        this.state = state;
        current = 0;
        if (animation.containsKey(state)) nextAnimation = animation.get(state);
        else{
//            Global.p("Can't find current state "+state.getName());
            nextAnimation = animation.get(Actions.Idle);
        }
        setClip();
    }

    public void setSpeed(int p){
        frame.set(Math.max(1, frame.time-p));
    }
    public void setMoveSpeed(int p){
        int[] t = animation.get(Actions.Walk);
        for (int i : t) localFrames[i] = Math.max(1, clip.frames[i]-p);
    }

    void setClip() {
        if (current >= nextAnimation.length) current = 0;
        if (nextAnimation[current] == -1) {
            current--;
            host.stateDone();
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

    public int update() {
        int r = -1;
        float high = 40;
        if (!frame.update()) {
            for (int i = 0; i < nextClip.length; i++) {
                if (!organs[i].amputated) {
                    float x = (((nextClip[i][0] * host.dir) + organs[i].pt.x) - organs[i].ptd.x) / (frame.max);
                    float y = ((nextClip[i][1] + organs[i].pt.y) - organs[i].ptd.y) / (frame.max);
                    organs[i].ptd.add(x, y);
                    float t = (x * x) + (y * y);
                    if (t > high) {
                        high = t;
                        r = i;
                    }
                }
            }
        } else {
            current++;
            setClip();
        }
        return r;
    }

    protected void updateStatus(){
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
            legCurrent = legs;
            setHeight();
        }
    }

    private void setHeight(){
        if (legCurrent == 0) organs[BODY].pt.y = 0;
        else organs[BODY].pt.y = -getHeight();
    }

    protected float getLegStatus(){
        if (legNum == 0) return 1;
        return (float)legCurrent/legNum;
    }

    @Override
    public String toString() {
        return nextAnimation[current] + "," + frame.time;
    }
}
