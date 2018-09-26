package com.ga.tool;

import com.ga.game.FileManager;
import com.ga.map.Global;
import com.ga.map.Picture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Created by Ga on 12/13/2016. asdasdasdawdadqw
 */
public class Standalone_ImageTool {
    public static void main(String[] args){
//        Global.init();
//        int max = 100;
//        Creation.seed = 199407;
//        for (int i = 0; i < max; i++) {
//            boolean town = GenerateBiome.getTown(i);
//            int humid = GenerateBiome.getHumidity(i);
//            int temp = GenerateBiome.getTemperature(i);
//            Global.p(i+" "+(town?"TOWN   humid:":"       humid:")+getInt(humid)+"   temp:"+getInt(temp));
//        }
        FileManager.init();
        Picture.init();
        String file = "Jean";
        Picture image = new Picture(new Random());
        image.set("/"+file+"/32/64/0/0");
        Picture image1 = new Picture(new Random());
        image1.set("/"+file+"/32/64/1/0");
        Picture image2 = new Picture(new Random());
        image2.set("/"+file+"/32/64/2/0");
        BufferedImage newimage = changeOrder(image.image, image1.image, image2.image);
        Picture.newImage("Data/item/armor/",file,newimage);
        Global.p("");
    }
    private static BufferedImage changeOrder(BufferedImage image,BufferedImage image1,BufferedImage image2){
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width*3, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int alpha = (rgb & 0xff000000) >>> 24;
                int red = (rgb & 0xff0000) >>> 16;
                int green = (rgb & 0xff00) >>> 8;
                int blue = (rgb & 0xff);
                result.setRGB(x, height-y-1, new Color(red,green,blue,alpha).getRGB());
            }
            for (int x = 0; x < width; x++) {
                int rgb = image1.getRGB(x, y);
                int alpha = (rgb & 0xff000000) >>> 24;
                int red = (rgb & 0xff0000) >>> 16;
                int green = (rgb & 0xff00) >>> 8;
                int blue = (rgb & 0xff);
                result.setRGB(x+width, y, new Color(red,green,blue,alpha).getRGB());
            }
            for (int x = 0; x < width; x++) {
                int rgb = image2.getRGB(x, y);
                int alpha = (rgb & 0xff000000) >>> 24;
                int red = (rgb & 0xff0000) >>> 16;
                int green = (rgb & 0xff00) >>> 8;
                int blue = (rgb & 0xff);
                result.setRGB(x+width*2, y, new Color(red,green,blue,alpha).getRGB());
            }
        }
        return result;
    }
}
