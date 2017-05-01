package com.ga.game;

/**
 * Created by Ga on 7/26/2016.
 */
public class Settings {
    public static final int latency = 2;
    public static int receiverPort = 1992;
    public static int senderPort = 1992;
    public static final int tcpPort = 2016;
    public static int screenX = 1024;
    public static int screenY = 720;
    public static int centerX = screenX/2;
    public static int centerY = screenY/2;
    public static final int gravity = 4;
    public static final int gravityMax = 64;
    public static final float friction = .05f;
    public static void setScreenSize(int x, int y){
        screenX = x;
        screenY = y;
        centerX = x/2;
        centerY = y/2;
    }
}
