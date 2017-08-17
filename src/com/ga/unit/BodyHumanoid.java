package com.ga.unit;

import com.ga.map.Global;
import com.ga.unit.skill.Actions;

/**
 * Created by Ga on 8/1/2016. asd
 */
public class BodyHumanoid extends Body {
    public static final int FIST = 0, LLEG = 1, RLEG = 2, BODY = 3, OFIST = 4, HEAD = 5;
    BodyHumanoid(Unit host) {
        super(host, "Man");
    }

    private void setAppendages(){
        switch (this.state) {
            case "Interact":
                rotateAppendage(FIST, 15, 0);
                localAnimation[15][OFIST][0] = localAnimation[1][OFIST][0];
                localAnimation[15][OFIST][1] = localAnimation[1][OFIST][1];
                rotateAppendage(BODY, 15, 0);
                localAnimation[15][BODY][0] = (int) Global.clamp(localAnimation[15][BODY][0],-15,15);
                localAnimation[15][BODY][1] = Math.min(5,localAnimation[15][BODY][1]);
                break;
            case "Grab":
                rotateAppendage(FIST, 15, 0);
                rotateAppendage(OFIST, 15, 0);
                rotateAppendage(BODY, 15, 0);
                localAnimation[15][BODY][0] = (int) Global.clamp(localAnimation[15][BODY][0],-15,15);
                localAnimation[15][BODY][1] = Math.min(5,localAnimation[15][BODY][1]);
                break;
            case "Cast":
                rotateAppendage(OFIST, 11, 0);
                rotateAppendage(FIST, 11, 0);
                break;
            case "Conjure":
                rotateAppendage(FIST, 20, 0);
                break;
            case "Aim":
                rotateAppendage(HEAD, 47, -1.6f*host.dir);
                rotateAppendage(FIST, 48, 0);
                rotateAppendage(OFIST, 48, 0);
                break;
            case "Shoot":
                rotateAppendage(FIST, 39, 0);
                break;
            case "Snipe":
                rotateAppendage(OFIST, 41, 0);
                rotateAppendage(OFIST, 42, 0);
                break;
            case "Dart":
                rotateAppendage(OFIST, 29, 0);
                break;
            case "Underhand":
                rotateAppendage(FIST, 23, 0);
                break;
            case "Thrust":
                rotateAppendage(FIST, 7, 0);
                break;
            case "Slash":
                rotateAppendage(FIST, 7, host.dir);
                break;
            case "Kick":
                rotateAppendage(LLEG, 5, 0);
                break;
            case "Sidekick":
                rotateAppendage(RLEG, 14, 0);
                break;
            case "Stab":
                rotateAppendage(OFIST, 11, 0);
                rotateAppendage(FIST, 11, 0);
                break;
        }
    }

    @Override
    public void setState(String state) {
        super.setState(state);
        setAppendages();
    }

    public boolean update() {
        if (host.state == Actions.Walk)
            if ((current == 0 || current == 2) && frame.over()) ((Man)host).stepSound();
        else if (host.state == Actions.Sprint)
            if ((current == 1 || current == 3) && frame.over()) ((Man)host).stepSound();
        return super.update();
    }
}
