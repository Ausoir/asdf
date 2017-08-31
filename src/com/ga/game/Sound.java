package com.ga.game;

import com.ga.map.Global;
import com.ga.map.Map;
import com.ga.tool.P;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;

/**
 * Created by Ga on 1/25/2017. asd
 */
public class Sound {
    private static HashMap<String,Clip> cache = new HashMap<>();
    static void init(){
        for (String[] i : FileManager.fileCache.get("wav")) {
            try {
                cache.put(i[0], makeAudio(i[1]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static boolean has(String p){
        return cache.get(p) != null;
    }
    public static void play(Map map, P location, String p){
        if (map == Global.player.map) {
            P dist = Global.camera.getSubtract(location);
            if (Math.abs(dist.x) < Settings.centerX && Math.abs(dist.y) < Settings.centerY) {
                try {
                    final Clip clip = cache.get(p);
                    clip.setFramePosition(0);
                    clip.start();
                } catch (Exception e) {
                    System.out.println("Could not play sound [" + p + "]");
                    e.printStackTrace();
                }
            }
        }
    }
    private static Clip makeAudio(String p) {
        File file = new File(p);
        AudioInputStream ais = null;
        try {
            try {
                ais = AudioSystem.getAudioInputStream(file);
                byte[] buffer = new byte[1024];
                int read;
                ByteArrayOutputStream baos = new ByteArrayOutputStream(buffer.length);
                while ((read = ais.read(buffer, 0, buffer.length)) != -1) {
                    baos.write(buffer, 0, read);
                }
                ais = new AudioInputStream(new ByteArrayInputStream(baos.toByteArray()), ais.getFormat(), AudioSystem.NOT_SPECIFIED);
            } finally {
                if (ais != null) ais.close();
            }
            Clip clip = AudioSystem.getClip(null);
            clip.open(ais);
            return clip;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
