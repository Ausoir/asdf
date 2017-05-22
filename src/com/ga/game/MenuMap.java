package com.ga.game;

import com.ga.map.Global;
import com.ga.map.Picture;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * Created by Ga on 11/15/2016. asd
 */
public class MenuMap extends Panel {
    private JPanel panel;
    private BufferedImage image = Picture.loadCache("map/Worldmap");
    private ImageIcon poi = new ImageIcon(Picture.loadCache("POI"));
    private JLabel[] labels = new JLabel[3];
    private int[] poiCoords = new int[]{
            164,400 , 245,386 , 117,356
    };
    private String[] poiNames = new String[]{
            "Wrar", "Blahville", "Props"
    };
    public MenuMap(){
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setVisible(false);
        panel = new JPanel();
        panel.setLayout(null);
        panel.setOpaque(false);
        setBackground(Panel.forecolor2);
        JLabel label = new JLabel();
        label.setBounds(40,0,200,40);
        label.setText("Select location to enable fast-travel");
        panel.add(label);
        for (int i=0;i<labels.length;i++){
            labels[i] = new JLabel(poiNames[i]);
            labels[i].setLocation(poiCoords[i*2],poiCoords[i*2+1]);
            labels[i].setIcon(poi);
            labels[i].setForeground(Color.WHITE);
            labels[i].setSize(labels[i].getPreferredSize());
            panel.add(labels[i]);
        }
        add(panel, BorderLayout.CENTER);
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
//                System.out.println(e.getX() + "," + (e.getY()-20));
                setVisible(false);
            }
        });
    }
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        g.drawImage(image,40,0, Global.display.menu.size.width, Global.display.menu.size.height,null);
    }
    protected void setColor() {
        setBackground(forecolor);
    }
}