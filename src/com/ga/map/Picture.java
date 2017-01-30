package com.ga.map;

import com.ga.tool.P;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

/**
 * Created by Ga on 5/6/2016.
 */
public class Picture extends Texture {
    private BufferedImage shade;
    public int sizeRadius = 10;
    public Picture(){
        super();
    }
    private Picture(BufferedImage image){
        super(image);
        sizeRadius = (int) size.getMult(.5f,.5f).getDist();
    }
    public Picture(String name){
        this(load(name));
    }
    public Picture(String name, int index){
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
    public void draw(Graphics2D g, float x, float y, float rot, int dir, float sizeX, float sizeY){
        if (visible) {
            float xMid = size.x*dir/2;
            float yMid = size.y/2;
            float rx = x + Global.rel.x;
            float ry = y + Global.rel.y;
            x = rx - xMid;
            y = ry - yMid;
            g.rotate(rot, rx, ry);
            g.drawImage(image, (int) x, (int) y, (int) sizeX * dir, (int) sizeY, null);
            if (shade != null){
                g.drawImage(shade, (int) x, (int) y, (int) sizeX * dir, (int) sizeY, null);
            }
            g.rotate(-rot, rx, ry);
        }
    }
    public void draw(Graphics2D g, float x, float y, float rot, int dir){
        draw(g,x,y,rot,dir,size.x,size.y);
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
                    float yShade = ((float) y / height) * 64;
                    for (int x = 0; x < width; x++) {
                        int rgb = shade.getRGB(x, y);
                        int alpha = (rgb & 0xff000000) >>> 24;
                        if (alpha != 0) {
                            float xShade = ((float) x / width) * 24;
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