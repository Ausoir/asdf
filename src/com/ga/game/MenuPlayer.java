package com.ga.game;

import com.ga.map.Global;
import com.ga.map.Picture;
import com.ga.tool.Performer;
import com.ga.unit.PlayerInput;
import com.ga.unit.Prop;
import com.ga.unit.Tag;
import com.ga.unit.Tags;
import com.ga.unit.item.Crafts;
import com.ga.unit.item.Item;
import com.ga.unit.man.Inventory;
import com.ga.unit.man.Player;
import com.ga.unit.skill.Actions;

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
public class MenuPlayer extends Panel {
    private class StatPanel extends JPanel{
        private GridBagConstraints statgrid = new GridBagConstraints();
        private String[] stats = new String[]{
                "Strength", "Dexterity", "Stamina", "Intelligence", "Wisdom"
        };
        private JButton[] statButtons = new JButton[stats.length];
        private JLabel level = new JLabel();
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
            add(points, statgrid);
            statgrid.gridx = 0;
            add(level, statgrid);
            statgrid.gridy = 1;
            for (int i=0;i<stats.length;i++) statButtons[i] = makeStatButton(stats[i]);
            statgrid.gridx = 1;
            statgrid.gridy = 1;
            for (int i=0;i<stats.length;i++){
                final int var = i;
                JButton button = makeStatButton("");
                button.setIcon(iconDescribe);
                final Performer perf = new Performer() {
                    public void perform() {
                        if (Global.player.get("Level").require(1))
                            Global.player.get(stats[var]).setMax(Global.player.get(stats[var]).getMax() + 1);
                    }
                };
                statButtons[var].addActionListener(e -> {
                    perf.perform();
                    refresh();
                });
                button.addActionListener(e -> {
                    if (Global.player.get("Level").get() > 0) {
                        Global.display.menu.showDialog(stats[var], Dialog.get(stats[var] + "Dsc"),
                                new String[]{"Increase Stat"}, new Performer[]{perf});
                        refresh();
                    } else Global.display.menu.showDialog(stats[var], Dialog.get(stats[var] + "Dsc"));
                });
            }
        }
        void refresh(){
            Player player = Global.player;
            level.setFont(Panel.font);
            level.setForeground(Panel.fontcolor);
            level.setText("Level "+player.get("Level").getMax() + " ");
            points.setFont(Panel.smallfont);
            points.setForeground(Color.RED);
            points.setText("Available Points "+player.get("Level").get());
            for (int i=0;i<stats.length;i++) statButtons[i].setText(stats[i]+" - "+player.get(stats[i]).getMax());
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
            int maxEncumbrance = Global.player.get("Dexterity").get();
            gold.setText("Cash: "+Dialog.money(Global.player.gold) +
                    "                 Encumbrance:"+(maxEncumbrance-Global.player.getVacancy())+"/"+maxEncumbrance+
                    "                 Weight:"+Global.player.getWeight()+"/"+Global.player.get("Strength").get()*2);
            panel.removeAll();

            grid.gridy = 0;
            Item wielded = Global.player.weapon;
            makeSlot(-1, wielded);
            TitledBorder b = BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(2,2,2,2,Panel.forecolor));
            b.setTitle(Dialog.get("wielded"));
            ((JPanel) panel.getComponent(0)).setBorder(b);

            for (int i=0;i<4;i++) {
                if (!Global.player.armor.isBare(i)) {
                    grid.gridy++;
                    Item equipped = Global.player.armor.armor[i];
                    JPanel panel = makeSlot(i, equipped);
                    b = BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Panel.forecolor));
                    String title = Dialog.get("equipped");
                    if (i == 0) title += " - "+Dialog.get("bhead");
                    else if (i == 1) title += " - "+Dialog.get("bleg");
                    else if (i == 2) title += " - "+Dialog.get("btorso");
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
                        Global.display.menu.showMenu();
                    }
                });
            } else { //armor
                result.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        Global.player.input.inputDirect("_WI1"+type,0,0);
                        Global.display.menu.showMenu();
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
                    Global.display.menu.showDialog(item.getFullName(),item.getDescription());
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
            gold.setText("Cash: "+Dialog.money(Global.player.gold)+
                    "                 Weight:"+Global.player.getWeight()+"/"+Global.player.get("Strength").get()*2);
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
                    Global.player.input.inputDirect("_WI"+(10+index),0,0);
                    Global.display.menu.showMenu();
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
                    Global.display.menu.showDialog(item.getFullName(),item.getDescription());
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
            for (Actions i : PlayerInput.basicMoves){
                if (i != Actions.Idle) makeSlot(i);
            }
            for (Actions i : Global.player.moveset) makeSlot(i);
            int points = Global.player.get("Intelligence").get();
            skillpoints.setText(Dialog.get("mem")+": "+points);
        }
        void makeSlot(final Actions move){
            final String displayName = Dialog.get("mvs"+move.getName());
            String key = Global.player.input.getMapping(move);
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
                    Global.display.menu.showDialog(move.getName(), move.getDescription());
                }
            });
            int level = Global.player.getStateLevel(move);
            final JLabel equip = new JLabel(displayName+" "+Dialog.get("pwr")+" "+level);
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
            panel.add(makeOption("Current Status","stat","Check current status, stats, and injuries."));
            panel.add(makeOption("Inspect Self","dsc","Your general description."));
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
                    Global.display.menu.showDialog(p,description);
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
                    Global.player.map.updater.sleep(100);
                    break;
                case "dsc":
                    Global.display.menu.onUpdate = new Performer(){
                        public void perform(){
                            Global.display.menu.showDialog("My Description", Global.player.getDescription());
                        }
                    };
                    Global.display.menu.onUpdate.perform();
                    break;
                case "stat":
                    Global.display.menu.onUpdate = new Performer(){
                        public void perform(){
                            String statusDsc = "Stamina:" + Global.player.get("Stamina") +
                                    "\nTiredness:" + Global.player.get("Endurance") +
                                    "\nMotivation:" + Global.player.get("Motivation")+"\n";
                            statusDsc += "\nCurrent Status Effects:\n" + Global.player.stats.getEffectsList();
                            Global.display.menu.showDialog("Current Status", statusDsc);
                        }
                    };
                    Global.display.menu.onUpdate.perform();
                    break;
                case "opt":
                    Global.display.menu.showDialog("Options", "apparently none.");
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
            panel.add(makeOption("[Available Structures]","?s","Show all structures available for crafting."));
            for (String i : Global.player.crafting.knowledge){
                ArrayList<String> recipe = new ArrayList<>();
                ArrayList<String> origRecipe = Crafts.recipes.get(i);
                for (String ii : origRecipe) recipe.add(ii.split(":")[0]);
                items.add("Time");
                if (items.containsAll(recipe)) {
                    String t = "";
                    for (String ii : origRecipe) t += ii+", ";
                    craftables.add(i);
                    panel.add(makeOption(i, i, "Craft " + i + " using ["+t.substring(0,t.length()-2)+"]"));
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
                    Global.display.menu.showDialog(p,description);
                }
            });
            subpanel.setOpaque(false);
            subpanel.setBorder(BorderFactory.createMatteBorder(1,1,1,1,Panel.forecolor));
            subpanel.add(button);
            subpanel.add(desc);
            return subpanel;
        }
        private void command(String p){
            Global.display.menu.showMenu();
            if (p.equals("?")) {
                String t = "";
                for (String i : items) {
                    if (!i.equals("Time") && !i.equals("Fist")) t += i + ", ";
                }
                String result = t.equals("") ? "None" : t.substring(0, t.length() - 2);
                Global.display.menu.showDialog("Available Items", result);
            } else if (p.equals("?s")){
                String t = "";
                for (String i : props) {
                    t += i + ", ";
                }
                String result = t.equals("") ? "None" : t.substring(0, t.length() - 2);
                Global.display.menu.showDialog("Available Structures", result);
            } else {
                Command.order("crft@"+p);
            }
        }
    }

    private ImageIcon iconDescribe = new ImageIcon(Picture.loadCache("Describe"));
    private GridBagConstraints grid = new GridBagConstraints();
    private JPanel mainpanel = new JPanel();
    private ActionPanel actionpanel = new ActionPanel();
    private StatPanel statspanel = new StatPanel();
    private EquipmentPanel equipmentPanel = new EquipmentPanel();
    private InventoryPanel inventorypanel = new InventoryPanel();
    private SkillPanel skillpanel = new SkillPanel();
    private CraftPanel craftpanel = new CraftPanel();
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
        add(actionpanel,grid);
        add(statspanel,grid);
        add(equipmentPanel,grid);
        add(inventorypanel,grid);
        add(skillpanel,grid);
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
            case "craft":
                craftpanel.setVisible(true);
                break;
        }
    }
    void toggle(){
        if (isVisible()){
            setVisible(false);
        } else {
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