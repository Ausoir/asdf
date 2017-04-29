package com.ga.map;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Ga on 1/14/2017.
 */
public enum Shader{
    None(new ShaderRenderer()),
    XYLinearGradient(new ShaderRenderer(){
        protected BufferedImage render(BufferedImage image, int param){
            int width = image.getWidth();
            int height = image.getHeight();
            BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < height; y++) {
                float yShade = ((float) y / height) * 64;
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    int alpha = (rgb & 0xff000000) >>> 24;
                    if (alpha != 0) {
                        float xShade = ((float) x / width) * 24;
                        result.setRGB(x, y, new Color(0, 0, 0, (int) Global.clamp(Math.max(xShade, yShade), 0, 255)).getRGB());
                    }
                }
            }
            return result;
        }
    }),
    Color(new ShaderRenderer(){
        protected BufferedImage render(BufferedImage image, int param){
            int width = image.getWidth();
            int height = image.getHeight();
            BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            int color = param;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    int alpha = (rgb & 0xff000000) >>> 24;
                    if (alpha != 0) {
                        result.setRGB(x, y, color);
                    }
                }
            }
            return result;
        }
    }),
    Invert(new ShaderRenderer(){
        protected BufferedImage render(BufferedImage image, int param){
            int width = image.getWidth();
            int height = image.getHeight();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    int alpha = (rgb & 0xff000000) >>> 24;
                    int red = (rgb & 0xff0000) >>> 16;
                    int green = (rgb & 0xff00) >>> 8;
                    int blue = (rgb & 0xff);
                    image.setRGB(x, y, new Color(255-red, 255-green, 255-blue, alpha).getRGB());
                }
            }
            return null;
        }
    }),
    Monocolor(new ShaderRenderer(){
        protected BufferedImage render(BufferedImage image, int param){
            int width = image.getWidth();
            int height = image.getHeight();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    int alpha = (rgb & 0xff000000) >>> 24;
                    int red = (rgb & 0xff0000) >>> 16;
                    int green = (rgb & 0xff00) >>> 8;
                    int blue = (rgb & 0xff);
                    int mid = (red+green+blue)/3;
                    image.setRGB(x, y, new Color(mid,mid,mid, alpha).getRGB());
                }
            }
            return null;
        }
    }),
    Cartoon(new ShaderRenderer(){
        protected BufferedImage render(BufferedImage image, int param){
            int width = image.getWidth();
            int height = image.getHeight();
            boolean test = true;
            for (int y = 1; y < height-1; y++) {
                for (int x = 0; x < width-1; x++) {
                    int rgb = image.getRGB(x, y);
                    int rgb2 = image.getRGB(x, y-1);
                    int rgb3 = image.getRGB(x + 1, y);
                    int rgb4 = image.getRGB(x, y + 1);
                    int alpha = (rgb & 0xff000000) >>> 24;
                    int alpha2 = (rgb2 & 0xff000000) >>> 24;
                    int alpha3 = (rgb3 & 0xff000000) >>> 24;
                    int alpha4 = (rgb4 & 0xff000000) >>> 24;
                    if (alpha == 0) test = true;
                    if (alpha != 0 && (test || alpha2 == 0 || alpha3 == 0 || alpha4 == 0 || x == width-2 || y == height-2 || x == 0 || y == 1)) {
                        image.setRGB(x, y, new Color(0, 0, 0, 255).getRGB());
                        test = false;
                    }
                }
            }
            return null;
        }
    }),
    Basic(new ShaderRenderer(){
        protected BufferedImage render(BufferedImage image, int x1, int x2, int y1, float slope){
            int y2 = image.getHeight()-1;
            for (int ix = x1; ix < x2; ix++) {
                int ty = y1 + (int) ((ix - x1) * slope);
                for (int iy = -1; iy < 24; iy++) {
                    int y = Math.min(ty+iy,y2);
                    if (y >= 0) {
                        int rgb = image.getRGB(ix, y);
                        int shadeMag = 24 - iy;
                        int alpha = (rgb & 0xff000000) >>> 24;
                        int red = Math.min(((rgb & 0xff0000) >>> 16) + shadeMag, 255);
                        int green = Math.min(((rgb & 0xff00) >>> 8) + shadeMag, 255);
                        int blue = Math.min(((rgb & 0xff)) + shadeMag, 255);
                        image.setRGB(ix, y, new Color(red, green, blue, alpha).getRGB());
                    }
                }
            }
            return null;
        }
    }),
    Grass(new ShaderRenderer(){
        protected BufferedImage render(BufferedImage image, int x1, int x2, int y1, float slope){
            int y2 = image.getHeight()-1;
            for (int ix = x1; ix < x2; ix++) {
                int ty = y1 + (int) ((ix - x1) * slope);
                for (int iy = -1; iy < 50; iy++) {
                    int y = Math.min(ty+iy,y2);
                    if (y >= 0) {
                        int rgb = image.getRGB(ix, y);
                        int shadeMag = -(150 - (iy * 3));
                        int alpha = (rgb & 0xff000000) >>> 24;
                        int red = Math.max(((rgb & 0xff0000) >>> 16) + shadeMag, 0);
                        int green = Math.max(((rgb & 0xff00) >>> 8), 0);
                        int blue = Math.max(((rgb & 0xff)) + shadeMag, 0);
                        image.setRGB(ix, y, new Color(red, green, blue, alpha).getRGB());
                    }
                }
            }
            return null;
        }
    }),
    Sand(new ShaderRenderer(){
        protected BufferedImage render(BufferedImage image, int x1, int x2, int y1, float slope){
            int y2 = image.getHeight()-1;
            for (int ix = x1; ix < x2; ix++) {
                int ty = y1 + (int) ((ix - x1) * slope);
                for (int iy = -1; iy < 75; iy++) {
                    int y = Math.min(ty+iy,y2);
                    if (y >= 0) {
                        int rgb = image.getRGB(ix, y);
                        int shadeMag = (225 - (iy * 3));
                        int alpha = (rgb & 0xff000000) >>> 24;
                        int randMax = Global.r.nextInt(20);
                        int red = (int) Global.clamp(((rgb & 0xff0000) >>> 16) + shadeMag, 0, 225 + randMax);
                        int green = (int) Global.clamp(((rgb & 0xff00) >>> 8) + shadeMag, 0, 210 + randMax);
                        int blue = (int) Global.clamp(((rgb & 0xff)) + shadeMag, 0, 60 + randMax);
                        image.setRGB(ix, y, new Color(red, green, blue, alpha).getRGB());
                        new Color(235, 224, 60,100);
                    }
                }
            }
            return null;
        }
    }),
    Snow(new ShaderRenderer(){
        protected BufferedImage render(BufferedImage image, int x1, int x2, int y1, float slope){
            int y2 = image.getHeight()-1;
            for (int ix = x1; ix < x2; ix++) {
                int ty = y1 + (int) ((ix - x1) * slope);
                for (int iy = -1; iy < 75; iy++) {
                    int y = Math.min(ty+iy,y2);
                    if (y >= 0) {
                        int rgb = image.getRGB(ix, y);
                        int shadeMag = (225 - (iy * 3));
                        int alpha = (rgb & 0xff000000) >>> 24;
                        int randMax = Global.r.nextInt(10);
                        int red = (int) Global.clamp(((rgb & 0xff0000) >>> 16) + shadeMag, 0, 215 + randMax);
                        int green = (int) Global.clamp(((rgb & 0xff00) >>> 8) + shadeMag, 0, 215 + randMax);
                        int blue = (int) Global.clamp(((rgb & 0xff)) + shadeMag, 0, 215 + randMax);
                        image.setRGB(ix, y, new Color(red, green, blue, alpha).getRGB());
                    }
                }
            }
            return null;
        }
    }),
    Belt(new ShaderRenderer(){
        protected BufferedImage render(BufferedImage image, int x1, int x2, int y1, float slope){
            int y2 = image.getHeight()-1;
            for (int ix = x1; ix < x2; ix++) {
                int ty = y1 + (int) ((ix - x1) * slope);
                for (int iy = -1; iy < 30; iy++) {
                    int y = Math.min(ty+iy,y2);
                    if (y >= 0) {
                        int rgb = image.getRGB(ix, y);
                        int shadeMag = (90 - (iy * 3));
                        int alpha = (rgb & 0xff000000) >>> 24;
                        int randMax = (int) (Global.r.nextInt(10) * Math.cos(ix * .2f));
                        int red = (int) Global.clamp(((rgb & 0xff0000) >>> 16) + shadeMag, 0, 140 + randMax);
                        int green = (int) Global.clamp(((rgb & 0xff00) >>> 8) + shadeMag, 0, 90 + randMax);
                        int blue = (int) Global.clamp(((rgb & 0xff)) + shadeMag, 0, 60 + randMax);
                        image.setRGB(ix, y, new Color(red, green, blue, alpha).getRGB());
                    }
                }
            }
            return null;
        }
    }),
    Road(new ShaderRenderer(){
        protected BufferedImage render(BufferedImage image, int x1, int x2, int y1, float slope){
            int y2 = image.getHeight()-1;
            for (int ix = x1; ix < x2; ix++) {
                int ty = y1 + (int) ((ix - x1) * slope);
                for (int iy = -1; iy < 20; iy++) {
                    int y = Math.min(ty+iy,y2);
                    if (y >= 0) {
                        int rgb = image.getRGB(ix, y);
                        int shadeMag = (60 - (iy * 3));
                        int alpha = (rgb & 0xff000000) >>> 24;
                        int randMax = Global.r.nextInt(16);
                        int red = (int) Global.clamp(((rgb & 0xff0000) >>> 16) + shadeMag, 0, 140 + randMax);
                        int green = (int) Global.clamp(((rgb & 0xff00) >>> 8) + shadeMag, 0, 90 + randMax);
                        int blue = (int) Global.clamp(((rgb & 0xff)) + shadeMag, 0, 60 + randMax);
                        image.setRGB(ix, y, new Color(red, green, blue, alpha).getRGB());
                    }
                }
            }
            return null;
        }
    }),
    Tile(new ShaderRenderer(){
        protected BufferedImage render(BufferedImage image, int x1, int x2, int y1, float slope){
            int y2 = image.getHeight()-1;
            for (int ix = x1; ix < x2; ix++) {
                int ty = y1 + (int) ((ix - x1) * slope);
                for (int iy = -1; iy < 75; iy++) {
                    int y = Math.min(ty+iy,y2);
                    if (y >= 0) {
                        int rgb = image.getRGB(ix, y);
                        int shadeMag = (225 - (iy * 3));
                        int alpha = (rgb & 0xff000000) >>> 24;
                        int randMax = (int) (Global.r.nextInt(10) * Math.sin(iy * .2f) * Math.cos(ix * .2f));
                        int red = (int) Global.clamp(((rgb & 0xff0000) >>> 16) + shadeMag, 0, 200 + randMax);
                        int green = (int) Global.clamp(((rgb & 0xff00) >>> 8) + shadeMag, 0, 200 + randMax);
                        int blue = (int) Global.clamp(((rgb & 0xff)) + shadeMag, 0, 200 + randMax);
                        image.setRGB(ix, y, new Color(red, green, blue, alpha).getRGB());
                    }
                }
            }
            return null;
        }
    });
    private ShaderRenderer shader;
    Shader(ShaderRenderer p){
        shader = p;
    }
    protected void render(BufferedImage image, int x1, int x2, int y1, int y2, float slope){
        shader.render(image, x1, x2, y1, slope);
        if (shader != None.shader) postRender(image, x1, x2, y1, y2, slope);
    }
    private void postRender(BufferedImage image, int x1, int x2, int y1, int y2, float slope){ //add textures to edges in terrain
        int height = image.getHeight()-1;
        if (x2 < x1) {
            for (int ix = x2; ix < x1; ix++) {
                int ty = y1 + (int) ((ix - x1) * slope);
                for (int iy = ty - 24; iy <= ty; iy++) {
                    int y = (int) Global.clamp(iy,0,height);
                    int shadeMag = 24 + y - ty;
                    if (shadeMag > 0) {
                        int rgb = image.getRGB(ix, y);
                        int alpha = (rgb & 0xff000000) >>> 24;
                        int red = (int) Global.clamp(((rgb & 0xff0000) >>> 16) - shadeMag, 0, 255);
                        int green = (int) Global.clamp(((rgb & 0xff00) >>> 8) - shadeMag, 0, 255);
                        int blue = (int) Global.clamp(((rgb & 0xff)) - shadeMag, 0, 255);
                        image.setRGB(ix, y, new Color(red, green, blue, alpha).getRGB());
                    }
                }
            }
        } else if (x1 == x2){
            if (y1 > y2){
                int t = y1;
                y1 = y2;
                y2 = t;
            }
            int width = Math.min(image.getWidth(), x1+12);
            for (int ix = x1; ix < width; ix++) {
                for (int iy = y1; iy < y2; iy++) {
                    int y = (int) Global.clamp(iy,0,height);
                    int rgb = image.getRGB(ix, y);
                    int shadeMag = 12 - (ix - x1);
                    int alpha = (rgb & 0xff000000) >>> 24;
                    int red = (int) Global.clamp(((rgb & 0xff0000) >>> 16) - shadeMag, 0, 255);
                    int green = (int) Global.clamp(((rgb & 0xff00) >>> 8) - shadeMag, 0, 255);
                    int blue = (int) Global.clamp(((rgb & 0xff)) - shadeMag, 0, 255);
                    image.setRGB(ix, y, new Color(red,green,blue,alpha).getRGB());
                }
            }
            width = Math.max(0, x1-12);
            for (int ix = width; ix < x1; ix++) {
                for (int iy = y1; iy < y2; iy++) {
                    int y = (int) Global.clamp(iy,0,height);
                    int rgb = image.getRGB(ix, y);
                    int shadeMag = 12 + (ix - x1);
                    int alpha = (rgb & 0xff000000) >>> 24;
                    int red = (int) Global.clamp(((rgb & 0xff0000) >>> 16) - shadeMag, 0, 255);
                    int green = (int) Global.clamp(((rgb & 0xff00) >>> 8) - shadeMag, 0, 255);
                    int blue = (int) Global.clamp(((rgb & 0xff)) - shadeMag, 0, 255);
                    image.setRGB(ix, y, new Color(red,green,blue,alpha).getRGB());
                }
            }
        }
    }
    protected BufferedImage render(BufferedImage image, int p){
        return shader.render(image, p);
    }
    public static Shader getByName(String p){
        if (p != null) {
            for (Shader s : values()) {
                if (s.name().toLowerCase().equals(p.toLowerCase())) {
                    return s;
                }
            }
        }
        return None;
    }
}

class ShaderRenderer {
    protected BufferedImage render(BufferedImage image, int x1, int x2, int y1, float slope){
        return render(image, 0);
    }
    protected BufferedImage render(BufferedImage image, int param){
        return null;
    }
}