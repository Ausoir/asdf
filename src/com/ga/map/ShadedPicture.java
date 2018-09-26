package com.ga.map;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Created by Ga on 3/4/2017. aaa
 */
public class ShadedPicture extends Picture {
    protected Shader shader;
    private BufferedImage shadow;

    public ShadedPicture(Random rand, BufferedImage p, Shader shader) {
        super(rand);
        set(p);
        setShader(shader);
    }

    public void setShader(Shader p) {
        shader = p;
    }

    public void shade(int p) {
        if (image != null) {
            shadow = shader.render(image, p);
            if (shadow != null) shadow = Picture.toCompatibleImage(shadow);
        }
    }

    protected void drawImage(Graphics2D g, int x, int y, int sizeX, int sizeY) {
        super.drawImage(g,x,y,sizeX,sizeY);
        g.drawImage(shadow, x, y, sizeX, sizeY, null);
    }
}