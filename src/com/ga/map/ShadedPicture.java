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

    public ShadedPicture(Random rand, String p, Shader shader) {
        super(rand);
        set(p);
        setShader(shader);
    }

    public ShadedPicture(Random rand, String p, int index, Shader shader) {
        super(rand);
        set(p, index);
        setShader(shader);
    }

    public void setShader(Shader p) {
        shader = p;
    }

    public void shade(int p) {
        shadow = shader.render(image, p);
    }

    protected void drawImage(Graphics2D g, int x, int y, int sizeX, int sizeY) {
        super.drawImage(g,x,y,sizeX,sizeY);
        g.drawImage(shadow, x, y, sizeX, sizeY, null);
    }
}