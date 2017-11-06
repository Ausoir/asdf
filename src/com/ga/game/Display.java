package com.ga.game;

import com.ga.map.Global;
import com.ga.map.Updater;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by Ga on 5/6/2016. asd
 */
public class Display extends JPanel {
    public JTextField chatbox;
    public Input input;
    public Menu menu;
    private int mode = 0;
    private int mX = 0;
    private int mY = 0;
    boolean focus = true;
    private String lastChat = "";
    Display() {
        Global.display = this;
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        setDoubleBuffered(true);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Settings.setScreenSize(getWidth(), getHeight());
                menu.resize();
                chatbox.setBounds(0,Settings.screenY-40,500,40);
            }
        });
        MouseAdapter mouseAdapter = new MouseAdapter() {
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
                mX = e.getX();
                mY = e.getY();
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
        };
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keycode = e.getKeyCode();
                if (keycode == Keybinding.get("ENTER")) chatbox.requestFocus();
                else if (keycode == Keybinding.get("TAB")) menu.showMenu();
                else input(keycode);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                input(-e.getKeyCode());
            }
        });
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);

        chatbox = new JTextField();
        chatbox.setFont(new Font("Ariel", Font.PLAIN, 18));
        chatbox.setBounds(0,Settings.screenY-100,500,40);
        chatbox.setFocusable(true);
        chatbox.setBorder(null);
        chatbox.setBackground(new Color(88,88,0,55));
        chatbox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == Keybinding.get("ENTER")) chat();
                else if (e.getKeyCode() == Keybinding.get("UP")) chatbox.setText(lastChat);
            }
        });
        add(chatbox);
        menu = new Menu();
        add(menu);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Settings.screenX, Settings.screenY);
    }

    public void removeSpeech(){
        for (Component i : getComponents()){
            if (i instanceof JTextArea) remove(i);
        }
    }

    private void chat(){
        String text = chatbox.getText();
        requestFocus();
        chatbox.setText("");
        input.chat(text, (int) (mX-Settings.centerX+Global.camera.x), (int) (mY-Settings.centerY+Global.camera.y));
        lastChat = text;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        Global.player.map.updater.draw(g2d);
        g2d.setColor(Color.BLACK);
        g2d.fillRect(mX, mY, 12, 12);
    }

    private void input(int mode){
        if (this.mode != mode || mode <= 0 || Updater.editor) {
            this.mode = mode;
            if (input != null) {
                input.input(mode, (int) (mX - Settings.centerX + Global.camera.x), (int) (mY - Settings.centerY + Global.camera.y));
            }
        }
    }
}
