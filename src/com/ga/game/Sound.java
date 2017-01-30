package com.ga.game;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;

/**
 * Created by Ga on 1/25/2017.
 */
public class Sound {
    private static HashMap<String,Clip> cache = new HashMap<>();
    public static void init(){
        search("Data/");
    }
    public static boolean has(String p){
        return cache.get(p) != null;
    }
    public static void play(String p){
        try {
            final Clip clip = cache.get(p);
            clip.setFramePosition(0);
            clip.start();
        } catch (Exception e){
            e.printStackTrace();
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
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            return clip;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private static void search(String p){
        File[] files = new File(p).listFiles();
        for (File file : files) {
            String path = file.getPath();
            if (file.isDirectory()) {
                search(path);
            } else if (file.isFile()){
                String t = file.getName();
                if (t.endsWith(".wav")){
                    try {
                        cache.put(t.substring(0,t.lastIndexOf(".")),makeAudio(path));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
