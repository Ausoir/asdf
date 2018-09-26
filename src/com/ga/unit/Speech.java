package com.ga.unit;

import com.ga.game.FileManager;
import com.ga.map.Global;
import com.ga.tool.T;
import com.ga.tool.Timer;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Ga on 7/31/2016. asd
 */
public class Speech extends Thing {
    private static T reactions = FileManager.readLiterals("Data/raw/Reaction.txt");
    private static Color forecolor = new Color(30,30,30,255);
    private Unit host;
    private ArrayList<String> msg = new ArrayList<>();
    private Timer timer = new Timer(-1,false);
    private Color color = new Color(255,255,255,150);
    boolean visible = false;
    String text = "";
    Speech(Unit host){
        super(null, "Speech", null, host.seed, 15);
        this.host = host;
        pos = host.pos;
    }
    public Thing start(){
        this.map = host.map;
        return super.start();
    }
    public void draw(Graphics2D g){
        if (visible) {
            int width = g.getFontMetrics(Global.font).stringWidth(text);
            int x = pos.x()+Global.rel.x(), y = pos.y()+Global.rel.y()-150;
            int[] xp = new int[]{
                    10 + x, x, -10 + x, x-width/2-10, x-width/2-10, x+width/2+10, x+width/2+10
            };
            int[] yp = new int[]{
                    y, y + 40, y, y, y-40, y-40, y
            };
            g.setColor(color);
            g.fillPolygon(xp, yp, xp.length);
            g.setColor(forecolor);
            g.setFont(Global.font);
            g.drawString(text, x-width/2, y-15);
        }
    }
    public void say(String p){
        String t;
        if (p.startsWith("@")) t = getRandom(p.substring(1));
        else t = p;
        if (!t.equals("")) {
            if (!text.equals(t)) {
                text = t;
                msg.clear();
                msg.add(t);
                visible = true;
            }
            timer.set(120 + t.length());

            int range = p.endsWith("!!")?2400:600;
            for (Unit i : host.map.updater.unitsLoaded){
                if (i != host && i.faction != host.faction) {
                    if (Math.abs(i.pos.x - pos.x) < range && Math.abs(i.pos.y - pos.y) < range/3) {
                        i.hear(host, p.toLowerCase());
                    }
                }
            }
        }
    }
    private String getRandom(String p){
        T results = reactions.startsWith(host.speechType+":"+p);
        if (results == null) return "";
        return results.get(1).getRand(rand).get();
    }

    public void update() {
        if (msg.size() > 0) {
            if (timer.update()) {
                msg.remove(0);
                visible = false;
                text = "";
            }
        }
    }
}
