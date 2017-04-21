package com.ga.game;

import com.ga.map.Global;
import com.ga.map.Picture;
import com.ga.unit.Prop;
import com.ga.unit.Tag;
import com.ga.unit.Tags;
import com.ga.unit.item.Crafts;
import com.ga.unit.item.Item;
import com.ga.unit.player.Inventory;
import com.ga.unit.player.Player;
import com.ga.unit.skill.Actions;
import com.ga.unit.skill.Moveset;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Created by Ga on 8/6/2016. asd
 */
public class Menu extends Panel {
    private class StatPanel extends JPanel{
        private GridBagConstraints statgrid = new GridBagConstraints();
        private String[][] stats = new String[][]{
                new String[]{Dialog.get("prf"),Dialog.get("prfDsc")},
                new String[]{Dialog.get("str"),Dialog.get("strDsc")},
                new String[]{Dialog.get("sta"),Dialog.get("staDsc")},
                new String[]{Dialog.get("end"),Dialog.get("endDsc")},
                new String[]{Dialog.get("mem"),Dialog.get("memDsc")},
                new String[]{Dialog.get("lvlPts"),Dialog.get("lvlPtsDsc")},
                new String[]{Dialog.get("rstPts"),Dialog.get("rstPtsDsc")}
        };
        private JButton[] statButtons = new JButton[stats.length];
        private JButton[] incButtons = new JButton[stats.length];
        private JButton[] decButtons = new JButton[stats.length];
        StatPanel(){
            setLayout(new GridBagLayout());
            setBackground(Panel.forecolor2);
            statgrid.anchor = GridBagConstraints.LINE_START;
            statgrid.fill = GridBagConstraints.BOTH;
            statgrid.ipadx = 120;
            statgrid.ipady = 20;
            statgrid.gridx = 0;
            statgrid.gridy = 0;
            for (int i=0;i<stats.length;i++) statButtons[i] = makeStatButton(stats[i][0],null);
            statgrid.ipadx = 5;
            statgrid.gridx = 1;
            statgrid.gridy = 0;
            for (int i=0;i<stats.length;i++) incButtons[i] = makeStatButton("[+]", "level@"+stats[i][0]);
            statgrid.gridx = 2;
            statgrid.gridy = 0;
            for (int i=0;i<stats.length;i++) decButtons[i] = makeStatButton("[-]", "delevel@"+stats[i][0]);
            statgrid.gridx = 3;
            statgrid.gridy = 0;
            for (String[] i : stats) makeStatButton("", "menuDialog@"+i[0]+"@"+i[1]).setIcon(iconDescribe);
        }
        void refresh(){
            Player player = Global.player;
            for (int i=0;i<stats.length;i++) statButtons[i].setText(stats[i][0]+" - "+player.get(stats[i][0]).getMax());
            if (player.get("Level Points").get() > 0) for (JButton i : incButtons) i.setVisible(true);
            else for (JButton i : incButtons) i.setVisible(false);
            if (player.get("Reset Points").get() > 0) for (JButton i : decButtons) i.setVisible(true);
            else for (JButton i : decButtons) i.setVisible(false);
            incButtons[5].setVisible(false);
            incButtons[6].setVisible(false);
            decButtons[5].setVisible(false);
            decButtons[6].setVisible(false);
        }
        JButton makeStatButton(String p, final String cmd){
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

    private class EquipmentPanel extends JTextArea{
        private GridBagConstraints grid = new GridBagConstraints();
        private JPanel panel;
        private JScrollPane scroll;
        private JLabel gold;
        private Border emptyborder = new EmptyBorder(5,8,5,8);
        EquipmentPanel(){
            setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
            gold = new JLabel();
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
            add(gold, BorderLayout.LINE_START);
            add(scroll, BorderLayout.LINE_START);
        }
        void refresh(){
            gold.setText("Gold: "+Global.player.gold);
            panel.removeAll();

            grid.gridy = 0;
            Item wielded = Global.player.weapon;
            makeSlot(-1, wielded);
            TitledBorder b = BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(2,2,2,2,Panel.forecolor));
            b.setTitle(Dialog.get("wielded"));
            ((JPanel) panel.getComponent(0)).setBorder(b);

            for (int i=0;i<3;i++) {
                if (!Global.player.armor.isBare(i)) {
                    grid.gridy = 1+i;
                    Item equipped = Global.player.armor.armor[i];
                    JPanel panel = makeSlot(i, equipped);
                    b = BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Panel.forecolor));
                    String title = Dialog.get("equipped");
                    if (i == 0) title += " - "+Dialog.get("bhead");
                    else if (i == 1) title += " - "+Dialog.get("btorso");
                    else if (i == 2) title += " - "+Dialog.get("bleg");
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
            if (type == -1){
                if (Global.player.wieldingNone()) panel.setVisible(false);
                result.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        Global.player.input.inputDirect("AWield",0,0);
                        Global.display.menu.setVisible(false);
                    }
                });
            } else { //armor
                result.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        Global.player.input.inputDirect("I1"+type,0,0);
                        Global.display.menu.setVisible(false);
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
                    Global.display.menuDialog.show(item.getFullName(),item.getDescription());
                }
            });
            final JLabel drop = new JLabel(item.getFullName()+"             ");
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
        private JLabel gold;
        private Border emptyborder = new EmptyBorder(5,8,5,8);
        InventoryPanel(){
            setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
            gold = new JLabel();
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
            add(gold, BorderLayout.LINE_START);
            add(scroll, BorderLayout.LINE_START);
        }
        void refresh(){
            gold.setText("Gold: "+Global.player.gold);
            panel.removeAll();
            Inventory inv = Global.player.inventory;
            int itemsize = inv.getItemSize();
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
                    Global.player.input.inputDirect("I"+index,0,0);
                    Global.display.menu.setVisible(false);
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
                    Global.display.menuDialog.show(item.getFullName(),item.getDescription());
                }
            });
            final JLabel drop = new JLabel(item.getFullName()+"             ");
            drop.setForeground(item.getRarityColor());
            options.add(describe);
            options.add(drop);
            panel.add(options);
            panel.add(result);
            this.panel.add(panel);
        }
    }

    private class SkillPanel extends JPanel{
        private JScrollPane scroll;
        private JPanel panel;
        private JLabel skillpoints;
        SkillPanel(){
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
            panel.add(new JLabel(Dialog.get("mvsRemap")));
            panel.add(Box.createRigidArea(new Dimension(0,12)));
            makeSlot(Actions.Use, false);
            makeSlot(Actions.Kick, false);
            makeSlot(Actions.Rest, false);
            for (Moveset i : Global.player.skillTree.skills){
                makeSlot(i, true);
            }
            int points = Global.player.get("Memory").get();
            skillpoints.setText(Dialog.get("mem")+": "+points);
        }
        void makeSlot(final Moveset move, boolean skill){
            final String displayName = Dialog.get("mvs"+move.getName());
            String key = Global.player.input.getMapping(move);
            if (key == null) key = "";
            else key = "["+key+"]";
            JPanel panel = new JPanel();
            panel.setOpaque(false);
            panel.setLayout(new BoxLayout(panel,BoxLayout.LINE_AXIS));
            panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
            final JLabel advance = new JLabel(" [+ Proficiency]");
            if (!Global.player.skillTree.advanceAble(move)) advance.setVisible(false);
            if (skill) {
                int points = Global.player.get("Memory").get();
                if (points <= 0) {
                    advance.setVisible(false);
                }
                advance.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        Global.player.say("Leveled up " + displayName + "!");
                        Global.player.advanceSkill(move);
                    }
                });
                panel.add(advance);
            }
            final JLabel describe = new JLabel(iconDescribe);
            describe.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Command.order("menuDialog@"+move.getName()+"@"+move.getDescription());
                }
            });
            int level = Global.player.skillTree.getSkillLevel(move);
            final JLabel equip = new JLabel(displayName+" "+Dialog.get("lvl")+" "+level);
            final JLabel keyLabel = new JLabel(" "+key+" ");
            keyLabel.setForeground(Color.RED);
            equip.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    describe.setVisible(false);
                    advance.setVisible(false);
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
                        advance.setVisible(true);
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
                    advance.setVisible(true);
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

    private class ActionPanel extends JPanel{
        private JPanel panel;
        private JPanel other;
        ActionPanel(){
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
            panel.add(makeOption("Inspect Self","stat","Check current status, stats, and injuries."));
            panel.add(makeOption("Rest","h","Rest for an hour, removing curable debuffs and consuming available food. Shortcut is ',' key."));
            panel.add(makeOption("Sleep","zzz","Sleep for six hours."));
            TitledBorder b = BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(2,2,2,2,Panel.forecolor));
            b.setTitle("Default");
            panel.setBorder(b);
            add(panel, BorderLayout.CENTER);

            other.removeAll();
            other.add(makeOption("Other","opt","Do stuff."));
            other.add(makeOption("Help","?","Noob"));
            b = BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(2,2,2,2,Panel.forecolor));
            b.setTitle("Other");
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
                    Global.display.menuDialog.show(p,description);
                }
            });
            subpanel.setOpaque(false);
            subpanel.setBorder(BorderFactory.createMatteBorder(1,1,1,1,Panel.forecolor));
            subpanel.add(button);
            subpanel.add(desc);
            return subpanel;
        }
        private void command(String p){
            Global.display.menu.setVisible(false);
            switch (p) {
                case "h":
                    Command.order("rest");
                    break;
                case "stat":
                    Player player = Global.player;
                    String statusDsc = "Stamina:" + player.get("Stamina") + "\nEncumberance:" + player.getStatPenalty() +
                        "\nMetabolism:" + player.health.getStatusDescription() + "\nMotivation:" + player.get("Motivation")+"\n";
                    statusDsc += "\nCurrent Status Effects:\n" + player.stats.getEffectsList();
                    Global.display.menuDialog.show("Current Status", statusDsc);
                    break;
                case "etc":
                    Global.display.menuDialog.show("Options", "apparently none.");
                    break;
            }
        }
    }

    private class TrainingPanel extends JPanel{
        private GridBagConstraints statgrid = new GridBagConstraints();
        private String[][] stats = new String[][]{
                new String[]{Dialog.get("ath"),Dialog.get("athDsc")},
                new String[]{Dialog.get("jmp"),Dialog.get("jmpDsc")},
                new String[]{Dialog.get("swg"),Dialog.get("swgDsc")},
                new String[]{Dialog.get("agl"),Dialog.get("aglDsc")},
                new String[]{Dialog.get("mrt"),Dialog.get("mrtDsc")},
                new String[]{Dialog.get("cst"),Dialog.get("cstDsc")},
                new String[]{Dialog.get("chn"),Dialog.get("chnDsc")}
        };
        private JPanel learning;
        private JButton learningButton;
        private JProgressBar learningProgress;
        private JButton[] statButtons = new JButton[stats.length];
        TrainingPanel(){
            setLayout(new GridBagLayout());
            setBackground(Panel.forecolor2);
            statgrid.anchor = GridBagConstraints.LINE_START;
            statgrid.fill = GridBagConstraints.BOTH;
            statgrid.ipadx = 120;
            statgrid.ipady = 20;
            statgrid.gridx = 0;
            statgrid.gridy = 0;
            statgrid.gridwidth = 2;
            learning = new JPanel();
            learning.setOpaque(false);
            add(learning,statgrid);
            statgrid.gridwidth = 1;
            statgrid.gridy = 1;
            for (int i=0;i<stats.length;i++) statButtons[i] = makeStatButton(this,stats[i][0],null);
            statgrid.gridx = 1;
            statgrid.gridy = 1;
            for (String[] i : stats){
                makeStatButton(this,"", "menuDialog@"+i[0]+"@"+i[1]).setIcon(iconDescribe);
            }
        }
        void refresh(){
            learning.removeAll();
            if (Global.player.learning != null) {
                TitledBorder b = BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Panel.forecolor));
                b.setTitle("Currently Learning");
                learning.setBorder(b);
                String title = Global.player.learning.toString();
                learningButton = makeStatButton(learning, title, "menuDialog@"+title+"@"+Global.player.learning.getDescription());
                learningButton.setIcon(iconDescribe);
                learningProgress = new JProgressBar(0,Global.player.learning.getDifficulty()*100);
                learningProgress.setBackground(Panel.fontcolor);
                Tags fields = Global.player.learning.getTags().getTrainers();
                int total = 0;
                for (Tag i : fields){
                    total += Global.player.get(i.name()).get();
                }
                learningProgress.setValue((total*100)+Global.player.get("Progress").get());
                learningProgress.setStringPainted(true);
                learning.add(learningProgress);
            }
            for (int i=0;i<stats.length;i++) statButtons[i].setText(stats[i][0]+" - "+Global.player.get(stats[i][0]).get());
        }
        JButton makeStatButton(JPanel panel, String p, final String cmd){
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
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Command.order(cmd);
                    refresh();
                }
            });
            panel.add(button, statgrid);
            statgrid.gridy++;
            return button;
        }
    }

    private class CraftPanel extends JPanel{
        private ArrayList<String> items;
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
            for (Prop i : Global.player.getAvailableItems()) items.add(i.name);
            panel.removeAll();
            panel.add(makeOption("[Available Items]","?","Show all items available for crafting."));
            for (String i : Global.player.crafting.knowledge){
                ArrayList<String> recipe = Crafts.recipes.get(i);
                if (items.containsAll(recipe)) {
                    String t = "";
                    for (String ii : recipe) t += ii+", ";
                    craftables.add(i);
                    panel.add(makeOption(i, i, "Craft a " + i + " using ["+t.substring(0,t.length()-2)+"]"));
                }
            }
            TitledBorder b = BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(2,2,2,2,Panel.forecolor));
            b.setTitle("Craftable");
            panel.setBorder(b);
            add(panel, BorderLayout.CENTER);

            other.removeAll();
            for (String i : Global.player.crafting.knowledge){
                if (!craftables.contains(i)) {
                    String t = "";
                    for (String ii : Crafts.recipes.get(i)) t += ii + ", ";
                    other.add(makeOption(i, i, "Crafting "+i+" requires ["+t.substring(0, t.length() - 2)+"]"));
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
                    command(cmd);
                }
            });
            JLabel desc = new JLabel("");
            desc.setIcon(iconDescribe);
            desc.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Global.display.menuDialog.show(p,description);
                }
            });
            subpanel.setOpaque(false);
            subpanel.setBorder(BorderFactory.createMatteBorder(1,1,1,1,Panel.forecolor));
            subpanel.add(button);
            subpanel.add(desc);
            return subpanel;
        }
        private void command(String p){
            Global.display.menu.setVisible(false);
            if (p.equals("?")){
                String t = "";
                for (String i : items) t += i + ", ";
                String result = t.equals("")?"None":t.substring(0,t.length()-2);
                Global.display.menuDialog.show("Available Items",result);
            } else {
                Command.order("crft@"+p);
            }
        }
    }

    private ImageIcon iconDescribe = new ImageIcon(Picture.load("Describe"));
    private GridBagConstraints grid = new GridBagConstraints();
    private JPanel mainpanel = new JPanel();
    private ActionPanel actionpanel = new ActionPanel();
    private StatPanel statspanel = new StatPanel();
    private EquipmentPanel equipmentPanel = new EquipmentPanel();
    private InventoryPanel inventorypanel = new InventoryPanel();
    private SkillPanel skillpanel = new SkillPanel();
    private TrainingPanel trainpanel = new TrainingPanel();
    private CraftPanel craftpanel = new CraftPanel();
    Menu() {
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
        add(actionpanel,grid);
        add(statspanel,grid);
        add(equipmentPanel,grid);
        add(inventorypanel,grid);
        add(skillpanel,grid);
        add(trainpanel,grid);
        add(craftpanel,grid);
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
        GridLayout gridlayout = new GridLayout(7,1,20,36);
        mainpanel.setOpaque(false);
        mainpanel.setLayout(gridlayout);
        mainpanel.add(makeOption(Dialog.get("act"), "act"));
        mainpanel.add(makeOption(Dialog.get("stats"), "stats"));
        mainpanel.add(makeOption(Dialog.get("equip"), "equip"));
        mainpanel.add(makeOption(Dialog.get("inv"), "inventory"));
        mainpanel.add(makeOption(Dialog.get("mvs"), "spell"));
        mainpanel.add(makeOption(Dialog.get("train"), "train"));
        mainpanel.add(makeOption(Dialog.get("crft"), "craft"));
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
        equipmentPanel.setVisible(false);
        inventorypanel.setVisible(false);
        skillpanel.setVisible(false);
        actionpanel.setVisible(false);
        trainpanel.setVisible(false);
        craftpanel.setVisible(false);
        switch (p) {
            case "act":
                actionpanel.setVisible(true);
                break;
            case "stats":
                statspanel.setVisible(true);
                break;
            case "equip":
                equipmentPanel.setVisible(true);
                break;
            case "inventory":
                inventorypanel.setVisible(true);
                break;
            case "spell":
                skillpanel.setVisible(true);
                break;
            case "train":
                trainpanel.setVisible(true);
                break;
            case "craft":
                craftpanel.setVisible(true);
                break;
        }
    }
    @Override
    public void setVisible(boolean p){
        super.setVisible(p);
        Global.display.focus = true;
    }
    public void toggle(){
        if (isVisible()) setVisible(false);
        else{
            display();
        }
    }
    public void display(){
        super.setVisible(true);
        if (Global.player != null){
            super.display();
            setBackground(new Color(120,120,120,180));
            actionpanel.refresh();
            statspanel.refresh();
            inventorypanel.refresh();
            skillpanel.refresh();
            equipmentPanel.refresh();
            trainpanel.refresh();
            craftpanel.refresh();
        }
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