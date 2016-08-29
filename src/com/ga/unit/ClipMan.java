package com.ga.unit;

import com.ga.game.FileManager;
import com.ga.map.Global;
import com.ga.tool.Clips;
import com.ga.tool.P;
import com.ga.tool.Timer;

import java.awt.*;
import java.util.HashMap;

/**
 * Created by Ga on 8/1/2016.
 */
public class ClipMan {
    private static final HashMap<Actions, int[]> animation = new HashMap<Actions, int[]>() {{
        put(Actions.IDLE, new int[]{0, -1});
        put(Actions.WALKL, new int[]{1, 2, 3, 2});
        put(Actions.WALKR, new int[]{1, 2, 3, 2});
        put(Actions.JUMP, new int[]{4, -1});
        put(Actions.ATTACK, new int[]{5, 6, 7, 2, -1});
        put(Actions.SWING, new int[]{20, 21, 22, -1});
        put(Actions.KICK, new int[]{12, 13, 2, -1});
        put(Actions.HIT, new int[]{14, -1});
        put(Actions.DOWN, new int[]{15, -1});
        put(Actions.DEAD, new int[]{16, -1});
        put(Actions.SKILL, new int[]{19, -1});
        put(Actions.SUMMON, new int[]{19, -1});
        put(Actions.DROP, new int[]{19, -1});
        put(Actions.WHIRLWIND, new int[]{8, 9, 10, 11, -1});
        put(Actions.ENTER, new int[]{19, -1});
        put(Actions.CAST, new int[]{19, -1});
//        put(Actions.DUCK, new int[]{23, -1});
        put(Actions.DUCK, new int[]{19, -1});
        put(Actions.CONSUME, new int[]{24,2, -1});
        put(Actions.EXHAUSTED, new int[]{25, 26});
        put(Actions.THROW, new int[]{20,21,22,2,-1});
        put(Actions.SHOOT, new int[]{27,28,2,-1});
        put(Actions.PICKUP, new int[]{23,-1});
    }};
    public static final int FIST = 1, OFIST = 5, FOOT = 3;
    public Man host;
    private int[] nextAnimation = animation.get(Actions.IDLE);
    public Organ[] organs = new Organ[6];
    private Timer frame;
    private int[][] nextClip;
    int[][][] localAnimation;
    private int clip = 0;
    private int height = 0;
    private Actions state = Actions.IDLE;

    ClipMan(Man host) {
        this.host = host;
        reset();
    }

    private void reset() {
        frame = new Timer(0);
        Clips.man = FileManager.readClip("unit/Man.2d");
        nextClip = Clips.clone(Clips.man.model);
        localAnimation = Clips.clone(Clips.man.clip);
        randomize();
        for (int i = 0; i < nextClip.length; i++) {
            initializeOrgan(i);
        }
        organs[0].setStyle(new Color(0, 0, 0, 0), 0);
    }

    void resetLocalAnimation(int p){
        localAnimation[p] = Clips.man.clip[p];
        localAnimation[p][4][1] -= height;
        localAnimation[p][3][1] += height;
        localAnimation[p][2][1] += height;
        localAnimation[p][1][1] += height / 2;
        localAnimation[p][5][1] += height / 2;
    }

    private void randomize() {
        height = (int) Math.max(Math.min(host.rand.nextGaussian() * 5, 15), -15);
        for (int i = 0; i < localAnimation.length; i++) {
            randomizeSection(i, (int) (Global.clamp((float) host.rand.nextGaussian(),-1,1) * 2), (int) (Global.clamp((float) host.rand.nextGaussian(),-1,1) * 2));
            localAnimation[i][4][1] -= height;
            localAnimation[i][3][1] += height;
            localAnimation[i][2][1] += height;
            localAnimation[i][1][1] += height / 2;
            localAnimation[i][5][1] += height / 2;
        }
        int dead = animation.get(Actions.DEAD)[0];
        localAnimation[dead] = localAnimation[dead + Global.r.nextInt(3)];
        randomizeSection(dead, Global.r.nextInt(30) - 15, Global.r.nextInt(30) - 15);
    }

    public int getHeight(){
        return localAnimation[2][3][1];
    }

    private void randomizeSection(int p, int x, int y) {
        for (int t = 0; t < localAnimation[p].length; t++)
            localAnimation[p][t] = new int[]{localAnimation[p][t][0] + x, localAnimation[p][t][1] + y};
    }

    private void initializeOrgan(int p) {
        if (organs[p] == null) {
            if (nextClip[p][0] == p) organs[p] = new Organ(new P(), 0, 0);
            else {
                if (organs[Math.abs(nextClip[p][0])] == null) initializeOrgan(Math.abs(nextClip[p][0]));
                organs[p] = new Organ(nextClip[p][0] <= 0 ? organs[nextClip[p][0] * -1].ptd : organs[nextClip[p][0]].pt, 0, 0);
                organs[p].stroke = new BasicStroke(13);
                organs[p].color = Color.black;
            }
        }
    }

    public void setState(Actions state) {
        this.state = state;
        clip = 0;
        nextAnimation = animation.get(state);
        switch (state){
            case SHOOT:
                rotateAppendage(OFIST,28);
                break;
            case THROW:
                rotateAppendage(FIST,21);
                break;
            case ATTACK:
                rotateAppendage(FIST,6);
                break;
            case KICK:
                rotateAppendage(FOOT,13);
                break;
        }
        setClip();
    }
    private void rotateAppendage(int organ, int frame){
        int length = Math.abs(Clips.man.clip[frame][organ][0])+Math.abs(Clips.man.clip[frame][organ][1]);
        P atanDir = host.target.getAdd(localAnimation[frame][organ][0]*host.dir,localAnimation[frame][organ][1]).getAtan(1);
        localAnimation[frame][organ][0] = (int) (length*atanDir.x)*host.dir;
        localAnimation[frame][organ][1] = (int) (length*atanDir.y);
    }

    void setClip() {
        if (clip >= nextAnimation.length) clip = 0;
        if (nextAnimation[clip] == -1) {
            clip--;
            host.setState(Actions.IDLE);
        }
        nextClip = localAnimation[nextAnimation[clip]];
        frame.set(Clips.man.frames[nextAnimation[clip]]);
    }

    public void update() {
        if (!frame.update()) {
            for (int i = 0; i < nextClip.length; i++) {
                float x = (((nextClip[i][0] * host.dir) + organs[i].pt.x) - organs[i].ptd.x) / (frame.max - 1);
                float y = ((nextClip[i][1] + organs[i].pt.y) - organs[i].ptd.y) / (frame.max - 1);
                if (state == Actions.ATTACK || state == Actions.KICK || state == Actions.WHIRLWIND) {
                    if ((x * x) + (y * y) > 50) {
                        if (i == 1) host.attack();
                        else if (i == 3) {
                            Updater.collide(host, organs[i].getAdd(host.pos), 5 + host.get("damage").get(), false);
                        }
                    }
                }
                organs[i].ptd.add(x, y);
            }
        } else {
            clip++;
            setClip();
        }
    }

    public String toString() {
        return nextAnimation[clip] + "," + frame.time;
    }
}
