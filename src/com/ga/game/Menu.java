package com.ga.game;

import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.unit.Inventory;
import com.ga.unit.Item;
import com.ga.unit.Player;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by Ga on 8/6/2016.
 */
public class Menu extends JPanel {
    public P size;
    private GridBagConstraints grid = new GridBagConstraints();
    private JPanel mainpanel = new JPanel();
    private StatPanel statspanel = new StatPanel();
    private InventoryPanel inventorypanel = new InventoryPanel();
    private JPanel optionspanel = new JPanel();
    private Image image;
    public Menu() {
        setLayout(new GridBagLayout());
        image = Texture.load("Menu.png");
        setBorder(new EmptyBorder(100,140,100,140));
        size = new P(image.getWidth(null), image.getHeight(null));
        resize();
        setVisible(false);
        grid.anchor = GridBagConstraints.LINE_START;
        grid.fill = GridBagConstraints.NONE;

        grid.weightx = .1;
        grid.weighty = 1;
        optionspanel.setOpaque(false);
        addMainPanel();
        grid.gridx = 1;
        grid.weightx = .9;
        grid.anchor = GridBagConstraints.FIRST_LINE_START;
        grid.fill = GridBagConstraints.BOTH;
        add(statspanel,grid);
        add(inventorypanel,grid);
        add(optionspanel,grid);
        command("");
    }
    private void addMainPanel(){
        GridLayout gridlayout = new GridLayout(3,1,20,40);
        mainpanel.setOpaque(false);
        mainpanel.setLayout(gridlayout);
        mainpanel.add(makeMenuButton("Stats", "stats"));
        mainpanel.add(makeMenuButton("Inventory", "inventory"));
        mainpanel.add(makeMenuButton("Options", "options"));
        grid.gridx = 0;
        add(mainpanel,grid);
    }
    @Override
    public void paintComponent(Graphics g){
        g.drawImage(image,0,0,null);
    }
    public JButton makeOption(String p){
        final JButton button = new JButton(p);
        button.setFont(new Font("Ariel",Font.ITALIC,16));
        button.setForeground(new Color(110,105,80));
        button.setBorder(null);
        button.setContentAreaFilled(false);
        button.setFocusable(false);
        button.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }
            @Override
            public void mousePressed(MouseEvent e) {
                button.setForeground(Color.blue);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                button.setForeground(new Color(110,105,80));
            }
            @Override
            public void mouseEntered(MouseEvent e) {

            }
            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        return button;
    }
    private JButton makeMenuButton(String p, final String cmd){
        JButton button = makeOption(p);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                command(cmd);
            }
        });
        return button;
    }
    private void command(String p){
        statspanel.setVisible(false);
        inventorypanel.setVisible(false);
        optionspanel.setVisible(false);
        if (p.equals("stats")) statspanel.setVisible(true);
        else if (p.equals("inventory")) inventorypanel.setVisible(true);
        else if (p.equals("options")) optionspanel.setVisible(true);
    }
    public void toggle(){
        if (isVisible()) setVisible(false);
        else{
            setVisible(true);
        }
    }
    public void resize(){
        int x = (int) (Settings.centerX-(size.x/2));
        int y = (int) (Settings.centerY-(size.y/2));
        setBounds(x,y, (int) size.x, (int) size.y);
    }
    public void setVisible(boolean visible){
        super.setVisible(visible);
        if (visible && Global.player != null){
            statspanel.refresh();
            inventorypanel.refresh();
        }
    }
}

class StatPanel extends JPanel{
    private GridBagConstraints statgrid = new GridBagConstraints();
    public StatPanel(){
        setLayout(new GridBagLayout());
        setOpaque(false);
        statgrid.anchor = GridBagConstraints.LINE_START;
        statgrid.fill = GridBagConstraints.BOTH;
        statgrid.ipadx = 120;
        statgrid.ipady = 20;
        statgrid.gridx = 0;
        statgrid.gridy = 0;
        makeStatButton("Damage - ", null);
        makeStatButton("Strength - ", null);
        makeStatButton("Stamina - ", null);
        makeStatButton("Memory - ", null);
        makeStatButton("Reset Points - ", null);
        makeStatButton("Level Points - ", null);
        statgrid.ipadx = 5;
        statgrid.gridx = 1;
        statgrid.gridy = 0;
        makeStatButton("[+]", "level@damage");
        makeStatButton("[+]", "level@arms");
        makeStatButton("[+]", "level@legs");
        makeStatButton("[+]", "level@int");
        statgrid.gridx = 2;
        statgrid.gridy = 0;
        makeStatButton("[-]", "delevel@damage");
        makeStatButton("[-]", "delevel@arms");
        makeStatButton("[-]", "delevel@legs");
        makeStatButton("[-]", "delevel@int");
        statgrid.gridx = 3;
        statgrid.gridy = 0;
//        statgrid.insets = new Insets(0,0,0,240);
        makeStatButton("[?]", "say@increases maximum damage by 1.");
        makeStatButton("[?]", "say@increases arm stamina by 1.");
        makeStatButton("[?]", "say@increases leg stamina by 1.");
        makeStatButton("[?]", "say@increases memory by 1.");
    }
    protected void refresh(){
        Player player = Global.player.host;
        ((JButton)getComponent(0)).setText("Damage - "+player.get("damage").max);
        ((JButton)getComponent(1)).setText("Strength - "+player.get("arms").max);
        ((JButton)getComponent(2)).setText("Stamina - "+player.get("legs").max);
        ((JButton)getComponent(3)).setText("Memory - "+player.get("damage").max);
        ((JButton)getComponent(4)).setText("Reset Points - "+player.get("adapt").get());
        ((JButton)getComponent(5)).setText("Level Points - "+player.get("points").get());
        if (player.get("points").get() > 0)
            for (int i=6;i<10;i++)
                getComponent(i).setVisible(true);
        else
            for (int i=6;i<10;i++)
                getComponent(i).setVisible(false);

        if (player.get("adapt").get() > 0)
            for (int i=10;i<14;i++)
                getComponent(i).setVisible(true);
        else
            for (int i=10;i<14;i++)
                getComponent(i).setVisible(false);
    }
    public JButton makeStatButton(String p, final String cmd){
        final JButton button = new JButton(p);
        button.setFont(new Font("Ariel",Font.ITALIC,16));
        button.setForeground(new Color(110,105,80));
        button.setBorder(null);
        button.setContentAreaFilled(false);
        button.setFocusable(false);
        button.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }
            @Override
            public void mousePressed(MouseEvent e) {
                button.setForeground(Color.blue);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                button.setForeground(new Color(110,105,80));
            }
            @Override
            public void mouseEntered(MouseEvent e) {

            }
            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        if (cmd == null) button.removeMouseListener(button.getMouseListeners()[0]);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Command.order(cmd);
                refresh();
            }
        });
        add(button, statgrid);
        statgrid.gridy++;
        return button;
    }
}

class InventoryPanel extends JPanel {
    private GridBagConstraints grid = new GridBagConstraints();
    private JPanel panel;
    private Border emptyborder = new EmptyBorder(5,8,5,8);
    private int[] selected;
    private boolean mRelease = true;
    private int prevY = 0;
    public InventoryPanel(){
        setLayout(null);
        setOpaque(false);
        setBorder(BorderFactory.createBevelBorder(1));
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int y = e.getY();
                if (mRelease) mRelease = false;
                else panel.setLocation(0, (int) Global.clamp(panel.getY() + (y - prevY),-600,0));
                prevY = y;
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
            }
            @Override
            public void mouseReleased(MouseEvent e){
                mRelease = true;
            }
        });
        panel = new JPanel();
        panel.setBounds(0,0,1000,1000);
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
//        refresh();
        add(panel);
    }
    public void refresh(){
        panel.removeAll();
        Inventory inv = Global.player.host.inventory;
        int itemsize = inv.items.size();
        selected = new int[itemsize];
        for (int i = 0; i < itemsize; i++) {
            Item item = inv.items.get(i);
            grid.gridy = i;
            makeSlot(item.name, item.image, i);
        }
        for (int i = 0; i < inv.pockets.length; i++) {
            int slot = inv.pockets[i];
            if (slot != -1) {
                equip(slot);
            }
        }
        if (inv.wielded != -1) equip(inv.wielded);
    }
    public void makeSlot(final String p, Image img, final int item){
        final JLabel result = new JLabel(p);
        result.setIcon(new ImageIcon(img));
        result.setBorder(emptyborder);
        result.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (selected[item] == 0) equip(item);
                else unequip(item);
            }
        });
        panel.add(result);
    }
    public void equip(int p){
        final Inventory inv = Global.player.host.inventory;
        int r = inv.equip(p);
        for (int i = 0; i < selected.length; i++) {
            if (i != p && r == selected[i]) unequip(i);
        }
        TitledBorder b = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder());
        if (r == 1) b.setTitle("Wielded");
        else if (r >= 10) b.setTitle(""+(r-9));
        ((JLabel) panel.getComponent(p)).setBorder(b);
        selected[p] = r;
    }
    public void unequip(int p){
        Global.player.host.inventory.unequip(p);
        ((JLabel) panel.getComponent(p)).setBorder(emptyborder);
        selected[p] = 0;
    }
}