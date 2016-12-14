package com.ga.map;

import com.ga.game.Settings;
import com.ga.unit.Updater;

import java.awt.*;

/**
 * Created by Ga on 9/2/2016.
 */
public class Weather implements Drawable{
    static int[][] colors = new int[][]{
            new int[]{120,40,220,50},
            new int[]{150,0,150,100},
            new int[]{255,255,255,70},
            new int[]{255,255,255,0},
            new int[]{255,255,255,40},
            new int[]{255,255,255,0},
            new int[]{255,255,255,0},
            new int[]{150,0,0,50},
            new int[]{0,0,150,70},
            new int[]{25,25,100,70},
            new int[]{0,0,0,70},
            new int[]{0,0,0,70}
    };
    public static boolean visible = true;
    private static final int hourLength = 100;
    private int[] time = Global.getTime();
    public Weather(){
        Updater.addDrawee(this);
    }
    private Color addition(int[] a, int[] b, float rate){
        return new Color((int) Global.clamp(a[0]+((b[0]-a[0])*rate),0,255),
                (int) Global.clamp(a[1]+((b[1]-a[1])*rate),0,255),
                (int) Global.clamp(a[2]+((b[2]-a[2])*rate),0,255),
                (int) Global.clamp(a[3]+((b[3]-a[3])*rate),0,255));
    }
    private Color getColor(){
        int hour = (time[0]/hourLength)%colors.length;
        int nexthour = ((time[0]+hourLength)/hourLength)%colors.length;
        float minute = (float)(time[0]%hourLength)/hourLength;
        return addition(colors[hour],colors[nexthour],minute);
    }
    @Override
    public void draw(Graphics2D g) {
//        if (visible) {
//            g.setColor(getColor());
//            g.fillRect(0, 0, Settings.screenX, Settings.screenY);
//        }
    }

    @Override
    public int getDepth() {
        return 31;
    }
}
