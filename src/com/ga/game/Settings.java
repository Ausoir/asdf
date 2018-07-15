package com.ga.game;

/**
 * Created by Ga on 7/26/2016.
 */
public class Settings {
    public static int receiverPort;
    public static int senderPort;
    public static int tcpDownloadPort;
    public static int tcpUploadPort;
    public static int screenX;
    public static int screenY;
    public static int centerX;
    public static int centerY;
    public static int gravity;
    public static int gravityMax;
    static int fps;
    static int ups;
    static int spc;
    static int spo;
    static int volumn;
    static void init(){
        String[] lines = FileManager.read("Data/raw/Settings.txt").split("%");
        for (String i : lines){
            String[] line = i.split(":");
            int value = Integer.parseInt(line[1]);
            switch (line[0]){
                case "receiverPort":
                    receiverPort = value;
                    break;
                case "senderPort":
                    senderPort = value;
                    break;
                case "tcpDownloadPort":
                    tcpDownloadPort = value;
                    break;
                case "tcpUploadPort":
                    tcpUploadPort = value;
                    break;
                case "screenX":
                    screenX = value;
                    centerX = screenX/2;
                    break;
                case "screenY":
                    screenY = value;
                    centerY = screenY/2;
                    break;
                case "gravity":
                    gravity = value;
                    break;
                case "gravityMax":
                    gravityMax = value;
                    break;
                case "fps":
                    fps = value;
                    break;
                case "ups":
                    ups = value;
                    break;
                case "spc":
                    spc = value;
                    break;
                case "spo":
                    spo = value;
                    break;
                case "volumn":
                    volumn = value;
                    break;
            }
        }
    }
    static void setScreenSize(int x, int y){
        screenX = x;
        screenY = y;
        centerX = x/2;
        centerY = y/2;
    }
}
