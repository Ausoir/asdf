package com.ga.game;

import com.ga.map.*;
import com.ga.tool.Clips;
import com.ga.unit.Stock;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Ga on 8/6/2016. asd
 */
class Game extends JFrame {
    Game(boolean editor, int x, int y){
        Settings.init();
        Keybinding.init();
        Global.init();
        Updater.editor = editor;
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        if (x == 0 && y == 0) {
            x = screen.width;
            y = screen.height;
            setExtendedState( getExtendedState()|JFrame.MAXIMIZED_BOTH );
        }
        Settings.setScreenSize(x, y);
        setSize(Settings.screenX, Settings.screenY);
        FileManager.init();
        Dialog.init();
        Clips.init();
        Picture.init();
        Sound.init();
        Stock.init();
        Display display = new Display();
        Creation.create();
        Global.cprocess = new Processes();
        if (editor) new EditorInput();
        add(display);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
