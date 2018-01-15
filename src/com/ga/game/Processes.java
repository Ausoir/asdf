package com.ga.game;

import com.ga.map.Global;
import com.ga.map.Map;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ga on 1/20/2017. asd
 */
public class Processes {
    private static Timer display, update, controllers;
    Processes(){
        resume();
    }
    public static void pause(){
        update.cancel();
        display.cancel();
        controllers.cancel();
    }
    public static void resume(){
        display = new Timer();
        update = new Timer();
        controllers = new Timer();
        controllers.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run(){
                ArrayList<Map> maps = (ArrayList<Map>) Global.maps.clone();
                for (Map i : maps) i.updater.updateControl();
            }
        }, 300, Settings.spc);
        display.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run(){
                Global.display.repaint();
            }
        }, 200, Settings.spf);
        update.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run(){
                ArrayList<Map> maps = (ArrayList<Map>) Global.maps.clone();
                for (Map i : maps) i.updater.update();
                Global.update();
            }
        }, 100, Settings.spu);
    }
    public static void editorMode(){
        controllers.cancel();
    }
}