package com.ga.game;

import com.ga.map.Global;
import com.ga.unit.Updater;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Ga on 8/6/2016.
 */
public class Game extends JFrame {
    public Game(boolean editor, int x, int y){
        Global.init();
        FileManager.readMap("Map",0);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        if (x == 0 && y == 0) {
            x = screen.width;
            y = screen.height;
        }
        Settings.setScreenSize(x, y);
        setSize(Settings.screenX, Settings.screenY);
        JPanel display;
//        Updater.showTerrain = true;
        if (!editor) display = new Display(Global.player);
        else{
            display = new Display(new EditorInput());
            Updater.editor = true;
        }
        add(display);
        setVisible(true);
        setExtendedState( getExtendedState()|JFrame.MAXIMIZED_BOTH );
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
