package com.ga.unit.intelligence;

import com.ga.game.FileManager;
import com.ga.map.Drawable;
import com.ga.map.Global;
import com.ga.map.Updatable;
import com.ga.tool.P;
import com.ga.tool.Timer;
import com.ga.unit.Unit;
import com.ga.unit.Updater;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ga on 7/31/2016.
 */
public class Speech extends JTextArea implements Drawable {
    public static final HashMap<String,ArrayList<String>> dialog = FileManager.readDialogue("dialog/Reaction.txt");
    private Unit host;
    private int[] xp, yp;
    public ArrayList<String> msg = new ArrayList<>();
    private Timer timer = new Timer(-1,false);
    private String recent = "";
    private Color color = new Color(255,255,255,150);
    private Font font;
    public Speech(Unit host){
        super();
        this.host = host;
        Updater.add(this);
        font = new Font("Helvetica",Font.PLAIN,18);
        setFont(font);
        setBackground(color);
        setLineWrap(true);
        setWrapStyleWord(true);
        Global.display.add(this);
        setMargin(new Insets(10,10,10,10));
        setEditable(false);
        setVisible(false);
    }
    public void remove(){
        Updater.remove(this);
        Global.display.remove(this);
    }
    public void draw(Graphics2D g){
        if (isVisible()) {
            P t = host.pos.getAdd(0,-150);
            int width = Math.min(g.getFontMetrics(font).stringWidth(getText()) + 30, 240);
            int height = (int) getPreferredSize().getHeight();
            float rx = Global.rel.x, ry = Global.rel.y;
            setBounds((int) (t.x - (width / 2) + rx), (int) (t.y - height + ry), width, height);
            g.setColor(color);
            xp = new int[]{
                    (int) (-10 + t.x+rx), (int) (10 + t.x+rx), (int) (0 + t.x+rx)
            };
            yp = new int[]{
                    (int) (t.y+ry), (int) (t.y+ry), (int) (t.y+ry) + 40
            };
            g.fillPolygon(xp, yp, xp.length);
        }
    }
    @Override
    public int getDepth() {
        return 99;
    }
    public void say(String p){
        String t;
        if (p.startsWith("@")) t = getRandom(p.substring(1));
        else t = p;
        if (!t.equals("")) {
            if (!recent.equals(p)) {
                recent = p;
                msg.clear();
                setVisible(true);
                msg.add(t);
                setText(t);
            }
            timer.set(120 + t.length());
            Updater.sound(host, p.toLowerCase());
        }
    }
    public String getRandom(String p){
        ArrayList<String> results = dialog.get(host.speechType+":"+p);
        if (results == null){
            results = dialog.get(":"+p);
            if (results == null) return "";
        }
        return results.get(Global.r.nextInt(results.size()));
    }

    public void update() {
        if (msg.size() > 0) {
            if (timer.update()) {
                msg.remove(0);
                setVisible(false);
                recent = "";
            }
        }
    }
}
