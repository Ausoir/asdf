package com.ga.game;

import com.ga.map.*;
import com.ga.tool.Clips;
import com.ga.unit.Stock;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl","True"); //Causes JPanel rendering to title bar
        boolean editor = false;
        int x = 0, y = 0;
        for (String i : args){
            if (i.equals("-editor")) editor = true;
            if (i.startsWith("-screenX")) x = Integer.parseInt(i.substring(i.indexOf('=')+1));
            if (i.startsWith("-screenY")) y = Integer.parseInt(i.substring(i.indexOf('=')+1));
        }
        Settings.init();
        Global.init();
        JFrame gamewindow = new JFrame();
//        gamewindow.setUndecorated(true);
        gamewindow.setVisible(true);
        gamewindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        FileManager.init();
        Generator.init();
        Dialog.init();
        Clips.init();
        Picture.init();
        Sound.init();
        Stock.init();
        Display display = new Display();
        Creation.create();
        if (x == 0 && y == 0) {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            x = screen.width-2;
            y = screen.height-36;
            gamewindow.setExtendedState( gamewindow.getExtendedState()|JFrame.MAXIMIZED_BOTH );
        } else {
            display.setPreferredSize(new Dimension(x, y));
        }
        Settings.setScreenSize(x, y);
        gamewindow.setContentPane(display);
        gamewindow.pack();
        gamewindow.setLocationRelativeTo(null);
        Global.cprocess = new Processes();
        if (editor) new EditorInput();
        display.requestFocus();
    }
}
