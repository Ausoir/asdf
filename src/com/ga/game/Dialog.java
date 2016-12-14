package com.ga.game;

import com.ga.tool.P;
import com.ga.tool.Performer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Ga on 11/15/2016.
 */
public class Dialog extends JPanel {
    public P size;
    private JPanel panel, optPanel;
    private JScrollPane scroll;
    private JLabel titleLabel;
    private JLabel[] options = new JLabel[0];
    private Performer[] actions;
    private JTextArea message;
    private Image image;
    public Dialog(){
        setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
        image = Texture.load("Shop");
        size = new P(image.getWidth(null), image.getHeight(null));
        resize();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                setVisible(false);
            }
        });
        setBorder(BorderFactory.createMatteBorder(2,2,2,2,Color.LIGHT_GRAY));
        titleLabel = new JLabel();
        panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
        message = new JTextArea();
        message.setOpaque(false);
        message.setFont(new Font("Arial",Font.PLAIN,18));
        message.setMargin(new Insets(10,10,10,10));
        message.setEditable(false);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        panel.add(message);
        optPanel = new JPanel();
        optPanel.setLayout(new BoxLayout(optPanel,BoxLayout.LINE_AXIS));
        optPanel.setOpaque(false);
        scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUI(new ScrollBar());
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(titleLabel, BorderLayout.CENTER);
        add(scroll, BorderLayout.CENTER);
        add(optPanel, BorderLayout.CENTER);
        setVisible(false);
    }
    @Override
    public void paintComponent(Graphics g){
        g.drawImage(image,0,0,null);
    }
    public void show(String title, String message){
        titleLabel.setText(title);
        this.message.setText(message);
        setVisible(true);
    }
    public void show(String title, String message, String[] options, final Performer[] actions){
        show(title, message);
        optPanel.removeAll();
        this.options = new JLabel[options.length];
        for (int i = 0; i < options.length; i++) {
            this.options[i] = new JLabel("   ["+options[i]+"]            ");
            final int ii = i;
            this.options[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    setVisible(false);
                    actions[ii].perform();
                }
            });
            optPanel.add(this.options[i]);
        }
        this.actions = actions;
    }
    public void resize(){
        int x = (int) (Settings.centerX-(size.x/2));
        int y = (int) (Settings.centerY-(size.y/2));
        setBounds(x,y, (int) size.x, (int) size.y);
    }
}