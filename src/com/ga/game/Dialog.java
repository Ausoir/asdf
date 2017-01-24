package com.ga.game;

import com.ga.tool.Performer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Ga on 11/15/2016.
 */
public class Dialog extends Panel {
    private JPanel panel, optPanel;
    private JScrollPane scroll;
    private JLabel titleLabel;
    private JLabel[] options = new JLabel[0];
    private Performer[] actions;
    private JTextArea message;
    public Dialog(){
        setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                setVisible(false);
            }
        });
        titleLabel = new JLabel();
        titleLabel.setFont(new Font("Helvetica",Font.BOLD,18));
        titleLabel.setForeground(new Color(200, 200, 200,200));
        panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
        message = new JTextArea();
        message.setOpaque(false);
        message.setFont(Panel.font);
        message.setForeground(Panel.fontcolor);
        message.setMargin(new Insets(10,10,10,10));
        message.setEditable(false);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                setVisible(false);
            }
        });
        panel.add(message);
        optPanel = new JPanel();
        optPanel.setLayout(new BoxLayout(optPanel,BoxLayout.LINE_AXIS));
        optPanel.setOpaque(false);
        scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUI(new ScrollBar());
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        add(titleLabel, BorderLayout.CENTER);
        add(scroll, BorderLayout.CENTER);
        add(optPanel, BorderLayout.CENTER);
    }
    protected void setColor() {
        setBackground(forecolor);
        scroll.setBackground(forecolor2);
    }
    public void show(String title, String message){
        display();
        setBackground(new Color(120,120,120,180));
        scroll.setBackground(new Color(120,120,120,180));
        titleLabel.setText(title);
        this.message.setText(message);
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
}