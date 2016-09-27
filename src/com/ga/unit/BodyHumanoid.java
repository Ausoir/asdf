package com.ga.unit;

import com.ga.tool.Clips;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Moveset;
import com.ga.unit.skill.Skills;

/**
 * Created by Ga on 8/1/2016.
 */
public class BodyHumanoid extends Body {
    public static final int FIST = 0, OFIST = 4, FOOT = 2;
    private int height = 0;

    BodyHumanoid(Unit host) {
        super(host, "Man");
        organs[5].setImage("unit/"+host.name);
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
            localAnimation[i][0][1] += height;
            localAnimation[i][1][1] += height;
            localAnimation[i][2][1] += height;
            localAnimation[i][3][1] += height;
            localAnimation[i][4][1] += height;
        }
    }

    @Override
    public void setState(Moveset state) {
        if (state instanceof Actions) {
            switch ((Actions) state) {
                case Shoot:
                    rotateAppendage(OFIST, 28);
                    break;
                case Cast:
                    rotateAppendage(OFIST, 10);
                    rotateAppendage(FIST, 10);
                    break;
                case Conjure:
                    rotateAppendage(FIST, 19);
                    break;
            }
        } else if (state instanceof Skills) {
            switch ((Skills) state) {
                case Throw:
                    rotateAppendage(FIST, 6);
                    break;
                case Riposte:
                    rotateAppendage(FIST, 22);
                    break;
                case Lunge:
                case Pierce:
                    rotateAppendage(FIST, 6);
                    break;
                case Kick:
                    rotateAppendage(FOOT, 13);
                    break;
                case Stab:
                    rotateAppendage(OFIST, 10);
                    rotateAppendage(FIST, 10);
                    break;
            }
        }
        super.setState(state);
    }

    public void update() {
        if (state instanceof Actions) {
            switch ((Actions) state) {
                case Buff:
                case Cast:
                    Particle.dripFire(host.pos.getAdd(organs[OFIST].ptd));
                case Conjure:
                    Particle.dripFire(host.pos.getAdd(organs[FIST].ptd));
                    break;
            }
        }
        super.update();
    }
}
