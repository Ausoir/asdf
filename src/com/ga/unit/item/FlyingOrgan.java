package com.ga.unit.item;

import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.unit.Matter;
import com.ga.unit.Organ;
import com.ga.unit.Particle;

import java.awt.*;

/**
 * Created by Ga on 8/12/2016.
 */
public class FlyingOrgan extends Projectile {
    private Organ organ;
    private float length;
    private boolean flying = true;
    public FlyingOrgan(Organ p, P dest, int speed) {
        super(null, new P(p.host.pos), dest, speed);
        organ = p;
        length = organ.getLength()*.4f;
        organ.ptd = new P();
        organ.pt = new P();
        tangible = false;
    }
    @Override
    public void stuck(){
        super.stuck();
        flying = false;
    }
    @Override
    protected void onAttack(Matter p){
    }
    @Override
    public void update(){
        if (flying) {
            Particle.explode("Blood", pos.getAdd(organ.pt), new P(0,-15), 4, 5, 5, true, 5);
            organ.pt.set(Global.cos(rotation)*-length,Global.sin(rotation)*-length);
            organ.ptd.set(Global.cos(rotation)*length,Global.sin(rotation)*length);
        }
        super.update();
    }
    @Override
    public void draw(Graphics2D g){
    }
}
