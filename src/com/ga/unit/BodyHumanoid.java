package com.ga.unit;

import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Moveset;
import com.ga.unit.skill.Skills;

/**
 * Created by Ga on 8/1/2016.
 */
public class BodyHumanoid extends Body {
    public static final int FIST = 0, OFIST = 4;
    BodyHumanoid(Unit host) {
        super(host, "Man");
        organs[5].setImage(host.name);
    }

    @Override
    public void setState(Moveset state) {
        if (state instanceof Actions) {
            switch ((Actions) state) {
                case Interact:
                    rotateAppendage(FIST, 15);
                    rotateAppendage(BODY, 15);
                    localAnimation[15][BODY][1] = Math.min(0,localAnimation[15][BODY][1]);
                    break;
                case Cast:
                    rotateAppendage(OFIST, 11);
                    rotateAppendage(FIST, 11);
                    break;
                case Conjure:
                    rotateAppendage(FIST, 20);
                    break;
            }
        } else if (state instanceof Skills) {
            switch ((Skills) state) {
                case Shoot:
                    rotateAppendage(FIST, 39);
                    break;
                case Snipe:
                    rotateAppendage(OFIST, 41);
                    rotateAppendage(OFIST, 42);
                    break;
                case Dart:
                    rotateAppendage(OFIST, 29);
                    break;
                case Underhand:
                    rotateAppendage(FIST, 23);
                    break;
                case Throw:
                case Lunge:
                case Pierce:
                case Swing:
                    rotateAppendage(FIST, 7);
                    break;
                case Kick:
                    rotateAppendage(FOOT-1, 5);
                    break;
                case Sidekick:
                    rotateAppendage(FOOT, 14);
                    break;
                case Stab:
                    rotateAppendage(OFIST, 11);
                    rotateAppendage(FIST, 11);
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
