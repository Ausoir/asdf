package com.ga.map;

import com.ga.game.FileManager;
import com.ga.tool.P;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Ga on 1/10/2017. asd
 */
public abstract class Texture {
    public BufferedImage image;
    public boolean visible = true;
    protected P size;
    private static HashMap<String,BufferedImage> cache = new HashMap<>();
    private static HashMap<String,String> directory = new HashMap<>();
    Texture(){
        visible = false;
    }
    Texture(BufferedImage image){
        set(image);
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
        String file = directory.get(dir);
        if (file != null) {
            try {
                image = ImageIO.read(new File(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
            cache.put(dir, image);
        } else System.out.println("Can't read file ["+dir + "]");
        return image;
    }
    static BufferedImage getTexture(BufferedImage image, int sx, int sy, int px, int py) {
        return image.getSubimage(px * sx, py * sy, sx, sy);
    }
    static void newImage(String path, String p, BufferedImage image){
        String fullPath = path+p+".png";
        FileManager.save(fullPath,"");
        File output = new File("Data/"+fullPath);
        try {
            ImageIO.write(image, "png", output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        cache.put(p,image);
    }
    public static void init(){
        for (String[] i : FileManager.fileCache.get("png")) directory.put(i[0], i[1]);
        directory.put("Mantis_skin","Data/unit/Spider_skin.png");
        cache.put("Drop",getTexture(load("Icon"),24,24,0,0));
        cache.put("Buy",getTexture(load("Icon"),24,24,1,0));
        cache.put("Describe",getTexture(load("Icon"),24,24,2,0));
        cache.put("POI",getTexture(load("Icon"),24,24,3,0));
    }
}
