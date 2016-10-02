package com.ga.game;

import com.ga.tool.P;
import com.ga.unit.Thing;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ga on 5/6/2016.
 */
public class Texture {
    private static HashMap<String,BufferedImage> cache = new HashMap<>();
    private static HashMap<String,String> directory = FileManager.readTextureDirectory("Texture.dir");
    public static BufferedImage load(String dir){
        BufferedImage image = cache.get(dir);
        if (image != null) return image;
        try {
//            if (dir.split("/")[0].equals("map")) p.image = imageToBufferedImage(ImageIO.read(new File("Data/"+dir)).getScaledInstance(4000,2000,Image.SCALE_FAST));
            image = ImageIO.read(new File("Data/"+directory.get(dir) + ".png"));

            cache.put(dir,image);
//            ArrayList<P> collider = new ArrayList<>();
//            ArrayList<P> collidee = new ArrayList<>();
//            getCollision(p.image, collider, collidee);
//            p.collidee = collidee.toArray(new P[collidee.size()]);
//            p.collider = collider.toArray(new P[collider.size()]);
        } catch (IOException e) {
            System.out.println("Can't read file ["+dir + "]");
            e.printStackTrace();
        }
        return image;
    }

    public static BufferedImage getTexture(BufferedImage image, int sx, int sy, int px, int py) {
        BufferedImage result = image.getSubimage(px * sx, py * sy, sx, sy);
        return result;
    }

    public static void getCollision(BufferedImage image, ArrayList<P> collider, ArrayList<P> collidee) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int  clr   = image.getRGB(x, y);
                int  red   = (clr & 0x00ff0000) >> 16;
                int  green = (clr & 0x0000ff00) >> 8;
                int  blue  =  clr & 0x000000ff;
                if (clr == -20791){
                    collider.add(new P(x, y));
                    image.setRGB(x, y, image.getRGB(x, y-1));
                }else if (clr == -255){
                    collidee.add(new P(x, y));
                    image.setRGB(x, y, image.getRGB(x, y-1));
                }
            }
        }
    }

    private static BufferedImage imageToBufferedImage(final Image image) {
        final BufferedImage bufferedImage = new BufferedImage(
                image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return bufferedImage;
    }

    public static BufferedImage makeColorTransparent(final BufferedImage im,
                                                     final Color color) {
        final ImageFilter filter = new RGBImageFilter() {
            public int markerRGB = color.getRGB();
            public final int filterRGB(final int x, final int y, final int rgb) {
                if ((rgb) == markerRGB) {
                    return 0x00FFFFFF;
                } else {
                    return rgb;
                }
            }
        };

        final ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
        return imageToBufferedImage(Toolkit.getDefaultToolkit().createImage(ip));
    }
}