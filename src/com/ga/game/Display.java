package com.ga.game;

import com.ga.map.Global;
import com.ga.map.Weather;
import com.ga.unit.Updater;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;

/**
 * Created by Ga on 5/6/2016.
 */
public class Display extends JPanel implements KeyListener, MouseListener, MouseMotionListener {
    public static JTextField chatbox;
    public static Input input;
    public static Dialog dialog;
    public static Menu menu;
    public static Shop shop;
    private int mode = 0;
    private int mX = 0;
    private int mY = 0;
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
                dialog.resize();
                menu.resize();
                shop.resize();
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
                if (e.getKeyCode() == KeyEvent.VK_ENTER) chat();
            }
        });
        add(chatbox);
        dialog = new Dialog();
        add(dialog);
        Updater.add(dialog);
        menu = new Menu();
        add(menu);
        Updater.add(menu);
        shop = new Shop();
        add(shop);
        Updater.add(shop);
        Global.weather = new Weather();
        new Timer(25, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                repaint();
            }
        }).start();
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
        if (this.mode != mode || mode < 0 || Updater.editor) {
            this.mode = mode;
            if (input != null)
                input.input(mode, (int) (mX-Settings.centerX+Global.camera.x), (int) (mY-Settings.centerY+Global.camera.y));
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
        if (t == KeyEvent.VK_UP || t == KeyEvent.VK_LEFT || t == KeyEvent.VK_DOWN || t == KeyEvent.VK_RIGHT ||
            t == KeyEvent.VK_A || t == KeyEvent.VK_D)
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
        if (Updater.editor) input(0);
    }
}
