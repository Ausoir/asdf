package com.ga.game;

import com.ga.map.Global;
import com.ga.map.Picture;
import com.ga.tool.Performer;
import com.ga.unit.item.Item;
import com.ga.unit.man.Player;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Ga on 11/15/2016. asd
 */
public class MenuShop extends Panel {
    private GridBagConstraints grid = new GridBagConstraints();
    private JPanel panel;
    private JScrollPane scroll;
    private JLabel gold, titleLabel;
    private Border emptyborder = new EmptyBorder(5,8,5,8);

    MenuShop(){
        super();
        setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
        titleLabel = new JLabel();
        gold = new JLabel();
        panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                setVisible(false);
            }
        });
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
        titleLabel.setForeground(fontcolor);
        gold.setForeground(fontcolor);
    }
    protected void setColor() {
        setBackground(forecolor);
        scroll.setBackground(forecolor2);
    }
    void show(String title, Item[] wares, int[] prices, Performer[] actions){
        display();
        titleLabel.setText(title);
        gold.setText("Cash: " + Dialog.money(Global.player.getGold()));
        panel.removeAll();
        int itemsize = wares.length;
        for (int i = 0; i < itemsize; i++) {
            if (wares[i] != null) {
                grid.gridy = i + 2;
                makeSlot(wares[i], prices[i], actions[i]);
            }
        }
        panel.updateUI();
    }
    private void makeSlot(final Item item, final int price, final Performer action){
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
        final JLabel drop = new JLabel(item.getFullName()+" - "+Dialog.money(price));
        drop.setIcon(new ImageIcon(Picture.loadCache("Buy")));
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
        Menu.menu.showDialog(item.getFullName(),"This is "+item.getDescription()+" It is being sold for "+Dialog.money(price)+".",
                new String[]{"Buy"}, new Performer[]{new Performer(){
                    public void perform(){
                        Player player = Global.player;
                        if (player.inventory.vacant() || player.wieldingNone()) {
                            if (player.requireGold(price)) {
                                if (player.wieldingNone()) player.setWeapon(item);
                                else {
                                    item.pos = player.pos.getAdd(0, 0);
                                }
                                action.perform();
                            } else {
                                Global.player.say("Not enough money..");
                            }
                        } else {
                            Global.player.say("Not enough inventory space..");
                        }
                        setVisible(false);
                    }
                }});
    }
}