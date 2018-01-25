package com.ga.game;

/**
 * Created by Ga on 7/31/2016.
 */
public interface Input {
    void input(int mode, int x, int y);
    void chat(String p, int x, int y);
    void onFocus(boolean focus);
}
