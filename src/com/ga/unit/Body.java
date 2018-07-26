package com.ga.unit;

import com.ga.map.Global;
import com.ga.tool.Clips;
import com.ga.tool.P;
import com.ga.tool.SyncRand;
import com.ga.tool.Timer;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ga on 8/1/2016. asd
 */
public class Body {
    private HashMap<String, int[][]> animation = new HashMap<String, int[][]>() {{
        put("Idle", new int[][]{new int[]{0, -1}, new int[]{-2}});
    }};
    private Clip clip;
    public Unit host;
    private int[] nextAnimation = animation.get("Idle")[0];
    private int nextTrigger = -2;
    public Organ[] organs;
    protected Timer frame;
    private int[][] nextClip;
    protected int[][][] localAnimation;
    private int[] localFrames;
    protected int current = 0;
    protected String state = "Idle";
    public int BODY;
    private int legNum = 0, legCurrent = 0;
    private int handNum = 0, handCurrent = 0;
    private float appendageSize = 1;
    protected int size = 0;

    protected Body(Unit host, String current) {
        this.host = host;
        this.clip = Clips.clip.get(current);
        set(clip);
        appendageSize = (float) Math.abs(host.rand.nextGaussian()*.3f)+.7f;
        randomize();
        organs[BODY].pt.y = -getHeight();
    }

    void setOrganHealth(){
        int hpPool = 0;
        for (Organ organ : organs) hpPool += organ.health;
//        int mod = host.get("Strength").getMax()*organs.length;
        int mod = 100;
        for (Organ organ : organs) {
            organ.health = (int) (((float) organ.health / hpPool) * mod);
        }
    }

    private void setProperties(int[][] p){
        for (int i=0;i<organs.length;i++){
            organs[i].setSize(p[0][i]);
            int type = p[1][i];
            if (type >= 4){
                organs[i].vital = true;
                type -= 4;
            }
            if (type >= 2){
                organs[i].leg = true;
                legNum++;
                legCurrent++;
                type -= 2;
            }
            if (type == 1){
                organs[i].hand = true;
                handNum++;
                handCurrent++;
            }
            organs[i].health = p[2][i];
        }
    }

    private void set(Clip p) {
        organs = new Organ[p.model.length];
        localAnimation = Clips.clone(p.clip);
        localFrames = Clips.clone(p.frames);
        frame = new Timer(0);
        nextClip = localAnimation[nextAnimation[current]];
        for (int i = 0; i < organs.length; i++)
            initializeOrgan(p.model,i);
        for (int i = 0; i < organs.length; i++)
            organs[i].setMaxLength((int) Math.sqrt((nextClip[i][0]*nextClip[i][0])+(nextClip[i][1]*nextClip[i][1])));
        setProperties(p.property);
        for (Map.Entry<String, int[]> i : p.moveset.entrySet()){
            int[] v = i.getValue();
            int high = 0;
            int highID = v[0];
            for (int ii=1;ii<v.length;ii++){
                if (v[ii] != -1) {
                    for (int iii=0;iii<organs.length;iii++) {
                        int t = (int) (Math.pow(localAnimation[v[ii]][iii][0]-localAnimation[v[ii-1]][iii][0], 2) +
                                Math.pow(localAnimation[v[ii]][iii][1]-localAnimation[v[ii-1]][iii][1], 2));
                        if (t > high) {
                            high = t;
                            highID = v[ii];
                        }
                    }
                }
            }
            animation.put(i.getKey(), new int[][]{v, new int[]{highID}});
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
                if (type == 0) organs[p] = new Organ(parent, tip) { //Head
                                        public void draw(Graphics2D g) {
//                                            g.drawOval((int)(host.pos.x+pt.x-12+Global.rel.x), (int)(host.pos.y+pt.y-24+Global.rel.y), 24, 24);
                                            image.draw(g, host.pos.x+pt.x, host.pos.y+pt.y, getAtan()+1.57f, host.dir);
                                        }
                                    };
                else organs[p] = new Organ(parent, tip);
                organs[p].host = host;
                organs[p].type = Math.abs(type);
                if (type == 0) organs[p].setImage(model[p][2]);
                else organs[p].setImage(model[p][2],organs[p].type-1);
            } else {
                organs[p] = new Organ(parent, tip);
                organs[p].host = host;
            }
            if (Math.abs(nextClip[p][0]) > size) size = Math.abs(nextClip[p][0]);
            if (Math.abs(nextClip[p][1]) > size) size = Math.abs(nextClip[p][1]);
            organs[p].ptd = new P(nextClip[p][0],nextClip[p][1]);
        }
    }

    private void randomize() {
        for (int i = 0; i < localAnimation.length; i++) {
            randomizeSection(i);
        }
        int dead = animation.get("Dead")[0][0];
        localAnimation[dead] = localAnimation[dead + Global.r.nextInt(2)];
        int i = (int) Math.abs(-organs[BODY].ptd.y/5f);
        if (i != 0) randomizeSection(dead);
    }

    private void randomizeSection(int p) {
        for (int t = 0; t < localAnimation[p].length; t++) {
            int x = (int) (localAnimation[p][t][0]*appendageSize);
            int y = (int) (localAnimation[p][t][1]*appendageSize);
            localAnimation[p][t] = new int[]{x, y};
        }
    }

    int getHeight(){
        int leg = getLeg();
        if (leg == -1) return 0;
        return Math.max(0, localAnimation[0][leg][1]);
    }

    P randomPoint(){
        return organs[SyncRand.r.nextInt(organs.length)].ptd;
    }

    public void setState(String state) {
        this.state = state;
        current = 0;
        if (animation.containsKey(state)){
            int[][] t = animation.get(state);
            nextAnimation = t[0];
            nextTrigger = t[1][0];
        } else {
            Global.p("Can't find current state "+state);
            nextAnimation = animation.get("Idle")[0];
        }
        setClip();
    }

    public int setSpeed(int p){
        frame.set(Math.max(1, frame.time-p));
        int total = 0;
        for (int i : nextAnimation){
            if (i != -1) total += localFrames[i];
        }
        return total;
    }
    public void setMoveSpeed(int p){
        int[] t = animation.get("Left")[0];
        for (int i : t) localFrames[i] = Math.max(1, clip.frames[i]-p);
    }

    public void setClip() {
        if (current >= nextAnimation.length) current = 0;
        if (nextAnimation[current] == -1) {
            current--;
            state = "Idle";
        }
        nextClip = localAnimation[nextAnimation[current]];
        frame.set(localFrames[nextAnimation[current]]);
    }

    protected void rotateAppendage(int organ, int frame, float offset){
        int length = Math.abs(clip.clip[frame][organ][0])+Math.abs(clip.clip[frame][organ][1]);
        float atan = host.target.getAdd(localAnimation[frame][organ][0]*host.dir, localAnimation[frame][organ][1]+getHeight()).getAtan();
        atan += offset;
        localAnimation[frame][organ][0] = (int) (length*Global.cos(atan))*host.dir;
        localAnimation[frame][organ][1] = (int) (length*Global.sin(atan));
    }

    public boolean update() {
        boolean triggered = false;
        if (!frame.update()) {
            for (int i = 0; i < nextClip.length; i++) {
                if (!organs[i].amputated) {
                    float x = (((nextClip[i][0] * host.dir) + organs[i].pt.x) - organs[i].ptd.x) / (frame.max);
                    float y = ((nextClip[i][1] + organs[i].pt.y) - organs[i].ptd.y) / (frame.max);
                    organs[i].ptd.add(x, y);
                }
            }
        } else {
            triggered = nextAnimation[current] == nextTrigger;
            current++;
            setClip();
        }
        return triggered;
    }

    void updateStatus(){
        legCurrent = legNum;
        handCurrent = handNum;
        for (Organ o : organs) {
            if (o.amputated) {
                if (o.leg){
                    legCurrent--;
                }
                if (o.hand){
                    handCurrent--;
                }
                if (o.vital) {
                    setState("Down");
                    host.die();
                    return;
                }
            }
        }
        resetHeight();
    }

    public void resetHeight(){
        organs[BODY].pt.x = 0;
        if (legCurrent == 0) organs[BODY].pt.y = 0;
        else organs[BODY].pt.y = -getHeight();
    }

    float getLegStatus(){
        if (legNum == 0) return 1;
        return (float)legCurrent/legNum;
    }

    public boolean hasOrgans(int[] p){
        for (int i : p) {
            if (i < organs.length && organs[i].amputated) return false;
        }
        return true;
    }

    public void setImage(String p){
        for (Organ i : organs){
            if (i.type != 0) i.setImage(p,i.type-1);
        }
    }

    int getHead(){
        for (int i=0;i<organs.length;i++) {
            if (organs[i].vital && !organs[i].amputated) return i;
        }
        return -1;
    }
    int getHand(){
        for (int i=0;i<organs.length;i++) {
            if (organs[i].hand && !organs[i].amputated) return i;
        }
        return -1;
    }
    private int getLeg(){
        for (int i=0;i<organs.length;i++) {
            if (organs[i].leg && !organs[i].amputated) return i;
        }
        return -1;
    }

    public void load(String p){
        for (int i=0;i<p.length();i++){
            organs[i].image.visible = !(organs[i].amputated = p.charAt(i) == '0');
        }
        updateStatus();
    }

    @Override
    public String toString() {
        String r = "";
        for (Organ i : organs){
            r += i.amputated?"0":"1";
        }
        return r;
    }
}
