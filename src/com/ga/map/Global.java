package com.ga.map;

import com.ga.connection.Server;
import com.ga.game.ControllerProcess;
import com.ga.game.Display;
import com.ga.game.FileManager;
import com.ga.game.Settings;
import com.ga.tool.EventTimer;
import com.ga.tool.P;
import com.ga.tool.Performer;
import com.ga.tool.Timer;
import com.ga.unit.Unit;
import com.ga.unit.player.MainPlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by Ga on 7/25/2016. asd
 */
public class Global {
    private static final int HOUR = 100;
    public static Map map;
    public static MainPlayer player;
    public static Server server;
    public static ControllerProcess cprocess;
    public static P offplace = new P(); //Off screen while existing
    public static P camera = new P();
    public static P cameraTo = new P();
    public static Display display;
    public static Random r = new Random();
    static int margin = 0;
    public static P rel = new P();
    private static float[] sinv = new float[360];
    private static float[] cosv = new float[360];
    public static int time;
    private static int controlId = 1;
    public static int houseId = 1;
    public static Timer updates = new Timer(40,true);
    public static ArrayList<EventTimer> timers = new ArrayList<>();

    public static void init(){
        for (int i=0;i<360;i++){
            sinv[i] = (float)((int)(Math.sin(Math.toRadians(i))*10000))/10000;
            cosv[i] = (float)((int)(Math.cos(Math.toRadians(i))*10000))/10000;
        }
    }
    static void setRel(){
        rel.set(-camera.x+Settings.centerX, -camera.y+Settings.centerY);
    }
    public static float sin(float p){
        return sin((int)Math.toDegrees(p));
    }
    public static float cos(float p){
        return cos((int)Math.toDegrees(p));
    }
    public static float sin(int p){
        int angleCircle = Math.abs(p % 360);
        return p >= 0 ? sinv[angleCircle] : -sinv[angleCircle];
    }
    public static float cos(int p){
        int angleCircle = Math.abs(p % 360);
        return cosv[angleCircle % 360];
    }
    public static float clamp(float p, float min, float max){
        return Math.max(Math.min(p,max),min);
    }
    public static int nextGaussian(int p){
        return (int) (r.nextGaussian()*p);
    }
    static float rng(float max){
        return clamp((float)r.nextGaussian(),-max,max);
    }
    public static void p(String p){
        System.out.println(p);
    }
    public static int mapEdge(P p){
        int result = 0;
        if (p.x < 0) result = 4;
        else if (p.x > Global.map.size.x) result = 6;
        if (p.y < 0) result = 8;
        else if (p.y > Global.map.size.y) result = 2;
        return result;
    }
    public static void mapEdgeCenter(P p){
        if (Global.map.size.x > Settings.screenX && Global.map.size.y > Settings.screenY)
            p.set(clamp(p.x, Settings.centerX+margin, Global.map.size.x-Settings.centerX-margin),
                clamp(p.y, Settings.centerY+40+margin, Global.map.size.y-Settings.centerY-margin-margin));
    }
    public static void update(){
        if (updates.update()) {
            timePass(1);
            FileManager.saveCharacter();
        }
    }
    static void timePass(int p){
        time+=p;
        if (p != 1 || time % HOUR == 0) map.onTimeChange();
        Iterator it = timers.iterator();
        while (it.hasNext()){
            EventTimer timer = (EventTimer) it.next();
            if (timer.update(p)) it.remove();
        }
    }
    static int getHour(){
        return time/HOUR;
    }
    public static int getControlId() {
        return controlId++;
    }
}
