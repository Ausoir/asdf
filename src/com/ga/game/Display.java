package com.ga.game;

import com.ga.map.Global;
import com.ga.unit.Updater;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by Ga on 5/6/2016.
 */
public class Display extends JPanel implements KeyListener, MouseListener, MouseMotionListener {
    private int mode = 0;
    public JTextField chatbox;
    public static Input input;
    public static Menu menu = new Menu();
    public Display(Input input) {
        setLayout(null);
        this.input = input;
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        setDoubleBuffered(true);

        chatbox = new JTextField();
        chatbox.setFont(new Font("Ariel", Font.PLAIN, 18));
        chatbox.setOpaque(false);
        chatbox.setBounds(0,Settings.screenY-100,500,40);
        chatbox.setFocusable(true);
        chatbox.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) chat();
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        add(chatbox);
        add(menu);
    }

    private void chat(){
        String text = chatbox.getText();
        requestFocus();
        chatbox.setText("");
        input.chat(text);
    }

    private void enter(){
        chatbox.requestFocus();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();//.getDrawGraphics();
        Updater.draw(g2);
        repaint();
        try {
            Thread.sleep(30);
        } catch (Exception e) {
            System.out.println("Thread Error");
            System.exit(1);
        }
        Global.update();
    }

    private void input(boolean key, int mode, int x, int y){
        if (mode == 0 || this.mode != mode) {
            if (mode != 0) this.mode = mode;
            if (input != null)
                input.input(key, mode, x, y);
            if (mode == KeyEvent.VK_ENTER) enter();
            else if (mode == KeyEvent.VK_TAB) menu.toggle();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        input(true,e.getKeyCode(),0,0);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        input(true,-e.getKeyCode(),0,0);
    }

    @Override
    public void mouseClicked(java.awt.event.MouseEvent e) {
    }

    @Override
    public void mousePressed(java.awt.event.MouseEvent e) {
        input(false,e.getButton(),e.getX(),e.getY());
    }

    @Override
    public void mouseReleased(java.awt.event.MouseEvent e) {
        input(false,-e.getButton(),e.getX(),e.getY());
    }

    @Override
    public void mouseEntered(java.awt.event.MouseEvent e) {

    }

    @Override
    public void mouseExited(java.awt.event.MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        input(false,0,e.getX(),e.getY());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        input(false,0,e.getX(),e.getY());
    }
}
