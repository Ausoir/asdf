package com.ga.game;

import com.ga.map.*;
import com.ga.tool.Clips;
import com.ga.unit.Stock;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created by Ga on 8/6/2016. asd
 */
class Game {
    Game(boolean editor, int x, int y){
        Settings.init();
        Keybinding.init();
        Global.init();
        Updater.editor = editor;
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        JFrame gamewindow = new JFrame();
        gamewindow.setVisible(true);
        gamewindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        FileManager.init();
        Dialog.init();
        Clips.init();
        Picture.init();
        Sound.init();
        Stock.init();
        Display display = new Display();
        gamewindow.setContentPane(display);
        Creation.create();
        if (x == 0 && y == 0) {
            x = screen.width;
            y = screen.height;
            gamewindow.setExtendedState( gamewindow.getExtendedState()|JFrame.MAXIMIZED_BOTH );
        } else {
            gamewindow.getContentPane().setPreferredSize(new Dimension(x, y));
            gamewindow.pack();
        }
        Settings.setScreenSize(x, y);
        Global.cprocess = new Processes();
        if (editor) new EditorInput();
        display.requestFocus();
    }
}
