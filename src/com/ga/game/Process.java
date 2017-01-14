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
            }
        }).start();
    }
}