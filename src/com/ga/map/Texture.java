package com.ga.map;

import com.ga.game.FileManager;
import com.ga.tool.P;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ga on 1/10/2017.
 */
public abstract class Texture {
    public BufferedImage image;
    public boolean visible = true;
    protected P size;
    protected static int transparentColor = new Color(0,0,0,0).getRGB();
    protected static HashMap<String,BufferedImage> cache = new HashMap<>();
    protected static HashMap<String,String> directory = new HashMap<>();
    public Texture(){
        visible = false;
    }
    public Texture(BufferedImage image){
        set(image);
    }
    public Texture(String name){
        this(load(name));
    }
    public void set(BufferedImage image){
        this.image = image;
        size = new P(image.getWidth(), image.getHeight());
    }
    public P getSize(){
        return size;
    }
    public static BufferedImage load(String dir){
        BufferedImage image = cache.get(dir);
        if (image != null) return image;
        try {
            String file = directory.get(dir);
            if (file == null) file = "Data/"+dir+".png";
            image = ImageIO.read(new File(file));
            transparency(image);
            cache.put(dir,image);
        } catch (IOException e) {
            System.out.println("Can't read file ["+dir + "]");
            e.printStackTrace();
        }
        return image;
    }
    private static void transparency(BufferedImage p) {
        int sizeX = p.getWidth();
        int sizeY = p.getHeight();
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                int rgb = p.getRGB(x, y);
                int r = (rgb & 0xff0000) >>> 16;
                int g = (rgb & 0xff00) >>> 8;
                int b = (rgb & 0xff);
                if ((r == 0xff && g == 0 && b == 0xff) ||
                        (r == 0xff && g == 0x66 && b == 0xff)) p.setRGB(x,y, transparentColor);
            }
        }
    }
    static BufferedImage getTexture(BufferedImage image, int sx, int sy, int px, int py) {
        return image.getSubimage(px * sx, py * sy, sx, sy);
    }
    public static void init(){
        for (String[] i : FileManager.fileCache.get("png")) directory.put(i[0], i[1]);
        for (String[] i : FileManager.fileCache.get("gif")) directory.put(i[0], i[1]);
        directory.put("Mantis_skin","Data/unit/Spider_skin.png");
        cache.put("Drop",getTexture(load("Icon"),24,24,0,0));
        cache.put("Buy",getTexture(load("Icon"),24,24,1,0));
        cache.put("Describe",getTexture(load("Icon"),24,24,2,0));
        cache.put("POI",getTexture(load("Icon"),24,24,3,0));
    }
}
