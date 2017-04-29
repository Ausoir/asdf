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

    int getCost();

    int getDel();

    int getCooldown();
    float getVelX();
    int getDifficulty();

    void trigger(Unit p);
    void onSet(Unit p);
    void onFinish(Unit p);
    void onInterrupt(Unit p);
    Tags getTags();

    void update(Unit p);
}
