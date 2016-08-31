package com.ga.unit;

import com.ga.game.Texture;
import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.tool.Timer;

/**
 * Created by Ga on 8/29/2016.
 */
public class Particle extends Unit implements Effect {
    private Timer timer = new Timer(4, false);
    public Particle(P pt, P vel, float rotation, float speed, boolean gravity) {
        super(null, 0, pt, 13);
        setImage(Texture.load("effect/Particle.png"));
        this.vel.set(Global.cos(rotation)*speed,Global.sin(rotation)*speed);
        this.vel.add(vel);
        this.gravity = gravity;
        this.rotation = rotation;
        tangible = false;
    }
    public static void explode(P pt, P vel, int num, int speed, boolean gravity){
        for (int i=0;i<num;i++){
            new Particle(new P(pt),new P(vel), Global.r.nextFloat()*6.28f,Global.r.nextFloat()*speed, gravity);
        }
    }

    @Override
    public boolean over() {
        return timer.update();
    }
}
