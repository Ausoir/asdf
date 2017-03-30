package com.ga.map;

import com.ga.connection.Server;
import com.ga.game.ControllerProcess;
import com.ga.game.Display;
import com.ga.game.Settings;
import com.ga.tool.EventTimer;
import com.ga.tool.P;
import com.ga.unit.MainPlayer;
import com.ga.unit.Updater;
import com.ga.unit.intelligence.PlayerInput;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by Ga on 7/25/2016.
 */
public class Global {
    public static Map map;
    public static MainPlayer player;
    public static Server server;
    public static ControllerProcess cprocess;
    public static P camera = new P();
    public static P cameraTo = new P();
    public static Display display;
    public static Random r = new Random();
    public static int margin = 0;
    public static P rel = new P();
    public static int gameHour = 0;
    private static float[] sinv = new float[360];
    private static float[] cosv = new float[360];
    private static int[] time = new int[]{0};
    public static int objectId = 1;
    public static int houseId = 0;
    public static int thouseId = -1;
    public static ArrayList<EventTimer> timers = new ArrayList<>();
    public static void init(){
        for (int i=0;i<360;i++){
            sinv[i] = (float)Math.sin(Math.toRadians(i));
            cosv[i] = (float)Math.cos(Math.toRadians(i));
        }
        Updater.unitList.put(0,null);
    }
    public static int getSeed(){
        return r.nextInt(9999)+1;
    }
    public static void setRel(){
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
    public static float rng(float max){
        return clamp((float)r.nextGaussian(),-max,max);
    }
    public static void p(String p){
        System.out.println(p);
    }
    public static int mapEdge(P p){
        int result = 1;
        if (p.x < 0) result = 2;
        else if (p.x > Global.map.size.x) result = 4;
        if (p.y < 0) result = 3;
        else if (p.y > Global.map.size.y) result = 9;
        return result;
    }
    public static void mapEdgeCenter(P p){
        if (Global.map.size.x > Settings.screenX && Global.map.size.y > Settings.screenY)
            p.set(clamp(p.x, Settings.centerX+margin, Global.map.size.x-Settings.centerX-margin),
                clamp(p.y, Settings.centerY+40+margin, Global.map.size.y-Settings.centerY-margin-margin));
    }
    public static int checkCooldown(int[] p, int cd){
        return cd-(time[0]-p[0]);
    }
    public static boolean cooldown(int[] p, int cd){
        if (time[0] - p[0] >= cd){
            p[0] = time[0];
            return true;
        }
        return false;
    }
    public static void update(){
        time[0]++;
        Iterator it = timers.iterator();
        while (it.hasNext()){
            EventTimer timer = (EventTimer) it.next();
            if (timer.update()) it.remove();
        }
    }
    public static void setTime(int p){
        time[0] = p;
    }
    public static int[] getTime(){
        return time;
    }
    public static void setHour(int p){
        player.onTimeChange(p-gameHour);
        map.onTimeChange();
        gameHour = p;
    }
    public static int getObjectId(){
        return objectId++;
    }
    public static int getHouseId(){
        return houseId++;
    }
    public static int getTempHouseId(){
        return --thouseId;
    }
}
