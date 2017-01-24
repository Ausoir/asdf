package com.ga.game;

import com.ga.unit.Updater;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Ga on 1/20/2017.
 */
public class ControllerProcess {
    public ControllerProcess(){
        new Timer(300, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Updater.updateControl();
            }
        }).start();
    }
}