package com.ga.map;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Ga on 3/4/2017.
 */
public class ShadedPicture extends Picture {
    protected Shader shader;
    protected BufferedImage shadow;

    public ShadedPicture(String p, Shader shader) {
        super(p);
        setShader(shader);
    }

    public ShadedPicture(String p, int index, Shader shader) {
        super(p, index);
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