package com.ga.tool;

/**
 * Created by Ga on 8/12/2016. asd
 */
public class Performer {
    private Runnable runner;
    public Performer(Runnable p) {
        runner = p;
    }

    public void run(){
        if (runner != null) runner.run();
    }
}
