package com.ga.intelligence;

import com.ga.game.FileManager;
import com.ga.map.Drawable;
import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.tool.Timer;
import com.ga.unit.Man;
import com.ga.unit.Updater;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ga on 7/31/2016.
 */
public class Speech implements Drawable{
    public static final HashMap<String,ArrayList<String>> dialogue = FileManager.readDialogue("Dialog.dlg");
    private Man host;
    private int[] xp, yp;
    private int w = 300, h = 120;
    public ArrayList<String> msg = new ArrayList<>();
    private Timer timer = new Timer(-1,false);
    private String recent = "";
    private Color color = new Color(255,255,255,150);
    public Speech(Man host){
        this.host = host;
        Updater.addDrawee(this);
    }
    public void draw(Graphics2D g){
        if (msg.size() > 0) {
            g.setColor(color);
            P t = Global.getRelPos(host.pos.x, host.pos.y);
            int x = (int) t.x - w + 25;
            int y = (int) t.y - h - 60;
            String txt = msg.get(0);
            w = (txt.length() * 11)+80;
            h = (txt.length() / 40)+80;
            xp = new int[]{
                    x, x + w, x + w, x + w - 50, x + w - 50, x + (w / 2), x
            };
            yp = new int[]{
                    y, y, y + h, y + h, y + h + 50, y + h, y + h
            };
            g.fillPolygon(xp, yp, xp.length);
            g.setColor(Color.black);
            g.setFont(new Font("Comic Sans MS",Font.BOLD,18));
            if (!txt.equals("")) g.drawString(txt,x+25,y+(h/3)+8);
            if (timer.update()) msg.remove(0);
        }
    }

    @Override
    public int getDepth() {
        return 99;
    }

    public void say(String p){
        if (!recent.equals(p)) {
            recent = p;
            msg.clear();
            String t = "";
            if (p.startsWith("@")) t = getRandom(p.substring(1));
            else t = p;
            if (!t.equals("")) {
                msg.add(t);
                timer.set(200);
            }
        }
    }
    public String getRandom(String p){
        ArrayList<String> results = dialogue.get(host.speechType+":"+p);
        if (results == null) return "";
        return results.get(Global.r.nextInt(results.size()));
    }
}
