package com.ga.map;

import com.ga.game.Settings;

import java.awt.*;

/**
 * Created by Ga on 9/2/2016. asd
 */
public class Weather extends Drawable{
    private static int[][] colors = new int[][]{
            new int[]{120,40,220,50},
            new int[]{150,0,150,100},
            new int[]{255,255,255,40},
            new int[]{255,255,255,0},
            new int[]{255,255,255,20},
            new int[]{255,255,255,0},
            new int[]{255,255,255,0},
            new int[]{150,0,0,50},
            new int[]{0,0,150,70},
            new int[]{0,0,0,50},
            new int[]{0,0,0,70},
            new int[]{0,0,0,70}
    };
    private Map map;
    private float fade = 0;
    private static Color color = new Color(0,0,0,0);
    private static int[] fromColor=new int[]{0,0,0,0}, toColor=new int[]{0,0,0,0};

    Weather(Map map){
        this.map = map;
    }
    private static Color addition(int[] a, int[] b, float rate){
        return new Color((int) Global.clamp(a[0]+((b[0]-a[0])*rate),0,255),
                (int) Global.clamp(a[1]+((b[1]-a[1])*rate),0,255),
                (int) Global.clamp(a[2]+((b[2]-a[2])*rate),0,255),
                (int) Global.clamp(a[3]+((b[3]-a[3])*rate),0,255));
    }
    private int[] getCurrentTimeColor(){
        return colors[Math.abs(Global.getHour()%colors.length)];
    }
    void refreshTimeColor(){
        int[] t = getCurrentTimeColor();
        fromColor = new int[]{color.getRed(),color.getGreen(),color.getBlue(),color.getAlpha()};
        toColor = t;
    }
    public void fadeIn(){
        fade = 0;
        fromColor = new int[]{0,0,0,255};
        toColor = getCurrentTimeColor();
        color = new Color(0,0,0,255);
    }
    public void fadeOut(){
        fade = 0;
        fromColor = new int[]{color.getRed(),color.getGreen(),color.getBlue(),color.getAlpha()};
        toColor = new int[]{0,0,0,255};
    }
    boolean isNight(){
        return Global.getHour() % colors.length >= 8;
    }
    public void start(){
        map.updater.addQueue.add(this);
    }
    @Override
    public void draw(Graphics2D g) {
        if (fade < 1){
            color = addition(fromColor,toColor,fade);
            fade+=.01f;
        }
        g.setColor(color);
        g.fillRect(0, 0, Settings.screenX, Settings.screenY);
        super.draw(g);
    }

    @Override
    public float getDepth() {
        return 14;
    }
}
