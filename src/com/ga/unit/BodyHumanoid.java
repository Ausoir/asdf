package com.ga.unit;

import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Moveset;
import com.ga.unit.skill.Skills;

/**
 * Created by Ga on 8/1/2016.
 */
public class BodyHumanoid extends Body {
    public static final int FIST = 0, OFIST = 4;
    private int height = 0;

    BodyHumanoid(Unit host) {
        super(host, "Man");
        organs[5].setImage(host.name);
    }

    @Override
    public void setState(Moveset state) {
        if (state instanceof Actions) {
            switch ((Actions) state) {
                case Interact:
                    rotateAppendage(FIST, 14);
                    rotateAppendage(BODY, 14);
                    localAnimation[14][BODY][1] = Math.min(0,localAnimation[14][BODY][1]);
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
                case Shoot:
                    rotateAppendage(FIST, 38);
                    break;
                case Snipe:
                    rotateAppendage(OFIST, 40);
                    break;
                case Dart:
                    rotateAppendage(OFIST, 28);
                    break;
                case Underhand:
                    rotateAppendage(FIST, 22);
                    break;
                case Throw:
                case Lunge:
                case Pierce:
                    rotateAppendage(FIST, 6);
                    break;
                case Kick:
                    rotateAppendage(FOOT-1, 4);
                    break;
                case Sidekick:
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
