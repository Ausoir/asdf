package com.ga.map;

import com.ga.game.Settings;

import java.awt.*;

/**
 * Created by Ga on 9/2/2016. asd
 */
public class Weather implements Drawable{
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
    private static Color color = new Color(0,0,0,0);
//    private Color addition(int[] a, int[] b, float rate){
//        return new Color((int) Global.clamp(a[0]+((b[0]-a[0])*rate),0,255),
//                (int) Global.clamp(a[1]+((b[1]-a[1])*rate),0,255),
//                (int) Global.clamp(a[2]+((b[2]-a[2])*rate),0,255),
//                (int) Global.clamp(a[3]+((b[3]-a[3])*rate),0,255));
//    }
    protected void setColor(){
        int[] t = colors[Global.gameHour%colors.length];
        color = new Color(t[0],t[1],t[2],t[3]);
    }
    boolean isNight(){
        return Global.gameHour % colors.length >= 8;
    }
    @Override
    public void draw(Graphics2D g) {
        g.setColor(color);
        g.fillRect(0, 0, Settings.screenX, Settings.screenY);
    }

    @Override
    public int getDepth() {
        return 31;
    }
}
