package com.ga.unit.skill;

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

    void onSet(Unit p);
    void onFinish(Unit p);
    void onInterrupt(Unit p);

    void update(Unit p, int level);
}
