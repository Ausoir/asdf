package com.ga.tool;

/**
 * Created by Ga on 7/26/2016. asdas
 */
public class EventTimer extends Timer {
    public Performer performer;
    public EventTimer(int time, boolean repeat, Performer performer) {
        super(time, repeat);
        this.performer = performer;
    }
    @Override
    public boolean update(int p){
        boolean result = super.update(p);
        if (result && performer != null){
            Performer orig = performer;
            performer.perform();
            if (performer == orig) performer = null;
        }
        return result;
    }
}
