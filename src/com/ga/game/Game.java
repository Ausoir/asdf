package com.ga.game;

import com.ga.connection.Server;
import com.ga.map.Global;
import com.ga.tool.Clips;
import com.ga.unit.Updater;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Ga on 8/6/2016.
 */
public class Game extends JFrame {
    public Game(boolean editor, int x, int y){
        Global.init();
        Updater.editor = editor;
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        if (x == 0 && y == 0) {
            x = screen.width;
            y = screen.height;
        }
        Settings.setScreenSize(x, y);
        setSize(Settings.screenX, Settings.screenY);
        JPanel display = new Display();
        Clips.init();
        Texture.init();
        Global.server = new Server();
        Global.server.start();
        FileManager.loadFirst(editor);
        add(display);
        setVisible(true);
        setExtendedState( getExtendedState()|JFrame.MAXIMIZED_BOTH );
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        new Process();
    }
}
