package com.ga.unit;

import com.ga.game.FileManager;
import com.ga.map.Drawable;
import com.ga.map.Global;
import com.ga.map.Updater;
import com.ga.tool.P;
import com.ga.tool.Timer;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ga on 7/31/2016. asd
 */
public class Speech extends Drawable {
    private Unit host;
    private ArrayList<String> msg = new ArrayList<>();
    private Timer timer = new Timer(-1,false);
    private String recent = "";
    private Color color = new Color(255,255,255,150);
    private JTextArea textarea;
    Speech(Unit host){
        this.host = host;
        host.map.updater.addQueue.add(this);
        textarea = new JTextArea();
        textarea.setFont(Global.font);
        textarea.setBackground(color);
        textarea.setLineWrap(true);
        textarea.setWrapStyleWord(true);
        Global.display.add(textarea);
        textarea.setMargin(new Insets(10,10,10,10));
        textarea.setEditable(false);
        textarea.setVisible(false);
        setDepth(15);
    }
    public void remove(){
        host.map.updater.remove(this);
        Global.display.remove(textarea);
    }
    public void draw(Graphics2D g){
        if (textarea.isVisible()) {
            P t = host.pos.getAdd(0,-150);
            int width = Math.min(g.getFontMetrics(Global.font).stringWidth(textarea.getText()) + 30, 240);
            int height = (int) textarea.getPreferredSize().getHeight();
            float rx = Global.rel.x, ry = Global.rel.y;
            textarea.setBounds((int) (t.x - (width / 2) + rx), (int) (t.y - height + ry), width, height);
            g.setColor(color);
            int[] xp = new int[]{
                    (int) (-10 + t.x + rx), (int) (10 + t.x + rx), (int) (0 + t.x + rx)
            };
            int[] yp = new int[]{
                    (int) (t.y + ry), (int) (t.y + ry), (int) (t.y + ry) + 40
            };
            g.fillPolygon(xp, yp, xp.length);
        }
    }
    public void say(String p){
        String t;
        if (p.startsWith("@")) t = getRandom(p.substring(1));
        else t = p;
        if (!t.equals("")) {
            if (!recent.equals(p)) {
                recent = p;
                msg.clear();
                textarea.setVisible(true);
                msg.add(t);
                textarea.setText(t);
            }
            timer.set(120 + t.length());

            for (Unit i : host.map.updater.unitsLoaded){
                if (i != host && i.faction != host.faction) {
                    if (Math.abs(i.pos.x - host.pos.x) < 600 && Math.abs(i.pos.y - host.pos.y) < 200) {
                        i.hear(host, p.toLowerCase());
                    }
                }
            }
        }
    }
    private String getRandom(String p){
        HashMap<String,ArrayList<String>> dialog = FileManager.readDialogue("raw/Reaction.txt"); //static cache?
        ArrayList<String> results = dialog.get(host.speechType+":"+p);
        if (results == null){
            results = dialog.get(":"+p);
            if (results == null) return "";
        }
        return results.get(Global.r.nextInt(results.size()));
    }

    public JTextArea getTextarea() {
        return textarea;
    }

    public void update() {
        if (msg.size() > 0) {
            if (timer.update()) {
                msg.remove(0);
                textarea.setVisible(false);
                recent = "";
            }
        }
    }
}
