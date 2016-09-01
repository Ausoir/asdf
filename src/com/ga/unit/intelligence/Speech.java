package com.ga.unit.intelligence;

import com.ga.game.Display;
import com.ga.game.FileManager;
import com.ga.map.Drawable;
import com.ga.map.Global;
import com.ga.tool.P;
import com.ga.tool.Timer;
import com.ga.unit.Man;
import com.ga.unit.Updater;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ga on 7/31/2016.
 */
public class Speech extends JTextArea implements Drawable{
    public static final HashMap<String,ArrayList<String>> dialogue = FileManager.readDialogue("Dialog.dlg");
    private Man host;
    private int[] xp, yp;
    private int w = 300, h = 120;
    public ArrayList<String> msg = new ArrayList<>();
    private Timer timer = new Timer(-1,false);
//    private String recent = "";
    private Color color = new Color(255,255,255,150);
    private Font font;
    public Speech(Man host){
        super();
        this.host = host;
        Updater.addDrawee(this);
        font = new Font("Comic Sans MS",Font.BOLD,18);
        setFont(font);
        setBackground(color);
        setLineWrap(true);
        setWrapStyleWord(true);
        Global.display.add(this);
        setMargin(new Insets(10,10,10,10));
        setEditable(false);
    }
    public void draw(Graphics2D g){
        if (msg.size() > 0) {
            P t = Global.getRelPos(host.pos.x, host.pos.y);
            int width = Math.min(g.getFontMetrics(font).stringWidth(getText())+30, 240);
            int height = (int) getPreferredSize().getHeight();
            setBounds((int) t.x-(width/2), (int) t.y-80-height, width, height);
            g.setColor(color);
            xp = new int[]{
                    (int) (-10+t.x), (int) (10+t.x), (int) (0+t.x)
            };
            yp = new int[]{
                    (int) (t.y)-80, (int) (t.y)-80, (int) (t.y)-40
            };
            g.fillPolygon(xp, yp, xp.length);
            if (timer.update()){
                msg.remove(0);
                setVisible(false);
            }
        }
    }

    @Override
    public int getDepth() {
        return 99;
    }
    public boolean isSpeaking(){
        return msg.size() > 0;
    }
    public void say(String p){
        String t;
        if (p.startsWith("@")) t = getRandom(p.substring(1));
        else t = p;
        if (!t.equals("")) {
            msg.clear();
            setVisible(true);
            msg.add(t);
            timer.set(120+t.length());
            Updater.sound(host,p.toLowerCase());
            setText(t);
        }
    }
    public String getRandom(String p){
        ArrayList<String> results = dialogue.get(host.speechType+":"+p);
        if (results == null){
            results = dialogue.get(":"+p);
            if (results == null) return "";
        }
        return results.get(Global.r.nextInt(results.size()));
    }
}
