package com.ga.game;

import com.ga.map.Global;
import com.ga.map.Picture;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.unit.Player;
import com.ga.unit.item.Item;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by Ga on 11/15/2016.
 */
public class Shop extends Panel {
    private GridBagConstraints grid = new GridBagConstraints();
    private JPanel panel;
    private JScrollPane scroll;
    private JLabel gold, titleLabel;
    private Border emptyborder = new EmptyBorder(5,8,5,8);
    private boolean mRelease = true;
    private int prevY = 0;
    private Image image;
    private Item[] wares;
    private int[] prices;
    private String title;
    private Performer[] actions;

    public Shop(){
        super();
        setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int y = e.getY();
                if (mRelease) mRelease = false;
                else panel.setLocation(0, Math.min(panel.getY() + (y - prevY),0));
                prevY = y;
            }
        });
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int notches = e.getWheelRotation();
                if (notches < 0) panel.setLocation(0, Math.min(panel.getY() - 30,0));
                else if (notches > 0) panel.setLocation(0, Math.min(panel.getY() + 30,0));
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == 2){
                    panel.setLocation(0, 0);
                }
            }
            @Override
            public void mouseReleased(MouseEvent e){
                mRelease = true;
            }
        });
        titleLabel = new JLabel();
        gold = new JLabel();
        panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
        scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUI(new ScrollBar());
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setOpaque(false);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        add(titleLabel, BorderLayout.CENTER);
        add(scroll, BorderLayout.CENTER);
        add(gold, BorderLayout.LINE_START);
    }
    protected void setColor() {
        setBackground(new Color(68, 95, 120, 180));
        scroll.setBackground(new Color(120, 86, 47, 180));
    }
    public void show(String title, Item[] wares, int[] prices, Performer[] actions){
        if (!isVisible()) {
            super.display();
            setBackground(new Color(120,120,120,180));
            scroll.setBackground(new Color(120,120,120,180));
            this.title = title;
            this.wares = wares;
            this.prices = prices;
            this.actions = actions;

            titleLabel.setText(title);
            gold.setText("Gold: " + Global.player.host.gold);
            panel.removeAll();
            int itemsize = wares.length;
            for (int i = 0; i < itemsize; i++) {
                if (wares[i] != null) {
                    grid.gridy = i + 2;
                    makeSlot(wares[i], prices[i], actions[i]);
                }
            }
        }
    }
    public void makeSlot(final Item item, final int price, final Performer action){
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
        panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        final JLabel result = new JLabel();
        result.setIcon(new ImageIcon(item.image.getImage()));
        result.setHorizontalTextPosition(JLabel.CENTER);
        result.setVerticalTextPosition(JLabel.TOP);
        result.setBorder(emptyborder);
        result.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                describe(item, price, action);
            }
        });
        JPanel options = new JPanel();
        options.setOpaque(false);
        options.setLayout(new BoxLayout(options,BoxLayout.LINE_AXIS));
        final JLabel drop = new JLabel(item.getFullName()+" - "+price);
        drop.setIcon(new ImageIcon(Picture.load("Buy")));
        drop.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                describe(item, price, action);
            }
        });
        drop.setForeground(item.getRarityColor());
        options.add(drop);
        panel.add(options);
        panel.add(result);
        this.panel.add(panel);
    }
    private void describe(final Item item, final int price, final Performer action){
        Display.dialog.show(item.getFullName(),"This is "+item.getDescription()+". It is being sold for "+price+" gold.",
                new String[]{"Buy"}, new Performer[]{new Performer(){
                    public void perform(){
                        Player player = Global.player.host;
                        if (player.gold >= price) {
                            if (player.inventory.vacant()) {
                                player.gold -= price;
                                if (player.wieldingNone()) player.setWeapon(item);
                                else {
                                    item.field();
                                    item.pos = player.pos.getAdd(0, 0);
                                }
                                action.perform();
                            }
                        }
                    }
                }});
    }
}