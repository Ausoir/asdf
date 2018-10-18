package com.ga.game;

import com.ga.map.Global;
import com.ga.map.Map;

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
        display.cancel();
        controllers.cancel();
    }
    public static void resume(){
        paused = false;
        display = new Timer();
        update = new Timer();
        controllers = new Timer();
        controllers.scheduleAtFixedRate(new TimerTask() {
            private int turn = 0;
            @Override
            public void run(){
                for (Map i : Global.maps){
                    if (turn < i.updater.controllee.size())
                        i.updater.controllee.get(turn++).update();
                    else turn = 0;
                }
            }
        }, 300, Settings.spc);
        display.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run(){
                Global.display.repaint();
            }
        }, 200, (int) ((1f/Settings.fps)*1000));
        update.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run(){ //Same speed same thread = no stutter
                for (Map i : Global.maps) i.updater.update();
                Global.update();
            }
        }, 100, (int) ((1f/Settings.ups)*1000));
//        new Thread(() -> {
//            int spf = (int) ((1f/Settings.ups)*1000);
//            long timeTaken = 0, last_frame_time, current_frame_time = System.currentTimeMillis();
//            while (!paused)
//            {
//                last_frame_time = current_frame_time;
//                current_frame_time = System.currentTimeMillis();
//                if (timeTaken >= spf) {
//                    ArrayList<Map> maps = (ArrayList<Map>) Global.maps.clone();
//                    for (Map i : maps) i.updater.update();
//                    Global.update();
//                    timeTaken = current_frame_time - last_frame_time;
//                } else {
//                    try {
//                        long reserve = spf-timeTaken;
//                        Thread.sleep(reserve);
//                        timeTaken = spf;
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