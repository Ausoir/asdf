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
    private JTextField chatbox;
    public Input input;
    private int mode = 0;
    private int mX = 0;
    private int mY = 0;
    boolean focus = true;
    private String lastChat = "";
    Display() {
        Global.display = this;
        setLayout(null);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        setDoubleBuffered(true);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Settings.setScreenSize(getWidth(), getHeight());
                Menu.menu.resize();
                chatbox.setBounds(0,Settings.screenY-40,500,40);
            }
        });
        addFocusListener(new FocusListener(){
            @Override
            public void focusGained(FocusEvent e) {
                input.onFocus(true);
            }
            @Override
            public void focusLost(FocusEvent e) {
                input.onFocus(false);
            }
        });
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                mX = e.getX();
                mY = e.getY();
                mouseInput(e.getButton());
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                mX = e.getX();
                mY = e.getY();
                if (Updater.editor) mouseInput(-e.getButton());
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
                if (Updater.editor) mouseInput(0);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mX = e.getX();
                mY = e.getY();
                mouseInput(0);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int notches = e.getWheelRotation();
                Global.scale += notches*.1f;
            }
        };
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                input(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                input(-e.getKeyCode());
            }
        });
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);

        Color chatboxBg = new Color(88,88,0,55);
        Color chatboxFocusBg = new Color(240,240,180,200);
        chatbox = new JTextField();
        chatbox.setFont(new Font("Ariel", Font.PLAIN, 18));
        chatbox.setBounds(0,Settings.screenY-100,500,40);
        chatbox.setFocusable(true);
        chatbox.setBorder(null);
        chatbox.setBackground(chatboxBg);
        chatbox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                String key = input.getKeyMap(e.getKeyCode());
                if (key != null) {
                    switch (key) {
                        case "_SAY":
                            chat();
                            break;
                        case "_UP":
                            chatbox.setText(lastChat);
                            break;
                        case "_EXIT":
                            chatbox.setText("");
                            chat();
                            break;
                    }
                }
            }
        });
        chatbox.addFocusListener(new FocusListener(){
            @Override
            public void focusGained(FocusEvent e) {
                chatbox.setBackground(chatboxFocusBg);
            }
            @Override
            public void focusLost(FocusEvent e) {
                chatbox.setBackground(chatboxBg);
            }
        });
        add(chatbox);
    }

    public void startChat(){
        chatbox.requestFocus();
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
        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF));
        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP));
        if (Global.player.map != null) Global.player.map.updater.draw(g2d);
        g2d.setColor(Color.BLACK);
    }

    private void mouseInput(int mode){
        if (focus) input(mode);
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
