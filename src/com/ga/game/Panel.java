package com.ga.game;

import com.ga.map.Global;
import com.ga.map.Updatable;
import com.ga.tool.Performer;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Ga on 1/22/2017. asd
 */
public class Panel extends JPanel implements Updatable {
    protected Dimension size = new Dimension(512,512);
    static Color forecolor = new Color(68, 102, 120, 240);
    static Color forecolor2 = new Color(95, 112, 120, 255);
    static Color fontcolor = new Color(205, 209, 227, 255);
    static Font font = new Font("Helvetica",Font.PLAIN,18);
    static Font smallfont = new Font("Helvetica",Font.PLAIN,16);
    int targetSizeX = 512;
    private int animation = 1;
    Panel(){
        resize();
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
    void resize(){
        int x = Settings.centerX-(size.width/2);
        int y = Settings.centerY-(size.height/2);
        setBounds(x,y, size.width, size.height);
    }
    public void setVisible(boolean p){
        Global.display.focus = !p;
        super.setVisible(p);
        Global.display.menu.onExit();
    }
    @Override
    public void update() {
        if (isVisible()) {
            if (animation < targetSizeX && animation != -1){
                setSize(animation += 128, size.height);
                int targetSizeY = 512;
                setLocation(Settings.centerX-(animation/2),
                        (int) (Settings.centerY-(((float)animation/targetSizeX)* targetSizeY *.5f)));
            } else if (animation >= targetSizeX){
                setColor();
                animation = -1;
                revalidate();
            }
        }
    }
}
