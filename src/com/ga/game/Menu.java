package com.ga.game;

import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.tool.Word;
import com.ga.unit.Actions;
import com.ga.unit.Moveset;
import com.ga.unit.item.Inventory;
import com.ga.unit.item.Item;
import com.ga.unit.Player;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
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
    private SkillPanel skillpanel = new SkillPanel();
    private HelpPanel helppanel = new HelpPanel();
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
        add(skillpanel,grid);
        add(helppanel,grid);
        add(optionspanel,grid);
        command("");
    }
    private void addMainPanel(){
        GridLayout gridlayout = new GridLayout(5,1,20,40);
        mainpanel.setOpaque(false);
        mainpanel.setLayout(gridlayout);
        mainpanel.add(makeMenuButton("Stats", "stats"));
        mainpanel.add(makeMenuButton("Inventory", "inventory"));
        mainpanel.add(makeMenuButton("Skills", "skill"));
        mainpanel.add(makeMenuButton("Help", "help"));
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
        skillpanel.setVisible(false);
        helppanel.setVisible(false);
        optionspanel.setVisible(false);
        if (p.equals("stats")) statspanel.setVisible(true);
        else if (p.equals("inventory")) inventorypanel.setVisible(true);
        else if (p.equals("skill")) skillpanel.setVisible(true);
        else if (p.equals("help")) helppanel.setVisible(true);
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
            skillpanel.refresh();
            helppanel.refresh();
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
        ((JButton)getComponent(0)).setText("Damage - "+player.get("damage").getMax());
        ((JButton)getComponent(1)).setText("Strength - "+player.get("arms").getMax());
        ((JButton)getComponent(2)).setText("Stamina - "+player.get("legs").getMax());
        ((JButton)getComponent(3)).setText("Memory - "+player.get("damage").getMax());
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
    private JScrollPane scroll;
    private JLabel gold;
    private Border emptyborder = new EmptyBorder(5,8,5,8);
    private int[] selected;
    private boolean mRelease = true;
    private int prevY = 0;
    public InventoryPanel(){
        setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
        setOpaque(false);
        setBorder(BorderFactory.createBevelBorder(1));
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
        gold = new JLabel();
        panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
        scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUI(new ScrollBar());
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);
        add(gold, BorderLayout.LINE_START);
    }
    public void refresh(){
        gold.setText("Gold: "+Global.player.host.gold);
        panel.removeAll();
        Inventory inv = Global.player.host.inventory;
        int itemsize = inv.getItemSize();
        selected = new int[itemsize];
        for (int i = 0; i < itemsize; i++) {
            Item item = inv.getItem(i);
            grid.gridy = i;
            makeSlot(item.getFullName(), item.image, i, item.getRarity());
        }
        for (int i = 0; i < inv.pockets.length; i++) {
            int slot = inv.pockets[i];
            if (slot != -1) {
                equip(slot);
            }
        }
        if (inv.wielded != -1) equip(inv.wielded);
        if (inv.worn != -1) equip(inv.worn);
    }
    public void makeSlot(final String p, Image img, final int item, int rarity){
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
        panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        final JLabel result = new JLabel();
        result.setIcon(new ImageIcon(img));
        result.setHorizontalTextPosition(JLabel.CENTER);
        result.setVerticalTextPosition(JLabel.TOP);
        result.setBorder(emptyborder);
        result.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (selected[item] == 0) equip(item);
                else unequip(item);
            }
        });
        JPanel options = new JPanel();
        options.setOpaque(false);
        options.setLayout(new BoxLayout(options,BoxLayout.LINE_AXIS));
        final JLabel describe = new JLabel(" [?]");
        describe.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                describe(item);
            }
        });
        final JLabel drop = new JLabel(p);
        drop.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                drop(item);
            }
        });
        drop.setForeground(getRarityColor(rarity));
        drop.setIcon(new ImageIcon(Texture.load("item/Drop.png")));
        options.add(describe);
        options.add(drop);
        panel.add(options);
        panel.add(result);
        this.panel.add(panel);
    }
    private Color getRarityColor(int rarity){
        Color result = Color.black;
        switch (rarity){
            case 1:
                result = Color.GRAY;
                break;
            case 2:
                result = Color.DARK_GRAY;
                break;
            case 3:
                result = Color.GREEN;
                break;
            case 4:
                return Color.ORANGE;
            case 5:
                return Color.MAGENTA;
        }
        if (rarity >= 100){
            result = Color.BLUE;
        }
        return result;
    }
    private void describe(int p){
        Global.player.host.describeItem(p);
    }
    private void drop(int p){
        unequip(p);
        Global.player.host.dropItem(p);
    }
    public void equip(int p){
        final Inventory inv = Global.player.host.inventory;
        int r = inv.equip(p);
        for (int i = 0; i < selected.length; i++) {
            if (i != p && r == selected[i]) unequip(i);
        }
        TitledBorder b = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder());
        if (r == 1) b.setTitle("Wielded");
        else if (r == 2) b.setTitle("Equipped");
        else if (r >= 10) b.setTitle(""+(r-9));
        ((JPanel) panel.getComponent(p)).setBorder(b);
        selected[p] = r;
    }
    public void unequip(int p){
        Global.player.host.inventory.unequip(p);
        ((JPanel) panel.getComponent(p)).setBorder(emptyborder);
        selected[p] = 0;
    }
}

class SkillPanel extends JPanel{
    private JScrollPane scroll;
    private JPanel panel;
    public SkillPanel(){
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBorder(BorderFactory.createBevelBorder(1));
        setOpaque(false);
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
        add(scroll, BorderLayout.CENTER);
    }
    protected void refresh(){
        panel.removeAll();
        panel.add(new JLabel("Select a skill to re-map its keybinding."));
        panel.add(Box.createRigidArea(new Dimension(0,12)));
        makeSlot(Actions.Weapon);
        makeSlot(Actions.Jump);
        makeSlot(Actions.Kick);
        makeSlot(Actions.Drop);
        makeSlot(Actions.Rest);
        for (Moveset i : Global.player.host.skillTree.getSkills()){
            makeSlot(i);
        }
    }
    protected void makeSlot(final Moveset move){
        String key = Global.player.getMapping(move);
        if (key == null) key = "";
        else key = "["+key+"]";
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel,BoxLayout.LINE_AXIS));
        panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        final JLabel describe = new JLabel(" [?]");
        describe.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Global.player.host.say(move.getDescription());
            }
        });
        final JLabel equip = new JLabel(" "+key+" "+Word.wordify(move.getName()));
        equip.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                describe.setVisible(false);
                equip.setText("Press a Key");
                equip.requestFocus();
            }
        });
        equip.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (equip.hasFocus()) {
                    Global.player.changeMapping(e.getButton(), move);
                    Global.display.requestFocus();
                    describe.setVisible(true);
                    refresh();
                }
            }
        });
        equip.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                Global.player.changeMapping(e.getKeyCode(), move);
                Global.display.requestFocus();
                describe.setVisible(true);
                refresh();
            }
        });
        panel.add(describe);
        panel.add(equip);
        this.panel.add(panel);
        this.panel.add(Box.createRigidArea(new Dimension(0,4)));
    }
}

class HelpPanel extends JTextArea{
    public HelpPanel(){
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setOpaque(false);
        setVisible(false);
    }
    protected void refresh(){
        setText("This help section is not feeling helpful.");
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