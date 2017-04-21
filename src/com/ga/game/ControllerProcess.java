package com.ga.game;

import com.ga.map.Updater;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Ga on 1/20/2017.
 */
public class ControllerProcess {
    private Timer timer;
    protected ControllerProcess(){
        timer = new Timer(120, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Updater.updateControl();
            }
        });
        timer.start();
    }
    public void stop(){
        timer.stop();
    }
}