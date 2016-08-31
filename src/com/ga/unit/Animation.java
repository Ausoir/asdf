package com.ga.unit;

import com.ga.game.Texture;
import com.ga.tool.P;
import com.ga.tool.Timer;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Ga on 8/29/2016.
 */
public class Animation extends Thing implements Effect {
    private BufferedImage[] imgs;
    private Timer timer;
    public Animation(String name, P pt, int num) {
        super(null, 0, pt, 12);
        imgs = new BufferedImage[num];
        for (int i=0;i<num;i++){
            imgs[i] = Texture.load("effect/"+name+"_"+i+".png");
        }
        timer = new Timer(num,false);
        setImage(imgs[0]);
    }
    public boolean over(){
        return timer.over();
    }
    @Override
    public void draw(Graphics2D g){
        if (!timer.update()){
            image = imgs[timer.time];
            super.draw(g);
        }
    }
}
