package com.ga.game;

import com.ga.map.Global;
import com.ga.tool.P;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Ga on 5/6/2016.
 */
public class Texture {
    private BufferedImage image;
    private BufferedImage shade;
    public P size;
    public boolean visible = true;
    public int sizeRadius;
    private static HashMap<String,BufferedImage> cache = new HashMap<>();
    private static HashMap<String,String> directory = new HashMap<>();
    private static int transparentColor = new Color(0,0,0,0).getRGB();

    public Texture(){
        visible = false;
    }
    private Texture(BufferedImage image){
        this.image = image;
        size = new P(image.getWidth(), image.getHeight());
        sizeRadius = (int) size.getMult(.5f,.5f).getDist();
    }
    public Texture(String name){
        this(load(name));
    }
    public Texture(String name, int index){
        this(getTexture(load(name),32,64,index,0));
    }
    private static BufferedImage clone(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(bi.getRaster().createCompatibleWritableRaster());
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
    public BufferedImage getImage(){
        return image;
    }
    public static void init(){
        search("Data/");
        directory.put("Mantis_skin","Data/unit/Spider_skin.png");
        cache.put("Drop",getTexture(load("Icon"),24,24,0,0));
        cache.put("Buy",getTexture(load("Icon"),24,24,1,0));
        cache.put("Describe",getTexture(load("Icon"),24,24,2,0));
    }
    public void draw(Graphics2D g, float x, float y, float rot, int dir, float posX, float posY){
        if (visible) {
            float xMid = size.x*dir/2;
            float yMid = size.y/2;
            P t = Global.getRelPos(x - xMid, y - yMid);
            P r = t.getAdd(xMid, yMid);
            g.rotate(rot, r.x, r.y);
            g.drawImage(image, (int) t.x, (int) t.y, (int) posX * dir, (int) posY, null);
            if (shade != null){
                g.drawImage(shade, (int) t.x, (int) t.y, (int) posX * dir, (int) posY, null);
            }
            g.rotate(-rot, r.x, r.y);
        }
    }
    public void draw(Graphics2D g, float x, float y, float rot, int dir){
        draw(g,x,y,rot,dir,size.x,size.y);
    }
    private static void search(String p){
        File[] files = new File(p).listFiles();
        for (File file : files) {
            String path = file.getPath();
            if (file.isDirectory()) {
                search(path);
            } else if (file.isFile()){
                String t = file.getName();
                if (t.endsWith(".png") || t.endsWith(".gif")) directory.put(t.substring(0,t.lastIndexOf(".")), path);
            }
        }
    }
    protected static BufferedImage load(String dir){
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

    private static BufferedImage getTexture(BufferedImage image, int sx, int sy, int px, int py) {
        BufferedImage result = image.getSubimage(px * sx, py * sy, sx, sy);
        return result;
    }

    public enum Shader{
        XYLinearGradient, Monocolor, Invert, Cartoon
    }

    public void setShader(Shader shader) {
        shade = clone(image);
        int width = shade.getWidth();
        int height = shade.getHeight();
        switch (shader) {
            case XYLinearGradient:
                for (int y = 0; y < height; y++) {
                    float yShade = ((float) y / height) * 80;
                    for (int x = 0; x < width; x++) {
                        int rgb = shade.getRGB(x, y);
                        int alpha = (rgb & 0xff000000) >>> 24;
                        if (alpha != 0) {
                            float xShade = ((float) x / width) * 25;
                            shade.setRGB(x, y, new Color(0, 0, 0, (int) Global.clamp(Math.max(xShade, yShade), 0, 255)).getRGB());
                        }
                    }
                }
                break;
            case Invert:
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int rgb = shade.getRGB(x, y);
                        int alpha = (rgb & 0xff000000) >>> 24;
                        int red = (rgb & 0xff0000) >>> 16;
                        int green = (rgb & 0xff00) >>> 8;
                        int blue = (rgb & 0xff);
                        shade.setRGB(x, y, new Color(255-red, 255-green, 255-blue, alpha).getRGB());
                    }
                }
                break;
            case Monocolor:
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int rgb = shade.getRGB(x, y);
                        int alpha = (rgb & 0xff000000) >>> 24;
                        int red = (rgb & 0xff0000) >>> 16;
                        int green = (rgb & 0xff00) >>> 8;
                        int blue = (rgb & 0xff);
                        int mid = (red+green+blue)/3;
                        shade.setRGB(x, y, new Color(mid,mid,mid, alpha).getRGB());
                    }
                }
                break;
            case Cartoon:
                boolean test = true;
                for (int y = 1; y < height-1; y++) {
                    for (int x = 0; x < width-1; x++) {
                        int rgb = shade.getRGB(x, y);
                        int rgb2 = shade.getRGB(x, y-1);
                        int rgb3 = shade.getRGB(x+1, y);
                        int rgb4 = shade.getRGB(x, y+1);
                        int alpha = (rgb & 0xff000000) >>> 24;
                        int alpha2 = (rgb2 & 0xff000000) >>> 24;
                        int alpha3 = (rgb3 & 0xff000000) >>> 24;
                        int alpha4 = (rgb4 & 0xff000000) >>> 24;
                        if (alpha == 0) test = true;
                        if (alpha != 0 && (test || alpha2 == 0 || alpha3 == 0 || alpha4 == 0)) {
                            shade.setRGB(x, y, new Color(0, 0, 0, 255).getRGB());
                            test = false;
                        }
                    }
                }
        }
    }
}