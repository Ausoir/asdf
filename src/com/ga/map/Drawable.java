package com.ga.map;

import java.awt.*;

/**
 * Created by Ga on 8/12/2016.
 */
public interface Drawable {
    void draw(Graphics2D g);
    int getDepth();
}
