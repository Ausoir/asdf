package com.ga.game;

import com.ga.map.Updater;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Ga on 12/30/2016. asd
 */
class Process {
    Process(){
        new Timer(25, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Updater.update();
            }
        }).start();
    }
}