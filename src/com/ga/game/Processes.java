package com.ga.game;

import com.ga.map.Global;
import com.ga.map.Updater;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ga on 1/20/2017. asd
 */
public class Processes {
    private static Timer display, update, controllers, outside;
    Processes(){
        controllers = new Timer();
        controllers.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run(){
                for (Updater i : Global.maps) i.updateControl();
            }
        }, 0, Settings.spc);
        outside = new Timer();
        outside.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run(){
                for (Updater i : Global.maps) i.updateOutside();
            }
        }, 0, Settings.spo);
        display = new Timer();
        display.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run(){
                Global.display.repaint();
            }
        }, 0, Settings.spf);
        update = new Timer();
        update.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run(){
                for (Updater i : Global.maps) i.update();
            }
        }, 0, Settings.spu);
    }
    public static void editorMode(){
        outside.cancel();
        controllers.cancel();
    }
}