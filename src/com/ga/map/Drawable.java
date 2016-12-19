package com.ga.map;

import com.ga.game.Observer;

import java.awt.*;

/**
 * Created by Ga on 8/12/2016.
 */
public interface Drawable extends Observer {
    void draw(Graphics2D g);
    int getDepth();
}
