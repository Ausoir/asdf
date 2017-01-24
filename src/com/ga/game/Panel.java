package com.ga.game;

import com.ga.map.Updatable;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Ga on 1/22/2017.
 */
public class Panel extends JPanel implements Updatable {
    protected Point location = new Point(4,4);
    protected Dimension size = new Dimension(512,512);
    protected static Color forecolor = new Color(68, 102, 120, 240);
    protected static Color forecolor2 = new Color(95, 112, 120, 255);
    protected static Color fontcolor = new Color(205, 209, 227, 255);
    protected static Font font = new Font("Helvetica",Font.PLAIN,18);
    protected int targetSizeX = 512;
    protected int targetSizeY = 512;
    protected int animation = 1;
    public Panel(){
        resize();
        setVisible(false);
        location = getLocation();
    }
    public void display(){
        setVisible(true);
        animation = 0;
        resize();
        setSize(1, size.height);
        setLocation(Settings.centerX, Settings.centerY);
    }
    protected void setColor(){
    }
    public void resize(){
        int x = Settings.centerX-(size.width/2);
        int y = Settings.centerY-(size.height/2);
        setBounds(x,y, size.width, size.height);
    }
    @Override
    public void update() {
        if (isVisible()) {
            if (animation < targetSizeX && animation != -1){
                setSize(animation += 128, size.height);
                setLocation(Settings.centerX-(animation/2), (int) (Settings.centerY-(((float)animation/targetSizeX)*targetSizeY*.5f)));
            } else if (animation >= targetSizeX){
                setColor();
                animation = -1;
                revalidate();
            }
        }
    }
}
