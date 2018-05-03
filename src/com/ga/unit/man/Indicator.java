package com.ga.unit.man;

import com.ga.map.Drawable;
import com.ga.map.Global;
import com.ga.unit.skill.Stat;

import java.awt.*;

/**
 * Created by ga on 4/23/17. asd
 */
public class Indicator extends Drawable {
    private Player host;
    private Color bg = new Color(58, 60, 60,0);
    private Color fg = new Color(2, 196, 0, 0);
    private Color sg = new Color(58, 60, 60,0);
    private Color sg2 = new Color(58, 60, 60,0);
    private float fade = .5f;
    private float fade2 = 0;
    private Stat measuree = new Stat(1);
    private Color[] bgFade = new Color[15];
    private Color[] fgFade = new Color[15];
    private int time;
    Indicator(Player host){
        this.host = host;
        host.map.updater.addQueue.add(this);
        for (int i=bgFade.length-1;i>=0;i--){
            bgFade[i] = new Color(58, 60, 60, i * 17);
            fgFade[i] = new Color(19, 86, 43, i * 15);
        }
        setDepth(15);
    }
    void remove(){
        host.map.updater.remove(this);
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
        int cd = time-Global.time;
        if (cd >= 0){
            g.setFont(Global.font);
            g.setColor(sg2);
            g.drawString(cd+"",x-28,y+24);
            g.setColor(sg);
            g.drawString(cd+"",x-29,y+25);
        }
    }
    void show(float strength){
        fade = strength;
    }
    void showCd(int time){
        fade2 = 2;
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
            sg2 = fgFade[(int) (t*fgFade.length)];
        }
    }
}
