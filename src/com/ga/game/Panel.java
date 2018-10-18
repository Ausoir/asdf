package com.ga.game;

import com.ga.map.Global;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Ga on 1/22/2017. asd
 */
public class Panel extends JPanel {
    protected Dimension size = new Dimension((int) (Settings.screenX * (3f/4)),512);
    static Color forecolor = new Color(68, 102, 120, 240);
    static Color forecolor2 = new Color(95, 112, 120, 255);
    static Color fontcolor = new Color(205, 209, 227, 255);
    static Color fontcolor2 = new Color(120, 180, 140, 200);
    static Font font = new Font("Helvetica",Font.PLAIN,18);
    static Font smallfont = new Font("Helvetica",Font.PLAIN,16);
    static Font titlefont = new Font("Helvetica",Font.BOLD,18);
    static Font optionfont = new Font("Helvetica", Font.ITALIC, 16);
    int targetSizeX = 512;
    Panel(){
        resize();
    }
    public void display(){
        setVisible(true);
        resize();
        setColor();
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
        Menu.menu.onExit();
    }
}
