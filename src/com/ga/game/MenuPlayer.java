package com.ga.game;

import com.ga.map.Global;
import com.ga.map.Picture;
import com.ga.tool.Performer;
import com.ga.tool.T;
import com.ga.unit.PlayerInput;
import com.ga.unit.Prop;
import com.ga.unit.Traits;
import com.ga.unit.item.Crafts;
import com.ga.unit.item.Item;
import com.ga.unit.man.Inventory;
import com.ga.unit.man.MainPlayer;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Buffs;
import com.ga.unit.skill.Stat;

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

/**
 * Created by Ga on 8/6/2016. asd
 */
public class MenuPlayer extends Panel {
    private class StatPanel extends JPanel {
        private Stat[] stats = new Stat[]{Global.player.stats.strength, Global.player.stats.agility, Global.player.stats.intelligence};
        private GridBagConstraints statgrid = new GridBagConstraints();
        private JButton[] statButtons = new JButton[3];
        private JLabel level = new JLabel(), points = new JLabel();
        private JLabel motivation = new JLabel(), nutrition = new JLabel(), fatigue = new JLabel();
        private JLabel stamina = new JLabel(), vitality = new JLabel(), energy = new JLabel();
        private JLabel guild = new JLabel(), insured = new JLabel();
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
            add(guild, statgrid);
            statgrid.gridx = 1;
            add(insured, statgrid);
            statgrid.gridx = 0;
            statgrid.gridy = 2;
            statButtons[0] = makeStatButton("Strength");
            statButtons[1] = makeStatButton("Agility");
            statButtons[2] = makeStatButton("Intelligence");
            statgrid.gridx = 1;
            statgrid.gridy = 2;
            statgrid.ipady = 32;
            for (int i=0;i<3;i++){
                final int var = i;
                JButton button = makeStatButton("");
                button.setIcon(iconDescribe);
                final Performer perf = new Performer(()->{
                    if (Global.player.stats.level.require(1))
                        stats[var].addTotal(1);
                });
                statButtons[var].addActionListener(e -> {
                    perf.run();
                    refresh();
                });
                button.addActionListener(e -> Menu.menu.showDialog(stats[var].name,Dialog.get("?"+stats[var])));
            }
            statgrid.gridx = 0;
            statgrid.gridy = stats.length+2;
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
                    Menu.menu.showDialog("Fatigue", "You require 1 fatigue every rest or spells cast.");
                }
            });
            stamina.setIcon(iconDescribe);
            stamina.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Menu.menu.showDialog("Stamina", "Most actions require Stamina.");
                }
            });
            vitality.setIcon(iconDescribe);
            vitality.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Menu.menu.showDialog("Vitality", "Vitality prevents most form of death.");
                }
            });
            energy.setIcon(iconDescribe);
            energy.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Menu.menu.showDialog("Energy", "Most spells require Energy.");
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
            level.setText("Level "+player.stats.level.getMax());
            guild.setFont(Panel.optionfont);
            guild.setForeground(Panel.fontcolor);
            guild.setText(player.getTitle());
            insured.setFont(Panel.optionfont);
            insured.setForeground(Panel.fontcolor2);
            int expiry = player.insuranceExpiry-Global.hour;
            insured.setText((!player.insuranceMap.isEmpty() && player.insuranceCost <= 0 && expiry >= 0) ? "Insured for "+expiry+" hours":"");
            motivation.setFont(Panel.smallfont);
            motivation.setForeground(Panel.fontcolor);
            motivation.setText("Motivation - "+player.stats.motivation.toString().replace(",", "/"));
            nutrition.setFont(Panel.smallfont);
            nutrition.setForeground(Panel.fontcolor);
            nutrition.setText("Nutrition - "+player.stats.nutrition.toString().replace(",", "/"));
            fatigue.setFont(Panel.smallfont);
            fatigue.setForeground(Panel.fontcolor);
            fatigue.setText("Fatigue - "+player.stats.fatigue.toString().replace(",", "/"));
            stamina.setFont(Panel.smallfont);
            stamina.setForeground(Panel.fontcolor);
            stamina.setText("Stamina - "+player.stats.strength.toString().replace(",", "/"));
            vitality.setFont(Panel.smallfont);
            vitality.setForeground(Panel.fontcolor);
            vitality.setText("Vitality - "+player.stats.agility.toString().replace(",", "/"));
            energy.setFont(Panel.smallfont);
            energy.setForeground(Panel.fontcolor);
            energy.setText("Energy - "+player.stats.intelligence.toString().replace(",", "/"));
            points.setFont(Panel.smallfont);
            points.setForeground(Color.RED);
            points.setText("Available Points: "+player.stats.level.get());
            if (player.stats.level.get() == 0) points.setVisible(false);
            else points.setVisible(true);
            for (int i=0;i<stats.length;i++) statButtons[i].setText(stats[i].name+" - "+stats[i].getMax());
            if (player.stats.strength.isFull()) stamina.setVisible(false);
            else stamina.setVisible(true);
            if (player.stats.agility.isFull()) vitality.setVisible(false);
            else vitality.setVisible(true);
            if (!player.traits.has(Traits.Trait.Affinity) || player.stats.intelligence.isFull()) energy.setVisible(false);
            else energy.setVisible(true);
            if (player.stats.motivation.get() == 0) motivation.setVisible(false);
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
            weight.setText("Encumbrance:"+Global.player.armor.getEncumbrance()+"/"+Global.player.stats.agility.getMax()+
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
            MouseAdapter adapter;
            if (type == -1) {
                if (Global.player.wieldingNone()) panel.setVisible(false);
                adapter = new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        Global.player.input.inputDirect("Wield", 0, 0);
                        Menu.menu.showMenu();
                    }
                };
            } else { //armor
                adapter = new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        Global.player.input.inputDirect("_WI"+type,0,0);
                        Menu.menu.showMenu();
                    }
                };
            }
            result.addMouseListener(adapter);
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
            drop.addMouseListener(adapter);
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
            makeSlot("_CHAT");
            for (Actions i : PlayerInput.basicMoves){
                if (i != Actions.Idle) makeSlot(i);
            }
            for (Actions i : Global.player.moveset){
                if (i != Actions.Use) makeSlot(i);
            }
            int points = Global.player.stats.intelligence.get();
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
            String restMenu = Global.player.map.isSnowing()?"Rest (Cold)":"Rest";
            panel.add(makeOption("Level Up","lvl","Level up with sufficient Motivation points and time."));
            panel.add(makeOption(restMenu,"h","Rest for an hour, removing curable debuffs and restoring fatigue. Restores double near chairs and benches, and triple near beds. Note that valuable items on ground disappear when time passes."));
            if (Global.player.stats.nutrition.get() == 0){
                if (Global.player.stats.agility.get() > 0) panel.add(makeOption("Starve", "st", "Starve, gaining 1 Nutrition point at the cost of 1 Vitality."));
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
                    int reqMot = Global.player.stats.motivation.getMax(), reqTime = 3;
                    String message = "If you have the time and motivation, you may train to make yourself stronger.";
                    message += "\nRequires "+reqMot+" Motivation points, and "+reqTime+" hours (= "+reqTime+" Nutrition and Fatigue points).";
                    message += "\n\nCurrent Motivation: "+Global.player.stats.motivation.get();
                    message += "\nCurrent Nutrition: "+Global.player.stats.nutrition.get();
                    message += "\nCurrent Fatigue: "+Global.player.stats.fatigue.get();
                    Menu.menu.showDialog("Level Up", message,
                            new String[]{"Begin Training!"},
                            new Performer[]{
                                new Performer(()->{
                                    if (Global.player.stats.motivation.get() >= reqMot){
                                        if (Global.player.map.updater.sleep(reqTime, true)) {
                                            Global.player.level();
                                            Global.player.stats.motivation.require(reqMot);
                                        }
                                    } else Global.player.say(Dialog.get("!m"));
                                })
                            });
                    break;
                case "?":
                    Menu.menu.showDialog("Help", "The controls are WASD for movement and left click to interact with the world.");
                    break;
                case "opt":
                    Menu.menu.showDialog("Options", "apparently none.");
                    break;
                case "st":
                    if (Global.player.stats.agility.require(1)){
                        Global.player.stats.nutrition.add(1, false);
                        Global.player.say("I have "+Global.player.stats.agility.get()+" Vitality left!");
                    } else Global.player.die("Starvation");
                    Menu.menu.showMenu();
                    break;
            }
        }
    }

    private class CraftPanel extends JPanel{
        private JPanel panel;
        private JScrollPane scroll;
        CraftPanel(){
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            setVisible(false);
            setBackground(Panel.forecolor2);
            panel = new JPanel();
            panel.setOpaque(false);
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.getVerticalScrollBar().setUI(new ScrollBar());
            scroll.getVerticalScrollBar().setOpaque(false);
            scroll.getVerticalScrollBar().setUnitIncrement(24);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(null);
            scroll.setOpaque(false);
            add(scroll, BorderLayout.LINE_START);
        }
        void refresh() {
            panel.removeAll();
            makeSlot("[Available Items]","?","Show all items available for crafting.");
            T slots = new T();
            for (String i : Global.player.crafting){
                T reqs = Crafts.craft(false, Global.player, i);
                T desc = new T();
                boolean craftable = true;
                assert reqs != null;
                for (T req : reqs) {
                    if (req.get(0).get().equals("Time")) desc.add(req.get(2).getInt() + " Hours");
                    else {
                        String t = req.get(0).get() + " (" + req.get(1).getInt() + "/" + req.get(2).getInt() + ")";
                        if (req.get(1).getInt() < req.get(2).getInt()) {
                            desc.add("<font color='#CC3333'>" + t + "</font>");
                            craftable = false;
                        } else desc.add(t);
                    }
                }
                String msg = i + " - " + desc.toDisplayString("Free", ", ");
                if (craftable) slots.add(0, new T().add(i).add(msg));
                else slots.add(new T().add(i).add(msg));
            }
            for (T i : slots){
                makeSlot(i.get(0).get(), i.get(0).get(), i.get(1).get());
            }
        }
        void makeSlot(String p, String cmd, String description){
            JPanel panel = new JPanel();
            panel.setOpaque(false);
            panel.setLayout(new BoxLayout(panel,BoxLayout.LINE_AXIS));
            panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
            final JLabel desc = new JLabel();
            desc.setIcon(iconDescribe);
            desc.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Menu.menu.showDialog(p, Crafts.getDescription(p));
                }
            });
            final JLabel craft = new JLabel("<html> "+description+"</html>");
            craft.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    command(cmd);
                }
            });
            panel.add(desc);
            panel.add(craft);
            this.panel.add(panel);
            this.panel.add(Box.createRigidArea(new Dimension(0,4)));
        }
        private void command(String p){
            Menu.menu.showMenu();
            switch (p) {
                case "?": {
                    T items = new T();
                    for (Prop i : Global.player.getAvailableItems()) items.add(i.name);
                    Menu.menu.showDialog("Available Items", items.toDisplayString("None", ", "));
                    break;
                }
                default:
                    Command.order("crft@" + p);
                    break;
            }
        }
    }

    private ImageIcon iconDescribe = new ImageIcon(Picture.loadCache("Info"));
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
    void command(String p){
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