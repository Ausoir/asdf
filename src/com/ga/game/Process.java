package com.ga.game;

import com.ga.map.Global;
import com.ga.unit.Updater;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Ga on 12/30/2016.
 */
public class Process {
    private long lastTime = System.currentTimeMillis();
    private int timeDebt = -100;

    public Process(){
        new Timer(25, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Updater.update();
                try {
                    long t = (long) Global.clamp(System.currentTimeMillis()-lastTime,0,30);
//            if (t >= 30){
//                timeDebt += t-30;
//                t = 30;
//            } else if (timeDebt > 0){
//                timeDebt -= 30-t;
//                t = 30;
//            }
                    Thread.sleep(30-t);
                    lastTime = System.currentTimeMillis();
                } catch (Exception e) {
                    System.out.println("Thread Error");
                    System.exit(1);
                }
            }
        }).start();
    }
}