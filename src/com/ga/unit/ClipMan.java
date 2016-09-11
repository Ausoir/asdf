package com.ga.unit;

import com.ga.map.Global;
import com.ga.tool.Clips;
import com.ga.tool.P;

import java.awt.*;

/**
 * Created by Ga on 8/1/2016.
 */
public class ClipMan extends ClipUnit{
    public static final int FIST = 0, OFIST = 4;
    private int height = 0;

    ClipMan(Unit host) {
        super(host, "Man");
    }

    protected void resetLocalAnimation(int p){
        localAnimation[p] = Clips.Man.get().clip[p];
        localAnimation[p][3][1] -= height;
        localAnimation[p][2][1] += height;
        localAnimation[p][1][1] += height;
        localAnimation[p][0][1] += height / 2;
        localAnimation[p][4][1] += height / 2;
    }

    @Override
    protected void randomize() {
        super.randomize();
        height = (int) Math.max(Math.min(host.rand.nextGaussian() * 5, 15), -15);
        for (int i = 0; i < localAnimation.length; i++) {
            localAnimation[i][3][1] -= height;
            localAnimation[i][0][1] += height;
            localAnimation[i][1][1] += height;
            localAnimation[i][2][1] += height;
            localAnimation[i][4][1] += height;
        }
    }

    public void setState(Actions state) {
        switch (state){
            case Shoot:
                rotateAppendage(OFIST,28);
                break;
            case Throw:
                rotateAppendage(FIST,6);
                break;
            case Riposte:
                rotateAppendage(FIST,22);
                break;
            case Lunge:
            case Pierce:
                rotateAppendage(FIST,6);
                break;
            case Kick:
                rotateAppendage(FOOT,13);
                break;
            case Stab:
            case Cast:
                rotateAppendage(OFIST,10);
                rotateAppendage(FIST,10);
                break;
            case Conjure:
                rotateAppendage(FIST,19);
                break;
        }
        super.setState(state);
    }
    private void rotateAppendage(int organ, int frame){
        int length = Math.abs(Clips.Man.get().clip[frame][organ][0])+Math.abs(Clips.Man.get().clip[frame][organ][1]);
        P atanDir = host.target.getAdd(localAnimation[frame][organ][0]*host.dir,localAnimation[frame][organ][1]).getAtan(1);
        localAnimation[frame][organ][0] = (int) (length*atanDir.x)*host.dir;
        localAnimation[frame][organ][1] = (int) (length*atanDir.y);
    }

    public void update() {
        switch (state){
            case Buff:
            case Cast:
                Particle.dripFire(host.pos.getAdd(organs[OFIST].ptd));
            case Conjure:
                Particle.dripFire(host.pos.getAdd(organs[FIST].ptd));
                break;
        }
        super.update();
    }
}
