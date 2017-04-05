package com.ga.game;

import com.ga.map.Global;
import com.ga.unit.Updater;
import com.ga.unit.intelligence.Speech;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by Ga on 5/6/2016. asd
 */
public class Display extends JPanel implements KeyListener, MouseListener, MouseMotionListener {
    public JTextField chatbox;
    public Input input;
    public MenuDialog menuDialog;
    public Menu menu;
    public MenuShop menuShop;
    public MenuMap menuMap;
    private int mode = 0;
    private int mX = 0;
    private int mY = 0;
    boolean focus = true;
    public Display() {
        Global.display = this;
        setLayout(null);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Settings.setScreenSize(getWidth(), getHeight());
                menuDialog.resize();
                menu.resize();
                menuShop.resize();
                menuMap.resize();
                chatbox.setBounds(0,Settings.screenY-40,500,40);
            }
        });
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        setDoubleBuffered(true);

        chatbox = new JTextField();
        chatbox.setFont(new Font("Ariel", Font.PLAIN, 18));
        chatbox.setOpaque(false);
        chatbox.setBounds(0,Settings.screenY-100,500,40);
        chatbox.setFocusable(true);
        chatbox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e){
                if (chatbox.getText().length() > 256) e.consume();
            }
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == Keybinding.get("ENTER")) chat();
            }
        });
        add(chatbox);
        menuDialog = new MenuDialog();
        add(menuDialog);
        menu = new Menu();
        add(menu);
        menuShop = new MenuShop();
        add(menuShop);
        menuMap = new MenuMap();
        add(menuMap);
        new Timer(25, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                repaint();
            }
        }).start();
        Updater.addUpdatee(menu);
        Updater.addUpdatee(menuDialog);
        Updater.addUpdatee(menuShop);
        Updater.addUpdatee(menuMap);
    }

    public void removeSpeech(){
        for (Component i : getComponents()){
            if (i instanceof Speech) remove(i);
        }
    }

    private void chat(){
        String text = chatbox.getText();
        requestFocus();
        chatbox.setText("");
        input.chat(text, (int) (mX-Settings.centerX+Global.camera.x), (int) (mY-Settings.centerY+Global.camera.y));
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();//.getDrawGraphics();
        Updater.draw(g2);
    }

    private void input(int mode){
        if (focus) {
            if (this.mode != mode || mode <= 0 || Updater.editor) {
                this.mode = mode;
                if (input != null)
                    input.input(mode, (int) (mX - Settings.centerX + Global.camera.x), (int) (mY - Settings.centerY + Global.camera.y));
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        input(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int t = e.getKeyCode();
        if (t == Keybinding.get("UP") || t == Keybinding.get("LEFT") || t == Keybinding.get("DOWN") || t == Keybinding.get("RIGHT") ||
            t == Keybinding.get("A") || t == Keybinding.get("D"))
            input(-1);
        else if (Updater.editor) input(-t);
        if (t == mode) mode = -1;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(java.awt.event.MouseEvent e) {
        mX = e.getX();
        mY = e.getY();
        input(e.getButton());
    }

    @Override
    public void mouseReleased(java.awt.event.MouseEvent e) {
        mX = e.getX();
        mY = e.getY();
        if (Updater.editor) input(-e.getButton());
        else if (mode == e.getButton()) mode = -1;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mX = e.getX();
        mY = e.getY();
        if (Updater.editor) input(0);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mX = e.getX();
        mY = e.getY();
        input(0);
    }
}
