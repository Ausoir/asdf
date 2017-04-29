package com.ga.unit.player;

import com.ga.map.Drawable;
import com.ga.map.Global;
import com.ga.map.Updater;
import com.ga.unit.skill.Stat;

import java.awt.*;

/**
 * Created by ga on 4/23/17. asd
 */
public class Indicater implements Drawable {
    private Player host;
    private Color bg = new Color(58, 60, 60,0);
    private Color fg = new Color(2, 179, 0, 0);
    private Color sg = new Color(58, 60, 60,0);
    private float fade = 0;
    private float fade2 = 0;
    private Stat measuree = new Stat(1);
    private Color[] bgFade = new Color[20];
    private Color[] fgFade = new Color[20];
    private int time;
    Indicater(Player host){
        this.host = host;
        Updater.add(this);
        for (int i=bgFade.length-1;i>=0;i--){
            bgFade[i] = new Color(58, 60, 60, i * 12);
            fgFade[i] = new Color(19, 86, 43, i * 9);
        }
    }
    void setMeasuree(Stat p){
        measuree = p;
    }
    public void draw(Graphics2D g) {
        int x = (int) (host.pos.x + Global.rel.x);
        int y = (int) (host.pos.y + Global.rel.y);
        g.setColor(bg);
        g.fillOval(x,y,36,36);
        g.setColor(fg);
        g.fillArc(x+2,y+2,32,32,0, (int) (measuree.getPercentage()*360));
        g.setColor(sg);
        int cd = time-Global.getTime()[0];
        if (cd >= 0) g.drawString(cd+"",x-28,y);
    }
    @Override
    public int getDepth() {
        return 150;
    }
    void show(float strength){
        fade = strength;
    }
    void showCd(int time){
        fade2 = 1;
        this.time = time;
    }
    void update(){
        if (fade > 0) {
            fade = Math.max(0, fade - .01f);
            float t = Math.min(.99f, fade);
            bg = bgFade[(int) (t*bgFade.length)];
            fg = fgFade[(int) (t*fgFade.length)];
        }
        if (fade2 > 0){
            fade2 = Math.max(0, fade2 - .01f);
            float t = Math.min(.99f, fade2);
            sg = bgFade[(int) (t*bgFade.length)];
        }
    }
}
