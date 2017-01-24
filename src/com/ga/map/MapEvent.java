package com.ga.map;

import com.ga.game.Observer;
import com.ga.tool.Performer;
import com.ga.tool.Timer;
import com.ga.unit.Updater;

/**
 * Created by Ga on 10/31/2016.
 */
public class MapEvent implements Updatable {
    private enum Map{
        None(new Performer(){
            public void perform(){
            }
        }),
        Town(new Performer(){
            public void perform(){
            }
        }),
        City(new Performer(){
            public void perform(){
            }
        });
        private Performer event;
        Map (Performer p){
            event = p;
        }
        private void update(){
            event.perform();
        }
    }
    private Map event;
    private Timer timer;
    public MapEvent(String p){
        event = Map.valueOf(p);
        Updater.add(this);
        timer = new Timer(100, true);
    }
    @Override
    public void update() {
        if (timer.update()) event.update();
    }
}