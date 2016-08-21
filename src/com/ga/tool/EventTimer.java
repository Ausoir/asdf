package com.ga.tool;

/**
 * Created by Ga on 7/26/2016.
 */
public class EventTimer extends Timer {
    public Performer performer;
    public EventTimer(Performer performer, int time, boolean repeat) {
        super(time, repeat);
        this.performer = performer;
    }
    public boolean update(){
        boolean result = super.update();
        if (result && performer != null) performer.perform();
        return result;
    }
}
