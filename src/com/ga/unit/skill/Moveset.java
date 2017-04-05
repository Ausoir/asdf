package com.ga.unit.skill;

import com.ga.unit.Tags;
import com.ga.unit.Unit;

/**
 * Created by Ga on 8/30/2016.
 */
public interface Moveset {
    String getName();
    String getCodeName();
    String getDescription();

    float getCost();

    int getDel();

    int getCooldown();
    float getVelX();

    void trigger(Unit p, int organ);
    void onSet(Unit p, int level);
    void onFinish(Unit p);
    void onInterrupt(Unit p);
    Tags getTags();

    void update(Unit p, int level);
}
