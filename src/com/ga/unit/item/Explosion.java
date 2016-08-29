package com.ga.unit.item;

import com.ga.tool.P;
import com.ga.tool.Performer;

/**
 * Created by Ga on 8/27/2016.
 */
public class Explosion extends Projectile {
    public Explosion(String name, P pt, P dest, int speed) {
        super(name, pt, new P(), 0);
        delay.set(50);
        delay.performer = new Performer(){
            @Override
            public void perform(){}
        };
    }
}
