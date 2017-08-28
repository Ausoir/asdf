package com.ga.game;

import com.ga.map.Global;
import com.ga.map.Updater;
import com.ga.unit.Speech;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by Ga on 5/6/2016. asd
 */
public class Display extends JPanel implements KeyListener, MouseListener, MouseMotionListener {
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
        setLayout(null);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Settings.setScreenSize(getWidth(), getHeight());
                menu.resize();
                chatbox.setBounds(0,Settings.screenY-40,500,40);
            }
        });
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        setDoubleBuffered(true);

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
        lastChat = text;
    }

    public void paintComponent(Graphics g) {
        Global.player.map.updater.draw((Graphics2D) g.create());
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
        input(-e.getKeyCode());
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
        mX = e.getX();
        mY = e.getY();
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
