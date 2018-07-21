package com.ga.map;

import com.ga.connection.Server;
import com.ga.game.Display;
import com.ga.game.FileManager;
import com.ga.game.Processes;
import com.ga.game.Settings;
import com.ga.tool.EventTimer;
import com.ga.tool.P;
import com.ga.tool.Timer;
import com.ga.unit.Traits;
import com.ga.unit.man.MainPlayer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by Ga on 7/25/2016. asd
 */
public class Global {
    public static ArrayList<Map> maps = new ArrayList<>();
    public static MainPlayer player;
    public static Server server;
    public static Processes cprocess;
    public static P camera = new P();
    public static P cameraTo = new P();
    public static Display display;
    public static Random r = new Random();
    public static P rel = new P();
    private static float[] sinv = new float[360];
    private static float[] cosv = new float[360];
    public static int time, hour;
    public static Timer updates = new Timer(40,true);
    public static ArrayList<EventTimer> timers = new ArrayList<>();
    public static Font font = new Font("Helvetica",Font.PLAIN,20);
    public static int controlId;
    public static float scale = 1;

    public static void init(){
        server = new Server();
        for (int i=0;i<360;i++){
            sinv[i] = (float)((int)(Math.sin(Math.toRadians(i))*10000))/10000;
            cosv[i] = (float)((int)(Math.cos(Math.toRadians(i))*10000))/10000;
        }
    }
    static void updateCamera(){
        camera.add(player.map.mapEdgeCenter(cameraTo).getSubtract(camera.getAdd(0,40)).mult(.1f,.1f));
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
    public static void p(String p){
        System.out.println(p);
    }
    public static void update(){
        if (updates.update()) {
            timePass(1);
            FileManager.saveCharacter();
            FileManager.saveWorld();
        }
    }
    public static int getControlId(){
        return controlId++;
    }
    public static void timePass(int p){
        time+=p;
        Iterator it = timers.iterator();
        while (it.hasNext()){
            EventTimer timer = (EventTimer) it.next();
            if (timer.update(p)) it.remove();
        }
    }
    public static void hourPass(int p){
        Global.player.get("Intelligence").add(Math.max(0, Global.player.traits.getTraitLevel(Traits.Trait.Attunement)-1), false);
        Global.player.get("Strength").heal();
        timePass(p*100);
        hour+=p;
        for (Map i : maps) i.onTimeChange();
    }
    public static boolean inCamera(float x, float y, float sizeX, float sizeY){
        return (x > Global.camera.x-Settings.centerX-sizeX && y > Global.camera.y-Settings.centerY-sizeY
                && x < Global.camera.x+Settings.centerX+sizeX && y < Global.camera.y+Settings.centerY+sizeY);
    }

    public static Map getMap(String name){
        for (Map i : maps){
            if (i.name.equals(name)) return i;
        }
        Map r = new Map(name);
        maps.add(r);
        r.start();
        return r;
    }
}
