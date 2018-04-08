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
    public static boolean paused = false;
    Processes(){
        resume();
    }
    public static void pause(){
        paused = true;
        update.cancel();
        controllers.cancel();
    }
    public static void resume(){
        paused = false;
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
            public void run(){ //Same speed same thread = no stutter
                ArrayList<Map> maps = (ArrayList<Map>) Global.maps.clone();
                for (Map i : maps) i.updater.update();
                Global.update();
            }
        }, 100, Settings.spu);
//        new Thread(() -> {
//            int fps = Settings.spu;
//            long timeTaken = 0, last_frame_time, current_frame_time = System.currentTimeMillis();
//            while (!paused)
//            {
//                last_frame_time = current_frame_time;
//                current_frame_time = System.currentTimeMillis();
//                if (timeTaken >= fps) {
//                    ArrayList<Map> maps = (ArrayList<Map>) Global.maps.clone();
//                    for (Map i : maps) i.updater.update();
//                    Global.update();
//                    timeTaken = current_frame_time - last_frame_time;
//                } else {
//                    try {
//                        Thread.sleep(fps-timeTaken);
//                        timeTaken = fps;
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();
    }
    public static void editorMode(){
        controllers.cancel();
    }
}