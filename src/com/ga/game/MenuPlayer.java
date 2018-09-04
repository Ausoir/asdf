package com.ga.game;

import com.ga.map.Global;
import com.ga.map.Picture;
import com.ga.tool.Performer;
import com.ga.unit.PlayerInput;
import com.ga.unit.Prop;
import com.ga.unit.Tag;
import com.ga.unit.Traits;
import com.ga.unit.item.Crafts;
import com.ga.unit.item.Item;
import com.ga.unit.man.Inventory;
import com.ga.unit.man.MainPlayer;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Buffs;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Created by Ga on 8/6/2016. asd
 */
public class MenuPlayer extends Panel {
    private class StatPanel extends JPanel {
        private GridBagConstraints statgrid = new GridBagConstraints();
        private String[] stats = MainPlayer.mainStats;
        private JButton[] statButtons = new JButton[stats.length];
        private JLabel level = new JLabel();
        private JLabel motivation = new JLabel(), nutrition = new JLabel(), fatigue = new JLabel();
        private JLabel stamina = new JLabel(), vitality = new JLabel(), energy = new JLabel();
        private JLabel points = new JLabel();
        StatPanel(){
            setLayout(new GridBagLayout());
            setBackground(Panel.forecolor2);
            statgrid.anchor = GridBagConstraints.LINE_START;
            statgrid.fill = GridBagConstraints.BOTH;
            statgrid.ipadx = 120;
            statgrid.ipady = 20;
            statgrid.gridx = 1;
            statgrid.gridy = 0;
            points.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Menu.menu.showDialog("Level up points", "Click on a stat to increase it by 1.");
                }
            });
            add(points, statgrid);
            statgrid.gridx = 0;
            add(level, statgrid);
            statgrid.gridy = 1;
            for (int i=0;i<stats.length;i++) statButtons[i] = makeStatButton(stats[i]);
            statgrid.gridx = 1;
            statgrid.gridy = 1;
            statgrid.ipady = 32;
            for (int i=0;i<stats.length;i++){
                final int var = i;
                JButton button = makeStatButton("");
                button.setIcon(iconDescribe);
                final Performer perf = new Performer() {
                    public void perform() {
                        if (Global.player.stats.get("Level").require(1))
                            Global.player.stats.get(stats[var]).addTotal(1);
                    }
                };
                statButtons[var].addActionListener(e -> {
                    perf.perform();
                    refresh();
                });
                button.addActionListener(e -> Menu.menu.showDialog(stats[var],Dialog.get("?"+stats[var])));
            }
            statgrid.gridx = 0;
            statgrid.gridy = stats.length+1;
            statgrid.ipady = 12;
            add(new JLabel(""), statgrid);
            statgrid.gridy++;
            motivation.setIcon(iconDescribe);
            motivation.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Menu.menu.showDialog("Motivation", "You need motivation to improve status or learn new things.");
                }
            });
            nutrition.setIcon(iconDescribe);
            nutrition.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Menu.menu.showDialog("Nutrition", "You require 1 nutrition every hour.");
                }
            });
            fatigue.setIcon(iconDescribe);
            fatigue.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Menu.menu.showDialog("Fatigue", "You require 1 fatigue every hour.");
                }
            });
            stamina.setIcon(iconDescribe);
            stamina.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Menu.menu.showDialog("Stamina", "You need Stamina to do most actions.");
                }
            });
            vitality.setIcon(iconDescribe);
            vitality.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Menu.menu.showDialog("Vitality", "When you are hit or otherwise hurt you will die unless you have Vitality.");
                }
            });
            energy.setIcon(iconDescribe);
            energy.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Menu.menu.showDialog("Energy", "You use Energy to cast magic.");
                }
            });
            add(stamina, statgrid);
            statgrid.gridy++;
            add(vitality, statgrid);
            statgrid.gridy++;
            add(energy, statgrid);
            statgrid.gridy-= 2;
            statgrid.gridx++;
            add(motivation, statgrid);
            statgrid.gridy++;
            add(nutrition, statgrid);
            statgrid.gridy++;
            add(fatigue, statgrid);
        }
        JButton makeStatButton(String p){
            final JButton button = new JButton(p);
            button.setFont(Panel.font);
            button.setForeground(Panel.fontcolor);
            button.setBorder(null);
            button.setContentAreaFilled(false);
            button.setFocusable(false);
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    button.setForeground(Color.CYAN);
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    button.setForeground(Panel.fontcolor);
                }
            });
            add(button, statgrid);
            statgrid.gridy++;
            return button;
        }
        void refresh(){
            MainPlayer player = Global.player;
            level.setFont(Panel.font);
            level.setForeground(Panel.fontcolor);
            level.setText("Level "+player.stats.get("Level").getMax());
            motivation.setFont(Panel.smallfont);
            motivation.setForeground(Panel.fontcolor);
            motivation.setText("Motivation - "+player.stats.get("Motivation").toString().replace(",", "/"));
            nutrition.setFont(Panel.smallfont);
            nutrition.setForeground(Panel.fontcolor);
            nutrition.setText("Nutrition - "+player.stats.get("Nutrition").toString().replace(",", "/"));
            fatigue.setFont(Panel.smallfont);
            fatigue.setForeground(Panel.fontcolor);
            fatigue.setText("Fatigue - "+player.stats.get("Fatigue").toString().replace(",", "/"));
            stamina.setFont(Panel.smallfont);
            stamina.setForeground(Panel.fontcolor);
            stamina.setText("Stamina - "+player.stats.get("Strength").toString().replace(",", "/"));
            vitality.setFont(Panel.smallfont);
            vitality.setForeground(Panel.fontcolor);
            vitality.setText("Vitality - "+player.stats.get("Agility").toString().replace(",", "/"));
            energy.setFont(Panel.smallfont);
            energy.setForeground(Panel.fontcolor);
            energy.setText("Energy - "+player.stats.get("Intelligence").toString().replace(",", "/"));
            points.setFont(Panel.smallfont);
            points.setForeground(Color.RED);
            points.setText("Available Points: "+player.stats.get("Level").get());
            if (player.stats.get("Level").get() == 0) points.setVisible(false);
            else points.setVisible(true);
            for (int i=0;i<stats.length;i++) statButtons[i].setText(stats[i]+" - "+player.stats.get(stats[i]).getMax());
            if (player.stats.get("Strength").isFull()) stamina.setVisible(false);
            else stamina.setVisible(true);
            if (player.stats.get("Agility").isFull()) vitality.setVisible(false);
            else vitality.setVisible(true);
            if (!player.traits.has(Traits.Trait.Affinity) || player.stats.get("Intelligence").isFull()) energy.setVisible(false);
            else energy.setVisible(true);
            if (player.stats.get("Motivation").get() == 0) motivation.setVisible(false);
            else motivation.setVisible(true);
        }
    }

    private class EquipmentPanel extends JTextArea{
        private GridBagConstraints grid = new GridBagConstraints();
        private JPanel panel;
        private JScrollPane scroll;
        private JLabel weight;
        private Border emptyborder = new EmptyBorder(5,8,5,8);
        EquipmentPanel(){
            setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
            weight = new JLabel();
            panel = new JPanel();
            panel.setOpaque(false);
            panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
            scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.getVerticalScrollBar().setUI(new ScrollBar());
            scroll.getVerticalScrollBar().setOpaque(false);
            scroll.getVerticalScrollBar().setUnitIncrement(24);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(null);
            scroll.setOpaque(false);
            setBackground(Panel.forecolor2);
            add(weight, BorderLayout.LINE_START);
            add(scroll, BorderLayout.LINE_START);
        }
        void refresh(){
            weight.setText("Encumbrance:"+Global.player.armor.getEncumbrance()+"/"+Global.player.stats.get("Agility").getMax()+
                    "                 Warmth:"+Global.player.armor.getWarmth()+"/"+Global.player.map.temperature);
            panel.removeAll();

            grid.gridy = 0;
            Item wielded = Global.player.weapon;
            makeSlot(-1, wielded);
            TitledBorder b = BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(2,2,2,2,Panel.forecolor));
            b.setTitle(Dialog.get("wielded"));
            ((JPanel) panel.getComponent(0)).setBorder(b);

            for (int i=0;i<5;i++) {
                if (!Global.player.armor.isBare(i)) {
                    grid.gridy++;
                    Item equipped = Global.player.armor.armor[i];
                    JPanel panel = makeSlot(i, equipped);
                    b = BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Panel.forecolor));
                    String title = Dialog.get("equipped");
                    if (i == 0) title += " - "+Dialog.get("bhead");
                    else if (i == 1) title += " - "+Dialog.get("btorso");
                    else if (i == 2) title += " - "+Dialog.get("bleg");
                    else if (i == 3) title += " - Back";
                    else title += " - Trinket";
                    b.setTitle(title);
                    panel.setBorder(b);
                }
            }
        }
        JPanel makeSlot(final int type, final Item item){
            JPanel panel = new JPanel();
            panel.setOpaque(false);
            panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
            panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
            final JLabel result = new JLabel();
            result.setIcon(new ImageIcon(item.image.getImage()));
            result.setHorizontalTextPosition(JLabel.CENTER);
            result.setVerticalTextPosition(JLabel.TOP);
            result.setBorder(emptyborder);
            if (type == -1) {
                if (Global.player.wieldingNone()) panel.setVisible(false);
                result.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        Global.player.input.inputDirect("Wield", 0, 0);
                        Menu.menu.showMenu();
                    }
                });
            } else { //armor
                result.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        Global.player.input.inputDirect("_WI"+type,0,0);
                        Menu.menu.showMenu();
                    }
                });
            }
            JPanel options = new JPanel();
            options.setOpaque(false);
            options.setLayout(new BoxLayout(options,BoxLayout.LINE_AXIS));
            final JLabel describe = new JLabel();
            describe.setIcon(iconDescribe);
            describe.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Menu.menu.showDialog(item.getFullName(),item.getDescription());
                }
            });
            final JLabel drop = new JLabel(item.getFullName()+" ("+Math.round(item.getPercentage()*100)+"%)             ");
            drop.setForeground(item.getRarityColor());
            options.add(describe);
            options.add(drop);
            panel.add(options);
            panel.add(result);
            this.panel.add(panel);
            return panel;
        }
    }

    private class InventoryPanel extends JPanel {
        private GridBagConstraints grid = new GridBagConstraints();
        private JPanel panel;
        private JScrollPane scroll;
        private JLabel weight;
        private Border emptyborder = new EmptyBorder(5,8,5,8);
        InventoryPanel(){
            setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
            weight = new JLabel();
            panel = new JPanel();
            panel.setOpaque(false);
            panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
            scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.getVerticalScrollBar().setUI(new ScrollBar());
            scroll.getVerticalScrollBar().setOpaque(false);
            scroll.getVerticalScrollBar().setUnitIncrement(24);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(null);
            scroll.setOpaque(false);
            setBackground(Panel.forecolor2);
            add(weight, BorderLayout.LINE_START);
            add(scroll, BorderLayout.LINE_START);
        }
        void refresh(){
            Inventory inv = Global.player.inventory;
            int itemsize = inv.getItemSize();
            weight.setText("Pockets:"+itemsize+"/"+Global.player.armor.getPockets());
            panel.removeAll();
            for (int i = 0; i < itemsize; i++) {
                Item item = inv.getItem(i);
                grid.gridy = i;
                makeSlot(item, i);
            }
        }
        void makeSlot(final Item item, final int index){
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
                    Global.player.input.inputDirect("_WI"+(10+index),0,0);
                    Menu.menu.showMenu();
                }
            });
            JPanel options = new JPanel();
            options.setOpaque(false);
            options.setLayout(new BoxLayout(options,BoxLayout.LINE_AXIS));
            final JLabel describe = new JLabel();
            describe.setIcon(iconDescribe);
            describe.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Menu.menu.showDialog(item.getFullName(),item.getDescription());
                }
            });
            final JLabel drop = new JLabel(item.getFullName()+" ("+Math.round(item.getPercentage()*100)+"%)             ");
            drop.setForeground(item.getRarityColor());
            options.add(describe);
            options.add(drop);
            panel.add(options);
            panel.add(result);
            this.panel.add(panel);
        }
    }

    private class ActionPanel extends JPanel{
        private JScrollPane scroll;
        private JPanel panel;
        private JLabel skillpoints;
        ActionPanel(){
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            setVisible(false);
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            panel.setOpaque(false);
            scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.getVerticalScrollBar().setUI(new ScrollBar());
            scroll.getVerticalScrollBar().setOpaque(false);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(null);
            setBackground(Panel.forecolor2);
            add(scroll, BorderLayout.CENTER);
            skillpoints = new JLabel();
            add(skillpoints);
        }
        void refresh(){
            panel.removeAll();
            panel.add(new JLabel(Dialog.get("Remap")));
            panel.add(Box.createRigidArea(new Dimension(0,12)));
            makeSlot("_EXIT");
            makeSlot("_MENU");
            makeSlot("_CTRL");
            makeSlot("_SAY");
            for (Actions i : PlayerInput.basicMoves){
                if (i != Actions.Idle) makeSlot(i);
            }
            for (Actions i : Global.player.moveset){
                if (i != Actions.Use) makeSlot(i);
            }
            int points = Global.player.stats.get("Intelligence").get();
            skillpoints.setText(Dialog.get("mem")+": "+points);
        }
        void makeSlot(String extra){
            String key = Global.player.input.getMapping(extra);
            if (key == null) key = "";
            else key = "["+key+"]";
            JPanel panel = new JPanel();
            panel.setOpaque(false);
            panel.setLayout(new BoxLayout(panel,BoxLayout.LINE_AXIS));
            panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
            final JLabel equip = new JLabel(extra.substring(1));
            final JLabel keyLabel = new JLabel(" "+key+" ");
            keyLabel.setForeground(Color.RED);
            equip.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    equip.setText(" "+Dialog.get("reqInput"));
                    equip.requestFocus();
                }
            });
            equip.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (equip.hasFocus()) {
                        Global.player.input.changeMapping(e.getButton(), extra);
                        Global.display.requestFocus();
                        Menu.menu.showMenu();
                        Menu.menu.showMenu();
                    }
                }
            });
            equip.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    Global.player.input.changeMapping(e.getKeyCode(), extra);
                    Global.display.requestFocus();
                    Menu.menu.showMenu();
                    Menu.menu.showMenu();
                }
            });
            panel.add(keyLabel);
            panel.add(equip);
            this.panel.add(panel);
            this.panel.add(Box.createRigidArea(new Dimension(0,4)));
        }
        void makeSlot(final Actions move){
            final String displayName = Dialog.get(move.name());
            String key = Global.player.input.getMapping(move.name());
            if (key == null) key = "";
            else key = "["+key+"]";
            JPanel panel = new JPanel();
            panel.setOpaque(false);
            panel.setLayout(new BoxLayout(panel,BoxLayout.LINE_AXIS));
            panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
            final JLabel describe = new JLabel(iconDescribe);
            describe.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Menu.menu.showDialog(move.name(), move.getDescription());
                }
            });
            int level = Global.player.getStateLevel(move);
            final JLabel equip = new JLabel(displayName+" - "+Dialog.get("pwr")+" "+level);
            final JLabel keyLabel = new JLabel(" "+key+" ");
            keyLabel.setForeground(Color.RED);
            equip.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    describe.setVisible(false);
                    equip.setText(" "+Dialog.get("reqInput"));
                    equip.requestFocus();
                }
            });
            equip.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (equip.hasFocus()) {
                        Global.player.input.changeMapping(e.getButton(), move);
                        Global.display.requestFocus();
                        describe.setVisible(true);
                        refresh();
                    }
                }
            });
            equip.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    Global.player.input.changeMapping(e.getKeyCode(), move);
                    Global.display.requestFocus();
                    describe.setVisible(true);
                    refresh();
                }
            });
            if (!move.getDescription().equals("")) panel.add(describe);
            panel.add(keyLabel);
            panel.add(equip);
            this.panel.add(panel);
            this.panel.add(Box.createRigidArea(new Dimension(0,4)));
        }
    }

    private class TraitPanel extends JPanel{
        private JScrollPane scroll;
        private JPanel panel;
        TraitPanel(){
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            setVisible(false);
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            panel.setOpaque(false);
            scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.getVerticalScrollBar().setUI(new ScrollBar());
            scroll.getVerticalScrollBar().setOpaque(false);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(null);
            setBackground(Panel.forecolor2);
            add(scroll, BorderLayout.CENTER);
        }
        void refresh(){
            panel.removeAll();
            panel.add(Box.createRigidArea(new Dimension(0,12)));
            for (Traits.Trait i : Global.player.traits.getTraits()){
                makeSlot(i);
            }
        }
        void makeSlot(final Traits.Trait move){
            final String displayName = Dialog.get(move.name());
            JPanel panel = new JPanel();
            panel.setOpaque(false);
            panel.setLayout(new BoxLayout(panel,BoxLayout.LINE_AXIS));
            panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
            final JLabel describe = new JLabel(iconDescribe);
            describe.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Menu.menu.showDialog(move.name(), move.getDescription());
                }
            });
            int level = Global.player.traits.getTraitLevel(move);
            String label = displayName;
            if (move.type == 3) label += " - "+Dialog.get("pwr")+" "+level+(level==Traits.MAXLEVEL?" [MAX]":"");
            final JLabel equip = new JLabel(label);
            if (!move.getDescription().equals("")) panel.add(describe);
            panel.add(equip);
            this.panel.add(panel);
            this.panel.add(Box.createRigidArea(new Dimension(0,4)));
        }
    }

    private class BuffPanel extends JPanel{
        private JScrollPane scroll;
        private JPanel panel;
        BuffPanel(){
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            setVisible(false);
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            panel.setOpaque(false);
            scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.getVerticalScrollBar().setUI(new ScrollBar());
            scroll.getVerticalScrollBar().setOpaque(false);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(null);
            setBackground(Panel.forecolor2);
            add(scroll, BorderLayout.CENTER);
        }
        void refresh(){
            panel.removeAll();
            panel.add(Box.createRigidArea(new Dimension(0,12)));
            for (Buffs.Buff i : Global.player.status.getEffects()){
                makeSlot(i);
            }
        }
        void makeSlot(Buffs.Buff buff){
            final String displayName = Dialog.get(buff.buff.name());
            JPanel panel = new JPanel();
            panel.setOpaque(false);
            panel.setLayout(new BoxLayout(panel,BoxLayout.LINE_AXIS));
            panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
            final JLabel describe = new JLabel(iconDescribe);
            describe.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Menu.menu.showDialog(buff.buff.name(), buff.getDescription());
                }
            });
            final JLabel display = new JLabel(displayName+" - Level "+buff.getLevel()+" (Duration: "+buff.getLengthDescription()+")");
            panel.add(describe);
            panel.add(display);
            this.panel.add(panel);
            this.panel.add(Box.createRigidArea(new Dimension(0,4)));
        }
    }

    private class OptionPanel extends JPanel{
        private JPanel panel;
        private JPanel other;
        OptionPanel(){
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            setVisible(false);
            setBackground(Panel.forecolor2);
            panel = new JPanel();
            panel.setOpaque(false);
            other = new JPanel();
            other.setOpaque(false);
        }
        void refresh() {
            panel.removeAll();
            panel.add(makeOption("Level Up","lvl","Level up with sufficient Motivation points and time."));
            panel.add(makeOption("Rest","h","Rest for an hour, removing curable debuffs and restoring fatigue. Restore fatigue twice more near chairs and benches, and three times more near beds."));
            if (Global.player.stats.get("Nutrition").get() == 0){
                if (Global.player.stats.get("Agility").get() > 0) panel.add(makeOption("Starve", "st", "Starve, gaining 1 Nutrition point at the cost of 1 Vitality."));
                else panel.add(makeOption("STARVE TO DEATH", "st", "Submit to starvation and die."));
            }
            TitledBorder b = BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(2,2,2,2,Panel.forecolor));
            b.setTitle("Strategic");
            panel.setBorder(b);
            add(panel, BorderLayout.CENTER);

            other.removeAll();
            other.add(makeOption("Other","opt","Do stuff."));
            other.add(makeOption("Help","?","Noob"));
            other.add(makeOption("Exit","x","Exit the game (Press Esc)."));
            b = BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(2,2,2,2,Panel.forecolor));
            b.setTitle("Game Menu");
            other.setBorder(b);
            add(other, BorderLayout.CENTER);
        }
        JPanel makeOption(final String p, final String cmd, final String description){
            JPanel subpanel = new JPanel();
            final JLabel button = new JLabel(" "+p+" ");
            button.setFont(Panel.font);
            button.setForeground(Panel.fontcolor);
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    button.setForeground(Color.CYAN);
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    button.setForeground(Panel.fontcolor);
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(Panel.forecolor2);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(Panel.forecolor);
                }
                @Override
                public void mouseClicked(MouseEvent e){
                    command(cmd);
                }
            });
            JLabel desc = new JLabel();
            desc.setIcon(iconDescribe);
            desc.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Menu.menu.showDialog(p,description);
                }
            });
            subpanel.setOpaque(false);
            subpanel.setBorder(BorderFactory.createMatteBorder(1,1,1,1,Panel.forecolor));
            subpanel.add(button);
            subpanel.add(desc);
            return subpanel;
        }
        private void command(String p){
            switch (p) {
                case "h":
                    Global.player.rest();
                    Menu.menu.showMenu();
                    break;
                case "lvl":
                    int reqMot = Global.player.stats.get("Motivation").getMax(), reqTime = 3;
                    String message = "If you have the time and motivation, you may train to make yourself stronger.";
                    message += "\nRequires "+reqMot+" Motivation points, and "+reqTime+" hours (= "+reqTime+" Nutrition and Fatigue points).";
                    message += "\n\nCurrent Motivation: "+Global.player.stats.get("Motivation").get();
                    message += "\nCurrent Nutrition: "+Global.player.stats.get("Nutrition").get();
                    message += "\nCurrent Fatigue: "+Global.player.stats.get("Fatigue").get();
                    Menu.menu.showDialog("Level Up", message,
                            new String[]{"Begin Training!"},
                            new Performer[]{
                                    new Performer(){
                                        public void perform(){
                                            if (Global.player.stats.get("Motivation").get() >= reqMot){
                                                if (Global.player.map.updater.sleep(reqTime, true)) {
                                                    Global.player.level();
                                                    Global.player.stats.get("Motivation").require(reqMot);
                                                }
                                            } else Global.player.say("Not enough Motivation..");
                                        }
                                    }
                            });
                    break;
                case "?":
                    Menu.menu.showDialog("Help", "The controls are WASD for movement and left click to interact with the world.");
                    break;
                case "opt":
                    Menu.menu.showDialog("Options", "apparently none.");
                    break;
                case "st":
                    if (Global.player.stats.get("Agility").require(1)){
                        Global.player.stats.get("Nutrition").add(1, false);
                        Global.player.say("I have "+Global.player.stats.get("Agility").get()+" Vitality left!");
                    } else Global.player.die("Starvation");
                    Menu.menu.showMenu();
                    break;
            }
        }
    }

    private class CraftPanel extends JPanel{
        private ArrayList<String> items, props;
        private JPanel panel;
        private JPanel other;
        CraftPanel(){
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            setVisible(false);
            setBackground(Panel.forecolor2);
            panel = new JPanel();
            panel.setOpaque(false);
            other = new JPanel();
            other.setOpaque(false);
        }
        void refresh() {
            ArrayList<String> craftables = new ArrayList<>();
            items = new ArrayList<>();
            props = new ArrayList<>();
            for (Prop i : Global.player.getAvailableItems()){
                items.add(i.name);
                for (Tag ii : i.tags) items.add(ii.name());
            }
            for (Prop i : Global.player.getAvailableProps()){
                props.add(i.name);
                for (Tag ii : i.tags) props.add(ii.name());
            }
            panel.removeAll();
            panel.add(makeOption("[Available Items]","?","Show all items available for crafting."));
            for (String i : Global.player.crafting){
                if (Crafts.canCraft(Global.player, i) >= 0){
                    StringBuilder t = new StringBuilder();
                    craftables.add(i);
                    for (String ii : Crafts.getRecipe(i)) t.append(ii).append(", ");
                    panel.add(makeOption(i, i, "Craft " + i + " using ["+t.substring(0,t.length()-2)+"]"));
                }
            }
            TitledBorder b = BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(2,2,2,2,Panel.forecolor));
            b.setTitle("Craftable");
            panel.setBorder(b);
            add(panel, BorderLayout.CENTER);

            other.removeAll();
            for (String i : Global.player.crafting){
                if (!craftables.contains(i)) {
                    StringBuilder t = new StringBuilder();
                    for (String ii : Crafts.getRecipe(i)) t.append(ii).append(", ");
                    other.add(makeOption(i, "=", "Crafting "+i+" requires ["+t.substring(0, t.length() - 2)+"]"));
                }
            }
            b = BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(2,2,2,2,Panel.forecolor));
            b.setTitle("Known");
            other.setBorder(b);
            add(other, BorderLayout.CENTER);
        }
        JPanel makeOption(final String p, final String cmd, final String description){
            JPanel subpanel = new JPanel();
            final JLabel button = new JLabel(" "+p+" ");
            button.setFont(Panel.font);
            button.setForeground(Panel.fontcolor);
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    button.setForeground(Color.CYAN);
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    button.setForeground(Panel.fontcolor);
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(Panel.forecolor2);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(Panel.forecolor);
                }
                @Override
                public void mouseClicked(MouseEvent e){
                    if (cmd.equals("="))
                        Menu.menu.showDialog(p,description);
                    else command(cmd);
                }
            });
            JLabel desc = new JLabel("");
            desc.setIcon(iconDescribe);
            desc.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Menu.menu.showDialog(p,description);
                }
            });
            subpanel.setOpaque(false);
            subpanel.setBorder(BorderFactory.createMatteBorder(1,1,1,1,Panel.forecolor));
            subpanel.add(button);
            subpanel.add(desc);
            return subpanel;
        }
        private void command(String p){
            Menu.menu.showMenu();
            switch (p) {
                case "?": {
                    StringBuilder t = new StringBuilder();
                    for (String i : items) {
                        if (!i.equals("Time") && !i.equals("Fist")) t.append(i).append(", ");
                    }
                    for (String i : props) {
                        t.append(i).append(", ");
                    }
                    String result = t.toString().equals("") ? "None" : t.substring(0, t.length() - 2);
                    Menu.menu.showDialog("Available Items", result);
                    break;
                }
                default:
                    Command.order("crft@" + p);
                    break;
            }
        }
    }

    private ImageIcon iconDescribe = new ImageIcon(Picture.loadCache("Describe"));
    private GridBagConstraints grid = new GridBagConstraints();
    private JPanel mainpanel = new JPanel();
    private OptionPanel optionpanel = new OptionPanel();
    private StatPanel statspanel = new StatPanel();
    private EquipmentPanel equipmentPanel = new EquipmentPanel();
    private InventoryPanel inventorypanel = new InventoryPanel();
    private ActionPanel actionpanel = new ActionPanel();
    private TraitPanel traitpanel = new TraitPanel();
    private CraftPanel craftpanel = new CraftPanel();
    private BuffPanel buffpanel = new BuffPanel();
    private final int MAX_ROWS = 8;
    private JButton[] buttons = new JButton[MAX_ROWS];
    MenuPlayer() {
        targetSizeX = 764;
        setLayout(new GridBagLayout());
        grid.anchor = GridBagConstraints.LINE_START;
        grid.fill = GridBagConstraints.NONE;

        grid.weightx = 0;
        grid.weighty = 1;
        addMainPanel();
        grid.gridx = 1;
        grid.weightx = 1;
        grid.anchor = GridBagConstraints.FIRST_LINE_START;
        grid.fill = GridBagConstraints.BOTH;
        add(optionpanel,grid);
        add(actionpanel,grid);
        add(statspanel,grid);
        add(equipmentPanel,grid);
        add(inventorypanel,grid);
        add(traitpanel,grid);
        add(craftpanel,grid);
        add(buffpanel,grid);
        command("");

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {}
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
        });
    }
    private void addMainPanel(){
        GridLayout gridlayout = new GridLayout(MAX_ROWS,1,20,36);
        mainpanel.setOpaque(false);
        mainpanel.setLayout(gridlayout);
        mainpanel.add(buttons[0] = makeOption(Dialog.get("Options"), "opt"));
        mainpanel.add(buttons[1] = makeOption(Dialog.get("Actions"), "act"));
        mainpanel.add(buttons[2] = makeOption(Dialog.get("Attributes"), "status"));
        mainpanel.add(buttons[3] = makeOption(Dialog.get("Equipment"), "equip"));
        mainpanel.add(buttons[4] = makeOption(Dialog.get("Inventory"), "inventory"));
        mainpanel.add(buttons[5] = makeOption(Dialog.get("Traits"), "trait"));
        mainpanel.add(buttons[6] = makeOption(Dialog.get("Crafting"), "craft"));
        mainpanel.add(buttons[7] = makeOption(Dialog.get("Status"), "buff"));
        grid.gridx = 0;
        add(mainpanel,grid);
    }
    @Override
    protected void setColor() {
        setBackground(forecolor);
    }
    private JButton makeOption(String p, final String cmd){
        final JButton button = new JButton(" "+p+" ");
        button.setFont(Panel.font);
        button.setForeground(Panel.fontcolor);
        button.setBorder(null);
        button.setContentAreaFilled(false);
        button.setFocusable(false);
        button.setOpaque(true);
        button.setBackground(Panel.forecolor);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                button.setForeground(Color.CYAN);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                button.setForeground(Panel.fontcolor);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(Panel.forecolor2);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Panel.forecolor);
            }
        });
        button.addActionListener(e -> command(cmd));
        return button;
    }
    private void command(String p){
        optionpanel.setVisible(false);
        actionpanel.setVisible(false);
        statspanel.setVisible(false);
        equipmentPanel.setVisible(false);
        inventorypanel.setVisible(false);
        traitpanel.setVisible(false);
        craftpanel.setVisible(false);
        buffpanel.setVisible(false);
        switch (p) {
            case "opt":
                optionpanel.setVisible(true);
                break;
            case "act":
                actionpanel.setVisible(true);
                break;
            case "status":
                statspanel.setVisible(true);
                break;
            case "equip":
                equipmentPanel.setVisible(true);
                break;
            case "inventory":
                inventorypanel.setVisible(true);
                break;
            case "trait":
                traitpanel.setVisible(true);
                break;
            case "craft":
                craftpanel.setVisible(true);
                break;
            case "buff":
                buffpanel.setVisible(true);
                break;
        }
        refresh();
    }
    void toggle(){
        if (isVisible()){
            setVisible(false);
        } else {
            display();
        }
    }
    private void refresh(){
        if (optionpanel.isVisible()) optionpanel.refresh();
        else if (actionpanel.isVisible()) actionpanel.refresh();
        else if (statspanel.isVisible()) statspanel.refresh();
        else if (inventorypanel.isVisible()) inventorypanel.refresh();
        else if (traitpanel.isVisible()) traitpanel.refresh();
        else if (equipmentPanel.isVisible()) equipmentPanel.refresh();
        else if (craftpanel.isVisible()) craftpanel.refresh();
        else if (buffpanel.isVisible()) buffpanel.refresh();
    }
    public void display(){
        super.display();
        setBackground(new Color(120,120,120,180));
        if (Global.player.armor.isBare() && Global.player.wieldingNone()) buttons[3].setVisible(false);
        else buttons[3].setVisible(true);
        if (Global.player.inventory.getItemSize() == 0) buttons[4].setVisible(false);
        else buttons[4].setVisible(true);
        if (Global.player.traits.getTraits().size() == 0) buttons[5].setVisible(false);
        else buttons[5].setVisible(true);
        if (Global.player.crafting.size() == 0) buttons[6].setVisible(false);
        else buttons[6].setVisible(true);
        if (Global.player.status.getEffects().size() == 0) buttons[7].setVisible(false);
        else buttons[7].setVisible(true);
        refresh();
    }
}

class ScrollBar extends BasicScrollBarUI{
    @Override
    protected void installComponents(){
        switch (scrollbar.getOrientation()) {
            case JScrollBar.VERTICAL:
                incrButton = createIncreaseButton(SOUTH);
                decrButton = createDecreaseButton(NORTH);
                break;

            case JScrollBar.HORIZONTAL:
                if (scrollbar.getComponentOrientation().isLeftToRight()) {
                    incrButton = createIncreaseButton(EAST);
                    decrButton = createDecreaseButton(WEST);
                } else {
                    incrButton = createIncreaseButton(WEST);
                    decrButton = createDecreaseButton(EAST);
                }
                break;
        }
        scrollbar.setEnabled(scrollbar.isEnabled());
    }
    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle bounds){ }
    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle bounds){
        if(bounds.isEmpty() || !scrollbar.isEnabled())     {
            return;
        }

        int w = bounds.width;
        int h = bounds.height;

        g.translate(bounds.x, bounds.y);

        g.setColor(new Color(180,180,180,125));
        g.fillRect(0, 0, w-1, h-1);

        g.translate(-bounds.x, -bounds.y);
    }
}