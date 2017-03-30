package com.ga.tool;

/**
 * Created by Ga on 7/26/2016.
 */
public class EventTimer extends Timer {
    public Performer performer;
    public EventTimer(int time, boolean repeat, Performer performer) {
        super(time, repeat);
        this.performer = performer;
    }
    public boolean update(){
        boolean result = super.update();
        if (result && performer != null){
            Performer orig = performer;
            performer.perform();
            if (performer == orig) performer = null;
        }
        return result;
    }
}
