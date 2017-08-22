package com.ga.game;

import com.ga.map.Updater;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Ga on 1/20/2017. asd
 */
public class OtherProcess {
    private Timer controllers, outside;
    OtherProcess(){
        controllers = new Timer(360, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Updater.updateControl();
            }
        });
        controllers.start();
        outside = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Updater.updateOutside();
            }
        });
        outside.start();
    }
}